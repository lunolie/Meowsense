package dev.hez.meowsense.module.modules.player.phase;

import dev.hez.meowsense.module.modules.player.Phase;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VanillaPhase extends PhaseMode {

    private boolean isClipping = false;
    private int phaseTicks = 0;

    public VanillaPhase(String name, Phase module) {
        super(name, module);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.noClip = true;
        }
    }

    @EventLink
    public final Listener<EventTickPre> onTick = event -> {
        onUpdate();
    };

    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        mc.player.noClip = true;

        if (mc.player.horizontalCollision) {
            isClipping = true;
            phaseTicks = 0;
        }

        if (isClipping) {
            phaseTicks++;

            if (phaseTicks <= 3) {
                float yawRadians = (float) Math.toRadians(mc.player.getYaw());
                double motionX = -MathHelper.sin(yawRadians);
                double motionZ = MathHelper.cos(yawRadians);

                double length = Math.sqrt(motionX * motionX + motionZ * motionZ);
                motionX /= length;
                motionZ /= length;

                double offset = (phaseTicks == 1) ? 0.06 : 1.7;
                double newX = mc.player.getX() + motionX * offset;
                double newZ = mc.player.getZ() + motionZ * offset;

                mc.player.setPosition(newX, mc.player.getY(), newZ);
            } else {
                isClipping = false;
                phaseTicks = 0;
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.noClip = false;
        }
    }
}