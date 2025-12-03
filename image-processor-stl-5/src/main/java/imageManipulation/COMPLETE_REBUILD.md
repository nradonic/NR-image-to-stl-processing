# COMPLETE CLEAN REBUILD - VERIFIED BYTE ARRAYS ONLY

## The Problem

Your JAR still has int[][][] compiled in (343 MB per image instead of 94.5 MB).
Evidence: Loading 1 image uses 987 MB instead of ~200 MB.

## Solution: Complete Clean Rebuild

### Step 1: Backup Everything
```bash
cd /Users/NickRadonic/source/.../image-processor-stl-5
mkdir backup-$(date +%Y%m%d)
cp -r src backup-$(date +%Y%m%d)/
cp -r target backup-$(date +%Y%m%d)/ 2>/dev/null || true
```

### Step 2: Download VERIFIED Files

All three files are confirmed to have ZERO int[][][] references:
1. **ImageData.java** - History limiting (MAX_HISTORY_SIZE=20) + memory logging
2. **ImageDisplayWindow.java** - Scaled caching (12 MB vs 132 MB)
3. **ImageProcessorApp.java** - Memory logging + all byte[][][]

### Step 3: Replace Source Files

```bash
cd /Users/NickRadonic/source/.../image-processor-stl-5/src/imageManipulation

# Verify current files are wrong:
grep "int\[\]\[\]\[\]" ImageProcessorApp.java
# If this returns anything, you have old files

# Replace with new files:
cp ~/Downloads/ImageData.java .
cp ~/Downloads/ImageDisplayWindow.java .
cp ~/Downloads/ImageProcessorApp.java .

# Verify new files are correct:
grep "int\[\]\[\]\[\]" ImageProcessorApp.java
# Should return NOTHING

grep "byte\[\]\[\]\[\]" ImageProcessorApp.java  
# Should return multiple lines
```

### Step 4: Nuclear Clean

```bash
cd /Users/NickRadonic/source/.../image-processor-stl-5

# Remove EVERYTHING:
rm -rf target/
rm -rf bin/ 2>/dev/null || true
rm -rf out/ 2>/dev/null || true

# Maven clean:
mvn clean

# Verify nothing remains:
ls target/ 2>&1
# Should say "No such file or directory"
```

### Step 5: Rebuild Everything

```bash
# Compile:
mvn compile

# Verify compiled classes have byte arrays:
cd target/classes/imageManipulation
javap -c ImageData | grep -i "byte"
# Should show bytecode with byte arrays

cd ../../..
```

### Step 6: Build JAR

```bash
# Package:
mvn package

# Verify JAR timestamp is NEW:
ls -lh target/image-processor-1.0.0.jar
# Check date/time - should be RIGHT NOW
```

### Step 7: Final Verification

```bash
# Extract JAR and check:
cd /tmp
rm -rf verify-jar 2>/dev/null
mkdir verify-jar
cd verify-jar
jar xf /Users/NickRadonic/source/.../target/image-processor-1.0.0.jar

# Check if ImageData has byte arrays:
strings imageManipulation/ImageData.class | grep -i "byte"
# Should show byte array references

cd -
```

### Step 8: Test Run

```bash
java -Xms8g -Xmx8g -jar target/image-processor-1.0.0.jar
```

## Expected Console Output

```
[MEMORY] After loading image: 200 MB / 8192 MB (2%) - 1 images in history
Cached scaled image: 1538x2000 (original: 5041x6555)
[MEMORY BEFORE Posterize] Used: 195 MB, Free: 8001 MB, Max: 8192 MB (2.4% used)
[MEMORY AFTER Posterize - before window creation] Used: 290 MB, Free: 7906 MB...
[MEMORY AFTER Posterize - after window creation] Used: 305 MB, Free: 7891 MB...
```

**If you see 987 MB after loading**, the JAR still has int[][][] - rebuild failed.
**If you see ~200 MB after loading**, the JAR has byte[][][] - rebuild worked!

## What Each Operation Should Use

| Operation | Memory Used | Delta |
|-----------|-------------|-------|
| Load image | 200 MB | - |
| Operation 1 | 305 MB | +105 MB |
| Operation 2 | 410 MB | +105 MB |
| Operation 3 | 515 MB | +105 MB |
| Operation 4 | 620 MB | +105 MB |
| Operation 10 | 1150 MB | +105 MB |
| Operation 20 | 2200 MB | History limit kicks in |

## If Still Using 987 MB After Rebuild

Then Maven is caching or using wrong source directory. Try:

### Option A: IntelliJ Rebuild
1. File → Invalidate Caches / Restart
2. Build → Clean Project  
3. Build → Rebuild Project
4. Build → Build Artifacts → Rebuild

### Option B: Force Source Touch
```bash
touch src/imageManipulation/ImageData.java
touch src/imageManipulation/ImageDisplayWindow.java
touch src/imageManipulation/ImageProcessorApp.java
mvn clean compile package
```

### Option C: Check POM
Verify `pom.xml` has correct source directory:
```xml
<sourceDirectory>src</sourceDirectory>
```

## Success Criteria

✅ Loading 1 image uses ~200 MB (not 987 MB)
✅ Console shows "Cached scaled image: 1538x2000"  
✅ Console shows "[MEMORY BEFORE/AFTER Posterize]" messages
✅ Each operation adds ~105 MB (not ~900 MB)
✅ Can complete 20+ operations without crash

## If It Works

You should be able to run 20+ operations easily with 4GB heap, 50+ with 8GB.
The memory will stabilize around 2GB due to history limit (20 images × 94.5 MB = 1.89 GB).
