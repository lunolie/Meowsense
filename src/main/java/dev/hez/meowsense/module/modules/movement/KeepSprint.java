package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;

public class KeepSprint extends Module {
    public static final BooleanSetting sprint = new BooleanSetting("Sprint",true);

    public KeepSprint() {
        super("KeepSprint", "Removes attack slowdown", 0, ModuleCategory.MOVEMENT);
        this.addSetting(sprint);
    }
}
