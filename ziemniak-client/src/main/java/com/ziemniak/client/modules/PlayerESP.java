package com.ziemniak.client.modules;

import com.ziemniak.client.ZiemniakClient;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class PlayerESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draws a line from your crosshair to the player.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the box.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the box.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("The color of the tracer line.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    public PlayerESP() {
        super(ZiemniakClient.CATEGORY, "player-esp", "Renders players through walls with boxes and tracers.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            // Fixed: Use player's lerped position instead of getCameraTarget
            Vec3d pos = player.getLerpedPos(event.tickDelta);
            
            double x = pos.x;
            double y = pos.y;
            double z = pos.z;
            double width = player.getWidth() / 2.0;
            double height = player.getHeight();

            event.renderer.box(
                x - width, y, z - width,
                x + width, y + height, z + width,
                sideColor.get(), lineColor.get(),
                shapeMode.get(), 0
            );

            if (tracers.get() && mc.crosshairTarget != null) {
                Vec3d from = mc.crosshairTarget.getPos();
                event.renderer.line(
                    from.x, from.y, from.z,
                    x, y + height / 2, z,
                    tracerColor.get()
                );
            }
        }
    }
}