package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.movement.clicktp.MospixelClickTp;
import dev.hez.meowsense.module.modules.movement.clicktp.VanillaClickTp;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;

public class ClickTP extends Module {
    public final NewModeSetting clickTPMode = new NewModeSetting("ClickTP Mode", "Vanilla",
            new VanillaClickTp("Vanilla", this),
            new MospixelClickTp("Mospixel", this));

    public ClickTP() {
        super("ClickTP", "Middle Click To Teleport", 0, ModuleCategory.MOVEMENT);
        this.addSettings(clickTPMode);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        this.setSuffix(clickTPMode.getCurrentMode().getName());
    };
}
