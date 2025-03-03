package dev.hez.meowsense.event.impl.player;

import dev.hez.meowsense.event.types.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
@AllArgsConstructor
public class EventPlayerVelocityStrafe implements Event {
    public Vec3d movementInput;
    public float speed;
    public float yaw;
    public Vec3d velocity;
}
