package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.modules.movement.fly.AirWalkFly;
import dev.hez.meowsense.module.modules.movement.fly.GrimBoatFly;
import dev.hez.meowsense.module.modules.movement.fly.VanillaFly;
import dev.hez.meowsense.module.modules.movement.fly.VulcanGlideFly;
import dev.hez.meowsense.module.modules.movement.fly.JumpFly;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;
import dev.hez.meowsense.utils.mc.PlayerUtil; // Import PlayerUtil

public class Fly extends Module {
    public final NewModeSetting flyMode = new NewModeSetting("Fly Mode", "Vanilla",
            new VanillaFly("Vanilla", this),
            new AirWalkFly("Air Walk", this),
            new GrimBoatFly("Grim Boat", this),
            new VulcanGlideFly("Vulcan Glide", this),
            new JumpFly("AirJump", this));

    public final NumberSetting speed = new NumberSetting("Speed", 0, 5, 1, 0.001);

    public Fly() {
        super("Fly", "Lets you fly in vanilla", 0, ModuleCategory.MOVEMENT);
        addSettings(flyMode, speed);
        speed.addDependency(flyMode, "Vanilla");
    }

    @Override
    public void onEnable() {
        if (isNull()) {
            toggle();
            return;
        }
        super.onEnable();
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        // Get the air time in ticks from PlayerUtil
        int airTimeTicks = PlayerUtil.inAirTicks();

        // Convert ticks to seconds (20 ticks = 1 second)
        double airTimeSeconds = airTimeTicks / 20.0;

        // Update the module suffix to show the air time in seconds
        this.setSuffix(flyMode.getCurrentMode().getName() + " | Air Time: " + String.format("%.1fs", airTimeSeconds));
    };
}