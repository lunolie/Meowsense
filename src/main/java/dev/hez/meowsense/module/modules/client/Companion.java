package dev.hez.meowsense.module.modules.client;

import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.render.EventRender2D;
import dev.hez.meowsense.utils.render.DrawUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Companion extends Module {
    private final List<Identifier> gifFrames = new ArrayList<>();
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DELAY = 100;

    public Companion() {
        super("Companion", "A cute companion if you feel lonely!", 0, ModuleCategory.CLIENT);

        for (int i = 1; i <= 39; i++) { // 39 frames
            gifFrames.add(Identifier.of("meowsense", "cha/frames/frame" + i + ".png"));
        }
    }

    @EventLink
    public final Listener<EventRender2D> eventRender2DListener = event -> {
        MatrixStack matrices = event.getContext().getMatrices();
        int width = event.getWidth();
        int height = event.getHeight();

        int imageWidth = 100;
        int imageHeight = 100;

        int x = 0;
        int y = 50;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= FRAME_DELAY) {
            currentFrame = (currentFrame + 1) % gifFrames.size();
            lastFrameTime = currentTime;
        }


        Identifier currentFrameTexture = gifFrames.get(currentFrame);


        MinecraftClient.getInstance().getTextureManager().bindTexture(currentFrameTexture);


        DrawUtils.drawImage(matrices, currentFrameTexture, x, y, imageWidth, imageHeight);
    };
}