package dev.hez.meowsense.module.modules.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.player.EventTickPre;
import dev.hez.meowsense.event.impl.render.EventRender2D;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.modules.combat.KillAura;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;
import dev.hez.meowsense.module.setting.impl.ModeSetting;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.utils.font.FontManager;
import dev.hez.meowsense.utils.font.fonts.FontRenderer;
import dev.hez.meowsense.utils.render.DrawUtils;
import dev.hez.meowsense.utils.render.ThemeUtils;
import dev.hez.meowsense.utils.render.animations.MutableAnimation;
import dev.hez.meowsense.utils.render.shaders.ShaderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class TargetHUD extends Module {
    public static final ModeSetting targethudmode = new ModeSetting("TargetHUD Mode", "Nexus", "Nexus", "Adjust", "Novo", "Meow");
    public static final BooleanSetting deb = new BooleanSetting("Debug", true);
    public static final NumberSetting opacity = new NumberSetting("BG Opacity", 0, 255, 80, 1);
    public static final NumberSetting animationSpeed = new NumberSetting("Animation Speed", 0.1, 1.0, 0.6, 0.1);

    // Position settings
    private int posX = 20;
    private int posY = 50;
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    // Enhanced animations
    private final MutableAnimation posXAnimation = new MutableAnimation(20, 0.2f);
    private final MutableAnimation posYAnimation = new MutableAnimation(50, 0.2f);
    private final MutableAnimation healthBarAnimation = new MutableAnimation(0, 1);
    private final MutableAnimation scaleAnimation = new MutableAnimation(0, 0.25f);
    private final MutableAnimation alphaAnimation = new MutableAnimation(0, 0.3f);

    // Animation states
    private boolean wasVisible = false;
    private boolean isAnimatingIn = false;
    private boolean isAnimatingOut = false;
    private long lastTargetChangeTime = 0;
    private PlayerEntity lastTarget = null;

    // HUD dimensions for different modes
    private int hudWidth = 150;
    private int hudHeight = 60;
    private final String configPath = "meowsense/targethud_position.txt";

    public TargetHUD() {
        super("TargetHUD", "Displays information about your targets", 0, ModuleCategory.CLIENT);
        this.addSettings(targethudmode, deb, opacity, animationSpeed);
        loadPosition();
    }

    private PlayerEntity target;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");

    @Override
    public void onEnable() {
        target = null;
        lastTarget = null;
        loadPosition();
        // Initialize animations
        posXAnimation.setValue(posX);
        posYAnimation.setValue(posY);
        scaleAnimation.setValue(0);
        alphaAnimation.setValue(0);
        wasVisible = false;
        isAnimatingIn = false;
        isAnimatingOut = false;
        super.onEnable();
    }

    @EventLink
    public final Listener<EventTickPre> eventTickListener = event -> {
        if (isNull()) return;

        // Update animation speed based on setting
        float speed = animationSpeed.getValueFloat();
        scaleAnimation.setSpeed(speed * 1.9f); // Adjust speed to be more responsive
        alphaAnimation.setSpeed(speed * 1.9f); // Slightly faster alpha for snappier feel

        if (!deb.getValue()) {
            KillAura aura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
            target = aura.isEnabled() ? aura.target : null;
        } else {
            target = mc.player;
        }

        // Handle target changes for pop animation
        if (target != lastTarget) {
            lastTargetChangeTime = System.currentTimeMillis();
            if (target != null && lastTarget == null) {
                // Target appeared - start pop in animation
                startPopInAnimation();
            } else if (target == null && lastTarget != null) {
                // Target disappeared - start pop out animation
                startPopOutAnimation();
            } else if (target != null && lastTarget != null) {
                // Target changed - quick pop effect
                startTargetChangeAnimation();
            }
            lastTarget = target;
        }

        if (mc.currentScreen instanceof ChatScreen && target != null) handleDragging();
    };

    @EventLink
    public final Listener<EventRender2D> eventRender2DListener = event -> {
        if (isNull()) return;

        boolean shouldShow = (mc.currentScreen instanceof ChatScreen) || target != null;

        // Update visibility state and animations
        if (shouldShow != wasVisible) {
            if (shouldShow) {
                startPopInAnimation();
            } else {
                startPopOutAnimation();
            }
            wasVisible = shouldShow;
        }

        // Don't render if completely invisible
        if (!shouldShow && scaleAnimation.getValue() <= 0.05 && alphaAnimation.getValue() <= 0.05) {
            return;
        }

        PlayerEntity renderTarget = target != null ? target : mc.player;
        if (renderTarget == null && !shouldShow) return;
        if (renderTarget == null) renderTarget = mc.player; // Fallback for chat screen

        updateHudDimensions();
        updateAnimations();

        // Only render if there's something to show
        if (scaleAnimation.getValue() > 0.05) {
            PlayerEntity finalRenderTarget = renderTarget;
            PlayerEntity finalRenderTarget1 = renderTarget;
            PlayerEntity finalRenderTarget2 = renderTarget;
            PlayerEntity finalRenderTarget3 = renderTarget;
            switch (targethudmode.getMode()) {
                case "Nexus" -> drawScaled(event, () -> drawNexusTargetHud(event, finalRenderTarget1));
                case "Adjust" -> drawScaled(event, () -> drawAdjustTargetHud(event, finalRenderTarget));
                case "Novo" -> drawScaled(event, () -> drawNovolineTargetHud(event, finalRenderTarget2));
                case "Meow" -> drawScaled(event, () -> drawMeowTargetHud(event, finalRenderTarget3));
            }
        }

        if (mc.currentScreen instanceof ChatScreen) drawDragIndicator(event);
    };

    private void startPopInAnimation() {
        isAnimatingIn = true;
        isAnimatingOut = false;
        // Start from small scale with overshoot effect
        scaleAnimation.setValue(0.0);
        alphaAnimation.setValue(0.0);
    }

    private void startPopOutAnimation() {
        isAnimatingIn = false;
        isAnimatingOut = true;
    }

    private void startTargetChangeAnimation() {
        // Quick bounce effect when target changes
        scaleAnimation.setValue(scaleAnimation.getValue() * 1.15); // Slight overshoot
    }

    private void updateAnimations() {
        posXAnimation.interpolate(posX);
        posYAnimation.interpolate(posY);

        if (isAnimatingIn) {
            // Pop in with overshoot effect
            double targetScale = 1.0;
            double currentScale = scaleAnimation.getValue();

            if (currentScale < 0.95) {
                // First phase: scale up to 1.1 (overshoot)
                scaleAnimation.interpolate(1.1);
            } else if (currentScale >= 1.05) {
                // Second phase: settle back to 1.0
                scaleAnimation.interpolate(1.0);
            }

            alphaAnimation.interpolate(1.0);

            // Check if animation is complete
            if (Math.abs(scaleAnimation.getValue() - 1.0) < 0.05 &&
                    Math.abs(alphaAnimation.getValue() - 1.0) < 0.05) {
                isAnimatingIn = false;
                scaleAnimation.setValue(1.0);
                alphaAnimation.setValue(1.0);
            }
        } else if (isAnimatingOut) {
            // Pop out - shrink and fade
            scaleAnimation.interpolate(0.0);
            alphaAnimation.interpolate(0.0);

            // Check if animation is complete
            if (scaleAnimation.getValue() < 0.05 && alphaAnimation.getValue() < 0.05) {
                isAnimatingOut = false;
                scaleAnimation.setValue(0.0);
                alphaAnimation.setValue(0.0);
            }
        } else if (target != null || mc.currentScreen instanceof ChatScreen) {
            // Maintain visible state
            scaleAnimation.interpolate(1.0);
            alphaAnimation.interpolate(1.0);
        }
    }

    private void drawScaled(EventRender2D event, Runnable drawer) {
        MatrixStack matrices = event.getContext().getMatrices();
        matrices.push();

        float scale = (float) scaleAnimation.getValue();
        float alpha = (float) alphaAnimation.getValue();

        // Calculate center point for scaling
        float centerX = (float) (posXAnimation.getValue() + hudWidth / 2f);
        float centerY = (float) (posYAnimation.getValue() + hudHeight / 2f);

        // Apply transformations
        matrices.translate(centerX, centerY, 0);
        matrices.scale(scale, scale, 1);
        matrices.translate(-centerX, -centerY, 0);

        // Store original alpha for restoration
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Apply alpha blending
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        drawer.run();

        // Restore alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }

    private void handleDragging() {
        if (mc.mouse == null) return;

        double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / (double) mc.getWindow().getHeight();

        boolean isMousePressed = org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

        // Use interpolated positions for hit detection
        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();

        if (isMousePressed && !isDragging) {
            // Check if mouse is over the HUD
            if (mouseX >= currentPosX && mouseX <= currentPosX + hudWidth &&
                    mouseY >= currentPosY && mouseY <= currentPosY + hudHeight) {
                isDragging = true;
                dragOffsetX = (int)(mouseX - currentPosX);
                dragOffsetY = (int)(mouseY - currentPosY);
            }
        } else if (!isMousePressed && isDragging) {
            // Stop dragging and save position
            isDragging = false;
            savePosition();
        }

        if (isDragging) {
            posX = (int)(mouseX - dragOffsetX);
            posY = (int)(mouseY - dragOffsetY);

            // Keep within screen bounds
            posX = Math.max(0, Math.min(posX, mc.getWindow().getScaledWidth() - hudWidth));
            posY = Math.max(0, Math.min(posY, mc.getWindow().getScaledHeight() - hudHeight));
        }
    }

    private void updateHudDimensions() {
        switch (targethudmode.getMode()) {
            case "Nexus" -> {
                hudWidth = 150;
                hudHeight = 60;
            }
            case "Adjust" -> {
                hudWidth = 130;
                hudHeight = 55;
            }
            case "Novo" -> {
                hudWidth = 130;
                hudHeight = 50;
            }
            case "Meow" -> {
                hudWidth = 150;
                hudHeight = 60;
            }
        }
    }

    private void drawDragIndicator(EventRender2D event) {
        MatrixStack matrices = event.getContext().getMatrices();

        // Use interpolated positions
        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();

        // Apply alpha to drag indicators too
        float alpha = (float) alphaAnimation.getValue();
        Color borderColor = new Color(255, 255, 255, (int)(100 * alpha));

        // Draw border to indicate draggable area
        DrawUtils.drawRect(matrices, currentPosX - 1, currentPosY - 1, currentPosX + hudWidth + 1, currentPosY - 1, borderColor);
        DrawUtils.drawRect(matrices, currentPosX - 1, currentPosY + hudHeight + 1, currentPosX + hudWidth + 1, currentPosY + hudHeight + 1, borderColor);
        DrawUtils.drawRect(matrices, currentPosX - 1, currentPosY - 1, currentPosX - 1, currentPosY + hudHeight + 1, borderColor);
        DrawUtils.drawRect(matrices, currentPosX + hudWidth + 1, currentPosY - 1, currentPosX + hudWidth + 1, currentPosY + hudHeight + 1, borderColor);

        // Draw drag text with alpha
        String dragText = "Drag to reposition";
        int textWidth = mc.textRenderer.getWidth(dragText);
        int textColor = (int)(255 * alpha) << 24 | 0xFFFFFF;
        event.getContext().drawText(mc.textRenderer, dragText, currentPosX + (hudWidth - textWidth) / 2, currentPosY - 12, textColor, true);
    }

    private void savePosition() {
        try {
            Path configDir = Paths.get("meowsense");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(configPath))) {
                writer.println(posX);
                writer.println(posY);
            }
        } catch (IOException e) {
            System.err.println("Failed to save TargetHUD position: " + e.getMessage());
        }
    }

    private void loadPosition() {
        try {
            File configFile = new File(configPath);
            if (configFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                    String xLine = reader.readLine();
                    String yLine = reader.readLine();

                    if (xLine != null && yLine != null) {
                        posX = Integer.parseInt(xLine.trim());
                        posY = Integer.parseInt(yLine.trim());

                        // Update animations with loaded values
                        posXAnimation.setValue(posX);
                        posYAnimation.setValue(posY);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load TargetHUD position, using defaults: " + e.getMessage());
            posX = 20;
            posY = 50;
            posXAnimation.setValue(posX);
            posYAnimation.setValue(posY);
        }
    }

    private void drawNovolineTargetHud(EventRender2D event, PlayerEntity renderTarget) {
        int maxxL = 100;
        int headSize = 24;

        String name = renderTarget.getGameProfile().getName();

        // Apply alpha to colors
        float alpha = (float) alphaAnimation.getValue();
        Color backgroundColor = new Color(45, 45, 45, (int)(255 * alpha));
        Color backgroundColor2 = new Color(21, 21, 21, (int)(255 * alpha));

        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();

        DrawUtils.drawRect(event.getContext().getMatrices(), currentPosX - 1, currentPosY - 1, currentPosX + 2 + headSize + maxxL + 1, currentPosY + 2 + headSize + 2 + 1, backgroundColor2);
        DrawUtils.drawRect(event.getContext().getMatrices(), currentPosX, currentPosY, currentPosX + 2 + headSize + maxxL, currentPosY + 2 + headSize + 2, backgroundColor);

        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 2, currentPosY + 2, headSize);

        int textColor = (int)(255 * alpha) << 24 | 0xFFFFFF;
        event.getContext().drawText(mc.textRenderer, name, currentPosX + 2 + headSize + 2, currentPosY + 4, textColor, true);

        int healthbarstartx = currentPosX + 2 + headSize + 2;
        int healthbarstarty = currentPosY + 4 + mc.textRenderer.fontHeight + 2;
        int healthbarendx = currentPosX + 2 + headSize + maxxL - 2;
        int healthbarendy = healthbarstarty + mc.textRenderer.fontHeight + 2;

        double healthN = renderTarget.getHealth();
        double maxHealth = renderTarget.getMaxHealth();
        double healthPercentage = healthN / maxHealth;

        String healthPercentageText = decimalFormat.format(healthPercentage * 100) + "%";

        Color darkBg = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), (int)(backgroundColor.getAlpha() * 0.8));
        DrawUtils.drawRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, healthbarendx, healthbarendy, darkBg);

        int filledHealthbarEndX = (int) (healthbarstartx + (healthbarendx - healthbarstartx) * healthPercentage);

        healthBarAnimation.interpolate(filledHealthbarEndX);

        Color mainColor = new Color(ThemeUtils.getMainColor().getRed(), ThemeUtils.getMainColor().getGreen(), ThemeUtils.getMainColor().getBlue(), (int)(255 * alpha));
        Color outlineColor = new Color(0, 0, 0, (int)(255 * alpha));
        DrawUtils.drawRectWithOutline(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, (int) healthBarAnimation.getValue(), healthbarendy, mainColor, outlineColor);

        int textWidth = mc.textRenderer.getWidth(healthPercentageText);
        int textHeight = mc.textRenderer.fontHeight;

        double rectCenterX = (healthbarstartx + healthbarendx) / 2.0;
        double rectCenterY = (healthbarstarty + healthbarendy) / 2.0;

        double textX = rectCenterX - textWidth / 2.0;
        double textY = rectCenterY - textHeight / 2.0;

        event.getContext().drawText(mc.textRenderer, healthPercentageText, (int) textX, (int) textY + 1, textColor, true);
    }

    private void drawAdjustTargetHud(EventRender2D event, PlayerEntity renderTarget) {
        String name = renderTarget.getGameProfile().getName();
        int maxxL = 100;

        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();
        float alpha = (float) alphaAnimation.getValue();

        if (PostProcessing.shouldBlurTargetHud()) {
            Color glowColor = new Color((int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha));
            ShaderUtils.drawGlow(event.getContext().getMatrices(), currentPosX, currentPosY, 3 + 24 + 3 + maxxL + 3, 3 + 24 + 8 + 3, 30, glowColor);
        }

        Color bgColor = new Color(43, 43, 43, (int)(opacity.getValueInt() * alpha));
        DrawUtils.drawRect(event.getContext().getMatrices(), currentPosX, currentPosY, currentPosX + 3 + 24 + 3 + maxxL + 3, currentPosY + 3 + 24 + 8 + 3, bgColor);

        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 3, currentPosY + 3, 24);

        Color gray = new Color((int)(Color.WHITE.darker().getRed() * alpha), (int)(Color.WHITE.darker().getGreen() * alpha), (int)(Color.WHITE.darker().getBlue() * alpha));
        FontRenderer fontRenderer = Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.VERDANA);
        fontRenderer.drawString(event.getContext().getMatrices(), name, currentPosX + 3 + 24 + 3, currentPosY + 2, gray);

        drawArmor(event.getContext(), renderTarget, currentPosX + 3 + 24, currentPosY + 3 + mc.textRenderer.fontHeight + 2, 1, alpha);

        int healthbarstartx = currentPosX + 3;
        int healthbarstarty = currentPosY + 3 + 24 + 3;
        int healthbarendx = currentPosX + 3 + 24 + 3 + maxxL;
        int healthbarendy = currentPosY + 3 + 24 + 8;

        Color healthBg = new Color(0, 0, 0, (int)(80 * alpha));
        DrawUtils.drawRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, healthbarendx, healthbarendy, healthBg);

        double healthN = renderTarget.getHealth();
        double maxHealth = renderTarget.getMaxHealth();
        double healthPercentage = healthN / maxHealth;

        int filledHealthbarEndX = (int) (healthbarstartx + (healthbarendx - healthbarstartx) * healthPercentage);

        healthBarAnimation.interpolate(filledHealthbarEndX);

        Color mainColor = new Color(ThemeUtils.getMainColor().getRed(), ThemeUtils.getMainColor().getGreen(), ThemeUtils.getMainColor().getBlue(), (int)(255 * alpha));
        Color outlineColor = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(), Color.DARK_GRAY.getBlue(), (int)(255 * alpha));
        DrawUtils.drawRectWithOutline(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, (int) healthBarAnimation.getValue(), healthbarendy, mainColor, outlineColor);

        String sheesh = decimalFormat.format(Math.abs(mc.player.getHealth() - renderTarget.getHealth()));
        String healthDiff = mc.player.getHealth() < renderTarget.getHealth() ? "-" + sheesh : "+" + sheesh;

        int healthDiffWidth = (int) fontRenderer.getStringWidth(healthDiff);
        int healthDiffHeight = (int) fontRenderer.getStringHeight(healthDiff);

        fontRenderer.drawString(event.getContext().getMatrices(), healthDiff, currentPosX + 3 + 24 + 3 + maxxL - healthDiffWidth, currentPosY + 3 + 24 + 3 - healthDiffHeight, gray);
    }

    private void drawMeowTargetHud(EventRender2D event, PlayerEntity renderTarget) {
        if (renderTarget == null || mc.player == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        MatrixStack matrices = event.getContext().getMatrices();

        int boxWidth = 150;
        int boxHeight = 60;
        int avatarSize = 40;

        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();
        float alpha = (float) alphaAnimation.getValue();

        // Background with alpha
        Color bgColor = new Color(15, 15, 15, (int)(opacity.getValueInt() * alpha));
        DrawUtils.drawRoundedRect(matrices, currentPosX, currentPosY, currentPosX + boxWidth, currentPosY + boxHeight, 6, bgColor);

        // Avatar
        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 5, currentPosY + (boxHeight - avatarSize) / 2, avatarSize);

        // Name
        String name = renderTarget.getGameProfile().getName();
        FontRenderer font = Client.INSTANCE.getFontManager().getSize(12, FontManager.Type.Tenacity);
        Color nameColor = new Color(255, 255, 255, (int)(255 * alpha));
        font.drawString(matrices, name, currentPosX + 5 + avatarSize + 6, currentPosY + 8, nameColor);

        // Health Bar
        double health = Math.max(0, renderTarget.getHealth());
        double maxHealth = Math.max(1, renderTarget.getMaxHealth());
        double pct = Math.min(1.0, health / maxHealth);

        int barX = currentPosX + 5 + avatarSize + 6;
        int barY = currentPosY + boxHeight - 18;
        int barWidth = boxWidth - (barX - currentPosX) - 10;
        int barHeight = 8;

        int filledWidth = (int) (barWidth * pct);
        healthBarAnimation.interpolate(barX + filledWidth);

        // Draw bar background with alpha
        Color barBg = new Color(0, 0, 0, (int)(100 * alpha));
        DrawUtils.drawRoundedRect(matrices, barX, barY, barX + barWidth, barY + barHeight, 3, barBg);

        // Draw filled health bar with alpha
        Color mainColor = new Color(ThemeUtils.getMainColor().getRed(), ThemeUtils.getMainColor().getGreen(), ThemeUtils.getMainColor().getBlue(), (int)(255 * alpha));
        Color secondColor = new Color(ThemeUtils.getSecondColor().getRed(), ThemeUtils.getSecondColor().getGreen(), ThemeUtils.getSecondColor().getBlue(), (int)(255 * alpha));
        DrawUtils.drawRoundedHorizontalGradientRect(
                matrices,
                barX,
                barY,
                (int) healthBarAnimation.getValue(),
                barY + barHeight,
                3,
                mainColor,
                secondColor
        );

        // Health text (centered over bar) with alpha
        String healthText = decimalFormat.format(health) + " / " + decimalFormat.format(maxHealth);
        int textWidth = mc.textRenderer.getWidth(healthText);
        int textX = barX + (barWidth - textWidth) / 2;
        int textY = barY - 2 - mc.textRenderer.fontHeight;

        int textColor = (int)(255 * alpha) << 24 | 0xFFFFFF;
        event.getContext().drawText(mc.textRenderer, healthText, textX, textY, textColor, true);
    }

    private void drawNexusTargetHud(EventRender2D event, PlayerEntity renderTarget) {
        String name = renderTarget.getGameProfile().getName();

        int nameL = mc.textRenderer.getWidth(name);

        String health = decimalFormat.format(renderTarget.getHealth()) + " HP" + " | " + getW_L(renderTarget);

        int healthLenght = mc.textRenderer.getWidth(health);

        int maxxL = Math.max(nameL, healthLenght);

        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();
        float alpha = (float) alphaAnimation.getValue();

        if (PostProcessing.shouldBlurTargetHud()) {
            Color glowColor = new Color((int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha));
            ShaderUtils.drawGlow(event.getContext().getMatrices(), currentPosX, currentPosY, 3 + 32 + 3 + maxxL, 3 + 32 + 3, 30, glowColor);
        }

        Color bgColor = new Color(43, 43, 43, (int)(opacity.getValueInt() * alpha));
        DrawUtils.drawRoundedRect(event.getContext().getMatrices(), currentPosX, currentPosY, currentPosX + 3 + 32 + 3 + maxxL + 3, currentPosY + 3 + 32 + 3, 3, bgColor);

        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 3, currentPosY + 3, 32);

        int textColor = (int)(255 * alpha) << 24 | 0xFFFFFF;
        event.getContext().drawText(mc.textRenderer, name, currentPosX + 3 + 32 + 3, currentPosY + 3, textColor, true);
        event.getContext().drawText(mc.textRenderer, health, currentPosX + 3 + 32 + 3, currentPosY + 3 + mc.textRenderer.fontHeight, textColor, true);

        int healthbarstartx = currentPosX + 3 + 32 + 3;
        int healthbarstarty = currentPosY + 3 + 2 * mc.textRenderer.fontHeight + 3;
        int healthbarendx = currentPosX + 3 + 32 + 3 + maxxL;
        int healthbarendy = currentPosY + 3 + 32;

        Color healthBg = new Color(0, 0, 0, (int)(80 * alpha));
        DrawUtils.drawRoundedRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, healthbarendx, healthbarendy, 3, healthBg);

        double healthN = renderTarget.getHealth();
        double maxHealth = renderTarget.getMaxHealth();
        double healthPercentage = healthN / maxHealth;

        int filledHealthbarEndX = (int) (healthbarstartx + (healthbarendx - healthbarstartx) * healthPercentage);

        filledHealthbarEndX = Math.clamp(filledHealthbarEndX, healthbarstartx, healthbarendx);

        healthBarAnimation.interpolate(filledHealthbarEndX);

        Color mainColor = new Color(ThemeUtils.getMainColor().getRed(), ThemeUtils.getMainColor().getGreen(), ThemeUtils.getMainColor().getBlue(), (int)(255 * alpha));
        Color secondColor = new Color(ThemeUtils.getSecondColor().getRed(), ThemeUtils.getSecondColor().getGreen(), ThemeUtils.getSecondColor().getBlue(), (int)(255 * alpha));
        DrawUtils.drawRoundedHorizontalGradientRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, (int) healthBarAnimation.getValue(), healthbarendy, 3, mainColor, secondColor);
    }

    private String getW_L(PlayerEntity renderTarget) {
        if (renderTarget.getHealth() < mc.player.getHealth()) {
            return "Winning";
        } else if (renderTarget.getHealth() > mc.player.getHealth()) {
            return "Losing";
        } else {
            return "Draw";
        }
    }

    private void drawArmor(DrawContext context, PlayerEntity target, float posX, float posY, float scale) {
        drawArmor(context, target, posX, posY, scale, 1.0f);
    }

    private void drawArmor(DrawContext context, PlayerEntity target, float posX, float posY, float scale, float alpha) {
        final float spacing = 15.0f * scale;
        float currentX = posX;

        // Apply alpha to rendering
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        for (int i = 0; i < 4; i++) {
            if (!target.getInventory().armor.get(3 - i).isEmpty()) {
                context.getMatrices().push();

                context.getMatrices().translate(currentX, posY, 0);
                context.getMatrices().scale(scale, scale, scale);

                context.drawItem(target.getInventory().armor.get(3 - i), 0, 0);
                context.drawItemInSlot(mc.textRenderer, target.getInventory().armor.get(3 - i), 0, 0);

                context.getMatrices().pop();

                currentX += spacing;
            }
        }

        // Restore alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}