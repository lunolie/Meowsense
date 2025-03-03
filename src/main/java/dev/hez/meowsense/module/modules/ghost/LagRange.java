package dev.hez.meowsense.module.modules.ghost;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.combat.KillAura;
import dev.hez.meowsense.module.modules.player.Blink;
import dev.hez.meowsense.module.setting.impl.RangeSetting;
import dev.hez.meowsense.utils.mc.CombatUtils;
import net.minecraft.entity.player.PlayerEntity;

public class LagRange extends Module {
    public static final RangeSetting range = new RangeSetting("Range", 0, 6, 3.2, 4.5, 0.1);

    public LagRange() {
        super("LagRange", "Teleports you in front of players", 0, ModuleCategory.GHOST);
        this.addSettings(range);
    }

    public boolean blinking = false;

    @Override
    public void onEnable() {
        blinking = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (blinking) {
            Client.INSTANCE.getModuleManager().getModule(Blink.class).setEnabled(false);
            blinking = false;
        }
        super.onDisable();
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) {
            return;
        }

        if (canBlink()) {
            if (!blinking) {
                Client.INSTANCE.getModuleManager().getModule(Blink.class).setEnabled(true);
                blinking = true;
            }
        } else {
            if (blinking) {
                Client.INSTANCE.getModuleManager().getModule(Blink.class).setEnabled(false);
                blinking = false;
            }
        }
    };

    private boolean canBlink() {
        KillAura killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
        if (!(killAura.isEnabled() && killAura.target != null)) {
            return false;
        }
        PlayerEntity killAuraTarget = killAura.target;

        return CombatUtils.getDistanceToEntity(killAuraTarget) >= range.getValueMin() && CombatUtils.getDistanceToEntity(killAuraTarget) <= range.getValueMax() && killAuraTarget.hurtTime == 0;
    }
}
