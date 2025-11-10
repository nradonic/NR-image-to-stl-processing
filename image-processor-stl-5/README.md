# Image to 3MF Conversion Utilities

## Overview
This is a Java Swing-based application that combines image processing with 3D model export capabilities. The application allows you to process images through various filters and export them as 3D models in the industry-standard 3MF format for 3D printing and manufacturing.

### Author: Nick Radonic, November 2025

### Purpose:
- Sequential image manipulation with chainable processing functions
- Export 2D images as 3D voxel models for 3D printing
- Bridge between image processing and additive manufacturing

### Build Method:
Java code, with a Maven build structure. Currently Java 11 in the POM file, but could be anything greater.
Developed with JetBrains IntelliJ IDEA Community Version 2025.2.4


## Features

### Main Components:
1. **Control Panel** - Main window with:
   - Menu bar with File, Process, View, and Help menus
   - Load Image button for file selection
   - Loaded filename display (shows under Load button)
   - Processing function buttons (fixed height panel)
   - Export to 3MF button for 3D model export
   - Source image indicator (shows currently selected source)
   - Function log showing the sequence of operations (resizable text area)

2. **Display Windows** - Each image (input and processed) is shown in a separate resizable window:
   - Window title shows function name, sequence number, and image dimensions (e.g., "Posterize - 2 (800x600)")
   - Images maintain aspect ratio when resized
   - Display window size ranges from 500x500 to 2000x2000 pixels

3. **Image Processing Functions**:
   - **Posterize** - Reduces color levels to 4 discrete values (0, 85, 170, 255)
   - **Monochrome** - Converts to grayscale by averaging RGB components
   - **Scale** - Resizes image with constraints (16x16 to 5000x5000 pixels)
   - **Save Image** - Exports current image as PNG or JPG to selected folder

4. **3D Export Functionality**:
   - **Export to 3MF** - Converts 2D images to 3D voxel models
   - **Smart dimension defaults** - Automatically prepopulates with image dimensions and 255mm thickness
   - Interactive dimension dialog for adjusting physical size (width, height, thickness in millimeters)
   - Uses Marching Cubes algorithm for surface extraction
   - Generates industry-standard 3MF files compatible with 3D printing slicers
   - Brightness-based height mapping: **white = tallest, black = zero height**

## How to Compile and Run

**Note:** This is a source distribution. You must compile before running.

### Option 1: Using Maven (Recommended)

#### Project Structure:
```
image-processor/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── ImageProcessor.java
└── README.md
```

#### Maven Commands:
```bash
# Compile the project
mvn compile

# Run the application
mvn exec:java

# Package as JAR
mvn package

# Run the packaged JAR
java -jar target/image-processor-1.0.0.jar

# Create executable JAR with dependencies
mvn clean package
java -jar target/image-processor-1.0.0-jar-with-dependencies.jar

# Clean build artifacts
mvn clean
```

#### IDE Setup:
1. Import the project as a Maven project
2. Your IDE should automatically recognize the pom.xml
3. Build and run using IDE's built-in Maven support
4. Main class: `ImageProcessor`

### Option 2: Direct Compilation (No Maven)

If you don't have Maven installed, you can compile directly with javac:

#### Compilation:
```bash
# Navigate to source directory
cd image-processor-7/src/main/java

# Compile all Java files
javac -d ../../../build *.java
```

#### Running:
```bash
# Run from the project root
java -cp build imageManipulation.ImageProcessorMain
```

## Usage Instructions

1. **Load an Image**:
   - Click "Load Image" button
   - Select an image file (JPG, PNG, GIF, BMP supported)
   - The image will display in a window titled "Input - 1"
   - The function log will show "Input - 1"

2. **Apply Processing Functions**:
   - Click any function button (Posterize, Monochrome, Scale)
   - A new window will open showing the processed result
   - The window title will show the function name and next sequence number
   - The function log will record the operation

3. **Chain Functions**:
   - Each function operates on the current image state
   - Results become the new current image
   - You can apply functions in any order
   - Each creates a new display window with incremented sequence number

4. **Scale Function**:
   - Opens a dialog to enter new width and height
   - Valid range: 16x16 to 5000x5000 pixels
   - Shows current dimensions as default values

5. **Save Image Function**:
   - Opens file chooser dialog
   - Select output folder and filename
   - Choose format: PNG (lossless) or JPG (compressed)
   - Automatically adds correct file extension
   - Silent operation - no confirmation dialogs
   - Logs save operation to function log

6. **Select Source Image**:
   - Click any line in the Function Log to select that image as the source
   - Selected source is shown in the blue "Source:" label
   - Next processing operation will use the selected image
   - Allows non-linear workflows (branch from any previous step)
   - **If the window was closed, it will automatically reopen** when selected
   - **If the window is open, it will come to the front**

7. **Export to 3MF**:
   - Click "Export to 3MF" button or select from File menu
   - Dimension dialog opens with prepopulated values:
     - **Width**: Automatically set to image width in pixels (as millimeters)
     - **Height**: Automatically set to image height in pixels (as millimeters)
     - **Thickness**: Automatically set to 255mm (max RGB brightness range)
   - Adjust dimensions as needed or use defaults
   - Choose output location and filename
   - Application converts image to 3D voxel model using brightness mapping
   - Generates 3MF file compatible with 3D printing slicers (Cura, PrusaSlicer, etc.)
   - **White pixels** become tallest columns, **black pixels** remain at zero height
   - Progress dialog shows conversion status

8. **Menu Bar**:
   - **File Menu**: Load Image, Save Image, Export to 3MF, Exit
   - **Process Menu**: Posterize, Monochrome, Scale
   - **View Menu**: Gather Windows
   - **Help Menu**: About (shows application information and credits)

## Technical Details

### Image Format:
- Images are stored in memory as 3D arrays: `int[height][width][RGB]`
- Each color component is 8-bit (0-255)

### Display Windows:
- Automatically scale to fit content while maintaining aspect ratio
- Constrained to 500x500 minimum and 2000x2000 maximum
- Resizable with real-time aspect ratio preservation
- Images are centered in the window

### Posterize Algorithm:
- Rounds each color component to nearest level:
  - 0-42 → 0 (black)
  - 43-127 → 85 (one-third)
  - 128-212 → 170 (two-thirds)
  - 213-255 → 255 (full)

### Monochrome Algorithm:
- Calculates average of R, G, B components
- Sets all three components to the average value

### Scale Algorithm:
- **Upscaling**: Uses bilinear interpolation for smooth gradients
- **Downscaling**: Uses area averaging (box filter) - averages all contributing source pixels
- High-quality results that avoid pixelation (upscaling) and aliasing (downscaling)

### 3MF Export Process:
1. **Image to Voxel Conversion**: Converts 2D RGB image to 3D voxel array [width][height][depth]
   - Each pixel becomes a vertical column of voxels
   - Pixel brightness (0-255) maps to column height (0-64 voxels by default)
   - **Black pixels (0)** → Zero height (no voxels)
   - **White pixels (255)** → Maximum height (all 64 voxels filled)
   - Formula: depth = (brightness × 64) / 256
2. **Dimension Prepopulation**: Dialog automatically fills with intelligent defaults
   - Width: Image width in pixels (as millimeters)
   - Height: Image height in pixels (as millimeters)
   - Thickness: 255mm (matches maximum RGB brightness value)
3. **Marching Cubes Algorithm**: Extracts smooth surface mesh from voxel data
   - Processes each 2×2×2 cube in the voxel grid
   - Generates triangulated surface mesh
4. **Mesh Optimization**:
   - Removes duplicate vertices (0.1 micron tolerance)
   - Removes invalid/degenerate triangles
5. **3MF File Generation**: Creates ZIP archive with XML structure
   - [Content_Types].xml (MIME types)
   - _rels/.rels (relationships)
   - 3D/3dmodel.model (mesh geometry)
6. **Physical Dimensions**: User-specified (or default) dimensions in millimeters are applied to scale the model appropriately

## Function Log
The function log displays each operation in sequence:
- Format: "FunctionName - SequenceNumber" or "FunctionName - SequenceNumber (dimensions)"
- Example log:
  ```
  Input - 1
  Posterize - 2
  Monochrome - 3
  Scale - 4 (800x600)
  Save - PNG - output.png
  ```

## Architecture

The program uses a clean modular design with separate responsibilities:

### Class Structure:

#### Image Processing Package (imageManipulation):
- **imageManipulation.ImageProcessorMain** - Entry point, launches application
- **imageManipulation.ImageProcessorApp** - Main coordinator, manages all components
- **imageManipulation.ControlPanel** - User interface with menu bar, buttons and log
- **imageManipulation.ImageProcessingFunctions** - All image processing algorithms
- **imageManipulation.ImageData** - Image state and history management
- **imageManipulation.FunctionLog** - Operation logging and tracking
- **imageManipulation.WindowManager** - Display window coordination
- **imageManipulation.ImageDisplayWindow** - Individual image display windows
- **imageManipulation.ImageConverter** - BufferedImage ↔ array conversions

#### 3D Export Package (to3MF):
- **to3MF.VoxelTo3MF** - Main entry point for 3D conversion
- **to3MF.DimensionDialog** - User interface for dimension input
- **to3MF.MarchingCubes** - Surface extraction algorithm implementation
- **to3MF.Mesh** - Mesh data structure with optimization methods
- **to3MF.ThreeMFWriter** - 3MF file format writer (ZIP + XML)
- **to3MF.Vector3D** - 3D vector mathematics
- **to3MF.Triangle** - Triangle mesh primitive

### Data Flow:
- Each processing function takes an image array and returns a new processed array
- Functions are chainable - output of one becomes input to the next
- Clean separation between UI (Swing) and processing logic
- All images stored in history for source selection
- 3MF export converts 2D image data to 3D voxel representation before mesh generation

## Adding New Functions

To add a new processing function:
1. Create a button in the control panel
2. Implement the processing method that takes `int[][][]` and returns `int[][][]`
3. Create an apply method that calls the processor and updates the display
4. Add logging of the function name and sequence number

## Requirements
- Java 8 or higher
- Swing GUI library (included in standard Java)

## Documentation
- **README.md** - This file (user guide and features)
- **src/main/java/to3MF/README.md** - Detailed documentation of 3MF export functionality
- **CHANGELOG.md** - Complete list of improvements and changes
- **MAVEN_SETUP.md** - Maven build and configuration details
- **TODO.txt** - Completed features and future ideas

## 3MF Export Use Cases
- Create relief maps from topographic images
- Generate embossed text or logos for 3D printing
- Convert photographs to tactile 3D representations
- Create depth-based sculptures from artwork
- Prototype designs that transition from 2D to 3D
