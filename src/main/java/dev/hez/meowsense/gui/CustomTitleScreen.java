package dev.hez.meowsense.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;

public class CustomTitleScreen extends TitleScreen {
    private static final Identifier CUSTOM_LOGO = Identifier.of("meowsense", "bgassets/logo.png");
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of("meowsense", "bgassets/background.png");
    
    private float time = 0.0f;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        time += delta * 0.05f;

        // Render custom PNG background
        renderCustomBackground(context);

        // Render watermark and title
        renderWatermarkAndTitle(context);

        // Render only the buttons and widgets, not the default logo/background
        // Use super.renderBackground to avoid rendering default elements, then render widgets
        for (var element : this.children()) {
            if (element instanceof net.minecraft.client.gui.Drawable drawable) {
                drawable.render(context, mouseX, mouseY, delta);
            }
        }

        // Handle tooltips
        for (var element : this.children()) {
            if (element.isMouseOver(mouseX, mouseY)) {
                element.setFocused(true);
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override to prevent any default background rendering
    }

    @Override
    protected void init() {
        super.init();
    }

    private void renderCustomBackground(DrawContext context) {
        try {
            // Set up rendering state
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            // Draw the background texture scaled to fill the screen
            context.drawTexture(BACKGROUND_TEXTURE, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
            
            RenderSystem.disableBlend();
        } catch (Exception e) {
            // Fallback to a simple gradient if the texture fails to load
            renderFallbackBackground(context);
        }
    }
    
    private void renderFallbackBackground(DrawContext context) {
        // Simple dark gradient fallback
        int color1 = 0xFF1a1a2e;
        int color2 = 0xFF16213e;
        context.fillGradient(0, 0, this.width, this.height, color1, color2);
    }

    private void renderWatermarkAndTitle(DrawContext context) {
        String title = "Meowsense";
        String version = "v1.3";

        // Center the title text on screen
        context.getMatrices().push();
        context.getMatrices().scale(3.0f, 3.0f, 1.0f); // Larger scale for better visibility

        // Calculate centered position for title
        int titleWidth = this.textRenderer.getWidth(title);
        int centerX = (this.width / 2) / 3; // Divide by scale factor
        int titleY = 30; // Higher up on screen

        // Draw centered title with animated color
        float colorShift = (float) Math.sin(time * 1.5f);
        int titleColor = 0xFF00FF | ((int)((0.5f + 0.5f * colorShift) * 255) << 8);

        context.drawCenteredTextWithShadow(this.textRenderer, title, centerX, titleY, titleColor);
        context.getMatrices().pop();

        // Version in top right corner
        int versionX = this.width - this.textRenderer.getWidth(version) - 15;
        context.drawTextWithShadow(this.textRenderer, version, versionX, 15, 0xAAAAAA);
    }

    @Override
    protected void renderPanoramaBackground(DrawContext context, float delta) {
        // Override to prevent panorama background rendering
    }

    @Override
    protected void renderDarkening(DrawContext context) {
        // Override to prevent darkening overlay
    }

    @Override
    public void close() {
        super.close();
    }
}