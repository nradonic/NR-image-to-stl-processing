package imageManipulation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all image display windows including gathering functionality.
 */
public class WindowManager {
    private static final int GATHER_WIDTH = 800;
    private static final int GATHER_HEIGHT = 600;
    private static final int GATHER_START_X = 100;
    private static final int GATHER_START_Y = 100;
    private static final int GATHER_OFFSET_X = 30;
    private static final int GATHER_OFFSET_Y = 30;

    private final List<JFrame> displayWindows;
    private final Map<Integer, ImageDisplayWindow> windowsBySequence; // Track windows by sequence number
    private String fileName = "";

    public WindowManager() {
        this.displayWindows = new ArrayList<>();
        this.windowsBySequence = new HashMap<>();
    }

    public void addWindow(JFrame window) {
        displayWindows.add(window);
    }

    public void clear() {
        displayWindows.forEach(item -> item.dispose());
        displayWindows.clear();
        windowsBySequence.clear();
    }

    public ImageDisplayWindow createAndShowWindow(BufferedImage image, String functionName,
                                                  int seqNum, int sourceSeqNum, String fileName) {
        this.fileName = fileName;
        return createAndShowWindow(image, functionName, seqNum, sourceSeqNum);
    }

    public ImageDisplayWindow createAndShowWindow(BufferedImage image, String functionName,
                                                  int seqNum, int sourceSeqNum) {
        ImageDisplayWindow window = new ImageDisplayWindow(image, functionName, seqNum, sourceSeqNum, fileName);
        window.setVisible(true);
        addWindow(window);
        windowsBySequence.put(seqNum, window);
        return window;
    }

    /**
     * Check if a window for the given sequence number is open.
     */
    public boolean isWindowOpen(int seqNum) {
        ImageDisplayWindow window = windowsBySequence.get(seqNum);
        return window != null && window.isDisplayable() && window.isVisible();
    }

    /**
     * Get the window for a given sequence number, or null if not found/closed.
     */
    public ImageDisplayWindow getWindow(int seqNum) {
        ImageDisplayWindow window = windowsBySequence.get(seqNum);
        if (window != null && window.isDisplayable() && window.isVisible()) {
            return window;
        }
        return null;
    }

    public int gatherWindows() {
        if (displayWindows.isEmpty()) {
            return 0;
        }

        int startX = GATHER_START_X;
        int startY = GATHER_START_Y;
        int currentX = startX;
        int currentY = startY;

        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxX = screenSize.width - GATHER_WIDTH - 50;
        int maxY = screenSize.height - GATHER_HEIGHT - 50;

        int gatheredCount = 0;

        for (JFrame frame : displayWindows) {
            if (frame.isDisplayable() && frame.isVisible()) {
                // Set to middle scale size
                frame.setSize(GATHER_WIDTH, GATHER_HEIGHT);

                // Position in cascade
                frame.setLocation(currentX, currentY);

                // Bring to front
                frame.toFront();

                // Calculate next position
                currentX += GATHER_OFFSET_X;
                currentY += GATHER_OFFSET_Y;

                // Wrap around if going off screen
                if (currentX > maxX || currentY > maxY) {
                    currentX = startX + (GATHER_OFFSET_X * 2);
                    currentY = startY + (GATHER_OFFSET_Y * 2);
                    startX += (GATHER_OFFSET_X * 2);
                    startY += (GATHER_OFFSET_Y * 2);
                }

                gatheredCount++;
            }
        }

        return gatheredCount;
    }
}
