package imageManipulation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Window for displaying an image with automatic scaling and aspect ratio preservation.
 */
public class ImageDisplayWindow extends JFrame {
    private static final int MAX_DISPLAY_SIZE = 2000;
    private static final int MIN_DISPLAY_SIZE = 500;
    
    private final BufferedImage image;

    public ImageDisplayWindow(int[][][] imageArray, String functionName, int seqNum, int sourceSeqNum, String fileName) {
        super(buildTitle(functionName, seqNum, sourceSeqNum, imageArray, fileName));
        
        this.image = ImageConverter.arrayToBufferedImage(imageArray);
        
        ImageDisplayPanel panel = new ImageDisplayPanel(image);
        add(panel);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Calculate initial window size
        Dimension displaySize = calculateDisplaySize(image.getWidth(), image.getHeight());
        setSize(displaySize);
        setLocationRelativeTo(null);
    }
    
    private static String buildTitle(String functionName, int seqNum, int sourceSeqNum, int[][][] imageArray, String fileName) {
        int width = imageArray[0].length;
        int height = imageArray.length;
        
        if (sourceSeqNum > 0) {
            return functionName + " - " + seqNum + " (from " + sourceSeqNum + ") ( " + width + " x " + height + " )";
        } else {
            return functionName + " - " + seqNum + " ( " + fileName + " )"+ " ( " + width + " x " + height + " )";
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
     * Panel that displays the image with aspect ratio preservation.
     */
    private static class ImageDisplayPanel extends JPanel {
        private BufferedImage image;
        
        public ImageDisplayPanel(BufferedImage image) {
            this.image = image;
            setBackground(Color.LIGHT_GRAY);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (image != null) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imgWidth = image.getWidth();
                int imgHeight = image.getHeight();
                
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
                g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);
            }
        }
    }
}
