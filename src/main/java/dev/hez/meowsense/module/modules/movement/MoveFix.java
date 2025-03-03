package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;

public class MoveFix extends Module {
    public static final BooleanSetting silent = new BooleanSetting("Silent", false);

    public MoveFix() {
        super("MoveFix", "Fixes your movement", 0, ModuleCategory.MOVEMENT);
        this.addSettings(silent);
    }
}
