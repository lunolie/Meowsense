package dev.hez.meowsense.module.modules.movement.longjump;

import dev.hez.meowsense.module.modules.movement.LongJump;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;

public class DoubleJumpLongJump extends SubMode<LongJump> {
    public DoubleJumpLongJump(String name, LongJump parentModule) {
        super(name, parentModule);
    }

    @Override
    public void onEnable() {
        mc.player.setJumping(false);
        mc.player.jump();
        mc.player.jump();
        getParentModule().toggle();
        super.onEnable();
    }
}
