package dev.hez.meowsense.utils.mc;

import dev.hez.meowsense.utils.Utils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PacketUtils implements Utils {

    public static void sendPacket(Packet<?> packet) {
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        mc.interactionManager.sendSequencedPacket(mc.world, packetCreator);
    }

    public static void sendPacketSilently(Packet<?> packet) {
        mc.getNetworkHandler().getConnection().send(packet, null);
    }

    public static void sendSequencedPacketSilently(SequencedPacketCreator packetCreator) {
        ClientWorld world = mc.world;
        PendingUpdateManager pendingUpdateManager = world.getPendingUpdateManager().incrementSequence();

        try {
            int sequence = pendingUpdateManager.getSequence();
            Packet<ServerPlayPacketListener> packet = packetCreator.predict(sequence);
            sendPacketSilently(packet);
        } catch (Throwable t) {
            if (pendingUpdateManager != null) {
                try {
                    pendingUpdateManager.close();
                } catch (Throwable suppressed) {
                    t.addSuppressed(suppressed);
                }
            }
            throw t;
        }

        if (pendingUpdateManager != null) {
            pendingUpdateManager.close();
        }
    }

    public static void releaseUseItem(boolean callEvent) {
        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                BlockPos.ORIGIN,
                Direction.DOWN
        );

        if (callEvent) {
            sendPacket(packet);
        } else {
            sendPacketSilently(packet);
        }
    }

    public static void handlePacket(Packet<?> packet) {
        try {
            ((Packet<ClientPlayNetworkHandler>) packet).apply(mc.getNetworkHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
