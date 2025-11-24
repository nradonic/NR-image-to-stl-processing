package imageManipulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the function log that tracks all operations.
 */
public class FunctionLog {
    private List<String> entries;
    
    public FunctionLog() {
        this.entries = new ArrayList<>();
    }
    
    public void clear() {
        entries.clear();
    }
    
    public void addEntry(String entry) {
        entries.add(entry);
    }
    
    public String getEntry(int index) {
        if (index >= 0 && index < entries.size()) {
            return entries.get(index);
        }
        return null;
    }
    
    public int size() {
        return entries.size();
    }
    
//    public List<String> getAllEntries() {
//        return new ArrayList<>(entries);
//    }
//
    /**
     * Extracts the sequence number from a log entry.
     * Format: "FunctionName - N" or "FunctionName - N (from M)"
     */
    public int extractSequenceNumber(String logEntry) {
        String[] parts = logEntry.split(" - ");
        if (parts.length >= 2) {
            String seqPart = parts[1].split(" ")[0];
            try {
                return Integer.parseInt(seqPart);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Extracts the function name from a log entry.
     * Format: "FunctionName - N" or "FunctionName - N (from M)"
     */
    public String extractFunctionName(String logEntry) {
        String[] parts = logEntry.split(" - ");
        if (parts.length >= 1) {
            return parts[0];
        }
        return "Unknown";
    }
    
    /**
     * Extracts the source sequence number from a log entry.
     * Format: "FunctionName - N (from M)" - returns M, or 0 if no source
     */
    public int extractSourceSequence(String logEntry) {
        if (logEntry.contains("(from ")) {
            try {
                int fromIndex = logEntry.indexOf("(from ") + 6;
                int endIndex = logEntry.indexOf(")", fromIndex);
                if (endIndex > fromIndex) {
                    return Integer.parseInt(logEntry.substring(fromIndex, endIndex));
                }
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}
