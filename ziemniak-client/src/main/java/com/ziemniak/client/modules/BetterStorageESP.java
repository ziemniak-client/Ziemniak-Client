package com.ziemniak.client.modules;

import com.ziemniak.client.ZiemniakClient;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class BetterStorageESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    // General settings
    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draws a line from your crosshair to the storage block.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    // Block type toggles
    private final Setting<Boolean> chests = sgGeneral.add(new BoolSetting.Builder()
        .name("chests")
        .description("Show regular chests.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> trappedChests = sgGeneral.add(new BoolSetting.Builder()
        .name("trapped-chests")
        .description("Show trapped chests.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enderChests = sgGeneral.add(new BoolSetting.Builder()
        .name("ender-chests")
        .description("Show ender chests.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> shulkerBoxes = sgGeneral.add(new BoolSetting.Builder()
        .name("shulker-boxes")
        .description("Show shulker boxes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> barrels = sgGeneral.add(new BoolSetting.Builder()
        .name("barrels")
        .description("Show barrels.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> furnaces = sgGeneral.add(new BoolSetting.Builder()
        .name("furnaces")
        .description("Show furnaces.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> spawners = sgGeneral.add(new BoolSetting.Builder()
        .name("spawners")
        .description("Show mob spawners.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enchantingTables = sgGeneral.add(new BoolSetting.Builder()
        .name("enchanting-tables")
        .description("Show enchanting tables.")
        .defaultValue(true)
        .build()
    );

    // Color settings
    private final Setting<SettingColor> chestColor = sgColors.add(new ColorSetting.Builder()
        .name("chest-color")
        .description("Color for regular chests.")
        .defaultValue(new SettingColor(156, 91, 0, 125))
        .build()
    );

    private final Setting<SettingColor> trappedChestColor = sgColors.add(new ColorSetting.Builder()
        .name("trapped-chest-color")
        .description("Color for trapped chests.")
        .defaultValue(new SettingColor(200, 91, 0, 125))
        .build()
    );

    private final Setting<SettingColor> enderChestColor = sgColors.add(new ColorSetting.Builder()
        .name("ender-chest-color")
        .description("Color for ender chests.")
        .defaultValue(new SettingColor(117, 0, 255, 125))
        .build()
    );

    private final Setting<SettingColor> shulkerBoxColor = sgColors.add(new ColorSetting.Builder()
        .name("shulker-box-color")
        .description("Color for shulker boxes.")
        .defaultValue(new SettingColor(134, 0, 158, 125))
        .build()
    );

    private final Setting<SettingColor> barrelColor = sgColors.add(new ColorSetting.Builder()
        .name("barrel-color")
        .description("Color for barrels.")
        .defaultValue(new SettingColor(255, 140, 140, 125))
        .build()
    );

    private final Setting<SettingColor> furnaceColor = sgColors.add(new ColorSetting.Builder()
        .name("furnace-color")
        .description("Color for furnaces.")
        .defaultValue(new SettingColor(125, 125, 125, 125))
        .build()
    );

    private final Setting<SettingColor> spawnerColor = sgColors.add(new ColorSetting.Builder()
        .name("spawner-color")
        .description("Color for mob spawners.")
        .defaultValue(new SettingColor(138, 126, 166, 125))
        .build()
    );

    private final Setting<SettingColor> enchantingTableColor = sgColors.add(new ColorSetting.Builder()
        .name("enchanting-table-color")
        .description("Color for enchanting tables.")
        .defaultValue(new SettingColor(80, 80, 255, 125))
        .build()
    );

    public BetterStorageESP() {
        super(ZiemniakClient.CATEGORY, "better-storage-esp", "Renders storage blocks through walls with custom colors.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        // Fixed: Iterate through chunks in render distance instead of using getLoadedChunks()
        int chunkX = mc.player.getChunkPos().x;
        int chunkZ = mc.player.getChunkPos().z;
        int renderDistance = mc.options.getViewDistance().getValue();

        for (int x = chunkX - renderDistance; x <= chunkX + renderDistance; x++) {
            for (int z = chunkZ - renderDistance; z <= chunkZ + renderDistance; z++) {
                WorldChunk chunk = mc.world.getChunk(x, z);
                if (chunk == null || chunk.isEmpty()) continue;

                // Iterate through block entities in the chunk
                for (BlockPos pos : chunk.getBlockEntityPositions()) {
                    BlockEntity blockEntity = mc.world.getBlockEntity(pos);
                    if (blockEntity == null) continue;

                    SettingColor color = getBlockEntityColor(blockEntity);
                    if (color == null) continue;

                    // Render box
                    double x1 = pos.getX() + 0.1;
                    double y1 = pos.getY() + 0.05;
                    double z1 = pos.getZ() + 0.1;
                    double x2 = pos.getX() + 0.9;
                    double y2 = pos.getY() + 0.85;
                    double z2 = pos.getZ() + 0.9;

                    event.renderer.box(x1, y1, z1, x2, y2, z2, color, color, shapeMode.get(), 0);

                    // Render tracers
                    if (tracers.get() && mc.crosshairTarget != null) {
                        Vec3d from = mc.crosshairTarget.getPos();
                        event.renderer.line(
                            from.x, from.y, from.z,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            color
                        );
                    }
                }
            }
        }
    }

    private SettingColor getBlockEntityColor(BlockEntity blockEntity) {
        if (blockEntity instanceof TrappedChestBlockEntity && trappedChests.get()) {
            return trappedChestColor.get();
        } else if (blockEntity instanceof ChestBlockEntity && chests.get()) {
            return chestColor.get();
        } else if (blockEntity instanceof EnderChestBlockEntity && enderChests.get()) {
            return enderChestColor.get();
        } else if (blockEntity instanceof ShulkerBoxBlockEntity && shulkerBoxes.get()) {
            return shulkerBoxColor.get();
        } else if (blockEntity instanceof BarrelBlockEntity && barrels.get()) {
            return barrelColor.get();
        } else if (blockEntity instanceof FurnaceBlockEntity && furnaces.get()) {
            return furnaceColor.get();
        } else if (blockEntity instanceof MobSpawnerBlockEntity && spawners.get()) {
            return spawnerColor.get();
        } else if (blockEntity instanceof EnchantingTableBlockEntity && enchantingTables.get()) {
            return enchantingTableColor.get();
        }
        return null;
    }
}