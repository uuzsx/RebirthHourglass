package com.xiaohunao.rebirthhourglass.item;

import com.xiaohunao.rebirthhourglass.HourglassConfig;
import com.xiaohunao.rebirthhourglass.ModContent;
import com.xiaohunao.rebirthhourglass.event.PlayerLifecycle;
import com.xiaohunao.rebirthhourglass.logic.ChargeRules;
import com.xiaohunao.rebirthhourglass.teleport.SafeLanding;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Optional;

public final class HourglassItem extends Item {
    public HourglassItem(Properties properties) {
        super(properties.stacksTo(1).component(ModContent.CHARGE.get(), 0));
    }

    public static int charge(ItemStack stack) { return Math.max(0, stack.getOrDefault(ModContent.CHARGE.get(), 0)); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flags) {
        lines.add(Component.translatable("tooltip.rebirth_hourglass.charge", time(charge(stack)), time(HourglassConfig.capacity())).withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.rebirth_hourglass.protection", time(HourglassConfig.deathCost()),
                (int) Math.round(HourglassConfig.experienceFraction() * 100)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.rebirth_hourglass.return").withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override public boolean isBarVisible(ItemStack stack) { return charge(stack) > 0; }
    @Override public int getBarWidth(ItemStack stack) { return Math.clamp(Math.round(13f * charge(stack) / Math.max(1, HourglassConfig.capacity())), 0, 13); }
    @Override public int getBarColor(ItemStack stack) { return 0xE9C46A; }
    @Override public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() == null ? InteractionResult.PASS : use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.success(stack);
        if (!serverPlayer.isAlive() || serverPlayer.isSpectator()) return InteractionResultHolder.fail(stack);
        var state = serverPlayer.getData(ModContent.RECOVERY);
        if (state.target().isEmpty()) return fail(serverPlayer, stack, "no_target");
        var target = state.target().get();
        int cost = ChargeRules.teleportCost(HourglassConfig.teleportCost(),
                PlayerLifecycle.gameTick(serverPlayer) - target.gameTick(), HourglassConfig.decaySeconds());
        if (charge(stack) < cost) {
            serverPlayer.displayClientMessage(Component.translatable("message.rebirth_hourglass.insufficient", time(cost), time(charge(stack))), true);
            return InteractionResultHolder.fail(stack);
        }
        var destination = serverPlayer.server.getLevel(target.dimension());
        if (destination == null) return fail(serverPlayer, stack, "missing_dimension");
        var landing = SafeLanding.find(destination, serverPlayer, target.position(), HourglassConfig.searchRadius());
        if (landing.isEmpty()) return fail(serverPlayer, stack, "unsafe");
        var transported = serverPlayer.changeDimension(new DimensionTransition(destination, landing.get(), Vec3.ZERO,
                serverPlayer.getYRot(), serverPlayer.getXRot(), DimensionTransition.PLACE_PORTAL_TICKET));
        if (!(transported instanceof ServerPlayer moved)) return fail(serverPlayer, stack, "blocked");
        // Travel events may veto teleportation. Charge and one-shot target are committed only on success.
        stack.set(ModContent.CHARGE.get(), ChargeRules.consume(charge(stack), cost));
        moved.getData(ModContent.RECOVERY).target(Optional.empty());
        moved.fallDistance = 0;
        moved.getInventory().setChanged();
        moved.inventoryMenu.broadcastChanges();
        moved.displayClientMessage(Component.translatable("message.rebirth_hourglass.returned", time(cost)), true);
        return InteractionResultHolder.consume(stack);
    }

    private static InteractionResultHolder<ItemStack> fail(ServerPlayer player, ItemStack stack, String key) {
        player.displayClientMessage(Component.translatable("message.rebirth_hourglass." + key).withStyle(ChatFormatting.RED), true);
        return InteractionResultHolder.fail(stack);
    }

    public static String time(int seconds) {
        int clamped = Math.max(0, seconds);
        return String.format(java.util.Locale.ROOT, "%d:%02d:%02d", clamped / 3600, clamped / 60 % 60, clamped % 60);
    }
}
