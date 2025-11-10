package imageManipulation;

import java.awt.image.BufferedImage;

/**
 * Utility class for converting between BufferedImage and int[][][] array format.
 */
public class ImageConverter {
    
    /**
     * Converts a BufferedImage to a 3D array format [height][width][RGB].
     */
    public static int[][][] bufferedImageToArray(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][][] array = new int[height][width][3];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                array[y][x][0] = (rgb >> 16) & 0xFF; // Red
                array[y][x][1] = (rgb >> 8) & 0xFF;  // Green
                array[y][x][2] = rgb & 0xFF;         // Blue
            }
        }
        return array;
    }
    
    /**
     * Converts a 3D array [height][width][RGB] to a BufferedImage.
     */
    public static BufferedImage arrayToBufferedImage(int[][][] array) {
        int height = array.length;
        int width = array[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = Math.min(255, Math.max(0, array[y][x][0]));
                int g = Math.min(255, Math.max(0, array[y][x][1]));
                int b = Math.min(255, Math.max(0, array[y][x][2]));
                int rgb = (r << 16) | (g << 8) | b;
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }
}
