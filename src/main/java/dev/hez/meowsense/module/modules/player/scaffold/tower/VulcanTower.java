package dev.hez.meowsense.module.modules.player.scaffold.tower;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.player.Scaffold;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.MoveUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;

public class VulcanTower extends SubMode<Scaffold> {
    public VulcanTower(String name, Scaffold parentModule) {
        super(name, parentModule);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        if (getParentModule().canTower()) {
            if (mc.player.isOnGround()) {
                MoveUtils.setMotionY(0.42F);
            }
            switch (PlayerUtil.inAirTicks() % 3) {
                case 0:
                    MoveUtils.setMotionY(0.41985 + (Math.random() * 0.000095));
                    break;
                case 2:
                    MoveUtils.setMotionY(Math.ceil(mc.player.getY()) - mc.player.getY());
                    break;
            }
        }
    };
}
