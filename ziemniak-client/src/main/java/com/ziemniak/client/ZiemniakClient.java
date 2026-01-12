package com.ziemniak.client;

import com.mojang.logging.LogUtils;
import com.ziemniak.client.modules.ChunkFinder;
import com.ziemniak.client.modules.BetterStorageESP;
import com.ziemniak.client.modules.AutoTool;
import com.ziemniak.client.modules.PlayerESP;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class ZiemniakClient extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Ziemniak");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Ziemniak Client");

        // Register modules
        Modules.get().add(new ChunkFinder());
        Modules.get().add(new BetterStorageESP());
        Modules.get().add(new AutoTool());
        Modules.get().add(new PlayerESP());

        LOG.info("Ziemniak Client initialized!");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.ziemniak.client";
    }
}