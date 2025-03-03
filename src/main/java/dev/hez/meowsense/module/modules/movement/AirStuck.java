package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.input.EventMovementInput;
import dev.hez.meowsense.event.impl.network.EventPacket;
import dev.hez.meowsense.event.impl.player.EventMotionPost;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.utils.mc.MoveUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class AirStuck extends Module {
    public AirStuck() {
        super("AirStuck", "Stuck", 0, ModuleCategory.MOVEMENT);
    }

    private Vec3d oldMotion;

    @Override
    public void onEnable() {
        oldMotion = mc.player.getVelocity();
    }

    @Override
    public void onDisable() {
        mc.player.setVelocity(oldMotion);
    }

    @EventLink
    public final Listener<EventMovementInput> eventMovementInputListener = event -> {
        if (isNull()) {
            return;
        }
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
    };

    @EventLink
    public final Listener<EventMotionPost> eventMotionPostListener = event -> {
        if (isNull()) {
            return;
        }
        MoveUtils.stop();
        MoveUtils.setMotionY(0);
    };

    @EventLink
    public final Listener<EventPacket> eventPacketListener = event -> {
        Packet<?> packet = event.getPacket();

        if (packet instanceof PlayerMoveC2SPacket) {
            event.cancel();
        }
    };
}
