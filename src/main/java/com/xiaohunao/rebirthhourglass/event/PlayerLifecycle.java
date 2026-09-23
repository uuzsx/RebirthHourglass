package com.xiaohunao.rebirthhourglass.event;

import com.xiaohunao.rebirthhourglass.HourglassConfig;
import com.xiaohunao.rebirthhourglass.ModContent;
import com.xiaohunao.rebirthhourglass.api.InventoryAdapters;
import com.xiaohunao.rebirthhourglass.data.*;
import com.xiaohunao.rebirthhourglass.item.HourglassItem;
import com.xiaohunao.rebirthhourglass.logic.ChargeRules;
import com.xiaohunao.rebirthhourglass.logic.ExperienceMath;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

public final class PlayerLifecycle {
    private PlayerLifecycle() {}
    public static long gameTick(ServerPlayer player) { return player.server.overworld().getGameTime(); }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void prepare(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        List<SavedSlot> slots = new ArrayList<>();
        InventoryAdapters.all().forEach((id, adapter) -> {
            for (int slot = 0; slot < adapter.size(player); slot++) {
                ItemStack stack = adapter.get(player, slot);
                if (!stack.isEmpty()) slots.add(new SavedSlot(id, slot, stack.copy()));
            }
        });
        SavedSlot payer = null;
        for (SavedSlot slot : slots) {
            if (slot.stack().is(ModContent.HOURGLASS.get()) && HourglassItem.charge(slot.stack()) >= HourglassConfig.deathCost()
                    && (payer == null || HourglassItem.charge(slot.stack()) > HourglassItem.charge(payer.stack()))) payer = slot;
        }
        int currentXp = ExperienceMath.currentPoints(player.experienceLevel, player.experienceProgress, player.getXpNeededForNextLevel());
        player.setData(ModContent.PLAN, Optional.of(new DeathPlan(slots, payer,
                new DeathPoint(player.level().dimension(), player.position(), gameTick(player)),
                player.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY),
                HourglassConfig.deathCost(), ExperienceMath.retained(currentXp, HourglassConfig.experienceFraction()))));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void experience(LivingExperienceDropEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getData(ModContent.PLAN).filter(DeathPlan::protects).ifPresent(plan -> {
            plan.suppressedOrbs = Math.max(0, event.getDroppedExperience());
            event.setDroppedExperience(0);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void capture(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Optional<DeathPlan> prepared = player.getData(ModContent.PLAN);
        if (prepared.isEmpty()) return;
        DeathPlan plan = prepared.get();
        player.removeData(ModContent.PLAN);
        RecoveryState state = player.getData(ModContent.RECOVERY);
        // A new actual death invalidates a previously unused return, even if this death is unprotected.
        state.target(Optional.empty());

        if (plan.keepInventory) {
            if (plan.payer != null) state.target(Optional.of(plan.point));
            return;
        }
        if (event.isCanceled()) {
            restoreSuppressedOrbs(player, plan);
            return;
        }

        // Claim only items that really reached the drop pipeline. This respects vanishing curses
        // and inventories retained by other mods, and never restores a snapshot in addition to a drop.
        Map<SavedSlot, ItemEntity> matched = new LinkedHashMap<>();
        Set<ItemEntity> assigned = Collections.newSetFromMap(new IdentityHashMap<>());
        for (SavedSlot slot : plan.slots) {
            for (ItemEntity drop : event.getDrops()) {
                if (!assigned.contains(drop) && ItemStack.matches(slot.stack(), drop.getItem())) {
                    matched.put(slot, drop);
                    assigned.add(drop);
                    break;
                }
            }
        }

        boolean protectAll = plan.protects() && matched.containsKey(plan.payer);
        List<SavedSlot> saved = new ArrayList<>();
        Set<ItemEntity> claimed = Collections.newSetFromMap(new IdentityHashMap<>());
        matched.forEach((slot, drop) -> {
            if (protectAll || HourglassConfig.keepHourglasses() && slot.stack().is(ModContent.HOURGLASS.get())) {
                ItemStack copy = drop.getItem().copy();
                if (protectAll && slot == plan.payer)
                    copy.set(ModContent.CHARGE.get(), ChargeRules.consume(HourglassItem.charge(copy), plan.fee));
                saved.add(new SavedSlot(slot.adapter(), slot.slot(), copy));
                claimed.add(drop);
            }
        });
        state.append(saved, protectAll ? plan.experience : 0);
        if (protectAll) state.target(Optional.of(plan.point));
        else restoreSuppressedOrbs(player, plan);
        event.getDrops().removeIf(claimed::contains);
    }

    private static void restoreSuppressedOrbs(ServerPlayer player, DeathPlan plan) {
        if (plan.suppressedOrbs > 0) ExperienceOrb.award(player.serverLevel(), player.position(), plan.suppressedOrbs);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player, true);
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player, true);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
        // A later listener may have canceled death. Transient preparation carries no spent charge/items.
        if (player.hasData(ModContent.PLAN)) player.removeData(ModContent.PLAN);
        if (player.tickCount % 20 != 0) return;
        InventoryAdapters.all().values().forEach(adapter -> {
            for (int i = 0; i < adapter.size(player); i++) {
                ItemStack stack = adapter.get(player, i);
                if (stack.is(ModContent.HOURGLASS.get())) {
                    int current = HourglassItem.charge(stack);
                    int next = ChargeRules.charge(current, HourglassConfig.capacity());
                    if (next != current) stack.set(ModContent.CHARGE.get(), next);
                }
            }
        });
        restore(player, false);
    }

    public static void restore(ServerPlayer player, boolean notify) {
        if (!player.isAlive() || !player.hasData(ModContent.RECOVERY)) return;
        RecoveryState state = player.getData(ModContent.RECOVERY);
        if (!state.hasPending()) return;
        List<SavedSlot> remaining = new ArrayList<>();
        int before = state.items().size();
        for (SavedSlot saved : state.items()) {
            var adapter = InventoryAdapters.get(saved.adapter());
            ItemStack rest = adapter == null ? saved.stack().copy()
                    : adapter.restore(player, saved.slot(), saved.stack().copy());
            if (!rest.isEmpty()) remaining.add(new SavedSlot(saved.adapter(), saved.slot(), rest.copy()));
        }
        state.items(remaining);
        int xp = state.experience();
        state.experience(0);
        if (xp > 0) player.giveExperiencePoints(xp);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (notify) player.displayClientMessage(Component.translatable(
                remaining.isEmpty() ? "message.rebirth_hourglass.restored" : "message.rebirth_hourglass.pending",
                before - remaining.size(), remaining.size()), false);
    }
}
