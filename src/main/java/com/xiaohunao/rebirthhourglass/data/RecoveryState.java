package com.xiaohunao.rebirthhourglass.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persistent per-player recovery, including items awaiting free inventory space. */
public final class RecoveryState implements INBTSerializable<CompoundTag> {
    private Optional<DeathPoint> target = Optional.empty();
    private List<SavedSlot> items = new ArrayList<>();
    private int experience;

    public Optional<DeathPoint> target() { return target; }
    public void target(Optional<DeathPoint> value) { target = value; }
    public List<SavedSlot> items() { return items.stream().map(SavedSlot::copy).toList(); }
    public void items(List<SavedSlot> value) { items = new ArrayList<>(value.stream().map(SavedSlot::copy).toList()); }
    public int experience() { return experience; }
    public void experience(int value) { experience = Math.max(0, value); }
    public boolean hasPending() { return !items.isEmpty() || experience != 0; }

    public void append(List<SavedSlot> restored, int xp) {
        restored.forEach(slot -> items.add(slot.copy()));
        experience = (int) Math.min(Integer.MAX_VALUE, (long) experience + Math.max(0, xp));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", 1);
        target.ifPresent(point -> tag.put("target", point.save()));
        tag.putInt("experience", experience);
        ListTag stored = new ListTag();
        for (SavedSlot slot : items) {
            if (slot.stack().isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putString("adapter", slot.adapter());
            entry.putInt("slot", slot.slot());
            entry.put("stack", slot.stack().save(provider));
            stored.add(entry);
        }
        tag.put("items", stored);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        target = tag.contains("target", Tag.TAG_COMPOUND) ? DeathPoint.read(tag.getCompound("target")) : Optional.empty();
        experience = Math.max(0, tag.getInt("experience"));
        items.clear();
        ListTag stored = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < stored.size(); i++) {
            CompoundTag entry = stored.getCompound(i);
            ItemStack.parse(provider, entry.getCompound("stack")).ifPresent(stack ->
                    items.add(new SavedSlot(entry.getString("adapter"), entry.getInt("slot"), stack)));
        }
    }
}
