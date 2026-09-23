package com.xiaohunao.rebirthhourglass.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Integration point for extra inventories. Register once during common setup.
 * get() returns the live stack. restore() inserts without overwriting occupied slots
 * and returns the uninserted remainder. Adapters must not expose the same slots twice.
 * Death protection claims only matching item entities in the normal death-drop event.
 */
public interface InventoryAdapter {
    int size(ServerPlayer player);
    ItemStack get(ServerPlayer player, int slot);
    ItemStack restore(ServerPlayer player, int originalSlot, ItemStack stack);
}
