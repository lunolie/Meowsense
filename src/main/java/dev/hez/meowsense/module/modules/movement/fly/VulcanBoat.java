package dev.hez.meowsense.module.modules.movement.fly;

import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.module.modules.movement.Fly;
import dev.hez.meowsense.module.setting.impl.newmodesetting.SubMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.math.Vec3d;

public class VulcanBoat extends SubMode<Fly> {
    private boolean wasInBoat = false; // Track if the player was in a boat
    private long enterBoatTime = 0; // Track when the player entered the boat
    private long exitBoatTime = 0; // Track when the player exited the boat
    private boolean isFlying = false; // Track if the player is flying

    public VulcanBoat(String name, Fly parentModule) {
        super(name, parentModule);
    }

    @Override
    public void onDisable() {
        super.onDisable(); // Call the parent onDisable method to unsubscribe from events

        // Reset flying state when the module is disabled
        if (isFlying) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.getAbilities().flying = false; // Disable flying
                mc.player.getAbilities().setFlySpeed(0.05f); // Reset flying speed to default
            }
            isFlying = false; // Reset the flying flag
        }
    }

    @EventLink
    public final Listener<EventTickPre> eventTickPreListener = event -> {
        if (isNull()) return; // Use the isNull() method from SubMode

        // Check if the player is currently in a boat
        boolean isInBoat = mc.player.getVehicle() instanceof BoatEntity;

        // If the player enters a boat
        if (!wasInBoat && isInBoat) {
            enterBoatTime = System.currentTimeMillis(); // Record the time when the player entered the boat
        }

        // If the player exits a boat
        if (wasInBoat && !isInBoat) {
            exitBoatTime = System.currentTimeMillis(); // Record the time when the player exited the boat
            isFlying = true; // Start flying

            // Teleport the player 3 blocks up to avoid getting stuck in blocks
            Vec3d currentPos = mc.player.getPos();
            mc.player.setPosition(currentPos.x, currentPos.y + 3, currentPos.z);

            mc.player.getAbilities().flying = true; // Enable flying
            mc.player.getAbilities().setFlySpeed(50 / 20f); // Set flying speed to 50 bps (blocks per second)
        }

        // If the player is in a boat and the parent module is enabled
        if (isInBoat && getParentModule().isEnabled()) { // Use the getter method
            // If 0.5 seconds have passed since entering the boat
            if (System.currentTimeMillis() - enterBoatTime >= 500) {
                // Exit the boat server-side
                if (mc.player.getVehicle() != null) {
                    mc.player.networkHandler.sendPacket(
                            PlayerInteractEntityC2SPacket.interact(mc.player.getVehicle(), false, mc.player.getActiveHand())
                    );
                }
                mc.player.stopRiding(); // Exit the boat client-side
            }
        }

        // If the player is flying and 2 seconds have passed since exiting the boat
        if (isFlying && System.currentTimeMillis() - exitBoatTime >= 2000) {
            isFlying = false; // Stop flying
            mc.player.getAbilities().flying = false; // Disable flying
            mc.player.getAbilities().setFlySpeed(0.05f); // Reset flying speed to default
        }

        wasInBoat = isInBoat; // Update the state for the next tick
    };
}