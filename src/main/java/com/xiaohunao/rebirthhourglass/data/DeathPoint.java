package com.xiaohunao.rebirthhourglass.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record DeathPoint(ResourceKey<Level> dimension, Vec3 position, long gameTick) {
    public static final Codec<DeathPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DeathPoint::dimension),
            Vec3.CODEC.fieldOf("position").forGetter(DeathPoint::position),
            Codec.LONG.fieldOf("game_tick").forGetter(DeathPoint::gameTick)
    ).apply(instance, DeathPoint::new));
}
