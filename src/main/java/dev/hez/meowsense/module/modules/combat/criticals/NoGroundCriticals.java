package dev.hez.meowsense.module.modules.combat.criticals;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.event.types.TransferOrder;
import dev.hez.meowsense.mixin.accesors.PlayerMoveC2SPacketAccessor;
import dev.hez.meowsense.module.modules.combat.Criticals;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoGroundCriticals extends SubMode<Criticals> {
    public NoGroundCriticals(String name, Criticals parentModule) {
        super(name, parentModule);
    }

    @EventLink
    public final Listener<EventPacket> eventPacketListener = event -> {
        if (isNull()) {
            return;
        }
        if (event.getOrder() == TransferOrder.SEND) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
                PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) packet;
                accessor.setOnGround(false);
            }
        }
    };
}
