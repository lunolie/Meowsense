package dev.hez.meowsense.module.modules.movement.fly;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.movement.Fly;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;

public class JumpFly extends SubMode<Fly> {
    private int tickCounter = 0;
    private boolean isEnabled = false;

    public JumpFly(String name, Fly parentModule) {
        super(name, parentModule);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        tickCounter = 0; // Reset tick counter when enabled
        isEnabled = true; // Mark as active
    }

    @Override
    public void onDisable() {
        super.onDisable();
        isEnabled = false; // Stop the jump loop
        tickCounter = 0; // Reset counter when disabled
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (!isEnabled || isNull() || mc.player == null) return;

        // Minecraft runs at 20 ticks per second, so 0.8 seconds = 16 ticks
        if (tickCounter % 11 == 0) {
            mc.player.jump();
        }

        tickCounter++;
    };
}
