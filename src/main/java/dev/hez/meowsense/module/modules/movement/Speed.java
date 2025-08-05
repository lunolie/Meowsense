package dev.hez.meowsense.module.modules.movement;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.input.EventMovementInput;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.movement.speed.*;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.module.setting.impl.newmodesetting.NewModeSetting;
import dev.hez.meowsense.utils.mc.*;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public class Speed extends Module {
    public final NewModeSetting speedMode = new NewModeSetting("Speed Mode", "Vanilla",
            new VanillaSpeed("Vanilla", this),
            new StrafeSpeed("Strafe", this),
            new WatchdogSpeed("Watchdog", this),
            new TestSpeed("Test", this),
            new VulcanSpeed("Vulcan", this),
            new NCPSpeed("NCP", this),
            new MospixelSpeed("Mospixel", this),
            new GrimCollideSpeed("Grim Collide", this),
            new PolarCollideSpeed("Polar Collide", this),
            new Balancespeed("Balance Timer", this),
            new CubecraftSpeed("Cubecraft", this),
            new MushMCSpeed("MushMC", this)
    );

    // vanilla
    public final NumberSetting vanillaSpeed = new NumberSetting("Vanilla Speed", 0, 5, 0.2, 0.001);

    // Vulcan
    public final NumberSetting vulcanGroundSpeed = new NumberSetting("Ground Speed", 0, 0.3, 0.2, 0.001);

    // NCP
    public final BooleanSetting ncpHurtBoost = new BooleanSetting("HurtBoost", false);
    public final NumberSetting ncpHurtBoostHurttime = new NumberSetting("HurtTime", 1, 10, 7, 1);
    public final NumberSetting ncpHurtBoostSpeed = new NumberSetting("Hurt Speed", 0.5, 2, 0.75, 0.001);
    public final BooleanSetting ncpLowHop = new BooleanSetting("Lowhop", false);
    public final BooleanSetting ncpGlide = new BooleanSetting("Glide", false);
    public final BooleanSetting ncpTimerBoost = new BooleanSetting("Timer Boost", false);

    // Cubecraft
    // public final BooleanSetting cubecraftHurtBoost = new BooleanSetting("HurtBoost", false);
    // public final NumberSetting cubecraftHurtBoostHurttime = new NumberSetting("HurtTime", 1, 10, 7, 1);
    // public final NumberSetting cubecraftHurtBoostSpeed = new NumberSetting("Hurt Speed", 0.5, 2, 0.75, 0.001);
    // public final BooleanSetting cubecraftLowHop = new BooleanSetting("Lowhop", false);
    // public final BooleanSetting cubecraftGlide = new BooleanSetting("Glide", false);
    // public final BooleanSetting cubecraftTimerBoost = new BooleanSetting("Timer Boost", false);

    // Watchdog
    public final BooleanSetting watchdogLowHop = new BooleanSetting("Watchdog LowHop", false);
    public final BooleanSetting watchdogStrafe = new BooleanSetting("Watchdog Strafe", false);
    public final BooleanSetting watchdogShouldCancelVelocity = new BooleanSetting("Cancel Velocity", false);
    public final BooleanSetting watchdogNeedDisabler = new BooleanSetting("Need Disabler", true);


    public Speed() {
        super("Speed", "Makes you speedy", 0, ModuleCategory.MOVEMENT);
        this.addSettings(speedMode, vanillaSpeed, watchdogLowHop, watchdogStrafe, watchdogShouldCancelVelocity, watchdogNeedDisabler, ncpHurtBoost, ncpHurtBoostHurttime, ncpHurtBoostSpeed, ncpLowHop, ncpGlide, ncpTimerBoost, vulcanGroundSpeed); // cubecraftHurtBoost, cubecraftHurtBoostHurttime, cubecraftHurtBoostSpeed, cubecraftLowHop, cubecraftGlide, cubecraftTimerBoost

        vanillaSpeed.addDependency(speedMode, "Vanilla");
        vulcanGroundSpeed.addDependency(speedMode, "Vulcan");

        watchdogLowHop.addDependency(speedMode, "Watchdog");
        watchdogStrafe.addDependency(speedMode, "Watchdog");
        watchdogShouldCancelVelocity.addDependency(speedMode, "Watchdog");
        watchdogShouldCancelVelocity.addDependency(watchdogLowHop, true);
        watchdogNeedDisabler.addDependency(speedMode, "Watchdog");
        watchdogNeedDisabler.addDependency(watchdogLowHop, true);

        ncpHurtBoost.addDependency(speedMode, "NCP");
        ncpHurtBoostHurttime.addDependency(speedMode, "NCP");
        ncpHurtBoostHurttime.addDependency(ncpHurtBoost, true);
        ncpHurtBoostSpeed.addDependency(speedMode, "NCP");
        ncpHurtBoostSpeed.addDependency(ncpHurtBoost, true);
        ncpLowHop.addDependency(speedMode, "NCP");
        ncpGlide.addDependency(speedMode, "NCP");
        ncpTimerBoost.addDependency(speedMode, "NCP");
    }
        // cubecraftHurtBoost.addDependency(speedMode, "Cubecraft");
        // cubecraftHurtBoostHurttime.addDependency(speedMode, "Cubecraft");
        // cubecraftHurtBoostHurttime.addDependency(cubecraftHurtBoost, true);
        // cubecraftHurtBoostSpeed.addDependency(speedMode, "Cubecraft");
        // cubecraftHurtBoostSpeed.addDependency(cubecraftHurtBoost, true);
        // cubecraftLowHop.addDependency(speedMode, "Cubecraft");
        // cubecraftGlide.addDependency(speedMode, "Cubecraft");
        // cubecraftTimerBoost.addDependency(speedMode, "Cubecraft");

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        this.setSuffix(speedMode.getCurrentMode().getName());
    };

    @EventLink
    public final Listener<EventMovementInput> eventMovementInputListener = event -> {
        if (isNull()) {
            return;
        }
        if (speedMode.isMode("Balance Timer") || speedMode.isMode("Polar Collide") || speedMode.isMode("Grim Collide") || speedMode.isMode("Test")) {
            return;
        }
        if (MoveUtils.isMoving2() && mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
            event.setJumping(true);
        }
    };

    @Override
    public void onDisable() {
        if (speedMode.isMode("Balance Timer")) {
            PlayerUtil.setTimer(1.0f);

            Iterator<Packet<?>> iterator = Balancespeed.transPackets.iterator();
            while (iterator.hasNext()) {
                Packet<?> packet = iterator.next();
                PacketUtils.sendPacketSilently(packet);
                iterator.remove();
            }

            Balancespeed.transPackets.clear();
        }
        super.onDisable();
    }


    public boolean isFullBlockBelow() {
        BlockPos blockUnder = mc.player.getBlockPos().down();
        BlockState blockState = mc.world.getBlockState(blockUnder);
        if (blockState.isAir()) {
            return true;
        }
        return blockState.isFullCube(mc.world, blockUnder);
    }

    public boolean canCauseSpeed(Entity entity) {
        return entity != mc.player && entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity);
    }
}