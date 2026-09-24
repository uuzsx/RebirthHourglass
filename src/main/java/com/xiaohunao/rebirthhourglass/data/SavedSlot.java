package com.xiaohunao.rebirthhourglass.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public record SavedSlot(String adapter, int slot, ItemStack stack) {
    public static final Codec<SavedSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("adapter").forGetter(SavedSlot::adapter),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("slot").forGetter(SavedSlot::slot),
            ItemStack.CODEC.fieldOf("stack").forGetter(SavedSlot::stack)
    ).apply(instance, SavedSlot::new));
    public SavedSlot copy() { return new SavedSlot(adapter, slot, stack.copy()); }
}
