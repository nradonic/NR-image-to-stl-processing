# Quick Start Guide

## First Time Setup


This is a **source distribution** - you must compile before running.

---
## Method 0: Use Maven inside Intelli-J

## Method 1: Using Maven Command Line (Recommended)

```bash
# Navigate to project directory
cd image-processor-XX

# Compile
mvn compile

# Run
mvn exec:java
```

---

## Method 2: Direct Compilation

```bash
# Navigate to project directory
cd image-processor-XX

# Create build directory
mkdir -p build

# Compile all Java files
cd src/main/java
javac -d ../../../build *.java

# Go back to project root
cd ../../..

# Run
java -cp build imageManipulation.ImageProcessorMain
```

---

## Quick Commands

**One-line compile and run:**
```bash
cd image-processor-XX/src/main/java && javac -d ../../../build *.java && cd ../../.. && java -cp build imageManipulation.ImageProcessorMain
```

**Or using Maven:**
```bash
cd image-processor-XX && mvn compile && mvn exec:java
```

---

## What You'll See

After running, the application window opens:
1. Click "Load Image" to select an image file
2. Use processing buttons (Posterize, Monochrome, Scale, etc.)
3. Each operation opens a new window with the result
4. Click log entries to select that image as source

---

## Troubleshooting

**"javac: command not found"**
- Install Java Development Kit (JDK 8 or higher)
- Make sure `javac` is in your PATH

**"mvn: command not found"**
- Install Maven from https://maven.apache.org/
- Or use Method 2 (direct compilation) instead

**"Main method not found"**
- Make sure you're running `imageManipulation.ImageProcessorMain` (not `ImageProcessor`)
- Check that you compiled all .java files

---

## File Structure

```
image-processor-7/
├── README.md              (Full documentation)
├── CHANGELOG.md           (All changes)
├── MAVEN_SETUP.md         (Maven details)
├── TODO.txt               (Feature list)
├── pom.xml                (Maven config)
└── src/main/java/         (9 Java files)
```

---

## Next Steps

Read **README.md** for complete documentation including:
- All features
- Usage instructions
- Technical details
- Architecture overview

---

## Support

For issues or questions:
1. Check README.md first
2. Review CHANGELOG.md for recent changes
3. Check Maven logs for compilation errors
