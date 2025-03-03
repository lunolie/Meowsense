package dev.hez.meowsense.event.impl.network;

import dev.hez.meowsense.event.types.CancellableEvent;
import dev.hez.meowsense.event.types.TransferOrder;
import net.minecraft.network.packet.Packet;


public class EventPacket extends CancellableEvent {
    private Packet packet;
    private TransferOrder order;

    public EventPacket(Packet packet, TransferOrder order) {
        this.packet = packet;
        this.order = order;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
    public Packet getPacket(){
        return this.packet;
    }
    public TransferOrder getOrder(){
        return this.order;
    }
}