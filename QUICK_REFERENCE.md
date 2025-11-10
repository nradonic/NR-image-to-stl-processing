# STL Export Dialog - Quick Reference

## Dialog Fields

```
┌─────────────────────────────────────────────┐
│  Enter Target Physical Dimensions           │
├─────────────────────────────────────────────┤
│  Width (mm):      [____100____]             │
│  Height (mm):     [____100____]             │
│  Thickness (mm):  [____255____]             │
│  Scale (%):       [_100_]  (1-300%)        │
│  ☐ White = Highest (instead of Black)       │
│  ☐ Flip Left-Right                          │
│                                              │
│          [ OK ]  [ Cancel ]                  │
└─────────────────────────────────────────────┘
```

## What Each Option Does

### Width, Height, Thickness
Base physical dimensions in millimeters before scaling.
- Pre-populated with your image dimensions
- Example: 800×600 image → 800mm × 600mm suggested

### Scale (%)
Multiplier for final size. Range: 1-300%
- **100%** = Original dimensions (no change)
- **50%** = Half size (for smaller prints)
- **200%** = Double size (for larger prints)
- **25%** = Tiny print (1/4 size)
- **300%** = Maximum size (triple)

**Calculation:**
```
Final Width = Width × (Scale / 100)
Final Height = Height × (Scale / 100)
Final Thickness = Thickness × (Scale / 100)
```

**Examples:**
| Base | Scale | Result |
|------|-------|--------|
| 100mm × 100mm × 100mm | 100% | 100mm × 100mm × 100mm |
| 100mm × 100mm × 100mm | 50% | 50mm × 50mm × 50mm |
| 100mm × 100mm × 100mm | 200% | 200mm × 200mm × 200mm |
| 200mm × 150mm × 255mm | 150% | 300mm × 225mm × 382.5mm |

### White = Highest (instead of Black)
Checkbox to reverse brightness-to-height mapping.

**UNCHECKED (default):**
- ⚫ Black pixels → Tallest features
- ⚪ White pixels → Shortest features
- Best for: Most images, photos, logos

**CHECKED:**
- ⚪ White pixels → Tallest features
- ⚫ Black pixels → Shortest features
- Best for: Inverted images, special cases

### Flip Left-Right
Checkbox to control horizontal mirroring.

**UNCHECKED (default):**
- Image is mirrored left-to-right
- Corrects for typical STL orientation issues
- Text/logos appear correct in most slicers

**CHECKED:**
- Image orientation preserved as-is
- No mirroring applied
- Use if default produces backwards result

## Usage Tips

### For Typical Photos/Images:
```
Width: [match image width]
Height: [match image height]
Thickness: 255
Scale: 100%
☐ White = Highest (default Black = Highest)
☐ Flip Left-Right (default mirrored)
```

### For Small Test Print:
```
Scale: 25%  ← Quarter size
Everything else: defaults
```

### For Large Display Print:
```
Scale: 200%  ← Double size
Everything else: defaults
```

### If Text Appears Backwards:
```
☑ Flip Left-Right  ← Check this
Everything else: defaults
```

### If Heights Are Inverted:
```
☑ White = Highest  ← Check this
Everything else: defaults
```

## Console Output Example

When you export, you'll see:
```
========================================
STARTING STL EXPORT PROCESS
========================================
Image size: 800 x 600 pixels
Source: Image 1
Output file: output.stl
Scale: 150%
Final dimensions: 150.00 x 112.50 x 382.50 mm
Height mapping: Black = Highest (default)
Flip left-right: NO (mirrored by default)
```

This tells you:
- Original image size
- Scale percentage applied
- Actual physical dimensions after scaling
- Whether black or white creates tallest features
- Whether horizontal mirroring is applied

## Common Scenarios

### Scenario 1: Logo Print (100mm wide)
```
1. Load logo image
2. Width: 100, Height: auto, Thickness: 255
3. Scale: 100%
4. Uncheck everything (use defaults)
5. Export
```

### Scenario 2: Tiny Test Print
```
1. Load any image
2. Keep default dimensions
3. Scale: 10%  ← Very small!
4. Uncheck everything (use defaults)
5. Export → Quick test print
```

### Scenario 3: Large Wall Art (300mm wide)
```
1. Load high-res image
2. Width: 300, Height: auto, Thickness: 255
3. Scale: 100%
4. Uncheck everything (use defaults)
5. Export
```

### Scenario 4: Inverted Heightmap
```
1. Load heightmap where white = peaks
2. Keep dimensions
3. Scale: 100%
4. ☑ White = Highest ← Check this!
5. Uncheck Flip Left-Right
6. Export
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| File too large | Reduce Scale % or base dimensions |
| Too small to print | Increase Scale % (150-300%) |
| Text backwards | Toggle Flip Left-Right checkbox |
| Heights inverted | Check "White = Highest" |
| Looks squashed | Adjust Width/Height ratio |
| Not enough detail | Increase Thickness value |

## File Sizes

Approximate .stl file sizes:

| Image Size | Triangles | File Size |
|------------|-----------|-----------|
| 200 × 200 | ~25K | 1-2 MB |
| 400 × 400 | ~100K | 5-8 MB |
| 800 × 600 | ~300K | 15-20 MB |
| 1600 × 1200 | ~1.2M | 60-80 MB |

**Note:** Binary STL format is already efficient. For smaller files, consider compressing with ZIP/GZIP after export (70% size reduction).
