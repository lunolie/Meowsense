package dev.hez.meowsense.module.modules.movement.speed;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.event.types.TransferOrder;
import dev.hez.meowsense.mixin.accesors.PlayerMoveC2SPacketAccessor;
import dev.hez.meowsense.module.modules.movement.Speed;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.MoveUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class MushMCSpeed extends SubMode<Speed> {
    public MushMCSpeed(String name, Speed parentModule) { super(name, parentModule); }

    @EventLink
    public final Listener<EventPacket> eventPacketListener = event -> {
        if (isNull()) {
            return;
        }

        // VulcanSpeed packet modification for low-hop
        if (event.getOrder() == TransferOrder.SEND) {
            if (PlayerUtil.getDistanceToGround() < 1.5) {
                if (event.getPacket() instanceof PlayerMoveC2SPacket && mc.player.getVelocity().y < 0) {
                    ((PlayerMoveC2SPacketAccessor) event.getPacket()).setOnGround(true);
                }
            }
        }
    };

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        if (!MoveUtils.isMoving2()) {
            return;
        }

        if (mc.player.isOnGround()) {
            MoveUtils.strafe(getParentModule().vulcanGroundSpeed.getValue() * 0.75); // Use Vulcan ground speed setting
        } else {
            // Vulcan low-hop feature
            MoveUtils.strafe(); // Maintain horizontal speed in the air
            if (PlayerUtil.inAirTicks() == 1) {
                MoveUtils.setMotionY(-0.08);
            }
        }
    };
}