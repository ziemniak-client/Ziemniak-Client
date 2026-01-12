package com.ziemniak.client.modules;

import com.ziemniak.client.ZiemniakClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChunkFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Single setting - show coordinates or not
    private final Setting<Boolean> showCoords = sgGeneral.add(new BoolSetting.Builder()
        .name("show-coordinates")
        .description("Show coordinates in chat and notifications")
        .defaultValue(true)
        .build()
    );

    // Hole detection mode
    private final Setting<HoleMode> holeMode = sgGeneral.add(new EnumSetting.Builder<HoleMode>()
        .name("hole-detection")
        .description("Detect chunks with or without holes")
        .defaultValue(HoleMode.Both)
        .build()
    );

    // Chunk color setting
    private final Setting<SettingColor> chunkColor = sgGeneral.add(new ColorSetting.Builder()
        .name("chunk-color")
        .description("Color of detected chunks")
        .defaultValue(new SettingColor(0, 255, 0, 100))
        .build()
    );

    // Tracer setting
    private final Setting<Boolean> showTracers = sgGeneral.add(new BoolSetting.Builder()
        .name("show-tracers")
        .description("Draw tracers to detected chunks")
        .defaultValue(false)
        .build()
    );

    // Hole detection radius - only visible when Without Holes is selected
    private final Setting<Integer> holeCheckRadius = sgGeneral.add(new IntSetting.Builder()
        .name("hole-check-radius")
        .description("Blocks radius to check for holes (higher = stricter)")
        .defaultValue(8)
        .min(0)
        .max(64)
        .sliderRange(0, 64)
        .visible(() -> holeMode.get() == HoleMode.Without_Holes)
        .build()
    );

    public enum HoleMode {
        Without_Holes("Without Holes"),
        Both("Both");

        private final String name;

        HoleMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Hardcoded detection settings (not changeable by user)
    private static final int DRIPSTONE_MIN_LENGTH = 7;
    private static final int STALAGMITE_MIN_LENGTH = 8;
    private static final int MIN_VINE_LENGTH = 20;
    private static final int ENTITY_MAX_DISTANCE = 256;
    
    // Render settings (hardcoded)
    private static final ShapeMode SHAPE_MODE = ShapeMode.Both;

    // Detection storage - use chunk positions as keys
    private final Map<ChunkPos, DetectionInfo> detectedChunks = new ConcurrentHashMap<>();
    private final Set<ChunkPos> chunksWithBrokenBlocks = ConcurrentHashMap.newKeySet(); // Track which chunks had blocks broken
    private final Set<Integer> detectedEntities = ConcurrentHashMap.newKeySet();
    private final Map<Integer, ChunkPos> entityChunkMap = new ConcurrentHashMap<>();
    private final Set<ChunkPos> scannedChunks = ConcurrentHashMap.newKeySet();

    private ExecutorService threadPool;
    
    // Area radius - only mark one chunk per area
    private static final int AREA_RADIUS = 2; // If new chunk is within 2 chunks of existing, skip it
    
    private static class DetectionInfo {
        final BlockPos position;
        final long timestamp;
        
        DetectionInfo(BlockPos position, long timestamp) {
            this.position = position;
            this.timestamp = timestamp;
        }
    }

    public ChunkFinder() {
        super(ZiemniakClient.CATEGORY, "chunk-finder", "Detects suspicious chunks [DONUT SMP ONLY]");
    }

    @Override
    public void onActivate() {
        if (mc.world == null) return;

        detectedChunks.clear();
        chunksWithBrokenBlocks.clear();
        detectedEntities.clear();
        entityChunkMap.clear();
        scannedChunks.clear();

        // Always use threading with 2 threads
        threadPool = Executors.newFixedThreadPool(2);

        // Scan all loaded chunks
        for (Chunk chunk : Utils.chunks()) {
            if (chunk instanceof WorldChunk worldChunk) {
                threadPool.submit(() -> scanChunk(worldChunk));
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            threadPool = null;
        }

        detectedChunks.clear();
        chunksWithBrokenBlocks.clear();
        detectedEntities.clear();
        entityChunkMap.clear();
        scannedChunks.clear();
    }

    @EventHandler
    private void onChunkLoad(ChunkDataEvent event) {
        if (!isActive()) return;

        WorldChunk chunk = event.chunk();
        threadPool.submit(() -> scanChunk(chunk));
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (!isActive()) return;

        BlockPos pos = event.pos;
        
        // Check if a block was broken (newState is air) in a detected chunk
        if (event.newState.isAir()) {
            ChunkPos chunkPos = new ChunkPos(pos);
            if (detectedChunks.containsKey(chunkPos)) {
                // Block was broken in a detected chunk - mark it so tracer disappears
                chunksWithBrokenBlocks.add(chunkPos);
                return;
            }
        }

        Runnable task = () -> {
            // Check for dripstone
            if (event.newState.isOf(Blocks.POINTED_DRIPSTONE)) {
                checkDripstone(pos);
            }
            // Check for vines
            if (event.newState.isOf(Blocks.VINE)) {
                checkVine(pos);
            }
        };

        threadPool.submit(task);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || !isActive()) return;

        // Check for entities
        checkEntities();

        // Render markers with user-selected color
        Color sideColor = new Color(chunkColor.get());
        Color lineColor = new Color(chunkColor.get().r, chunkColor.get().g, chunkColor.get().b, 255);

        for (Map.Entry<ChunkPos, DetectionInfo> entry : detectedChunks.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            BlockPos pos = entry.getValue().position;
            
            // Calculate chunk boundaries
            double x1 = chunkPos.getStartX();
            double z1 = chunkPos.getStartZ();
            double x2 = x1 + 16;
            double z2 = z1 + 16;
            
            // Calculate chunk center
            double centerX = x1 + 8;
            double centerZ = z1 + 8;
            double y = pos.getY();
            double centerY = y;
            
            // Always render the chunk square
            event.renderer.box(x1, y, z1, x2, y + 0.3, z2, sideColor, lineColor, SHAPE_MODE, 0);

            // Draw tracer if enabled AND no blocks have been broken in this chunk
            if (showTracers.get() && !chunksWithBrokenBlocks.contains(chunkPos)) {
                // Draw tracer line from player eye to chunk center
                double playerX = mc.player.getX();
                double playerY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
                double playerZ = mc.player.getZ();
                
                event.renderer.line(playerX, playerY, playerZ, centerX, centerY, centerZ, lineColor);
            }
        }
    }

    private void scanChunk(WorldChunk chunk) {
        ChunkPos cpos = chunk.getPos();
        if (!scannedChunks.add(cpos)) return; // Already scanned

        // Always scan for dripstone and vines
        scanChunkForDripstone(chunk);
        scanChunkForVines(chunk);
    }

    private void scanChunkForDripstone(WorldChunk chunk) {
        ChunkPos cpos = chunk.getPos();
        int xStart = cpos.getStartX();
        int zStart = cpos.getStartZ();
        int yMin = chunk.getBottomY();
        int yMax = yMin + chunk.getHeight();

        for (int x = xStart; x < xStart + 16; x++) {
            for (int z = zStart; z < zStart + 16; z++) {
                for (int y = yMin; y < yMax; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.isOf(Blocks.POINTED_DRIPSTONE)) {
                        // Check stalactites (pointing down)
                        if (state.contains(PointedDripstoneBlock.VERTICAL_DIRECTION) &&
                            state.get(PointedDripstoneBlock.VERTICAL_DIRECTION) == Direction.DOWN) {

                            int length = getStalactiteLength(pos);
                            if (length >= DRIPSTONE_MIN_LENGTH) {
                                addDetection(pos);
                            }
                        }
                        // Check stalagmites (pointing up)
                        else if (state.contains(PointedDripstoneBlock.VERTICAL_DIRECTION) &&
                            state.get(PointedDripstoneBlock.VERTICAL_DIRECTION) == Direction.UP) {

                            int length = getStalagmiteLength(pos);
                            if (length >= STALAGMITE_MIN_LENGTH) {
                                addDetection(pos);
                            }
                        }
                    }
                }
            }
        }
    }

    private void scanChunkForVines(WorldChunk chunk) {
        ChunkPos cpos = chunk.getPos();
        int xStart = cpos.getStartX();
        int zStart = cpos.getStartZ();
        int yMin = Math.max(chunk.getBottomY(), 16);
        int yMax = yMin + chunk.getHeight();

        for (int x = xStart; x < xStart + 16; x++) {
            for (int z = zStart; z < zStart + 16; z++) {
                for (int y = yMax - 1; y >= yMin; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (chunk.getBlockState(pos).isOf(Blocks.VINE)) {
                        // Check if vine extends down to y=16
                        if (vineExtendsToY16(pos)) {
                            int length = getVineLength(pos);
                            if (length >= MIN_VINE_LENGTH) {
                                addDetection(pos);
                            }
                        }
                    }
                }
            }
        }
    }

    // Check if there are holes within 5 blocks of any vine position


    private void checkDripstone(BlockPos pos) {
        if (mc.world == null) return;

        BlockState state = mc.world.getBlockState(pos);
        if (!state.isOf(Blocks.POINTED_DRIPSTONE)) return;

        if (state.contains(PointedDripstoneBlock.VERTICAL_DIRECTION)) {
            Direction dir = state.get(PointedDripstoneBlock.VERTICAL_DIRECTION);

            if (dir == Direction.DOWN) {
                int length = getStalactiteLength(pos);
                if (length >= DRIPSTONE_MIN_LENGTH) {
                    addDetection(pos);
                }
            } else if (dir == Direction.UP) {
                int length = getStalagmiteLength(pos);
                if (length >= STALAGMITE_MIN_LENGTH) {
                    addDetection(pos);
                }
            }
        }
    }

    private void checkVine(BlockPos pos) {
        if (mc.world == null) return;

        if (vineExtendsToY16(pos)) {
            int length = getVineLength(pos);
            if (length >= MIN_VINE_LENGTH) {
                addDetection(pos);
            }
        }
    }

    private void checkEntities() {
        if (mc.world == null || mc.player == null) return;

        Set<Integer> currentEntities = new HashSet<>();

        for (Entity entity : mc.world.getEntities()) {
            boolean shouldDetect = false;
            BlockPos entityPos = entity.getBlockPos();

            if (entity instanceof PillagerEntity) {
                double distance = mc.player.distanceTo(entity);
                if (distance <= ENTITY_MAX_DISTANCE) {
                    shouldDetect = true;
                    currentEntities.add(entity.getId());
                }
            }

            if (entity instanceof WanderingTraderEntity) {
                double distance = mc.player.distanceTo(entity);
                if (distance <= ENTITY_MAX_DISTANCE) {
                    shouldDetect = true;
                    currentEntities.add(entity.getId());
                }
            }

            // Only add detection if this is a NEW entity we haven't seen before
            if (shouldDetect && !detectedEntities.contains(entity.getId())) {
                detectedEntities.add(entity.getId());
                // Mark the chunk where we first detected the entity
                addDetection(entityPos);
            }
        }

        // Entities stay marked permanently (no cleanup)
    }

    private void addDetection(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        
        // Check if chunk has holes and filter based on setting
        boolean hasHoles = chunkHasHoles(chunkPos);
        
        // Only Without_Holes and Both modes exist now
        if (holeMode.get() == HoleMode.Without_Holes && hasHoles) return;
        // If mode is Both, accept all chunks

        // Check if there's already a detection nearby (within AREA_RADIUS)
        for (ChunkPos existing : detectedChunks.keySet()) {
            int dx = Math.abs(existing.x - chunkPos.x);
            int dz = Math.abs(existing.z - chunkPos.z);
            
            // If too close to an existing detection, skip this one
            if (dx <= AREA_RADIUS && dz <= AREA_RADIUS) {
                return;
            }
        }

        // Add detection immediately
        detectedChunks.put(chunkPos, new DetectionInfo(pos, System.currentTimeMillis()));

        // Always play sound
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        // Show coordinates if enabled
        if (showCoords.get()) {
            // Toast notification with coordinates
            if (mc != null) {
                String message = String.format("X: %d Z: %d", pos.getX(), pos.getZ());
                mc.getToastManager().add(new MeteorToast(Items.CHEST, "ChunkFinder", message));
            }

            // Chat message with arrow
            if (mc.player != null) {
                info("§aChunkFinder §7———> §fX: %d Z: %d", pos.getX(), pos.getZ());
            }
        } else {
            // Just show "ChunkFinder" without coordinates
            if (mc != null) {
                mc.getToastManager().add(new MeteorToast(Items.CHEST, "ChunkFinder", "Found!"));
            }
            if (mc.player != null) {
                info("§aChunkFinder");
            }
        }
    }

    private boolean chunkHasHoles(ChunkPos chunkPos) {
        if (mc.world == null) return false;

        // Get the chunk
        if (!mc.world.isChunkLoaded(chunkPos.x, chunkPos.z)) return false;
        Chunk chunk = mc.world.getChunk(chunkPos.x, chunkPos.z);
        if (!(chunk instanceof WorldChunk worldChunk)) return false;

        // Center of the chunk
        int centerX = chunkPos.getStartX() + 8;
        int centerZ = chunkPos.getStartZ() + 8;
        
        // Use user-defined radius for hole checking
        int radius = holeCheckRadius.get();
        
        // Check for holes (air pockets) in horizontal radius around center
        int airBlocks = 0;
        int totalChecked = 0;

        // Check from surface level down to find holes
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                // Check Y levels 60-70 (typical surface/cave level)
                for (int y = 60; y <= 70; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    // Make sure we can access this position
                    ChunkPos checkChunk = new ChunkPos(pos);
                    if (!mc.world.isChunkLoaded(checkChunk.x, checkChunk.z)) continue;
                    
                    BlockState state = mc.world.getBlockState(pos);
                    
                    if (state.isAir()) {
                        airBlocks++;
                    }
                    totalChecked++;
                }
            }
        }

        if (totalChecked == 0) return false;

        // If more than 10% of checked blocks are air, consider it has holes  
        // (Stricter than 30% - even small caves will be detected)
        double airPercentage = (double) airBlocks / totalChecked;
        return airPercentage > 0.10;
    }

    private int getStalactiteLength(BlockPos tipPos) {
        if (mc.world == null) return 0;

        int length = 0;
        BlockPos currentPos = tipPos;

        while (currentPos.getY() >= mc.world.getBottomY()) {
            BlockState state = mc.world.getBlockState(currentPos);

            if (!state.isOf(Blocks.POINTED_DRIPSTONE)) break;
            if (!state.contains(PointedDripstoneBlock.VERTICAL_DIRECTION)) break;

            Direction dir = state.get(PointedDripstoneBlock.VERTICAL_DIRECTION);
            if (dir != Direction.DOWN) break;

            length++;
            currentPos = currentPos.down();
        }

        return length;
    }

    private int getStalagmiteLength(BlockPos tipPos) {
        if (mc.world == null) return 0;

        int length = 0;
        BlockPos currentPos = tipPos;

        while (currentPos.getY() < 320) {
            BlockState state = mc.world.getBlockState(currentPos);

            if (!state.isOf(Blocks.POINTED_DRIPSTONE)) break;
            if (!state.contains(PointedDripstoneBlock.VERTICAL_DIRECTION)) break;

            Direction dir = state.get(PointedDripstoneBlock.VERTICAL_DIRECTION);
            if (dir != Direction.UP) break;

            length++;
            currentPos = currentPos.up();
        }

        return length;
    }

    private boolean vineExtendsToY16(BlockPos startPos) {
        if (mc.world == null) return false;

        BlockPos currentPos = startPos;

        // Trace vine downward
        while (currentPos.getY() >= 16) {
            if (!mc.world.getBlockState(currentPos).isOf(Blocks.VINE)) {
                return false;
            }

            if (currentPos.getY() == 16) {
                return true;
            }

            currentPos = currentPos.down();
        }

        return false;
    }

    private int getVineLength(BlockPos startPos) {
        if (mc.world == null) return 0;

        int length = 0;
        BlockPos currentPos = startPos;

        // Count upward
        while (mc.world.getBlockState(currentPos).isOf(Blocks.VINE)) {
            length++;
            currentPos = currentPos.up();
        }

        // Reset and count downward
        currentPos = startPos.down();
        while (mc.world.getBlockState(currentPos).isOf(Blocks.VINE)) {
            length++;
            currentPos = currentPos.down();
        }

        return length;
    }

    @Override
    public String getInfoString() {
        return detectedChunks.isEmpty() ? null : String.valueOf(detectedChunks.size());
    }
}