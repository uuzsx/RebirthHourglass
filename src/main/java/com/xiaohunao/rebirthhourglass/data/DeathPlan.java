package com.xiaohunao.rebirthhourglass.data;

import java.util.List;

/** Transient preparation only: no inventory/charge mutation until actual drops happen. */
public final class DeathPlan {
    public final List<SavedSlot> slots;
    public final SavedSlot payer;
    public final DeathPoint point;
    public final boolean keepInventory;
    public final int fee;
    public final int experience;
    public int suppressedOrbs;

    public DeathPlan(List<SavedSlot> slots, SavedSlot payer, DeathPoint point, boolean keepInventory, int fee, int experience) {
        this.slots = List.copyOf(slots);
        this.payer = payer;
        this.point = point;
        this.keepInventory = keepInventory;
        this.fee = fee;
        this.experience = experience;
    }
    public boolean protects() { return payer != null && !keepInventory; }
}
