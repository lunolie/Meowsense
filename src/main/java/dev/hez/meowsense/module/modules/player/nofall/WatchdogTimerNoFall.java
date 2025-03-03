package dev.hez.meowsense.module.modules.player.nofall;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.player.NoFall;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.PacketUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class WatchdogTimerNoFall extends SubMode<NoFall> {
    public WatchdogTimerNoFall(String name, NoFall parentModule) {
        super(name, parentModule);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        if (PlayerUtil.isOverVoid()) {
            return;
        }
        if (mc.player.fallDistance >= getParentModule().minFallDistance.getValue()) {
            PlayerUtil.setTimer(0.5f);

            PacketUtils.sendPacketSilently(new PlayerMoveC2SPacket.OnGroundOnly(true));

            mc.player.fallDistance = 0f;

            Client.INSTANCE.getDelayUtil().queue(ev -> PlayerUtil.setTimer(1F), 1);
        }
    };
}
