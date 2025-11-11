# Image Processor with STL Export

Java-based image processing application featuring operation chaining, history tracking, and 3D heightfield export for 3D printing.

**Author:** Nick Radonic  
**Date:** November 2025

---

## Overview

This application bridges 2D image processing and 3D model generation. Process images with multiple filters, chain operations together, and export results as STL files for 3D printing. The STL export converts grayscale values to height information, creating efficient voxel-based meshes with only exposed surface triangles.

---

## Features

### Image Processing

**Posterize**
- Reduces each color channel to 4 levels: 0, 85, 170, 255
- Creates flat-shaded appearance
- Useful for preparing images for 3D extrusion

**Monochrome**
- Converts to grayscale via RGB channel averaging
- Required preprocessing for STL export

**Scale**
- High-quality adaptive resizing
- **Upscaling**: Bilinear interpolation for smooth gradients
- **Downscaling**: Area averaging (box filter) prevents aliasing
- Constraints: 16-5000 pixels per dimension

### Operation Chaining

- Chain unlimited operations in sequence
- Click any log entry to select it as source for next operation
- Full history maintained with sequence numbers
- Reopen closed windows by clicking log entries
- Visual tracking: `"FunctionName - N (from M)"` format

### STL Export

**Height Field Conversion**
- Grayscale brightness → voxel column height (0-64 levels)
- Default: Black = tallest, White = shortest
- Optional inversion: White = tallest
- Optional left-right flip

**Physical Dimensions**
- Specify target width, height, thickness in millimeters
- Uniform voxel size calculated automatically
- Scale adjustment: 1-300%

**Mesh Generation**
- Quad-based heightfield surface
- Vertical walls only at perimeter/discontinuities
- Closed manifold mesh (printable)
- Binary STL format (compact files)
- Exposed face detection eliminates interior triangles

### UI Features

- Multiple simultaneous image windows
- Automatic aspect ratio preservation
- "Gather Windows" cascade arrangement
- Click-to-select source from operation log
- Live source indicator
- Progress dialog for STL export

---

## Architecture

### Package: `imageManipulation`

**ImageProcessorMain**
- Application entry point
- Launches on Swing Event Dispatch Thread
- Sets system look and feel

**ImageProcessorApp**
- Central coordinator
- Manages all subsystems
- Handles file I/O and processing dispatch
- Orchestrates STL export pipeline

**ImageData**
- State management and history
- Storage: `int[height][width][RGB]` arrays
- Sequence number tracking
- Methods:
  - `setInitialImage()` - establishes sequence 1
  - `addProcessedImage()` - increments sequence
  - `getImageBySequence()` - retrieve historical result

**ImageProcessingFunctions**
- Pure static functions
- No side effects - immutable transformations
- Algorithms:
  - `posterize()` - threshold-based color reduction
  - `monochrome()` - simple RGB averaging
  - `scale()` - adaptive interpolation/filtering

**ControlPanel**
- Main UI window
- Menu bar and button interface
- Function log with click-to-select
- Source indicator displays active image
- Filename display

**WindowManager**
- Tracks display windows by sequence number
- Methods:
  - `createAndShowWindow()` - display result
  - `isWindowOpen()` - check window state
  - `gatherWindows()` - cascade layout
  - `getWindow()` - retrieve by sequence

**ImageDisplayWindow**
- Individual result viewer
- Automatic size calculation (500-2000px)
- Real-time aspect ratio maintenance
- Title format: `"Function - N (from M) (WxH)"`

**FunctionLog**
- Operation history tracking
- Entry format: `"FunctionName - N (from M)"`
- Parsing methods extract sequence numbers and function names

**ImageConverter**
- Bidirectional conversion between `BufferedImage` and array format
- RGB channel extraction/packing
- Value clamping to valid range

### Package: `toSTL`

**VoxelToSTL**
- Core mesh generation
- Converts `boolean[x][y][z]` voxel array to triangles
- Height field approach:
  - Calculates height from voxel column density
  - Z-scale factor: 64 (maps 64 voxels → unit height)
  - Generates top surface with variable heights
  - Flat bottom surface at z=0
  - Vertical walls only where neighbors don't exist

**DimensionDialog**
- Swing dialog for physical parameters
- Fields: width, height, thickness (mm)
- Scale percentage: 1-300%
- Options: invert heights, flip left-right
- Input validation

**Triangle**
- Immutable triangle data structure
- Fields: `normal`, `v1`, `v2`, `v3`
- Automatic normal calculation from vertices
- Constructor accepts pre-computed normal for efficiency

**Vector3**
- 3D vector mathematics
- Operations: subtract, cross product, normalize
- Immutable design
- Float precision for STL compatibility

**STLWriter**
- Dual-format output
- **Binary STL** (recommended):
  - 80-byte header with metadata
  - Little-endian float encoding
  - ~50 bytes per triangle
  - Typical file sizes: 0.5-5 MB
- **ASCII STL**:
  - Human-readable format
  - Scientific notation for coordinates
  - ~200 bytes per triangle
  - 4-10× larger than binary

---

## Technical Details

### Image Storage Format

All images stored as `int[height][width][3]` where:
- Index [y][x][0] = Red (0-255)
- Index [y][x][1] = Green (0-255)  
- Index [y][x][2] = Blue (0-255)

Convention matches BufferedImage RGB packing: `(R << 16) | (G << 8) | B`

### Scaling Algorithms

**Bilinear Interpolation** (upscaling)
- Maps destination pixel to source coordinates
- Interpolates between 4 nearest source pixels
- Weights based on fractional position
- Produces smooth gradients

**Area Averaging** (downscaling)
- Calculates source region for each destination pixel
- Weighted sum of all contributing source pixels
- Weight = overlap area
- Prevents aliasing and maintains detail

### Voxel to Heightfield Conversion

```
For each image pixel (x, y):
  brightness = (R + G + B) / 3
  if (!invertHeights):
    brightness = 255 - brightness
  
  depth = (brightness * 64) / 256
  
  for z in 0..depth:
    voxel[x][y][z] = true
```

### Mesh Generation Algorithm

1. **Height Calculation**: Count filled voxels per XY column
2. **Quad Detection**: Mark quads where all 4 corners have height > 0
3. **Top Surface**: Generate 2 triangles per quad with variable Z
4. **Bottom Surface**: Generate 2 triangles per quad at Z=0
5. **Walls**: For each quad edge, add wall if neighbor quad missing

Result: Closed manifold mesh with minimized triangle count

### Sequence Number System

- Sequence 1: Input image (reserved)
- Sequence N+1: Result of operation N
- Log entry format enables reconstruction of operation chain
- Clicking log entry reactivates that sequence as source

---

## Usage

### Compilation

```bash
javac -d bin imageManipulation/*.java toSTL/*.java
```

### Execution

```bash
java -cp bin imageManipulation.ImageProcessorMain
```

### Typical Workflow

1. **Load Image** - File → Load Image
2. **Process** - Apply Monochrome (required for STL)
3. **Optional** - Apply Posterize for distinct levels
4. **Optional** - Scale to desired resolution
5. **Export** - File → Export to STL
   - Specify physical dimensions
   - Choose inversion/flip options
   - Select output file
6. **3D Print** - Load STL into slicer software

### Operation Chaining Example

```
Input - 1 (original.jpg)
  ↓ [Click on "Input - 1" in log]
Monochrome - 2 (from 1)
  ↓ [Click on "Monochrome - 2" in log]  
Posterize - 3 (from 2)
  ↓ [Click on "Input - 1" in log - go back to original]
Scale - 4 (from 1) (500x500)
  ↓ [Click on "Scale - 4" in log]
Export to STL - Source 4 - output.stl (100x100x50mm)
```

---

## Performance Characteristics

### Image Processing
- Posterize: O(pixels) - single pass
- Monochrome: O(pixels) - single pass
- Scale: O(srcPixels × dstPixels / max(srcDim, dstDim)) - area dependent

### STL Export
- Voxel conversion: O(pixels × depth) - typically ~1-2 seconds
- Triangle generation: O(quads) - typically ~100-500ms
- Binary STL write: O(triangles) - typically ~200-800ms

Typical total export time: 2-4 seconds for 500×500 image

### Memory Usage
- Image: width × height × 3 × 4 bytes (int array)
- History: cumulative per operation
- Voxel array: width × height × 64 bytes (boolean)
- Triangle list: ~2N triangles for N×N image (~200 bytes each)

Example: 500×500 image → ~3MB image + ~16MB voxels + ~50MB triangles

---

## File Formats

### Input
- JPEG, PNG, GIF, BMP (via ImageIO)
- Recommended: PNG for lossless workflow

### Output
- Images: PNG, JPEG
- 3D Models: Binary STL (little-endian IEEE 754 floats)

### STL Structure
```
[80-byte header]
[4-byte triangle count]
For each triangle:
  [12 bytes: normal vector (3 floats)]
  [12 bytes: vertex 1 (3 floats)]
  [12 bytes: vertex 2 (3 floats)]
  [12 bytes: vertex 3 (3 floats)]
  [2 bytes: attribute (unused)]
```

---

## Design Patterns

**MVC Architecture**
- Model: ImageData, FunctionLog
- View: ControlPanel, ImageDisplayWindow
- Controller: ImageProcessorApp

**Pure Functions**
- All image processing operations are stateless
- Facilitates testing and reasoning

**Immutable Data**
- Original images preserved in history
- Each operation produces new array

**Factory Pattern**
- WindowManager creates and tracks display windows

**Strategy Pattern**
- Scale algorithm selection based on direction (up/down)

---

## Limitations & Constraints

- Image dimensions: 16-5000 pixels
- Maximum voxel depth: 64 layers
- STL scale: 1-300% of specified dimensions
- Single-channel height mapping (monochrome only)
- Memory: ~100MB per 1000×1000 image with history
- No undo operation (use log selection instead)

---

## Future Enhancements

Potential improvements:
- Multi-level undo/redo
- Batch processing
- Additional filters (blur, sharpen, edge detect)
- Custom voxel depth configuration
- Marching cubes for smooth surfaces
- Variable Z-scale per export
- Preview 3D rendering before export
- Grayscale to multi-material STL
- Direct integration with slicer APIs

---

## Dependencies

- Java SE 8 or higher
- Swing/AWT for UI
- javax.imageio for image I/O
- No external libraries required

---

## License

© 2025 Nick Radonic. All rights reserved