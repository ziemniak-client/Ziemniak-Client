# Ziemniak Client - Project Structure

```
ziemniak-client/
│
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties      # Gradle wrapper configuration
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── ziemniak/
│       │           └── client/
│       │               ├── ZiemniakClient.java          # Main addon class
│       │               └── modules/
│       │                   └── ChunkFinder.java         # ChunkFinder module
│       │
│       └── resources/
│           ├── fabric.mod.json                          # Fabric mod metadata
│           └── assets/
│               └── ziemniak-client/
│                   └── ICON_README.txt                  # Icon placeholder
│
├── build.gradle                          # Gradle build configuration
├── gradle.properties                     # Gradle properties
├── settings.gradle                       # Gradle settings
├── gradlew                              # Gradle wrapper script (Unix/Mac)
├── gradlew.bat                          # Gradle wrapper script (Windows)
├── .gitignore                           # Git ignore file
├── LICENSE                              # MIT License
├── README.md                            # Main documentation
├── QUICK_START.md                       # Quick start guide
└── PROJECT_STRUCTURE.md                 # This file!
```

## Key Files Explained

### ZiemniakClient.java
The main entry point for your addon. This file:
- Initializes the addon
- Creates the "Ziemniak" category in Meteor Client
- Registers all modules (currently ChunkFinder)

### ChunkFinder.java
The main module that provides:
- Dripstone detection (stalactites & stalagmites)
- Kelp farm detection (perfect chunks)
- Vine detection (extending to Y=16)
- Pillager detection
- Wandering trader detection
- Visual markers and notifications

### build.gradle
Defines:
- Minecraft version (1.21.1)
- Fabric Loader version
- Meteor Client dependency
- Build tasks and configurations

### fabric.mod.json
Fabric metadata file that tells Fabric:
- Mod ID: `ziemniak-client`
- Mod name: `Ziemniak Client`
- Entry point: Uses Meteor's addon system
- Dependencies: Minecraft, Fabric Loader, Meteor Client

## How to Add New Modules

1. Create a new Java file in `src/main/java/com/ziemniak/client/modules/`
2. Extend the `Module` class from Meteor Client
3. Register it in `ZiemniakClient.java` in the `onInitialize()` method:
   ```java
   Modules.get().add(new YourNewModule());
   ```

Example:
```java
package com.ziemniak.client.modules;

import com.ziemniak.client.ZiemniakClient;
import meteordevelopment.meteorclient.systems.modules.Module;

public class YourNewModule extends Module {
    public YourNewModule() {
        super(ZiemniakClient.CATEGORY, "your-module-name", "Description");
    }
    
    @Override
    public void onActivate() {
        // Module activated
    }
    
    @Override
    public void onDeactivate() {
        // Module deactivated
    }
}
```

## Build Output

After running `./gradlew build`, you'll find:
- Compiled JAR: `build/libs/ziemniak-client-1.0.0.jar`
- Sources JAR: `build/libs/ziemniak-client-1.0.0-sources.jar`

## Development Workflow

1. **Edit code** in IntelliJ IDEA or your preferred IDE
2. **Test changes** by running `Tasks → fabric → runClient` in Gradle
3. **Build JAR** with `./gradlew build` when ready to release
4. **Install** by copying the JAR to `.minecraft/mods/`

## Dependencies

The project depends on:
- **Minecraft 1.21.1** - The game
- **Fabric Loader 0.15.11+** - Mod loader
- **Fabric API 0.102.0+1.21.1** - Fabric API
- **Meteor Client 0.5.8-SNAPSHOT+1.21.1** - Base client

All dependencies are automatically downloaded by Gradle during build.
