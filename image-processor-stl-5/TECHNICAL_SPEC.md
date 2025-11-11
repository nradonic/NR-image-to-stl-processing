# Technical Specification

Detailed technical documentation for Image Processor with STL Export

**Author:** Nick Radonic  
**Version:** 1.0  
**Date:** November 2025

---

## System Architecture

### Overview

Two-package architecture with clear separation of concerns:

```
imageManipulation/          # UI and image processing
├── ImageProcessorMain      # Entry point
├── ImageProcessorApp       # Controller
├── ImageData               # Model
├── FunctionLog             # Model
├── ControlPanel            # View
├── ImageDisplayWindow      # View
├── WindowManager           # View Controller
├── ImageConverter          # Utility
└── ImageProcessingFunctions # Algorithms

toSTL/                      # 3D conversion
├── VoxelToSTL              # Mesh generation
├── DimensionDialog         # UI
├── STLWriter               # I/O
├── Triangle                # Data
└── Vector3                 # Math
```

### Design Principles

**Immutability**
- All image operations create new arrays
- Original images preserved in history
- Pure functions with no side effects

**Single Responsibility**
- Each class has one clear purpose
- Processing functions separated from UI
- File I/O isolated in dedicated classes

**Model-View-Controller**
- Model: ImageData, FunctionLog
- View: ControlPanel, ImageDisplayWindow
- Controller: ImageProcessorApp

---

## Data Structures

### Image Representation

**Internal Format:** `int[height][width][3]`

```java
int[][][] image = new int[height][width][3];
// Access: image[y][x][c] where c ∈ {0=R, 1=G, 2=B}
```

**Rationale:**
- Direct channel access without bit operations
- Intuitive indexing: [row][column][channel]
- Compatible with processing algorithms
- Easier debugging (inspect values directly)

**Memory:** 
- 4 bytes per channel (Java int)
- Total: width × height × 3 × 4 bytes
- Example: 500×500 = 3MB

**Conversion to BufferedImage:**
```java
int rgb = (R << 16) | (G << 8) | B;
bufferedImage.setRGB(x, y, rgb);
```

### Voxel Representation

**Format:** `boolean[x][y][z]`

```java
boolean[][][] voxels = new boolean[width][height][depth];
// voxels[x][y][z] = true if solid, false if empty
```

**Coordinate System:**
- X: Left to right (image width)
- Y: Top to bottom (image height)
- Z: Bottom to top (extrusion depth)

**Memory:**
- 1 byte per voxel (boolean array)
- Depth fixed at 64
- Example: 500×500×64 = 16MB

### Triangle Mesh

**Triangle Structure:**
```java
public class Triangle {
    Vector3 normal;  // Unit normal vector
    Vector3 v1, v2, v3;  // Vertices (CCW winding)
}
```

**Memory:**
- 12 floats per triangle (3 vertices + 1 normal × 3 components)
- 48 bytes per triangle (12 × 4)
- Plus overhead: ~200 bytes including object headers

**Typical Mesh Size:**
- N×N image → ~2N² triangles
- 500×500 → ~500K triangles → ~100MB in memory

---

## Image Processing Algorithms

### Posterize

**Algorithm:** Threshold-based quantization

```
For each pixel (x, y):
  For each channel c:
    if value < 43:    output = 0
    if 43 ≤ value < 128:  output = 85
    if 128 ≤ value < 213: output = 170
    if value ≥ 213:   output = 255
```

**Threshold Selection:**
- Midpoints: 42.5, 127.5, 212.5
- Rounded: 43, 128, 213
- Equal spacing: 85 between levels

**Complexity:** O(width × height)  
**Memory:** O(width × height) for output

### Monochrome

**Algorithm:** Simple averaging

```
For each pixel (x, y):
  gray = (R + G + B) / 3
  output[y][x] = {gray, gray, gray}
```

**Alternative Methods (Not Used):**
- Luminance: 0.299R + 0.587G + 0.114B (perceptual)
- Lightness: (max(R,G,B) + min(R,G,B)) / 2
- Desaturation: max(R, G, B)

**Rationale for Simple Average:**
- Adequate for height mapping
- Faster than weighted methods
- Predictable behavior

**Complexity:** O(width × height)  
**Memory:** O(width × height)

### Scale - Bilinear Interpolation

**Used for:** Upscaling (enlarging images)

**Algorithm:**
```
For each destination pixel (dx, dy):
  # Map to source coordinates
  sx = (dx + 0.5) × srcWidth / dstWidth - 0.5
  sy = (dy + 0.5) × srcHeight / dstHeight - 0.5
  
  # Clamp to valid range
  sx = clamp(sx, 0, srcWidth - 1.001)
  sy = clamp(sy, 0, srcHeight - 1.001)
  
  # Get four nearest pixels
  x0 = floor(sx), y0 = floor(sy)
  x1 = x0 + 1,    y1 = y0 + 1
  
  # Calculate weights
  wx = sx - x0
  wy = sy - y0
  
  # Interpolate
  top = src[y0][x0] × (1-wx) + src[y0][x1] × wx
  bottom = src[y1][x0] × (1-wx) + src[y1][x1] × wx
  result = top × (1-wy) + bottom × wy
```

**Key Details:**
- Pixel centers: +0.5 offset ensures proper alignment
- Edge handling: Clamp prevents out-of-bounds
- Weights: Linear interpolation between neighbors
- Per-channel: Applied independently to R, G, B

**Visual Quality:**
- Smooth gradients
- No blocky artifacts
- Slight blur (intended for upscaling)

**Complexity:** O(dstWidth × dstHeight)  
**Memory:** O(dstWidth × dstHeight) + O(srcWidth × srcHeight)

### Scale - Area Averaging

**Used for:** Downscaling (reducing images)

**Algorithm:**
```
For each destination pixel (dx, dy):
  # Calculate source region
  srcX0 = dx × (srcWidth / dstWidth)
  srcY0 = dy × (srcHeight / dstHeight)
  srcX1 = (dx + 1) × (srcWidth / dstWidth)
  srcY1 = (dy + 1) × (srcHeight / dstHeight)
  
  # Get integer bounds
  x0 = floor(srcX0), y0 = floor(srcY0)
  x1 = ceil(srcX1),  y1 = ceil(srcY1)
  
  # Accumulate weighted sum
  colorSum = 0
  totalWeight = 0
  
  For sy in y0..y1-1:
    For sx in x0..x1-1:
      # Calculate overlap area
      xOverlap = min(srcX1, sx+1) - max(srcX0, sx)
      yOverlap = min(srcY1, sy+1) - max(srcY0, sy)
      weight = xOverlap × yOverlap
      
      # Add weighted contribution
      colorSum += src[sy][sx] × weight
      totalWeight += weight
  
  # Average
  result = colorSum / totalWeight
```

**Key Details:**
- Box filter: All pixels in region contribute
- Weighted: Partial overlaps counted proportionally
- Antialiasing: Prevents information loss
- Exact: Mathematically correct averaging

**Visual Quality:**
- Sharp edges preserved
- Fine details maintained
- No aliasing/moiré patterns
- Superior to nearest-neighbor

**Complexity:** O(dstWidth × dstHeight × (srcWidth/dstWidth) × (srcHeight/dstHeight))  
Simplifies to O(srcWidth × srcHeight) since each source pixel contributes once

**Memory:** O(dstWidth × dstHeight) + O(srcWidth × srcHeight)

---

## 3D Conversion Pipeline

### Stage 1: Image to Voxels

**Input:** `int[height][width][3]` RGB image  
**Output:** `boolean[width][height][64]` voxel array

**Process:**
```
For each pixel (x, y):
  # Calculate brightness
  brightness = (R + G + B) / 3
  
  # Apply inversion (optional)
  if (!invertHeights):
    brightness = 255 - brightness
  
  # Map to depth
  depth = (brightness × 64) / 256
  
  # Apply horizontal flip (optional)
  voxelX = flipLeftRight ? x : (width - 1 - x)
  
  # Fill column
  For z in 0..depth-1:
    voxels[voxelX][y][z] = true
```

**Parameters:**
- `invertHeights`: false = black tall, true = white tall
- `flipLeftRight`: Mirror horizontally
- Fixed depth: 64 layers

**Design Decisions:**

**Why 64 layers?**
- Sufficient detail for most applications
- Keeps memory reasonable
- Balances resolution vs. performance
- Divisible by powers of 2

**Why bottom-up filling?**
- Creates solid objects (no hollow columns)
- Ensures printability
- Matches physical intuition

**Default inversion (black = tall):**
- Photos: Shadows = raised detail
- Better for terrain/relief
- User can override for logos

### Stage 2: Voxels to Height Field

**Input:** `boolean[width][height][64]` voxels  
**Output:** `float[width][height]` heights

**Process:**
```
For each column (x, y):
  voxelCount = sum of voxels[x][y][z] for all z
  height[x][y] = (voxelCount × voxelSize) / zScale
```

**Constants:**
- `zScale = 64` (maps 64 voxels → 1 unit height)
- `voxelSize` = user-specified physical dimension

**Example:**
```
Image: 500×500 pixels
Target: 100mm × 100mm × 20mm
voxelSize = 100 / 500 = 0.2mm
Max height = (64 × 0.2) / 64 = 0.2mm × thickness_ratio
```

### Stage 3: Height Field to Triangles

**Algorithm:** Quad tessellation with neighbor detection

**Phase 1: Identify Valid Quads**
```
For each position (x, y) in [0..width-2] × [0..height-2]:
  z00 = height[x][y]
  z10 = height[x+1][y]
  z01 = height[x][y+1]
  z11 = height[x+1][y+1]
  
  quadExists[x][y] = (z00 > 0 && z10 > 0 && z01 > 0 && z11 > 0)
```

**Rationale:** Only generate geometry where all corners have height. This:
- Eliminates triangles with zero-height vertices
- Creates clean perimeter edges
- Reduces triangle count by ~50%

**Phase 2: Generate Top Surface**
```
For each valid quad (x, y):
  # Define corner positions
  v00 = (x×size, y×size, height[x][y])
  v10 = ((x+1)×size, y×size, height[x+1][y])
  v01 = (x×size, (y+1)×size, height[x][y+1])
  v11 = ((x+1)×size, (y+1)×size, height[x+1][y+1])
  
  # Create two triangles
  Triangle1: (v00, v10, v11) with normal from cross product
  Triangle2: (v00, v11, v01) with normal from cross product
```

**Winding Order:** Counter-clockwise (CCW) for outward-facing normals

**Phase 3: Generate Bottom Surface**
```
For each valid quad (x, y):
  # Flat bottom at z=0
  b00 = (x×size, y×size, 0)
  b10 = ((x+1)×size, y×size, 0)
  b01 = (x×size, (y+1)×size, 0)
  b11 = ((x+1)×size, (y+1)×size, 0)
  
  # Reverse winding for downward normal
  Triangle1: (b00, b11, b10) with normal (0, 0, -1)
  Triangle2: (b00, b01, b11) with normal (0, 0, -1)
```

**Phase 4: Generate Vertical Walls**

For each valid quad, check four edges:

**Bottom Edge (y-direction):**
```
hasBottomNeighbor = (y > 0) && quadExists[x][y-1]
if (!hasBottomNeighbor):
  v1 = (x×size, y×size, height[x][y])
  v2 = ((x+1)×size, y×size, height[x+1][y])
  v3 = (x×size, y×size, 0)
  v4 = ((x+1)×size, y×size, 0)
  normal = (0, -1, 0)  # Outward facing
  
  Triangle1: (v1, v3, v4)
  Triangle2: (v1, v4, v2)
```

**Similarly for Top, Left, Right edges** with appropriate:
- Neighbor checks
- Vertex positions
- Normal directions

**Result:** Watertight mesh with:
- Variable-height top surface
- Flat bottom
- Walls only at perimeter and discontinuities

### Triangle Count Estimation

For an N×N image with 80% fill:

```
Quads = N² × 0.8 = 0.8N²

Top surface: 2 triangles/quad = 1.6N²
Bottom surface: 2 triangles/quad = 1.6N²
Walls: ~0.2N perimeter + internal edges ≈ 0.4N²

Total ≈ 3.6N² triangles
```

**Examples:**
- 200×200: ~144K triangles
- 500×500: ~900K triangles  
- 1000×1000: ~3.6M triangles

---

## STL File Format

### Binary STL Structure

```
HEADER (80 bytes)
    Bytes 0-79: ASCII text (metadata)
    
TRIANGLE_COUNT (4 bytes)
    Little-endian uint32
    
FOR EACH TRIANGLE (50 bytes each):
    NORMAL (12 bytes)
        3 × little-endian float32
        Components: nx, ny, nz
    
    VERTEX1 (12 bytes)
        3 × little-endian float32
        Components: x, y, z
    
    VERTEX2 (12 bytes)
        3 × little-endian float32
        
    VERTEX3 (12 bytes)
        3 × little-endian float32
    
    ATTRIBUTE (2 bytes)
        Unused, set to 0
```

**Total File Size:**
```
size = 80 + 4 + (triangleCount × 50)
```

**Example Sizes:**
- 100K triangles: 4.77 MB
- 500K triangles: 23.84 MB
- 1M triangles: 47.68 MB

### Writing Binary STL

**Java Implementation Details:**

**Little-Endian Encoding:**
```java
ByteBuffer buffer = ByteBuffer.allocate(4);
buffer.order(ByteOrder.LITTLE_ENDIAN);
buffer.putFloat(value);
out.write(buffer.array());
```

**Why Little-Endian?**
- STL standard requires it
- Most CAD software expects it
- Intel/AMD processors native format

**Buffering:**
```java
DataOutputStream out = new DataOutputStream(
    new BufferedOutputStream(new FileOutputStream(filename))
);
```
- Buffering critical for performance
- Write buffer: 8KB default
- Reduces system calls by ~1000×

### ASCII STL (Alternative)

```
solid model
  facet normal nx ny nz
    outer loop
      vertex x1 y1 z1
      vertex x2 y2 z2
      vertex x3 y3 z3
    endloop
  endfacet
  ...
endsolid model
```

**Disadvantages:**
- 4-10× larger files
- Slower parsing
- Precision loss (text representation)

**When to Use:**
- Debugging mesh issues
- Manual inspection/editing
- Legacy software compatibility

---

## Performance Optimization

### Image Processing

**Current Implementation:**
- Single-threaded
- No caching
- Direct pixel manipulation

**Optimization Opportunities:**

**Parallel Processing:**
```java
IntStream.range(0, height).parallel().forEach(y -> {
    for (int x = 0; x < width; x++) {
        // Process pixel (y, x)
    }
});
```
Expected speedup: 2-4× on multi-core systems

**SIMD Operations:**
- Process 4+ pixels simultaneously
- Requires Java Vector API (JDK 16+)
- Expected speedup: 2-8×

### Voxel Conversion

**Current: O(pixels × depth)**

**Optimization 1: Early Termination**
```java
// Instead of filling all Z layers:
for (int z = 0; z < depth; z++) {
    voxels[x][y][z] = true;
}

// Set surface voxel only:
if (depth > 0) {
    voxels[x][y][depth-1] = true;
    voxels[x][y][0] = true;  // Bottom for stability
}
```
Memory reduction: 64× (only 2 voxels per column)

**Optimization 2: Run-Length Encoding**
Store (start, length) pairs instead of boolean array:
```java
class VoxelColumn {
    int start;  // Always 0
    int length; // Depth value
}
```
Memory: N² × 8 bytes instead of N² × 64 bytes

### Mesh Generation

**Current Bottlenecks:**
1. Triangle allocation: ~200 bytes × count
2. Normal calculation: 1 cross product per triangle
3. ArrayList growth: Periodic reallocation

**Optimization 1: Pre-allocation**
```java
int estimatedTriangles = (width - 1) × (height - 1) × 4;
List<Triangle> triangles = new ArrayList<>(estimatedTriangles);
```

**Optimization 2: Vertex Sharing**
Instead of per-triangle vertices, use indexed mesh:
```java
List<Vector3> vertices;  // Shared vertex pool
List<int[]> indices;     // Triangle indices (3 ints)
```
Memory reduction: ~66% (vertices referenced 6× on average)

**Optimization 3: Strip/Fan Generation**
Group adjacent triangles:
- Triangle strips: Share edges
- Triangle fans: Share center vertex
- Memory: 1/3 reduction
- STL format limitation: No native support

### STL Writing

**Current: Buffered sequential writes**

**Already Optimized:**
- BufferedOutputStream eliminates small-write overhead
- ByteBuffer reduces allocation
- Sequential access pattern cache-friendly

**Further Optimization:**
- Memory-mapped files for >50MB STL
- Async I/O for overlap with computation
- Compression (non-standard)

---

## Memory Management

### Current Usage Profile

**500×500 Image Processing:**
```
Input image:          3 MB
Monochrome result:    3 MB
Posterize result:     3 MB
Scaled result:        Variable
History total:        9-12 MB

Display windows:
  BufferedImage cache: 3 MB each × count
  
Export temporary:
  Voxel array:        16 MB
  Triangle list:      100 MB
  STL buffer:         8 KB

Peak during export: ~130 MB
```

### Garbage Collection Impact

**Frequent Allocation:**
- Each operation creates new 3 MB array
- Each triangle allocates ~200 bytes
- Each window creates BufferedImage

**GC Pressure:**
- Young generation: High churn rate
- Old generation: History retention
- Large objects: Voxel/triangle arrays

**Heap Recommendations:**
```bash
# Small images (<500px):
java -Xmx512m ...

# Medium images (500-1000px):
java -Xmx2g ...

# Large images (>1000px):
java -Xmx4g ...
```

### Memory Leak Prevention

**No Leaks Present:**
- All images stored intentionally (history)
- Windows disposed properly on close
- Voxel/triangle arrays eligible after export

**To Free Memory:**
1. Close unused image windows
2. Load new image (clears history)
3. Manual System.gc() call (not recommended)

---

## Thread Safety

### Current Threading Model

**Event Dispatch Thread (EDT):**
- All UI operations
- Button clicks
- Window creation
- Log updates

**Main Thread:**
- Application startup only
- Hands off to EDT immediately

**Background Thread:**
- STL export only (SwingWorker)
- Voxel conversion
- Triangle generation  
- File writing

### Synchronization

**None Required Currently:**
- ImageData: Single-threaded access
- FunctionLog: EDT only
- WindowManager: EDT only

**SwingWorker Protection:**
- Background thread: Read-only image data
- EDT updates: Via done() callback
- No shared mutable state

### Future Multi-threading

**If Parallelizing Image Processing:**

**Option 1: Immutable State**
```java
public static int[][][] posterize(int[][][] source) {
    // Source read-only, result independent
    return IntStream.range(0, height).parallel()
        .mapToObj(y -> processRow(source, y))
        .toArray(int[][][]::new);
}
```
Safe: No synchronization needed

**Option 2: Concurrent Collections**
```java
private ConcurrentHashMap<Integer, int[][][]> imageHistory;
```
If multiple threads modify history

---

## Testing Strategy

### Unit Testing (Recommended)

**ImageProcessingFunctions:**
```java
@Test
public void testPosterize() {
    int[][][] input = createTestImage(10, 10);
    input[0][0] = {30, 100, 200};  // Should map to {0, 85, 170}
    
    int[][][] result = ImageProcessingFunctions.posterize(input);
    
    assertEquals(0, result[0][0][0]);
    assertEquals(85, result[0][0][1]);
    assertEquals(170, result[0][0][2]);
}
```

**Vector3:**
```java
@Test
public void testCrossProduct() {
    Vector3 v1 = new Vector3(1, 0, 0);
    Vector3 v2 = new Vector3(0, 1, 0);
    Vector3 result = v1.cross(v2);
    
    assertEquals(0, result.x, 0.001);
    assertEquals(0, result.y, 0.001);
    assertEquals(1, result.z, 0.001);
}
```

### Integration Testing

**Full Pipeline:**
```java
@Test
public void testImageToSTL() {
    BufferedImage input = loadTestImage();
    int[][][] imageArray = ImageConverter.bufferedImageToArray(input);
    
    boolean[][][] voxels = convertImageToVoxels(imageArray, false, false);
    VoxelToSTL converter = new VoxelToSTL(voxels, 1.0f);
    List<Triangle> triangles = converter.convert();
    
    assertTrue(triangles.size() > 0);
    // Verify triangle validity
    for (Triangle tri : triangles) {
        assertNotNull(tri.normal);
        assertNotNull(tri.v1);
        assertTrue(tri.normal.x != 0 || tri.normal.y != 0 || tri.normal.z != 0);
    }
}
```

### Manual Testing Checklist

**Image Processing:**
- [ ] Load various image formats (JPG, PNG, GIF)
- [ ] Test each processing function
- [ ] Verify operation chaining
- [ ] Check log entry format
- [ ] Test window reopening from log

**STL Export:**
- [ ] Export with default settings
- [ ] Export with inverted heights
- [ ] Export with horizontal flip
- [ ] Verify STL in external viewer
- [ ] Test print on 3D printer

**Edge Cases:**
- [ ] Very small images (16×16)
- [ ] Very large images (5000×5000)
- [ ] Extreme aspect ratios (100×5000)
- [ ] All-white image
- [ ] All-black image
- [ ] High-frequency detail (checkerboard)

---

## Known Limitations

### Technical Constraints

**Image Size:**
- Min: 16×16 (arbitrary, could be smaller)
- Max: 5000×5000 (memory constraint)
- Very large images may cause OutOfMemoryError

**Voxel Depth:**
- Fixed at 64 layers
- Not configurable by user
- Adequate for most use cases

**STL Precision:**
- Float32: ~7 decimal digits
- Sufficient for mm-scale printing
- Not suitable for micro-precision

### Algorithm Limitations

**Height Mapping:**
- Single-channel only (monochrome required)
- No support for color-to-material mapping
- No transparency handling

**Mesh Quality:**
- Quad-based: Not ideal for organic shapes
- No smoothing/subdivision
- Sharp corners at height discontinuities

**Scale Algorithm:**
- No gamma correction
- No sharpening/enhancement
- Fixed method selection (up vs. down)

### UI Limitations

**No Undo:**
- Must use log selection to go back
- Can't delete history entries
- No operation removal

**Single Window per Sequence:**
- Closing window doesn't remove from history
- Can reopen but loses position/size

**No Batch Processing:**
- One image at a time
- Manual operation application

### STL Export Limitations

**Binary Only:**
- ASCII STL available in code but not in UI
- No format selection dialog

**No Mesh Optimization:**
- No vertex welding
- No triangle reduction
- No manifold repair

**Fixed Z-Scale:**
- Hardcoded zScale = 64
- Affects height-to-thickness ratio
- Not exposed to user

---

## Future Enhancement Roadmap

### Phase 1: Polish (Low-hanging Fruit)

**UI Improvements:**
- Add undo/redo stack
- Remember last used directories
- Add keyboard shortcuts
- Drag-and-drop image loading
- Recent files menu

**Processing:**
- Add blur filter (Gaussian)
- Add sharpen filter
- Add edge detection
- Add brightness/contrast adjustment

**Export:**
- Expose Z-scale parameter
- Add ASCII STL option to UI
- Preview 3D mesh before export
- Export selected region only

### Phase 2: Performance

**Optimization:**
- Parallel image processing
- Lazy history loading
- Compressed history storage
- Incremental triangle generation

**Scalability:**
- Support >5000px images
- Streaming STL export
- Progress indication for all ops
- Cancel long operations

### Phase 3: Advanced Features

**3D Processing:**
- Marching cubes algorithm (smooth surfaces)
- Variable Z-scale per region
- Multi-material STL export
- Texture mapping to height

**Batch Processing:**
- Script recording/playback
- Batch file processing
- Command-line interface
- Operation templates

**Integration:**
- Direct slicer integration
- Cloud 3D print services
- WebAssembly port
- Mobile app version

### Phase 4: Professional

**Analysis:**
- Histogram display
- Height distribution chart
- Print time estimation
- Material usage calculation

**Quality:**
- Mesh repair algorithms
- Hole filling
- Surface smoothing
- Normal smoothing

**Formats:**
- OBJ export
- 3MF export
- STEP export
- Point cloud export

---

## Code Style Guidelines

### Formatting

**Indentation:** 4 spaces  
**Line Length:** 120 characters  
**Braces:** Egyptian style (opening brace on same line)

### Naming Conventions

**Classes:** PascalCase  
**Methods:** camelCase  
**Constants:** UPPER_SNAKE_CASE  
**Private Fields:** camelCase

### Documentation

**All Public APIs:**
```java
/**
 * Brief description.
 * 
 * @param paramName Parameter description
 * @return Return value description
 */
```

**Complex Algorithms:**
```java
// Phase 1: Calculate height field
// For each column, count the number of filled voxels
// and map to physical height using voxel size.
```

### Error Handling

**User Errors:** Show dialog with clear message  
**Unexpected Errors:** Log to console + show generic error  
**Invalid Input:** Validate early, fail fast

---

## Build and Deployment

### Compilation

**Standard:**
```bash
javac -d bin imageManipulation/*.java toSTL/*.java
```

**With Debug Info:**
```bash
javac -g -d bin imageManipulation/*.java toSTL/*.java
```

**With Warnings:**
```bash
javac -Xlint:all -d bin imageManipulation/*.java toSTL/*.java
```

### JAR Creation

```bash
cd bin
jar cfe ImageProcessor.jar imageManipulation.ImageProcessorMain imageManipulation/ toSTL/
cd ..
```

**Run JAR:**
```bash
java -jar bin/ImageProcessor.jar
```

### Deployment

**Standalone:**
- JAR file is self-contained
- Requires Java 8+ installed
- No external dependencies

**With JRE:**
```bash
jlink --module-path $JAVA_HOME/jmods \
      --add-modules java.desktop \
      --output jre
      
tar czf ImageProcessor.tar.gz bin/ImageProcessor.jar jre/
```

**Native Executable:**
Use GraalVM native-image (requires configuration):
```bash
native-image -jar ImageProcessor.jar ImageProcessor
```

---

## References

### Algorithms

**Bilinear Interpolation:**
- Wikipedia: Bilinear interpolation
- Fundamentals of Texture Mapping and Image Warping (Heckbert, 1989)

**Area Averaging:**
- High Quality Image Resizing (Mitchell & Netravali, 1988)
- Image Resampling (Turkowski, 1990)

**Height Field Rendering:**
- Real-Time Rendering (Akenine-Möller et al.)
- 3D Game Engine Design (Eberly)

### File Formats

**STL Specification:**
- STL Format (3D Systems)
- STL (file format) - Wikipedia
- Binary STL Explanation (Fabbers.com)

### Java APIs

**Swing:**
- Trail: Creating a GUI With JFC/Swing (Oracle)
- Java Swing Tutorial

**ImageIO:**
- Reading/Loading an Image (Oracle)
- Writing/Saving an Image (Oracle)

**NIO:**
- Java NIO Tutorial
- ByteBuffer Guide

---

**Document Version:** 1.0  
**Last Updated:** November 2025  
**Maintainer:** Nick Radonic
