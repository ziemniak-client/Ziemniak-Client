# Ziemniak Client - Quick Start Guide

## Prerequisites
Before you begin, make sure you have:
- **Java 21** or higher installed
- **IntelliJ IDEA** or another Java IDE (recommended)
- Basic knowledge of Java and Gradle

## Option 1: Building the Addon

### Step 1: Set Up the Project
1. Download or clone the ziemniak-client folder
2. Open the project in IntelliJ IDEA:
   - File → Open → Select the `ziemniak-client` folder
   - Wait for Gradle to sync and download dependencies

### Step 2: Build the JAR
In the terminal within IntelliJ (or a separate terminal):

**On Windows:**
```bash
gradlew.bat build
```

**On Mac/Linux:**
```bash
./gradlew build
```

### Step 3: Find Your JAR
After building, your addon JAR will be located at:
```
build/libs/ziemniak-client-1.0.0.jar
```

### Step 4: Install the Addon
1. Locate your Minecraft installation folder:
   - **Windows:** `%APPDATA%\.minecraft`
   - **Mac:** `~/Library/Application Support/minecraft`
   - **Linux:** `~/.minecraft`

2. Copy `ziemniak-client-1.0.0.jar` to the `mods` folder

3. Make sure you also have these mods installed:
   - Fabric API (1.21.1)
   - Meteor Client (1.21.1)

### Step 5: Launch Minecraft
1. Open the Minecraft Launcher
2. Select the Fabric 1.21.1 profile
3. Click Play!

## Option 2: Development Mode (Testing)

If you want to test changes without building every time:

1. Open the project in IntelliJ IDEA
2. Run the Gradle task: `Tasks → fabric → runClient`
3. This will launch Minecraft with your addon loaded automatically

## Using ChunkFinder

Once in-game:

1. Press **Right Shift** (default) to open Meteor Client GUI
2. Navigate to the **Ziemniak** category
3. Click on **ChunkFinder** to enable it
4. Click the gear icon to configure settings:
   - Toggle detection types (Dripstone, Kelp, Vines, etc.)
   - Adjust minimum lengths and requirements
   - Customize marker colors and duration

5. Start exploring! When ChunkFinder detects something:
   - You'll hear a sound (XP orb pickup)
   - A toast notification will appear with coordinates
   - A chat message will show the coordinates
   - A green marker box will appear at the location

## Troubleshooting

### "Cannot find Meteor Client dependency"
Make sure your `build.gradle` has the correct Meteor Client repository:
```gradle
repositories {
    maven { url = "https://maven.meteordev.org/releases" }
    maven { url = "https://maven.meteordev.org/snapshots" }
}
```

### "Java version mismatch"
Ensure you're using Java 21:
- In IntelliJ: File → Project Structure → Project SDK → Select Java 21
- Check terminal: `java -version` should show version 21

### "Module not showing in Meteor Client"
1. Check that the addon JAR is in the `mods` folder
2. Make sure Meteor Client is also installed
3. Check the logs (`logs/latest.log`) for any errors
4. Look for "Initializing Ziemniak Client" in the logs

### Build fails with missing dependencies
Run this to refresh dependencies:
```bash
./gradlew clean build --refresh-dependencies
```

## Configuration Tips

### Optimal Dripstone Settings
- Min Stalactite Length: 7
- Min Stalagmite Length: 8

### Optimal Kelp Settings
- Min Kelp Columns: 20
- Min Tops at Y62: 18

### Optimal Vine Settings
- Min Vine Length: 20 (vines extending to Y=16)

### Performance Settings
- Enable Threading: ON
- Thread Pool Size: 2-4 (depending on your CPU)

## Next Steps

- Customize detection parameters for your needs
- Experiment with marker colors and durations
- Join a server and start finding valuable chunks!
- Consider adding more modules to the addon

## Support

If you encounter issues:
1. Check the logs in `.minecraft/logs/latest.log`
2. Look for error messages related to "ziemniak" or "chunk-finder"
3. Make sure all dependencies are correctly installed
4. Verify you're using Minecraft 1.21.1 with Fabric Loader 0.15.11+

Enjoy using Ziemniak Client! 🥔
