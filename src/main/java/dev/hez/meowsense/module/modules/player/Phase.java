package dev.hez.meowsense.module.modules.player;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.modules.player.phase.*;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;
import dev.hez.meowsense.module.modules.player.phase.PhaseMode;

public class Phase extends Module {
    public final NewModeSetting phaseMode = new NewModeSetting("Phase Mode", "Vanilla", new VanillaPhase("Vanilla", this));

    public Phase() {
        super("Phase", "Allows you to phase through blocks! Currently not working in any anticheat lmao", 0, ModuleCategory.PLAYER);
        this.addSettings(phaseMode);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        this.setSuffix(phaseMode.getCurrentMode().getName());

        PhaseMode currentPhaseMode = (PhaseMode) phaseMode.getCurrentMode();

        currentPhaseMode.onUpdate();
    };
}


// this module is WIP because im the god skidder trust
