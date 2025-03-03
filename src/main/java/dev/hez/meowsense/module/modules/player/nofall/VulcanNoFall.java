package dev.hez.meowsense.module.modules.player.nofall;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.event.types.TransferOrder;
import dev.hez.meowsense.mixin.accesors.PlayerMoveC2SPacketAccessor;
import dev.hez.meowsense.module.modules.player.NoFall;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.MoveUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class VulcanNoFall extends SubMode<NoFall> {
    public VulcanNoFall(String name, NoFall parentModule) {
        super(name, parentModule);
    }

    @EventLink
    public final Listener<EventPacket> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        if (PlayerUtil.isOverVoid()) {
            return;
        }
        if (event.getOrder() == TransferOrder.SEND) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
                PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) packet;

                if (mc.player.fallDistance >= getParentModule().minFallDistance.getValue()) {
                    accessor.setOnGround(true);
                    mc.player.fallDistance = 0f;
                    MoveUtils.setMotionY(0);
                }
            }
        }
    };
}
