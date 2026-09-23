package com.xiaohunao.rebirthhourglass.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public final class InventoryAdapters {
    private static final Map<String, InventoryAdapter> ADAPTERS = new ConcurrentSkipListMap<>();
    static {
        register(ResourceLocation.withDefaultNamespace("inventory"), new InventoryAdapter() {
            @Override public int size(ServerPlayer player) { return player.getInventory().getContainerSize(); }
            @Override public ItemStack get(ServerPlayer player, int slot) { return player.getInventory().getItem(slot); }
            @Override public ItemStack restore(ServerPlayer player, int slot, ItemStack stack) {
                var inventory = player.getInventory();
                if (slot >= 0 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
                    inventory.setItem(slot, stack);
                    return ItemStack.EMPTY;
                }
                // Inventory.add discards an uninsertable remainder for creative players.
                // Recovery must conserve the remainder in every game mode.
                for (int i = 0; i < inventory.items.size() && !stack.isEmpty(); i++) {
                    ItemStack existing = inventory.getItem(i);
                    if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                        int room = Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize()) - existing.getCount();
                        int moved = Math.min(Math.max(0, room), stack.getCount());
                        existing.grow(moved);
                        stack.shrink(moved);
                    }
                }
                for (int i = 0; i < inventory.items.size() && !stack.isEmpty(); i++) {
                    if (inventory.getItem(i).isEmpty()) {
                        int moved = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize()));
                        inventory.setItem(i, stack.copyWithCount(moved));
                        stack.shrink(moved);
                    }
                }
                return stack.isEmpty() ? ItemStack.EMPTY : stack;
            }
        });
    }
    private InventoryAdapters() {}
    public static void register(ResourceLocation id, InventoryAdapter adapter) {
        if (ADAPTERS.putIfAbsent(id.toString(), java.util.Objects.requireNonNull(adapter)) != null)
            throw new IllegalArgumentException("Duplicate inventory adapter: " + id);
    }
    public static Map<String, InventoryAdapter> all() { return java.util.Collections.unmodifiableMap(ADAPTERS); }
    public static InventoryAdapter get(String id) { return ADAPTERS.get(id); }
}
