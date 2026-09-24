package com.xiaohunao.rebirthhourglass.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persistent per-player recovery, including items awaiting free inventory space. */
public final class RecoveryState {
    public static final MapCodec<RecoveryState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DeathPoint.CODEC.optionalFieldOf("target").forGetter(RecoveryState::target),
            SavedSlot.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(RecoveryState::items),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("experience", 0).forGetter(RecoveryState::experience)
    ).apply(instance, RecoveryState::new));

    public RecoveryState() {}
    private RecoveryState(Optional<DeathPoint> target, List<SavedSlot> items, int experience) {
        target(target); items(items); experience(experience);
    }

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

}
