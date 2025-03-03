package dev.hez.meowsense.module.modules.movement.fly;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.movement.Fly;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.MoveUtils;

public class VanillaFly extends SubMode<Fly> {
    public VanillaFly(String name, Fly parentModule) {
        super(name, parentModule);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        mc.player.getVelocity().y = 0.0D + (mc.options.jumpKey.isPressed() ? 0.42f : 0.0D) - (mc.options.sneakKey.isPressed() ? 0.42f : 0.0D);
        if (MoveUtils.isMoving2()) {
            MoveUtils.strafe(getParentModule().speed.getValue());
        }
    };
}
