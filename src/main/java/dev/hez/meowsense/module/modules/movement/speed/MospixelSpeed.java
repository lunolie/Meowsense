package dev.hez.meowsense.module.modules.movement.speed;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.movement.Speed;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.MoveUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;

public class MospixelSpeed extends SubMode<Speed> {
    public MospixelSpeed(String name, Speed parentModule) {
        super(name, parentModule);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        if (!MoveUtils.isMoving2()) {
            return;
        }
        switch (PlayerUtil.inAirTicks()) {
            case 1:
                if (mc.player.hurtTime > 0) MoveUtils.setMotionY(-0.1);
                break;
            case 4:
                MoveUtils.setMotionY(-0.09800000190734864);
                mc.player.getVelocity().x *= Math.sqrt(2);
                mc.player.getVelocity().z *= Math.sqrt(2);
                break;
        }

        MoveUtils.strafe(Math.max(MoveUtils.getSpeed(), MoveUtils.getAllowedHorizontalDistance() - 0.001));
    };
}
