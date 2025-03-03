package dev.hez.meowsense.module.modules.player.antivoid;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.movement.Fly;
import dev.hez.meowsense.module.modules.player.AntiVoid;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.PlayerUtil;
import net.minecraft.util.math.Vec3d;

public class VanillaAntiVoid extends SubMode<AntiVoid> {
    public VanillaAntiVoid(String name, AntiVoid parentModule) {
        super(name, parentModule);
    }

    private Vec3d lastSafePos;

    @EventLink
    public final Listener<EventTickPre> eventTickListener = event -> {
        if (isNull()) {
            return;
        }

        if (Client.INSTANCE.getModuleManager().getModule(Fly.class).isEnabled()) {
            return;
        }

        if (PlayerUtil.isOverVoid() && mc.player.fallDistance >= getParentModule().minFallDistance.getValueFloat()) {
            mc.player.setPosition(lastSafePos);
        } else if (mc.player.isOnGround()) {
            lastSafePos = new Vec3d(mc.player.getBlockPos().toCenterPos().x, mc.player.getY(), mc.player.getBlockPos().toCenterPos().z);
        }
    };
}
