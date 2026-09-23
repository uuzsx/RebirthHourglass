package com.xiaohunao.rebirthhourglass;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HourglassConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue CAPACITY = BUILDER
            .comment("Maximum stored charge, in seconds of online play. Default: 24 minutes.")
            .defineInRange("capacitySeconds", 1440, 0, 604800);
    public static final ModConfigSpec.IntValue DEATH_COST = BUILDER
            .comment("Charge consumed once for a protected death. Must not exceed capacitySeconds to be usable.")
            .defineInRange("deathCostSeconds", 360, 0, 604800);
    public static final ModConfigSpec.DoubleValue XP_FRACTION = BUILDER
            .comment("Fraction of CURRENT experience restored after protection. Remaining XP is lost; no XP orbs are spawned.")
            .defineInRange("retainedExperienceFraction", 0.5, 0, 1);
    public static final ModConfigSpec.IntValue TELEPORT_COST = BUILDER
            .comment("Initial return cost in charge seconds. Checked against the actual discounted cost.")
            .defineInRange("teleportCostSeconds", 360, 0, 604800);
    public static final ModConfigSpec.IntValue DECAY = BUILDER
            .comment("World-running seconds until return is free. Set 0 for a fixed teleport cost.")
            .defineInRange("teleportDecaySeconds", 300, 0, 604800);
    public static final ModConfigSpec.IntValue SEARCH_RADIUS = BUILDER
            .comment("Horizontal safe-landing search radius around the death point, in blocks.")
            .defineInRange("safeLandingRadius", 8, 0, 16);
    public static final ModConfigSpec.BooleanValue KEEP_HOURGLASSES = BUILDER
            .comment("Retain every dropped hourglass even when none has enough charge to protect the inventory.")
            .define("keepUnchargedHourglasses", true);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private HourglassConfig() {}
    public static int capacity() { return SPEC.isLoaded() ? CAPACITY.get() : 1440; }
    public static int deathCost() { return SPEC.isLoaded() ? DEATH_COST.get() : 360; }
    public static double experienceFraction() { return SPEC.isLoaded() ? XP_FRACTION.get() : 0.5; }
    public static int teleportCost() { return SPEC.isLoaded() ? TELEPORT_COST.get() : 360; }
    public static int decaySeconds() { return SPEC.isLoaded() ? DECAY.get() : 300; }
    public static int searchRadius() { return SPEC.isLoaded() ? SEARCH_RADIUS.get() : 8; }
    public static boolean keepHourglasses() { return !SPEC.isLoaded() || KEEP_HOURGLASSES.get(); }
}
