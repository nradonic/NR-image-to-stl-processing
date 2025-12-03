package toSTL;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for collecting target physical dimensions from the user.
 */
public class DimensionDialog {
    private double width = 100.0;
    private double height = 100.0;
    private double thickness = 100.0;
    private boolean invertHeights = false;
    private boolean flipLeftRight = false;
    private double scalePercent = 100.0;
    private int pixelClipping = 500;
    private boolean confirmed = false;

    /**
     * Create a dimension dialog with default values (100mm each)
     */
    public DimensionDialog() {
        // Use default values
    }

    /**
     * Create a dimension dialog with specified initial values
     * 
     * @param initialWidth     Initial width in millimeters
     * @param initialHeight    Initial height in millimeters
     * @param initialThickness Initial thickness in millimeters
     */
    public DimensionDialog(double initialWidth, double initialHeight, double initialThickness) {
        this.width = initialWidth;
        this.height = initialHeight;
        this.thickness = initialThickness;
    }

    /**
     * Show the dialog and get user input.
     * 
     * @return true if user confirmed, false if cancelled
     */
    public boolean showDialog() {
        JTextField widthField = new JTextField(String.valueOf(width), 10);
        JTextField heightField = new JTextField(String.valueOf(height), 10);
        JTextField thicknessField = new JTextField(String.valueOf(thickness), 10);
        JCheckBox invertCheckBox = new JCheckBox("White = Highest (instead of Black)", invertHeights);
        JCheckBox flipCheckBox = new JCheckBox("Flip Left-Right", flipLeftRight);
        JTextField scaleField = new JTextField("100", 5);
        JTextField pixelClippingField = new JTextField(String.valueOf(pixelClipping), 5);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Width (mm):"), gbc);

        gbc.gridx = 1;
        panel.add(widthField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Height (mm):"), gbc);

        gbc.gridx = 1;
        panel.add(heightField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Thickness (mm):"), gbc);

        gbc.gridx = 1;
        panel.add(thicknessField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Scale (%):"), gbc);

        gbc.gridx = 1;
        panel.add(scaleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Pixel clipping (px):"), gbc);

        gbc.gridx = 1;
        panel.add(pixelClippingField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(invertCheckBox, gbc);

        gbc.gridy = 6;
        panel.add(flipCheckBox, gbc);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Enter Target Physical Dimensions",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                width = Double.parseDouble(widthField.getText());
                height = Double.parseDouble(heightField.getText());
                thickness = Double.parseDouble(thicknessField.getText());
                scalePercent = Double.parseDouble(scaleField.getText());
                pixelClipping = Integer.parseInt(pixelClippingField.getText());
                invertHeights = invertCheckBox.isSelected();
                flipLeftRight = flipCheckBox.isSelected();

                if (width <= 0 || height <= 0 || thickness <= 0) {
                    JOptionPane.showMessageDialog(null,
                            "All dimensions must be positive values!",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (scalePercent < 1 || scalePercent > 300) {
                    JOptionPane.showMessageDialog(null,
                            "Scale percentage must be between 1 and 300!",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                confirmed = true;
                return true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Please enter valid numeric values!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return false;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getThickness() {
        return thickness;
    }

    public boolean isInvertHeights() {
        return invertHeights;
    }

    public boolean isFlipLeftRight() {
        return flipLeftRight;
    }

    public double getScalePercent() {
        return scalePercent;
    }

    public int getPixelClipping() {
        return pixelClipping;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
