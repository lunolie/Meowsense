package dev.hez.meowsense.module.modules.movement.fly;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.movement.Fly;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.MoveUtils;
import net.minecraft.entity.vehicle.BoatEntity;

public class GrimBoatFly extends SubMode<Fly> {
    public GrimBoatFly(String name, Fly parentModule) {
        super(name, parentModule);
    }

    private boolean grimBoatSend;

    @Override
    public void onEnable() {
        grimBoatSend = false;
        super.onEnable();
    }
    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        if (mc.player.hasVehicle() && mc.player.getVehicle() instanceof BoatEntity boat) {
            mc.player.setYaw(boat.getYaw());
            mc.player.setPitch(90f);
            grimBoatSend = true;
        }

        if (grimBoatSend && mc.player.hasVehicle()) {
            mc.options.sneakKey.setPressed(true);
        }

        if (grimBoatSend && !mc.player.hasVehicle()) {
            MoveUtils.setMotionY(1.8);
            grimBoatSend = false;
            mc.options.sneakKey.setPressed(false);
            getParentModule().toggle();
        }
    };
}
