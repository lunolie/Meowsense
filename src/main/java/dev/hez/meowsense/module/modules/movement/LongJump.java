package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.movement.longjump.DoubleJumpLongJump;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;

public class LongJump extends Module {
    public final NewModeSetting longJumpMode = new NewModeSetting("Long Jump Mode", "Double Jump",
            new DoubleJumpLongJump("Double Jump", this));

    public LongJump() {
        super("LongJump", "Jumps big long distance", 0, ModuleCategory.MOVEMENT);
        this.addSetting(longJumpMode);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        this.setSuffix(longJumpMode.getCurrentMode().getName());
    };
}
