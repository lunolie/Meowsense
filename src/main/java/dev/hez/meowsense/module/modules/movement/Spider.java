package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.modules.movement.spider.VanillaSpider;
import dev.hez.meowsense.module.modules.movement.spider.VerusSpider;
import dev.hez.meowsense.module.modules.movement.spider.VulcanSpider;
import dev.hez.meowsense.module.setting.impl.ModeSetting;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;
import dev.hez.meowsense.utils.mc.MoveUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;

public class Spider extends Module {

    public final NewModeSetting spiderMode = new NewModeSetting("Mode", "Vanilla",
            new VanillaSpider("Vanilla", this),
            new VerusSpider("Verus", this),
            new VulcanSpider("Vulcan", this));

    public final NumberSetting verticalMotion = new NumberSetting("Vertical Motion", 0.1, 1, 0.42, 0.01);

    public Spider() {
        super("Spider", "Allows you to climb walls", 0, ModuleCategory.MOVEMENT);
        this.addSettings(spiderMode, verticalMotion);

        verticalMotion.addDependency(spiderMode, "Vanilla");
    }

    @EventLink
    public final Listener<EventTickPre> eventTickListener = event -> {
        this.setSuffix(spiderMode.getCurrentMode().getName());
    };
}
