package dev.hez.meowsense.module.modules.player;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.player.regen.FullRegen;
import dev.hez.meowsense.module.modules.player.regen.VanillaRegen;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;

public class Regen extends Module {
    public final NewModeSetting regenMode = new NewModeSetting("Regen Mode", "Vanilla",
            new VanillaRegen("Vanilla", this),
            new FullRegen("Full", this));

    public final NumberSetting health = new NumberSetting("Health", 1, 20, 10, 1);
    public final NumberSetting packets = new NumberSetting("Packets", 1, 100, 30, 1);

    public Regen() {
        super("Regen", "Regen your health", 0, ModuleCategory.PLAYER);
        this.addSettings(regenMode, health, packets);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
      this.setSuffix(regenMode.getCurrentMode().getName());
    };
}
