package imageManipulation;

import java.awt.image.BufferedImage;

/**
 * Utility class for converting between BufferedImage and byte[][][] array format.
 * Uses byte arrays for 4x memory reduction compared to int arrays.
 */
public class ImageConverter {
    
    /**
     * Converts a BufferedImage to a 3D byte array format [height][width][RGB].
     * Each color component is stored as a signed byte (-128 to 127) but represents 0-255.
     */
    public static byte[][][] bufferedImageToArray(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        byte[][][] array = new byte[height][width][3];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                array[y][x][0] = (byte)((rgb >> 16) & 0xFF); // Red
                array[y][x][1] = (byte)((rgb >> 8) & 0xFF);  // Green
                array[y][x][2] = (byte)(rgb & 0xFF);         // Blue
            }
        }
        return array;
    }
    
    /**
     * Converts a 3D byte array [height][width][RGB] to a BufferedImage.
     * Masks bytes with 0xFF to convert signed bytes to unsigned 0-255 range.
     */
    public static BufferedImage arrayToBufferedImage(byte[][][] array) {
        int height = array.length;
        int width = array[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = array[y][x][0] & 0xFF; // Mask to convert to 0-255
                int g = array[y][x][1] & 0xFF;
                int b = array[y][x][2] & 0xFF;
                int rgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }
}
