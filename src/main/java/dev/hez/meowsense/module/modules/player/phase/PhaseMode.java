package dev.hez.meowsense.module.modules.player.phase;

import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.modules.player.Phase;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.PacketUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public abstract class PhaseMode extends SubMode<Phase> {
    public PhaseMode(String name, Phase module) {
        super(name, module);
    }

    public abstract void onUpdate();
}
