package dev.hez.meowsense.event.impl.world;

import dev.hez.meowsense.event.types.Event;
import lombok.Getter;
import net.minecraft.client.world.ClientWorld;

@Getter
public class EventWorldChange implements Event {
    ClientWorld world;
    public EventWorldChange(ClientWorld world){
        this.world = world;
    }
}
