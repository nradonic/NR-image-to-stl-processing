package imageManipulation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages image data, history, and current state.
 * Uses byte[][][] for 4x memory reduction (86 MB vs 343 MB for 6000x5000 images)
 */
public class ImageData {
    private BufferedImage currentImage; // [height][width][RGB]
    private int currentSequenceNumber;
    private List<BufferedImage> imageHistory;

    public ImageData() {
        this.currentSequenceNumber = 0;
        this.imageHistory = new ArrayList<>();
        imageHistory.add(null); // Index 0 unused
    }

    public void reset() {
        currentImage = null;
        imageHistory.clear();
        imageHistory.add(null); // Index 0 unused
        currentSequenceNumber = 0;
        System.gc(); // Suggest garbage collection after reset
    }

    public void setInitialImage(BufferedImage image) {
        reset();
        addProcessedImage(image);
        logMemory("After loading image : <" + currentSequenceNumber + "> ");
    }

    public void addProcessedImage(BufferedImage image) {
        currentImage = image;
        imageHistory.add(image);
        currentSequenceNumber = getNextSequenceNumber();
        logMemory("After adding image : <" + currentSequenceNumber + "> ");

    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public int getCurrentSequenceNumber() {
        return currentSequenceNumber;
    }

    public void setCurrentSequenceNumber(int seqNum) {
        this.currentSequenceNumber = seqNum;
        if (seqNum > 0 && seqNum < imageHistory.size()) {
            currentSequenceNumber = seqNum;
            currentImage = imageHistory.get(seqNum);
        }
    }

    public int getNextSequenceNumber() {
        return imageHistory.size();
    }

    public BufferedImage getImageBySequence(int seqNum) {
        if (seqNum > 0 && seqNum < imageHistory.size()) {
            return imageHistory.get(seqNum);
        }
        return null;
    }

    public boolean hasImage() {
        return currentImage != null;
    }

    /**
     * Log current memory usage to help diagnose issues.
     */
    private void logMemory(String context) {
        Runtime runtime = Runtime.getRuntime();
        long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMB = runtime.maxMemory() / 1024 / 1024;
        long percentUsed = (usedMB * 100) / maxMB;
        System.out.println(String.format("[MEMORY] %s: %d MB / %d MB (%d%%) - %d images in history\n",
                context, usedMB, maxMB, percentUsed, imageHistory.size() - 1));
    }
}
