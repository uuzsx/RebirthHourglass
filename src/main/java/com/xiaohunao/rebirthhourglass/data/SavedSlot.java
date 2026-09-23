package com.xiaohunao.rebirthhourglass.data;

import net.minecraft.world.item.ItemStack;

/** Stack is owned by this snapshot; callers must copy it before restoring or charging. */
public record SavedSlot(String adapter, int slot, ItemStack stack) {
    public SavedSlot copy() { return new SavedSlot(adapter, slot, stack.copy()); }
}
