package imageManipulation;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import toSTL.VoxelToSTL;
import toSTL.DimensionDialog;
import toSTL.Triangle;
import toSTL.STLWriter;

import java.util.List;

/**
 * Main application coordinator that manages all components.
 */
public class ImageProcessorApp {
    private static final int MAX_IMAGE_DIM = 5000;
    private static final int MIN_IMAGE_DIM = 16;

    private ImageData imageData;
    private FunctionLog functionLog;
    private WindowManager windowManager;
    private ControlPanel controlPanel;

    public ImageProcessorApp() {
        this.imageData = new ImageData();
        this.functionLog = new FunctionLog();
        this.windowManager = new WindowManager();
    }

    public void start() {
        controlPanel = new ControlPanel(this);
        controlPanel.setVisible(true);
    }

    // ===== File Operations =====

    public void loadImage(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(selectedFile);
                if (img != null) {
                    int[][][] imageArray = ImageConverter.bufferedImageToArray(img);
                    int width = imageArray.length;
                    int height = imageArray[0].length;

                    imageData.reset();
                    imageData.setInitialImage(imageArray);

                    functionLog.clear();
                    windowManager.clear();
                    controlPanel.clearLog();
                    String fileName = selectedFile.getName();
                    controlPanel.setFilename(fileName);
                    controlPanel.resetSourceImage();

                    windowManager.createAndShowWindow(imageArray, "Input", 1, 0, fileName);
                    logFunction("Input - " + imageData.getCurrentSequenceNumber() + " ( " + fileName + " ) - " + width + " x " + height);
                } else {
                    showError(parent, "Failed to load image", "Error");
                }
            } catch (IOException ex) {
                showError(parent, "Error reading file: " + ex.getMessage(), "Error");
            }
        }
    }

    public void saveImage(JFrame parent) {
        if (!imageData.hasImage()) {
            showWarning(parent, "Please load an image first", "No Image");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");

        // Format selection panel
        JPanel formatPanel = new JPanel();
        formatPanel.add(new JLabel("Format:"));
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{"PNG", "JPG"});
        formatPanel.add(formatCombo);
        fileChooser.setAccessory(formatPanel);

        fileChooser.setSelectedFile(new File("output.png"));

        // Update extension when format changes
        formatCombo.addActionListener(e -> {
            String format = (String) formatCombo.getSelectedItem();
            String currentName = fileChooser.getSelectedFile().getName();
            String baseName = currentName.replaceAll("\\.(png|jpg|jpeg)$", "");
            String extension = format.equalsIgnoreCase("PNG") ? ".png" : ".jpg";
            fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), baseName + extension));
        });

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String format = (String) formatCombo.getSelectedItem();

            // Ensure correct extension
            String filename = selectedFile.getName();
            if (format.equalsIgnoreCase("PNG") && !filename.toLowerCase().endsWith(".png")) {
                selectedFile = new File(selectedFile.getParentFile(), filename + ".png");
            } else if (format.equalsIgnoreCase("JPG") && !filename.toLowerCase().endsWith(".jpg")
                    && !filename.toLowerCase().endsWith(".jpeg")) {
                selectedFile = new File(selectedFile.getParentFile(), filename + ".jpg");
            }

            try {
                BufferedImage img = ImageConverter.arrayToBufferedImage(imageData.getCurrentImage());
                boolean success = ImageIO.write(img, format, selectedFile);

                if (success) {
                    // Silent success - just log the operation
                    logFunction("Save - " + format + " - " + selectedFile.getName());
                } else {
                    showError(parent, "Failed to save image", "Save Error");
                }
            } catch (IOException ex) {
                showError(parent, "Error saving file: " + ex.getMessage(), "Save Error");
            }
        }
    }

    // ===== Processing Functions =====

    public void applyPosterize(JFrame parent) {
        if (!imageData.hasImage()) {
            showWarning(parent, "Please load an image first", "No Image");
            return;
        }

        int[][][] currentImage = imageData.getCurrentImage();
        int[][][] result = ImageProcessingFunctions.posterize(currentImage);
        int sourceSeq = imageData.getCurrentSequenceNumber();
        int newSeq = imageData.getNextSequenceNumber();

        int height = currentImage.length;
        int width = currentImage[0].length;

        imageData.addProcessedImage(result);
        windowManager.createAndShowWindow(result, "Posterize", newSeq, sourceSeq);
        String logData = "Posterize - " + newSeq + " (from " + sourceSeq + ") - " + width + " x " + height;
        logFunction(logData);
        updateSourceLabel(logData);
    }

    public void applyMonochrome(JFrame parent) {
        if (!imageData.hasImage()) {
            showWarning(parent, "Please load an image first", "No Image");
            return;
        }

        int[][][] currentImage = imageData.getCurrentImage();
        int[][][] result = ImageProcessingFunctions.monochrome(currentImage);
        int sourceSeq = imageData.getCurrentSequenceNumber();
        int newSeq = imageData.getNextSequenceNumber();
        int height = currentImage.length;
        int width = currentImage[0].length;

        imageData.addProcessedImage(result);
        windowManager.createAndShowWindow(result, "Monochrome", newSeq, sourceSeq);
        String logData = "Monochrome - " + newSeq + " (from " + sourceSeq + ") - " + width + " x " + height;
        logFunction(logData);
        updateSourceLabel(logData);
    }

    public void applyScale(JFrame parent) {
        if (!imageData.hasImage()) {
            showWarning(parent, "Please load an image first", "No Image");
            return;
        }

        int[][][] currentImage = imageData.getCurrentImage();
        int currentWidth = currentImage[0].length;
        int currentHeight = imageData.getCurrentImage().length;

        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        JTextField widthField = new JTextField(String.valueOf(currentWidth), 10);
        JTextField heightField = new JTextField(String.valueOf(currentHeight), 10);

        panel.add(new JLabel("Width (" + MIN_IMAGE_DIM + "-" + MAX_IMAGE_DIM + "):"));
        panel.add(widthField);
        panel.add(new JLabel("Height (" + MIN_IMAGE_DIM + "-" + MAX_IMAGE_DIM + "):"));
        panel.add(heightField);

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Enter new dimensions", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int newWidth = Integer.parseInt(widthField.getText());
                int newHeight = Integer.parseInt(heightField.getText());

                if (newWidth < MIN_IMAGE_DIM || newWidth > MAX_IMAGE_DIM ||
                        newHeight < MIN_IMAGE_DIM || newHeight > MAX_IMAGE_DIM) {
                    showError(parent, "Dimensions must be between " + MIN_IMAGE_DIM + " and " + MAX_IMAGE_DIM,
                            "Invalid Dimensions");
                    return;
                }

                int[][][] scaled = ImageProcessingFunctions.scale(imageData.getCurrentImage(), newWidth, newHeight);
                int sourceSeq = imageData.getCurrentSequenceNumber();
                int newSeq = imageData.getNextSequenceNumber();

                imageData.addProcessedImage(scaled);
                windowManager.createAndShowWindow(scaled, "Scale", newSeq, sourceSeq);
                String logData = "Scale - " + newSeq + " (from " + sourceSeq + ") - " + newWidth + " x " + newHeight;
                logFunction(logData);
                updateSourceLabel(logData);
            } catch (NumberFormatException ex) {
                showError(parent, "Please enter valid numbers", "Invalid Input");
            }
        }
    }

    // ===== Window Management =====

    public void gatherWindows(JFrame parent) {
        // Silent operation - just gather the windows
        windowManager.gatherWindows();
    }

    // ===== STL Export =====

    public void exportToSTL(JFrame parent) {
        if (!imageData.hasImage()) {
            showWarning(parent, "Please load an image first", "No Image");
            return;
        }

        // Get current image dimensions
        int[][][] currentImage = imageData.getCurrentImage();
        int imageHeight = currentImage.length;
        int imageWidth = currentImage[0].length;

        // Prepopulate dimension dialog with image dimensions
        // Width and height match image dimensions in pixels (as millimeters)
        // Thickness defaults to 255 (max RGB brightness value)
        DimensionDialog dimensionDialog = new DimensionDialog(imageWidth, imageHeight, 255.0);

        if (!dimensionDialog.showDialog()) {
            // User cancelled
            return;
        }

        // Apply scale percentage

        double scalePercent = dimensionDialog.getScalePercent();
        double width = dimensionDialog.getWidth() * (scalePercent / 100.0);
        ;
        double height = dimensionDialog.getHeight() * (scalePercent / 100.0);
        ;
        double thickness = dimensionDialog.getThickness() * (scalePercent / 100.0);
        ;

        boolean invertHeights = dimensionDialog.isInvertHeights();
        boolean flipLeftRight = dimensionDialog.isFlipLeftRight();
        final int sourceNumber = imageData.getCurrentSequenceNumber();


        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save STL File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("STL Files", "stl"));
        fileChooser.setSelectedFile(new File("output.stl"));

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Ensure .stl extension
            String filename = selectedFile.getName();
            if (!filename.toLowerCase().endsWith(".stl")) {
                selectedFile = new File(selectedFile.getParentFile(), filename + ".stl");
            }

            System.out.println("\n========================================");
            System.out.println("STARTING STL EXPORT PROCESS");
            System.out.println("========================================");
            System.out.println("Image size: " + imageWidth + " x " + imageHeight + " pixels");
            System.out.println("Source: Image " + sourceNumber);
            System.out.println("Output file: " + selectedFile.getName());
            System.out.println("Scale: " + scalePercent + "%");
            System.out.println("Final dimensions: " + String.format("%.2f x %.2f x %.2f mm", width, height, thickness));
            System.out.println("Height mapping: " + (invertHeights ? "White = Highest" : "Black = Highest (default)"));
            System.out.println("Flip left-right: " + (flipLeftRight ? "YES" : "NO (mirrored by default)"));

            // Convert 2D RGB image to 3D voxel array
            System.out.println("\n--- IMAGE TO VOXEL CONVERSION ---");
            long voxelStart = System.nanoTime();
            int[][][] rgbImage = imageData.getCurrentImage();
            boolean[][][] voxelData = convertImageToVoxels(rgbImage, invertHeights, flipLeftRight);
            long voxelTime = (System.nanoTime() - voxelStart) / 1_000_000;
            System.out.println("[TIMING] Image to voxel conversion: " + voxelTime + " ms");

            // Show progress dialog
            JDialog progressDialog = new JDialog(parent, "Exporting to STL", true);
            JLabel progressLabel = new JLabel("Converting image to 3D model...", JLabel.CENTER);
            progressLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressDialog.add(progressLabel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(parent);

            // Perform conversion in background thread
            File finalFile = selectedFile;
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    long startTime = System.nanoTime();

                    System.out.println("\n========================================");
                    System.out.println("STARTING STL CONVERSION WITH TIMING");
                    System.out.println("========================================");
                    System.out.println("Target dimensions: " + width + " x " + height + " x " + thickness + " mm");

                    // Calculate voxel size based on target dimensions
                    int xSize = voxelData.length;
                    int ySize = voxelData[0].length;
                    int zSize = voxelData[0][0].length;

                    float voxelWidth = (float) (width / xSize);
                    float voxelHeight = (float) (height / ySize);
                    float voxelThickness = (float) (thickness / zSize);

                    // Use smallest voxel dimension for uniform cubes
                    float voxelSize = Math.min(voxelWidth, Math.min(voxelHeight, voxelThickness));

                    System.out.println("Voxel size: " + voxelSize + " mm (uniform cubes)");

                    // Convert voxels to mesh
                    VoxelToSTL converter = new VoxelToSTL(voxelData, voxelSize);
                    List<Triangle> triangles = converter.convert();

                    // Write to binary STL file (much smaller than ASCII)
                    STLWriter.writeBinary(triangles, finalFile.getAbsolutePath());

                    long totalTime = (System.nanoTime() - startTime) / 1_000_000;

                    System.out.println("\n========================================");
                    System.out.println("CONVERSION COMPLETE - SUMMARY");
                    System.out.println("========================================");
                    System.out.println("Total triangles: " + triangles.size());
                    System.out.println("Total time:      " + totalTime + " ms (" +
                            String.format("%.2f", totalTime / 1000.0) + " seconds)");
                    System.out.println("========================================");

                    return true;
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        boolean success = get();
                        if (success) {
                            showInfo(parent, "Successfully exported to:\n" + finalFile.getName(),
                                    "Export Complete");
                            logFunction("Export to STL - Source " + sourceNumber + " - " + finalFile.getName() +
                                    " (" + width + " x " + height + " x " + thickness + "mm)");
                        } else {
                            showError(parent, "Failed to export STL file", "Export Error");
                        }
                    } catch (Exception ex) {
                        showError(parent, "Error exporting STL: " + ex.getMessage(), "Export Error");
                        ex.printStackTrace();
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    /**
     * Convert a 2D RGB image to a 3D boolean voxel array.
     * The brightness of each pixel determines its height in the Z dimension.
     *
     * @param rgbImage      2D RGB image [height][width][RGB]
     * @param invertHeights If true, black=tallest and white=shortest; if false, white=tallest and black=shortest
     * @param flipLeftRight If true, flip the image horizontally (mirror left-right)
     * @return 3D voxel array [width][height][depth] where false=empty, true=filled
     */
    private boolean[][][] convertImageToVoxels(int[][][] rgbImage, boolean invertHeights, boolean flipLeftRight) {
        long conversionStart = System.nanoTime();

        int imgHeight = rgbImage.length;
        int imgWidth = rgbImage[0].length;

        // Determine max depth based on the brightest pixel
        int maxDepth = 64; // Default depth for voxel extrusion

        System.out.println("Converting " + imgWidth + " x " + imgHeight +
                " image to voxel array (depth: " + maxDepth + ")");
        System.out.println("Height mapping: " + (invertHeights ? "White = Highest" : "Black = Highest (default)"));
        System.out.println("Horizontal flip: " + (flipLeftRight ? "Enabled" : "Disabled (mirrored by default)"));

        // Create voxel array [x][y][z]
        long allocStart = System.nanoTime();
        boolean[][][] voxels = new boolean[imgWidth][imgHeight][maxDepth];
        long allocTime = (System.nanoTime() - allocStart) / 1_000_000;
        System.out.println("  Array allocation: " + allocTime + " ms");

        // Convert each pixel to a column of voxels
        long processStart = System.nanoTime();
        int totalVoxelsFilled = 0;

        for (int y = 0; y < imgHeight; y++) {
            for (int x = 0; x < imgWidth; x++) {
                int r = rgbImage[y][x][0];
                int g = rgbImage[y][x][1];
                int b = rgbImage[y][x][2];

                // Calculate brightness (0-255)
                int brightness = (r + g + b) / 3;

                // By default, invert so black = highest (255 - brightness)
                // If invertHeights is checked, keep original (white = highest)
                if (!invertHeights) {
                    brightness = 255 - brightness;
                }

                // Map brightness to voxel depth (0-255 -> 0-maxDepth)
                int depth = (brightness * maxDepth) / 256;

                // Calculate voxel X coordinate (flip if NOT requested - fixes backwards behavior)
                int voxelX = flipLeftRight ? x : (imgWidth - 1 - x);

//                Fill voxels from bottom to depth
                for (int z = 0; z < depth; z++) {
                    voxels[voxelX][y][z] = true;
                    totalVoxelsFilled++;
                }
//try {
//    int depth2 = Math.max(0,Math.min(maxDepth-1,depth));
//    voxels[voxelX][y][depth2] = true;
//    totalVoxelsFilled++;
//    voxels[voxelX][y][0] = true;
//    totalVoxelsFilled++;
//}catch(Exception ex){System.out.println(ex.toString() + "\n" +"depth:"+depth);}
            }
        }
        long processTime = (System.nanoTime() - processStart) / 1_000_000;

        long totalTime = (System.nanoTime() - conversionStart) / 1_000_000;

        int totalPossibleVoxels = imgWidth * imgHeight * maxDepth;
        double fillPercentage = (totalVoxelsFilled * 100.0) / totalPossibleVoxels;

        System.out.println("  Pixel processing: " + processTime + " ms");
        System.out.println("  Total voxels filled: " + totalVoxelsFilled + " / " +
                totalPossibleVoxels + " (" + String.format("%.1f", fillPercentage) + "%)");
        System.out.println("  Total conversion time: " + totalTime + " ms");

        return voxels;
    }

    // ===== Log Management =====

    public void selectImageFromLog(JTextArea logArea) {
        int caretPosition = logArea.getCaretPosition();
        try {
            int line = logArea.getLineOfOffset(caretPosition);
            if (line < functionLog.size()) {
                String logEntry = functionLog.getEntry(line);
                int selectedSeq = functionLog.extractSequenceNumber(logEntry);

                if (selectedSeq > 0) {
                    int[][][] image = imageData.getImageBySequence(selectedSeq);
                    if (image != null) {
                        imageData.setCurrentImage(image);
                        imageData.setCurrentSequenceNumber(selectedSeq);
                        // Update source label to show selection
                        controlPanel.setSourceText(logEntry);

                        // If window was closed, reopen it
                        if (!windowManager.isWindowOpen(selectedSeq)) {
                            String functionName = functionLog.extractFunctionName(logEntry);
                            int sourceSeq = functionLog.extractSourceSequence(logEntry);
                            windowManager.createAndShowWindow(image, functionName, selectedSeq, sourceSeq);
                        } else {
                            // Window is already open, bring it to front
                            ImageDisplayWindow window = windowManager.getWindow(selectedSeq);
                            if (window != null) {
                                window.toFront();
                                window.requestFocus();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors in selection
        }
    }

    private void logFunction(String entry) {
        functionLog.addEntry(entry);
        controlPanel.appendToLog(entry);
    }

    private void updateSourceLabel(String logData) {
        controlPanel.setSourceText(logData);
    }

    // ===== Dialog Helpers =====

    private void showError(JFrame parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(JFrame parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(JFrame parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
