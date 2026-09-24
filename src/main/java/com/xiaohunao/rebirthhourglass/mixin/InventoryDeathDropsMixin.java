package com.xiaohunao.rebirthhourglass.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.xiaohunao.rebirthhourglass.compat.DeathDropBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** 26.3 inventory drops bypass NeoForge's capture buffer unless bridged here. */
@Mixin(Inventory.class)
abstract class InventoryDeathDropsMixin {
    @Shadow @Final public Player player;

    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean rebirthHourglass$capture(Level level, Entity entity, Operation<Boolean> original) {
        return DeathDropBridge.spawnOrCapture(level, entity, this.player, original);
    }
}
