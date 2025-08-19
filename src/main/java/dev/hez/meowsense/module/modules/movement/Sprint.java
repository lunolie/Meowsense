package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.mixin.accesors.GameOptionsAccessor;
import dev.hez.meowsense.mixin.accesors.KeyBindingAccessor;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.player.Scaffold;
import dev.hez.meowsense.module.setting.impl.ModeSetting;
import net.minecraft.client.option.KeyBinding;

public class Sprint extends Module {
    public static ModeSetting mode = new ModeSetting("Mode", "Legit", "Legit", "Omni");

    public Sprint() {
        super("Sprint", "Sprints for you", 0, ModuleCategory.MOVEMENT);
        this.addSetting(mode);
    }

    public static boolean shouldSprintDiagonally() {
        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled())
            return false;
        return mode.getMode().equals("Omni");
    }

    private boolean shouldSprint() {
        // Don't sprint if scaffold is enabled
        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) {
            return false;
        }

        // Check if player has enough food/hunger to sprint
        if (mc.player.getHungerManager().getFoodLevel() <= 6) {
            return false;
        }

        // Check movement input based on mode
        switch (mode.getMode()) {
            case "Legit":
                // Only sprint when moving forward (not diagonally or backwards)
                return mc.player.input.movementForward > 0.8f && Math.abs(mc.player.input.movementSideways) < 0.1f;
            case "Omni":
                // Sprint in any direction (omnidirectional) - same as old rage behavior
                return Math.abs(mc.player.input.movementForward) > 0.1f || Math.abs(mc.player.input.movementSideways) > 0.1f;
            default:
                return false;
        }
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }

        // Update suffix based on current mode
        this.setSuffix(mode.getMode());

        // Disable sprint toggle to prevent conflicts
        ((GameOptionsAccessor) mc.options).getSprintToggled().setValue(false);

        // Apply sprint based on conditions
        if (shouldSprint()) {
            KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), true);
        } else {
            KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), false);
        }
    };
}
