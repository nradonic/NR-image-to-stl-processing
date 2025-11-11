package toSTL;

import java.util.ArrayList;
import java.util.List;

public class VoxelToSTL {
    private final boolean[][][] voxels;
    private final float voxelSize;
    private final List<Triangle> triangles;
    private final float zScale = 64;

    public static void main(String[] args) {
        boolean[][][] voxels = new boolean[2][2][2];
        voxels[0][0][0] = true;
        voxels[0][0][1] = true;

        VoxelToSTL converter = new VoxelToSTL(voxels, 1.0f);
        List<Triangle> triangles = converter.convert();
        System.out.println(triangles.toString());
    }

    public VoxelToSTL(boolean[][][] voxels, float voxelSize) {
        this.voxels = voxels;
        this.voxelSize = voxelSize;
        this.triangles = new ArrayList<>();
    }

    /**
     * Convert voxel array to mesh triangles with timing instrumentation
     * Only creates faces that are exposed (not touching another filled voxel)
     */
    public List<Triangle> convert() {
        long startTime = System.nanoTime();
        triangles.clear();

        int xSize = voxels.length;
        int ySize = voxels[0].length;
        int zSize = 1; //voxels[0][0].length;

        System.out.println("\n--- VOXEL TO STL MESH GENERATION ---");
        System.out.println("Voxel grid: " + xSize + " x " + ySize + " x " + zSize);
        System.out.println("Voxel size: " + voxelSize + " mm");

        // Count filled voxels
        long countStart = System.nanoTime();
        int filledVoxels = 0;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                for (int z = 0; z < zSize; z++) {
                    if (voxels[x][y][z]) filledVoxels++;
                }
            }
        }
        long countTime = (System.nanoTime() - countStart) / 1_000_000;
        System.out.println("Filled voxels: " + filledVoxels);
        System.out.println("[TIMING] Voxel counting: " + countTime + " ms");

        // Generate mesh faces
        long meshStart = System.nanoTime();
        long totalVoxels = (long) xSize * ySize * zSize;
        long voxelsProcessed = 0;
        long lastReportTime = meshStart;
        long reportInterval = 1_000_000_000; // Report every 1 second

        System.out.println("\nGenerating mesh faces for voxel counts...");

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                int voxelCount = 0;
                for (boolean b : voxels[x][y]) {
                    if (b) voxelCount++;
                }

                for (int z = 0; z < zSize; z++) {
                    if (voxels[x][y][z]) {
                        addVoxelFaces(x, y, z, xSize, ySize, zSize, voxelCount);
                    }
                    voxelsProcessed++;

                    // Progress reporting every second
                    long currentTime = System.nanoTime();
                    if (currentTime - lastReportTime >= reportInterval) {
                        double percentComplete = (voxelsProcessed * 100.0) / totalVoxels;
                        long elapsedMs = (currentTime - meshStart) / 1_000_000;
                        long voxelsPerSecond = voxelsProcessed * 1000 / (elapsedMs + 1);
                        System.out.println(String.format("  Progress: %.1f%% (%d voxels, %d voxels/sec, %d triangles)",
                                percentComplete, voxelsProcessed, voxelsPerSecond, triangles.size()));
                        lastReportTime = currentTime;
                    }
                }
            }
        }

        long meshTime = (System.nanoTime() - meshStart) / 1_000_000;
        long totalTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("\nMesh generation complete:");
        System.out.println("  Triangles generated: " + triangles.size());
        System.out.println("  Triangles per filled voxel: " + String.format("%.1f", (double) triangles.size() / filledVoxels));
        System.out.println("[TIMING] Face generation: " + meshTime + " ms");
        System.out.println("[TIMING] Total conversion: " + totalTime + " ms");

        return triangles;
    }

    /**
     * Add faces for a voxel at position (x, y, z)
     * Only adds faces that are exposed to air
     */
    private void addVoxelFaces(int x, int y, int z, int xSize, int ySize, int zSize, int voxelCount) {
        float x0 = x * voxelSize;
        float y0 = y * voxelSize;
        float z0 = z * voxelSize;
        float x1 = x0 + voxelSize;
        float y1 = y0 + voxelSize;
        float z1 = z0 + voxelSize * voxelCount / zScale; // scaling triangles by color value to get height

        // Check each face and only add if exposed
        // Front face (positive Z)
        if (z == zSize - 1 || !voxels[x][y][z + 1]) {
            addQuad(
                    new Vector3(x0, y0, z1), new Vector3(x1, y0, z1),
                    new Vector3(x1, y1, z1), new Vector3(x0, y1, z1),
                    new Vector3(0, 0, 1)
            );
        }

        // Back face (negative Z)
        if (z == 0 || !voxels[x][y][z - 1]) {
            addQuad(
                    new Vector3(x1, y0, z0), new Vector3(x0, y0, z0),
                    new Vector3(x0, y1, z0), new Vector3(x1, y1, z0),
                    new Vector3(0, 0, -1)
            );
        }

        // Right face (positive X)
        if (x == xSize - 1 || !voxels[x + 1][y][z]) {
            addQuad(
                    new Vector3(x1, y0, z0), new Vector3(x1, y0, z1),
                    new Vector3(x1, y1, z1), new Vector3(x1, y1, z0),
                    new Vector3(1, 0, 0)
            );
        }

        // Left face (negative X)
        if (x == 0 || !voxels[x - 1][y][z]) {
            addQuad(
                    new Vector3(x0, y0, z1), new Vector3(x0, y0, z0),
                    new Vector3(x0, y1, z0), new Vector3(x0, y1, z1),
                    new Vector3(-1, 0, 0)
            );
        }

        // Top face (positive Y)
        if (y == ySize - 1 || !voxels[x][y + 1][z]) {
            addQuad(
                    new Vector3(x0, y1, z0), new Vector3(x1, y1, z0),
                    new Vector3(x1, y1, z1), new Vector3(x0, y1, z1),
                    new Vector3(0, 1, 0)
            );
        }

        // Bottom face (negative Y)
        if (y == 0 || !voxels[x][y - 1][z]) {
            addQuad(
                    new Vector3(x0, y0, z1), new Vector3(x1, y0, z1),
                    new Vector3(x1, y0, z0), new Vector3(x0, y0, z0),
                    new Vector3(0, -1, 0)
            );
        }
    }

    /**
     * Add a quadrilateral as two triangles
     */
    private void addQuad(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4, Vector3 normal) {
        triangles.add(new Triangle(normal, v1, v2, v3));
        triangles.add(new Triangle(normal, v1, v3, v4));
    }

    /**
     * Get statistics about the conversion
     */
    public String getStats() {
        int filledVoxels = 0;
        for (int x = 0; x < voxels.length; x++) {
            for (int y = 0; y < voxels[0].length; y++) {
                for (int z = 0; z < voxels[0][0].length; z++) {
                    if (voxels[x][y][z]) filledVoxels++;
                }
            }
        }

        return String.format("Voxels: %d filled, Triangles: %d, Voxel size: %.2f",
                filledVoxels, triangles.size(), voxelSize);
    }
}
