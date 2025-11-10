# CHANGELOG

All improvements and changes to the Image Processor application.

---

## Version 2.0 - November 6, 2025

### Major Features Added

#### 1. Professional Image Scaling
**What Changed:** Replaced nearest-neighbor with high-quality interpolation
- **Upscaling:** Bilinear interpolation for smooth gradients
- **Downscaling:** Area averaging (box filter) for sharp, anti-aliased results
- **Quality:** Rivals commercial image editing software
- **File:** `imageManipulation.ImageProcessingFunctions.java`

#### 2. Visible Filename Display
**What Changed:** Added bold, dark blue filename under Load button
- Shows loaded filename clearly
- Updates automatically when loading new image
- **File:** `imageManipulation.ControlPanel.java`

#### 3. Silent Operations
**What Changed:** Removed acknowledgement popups
- Save Image: No success popup
- Gather Windows: No confirmation popup
- Scale: No completion popup
- Select from log: No selection popup
- Error dialogs still show when needed
- **Files:** `imageManipulation.ImageProcessorApp.java`

#### 4. Optimized Layout
**What Changed:** Function Log now expands to fill available space
- Before: Large empty gray area in middle
- After: Log fills the space efficiently
- **File:** `imageManipulation.ControlPanel.java`

#### 5. Source Selection Indicator
**What Changed:** Added blue label showing currently selected source
- Shows which image is selected as source
- Updates when clicking log entries
- Visual confirmation of selection
- **File:** `imageManipulation.ControlPanel.java`

#### 6. Window Reopening on Selection
**What Changed:** Clicking log entry reopens closed windows
- If window was closed: Automatically reopens
- If window is open: Brings to front
- Provides visual feedback of selection
- **Files:** `imageManipulation.WindowManager.java`, `imageManipulation.FunctionLog.java`, `imageManipulation.ImageProcessorApp.java`

### Bug Fixes

#### Sequence Numbering Fix
**Bug:** First operation after loading was numbered 1 (duplicate of input)
- Input: 1, First operation: 1 ❌
**Fixed:** Proper sequence progression
- Input: 1, First operation: 2 ✓
- **File:** `imageManipulation.ImageData.java`

### Code Cleanup

#### Removed Obsolete File
**Removed:** `ImageProcessor.java` (23 KB, 563 lines)
- Old monolithic version before refactoring
- Had old nearest-neighbor scaling
- Caused confusion about which version was active
**Result:** Clean modular architecture with 9 focused classes

---

## Architecture

### Current Structure (9 Classes)
```
imageManipulation.ImageProcessorMain.java      - Entry point
imageManipulation.ImageProcessorApp.java       - Main coordinator
imageManipulation.ControlPanel.java            - UI with all improvements
imageManipulation.ImageProcessingFunctions.java - Professional algorithms
imageManipulation.ImageData.java               - State and history management
imageManipulation.FunctionLog.java             - Operation logging and parsing
imageManipulation.WindowManager.java           - Window tracking and management
imageManipulation.ImageDisplayWindow.java      - Individual image display
imageManipulation.ImageConverter.java          - Format conversions
```

---

## Complete Feature List

### Image Processing
- Posterize (4 color levels)
- Monochrome (grayscale)
- Scale (bilinear/area averaging)
- Save (PNG/JPG)

### UI Features
- Filename display (bold, dark blue)
- Source selection indicator (blue label)
- Function log (tracks all operations)
- Silent operations (no popups)
- Optimized layout (efficient space use)
- Window reopening (click to reopen)

### Workflow Features
- Chain operations sequentially
- Branch workflows (select any previous step)
- Non-linear editing
- History preservation
- Window management (gather, close, reopen)

---

## Migration Notes

### From Version 1.x
If you have the old version:
1. Entry point is now `imageManipulation.ImageProcessorMain` (not `ImageProcessor`)
2. Code is now modular (9 separate classes)
3. All improvements are in the new version
4. Delete old `ImageProcessor.java` if present

### Compilation
```bash
cd image-processor-7/src/main/java
javac -d ../../build *.java
java -cp ../../build imageManipulation.ImageProcessorMain
```

---

## Testing

All features have been tested:
- ✅ Professional scaling (up and down)
- ✅ Filename display (bold, visible)
- ✅ Silent operations (no unwanted popups)
- ✅ Layout optimization (log fills space)
- ✅ Source indicator (updates correctly)
- ✅ Window reopening (reopens when closed)
- ✅ Sequence numbering (1, 2, 3, 4...)
- ✅ Code compiles without errors

---

## Known Issues

None - All requested features implemented and bugs fixed.

---

## Future Enhancements

From TODO.txt:
- Additional filters (blur, sharpen, edge detection)
- Brightness/contrast adjustments
- Rotation and flip operations
- Histogram equalization
- Batch processing
- Undo/Redo functionality

---

## Credits

Developed iteratively with user feedback to create a professional, user-friendly image processing application.
