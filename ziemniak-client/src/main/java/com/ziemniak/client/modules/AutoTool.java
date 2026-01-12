package com.ziemniak.client.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;

import java.util.function.Predicate;

public class AutoTool extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-break")
        .description("Prevents tools from breaking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> antiBreakPercentage = sgGeneral.add(new IntSetting.Builder()
        .name("anti-break-percentage")
        .description("The percentage of durability at which to stop using the tool.")
        .defaultValue(5)
        .min(1)
        .max(100)
        .sliderMin(1)
        .sliderMax(100)
        .visible(antiBreak::get)
        .build()
    );

    private final Setting<Boolean> preferSilkTouch = sgGeneral.add(new BoolSetting.Builder()
        .name("prefer-silk-touch")
        .description("Prefer silk touch tools when available.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> preferFortune = sgGeneral.add(new BoolSetting.Builder()
        .name("prefer-fortune")
        .description("Prefer fortune tools when available.")
        .defaultValue(false)
        .build()
    );

    private boolean isToolSwapping;
    private int selectedToolSlot = -1;

    public AutoTool() {
        super(Categories.Player, "auto-tool", "Automatically switches to the best tool when breaking blocks.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (isToolSwapping && selectedToolSlot != -1) {
            InvUtils.swap(selectedToolSlot, false);
            isToolSwapping = false;
        }
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (mc.player == null || mc.world == null) return;

        BlockState blockState = mc.world.getBlockState(event.blockPos);
        ItemStack currentStack = mc.player.getMainHandStack();
        
        double bestEfficiency = -1.0;
        selectedToolSlot = -1;

        // Find the best tool in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            double efficiency = calculateToolEfficiency(stack, blockState, this::isToolValid);
            
            if (efficiency >= 0.0 && efficiency > bestEfficiency) {
                selectedToolSlot = i;
                bestEfficiency = efficiency;
            }
        }

        // Calculate current tool efficiency
        double currentEfficiency = calculateToolEfficiency(currentStack, blockState, this::isToolValid);

        // Switch if we found a better tool, current tool is breaking soon, or current item isn't a tool
        if ((selectedToolSlot != -1 && bestEfficiency > currentEfficiency) || 
            isToolBreakingSoon(currentStack) || 
            !isToolItemStack(currentStack)) {
            
            if (selectedToolSlot != -1) {
                InvUtils.swap(selectedToolSlot, false);
            }
        }

        // Cancel breaking if current tool is about to break
        ItemStack newStack = mc.player.getMainHandStack();
        if (isToolBreakingSoon(newStack) && isToolItemStack(newStack)) {
            event.cancel();
        }
    }

    private double calculateToolEfficiency(ItemStack itemStack, BlockState blockState, Predicate<ItemStack> predicate) {
        if (!predicate.test(itemStack) || !isToolItemStack(itemStack)) {
            return -1.0;
        }

        // Check if tool is suitable for the block
        boolean isSuitable = itemStack.isSuitableFor(blockState);
        
        // Special cases for certain tool-block combinations
        boolean isSwordOnBamboo = itemStack.getItem() instanceof SwordItem && 
                                   (blockState.getBlock() instanceof BambooBlock || 
                                    blockState.getBlock() instanceof BambooShootBlock);
        
        boolean isShearsOnLeaves = itemStack.getItem() instanceof ShearsItem && 
                                    blockState.getBlock() instanceof LeavesBlock;
        
        boolean isShearsOnWool = itemStack.getItem() instanceof ShearsItem && 
                                  blockState.isIn(BlockTags.WOOL);

        if (!isSuitable && !isSwordOnBamboo && !isShearsOnLeaves && !isShearsOnWool) {
            return -1.0;
        }

        // Calculate efficiency with mining speed multiplier
        double efficiency = itemStack.getMiningSpeedMultiplier(blockState) * 1000.0;

        // Bonus for enchantments if preferred
        if (preferSilkTouch.get() && hasSilkTouch(itemStack)) {
            efficiency += 1000.0;
        }
        
        if (preferFortune.get() && hasFortune(itemStack)) {
            efficiency += 1000.0;
        }

        return efficiency;
    }

    private boolean isToolValid(ItemStack itemStack) {
        return !isToolBreakingSoon(itemStack);
    }

    private boolean isToolBreakingSoon(ItemStack itemStack) {
        if (!antiBreak.get() || !itemStack.isDamageable()) {
            return false;
        }
        
        int maxDamage = itemStack.getMaxDamage();
        int currentDamage = itemStack.getDamage();
        int remainingDurability = maxDamage - currentDamage;
        int threshold = maxDamage * antiBreakPercentage.get() / 100;
        
        return remainingDurability < threshold;
    }

    private static boolean isToolItemStack(ItemStack itemStack) {
        return isToolItem(itemStack.getItem());
    }

    private static boolean isToolItem(Item item) {
        return item instanceof ToolItem || 
               item instanceof ShearsItem || 
               item instanceof TridentItem;
    }

    private boolean hasSilkTouch(ItemStack itemStack) {
        return itemStack.hasEnchantments() && 
               itemStack.getEnchantments().toString().contains("silk_touch");
    }

    private boolean hasFortune(ItemStack itemStack) {
        return itemStack.hasEnchantments() && 
               itemStack.getEnchantments().toString().contains("fortune");
    }
}