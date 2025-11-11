package toSTL;

import java.util.ArrayList;
import java.util.List;

public class VoxelToSTL {
    private final boolean[][][] voxels;
    private final float voxelSize;
    private final List<Triangle> triangles;
    private final float zScale = 64;

    public VoxelToSTL(boolean[][][] voxels, float voxelSize) {
        this.voxels = voxels;
        this.voxelSize = voxelSize;
        this.triangles = new ArrayList<>();
    }

    public List<Triangle> convert() {
        long startTime = System.nanoTime();
        triangles.clear();

        int xSize = voxels.length;
        int ySize = voxels[0].length;
        int zSize = voxels[0][0].length;

        System.out.println("\n--- HEIGHT FIELD TO STL MESH GENERATION ---");
        System.out.println("Grid: " + xSize + " x " + ySize);

        // Calculate heights
        float[][] heights = new float[xSize][ySize];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                int voxelCount = 0;
                for (int z = 0; z < zSize; z++) {
                    if (voxels[x][y][z]) voxelCount++;
                }
                heights[x][y] = voxelCount * voxelSize / zScale;
            }
        }

        // Track which quads exist (all 4 corners non-zero)
        boolean[][] quadExists = new boolean[xSize - 1][ySize - 1];

        // Generate top and bottom surfaces
        for (int x = 0; x < xSize - 1; x++) {
            for (int y = 0; y < ySize - 1; y++) {
                float z00 = heights[x][y];
                float z10 = heights[x + 1][y];
                float z01 = heights[x][y + 1];
                float z11 = heights[x + 1][y + 1];

                // Skip unless ALL corners are non-zero
                if (z00 <= 0 || z10 <= 0 || z01 <= 0 || z11 <= 0) {
                    continue;
                }

                quadExists[x][y] = true;

                float x0 = x * voxelSize;
                float y0 = y * voxelSize;
                float x1 = (x + 1) * voxelSize;
                float y1 = (y + 1) * voxelSize;

                // Top surface
                Vector3 v00 = new Vector3(x0, y0, z00);
                Vector3 v10 = new Vector3(x1, y0, z10);
                Vector3 v01 = new Vector3(x0, y1, z01);
                Vector3 v11 = new Vector3(x1, y1, z11);

                Vector3 n1 = calculateNormal(v00, v10, v11);
                Vector3 n2 = calculateNormal(v00, v11, v01);

                triangles.add(new Triangle(n1, v00, v10, v11));
                triangles.add(new Triangle(n2, v00, v11, v01));

                // Bottom surface
                Vector3 b00 = new Vector3(x0, y0, 0);
                Vector3 b10 = new Vector3(x1, y0, 0);
                Vector3 b01 = new Vector3(x0, y1, 0);
                Vector3 b11 = new Vector3(x1, y1, 0);

                Vector3 bn = new Vector3(0, 0, -1);
                triangles.add(new Triangle(bn, b00, b11, b10));
                triangles.add(new Triangle(bn, b00, b01, b11));
            }
        }

        // Generate walls for exposed edges of each quad
        for (int x = 0; x < xSize - 1; x++) {
            for (int y = 0; y < ySize - 1; y++) {
                if (!quadExists[x][y]) continue;

                float x0 = x * voxelSize;
                float y0 = y * voxelSize;
                float x1 = (x + 1) * voxelSize;
                float y1 = (y + 1) * voxelSize;

                float z00 = heights[x][y];
                float z10 = heights[x + 1][y];
                float z01 = heights[x][y + 1];
                float z11 = heights[x + 1][y + 1];

                // Check each of 4 edges - add wall if neighbor quad doesn't exist

                // Bottom edge (y = y0, from x0 to x1)
                boolean hasBottomNeighbor = (y > 0) && quadExists[x][y - 1];
                if (!hasBottomNeighbor) {
                    Vector3 v1 = new Vector3(x0, y0, z00);
                    Vector3 v2 = new Vector3(x1, y0, z10);
                    Vector3 v3 = new Vector3(x0, y0, 0);
                    Vector3 v4 = new Vector3(x1, y0, 0);
                    Vector3 n = new Vector3(0, -1, 0);
                    triangles.add(new Triangle(n, v1, v3, v4));
                    triangles.add(new Triangle(n, v1, v4, v2));
                }

                // Top edge (y = y1, from x0 to x1)
                boolean hasTopNeighbor = (y < ySize - 2) && quadExists[x][y + 1];
                if (!hasTopNeighbor) {
                    Vector3 v1 = new Vector3(x0, y1, z01);
                    Vector3 v2 = new Vector3(x1, y1, z11);
                    Vector3 v3 = new Vector3(x0, y1, 0);
                    Vector3 v4 = new Vector3(x1, y1, 0);
                    Vector3 n = new Vector3(0, 1, 0);
                    triangles.add(new Triangle(n, v1, v4, v3));
                    triangles.add(new Triangle(n, v1, v2, v4));
                }

                // Left edge (x = x0, from y0 to y1)
                boolean hasLeftNeighbor = (x > 0) && quadExists[x - 1][y];
                if (!hasLeftNeighbor) {
                    Vector3 v1 = new Vector3(x0, y0, z00);
                    Vector3 v2 = new Vector3(x0, y1, z01);
                    Vector3 v3 = new Vector3(x0, y0, 0);
                    Vector3 v4 = new Vector3(x0, y1, 0);
                    Vector3 n = new Vector3(-1, 0, 0);
                    triangles.add(new Triangle(n, v1, v4, v3));
                    triangles.add(new Triangle(n, v1, v2, v4));
                }

                // Right edge (x = x1, from y0 to y1)
                boolean hasRightNeighbor = (x < xSize - 2) && quadExists[x + 1][y];
                if (!hasRightNeighbor) {
                    Vector3 v1 = new Vector3(x1, y0, z10);
                    Vector3 v2 = new Vector3(x1, y1, z11);
                    Vector3 v3 = new Vector3(x1, y0, 0);
                    Vector3 v4 = new Vector3(x1, y1, 0);
                    Vector3 n = new Vector3(1, 0, 0);
                    triangles.add(new Triangle(n, v1, v3, v4));
                    triangles.add(new Triangle(n, v1, v4, v2));
                }
            }
        }

        long totalTime = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("Triangles generated: " + triangles.size());
        System.out.println("[TIMING] Total: " + totalTime + " ms");

        return triangles;
    }

    private Vector3 calculateNormal(Vector3 v1, Vector3 v2, Vector3 v3) {
        Vector3 u = v2.subtract(v1);
        Vector3 v = v3.subtract(v1);
        return u.cross(v).normalize();
    }

    public String getStats() {
        return String.format("Triangles: %d, Voxel size: %.2f", triangles.size(), voxelSize);
    }
}