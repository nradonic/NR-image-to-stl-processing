package imageManipulation;

import javax.swing.*;
import java.awt.*;

/**
 * Main control panel UI with buttons and function log.
 */
public class ControlPanel extends JFrame {
    private JTextArea logArea;
    private JLabel filenameLabel;
    private JLabel sourceLabel;
    private ImageProcessorApp app;
    
    public ControlPanel(ImageProcessorApp app) {
        super("ImageProcessorToSTL");
        this.app = app;
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        createMenuBar();
        createUI();
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem loadItem = new JMenuItem("Load Image");
        loadItem.addActionListener(e -> app.loadImage(this));
        fileMenu.add(loadItem);
        
        JMenuItem saveItem = new JMenuItem("Save Image");
        saveItem.addActionListener(e -> app.saveImage(this));
        fileMenu.add(saveItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exportToSTLItem = new JMenuItem("Export to STL...");
        exportToSTLItem.addActionListener(e -> app.exportToSTL(this));
        fileMenu.add(exportToSTLItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Process Menu
        JMenu processMenu = new JMenu("Process");
        
        JMenuItem posterizeItem = new JMenuItem("Posterize");
        posterizeItem.addActionListener(e -> app.applyPosterize(this));
        processMenu.add(posterizeItem);
        
        JMenuItem monochromeItem = new JMenuItem("Monochrome");
        monochromeItem.addActionListener(e -> app.applyMonochrome(this));
        processMenu.add(monochromeItem);
        
        JMenuItem scaleItem = new JMenuItem("Scale...");
        scaleItem.addActionListener(e -> app.applyScale(this));
        processMenu.add(scaleItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        
        JMenuItem gatherItem = new JMenuItem("Gather Windows");
        gatherItem.addActionListener(e -> app.gatherWindows(this));
        viewMenu.add(gatherItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(processMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void showAboutDialog() {
        String aboutText = "<html><body style='width: 400px; padding: 10px;'>" +
            "<h2>Image to STL Conversion Utilities</h2>" +
            "<p><b>November 2025</b></p>" +
            "<p><b>Author:</b> Nick Radonic</p>" +
            "<br>" +
            "<p>This application provides image processing and 3D model export capabilities:</p>" +
            "<ul>" +
            "<li>Process images with various filters (Posterize, Monochrome, Scale)</li>" +
            "<li>Chain operations for complex transformations</li>" +
            "<li>Export images as 3D models in STL format for 3D printing</li>" +
            "</ul>" +
            "<br>" +
            "<p>The STL export feature converts 2D images into 3D voxel models with " +
            "exposed face detection. Only surface triangles are generated, creating " +
            "efficient meshes suitable for 3D printing and manufacturing.</p>" +
            "<br>" +
            "<p><i>Developed with Java and Swing</i></p>" +
            "</body></html>";
        
        JEditorPane editorPane = new JEditorPane("text/html", aboutText);
        editorPane.setEditable(false);
        editorPane.setBackground(null);
        editorPane.setBorder(null);
        
        JOptionPane.showMessageDialog(this, editorPane, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void createUI() {
        // Top panel with Load button and filename
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Load button
        JButton loadButton = new JButton("Load Image");
        loadButton.addActionListener(e -> app.loadImage(this));
        
        // Filename label - make it more visible
        filenameLabel = new JLabel("No image loaded");
        filenameLabel.setHorizontalAlignment(JLabel.CENTER);
        filenameLabel.setFont(filenameLabel.getFont().deriveFont(Font.BOLD, 11f));
        filenameLabel.setForeground(new Color(0, 0, 150)); // Dark blue color
        filenameLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        
        topPanel.add(loadButton, BorderLayout.NORTH);
        topPanel.add(filenameLabel, BorderLayout.CENTER);
        
        // Function buttons panel - fixed height
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Processing Functions"));
        
        JButton posterizeBtn = new JButton("Posterize");
        posterizeBtn.addActionListener(e -> app.applyPosterize(this));
        
        JButton monochromeBtn = new JButton("Monochrome");
        monochromeBtn.addActionListener(e -> app.applyMonochrome(this));
        
        JButton scaleBtn = new JButton("Scale");
        scaleBtn.addActionListener(e -> app.applyScale(this));
        
        JButton saveBtn = new JButton("Save Image");
        saveBtn.addActionListener(e -> app.saveImage(this));
        
        JButton exportBtn = new JButton("Export to STL");
        exportBtn.addActionListener(e -> app.exportToSTL(this));
        
        JButton gatherBtn = new JButton("Gather Windows");
        gatherBtn.addActionListener(e -> app.gatherWindows(this));
        
        buttonPanel.add(posterizeBtn);
        buttonPanel.add(monochromeBtn);
        buttonPanel.add(scaleBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(gatherBtn);
        
        // Wrap button panel to prevent resizing
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.add(buttonPanel, BorderLayout.NORTH);
        
        // Source selection label
        sourceLabel = new JLabel("Source: Input - 1");
        sourceLabel.setHorizontalAlignment(JLabel.CENTER);
        sourceLabel.setFont(sourceLabel.getFont().deriveFont(Font.BOLD, 11f));
        sourceLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
            )
        ));
        sourceLabel.setOpaque(true);
        sourceLabel.setBackground(new Color(220, 240, 255));
        
        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.add(sourceLabel, BorderLayout.CENTER);
        buttonWrapper.add(sourcePanel, BorderLayout.SOUTH);
        
        // Log area - resizable
        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        logArea.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                app.selectImageFromLog(logArea);
            }
        });
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Function Log (click to select source)"));
        
        // Combine top panel and buttons into one north panel
        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(buttonWrapper, BorderLayout.CENTER);
        
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void clearLog() {
        logArea.setText("");
    }
    
    public void appendToLog(String text) {
        logArea.append(text + "\n");
    }
    
    public void setFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            filenameLabel.setText("No image loaded");
        } else {
            filenameLabel.setText(filename);
        }
    }
    
    public void setSourceText(String sourceInfo) {
        sourceLabel.setText("Source: " + sourceInfo);
    }
    
    public void resetSourceImage() {
        sourceLabel.setText("Source: Input - 1");
    }
}
