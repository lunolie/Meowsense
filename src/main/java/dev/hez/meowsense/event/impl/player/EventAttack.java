package dev.hez.meowsense.event.impl.player;

import dev.hez.meowsense.event.types.Event;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
public class EventAttack implements Event {
    Entity target;
    public EventAttack(Entity target){
        this.target = target;
    }
}
