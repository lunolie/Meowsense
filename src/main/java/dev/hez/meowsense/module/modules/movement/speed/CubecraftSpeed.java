package dev.hez.meowsense.module.modules.movement.speed;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.movement.Speed;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import dev.hez.meowsense.utils.mc.CombatUtils;
import dev.hez.meowsense.utils.mc.MoveUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;

public class CubecraftSpeed extends SubMode<Speed> {
    public CubecraftSpeed(String name, Speed parentModule) {
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
        MoveUtils.strafe(Math.max(MoveUtils.getSpeed(), MoveUtils.getAllowedHorizontalDistance()));
    };
}
       // if (mc.player.hurtTime > getParentModule().cubecraftHurtBoostHurttime.getValueInt() && getParentModule().ncpHurtBoost.getValue() && CombatUtils.isInCombat()) {
           // MoveUtils.strafe(getParentModule().cubecraftHurtBoostSpeed.getValue());
      //  } else {
         //   MoveUtils.strafe(Math.max(MoveUtils.getSpeed(), MoveUtils.getAllowedHorizontalDistance()));
    // }

      //  if (getParentModule().cubecraftLowHop.getValue()) {
           // if (PlayerUtil.inAirTicks() == 4) {
                // MoveUtils.setMotionY(-0.09800000190734864);
           // }
       // }

      //  if (getParentModule().cubecraftTimerBoost.getValue()) {
         //   switch (PlayerUtil.inAirTicks()) {
             //   case 0 -> PlayerUtil.setTimer(0.8f);
             //   case 1 -> PlayerUtil.setTimer(1.5f);
              //  case 3 -> PlayerUtil.setTimer(1.008f);
          //  }
      //  }
    //    if (getParentModule().cubecraftGlide.getValue()) {
           // if (PlayerUtil.getDistanceToGround() != 0 && PlayerUtil.getDistanceToGround() <= 1 && mc.player.fallDistance <= 1) {
              //  if (PlayerUtil.inAirTicks() >= 10) {
                //    MoveUtils.setMotionY(MoveUtils.getMotionY() / 27);
               // }
            // }

