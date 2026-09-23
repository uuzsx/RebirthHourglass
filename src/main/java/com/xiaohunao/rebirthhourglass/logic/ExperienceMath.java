package com.xiaohunao.rebirthhourglass.logic;

/** Reconstructs currently spendable XP; Player.totalExperience is not the current balance after enchanting. */
public final class ExperienceMath {
    private ExperienceMath() {}

    public static int pointsAtLevel(int level) {
        double n = Math.max(0, level);
        double points = level <= 16 ? n * n + 6 * n
                : level <= 31 ? 2.5 * n * n - 40.5 * n + 360
                : 4.5 * n * n - 162.5 * n + 2220;
        return (int) Math.min(Integer.MAX_VALUE, points);
    }

    public static int currentPoints(int level, float progress, int nextLevelCost) {
        double fraction = Float.isFinite(progress) ? Math.clamp(progress, 0.0f, 1.0f) : 0;
        long partial = Math.round(fraction * Math.max(0, nextLevelCost));
        return (int) Math.min(Integer.MAX_VALUE, (long) pointsAtLevel(level) + partial);
    }

    public static int retained(int currentPoints, double fraction) {
        double ratio = Double.isFinite(fraction) ? Math.clamp(fraction, 0.0, 1.0) : 0;
        return (int) Math.floor(Math.max(0, currentPoints) * ratio);
    }
}
