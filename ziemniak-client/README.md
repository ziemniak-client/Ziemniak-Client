# Ziemniak Client

A Meteor Client addon for Minecraft 1.21.1 Fabric that provides advanced chunk detection capabilities.

## Features

### ChunkFinder Module
Secretly detects valuable chunks and locations without revealing what was found:

- **Dripstone Detection**: Finds long stalactites (7+ blocks) and stalagmites (8+ blocks)
- **Kelp Farm Detection**: Locates perfect kelp chunks (18/20 tops at Y=62)
  - **Smart Threshold**: Only notifies after finding 9 kelp chunks (configurable) to reduce false positives
- **Vine Detection**: Detects long vines extending to Y=16
- **Pillager Detection**: Alerts when pillagers are nearby
- **Wandering Trader Detection**: Alerts when wandering traders are found

### Notifications
When something is detected:
- 🔊 Sound notification (experience orb pickup sound)
- 📋 Toast notification with coordinates
- 💬 Chat message showing coordinates
- 🟩 Green marker box at the detection location

### Customization
- Toggle each detection type individually
- Adjust minimum lengths/requirements for each feature
- Configure marker appearance and duration
- Multi-threaded scanning for better performance

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download and install [Meteor Client](https://meteorclient.com/)
4. Place the Ziemniak Client JAR file in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Building from Source

### Prerequisites
- Java 21 or higher
- Git (optional)

### Build Steps

1. Clone or download this repository
2. Open a terminal in the project directory
3. Run the build command:
   ```bash
   ./gradlew build
   ```
   On Windows:
   ```bash
   gradlew.bat build
   ```
4. The compiled JAR will be in `build/libs/`

## Usage

1. Launch Minecraft with Meteor Client and Ziemniak Client installed
2. Open the Meteor Client GUI (Right Shift by default)
3. Navigate to the "Ziemniak" category
4. Enable the "ChunkFinder" module
5. Configure detection settings as desired
6. Explore and wait for detections!

## Configuration

### Dripstone Settings
- **Detect Dripstone**: Enable/disable dripstone detection
- **Min Stalactite Length**: Minimum length for stalactites (default: 7)
- **Min Stalagmite Length**: Minimum length for stalagmites (default: 8)

### Kelp Settings
- **Detect Kelp**: Enable/disable kelp chunk detection
- **Min Kelp Columns**: Minimum kelp columns required (default: 20)
- **Min Tops at Y62**: Minimum tops at Y=62 (default: 18)
- **Kelp Chunk Threshold**: Number of kelp chunks to find before notifying (default: 9)

### Vine Settings
- **Detect Vines**: Enable/disable vine detection
- **Min Vine Length**: Minimum vine length (default: 20)

### Entity Settings
- **Detect Pillagers**: Enable/disable pillager detection
- **Detect Wandering Traders**: Enable/disable trader detection
- **Max Distance**: Maximum detection distance (default: 128)

### Render Settings
- **Marker Color**: Color of the detection marker
- **Shape Mode**: Rendering mode (Both/Sides/Lines)
- **Marker Duration**: How long markers stay visible (0 = permanent)

### Threading Settings
- **Enable Threading**: Use multi-threading for better performance
- **Thread Pool Size**: Number of threads to use (default: 2)

## License

This project is licensed under the MIT License.

## Credits

Built with [Meteor Client](https://meteorclient.com/) addon template.

## Support

If you encounter any issues or have suggestions, please open an issue on GitHub.
