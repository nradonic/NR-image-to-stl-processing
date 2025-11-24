package imageManipulation;

/**
 * Contains all image processing algorithms.
 * All functions use byte[][][] for memory efficiency with proper masking (& 0xFF).
 */
public class ImageProcessingFunctions {
    
    /**
     * Posterize an image to 4 color levels: 0, 85, 170, 255
     */
    public static byte[][][] posterize(byte[][][] source) {
        int height = source.length;
        int width = source[0].length;
        byte[][][] result = new byte[height][width][3];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < 3; c++) {
                    int value = source[y][x][c] & 0xFF; // Mask to get 0-255
                    // Round to nearest level: 0, 85, 170, 255
                    if (value < 43) {
                        result[y][x][c] = (byte)0;
                    } else if (value < 128) {
                        result[y][x][c] = (byte)85;
                    } else if (value < 213) {
                        result[y][x][c] = (byte)170;
                    } else {
                        result[y][x][c] = (byte)255;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Convert image to monochrome by averaging RGB components.
     */
    public static byte[][][] monochrome(byte[][][] source) {
        int height = source.length;
        int width = source[0].length;
        byte[][][] result = new byte[height][width][3];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = source[y][x][0] & 0xFF;
                int g = source[y][x][1] & 0xFF;
                int b = source[y][x][2] & 0xFF;
                int avg = (r + g + b) / 3;
                byte avgByte = (byte)avg;
                result[y][x][0] = avgByte;
                result[y][x][1] = avgByte;
                result[y][x][2] = avgByte;
            }
        }
        return result;
    }
    
    /**
     * Scale image to new dimensions using high-quality interpolation.
     * Uses bilinear interpolation for upscaling and area averaging (box filter) for downscaling.
     */
    public static byte[][][] scale(byte[][][] source, int newWidth, int newHeight) {
        int srcHeight = source.length;
        int srcWidth = source[0].length;
        byte[][][] result = new byte[newHeight][newWidth][3];
        
        // Determine if we're upscaling or downscaling
        boolean isUpscaling = (newWidth > srcWidth) || (newHeight > srcHeight);
        
        if (isUpscaling) {
            // Use bilinear interpolation for upscaling
            scaleWithBilinear(source, result, srcWidth, srcHeight, newWidth, newHeight);
        } else {
            // Use area averaging (box filter) for downscaling
            scaleWithAreaAveraging(source, result, srcWidth, srcHeight, newWidth, newHeight);
        }
        
        return result;
    }
    
    /**
     * Bilinear interpolation for upscaling - smooth gradients between pixels.
     */
    private static void scaleWithBilinear(byte[][][] source, byte[][][] result, 
                                           int srcWidth, int srcHeight, 
                                           int newWidth, int newHeight) {
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // Map destination pixel to source coordinates (floating point)
                double srcX = (x + 0.5) * srcWidth / newWidth - 0.5;
                double srcY = (y + 0.5) * srcHeight / newHeight - 0.5;
                
                // Clamp to valid range
                srcX = Math.max(0, Math.min(srcWidth - 1.001, srcX));
                srcY = Math.max(0, Math.min(srcHeight - 1.001, srcY));
                
                // Get the four nearest pixels
                int x0 = (int) Math.floor(srcX);
                int y0 = (int) Math.floor(srcY);
                int x1 = Math.min(x0 + 1, srcWidth - 1);
                int y1 = Math.min(y0 + 1, srcHeight - 1);
                
                // Calculate interpolation weights
                double wx = srcX - x0;
                double wy = srcY - y0;
                
                // Bilinear interpolation for each color channel
                for (int c = 0; c < 3; c++) {
                    double val00 = source[y0][x0][c] & 0xFF; // Mask bytes
                    double val10 = source[y0][x1][c] & 0xFF;
                    double val01 = source[y1][x0][c] & 0xFF;
                    double val11 = source[y1][x1][c] & 0xFF;
                    
                    // Interpolate horizontally
                    double valTop = val00 * (1 - wx) + val10 * wx;
                    double valBottom = val01 * (1 - wx) + val11 * wx;
                    
                    // Interpolate vertically
                    double finalVal = valTop * (1 - wy) + valBottom * wy;
                    
                    result[y][x][c] = (byte)Math.round(finalVal);
                }
            }
        }
    }
    
    /**
     * Area averaging (box filter) for downscaling - averages all contributing source pixels.
     * This produces much better quality than nearest-neighbor when reducing image size.
     */
    private static void scaleWithAreaAveraging(byte[][][] source, byte[][][] result,
                                               int srcWidth, int srcHeight,
                                               int newWidth, int newHeight) {
        // Calculate the ratio of source to destination
        double xRatio = (double) srcWidth / newWidth;
        double yRatio = (double) srcHeight / newHeight;
        
        for (int destY = 0; destY < newHeight; destY++) {
            for (int destX = 0; destX < newWidth; destX++) {
                // Calculate the source region that maps to this destination pixel
                double srcX0 = destX * xRatio;
                double srcY0 = destY * yRatio;
                double srcX1 = (destX + 1) * xRatio;
                double srcY1 = (destY + 1) * yRatio;
                
                // Get integer bounds
                int x0 = (int) Math.floor(srcX0);
                int y0 = (int) Math.floor(srcY0);
                int x1 = (int) Math.ceil(srcX1);
                int y1 = (int) Math.ceil(srcY1);
                
                // Clamp to image bounds
                x0 = Math.max(0, x0);
                y0 = Math.max(0, y0);
                x1 = Math.min(srcWidth, x1);
                y1 = Math.min(srcHeight, y1);
                
                // Average all pixels in the source region
                double[] colorSum = new double[3];
                double totalWeight = 0;
                
                for (int sy = y0; sy < y1; sy++) {
                    for (int sx = x0; sx < x1; sx++) {
                        // Calculate the weight (area of overlap)
                        double xOverlap = Math.min(srcX1, sx + 1) - Math.max(srcX0, sx);
                        double yOverlap = Math.min(srcY1, sy + 1) - Math.max(srcY0, sy);
                        double weight = xOverlap * yOverlap;
                        
                        // Add weighted contribution of this pixel (with masking)
                        for (int c = 0; c < 3; c++) {
                            colorSum[c] += (source[sy][sx][c] & 0xFF) * weight;
                        }
                        totalWeight += weight;
                    }
                }
                
                // Calculate final color by dividing by total weight
                for (int c = 0; c < 3; c++) {
                    int finalValue = (int) Math.round(colorSum[c] / totalWeight);
                    // Clamp to valid range and store as byte
                    result[destY][destX][c] = (byte)Math.max(0, Math.min(255, finalValue));
                }
            }
        }
    }
}
