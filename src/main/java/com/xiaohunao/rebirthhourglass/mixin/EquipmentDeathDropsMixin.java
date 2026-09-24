package com.xiaohunao.rebirthhourglass.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.xiaohunao.rebirthhourglass.compat.DeathDropBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep armor and offhand drops in the same death transaction as inventory drops. */
@Mixin(EntityEquipment.class)
abstract class EquipmentDeathDropsMixin {
    @WrapOperation(method = "dropAll", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean rebirthHourglass$capture(Level level, Entity entity, Operation<Boolean> original, LivingEntity dropper) {
        return DeathDropBridge.spawnOrCapture(level, entity, dropper, original);
    }
}
