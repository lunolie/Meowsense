package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.mixin.accesors.GameOptionsAccessor;
import dev.hez.meowsense.mixin.accesors.KeyBindingAccessor;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.combat.Criticals;
import dev.hez.meowsense.module.modules.player.Scaffold;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;
import net.minecraft.client.option.KeyBinding;

public class Sprint extends Module {
    public static BooleanSetting rage = new BooleanSetting("Rage", false);

    public Sprint() {
        super("Sprint", "Sprints for you", 0, ModuleCategory.MOVEMENT);
        this.addSetting(rage);
    }

    public static boolean shouldSprintDiagonally() {
        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled())
            return false;
        return rage.getValue();
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }
        this.setSuffix(rage.getValue() ? "Rage" : "Legit");
        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled())
            return;

        ((GameOptionsAccessor) mc.options).getSprintToggled().setValue(false);
        KeyBinding.setKeyPressed(((KeyBindingAccessor) mc.options.sprintKey).getBoundKey(), true);
    };
}
