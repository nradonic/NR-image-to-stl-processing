package imageManipulation;

import javax.swing.*;

/**
 * Main entry point for the Image Processor application.
 * Launches the imageManipulation.ImageProcessorApp on the Event Dispatch Thread.
 */
public class ImageProcessorMain {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for better integration
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look and feel
            }
            
            // Create and start the application
            ImageProcessorApp app = new ImageProcessorApp();
            app.start();
        });
    }
}
