package com.xiaohunao.rebirthhourglass.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public record DeathPoint(ResourceKey<Level> dimension, Vec3 position, long gameTick) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension.location().toString());
        tag.putDouble("x", position.x); tag.putDouble("y", position.y); tag.putDouble("z", position.z);
        tag.putLong("game_tick", gameTick);
        return tag;
    }

    public static Optional<DeathPoint> read(CompoundTag tag) {
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        double x = tag.getDouble("x"), y = tag.getDouble("y"), z = tag.getDouble("z");
        if (dimension == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return Optional.empty();
        return Optional.of(new DeathPoint(ResourceKey.create(Registries.DIMENSION, dimension),
                new Vec3(x, y, z), tag.getLong("game_tick")));
    }
}
