# Kelp Chunk Threshold Feature

## How It Works

The ChunkFinder module has a **smart threshold system** for kelp chunk detection to avoid false positives and spam.

### The Problem
If ChunkFinder notified you every time it found a single perfect kelp chunk, you'd get too many notifications in ocean biomes. Most of these wouldn't be useful.

### The Solution
ChunkFinder now **counts** how many perfect kelp chunks it finds and only shows a notification after finding **9 kelp chunks** (by default).

## Behavior

### Silent Counting (Kelp Chunks 1-8)
- ChunkFinder detects perfect kelp chunks
- Tracks them internally
- **No notification, no sound, no marker**
- Just counting silently

### Notification (9th Kelp Chunk)
When the 9th perfect kelp chunk is found:
- 🔊 Sound plays (XP orb pickup)
- 📋 Toast notification appears
- 💬 Chat message: "ChunkFinder X: [coords] Z: [coords]"
- 🟩 Green marker appears at the location

This means you're in a **genuinely valuable area** with multiple perfect kelp chunks nearby!

## Configuration

You can adjust the threshold in the settings:

**Kelp Chunk Threshold** (default: 9)
- Set to 1: Notify on every kelp chunk (like old behavior)
- Set to 5: Notify after 5 kelp chunks
- Set to 9: Notify after 9 kelp chunks (recommended)
- Set to 15: Only notify in areas with many kelp chunks

## Example Scenario

Let's say you're exploring an ocean:

1. **Chunk 1**: ChunkFinder finds a perfect kelp chunk
   - Internal count: 1/9
   - You see: Nothing

2. **Chunk 2**: Another perfect kelp chunk
   - Internal count: 2/9
   - You see: Nothing

3. **Chunks 3-8**: More perfect kelp chunks
   - Internal count: 8/9
   - You see: Nothing

4. **Chunk 9**: Another perfect kelp chunk
   - Internal count: 9/9 ✅
   - **NOTIFICATION!** ChunkFinder X: -1234 Z: 5678
   - Green marker appears
   - You know this is a great area for kelp farming!

## Why This Is Useful

### For Kelp Farming
- One perfect kelp chunk = might be luck
- Nine perfect kelp chunks = this is a prime location!
- Helps you find the **best** areas, not just any area

### For Other Detections
Note: This threshold **only applies to kelp chunks**!

Other detections still notify immediately:
- Dripstone (stalactites/stalagmites)
- Vines extending to Y=16
- Pillagers
- Wandering traders

These are rarer and always worth knowing about immediately.

## Resetting the Counter

The kelp counter resets when you:
- Disable the ChunkFinder module
- Re-enable the ChunkFinder module
- Restart the game

This ensures fresh counting in each play session.

## Pro Tips

1. **Lower threshold (3-5)**: If you want to find kelp areas faster
2. **Default threshold (9)**: Balanced for finding quality locations
3. **Higher threshold (12-15)**: Only want the absolute best kelp farming regions

Adjust based on your needs!
