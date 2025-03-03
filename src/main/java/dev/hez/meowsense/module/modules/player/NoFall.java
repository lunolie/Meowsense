package dev.hez.meowsense.module.modules.player;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.modules.player.nofall.*;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;

public class NoFall extends Module {
    public final NewModeSetting nofallMode = new NewModeSetting("NoFall Mode", "Packet",
            new PacketNoFall("Packet", this),
            new NoGroundNoFall("No Ground", this),
            new WatchdogTimerNoFall("Watchdog Timer", this),
            new VulcanNoFall("Vulcan", this),
            new MospixelNoFall("Mospixel", this));

    public final NumberSetting minFallDistance = new NumberSetting("Min NoFall Distance", 2, 30, 4, 0.1);

    public NoFall() {
        super("NoFall", "Removes fall damage", 0, ModuleCategory.PLAYER);
        this.addSettings(nofallMode, minFallDistance);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        this.setSuffix(nofallMode.getCurrentMode().getName());
    };
}
