# Maven Setup Guide for Image Processor

## Quick Start

### For IntelliJ IDEA:
1. File → Open → Select the `image-processor` folder (containing pom.xml)
2. IntelliJ will automatically detect it's a Maven project
3. Wait for Maven to index and download dependencies
4. Right-click on ImageProcessor.java → Run 'ImageProcessor.main()'

### For Eclipse:
1. File → Import → Maven → Existing Maven Projects
2. Browse to the `image-processor` folder
3. Click Finish
4. Wait for Maven to build the workspace
5. Right-click on ImageProcessor.java → Run As → Java Application

### For VS Code:
1. Open the `image-processor` folder
2. Install "Extension Pack for Java" if not already installed
3. VS Code will detect pom.xml and configure automatically
4. Press F5 or use Run → Start Debugging

### For NetBeans:
1. File → Open Project
2. Navigate to the `image-processor` folder
3. NetBeans will recognize it as a Maven project
4. Right-click project → Run

## Maven Command Line

If your IDE doesn't automatically work, you can use Maven from the command line:

```bash
# Navigate to the project directory
cd image-processor

# Compile the project
mvn compile

# Run the application
mvn exec:java

# Package as executable JAR
mvn package

# Run the packaged JAR
java -jar target/image-processor-1.0.0.jar

# Clean build artifacts
mvn clean
```

## Project Structure

```
image-processor/
├── pom.xml                     # Maven configuration file
├── README.md                   # Full documentation
├── MAVEN_SETUP.md             # This file
└── src/
    └── main/
        └── java/
            └── ImageProcessor.java   # Main application
```

## Requirements

- Java JDK 11 or higher
- Maven 3.6 or higher (usually bundled with modern IDEs)

## Troubleshooting

### "Maven not found" error:
- Make sure Maven is installed: `mvn --version`
- Most modern IDEs include Maven bundled

### IDE not recognizing as Maven project:
- Make sure pom.xml is in the root directory
- Try reimporting: Right-click project → Maven → Reload Project

### Java version error:
- This project requires Java 11+
- Check your Java version: `java -version`
- Update IDE project SDK to Java 11 or higher

### Cannot run application:
- Make sure main class is set to: `ImageProcessor`
- No package declaration needed (it's in the default package)

## Manual Compilation (No Maven)

If you prefer not to use Maven:

```bash
# Navigate to src/main/java
cd src/main/java

# Compile
javac ImageProcessor.java

# Run
java ImageProcessor
```

## IDE Configuration Notes

### Main Class Configuration:
- Package: (default/none)
- Main Class: `ImageProcessor`
- No additional VM arguments needed
- No external dependencies required (uses only Java standard library)

### Running from IDE:
Most IDEs will show a green "Run" icon next to the main() method in ImageProcessor.java.
Simply click it to run the application.
