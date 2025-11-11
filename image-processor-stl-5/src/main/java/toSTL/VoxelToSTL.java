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

        // Calculate heights for each x,y position
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

        // Generate top surface
        for (int x = 0; x < xSize - 1; x++) {
            for (int y = 0; y < ySize - 1; y++) {
                float z00 = heights[x][y];
                float z10 = heights[x + 1][y];
                float z01 = heights[x][y + 1];
                float z11 = heights[x + 1][y + 1];

                // Skip if all corners are zero
                if (z00 == 0 && z10 == 0 && z01 == 0 && z11 == 0) {
                    continue;
                }

                float x0 = x * voxelSize;
                float y0 = y * voxelSize;
                float x1 = (x + 1) * voxelSize;
                float y1 = (y + 1) * voxelSize;

                Vector3 v00 = new Vector3(x0, y0, z00);
                Vector3 v10 = new Vector3(x1, y0, z10);
                Vector3 v01 = new Vector3(x0, y1, z01);
                Vector3 v11 = new Vector3(x1, y1, z11);

                Vector3 n1 = calculateNormal(v00, v10, v11);
                Vector3 n2 = calculateNormal(v00, v11, v01);

                triangles.add(new Triangle(n1, v00, v10, v11));
                triangles.add(new Triangle(n2, v00, v11, v01));
            }
        }

        // Generate bottom surface (same pattern as top)
        for (int x = 0; x < xSize - 1; x++) {
            for (int y = 0; y < ySize - 1; y++) {
                float z00 = heights[x][y];
                float z10 = heights[x + 1][y];
                float z01 = heights[x][y + 1];
                float z11 = heights[x + 1][y + 1];

                // Skip if all corners are zero
                if (z00 == 0 && z10 == 0 && z01 == 0 && z11 == 0) {
                    continue;
                }

                float x0 = x * voxelSize;
                float y0 = y * voxelSize;
                float x1 = (x + 1) * voxelSize;
                float y1 = (y + 1) * voxelSize;

                Vector3 v00 = new Vector3(x0, y0, 0);
                Vector3 v10 = new Vector3(x1, y0, 0);
                Vector3 v01 = new Vector3(x0, y1, 0);
                Vector3 v11 = new Vector3(x1, y1, 0);

                Vector3 normal = new Vector3(0, 0, -1);

                triangles.add(new Triangle(normal, v00, v11, v10));
                triangles.add(new Triangle(normal, v00, v01, v11));
            }
        }

        // Generate vertical walls between cells with different height states
        // Walls along X direction (between x and x+1)
        for (int x = 0; x < xSize - 1; x++) {
            for (int y = 0; y < ySize; y++) {
                float h1 = heights[x][y];
                float h2 = heights[x + 1][y];

                // Wall needed if one side is zero and other isn't
                boolean side1HasHeight = h1 > 0;
                boolean side2HasHeight = h2 > 0;

                if (side1HasHeight != side2HasHeight) {
                    float px = (x + 1) * voxelSize;
                    float py0 = y * voxelSize;
                    float py1 = (y + 1) * voxelSize;

                    // Get heights at both ends of the edge
                    float h_y0_left = h1;
                    float h_y0_right = h2;
                    float h_y1_left = (y + 1 < ySize) ? heights[x][y + 1] : 0;
                    float h_y1_right = (y + 1 < ySize) ? heights[x + 1][y + 1] : 0;

                    if (side1HasHeight) {
                        // Wall faces right (normal points in +X)
                        Vector3 normal = new Vector3(1, 0, 0);
                        Vector3 v1 = new Vector3(px, py0, h_y0_left);
                        Vector3 v2 = new Vector3(px, py1, h_y1_left);
                        Vector3 v3 = new Vector3(px, py0, 0);
                        Vector3 v4 = new Vector3(px, py1, 0);

                        triangles.add(new Triangle(normal, v1, v3, v4));
                        triangles.add(new Triangle(normal, v1, v4, v2));
                    } else {
                        // Wall faces left (normal points in -X)
                        Vector3 normal = new Vector3(-1, 0, 0);
                        Vector3 v1 = new Vector3(px, py0, h_y0_right);
                        Vector3 v2 = new Vector3(px, py1, h_y1_right);
                        Vector3 v3 = new Vector3(px, py0, 0);
                        Vector3 v4 = new Vector3(px, py1, 0);

                        triangles.add(new Triangle(normal, v1, v4, v3));
                        triangles.add(new Triangle(normal, v1, v2, v4));
                    }
                }
            }
        }

        // Walls along Y direction (between y and y+1)
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize - 1; y++) {
                float h1 = heights[x][y];
                float h2 = heights[x][y + 1];

                boolean side1HasHeight = h1 > 0;
                boolean side2HasHeight = h2 > 0;

                if (side1HasHeight != side2HasHeight) {
                    float px0 = x * voxelSize;
                    float px1 = (x + 1) * voxelSize;
                    float py = (y + 1) * voxelSize;

                    float h_x0_down = h1;
                    float h_x0_up = h2;
                    float h_x1_down = (x + 1 < xSize) ? heights[x + 1][y] : 0;
                    float h_x1_up = (x + 1 < xSize) ? heights[x + 1][y + 1] : 0;

                    if (side1HasHeight) {
                        // Wall faces up (normal points in +Y)
                        Vector3 normal = new Vector3(0, 1, 0);
                        Vector3 v1 = new Vector3(px0, py, h_x0_down);
                        Vector3 v2 = new Vector3(px1, py, h_x1_down);
                        Vector3 v3 = new Vector3(px0, py, 0);
                        Vector3 v4 = new Vector3(px1, py, 0);

                        triangles.add(new Triangle(normal, v1, v4, v3));
                        triangles.add(new Triangle(normal, v1, v2, v4));
                    } else {
                        // Wall faces down (normal points in -Y)
                        Vector3 normal = new Vector3(0, -1, 0);
                        Vector3 v1 = new Vector3(px0, py, h_x0_up);
                        Vector3 v2 = new Vector3(px1, py, h_x1_up);
                        Vector3 v3 = new Vector3(px0, py, 0);
                        Vector3 v4 = new Vector3(px1, py, 0);

                        triangles.add(new Triangle(normal, v1, v3, v4));
                        triangles.add(new Triangle(normal, v1, v4, v2));
                    }
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