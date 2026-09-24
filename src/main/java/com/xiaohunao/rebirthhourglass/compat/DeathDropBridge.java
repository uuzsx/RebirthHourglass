package com.xiaohunao.rebirthhourglass.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaohunao.rebirthhourglass.ModContent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

public final class DeathDropBridge {
    private DeathDropBridge() {}

    public static boolean spawnOrCapture(Level level, Entity entity, LivingEntity dropper, Operation<Boolean> original) {
        if (dropper instanceof ServerPlayer player && player.hasData(ModContent.PLAN)
                && entity instanceof ItemEntity item && player.captureDrops() != null) {
            // Submit the real entity to LivingDropsEvent; never both capture and spawn it.
            player.captureDrops().add(item);
            return true;
        }
        return original.call(level, entity);
    }
}
