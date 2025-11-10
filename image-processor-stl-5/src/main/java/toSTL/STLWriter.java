package toSTL;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class STLWriter {
    
    /**
     * Write triangles to an ASCII STL file
     */
    public static void writeASCII(List<Triangle> triangles, String filename) throws IOException {
        long startTime = System.nanoTime();
        System.out.println("\n--- WRITING ASCII STL FILE ---");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("solid model");
            
            for (Triangle tri : triangles) {
                writer.printf("  facet normal %e %e %e\n", 
                    tri.normal.x, tri.normal.y, tri.normal.z);
                writer.println("    outer loop");
                writer.printf("      vertex %e %e %e\n", tri.v1.x, tri.v1.y, tri.v1.z);
                writer.printf("      vertex %e %e %e\n", tri.v2.x, tri.v2.y, tri.v2.z);
                writer.printf("      vertex %e %e %e\n", tri.v3.x, tri.v3.y, tri.v3.z);
                writer.println("    endloop");
                writer.println("  endfacet");
            }
            
            writer.println("endsolid model");
        }
        
        long writeTime = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("[TIMING] ASCII STL writing: " + writeTime + " ms");
    }
    
    /**
     * Write triangles to a binary STL file (recommended - much smaller files)
     */
    public static void writeBinary(List<Triangle> triangles, String filename) throws IOException {
        long startTime = System.nanoTime();
        System.out.println("\n--- WRITING BINARY STL FILE ---");
        
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(filename)))) {
            
            // 80-byte header
            byte[] header = new byte[80];
            String headerText = "Binary STL - Image to STL Converter - Nick Radonic 2025";
            System.arraycopy(headerText.getBytes(), 0, header, 0, 
                Math.min(headerText.length(), 80));
            out.write(header);
            
            // Number of triangles
            writeIntLE(out, triangles.size());
            
            // Triangle data
            for (Triangle tri : triangles) {
                writeFloatLE(out, tri.normal.x);
                writeFloatLE(out, tri.normal.y);
                writeFloatLE(out, tri.normal.z);
                
                writeFloatLE(out, tri.v1.x);
                writeFloatLE(out, tri.v1.y);
                writeFloatLE(out, tri.v1.z);
                
                writeFloatLE(out, tri.v2.x);
                writeFloatLE(out, tri.v2.y);
                writeFloatLE(out, tri.v2.z);
                
                writeFloatLE(out, tri.v3.x);
                writeFloatLE(out, tri.v3.y);
                writeFloatLE(out, tri.v3.z);
                
                // Attribute byte count (unused)
                out.writeShort(0);
            }
        }
        
        long writeTime = (System.nanoTime() - startTime) / 1_000_000;
        File file = new File(filename);
        double fileSizeMB = file.length() / (1024.0 * 1024.0);
        System.out.println("[TIMING] Binary STL writing: " + writeTime + " ms");
        System.out.println("File size: " + String.format("%.2f", fileSizeMB) + " MB");
    }
    
    private static void writeFloatLE(DataOutputStream out, float value) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(value);
        out.write(buffer.array());
    }
    
    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value);
        out.write(buffer.array());
    }
}
