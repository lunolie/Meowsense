package dev.hez.meowsense.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class CustomTitleScreen extends TitleScreen {
    private static final Identifier CUSTOM_LOGO = Identifier.of("meowsense", "cha/watermarklogo/logo.png");

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render custom background FIRST
        renderCustomBackground(context, delta);

        // Render custom logo
        renderCustomLogo(context);

        // Render buttons and other UI elements
        for (var drawable : this.children()) {
            if (drawable instanceof net.minecraft.client.gui.Drawable) {
                ((net.minecraft.client.gui.Drawable) drawable).render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public void renderPanoramaBackground(DrawContext context, float delta) {
        // Override to prevent default panorama
    }

    private void renderCustomBackground(DrawContext context, float delta) {
        // Fill the entire screen with black
        context.fill(0, 0, this.width, this.height, 0xFF000000);

        // Animated unique shader background with wave patterns
        float time = (System.currentTimeMillis() % 100000) / 1000.0f;

        MatrixStack matrices = context.getMatrices();
        matrices.push();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Create a more unique wave-like pattern across the screen
        int segments = 20;
        float segmentWidth = (float) this.width / segments;
        float segmentHeight = (float) this.height / segments;

        for (int x = 0; x < segments; x++) {
            for (int y = 0; y < segments; y++) {
                float xPos = x * segmentWidth;
                float yPos = y * segmentHeight;

                // Create wave interference pattern
                float wave1 = (float) Math.sin((x * 0.3f + time * 1.2f)) * 0.5f + 0.5f;
                float wave2 = (float) Math.cos((y * 0.4f + time * 0.8f)) * 0.5f + 0.5f;
                float wave3 = (float) Math.sin((x + y) * 0.2f + time * 1.5f) * 0.5f + 0.5f;

                // Combine waves for unique color mixing
                float intensity = (wave1 + wave2 + wave3) / 3.0f;

                // Purple/magenta theme with wave modulation
                float r = 0.3f + intensity * 0.4f + 0.1f * (float) Math.sin(time * 0.7f + x * 0.1f);
                float g = 0.1f + intensity * 0.2f + 0.05f * (float) Math.cos(time * 0.5f + y * 0.1f);
                float b = 0.6f + intensity * 0.3f + 0.2f * (float) Math.sin(time * 0.9f + (x + y) * 0.05f);
                float alpha = 0.4f + intensity * 0.3f;

                // Draw segment
                buffer.vertex(matrix, xPos, yPos, 0).color(r, g, b, alpha);
                buffer.vertex(matrix, xPos + segmentWidth, yPos, 0).color(r * 1.1f, g * 1.1f, b * 1.1f, alpha);
                buffer.vertex(matrix, xPos + segmentWidth, yPos + segmentHeight, 0).color(r * 0.8f, g * 0.8f, b * 0.8f, alpha);
                buffer.vertex(matrix, xPos, yPos + segmentHeight, 0).color(r * 0.9f, g * 0.9f, b * 0.9f, alpha);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
        matrices.pop();

        // Add animated stars
        renderAnimatedStars(context, time);
    }

    private void renderAnimatedStars(DrawContext context, float time) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Render white twinkling stars
        for (int i = 0; i < 50; i++) {
            // Use better distribution for star positions
            float baseX = (i * 127.3f) % this.width;
            float baseY = (i * 83.7f) % this.height;

            // Add gentle movement
            float x = baseX + 10.0f * (float) Math.sin(time * 0.3f + i * 0.2f);
            float y = baseY + 8.0f * (float) Math.cos(time * 0.4f + i * 0.15f);

            // Ensure stars stay on screen
            x = Math.max(2, Math.min(this.width - 2, x));
            y = Math.max(2, Math.min(this.height - 2, y));

            // Twinkling effect
            float twinkle = 0.4f + 0.6f * (float) Math.abs(Math.sin(time * 2.0f + i * 0.7f));
            float alpha = twinkle;

            // Variable star sizes
            float size = 0.5f + 1.5f * (float) Math.abs(Math.cos(time * 1.2f + i * 0.5f));

            // Pure white color for stars
            float r = 1.0f;
            float g = 1.0f;
            float b = 1.0f;

            // Create star shape (cross pattern for better visibility)
            // Horizontal line
            buffer.vertex(matrix, x - size * 2, y - size * 0.3f, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, x + size * 2, y - size * 0.3f, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, x + size * 2, y + size * 0.3f, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, x - size * 2, y + size * 0.3f, 0).color(r, g, b, alpha);

            // Vertical line
            buffer.vertex(matrix, x - size * 0.3f, y - size * 2, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, x + size * 0.3f, y - size * 2, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, x + size * 0.3f, y + size * 2, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, x - size * 0.3f, y + size * 2, 0).color(r, g, b, alpha);

            // Center bright spot
            buffer.vertex(matrix, x - size, y - size, 0).color(r, g, b, alpha * 1.5f);
            buffer.vertex(matrix, x + size, y - size, 0).color(r, g, b, alpha * 1.5f);
            buffer.vertex(matrix, x + size, y + size, 0).color(r, g, b, alpha * 1.5f);
            buffer.vertex(matrix, x - size, y + size, 0).color(r, g, b, alpha * 1.5f);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private void renderCustomLogo(DrawContext context) {
        // Main title
        String mainTitle = "Meowsense";

        // Calculate positions
        int mainTitleWidth = this.textRenderer.getWidth(mainTitle) * 3; // Scale by 3
        int mainX = (this.width - mainTitleWidth) / 2;

        int mainY = 40;

        MatrixStack matrices = context.getMatrices();

        // Draw main title (scaled up)
        matrices.push();
        matrices.scale(3.0f, 3.0f, 1.0f);
        context.drawTextWithShadow(this.textRenderer, mainTitle, mainX / 3, mainY / 3, 0xFF00FF); // Bright magenta
        matrices.pop();

        // Draw version info
        String version = "v1.3";
        int versionX = this.width - this.textRenderer.getWidth(version) - 10;
        context.drawTextWithShadow(this.textRenderer, version, versionX, 10, 0x888888);
    }

    @Override
    protected void init() {
        super.init();
    }
}