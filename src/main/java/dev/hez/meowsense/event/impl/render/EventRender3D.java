package dev.hez.meowsense.event.impl.render;

import dev.hez.meowsense.event.types.Event;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4fStack;

@Getter
public class EventRender3D implements Event {
    MatrixStack matrixStack;

    public EventRender3D(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}
