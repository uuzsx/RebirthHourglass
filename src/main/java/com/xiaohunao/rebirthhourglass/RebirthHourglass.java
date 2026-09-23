package com.xiaohunao.rebirthhourglass;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import com.xiaohunao.rebirthhourglass.event.PlayerLifecycle;

@Mod(RebirthHourglass.MOD_ID)
public final class RebirthHourglass {
    public static final String MOD_ID = "rebirth_hourglass";
    public RebirthHourglass(IEventBus bus, ModContainer container) {
        ModContent.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, HourglassConfig.SPEC);
        NeoForge.EVENT_BUS.register(PlayerLifecycle.class);
    }
}
