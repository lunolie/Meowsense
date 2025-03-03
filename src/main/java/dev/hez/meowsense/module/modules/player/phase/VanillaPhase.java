package dev.hez.meowsense.module.modules.player.phase;

import dev.hez.meowsense.module.modules.player.Phase;
import dev.hez.meowsense.utils.mc.PacketUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class VanillaPhase extends PhaseMode {
    private static final double BLINK_DISTANCE = 5.0; // Distance to teleport forward

    public VanillaPhase(String name, Phase module) {
        super(name, module);
    }

    @Override
    public void onEnable() {
        System.out.println("BlinkPhase enabled!");
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        // Get player’s current position and facing direction
        double playerYaw = Math.toRadians(mc.player.getYaw()); // Get the player's facing direction (yaw)
        double offsetX = -MathHelper.sin((float) playerYaw) * BLINK_DISTANCE; // X offset based on yaw
        double offsetZ = MathHelper.cos((float) playerYaw) * BLINK_DISTANCE; // Z offset based on yaw

        // Calculate new position (same Y value, but offset X and Z based on facing direction)
        double newX = mc.player.getX() + offsetX;
        double newY = mc.player.getY(); // Keep Y position the same
        double newZ = mc.player.getZ() + offsetZ;

        // Check if the player's movement would collide with a block
        BlockPos currentPos = new BlockPos((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ());
        BlockPos targetPos = new BlockPos((int) newX, (int) newY, (int) newZ);

        // If the new position is colliding with a block, teleport
        if (mc.world.getBlockState(targetPos).isSolid()) {
            // Send the position update packet to the server to teleport
            PacketUtils.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(newX, newY, newZ, false));

            // Update the player’s position on the client side
            mc.player.setPosition(newX, newY, newZ);

            System.out.println("Player blinked through a block to: " + newX + ", " + newY + ", " + newZ);
        } else {
            System.out.println("No block detected, blink not triggered.");
        }
    }

    @Override
    public void onDisable() {
        // Reset anything on disable if needed
        System.out.println("BlinkPhase disabled.");
    }
}
