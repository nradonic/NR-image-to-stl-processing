package imageManipulation;

import java.awt.*;
import java.awt.image.*;

/**
 * Contains all image processing algorithms.
 * All functions use byte[][][] for memory efficiency with proper masking (& 0xFF).
 */
public class ImageProcessingFunctions {

    /**
     * Posterize an image to 4 color levels: 0, 85, 170, 255
     */
    public static BufferedImage posterize(BufferedImage source) {

        int height = source.getHeight();
        int width = source.getWidth();


        int[] srcPixels = new int[width * height];
        source.getRGB(0, 0, width, height, srcPixels, 0, width);
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < srcPixels.length; i++) {
            int p = srcPixels[i];

            // Example: posterize 'autotune' color
            int a = (p >>> 24) & 0xFF;
            int r = limit4parts(((p >>> 16) & 0xFF));
            int g = limit4parts(((p >>> 8) & 0xFF));
            int b = limit4parts((p & 0xFF));

            srcPixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        dst.setRGB(0, 0, width, height, srcPixels, 0, width);
        return dst;
    }

    static int limit4parts(int value) {
        // Round to nearest level: 0, 85, 170, 255
        int result;
        if (value < 43) {
            result = 0;
        } else if (value < 128) {
            result = 85;
        } else if (value < 213) {
            result = 170;
        } else {
            result = 255;
        }
        return result;
    }

    /**
     * Convert image to monochrome by averaging RGB components.
     */
    public static BufferedImage monochrome(BufferedImage source) {
        int height = source.getHeight();
        int width = source.getWidth();

        BufferedImage dst = new BufferedImage(width, height, source.getType());

        int[] srcPixels = ((DataBufferInt) source.getRaster().getDataBuffer()).getData();
        int[] dstPixels = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < srcPixels.length; i++) {
            int p = srcPixels[i];

            // Example: monochrome color
            int a = (p >>> 24) & 0xFF;
            int r = ((p >>> 16) & 0xFF);
            int g = ((p >>> 8) & 0xFF);
            int b = (p & 0xFF);
            int average = (r + g + b) / 3;
            dstPixels[i] = (a << 24) | (average << 16) | (average << 8) | average;
        }
        return dst;
    }

    // scale the image using clipping limits
    public static BufferedImage scaleClipping(BufferedImage source, int maxWidth, int maxHeight) {
        double oldWidth = (double) source.getWidth();
        double oldHeight = (double) source.getHeight();
        double scaleFactor = Math.min((double) (maxWidth / oldWidth), (double) (maxHeight / oldHeight));
        return copyAndScale(source, scaleFactor);
    }

    // scale image by scaleFactor
    public static BufferedImage copyAndScale(BufferedImage source, double scaleFactor) {
        int newWidth = (int) (source.getWidth() * scaleFactor);
        int newHeight = (int) (source.getHeight() * scaleFactor);

        BufferedImage scaled = copyAndScale(source, newWidth, newHeight);
        return scaled;
    }

    // scale image to given dimensions
    public static BufferedImage copyAndScale(BufferedImage source, int newWidth, int newHeight) {
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, source.getType());
        Graphics2D g = scaled.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g.drawImage(source, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }

    public static BufferedImage copyBufferedImage(BufferedImage source) {
        ColorModel cm = source.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = source.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
