# API Reference

Complete API documentation for Image Processor with STL Export

---

## Package: imageManipulation

### ImageProcessorMain

**Purpose:** Application entry point

```java
public static void main(String[] args)
```
Launches application on EDT with system look-and-feel.

---

### ImageProcessorApp

**Purpose:** Central coordinator managing all subsystems

#### Constructor
```java
public ImageProcessorApp()
```
Initializes ImageData, FunctionLog, and WindowManager.

#### Public Methods

**Lifecycle**
```java
public void start()
```
Creates and displays ControlPanel.

**File Operations**
```java
public void loadImage(JFrame parent)
public void saveImage(JFrame parent)
```
- `loadImage`: Opens file chooser, loads image, resets state
- `saveImage`: Exports current image as PNG or JPEG

**Processing Operations**
```java
public void applyPosterize(JFrame parent)
public void applyMonochrome(JFrame parent)
public void applyScale(JFrame parent)
```
Each method:
1. Validates image exists
2. Gets current image/sequence
3. Applies processing function
4. Adds result to history
5. Creates display window
6. Logs operation

**STL Export**
```java
public void exportToSTL(JFrame parent)
```
Workflow:
1. Shows DimensionDialog
2. Converts image to voxels (background thread)
3. Generates mesh with VoxelToSTL
4. Writes binary STL file
5. Shows progress dialog

**Window Management**
```java
public void gatherWindows(JFrame parent)
```
Cascades all open image windows.

**Log Interaction**
```java
public void selectImageFromLog(JTextArea logArea)
```
Parses clicked log line, sets as current source, reopens window if closed.

#### Private Methods

```java
private boolean[][][] convertImageToVoxels(int[][][] rgbImage, 
                                           boolean invertHeights, 
                                           boolean flipLeftRight)
```
Converts 2D RGB image to 3D boolean voxel array.
- Calculates brightness per pixel
- Maps to voxel column height (0-64)
- Applies inversion/flipping options
- Returns `boolean[width][height][64]`

---

### ImageData

**Purpose:** Image state and history management

#### Fields
```java
private int[][][] currentImage;           // Active image
private int sequenceNumber;               // Next available sequence
private int currentSequenceNumber;        // Active sequence
private List<int[][][]> imageHistory;     // All images
```

#### Methods

**Initialization**
```java
public ImageData()
public void reset()
public void setInitialImage(int[][][] image)
```

**State Access**
```java
public int[][][] getCurrentImage()
public void setCurrentImage(int[][][] image)
public int getCurrentSequenceNumber()
public void setCurrentSequenceNumber(int seqNum)
public int getNextSequenceNumber()
public boolean hasImage()
```

**History Management**
```java
public void addProcessedImage(int[][][] image)
public int[][][] getImageBySequence(int seqNum)
public void incrementSequenceNumber()
```

**Invariants:**
- Sequence 0: unused (null)
- Sequence 1: input image
- Sequence N (N≥2): operation results

---

### ImageProcessingFunctions

**Purpose:** Pure image processing algorithms

#### Methods

```java
public static int[][][] posterize(int[][][] source)
```
Maps each channel to {0, 85, 170, 255} via thresholds at {43, 128, 213}.

```java
public static int[][][] monochrome(int[][][] source)
```
Converts to grayscale: `gray = (R + G + B) / 3`

```java
public static int[][][] scale(int[][][] source, int newWidth, int newHeight)
```
Adaptive scaling:
- Upscaling: Bilinear interpolation
- Downscaling: Area averaging
- Auto-detects based on dimension comparison

#### Private Methods

```java
private static void scaleWithBilinear(int[][][] source, int[][][] result, ...)
```
Interpolates between 4 nearest neighbors with fractional weights.

```java
private static void scaleWithAreaAveraging(int[][][] source, int[][][] result, ...)
```
Calculates weighted average of all source pixels contributing to each destination pixel.

---

### ControlPanel

**Purpose:** Main UI window

#### Constructor
```java
public ControlPanel(ImageProcessorApp app)
```
Creates menu bar, buttons, log area, and labels.

#### Public Methods

**Log Management**
```java
public void clearLog()
public void appendToLog(String text)
```

**UI Updates**
```java
public void setFilename(String filename)
public void setSourceImage(String sourceInfo)
public void resetSourceImage()
```

#### Menu Structure
- **File:** Load, Save, Export to STL, Exit
- **Process:** Posterize, Monochrome, Scale
- **View:** Gather Windows
- **Help:** About

---

### ImageDisplayWindow

**Purpose:** Display individual image result

#### Constructor
```java
public ImageDisplayWindow(int[][][] imageArray, 
                          String functionName, 
                          int seqNum, 
                          int sourceSeqNum)
```
Creates window with title: `"FunctionName - N (from M) (WxH)"`

#### Display Behavior
- Min size: 500px (smallest dimension)
- Max size: 2000px (largest dimension)
- Aspect ratio: Always preserved
- Scaling: Bilinear interpolation for display
- Background: Light gray for letterboxing

---

### WindowManager

**Purpose:** Track and organize display windows

#### Fields
```java
private List<JFrame> displayWindows;
private Map<Integer, ImageDisplayWindow> windowsBySequence;
```

#### Methods

**Window Lifecycle**
```java
public void addWindow(JFrame window)
public void clear()
public ImageDisplayWindow createAndShowWindow(int[][][] imageArray, 
                                               String functionName,
                                               int seqNum, 
                                               int sourceSeqNum)
```

**Window Queries**
```java
public boolean isWindowOpen(int seqNum)
public ImageDisplayWindow getWindow(int seqNum)
```

**Organization**
```java
public int gatherWindows()
```
Returns count of gathered windows.

**Gathering Parameters:**
- Base size: 800×600
- Start position: (100, 100)
- Offset: (30, 30) per window
- Wraps when reaching screen edge

---

### FunctionLog

**Purpose:** Operation history and parsing

#### Methods

**Management**
```java
public FunctionLog()
public void clear()
public void addEntry(String entry)
public String getEntry(int index)
public int size()
public List<String> getAllEntries()
```

**Parsing**
```java
public int extractSequenceNumber(String logEntry)
public String extractFunctionName(String logEntry)
public int extractSourceSequence(String logEntry)
```

**Log Format:** `"FunctionName - N (from M)"`
- N: Current sequence number
- M: Source sequence number
- M absent for input image

---

### ImageConverter

**Purpose:** Format conversion utilities

#### Methods

```java
public static int[][][] bufferedImageToArray(BufferedImage img)
```
Converts BufferedImage to `int[height][width][3]` array.
- Extracts RGB channels via bit shifting
- Stores in [y][x][channel] order

```java
public static BufferedImage arrayToBufferedImage(int[][][] array)
```
Converts array to BufferedImage.
- Clamps values to [0, 255]
- Packs RGB into 32-bit int
- Creates TYPE_INT_RGB image

---

## Package: toSTL

### VoxelToSTL

**Purpose:** Convert voxel array to triangle mesh

#### Constructor
```java
public VoxelToSTL(boolean[][][] voxels, float voxelSize)
```
- `voxels`: Array indexed [x][y][z]
- `voxelSize`: Physical size of each voxel in mm

#### Methods

```java
public List<Triangle> convert()
```
Generates mesh:
1. Calculates height field from voxel columns
2. Identifies valid quads (all corners > 0)
3. Generates top surface triangles with variable Z
4. Generates bottom surface at Z=0
5. Generates walls for exposed edges
6. Returns complete triangle list

```java
public String getStats()
```
Returns summary: triangle count and voxel size.

#### Algorithm Details

**Height Calculation:**
```
height[x][y] = (voxelCount[x][y] * voxelSize) / zScale
where zScale = 64
```

**Quad Validation:**
Quad at (x,y) exists if all corners have height > 0:
- (x, y), (x+1, y), (x, y+1), (x+1, y+1)

**Wall Generation:**
Add wall for edge if neighbor quad doesn't exist:
- Bottom: neighbor at (x, y-1)
- Top: neighbor at (x, y+1)
- Left: neighbor at (x-1, y)
- Right: neighbor at (x+1, y)

---

### DimensionDialog

**Purpose:** UI for physical dimension input

#### Constructors
```java
public DimensionDialog()
public DimensionDialog(double initialWidth, 
                       double initialHeight, 
                       double initialThickness)
```

#### Methods

```java
public boolean showDialog()
```
Displays modal dialog, returns true if confirmed.

**Validation:**
- All dimensions > 0
- Scale: 1-300%

**Getters:**
```java
public double getWidth()
public double getHeight()
public double getThickness()
public boolean isInvertHeights()
public boolean isFlipLeftRight()
public double getScalePercent()
public boolean isConfirmed()
```

---

### Triangle

**Purpose:** Immutable triangle representation

#### Fields
```java
public final Vector3 normal;
public final Vector3 v1, v2, v3;
```

#### Constructors
```java
public Triangle(Vector3 v1, Vector3 v2, Vector3 v3)
public Triangle(Vector3 normal, Vector3 v1, Vector3 v2, Vector3 v3)
```

First constructor auto-calculates normal via cross product.

---

### Vector3

**Purpose:** 3D vector mathematics

#### Fields
```java
public final float x, y, z;
```

#### Constructor
```java
public Vector3(float x, float y, float z)
```

#### Methods

```java
public Vector3 subtract(Vector3 other)
```
Returns `this - other`.

```java
public Vector3 cross(Vector3 other)
```
Returns cross product `this × other`.

```java
public Vector3 normalize()
```
Returns unit vector in same direction.
Returns (0,0,1) if zero-length.

---

### STLWriter

**Purpose:** Export triangles to STL format

#### Methods

```java
public static void writeASCII(List<Triangle> triangles, String filename)
    throws IOException
```
Writes human-readable ASCII STL.
- Format: `facet normal`, `outer loop`, vertices, `endloop`, `endfacet`
- Scientific notation for coordinates
- ~200 bytes per triangle

```java
public static void writeBinary(List<Triangle> triangles, String filename)
    throws IOException
```
Writes compact binary STL (recommended).
- 80-byte header (metadata string)
- 4-byte triangle count (little-endian)
- Per triangle (50 bytes):
  - 12 bytes: normal (3 floats)
  - 36 bytes: vertices (9 floats)
  - 2 bytes: attributes (unused)
- ~50 bytes per triangle

#### Private Methods

```java
private static void writeFloatLE(DataOutputStream out, float value)
private static void writeIntLE(DataOutputStream out, int value)
```
Write values in little-endian byte order (STL standard).

---

## Usage Examples

### Basic Processing Chain

```java
// Create application
ImageProcessorApp app = new ImageProcessorApp();
app.start();

// User loads image via UI
// Internally calls:
BufferedImage img = ImageIO.read(file);
int[][][] imageArray = ImageConverter.bufferedImageToArray(img);
imageData.setInitialImage(imageArray);

// Apply monochrome
int[][][] mono = ImageProcessingFunctions.monochrome(
    imageData.getCurrentImage());
imageData.addProcessedImage(mono);

// Apply posterize to monochrome result
int[][][] post = ImageProcessingFunctions.posterize(mono);
imageData.addProcessedImage(post);
```

### Direct STL Export

```java
// Convert image to voxels
boolean[][][] voxels = convertImageToVoxels(
    rgbImage, 
    false,  // invertHeights
    false   // flipLeftRight
);

// Set up conversion
float voxelSize = 100.0f / imageWidth;  // mm per voxel
VoxelToSTL converter = new VoxelToSTL(voxels, voxelSize);

// Generate mesh
List<Triangle> triangles = converter.convert();

// Export
STLWriter.writeBinary(triangles, "output.stl");
```

### Scale with Quality

```java
int[][][] source = getSourceImage();
int newWidth = 1000;
int newHeight = 800;

// Automatically selects best algorithm
int[][][] scaled = ImageProcessingFunctions.scale(
    source, newWidth, newHeight);
```

---

## Constants

### ImageProcessorApp
```java
private static final int MAX_IMAGE_DIM = 5000;
private static final int MIN_IMAGE_DIM = 16;
```

### ImageDisplayWindow
```java
private static final int MAX_DISPLAY_SIZE = 2000;
private static final int MIN_DISPLAY_SIZE = 500;
```

### WindowManager
```java
private static final int GATHER_WIDTH = 800;
private static final int GATHER_HEIGHT = 600;
private static final int GATHER_START_X = 100;
private static final int GATHER_START_Y = 100;
private static final int GATHER_OFFSET_X = 30;
private static final int GATHER_OFFSET_Y = 30;
```

### VoxelToSTL
```java
private final float zScale = 64;  // Voxel layers per unit height
```

---

## Error Handling

### User-Facing Errors
- File I/O failures → Error dialog with exception message
- Invalid dimensions → Warning dialog before operation
- No image loaded → Warning dialog when operation attempted

### Silent Failures
- Window selection errors → Ignored (log parsing)
- Closed window queries → Returns null/false

### Validation
- Scale dimensions: Clamped to [MIN_IMAGE_DIM, MAX_IMAGE_DIM]
- DimensionDialog: Validates positivity and range
- Color values: Clamped to [0, 255] during conversion

---

## Thread Safety

### EDT Operations
- All UI interactions on Event Dispatch Thread
- SwingUtilities.invokeLater() in main()

### Background Operations
- STL export uses SwingWorker
- Progress dialog shown during processing
- Results posted back to EDT

### Shared State
- ImageData, FunctionLog: Single-threaded access
- WindowManager: Thread-safe (EDT only)

---

## Memory Management

### Image Lifecycle
```
Load → ImageData.imageHistory
Process → New array added to history
Close → Frames disposed, arrays retained
Reset → History cleared, GC eligible
```

### Voxel Arrays
- Created on-demand for export
- Released after triangle generation
- Not retained in history

### Display Windows
- JFrame disposal on close
- BufferedImage cached in window
- Window manager removes on dispose
