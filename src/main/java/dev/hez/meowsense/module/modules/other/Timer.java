package dev.hez.meowsense.module.modules.other;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.utils.mc.PlayerUtil;

public class Timer extends Module {
    public static final NumberSetting dhauohfeidbf = new NumberSetting("Timer", 0.1, 10, 1, 0.001);

    public Timer() {
        super("Timer", "Modify game speed", 0, ModuleCategory.OTHER);
        this.addSetting(dhauohfeidbf);
    }

    @Override
    public void onEnable() {
        PlayerUtil.setTimer(dhauohfeidbf.getValueFloat());
        super.onEnable();
    }

    @EventLink
    public final Listener<EventTickPre> eventPacketListener = event -> {
        if (isNull()) {
            return;
        }
        PlayerUtil.setTimer(dhauohfeidbf.getValueFloat());
    };

    @Override
    public void onDisable() {
        PlayerUtil.setTimer(1.0f);
        super.onDisable();
    }
}
