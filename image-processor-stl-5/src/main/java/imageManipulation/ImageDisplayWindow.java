package imageManipulation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Window for displaying an image with automatic scaling and aspect ratio preservation.
 * Stores a SCALED COPY (max 1000x1000) separate from history to minimize memory usage.
 */
public class ImageDisplayWindow extends JFrame {
    private static final int MAX_DISPLAY_SIZE = 2000;
    private static final int MIN_DISPLAY_SIZE = 500;
    private static final int MAX_DISPLAY_RESOLUTION = 1000; // Max resolution for display copy

    private final BufferedImage scaledImageArray;  // Scaled copy for display only

    public ImageDisplayWindow(BufferedImage imageArray, String functionName, int seqNum, int sourceSeqNum, String fileName) {
        super(buildTitle(functionName, seqNum, sourceSeqNum, imageArray, fileName));

        // Create scaled copy immediately - breaks reference to full-resolution history
        this.scaledImageArray = createScaledCopy(imageArray);

        ImageDisplayPanel panel = new ImageDisplayPanel(scaledImageArray);
        add(panel);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Calculate initial window size based on scaled image
        int width = scaledImageArray.getWidth();
        int height = scaledImageArray.getHeight();
        Dimension displaySize = calculateDisplaySize(width, height);
        setSize(displaySize);
        setLocationRelativeTo(null);

        System.out.println("Display window created with " + height + "x" + width +
                " scaled copy (original: " + imageArray.getHeight() + "x" + imageArray.getWidth() + ")" );
    }

    /**
     * Create a scaled-down copy of the image for display only.
     * This breaks the reference to the full-resolution version in history.
     * Scales the byte[][][] directly without creating intermediate BufferedImage.
     */
    private BufferedImage createScaledCopy(BufferedImage original) {
        int origHeight = original.getHeight();
        int origWidth = original.getWidth();

        // If already small enough, make a copy to break reference
        if (origWidth <= MAX_DISPLAY_RESOLUTION && origHeight <= MAX_DISPLAY_RESOLUTION) {
            return ImageProcessingFunctions.copyBufferedImage(original);
        }

        // Direct scaling on BufferedImage - NO BufferedImage intermediate
        return ImageProcessingFunctions.scaleClipping(original, MAX_DISPLAY_RESOLUTION, MAX_DISPLAY_RESOLUTION);
    }

    private static String buildTitle(String functionName, int seqNum, int sourceSeqNum, BufferedImage imageArray, String fileName) {
        int width = imageArray.getWidth();
        int height = imageArray.getHeight();

        if (sourceSeqNum > 0) {
            return functionName + " - " + seqNum + " (from " + sourceSeqNum + ") ( " + width + " x " + height + " )";
        } else {
            return functionName + " - " + seqNum + " ( " + fileName + " )" + " ( " + width + " x " + height + " )";
        }
    }

    private Dimension calculateDisplaySize(int imgWidth, int imgHeight) {
        double aspectRatio = (double) imgWidth / imgHeight;
        int width, height;

        if (imgWidth > imgHeight) {
            width = Math.min(imgWidth, MAX_DISPLAY_SIZE);
            height = (int) (width / aspectRatio);
            if (height < MIN_DISPLAY_SIZE && imgHeight >= MIN_DISPLAY_SIZE) {
                height = MIN_DISPLAY_SIZE;
                width = (int) (height * aspectRatio);
            }
        } else {
            height = Math.min(imgHeight, MAX_DISPLAY_SIZE);
            width = (int) (height * aspectRatio);
            if (width < MIN_DISPLAY_SIZE && imgWidth >= MIN_DISPLAY_SIZE) {
                width = MIN_DISPLAY_SIZE;
                height = (int) (width / aspectRatio);
            }
        }

        // Ensure within bounds
        width = Math.min(MAX_DISPLAY_SIZE, Math.max(MIN_DISPLAY_SIZE, width));
        height = Math.min(MAX_DISPLAY_SIZE, Math.max(MIN_DISPLAY_SIZE, height));

        return new Dimension(width, height);
    }

    /**
     * Panel that displays the scaled image copy.
     * Much simpler now - just converts byte[][][] to BufferedImage and displays.
     */
    private static class ImageDisplayPanel extends JPanel {
        private final BufferedImage scaledImageArray;

        public ImageDisplayPanel(BufferedImage scaledImageArray) {
            this.scaledImageArray = scaledImageArray;
            setBackground(Color.LIGHT_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (scaledImageArray != null) {

                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imgWidth = scaledImageArray.getWidth();
                int imgHeight = scaledImageArray.getHeight();

                // Calculate scaling to maintain aspect ratio
                double scaleX = (double) panelWidth / imgWidth;
                double scaleY = (double) panelHeight / imgHeight;
                double scale = Math.min(scaleX, scaleY);

                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);

                // Center the image
                int x = (panelWidth - scaledWidth) / 2;
                int y = (panelHeight - scaledHeight) / 2;

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(scaledImageArray, x, y, scaledWidth, scaledHeight, null);
            }
        }
    }
}
