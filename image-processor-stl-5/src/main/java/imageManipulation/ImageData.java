package imageManipulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages image data, history, and current state.
 */
public class ImageData {
    private int[][][] currentImage; // [height][width][RGB]
    private int sequenceNumber;
    private int currentSequenceNumber;
    private List<int[][][]> imageHistory;
    
    public ImageData() {
        this.sequenceNumber = 1;
        this.currentSequenceNumber = 0;
        this.imageHistory = new ArrayList<>();
    }
    
    public void reset() {
        currentImage = null;
        sequenceNumber = 1;
        currentSequenceNumber = 1;
        imageHistory.clear();
        imageHistory.add(null); // Index 0 unused
    }
    
    public void setInitialImage(int[][][] image) {
        currentImage = image;
        currentSequenceNumber = 1;
        imageHistory.add(image); // Index 1 is the input image
        sequenceNumber = 2; // Next operation should be sequence 2
    }
    
    public void addProcessedImage(int[][][] image) {
        currentImage = image;
        imageHistory.add(image);
        currentSequenceNumber = sequenceNumber;
        sequenceNumber++;
    }
    
    public int[][][] getCurrentImage() {
        return currentImage;
    }
    
    public void setCurrentImage(int[][][] image) {
        this.currentImage = image;
    }
    
    public int getCurrentSequenceNumber() {
        return currentSequenceNumber;
    }
    
    public void setCurrentSequenceNumber(int seqNum) {
        this.currentSequenceNumber = seqNum;
    }
    
    public int getNextSequenceNumber() {
        return sequenceNumber;
    }
    
    public void incrementSequenceNumber() {
        sequenceNumber++;
    }
    
    public int[][][] getImageBySequence(int seqNum) {
        if (seqNum > 0 && seqNum < imageHistory.size()) {
            return imageHistory.get(seqNum);
        }
        return null;
    }
    
    public boolean hasImage() {
        return currentImage != null;
    }
}
