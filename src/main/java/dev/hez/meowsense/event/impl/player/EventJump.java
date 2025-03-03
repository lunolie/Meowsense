package dev.hez.meowsense.event.impl.player;

import dev.hez.meowsense.event.types.Event;
import lombok.Getter;
import lombok.Setter;

public class EventJump implements Event {
    @Getter
    @Setter
    float yaw;

    public EventJump(float yaw) {
        this.yaw = yaw;
    }
}
