package dev.hez.meowsense.module.modules.combat;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventAttack;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.modules.combat.criticals.*;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

public class Criticals extends Module {
    public final NewModeSetting critMode = new NewModeSetting("Crit Mode", "Vanilla",
            new VanillaCriticals("Vanilla", this),
            new NoGroundCriticals("NoGround", this),
            new WatchdogCriticals("Watchdog", this),
            new VulcanCriticals("Vulcan", this),
            new MospixelCriticals("Mospixel", this));

    public Criticals() {
        super("Criticals", "Always deal criticals", 0, ModuleCategory.COMBAT);
        this.addSetting(critMode);
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        this.setSuffix(critMode.getCurrentMode().getName());
    };

    @EventLink
    public final Listener<EventAttack> eventAttackListener = event -> {
        if (isNull()) {
            return;
        }
        boolean willCritLegit = mc.player.fallDistance > 0.0F && !mc.player.isOnGround() && !mc.player.isClimbing() && !mc.player.isTouchingWater() && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && !mc.player.hasVehicle() && event.getTarget() instanceof LivingEntity;

        if (!willCritLegit) {
            mc.player.addCritParticles(event.getTarget());
        }
    };
}
