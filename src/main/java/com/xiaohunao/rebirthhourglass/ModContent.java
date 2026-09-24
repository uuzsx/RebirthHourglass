package com.xiaohunao.rebirthhourglass;

import com.mojang.serialization.Codec;
import com.xiaohunao.rebirthhourglass.data.DeathPlan;
import com.xiaohunao.rebirthhourglass.data.RecoveryState;
import com.xiaohunao.rebirthhourglass.item.HourglassItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.Optional;
import java.util.function.Supplier;

public final class ModContent {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, RebirthHourglass.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RebirthHourglass.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RebirthHourglass.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RebirthHourglass.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> CHARGE = COMPONENTS.register("stored_seconds", () ->
            DataComponentType.<Integer>builder().persistent(Codec.intRange(0, Integer.MAX_VALUE))
                    .networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<HourglassItem> HOURGLASS = ITEMS.registerItem("rebirth_hourglass", HourglassItem::new);
    public static final Supplier<AttachmentType<RecoveryState>> RECOVERY = ATTACHMENTS.register("recovery",
            () -> AttachmentType.builder(RecoveryState::new).serialize(RecoveryState.CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<Optional<DeathPlan>>> PLAN = ATTACHMENTS.register("death_plan",
            () -> AttachmentType.<Optional<DeathPlan>>builder(Optional::empty).build());

    static {
        TABS.register("hourglass", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.rebirth_hourglass"))
                .icon(() -> HOURGLASS.get().getDefaultInstance())
                .displayItems((parameters, output) -> output.accept(HOURGLASS.get())).build());
    }

    private ModContent() {}
    public static void register(IEventBus bus) {
        COMPONENTS.register(bus); ITEMS.register(bus); TABS.register(bus); ATTACHMENTS.register(bus);
    }
}
