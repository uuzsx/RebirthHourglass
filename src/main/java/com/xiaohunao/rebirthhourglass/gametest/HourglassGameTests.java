package com.xiaohunao.rebirthhourglass.gametest;

import com.xiaohunao.rebirthhourglass.ModContent;
import com.xiaohunao.rebirthhourglass.RebirthHourglass;
import com.xiaohunao.rebirthhourglass.data.*;
import com.xiaohunao.rebirthhourglass.event.PlayerLifecycle;
import com.xiaohunao.rebirthhourglass.item.HourglassItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@net.neoforged.fml.common.EventBusSubscriber(modid = RebirthHourglass.MOD_ID)
public final class HourglassGameTests {
    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.registries.RegisterEvent event) {
        event.register(net.minecraft.core.registries.Registries.TEST_FUNCTION, helper -> {
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "inventorydropoutsidedeathremainsvanilla"), HourglassGameTests::inventoryDropOutsideDeathRemainsVanilla);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "multipleunchargedandoffhandsurvive"), HourglassGameTests::multipleUnchargedAndOffhandSurvive);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "protecteddeathrestoresexactlyonce"), HourglassGameTests::protectedDeathRestoresExactlyOnce);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "keepinventorydoesnotchargeormultiplyxp"), HourglassGameTests::keepInventoryDoesNotChargeOrMultiplyXp);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "laterdeathcancellationleavesinventoryuntouched"), HourglassGameTests::laterDeathCancellationLeavesInventoryUntouched);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "fullinventoryretainsunclaimeditems"), HourglassGameTests::fullInventoryRetainsUnclaimedItems);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "persistentrecoverycopiescomponentsanddimension"), HourglassGameTests::persistentRecoveryCopiesComponentsAndDimension);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "crossdimensionreturnusesexactbalance"), HourglassGameTests::crossDimensionReturnUsesExactBalance);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "unsafereturndoesnotspendcharge"), HourglassGameTests::unsafeReturnDoesNotSpendCharge);
            helper.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(RebirthHourglass.MOD_ID, "samedimensionreturnconsumestarget"), HourglassGameTests::sameDimensionReturnConsumesTarget);
        });
    }


    public static void multipleUnchargedAndOffhandSurvive(GameTestHelper test) {
        ServerPlayer player = player(test);
        boolean previous = keep(player, false);
        try {
            player.getInventory().setItem(0, sand(0));
            player.getInventory().setItem(1, sand(100));
            player.getInventory().setItem(40, sand(200));
            player.getInventory().setItem(2, new ItemStack(Items.DIAMOND, 17));
            die(player);
            test.assertTrue(dropped(player, ModContent.HOURGLASS.get()) == 0, "Recovered hourglasses must not also spawn in the world");
            test.assertTrue(dropped(player, Items.DIAMOND) == 17, "Unprotected diamonds must be emitted exactly once; found " + dropped(player, Items.DIAMOND));
            ServerPlayer next = respawn(player);
            test.assertTrue(count(next, ModContent.HOURGLASS.get()) == 3, "All three undercharged hourglasses must survive");
            test.assertTrue(count(next, Items.DIAMOND) == 0, "Unprotected diamonds must not be restored");
            test.assertTrue(next.getOffhandItem().is(ModContent.HOURGLASS.get()), "Offhand slot must be restored");
            test.assertTrue(next.getData(ModContent.RECOVERY).target().isEmpty(), "Unprotected death must not leave a return target");
            test.succeed();
        } finally { keep(player, previous); close(player); }
    }

    public static void protectedDeathRestoresExactlyOnce(GameTestHelper test) {
        ServerPlayer player = player(test);
        boolean previous = keep(player, false);
        try {
            player.getInventory().setItem(0, sand(0));
            player.getInventory().setItem(2, sand(360));
            player.getInventory().setItem(5, new ItemStack(Items.DIAMOND, 17));
            player.getInventory().setItem(38, new ItemStack(Items.IRON_CHESTPLATE));
            player.getInventory().setItem(40, new ItemStack(Items.SHIELD));
            player.experienceLevel = 27; player.experienceProgress = 0; player.totalExperience = 1395;
            die(player);
            ServerPlayer next = respawn(player);
            test.assertTrue(count(next, ModContent.HOURGLASS.get()) == 2, "An undercharged hourglass before the payer must not disappear");
            test.assertTrue(dropped(player, ModContent.HOURGLASS.get()) == 0 && dropped(player, Items.DIAMOND) == 0
                    && dropped(player, Items.IRON_CHESTPLATE) == 0 && dropped(player, Items.SHIELD) == 0,
                    "Protected inventory and equipment must not also spawn in the world");
            test.assertTrue(count(next, Items.DIAMOND) == 17, "All protected diamonds must return");
            test.assertTrue(next.getInventory().getItem(38).is(Items.IRON_CHESTPLATE), "Armor slot must survive");
            test.assertTrue(next.getOffhandItem().is(Items.SHIELD), "Offhand must survive");
            test.assertTrue(HourglassItem.charge(next.getInventory().getItem(2)) == 0, "Exact death fee must be deducted once");
            test.assertTrue(next.totalExperience == 544, "XP must be based on the current level after enchanting");
            PlayerLifecycle.restore(next, false);
            test.assertTrue(count(next, Items.DIAMOND) == 17 && next.totalExperience == 544, "Repeated restore must not duplicate assets");
            test.assertTrue(next.getData(ModContent.RECOVERY).target().isPresent(), "Protected death must retain a target");
            test.succeed();
        } finally { keep(player, previous); close(player); }
    }

    public static void keepInventoryDoesNotChargeOrMultiplyXp(GameTestHelper test) {
        ServerPlayer player = player(test);
        boolean previous = keep(player, true);
        try {
            player.getInventory().setItem(0, sand(360));
            player.getInventory().setItem(1, new ItemStack(Items.DIAMOND, 7));
            player.experienceLevel = 10; player.experienceProgress = 0; player.totalExperience = 160;
            die(player);
            ServerPlayer next = respawn(player);
            test.assertTrue(HourglassItem.charge(next.getInventory().getItem(0)) == 360, "keepInventory must not spend charge");
            test.assertTrue(next.totalExperience == 160, "keepInventory must not multiply XP");
            test.assertTrue(count(next, Items.DIAMOND) == 7, "keepInventory must not duplicate items");
            test.succeed();
        } finally { keep(player, previous); close(player); }
    }

    public static void laterDeathCancellationLeavesInventoryUntouched(GameTestHelper test) {
        ServerPlayer player = player(test);
        player.getInventory().setItem(0, sand(500));
        player.getInventory().setItem(1, new ItemStack(Items.DIAMOND, 19));
        Consumer<LivingDeathEvent> cancel = event -> {
            if (event.getEntity() == player) { event.setCanceled(true); player.setHealth(1); }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, cancel);
        try {
            die(player);
            test.assertTrue(HourglassItem.charge(player.getInventory().getItem(0)) == 500, "Canceled death must not spend charge");
            player.tickCount = 1; // Do not advance the separate once-per-second charging mechanic.
            PlayerLifecycle.tick(new PlayerTickEvent.Post(player));
            test.assertTrue(count(player, Items.DIAMOND) == 19, "Canceled death must not clear items");
            test.assertTrue(HourglassItem.charge(player.getInventory().getItem(0)) == 500, "Canceled death must not spend charge");
            test.assertTrue(!player.hasData(ModContent.RECOVERY) || !player.getData(ModContent.RECOVERY).hasPending(), "Canceled death must not mint a recovery");
            test.assertTrue(!player.hasData(ModContent.PLAN), "Canceled preparation must be discarded");
            test.succeed();
        } finally { NeoForge.EVENT_BUS.unregister(cancel); close(player); }
    }

    public static void fullInventoryRetainsUnclaimedItems(GameTestHelper test) {
        ServerPlayer player = player(test);
        try {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++)
                player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
            var state = player.getData(ModContent.RECOVERY);
            state.append(List.of(new SavedSlot("minecraft:inventory", 0, new ItemStack(Items.DIAMOND, 23))), 0);
            PlayerLifecycle.restore(player, false);
            test.assertTrue(state.hasPending(), "Full inventory must preserve a pending recovery");
            player.getInventory().setItem(5, ItemStack.EMPTY);
            PlayerLifecycle.restore(player, false);
            test.assertTrue(!state.hasPending() && count(player, Items.DIAMOND) == 23, "Freeing space must deliver the items once");
            test.succeed();
        } finally { close(player); }
    }

    public static void persistentRecoveryCopiesComponentsAndDimension(GameTestHelper test) {
        ServerPlayer player = player(test);
        try {
            RecoveryState original = new RecoveryState();
            original.target(Optional.of(new DeathPoint(Level.NETHER, new Vec3(13, 70, -41), 5_000_000_000L)));
            original.append(List.of(new SavedSlot("minecraft:inventory", 40, sand(777))), 42);
            var ops = player.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
            var encoded = RecoveryState.CODEC.codec().encodeStart(ops, original).getOrThrow();
            RecoveryState loaded = RecoveryState.CODEC.codec().parse(ops, encoded).getOrThrow();
            test.assertTrue(loaded.target().get().dimension().equals(Level.NETHER), "Dimension must survive saving");
            test.assertTrue(loaded.target().get().gameTick() == 5_000_000_000L, "Timestamp must remain a long");
            test.assertTrue(HourglassItem.charge(loaded.items().getFirst().stack()) == 777 && loaded.experience() == 42, "Item components and XP must survive saving");
            original.items(List.of());
            test.assertTrue(loaded.items().size() == 1, "Recovery copies must not share a mutable list");
            test.succeed();
        } finally { close(player); }
    }

    public static void crossDimensionReturnUsesExactBalance(GameTestHelper test) {
        ServerPlayer player = player(test);
        try {
            var nether = player.level().getServer().getLevel(Level.NETHER);
            test.assertTrue(nether != null, "Nether must exist");
            BlockPos feet = new BlockPos(120, 100, 120);
            nether.setBlockAndUpdate(feet.below(), Blocks.STONE.defaultBlockState());
            nether.setBlockAndUpdate(feet, Blocks.AIR.defaultBlockState());
            nether.setBlockAndUpdate(feet.above(), Blocks.AIR.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, sand(360));
            player.getData(ModContent.RECOVERY).target(Optional.of(new DeathPoint(Level.NETHER, Vec3.atBottomCenterOf(feet), PlayerLifecycle.gameTick(player))));
            var result = ModContent.HOURGLASS.get().use(player.level(), player, InteractionHand.MAIN_HAND);
            test.assertTrue(result.consumesAction() && player.level().dimension().equals(Level.NETHER), "Return must change dimension");
            test.assertTrue(HourglassItem.charge(player.getMainHandItem()) == 0, "Exact balance must be accepted");
            test.assertTrue(player.getData(ModContent.RECOVERY).target().isEmpty(), "Successful return must consume the target");
            test.succeed();
        } finally { close(player); }
    }

    public static void unsafeReturnDoesNotSpendCharge(GameTestHelper test) {
        ServerPlayer player = player(test);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, sand(500));
            player.getData(ModContent.RECOVERY).target(Optional.of(new DeathPoint(player.level().dimension(),
                    new Vec3(0, player.level().getMaxY() - 3, 0), PlayerLifecycle.gameTick(player))));
            var result = ModContent.HOURGLASS.get().use(player.level(), player, InteractionHand.MAIN_HAND);
            test.assertTrue(result == InteractionResult.FAIL, "Return into the sky must fail");
            test.assertTrue(HourglassItem.charge(player.getMainHandItem()) == 500, "Failed return must not spend charge");
            test.assertTrue(player.getData(ModContent.RECOVERY).target().isPresent(), "Failed return must retain the target");
            test.succeed();
        } finally { close(player); }
    }

    public static void sameDimensionReturnConsumesTarget(GameTestHelper test) {
        ServerPlayer player = player(test);
        try {
            var level = player.level();
            BlockPos feet = new BlockPos(160, 100, 160);
            level.setBlockAndUpdate(feet.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(feet, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(feet.above(), Blocks.AIR.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, sand(360));
            player.getData(ModContent.RECOVERY).target(Optional.of(new DeathPoint(level.dimension(),
                    Vec3.atBottomCenterOf(feet), PlayerLifecycle.gameTick(player))));
            var result = ModContent.HOURGLASS.get().use(player.level(), player, InteractionHand.MAIN_HAND);
            test.assertTrue(result.consumesAction() && player.position().distanceToSqr(Vec3.atBottomCenterOf(feet)) < 0.01,
                    "Return in the same dimension must reach the target");
            test.assertTrue(HourglassItem.charge(player.getMainHandItem()) == 0, "Same-dimension return must deduct the exact fee");
            test.assertTrue(player.getData(ModContent.RECOVERY).target().isEmpty(), "Same-dimension return must consume the target");
            test.succeed();
        } finally { close(player); }
    }

    public static void inventoryDropOutsideDeathRemainsVanilla(GameTestHelper test) {
        ServerPlayer player = player(test);
        try {
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 17));
            player.getInventory().setItem(40, new ItemStack(Items.SHIELD));
            int diamondsBefore = dropped(player, Items.DIAMOND);
            int shieldsBefore = dropped(player, Items.SHIELD);
            player.getInventory().dropAll();
            test.assertTrue(player.getInventory().isEmpty(), "Ordinary dropAll must empty the inventory");
            test.assertTrue(dropped(player, Items.DIAMOND) - diamondsBefore == 17
                    && dropped(player, Items.SHIELD) - shieldsBefore == 1,
                    "Outside death capture, inventory and equipment must spawn normally and exactly once");
            test.assertTrue(!player.hasData(ModContent.RECOVERY) || !player.getData(ModContent.RECOVERY).hasPending(),
                    "Ordinary drops must not create recovery items");
            test.succeed();
        } finally { close(player); }
    }

    private static int dropped(ServerPlayer player, net.minecraft.world.item.Item item) {
        return player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                player.getBoundingBox().inflate(3)).stream().filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    @SuppressWarnings("removal")
    private static ServerPlayer player(GameTestHelper test) {
        ServerPlayer player = test.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        player.snapTo(test.absoluteVec(new Vec3(1, 1, 1)));
        return player;
    }
    private static ItemStack sand(int seconds) {
        ItemStack stack = new ItemStack(ModContent.HOURGLASS.get());
        stack.set(ModContent.CHARGE.get(), seconds);
        return stack;
    }
    private static void die(ServerPlayer player) {
        player.setHealth(0);
        player.die(player.damageSources().genericKill());
    }
    private static ServerPlayer respawn(ServerPlayer player) {
        return player.level().getServer().getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);
    }
    private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }
    private static boolean keep(ServerPlayer player, boolean enabled) {
        var rules = player.level().getGameRules();
        boolean old = rules.get(GameRules.KEEP_INVENTORY);
        rules.set(GameRules.KEEP_INVENTORY, enabled, player.level().getServer());
        return old;
    }
    private static void close(ServerPlayer player) {
        ServerPlayer current = player.level().getServer().getPlayerList().getPlayer(player.getUUID());
        if (current != null) player.level().getServer().getPlayerList().remove(current);
    }
}
