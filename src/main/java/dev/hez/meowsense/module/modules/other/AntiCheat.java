package dev.hez.meowsense.module.modules.other;

import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;

public class AntiCheat extends Module {
    public static final BooleanSetting checkSelf = new BooleanSetting("Check self", true);

    public AntiCheat() {
        super("AntiCheat", "Detects if other players are cheating", 0, ModuleCategory.OTHER);
        this.addSettings(checkSelf);
    }
}