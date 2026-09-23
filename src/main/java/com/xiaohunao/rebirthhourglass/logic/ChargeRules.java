package com.xiaohunao.rebirthhourglass.logic;

/** Pure gameplay arithmetic; all charge and costs are expressed in seconds. */
public final class ChargeRules {
    private ChargeRules() {}

    public static int charge(int stored, int capacity) {
        int cap = Math.max(0, capacity);
        return (int) Math.min(cap, (long) Math.max(0, stored) + 1);
    }

    public static int teleportCost(int base, long elapsedTicks, int decaySeconds) {
        if (base <= 0) return 0;
        if (decaySeconds <= 0) return base;
        long duration = (long) decaySeconds * 20;
        long remaining = duration - Math.min(duration, Math.max(0, elapsedTicks));
        // Round up so a positive fractional cost is never silently free.
        return (int) (((long) base * remaining + duration - 1) / duration);
    }

    public static int consume(int stored, int cost) {
        if (cost < 0 || stored < cost) throw new IllegalArgumentException("Insufficient charge or negative cost");
        return stored - cost;
    }
}
