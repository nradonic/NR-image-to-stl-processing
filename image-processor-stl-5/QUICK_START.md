# Quick Start Guide

Get up and running with Image Processor and STL Export in 5 minutes.

---

## Installation

### Requirements
- Java 8 or higher
- No external dependencies

### Compilation
```bash
# Navigate to project directory
cd image-processor

# Compile both packages
javac -d bin imageManipulation/*.java toSTL/*.java

# Run
java -cp bin imageManipulation.ImageProcessorMain
```

---

## Basic Workflow

### 1. Load an Image

**Via Menu:** File → Load Image  
**Via Button:** Click "Load Image"

Supported formats: JPG, PNG, GIF, BMP

The image appears in a new window labeled "Input - 1".

---

### 2. Process Your Image

Click any processing button:

**Monochrome** - Convert to grayscale
- **When:** Preparing for 3D export (required step)
- **Result:** Averaged RGB channels

**Posterize** - Reduce to 4 colors
- **When:** Creating distinct height levels
- **Result:** Flat-shaded appearance (values: 0, 85, 170, 255)

**Scale** - Resize image
- **When:** Adjusting resolution for export
- **Input:** Enter new width and height (16-5000 pixels)
- **Quality:** Automatic - smooth upscaling, sharp downscaling

Each operation creates a new window with the result.

---

### 3. Chain Operations

**The function log shows all operations:**
```
Input - 1
Monochrome - 2 (from 1)
Posterize - 3 (from 2)
```

**To use a previous result as source:**
1. Click any line in the function log
2. The "Source:" label updates to show your selection
3. Next operation will use that image

**Example chain:**
```
Input - 1 (photo.jpg)
  ↓ Click "Input - 1"
Monochrome - 2 (from 1)
  ↓ Click "Monochrome - 2"
Posterize - 3 (from 2)
  ↓ Click "Input - 1" (go back to original)
Scale - 4 (from 1) (new size)
```

---

### 4. Export to STL

**Via Menu:** File → Export to STL  
**Via Button:** Click "Export to STL"

**Dialog appears:**

| Field | Description | Example |
|-------|-------------|---------|
| Width (mm) | Physical width of 3D print | 100 |
| Height (mm) | Physical height of 3D print | 100 |
| Thickness (mm) | Maximum depth/relief height | 20 |
| Scale (%) | Adjustment factor | 100 |
| White = Highest | Invert height mapping | ☐ |
| Flip Left-Right | Mirror horizontally | ☐ |

**Default behavior:**
- Black pixels = tallest
- White pixels = shortest
- Orientation: Mirrored left-right by default

**Click OK:**
- Progress dialog appears
- Processing takes 2-5 seconds
- Binary STL file is created
- Ready for 3D printing!

---

## Common Workflows

### Simple Relief from Photo

```
1. Load image
2. Monochrome
3. Export to STL
   - Width: 80mm
   - Height: 80mm  
   - Thickness: 15mm
```

Result: Smooth gradients, photo-like relief

### Stepped Relief (Lithophane-style)

```
1. Load image
2. Monochrome
3. Posterize (creates 4 distinct levels)
4. Export to STL
   - Width: 100mm
   - Height: 100mm
   - Thickness: 20mm
```

Result: Flat terraced levels, architectural feel

### High-Resolution Export

```
1. Load image
2. Monochrome
3. Scale to 1000×1000 (increases detail)
4. Export to STL
   - Width: 150mm
   - Height: 150mm
   - Thickness: 30mm
```

Result: Fine detail, larger file size (~5MB)

### Logo to 3D Model

```
1. Load logo image
2. Monochrome
3. Posterize (simplify to 4 levels)
4. Export to STL
   - Check "White = Highest" (logo = raised)
   - Width: 50mm
   - Height: 50mm
   - Thickness: 5mm
```

Result: Raised logo suitable for embedding

---

## Tips & Tricks

### Window Management

**"Gather Windows" button:**
- Arranges all open image windows in cascade
- Useful when you've created many operations
- Brings all windows to front

**Window reopening:**
- Closed a window by accident?
- Click its entry in the function log to reopen it

### Image Quality

**For best 3D printing results:**
1. Start with high contrast images
2. Use Monochrome (always required)
3. Consider Posterize for defined levels
4. Scale to 500-1000 pixels per side for good detail

**Resolution vs. file size:**
- 200×200: ~500KB STL, ~80K triangles
- 500×500: ~3MB STL, ~500K triangles
- 1000×1000: ~12MB STL, ~2M triangles

### Height Mapping

**Default (unchecked):**
- Black = Tall (raised areas)
- White = Flat (background)
- Best for: Photos, terrain, general relief

**Inverted (checked "White = Highest"):**
- White = Tall
- Black = Flat  
- Best for: Logos, text, line art

### Physical Dimensions

**Voxel size calculation:**
```
voxelSize = width_mm / image_width_pixels
```

Example: 100mm / 500 pixels = 0.2mm per pixel

**Thickness determines Z-scale:**
- Small thickness (5-10mm): Subtle relief
- Medium thickness (15-25mm): Noticeable depth
- Large thickness (30-50mm): Dramatic relief

---

## Keyboard Shortcuts

Currently menu-driven only. Function keys:
- Alt+F: File menu
- Alt+P: Process menu
- Alt+V: View menu
- Alt+H: Help menu

---

## Troubleshooting

### "No Image" Warning
**Problem:** Tried to process before loading image  
**Solution:** Load an image first via File → Load Image

### Image Too Large/Small
**Problem:** Scale validation error  
**Solution:** Keep dimensions between 16-5000 pixels

### STL Won't Print
**Problem:** Slicer reports errors  
**Solution:**
1. Verify image was converted to Monochrome first
2. Check STL in viewer (MeshLab, Windows 3D Viewer)
3. Ensure dimensions are reasonable (>10mm)

### Memory Issues with Large Images
**Problem:** Application slows or crashes  
**Solution:**
1. Scale image down before processing
2. Limit chain length (fewer operations)
3. Close old windows to free memory
4. Increase Java heap: `java -Xmx2G -cp bin ...`

### Wrong Orientation in STL
**Problem:** Model is mirrored or inverted  
**Solution:**
- Use "Flip Left-Right" checkbox
- Use "White = Highest" checkbox
- Try different combinations

---

## File Naming Convention

**Recommended naming for organization:**
```
original.jpg
original_mono.png
original_mono_posterize.png
original_100x100x20mm.stl
```

Or use sequence numbers from log:
```
photo.jpg
photo_seq2_mono.png
photo_seq3_posterize.png
photo_seq3_model.stl
```

---

## Exporting Images (Not STL)

**To save a processed image:**
1. Perform operations
2. File → Save Image (or click "Save Image" button)
3. Choose format: PNG (lossless) or JPG (smaller)
4. Enter filename

**Note:** This saves the *current* image. Click a log entry first to select which version to save.

---

## Understanding the Function Log

Each line shows the operation history:

```
Input - 1
```
- "Input" = function name
- "1" = sequence number

```
Monochrome - 2 (from 1)
```
- "Monochrome" = function applied
- "2" = this result's sequence number
- "(from 1)" = source was sequence 1

**Why this matters:**
- Clicking a log line makes that image the source
- Enables non-linear workflows
- Compare different processing paths

---

## Performance Guide

### Typical Processing Times (500×500 image)

| Operation | Time |
|-----------|------|
| Load | <100ms |
| Monochrome | ~50ms |
| Posterize | ~50ms |
| Scale (up) | 100-300ms |
| Scale (down) | 200-500ms |
| Export to STL | 2-4 seconds |

### Optimization Tips

**Fast workflow:**
- Process at lower resolution
- Scale up only for final export
- Avoid excessive operation chains

**Quality workflow:**
- Start with high-res image
- Do all processing at full resolution
- Posterize after scaling for crisp levels

**Memory-efficient workflow:**
- Close old windows after viewing
- Scale down large images early
- Export frequently to clear history

---

## 3D Printing Tips

### Slicer Settings (Example: Cura/PrusaSlicer)

**For photo reliefs:**
- Layer height: 0.2mm
- Infill: 20% (interior support)
- Supports: None needed
- Orientation: Flat on bed

**For fine detail:**
- Layer height: 0.1mm
- Infill: 15%
- Print speed: 40mm/s
- Consider higher resolution export

### Material Recommendations

**Best for reliefs:**
- PLA: Easy, good detail
- PETG: More durable
- Resin: Highest detail

**Color selection:**
- Light colors: Show depth better
- Dark colors: Hide layer lines
- Translucent: Lithophane effect

---

## Advanced: Batch Processing

**Not currently supported in UI, but you can:**

1. Process one image completely
2. Note the operation sequence
3. Create similar images
4. Apply same operations manually

**Future enhancement:** Save/load operation chains

---

## Getting Help

### In Application
- Help → About: Shows version and summary
- Function log: Hover for tooltip (future feature)

### Common Issues
- See Troubleshooting section above
- Check that Monochrome was applied before STL export
- Verify dimensions are within 16-5000 pixel range

### Output Files
- STL files: Binary format, open with any 3D software
- Image files: Standard PNG/JPG, open with any image viewer

---

## Next Steps

**Once comfortable with basics:**
1. Experiment with different thickness values
2. Try posterize → scale vs. scale → posterize
3. Create operation chains for signature effects
4. Export at different resolutions to compare file sizes
5. Test prints at different physical scales

**Resources:**
- README.md: Full technical documentation
- API_REFERENCE.md: Developer documentation
- This guide: Keep handy for reference

---

## Quick Reference Card

| Action | Menu | Button |
|--------|------|--------|
| Load Image | File → Load Image | Load Image |
| Save Image | File → Save Image | Save Image |
| Export STL | File → Export to STL | Export to STL |
| Posterize | Process → Posterize | Posterize |
| Monochrome | Process → Monochrome | Monochrome |
| Scale | Process → Scale | Scale |
| Organize Windows | View → Gather Windows | Gather Windows |
| Select Source | (none) | Click log entry |

**Remember:** Always Monochrome before STL export!
