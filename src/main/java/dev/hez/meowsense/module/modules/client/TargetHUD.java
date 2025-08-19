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
    public static final ModeSetting targethudmode = new ModeSetting("TargetHUD Mode", "Nexus", "Nexus", "Adjust", "Novo", "Meow", "Cool", "Compact");
    public static final BooleanSetting deb = new BooleanSetting("Debug", true);
    public static final NumberSetting opacity = new NumberSetting("BG Opacity", 0, 255, 80, 1);
    public static final NumberSetting animationSpeed = new NumberSetting("Animation Speed", 0.1, 1.0, 0.6, 0.1);

    // Position settings
    private int posX = 20;
    private int posY = 50;
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    // Enhanced animations - fast but smooth (under 0.5 seconds)
    private final MutableAnimation posXAnimation = new MutableAnimation(20, 0.4f);
    private final MutableAnimation posYAnimation = new MutableAnimation(50, 0.4f);
    private final MutableAnimation healthBarAnimation = new MutableAnimation(0, 1.8f);
    private final MutableAnimation scaleAnimation = new MutableAnimation(0, 1.2f);
    private final MutableAnimation alphaAnimation = new MutableAnimation(0, 1.4f);
    private final MutableAnimation slideAnimation = new MutableAnimation(0, 1.0f);

    // Animation states
    private boolean wasVisible = false;
    private boolean isAnimatingIn = false;
    private boolean isAnimatingOut = false;
    private long animationStartTime = 0;
    private PlayerEntity lastTarget = null;
    
    // Smooth animation parameters
    private double targetScale = 0.0;
    private double targetAlpha = 0.0;
    private double targetSlide = 0.0;

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
        // Initialize animations to 0 so they can animate in
        posXAnimation.setValue(posX);
        posYAnimation.setValue(posY);
        scaleAnimation.setValue(0);
        alphaAnimation.setValue(0);
        slideAnimation.setValue(-10);
        wasVisible = false;
        isAnimatingIn = false;
        isAnimatingOut = false;
        super.onEnable();
    }

    @EventLink
    public final Listener<EventTickPre> eventTickListener = event -> {
        if (isNull()) return;

        // Set smooth but fast animation speeds (under 0.5 seconds)
        float baseSpeed = animationSpeed.getValueFloat();
        scaleAnimation.setSpeed(baseSpeed * 1.2f);
        alphaAnimation.setSpeed(baseSpeed * 1.4f);
        slideAnimation.setSpeed(baseSpeed * 1.0f);
        healthBarAnimation.setSpeed(baseSpeed * 1.8f);

        if (!deb.getValue()) {
            KillAura aura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
            target = aura.isEnabled() ? aura.target : null;
        } else {
            target = mc.player;
        }

        // Handle target changes for smooth animations
        if (target != lastTarget) {
            animationStartTime = System.currentTimeMillis();
            if (target != null && lastTarget == null) {
                // Target appeared - force scale in animation
                forceScaleInAnimation();
            } else if (target == null && lastTarget != null) {
                // Target disappeared - start smooth fade out
                startSmoothOutAnimation();
            } else if (target != null && lastTarget != null) {
                // Target changed - force scale in animation for new target
                forceScaleInAnimation();
            }
            lastTarget = target;
        }

        // Ensure animation starts when target exists but animations aren't running
        if (target != null && !isAnimatingIn && !isAnimatingOut && scaleAnimation.getValue() < 0.1) {
            forceScaleInAnimation();
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
                forceScaleInAnimation();
            } else {
                startSmoothOutAnimation();
            }
            wasVisible = shouldShow;
        }

        // Force animation if target exists but scale is at 0 (not animating)
        if (shouldShow && !isAnimatingIn && !isAnimatingOut && scaleAnimation.getValue() < 0.1) {
            forceScaleInAnimation();
        }

        // Don't render if completely invisible
        if (!shouldShow && scaleAnimation.getValue() <= 0.02 && alphaAnimation.getValue() <= 0.02) {
            return;
        }

        PlayerEntity renderTarget = target != null ? target : mc.player;
        if (renderTarget == null && !shouldShow) return;
        if (renderTarget == null) renderTarget = mc.player; // Fallback for chat screen

        updateHudDimensions();
        updateAnimations();

        // Only render if there's something to show
        if (scaleAnimation.getValue() > 0.02) {
            PlayerEntity finalRenderTarget = renderTarget;
            PlayerEntity finalRenderTarget1 = renderTarget;
            PlayerEntity finalRenderTarget2 = renderTarget;
            PlayerEntity finalRenderTarget3 = renderTarget;
            PlayerEntity finalRenderTarget4 = renderTarget;
            PlayerEntity finalRenderTarget5 = renderTarget;
            switch (targethudmode.getMode()) {
                case "Nexus" -> drawSmooth(event, () -> drawNexusTargetHud(event, finalRenderTarget1));
                case "Adjust" -> drawSmooth(event, () -> drawAdjustTargetHud(event, finalRenderTarget));
                case "Novo" -> drawSmooth(event, () -> drawNovolineTargetHud(event, finalRenderTarget2));
                case "Meow" -> drawSmooth(event, () -> drawMeowTargetHud(event, finalRenderTarget3));
                case "Cool" -> drawSmooth(event, () -> drawCoolTargetHud(event, finalRenderTarget4));
                case "Compact" -> drawSmooth(event, () -> drawCompactTargetHud(event, finalRenderTarget5));
            }
        }

        if (mc.currentScreen instanceof ChatScreen) drawDragIndicator(event);
    };

    private void forceScaleInAnimation() {
        // Force immediate reset to 0 and start animation
        scaleAnimation.setValue(0.0);
        alphaAnimation.setValue(0.0);
        slideAnimation.setValue(-10.0);
        
        isAnimatingIn = true;
        isAnimatingOut = false;
        
        targetScale = 1.0;
        targetAlpha = 1.0;
        targetSlide = 0.0;
        
        // Debug output
        System.out.println("Starting scale in animation - Scale: " + scaleAnimation.getValue() + " -> " + targetScale);
    }

    private void startSmoothInAnimation() {
        isAnimatingIn = true;
        isAnimatingOut = false;
        
        // Always start from scaled down state for consistent scale-in effect
        scaleAnimation.setValue(0.0);
        alphaAnimation.setValue(0.0);
        slideAnimation.setValue(-10.0);
        
        targetScale = 1.0;
        targetAlpha = 1.0;
        targetSlide = 0.0;
    }

    private void startSmoothOutAnimation() {
        isAnimatingIn = false;
        isAnimatingOut = true;
        targetScale = 0.0;
        targetAlpha = 0.0;
        targetSlide = -10.0; // Slide out slightly
        
        // Debug output
        System.out.println("Starting scale out animation - Scale: " + scaleAnimation.getValue() + " -> " + targetScale);
    }

    private void startTargetChangeAnimation() {
        // Gentle pulse effect when target changes
        if (scaleAnimation.getValue() > 0.8) {
            targetScale = 1.02;
            // Will settle back to 1.0 naturally
        }
    }

    private void updateAnimations() {
        posXAnimation.interpolate(posX);
        posYAnimation.interpolate(posY);

        if (isAnimatingIn) {
            // Smooth fade in with gentle scale
            scaleAnimation.interpolate(targetScale);
            alphaAnimation.interpolate(targetAlpha);
            slideAnimation.interpolate(targetSlide);

            // Check if animation is complete
            if (Math.abs(scaleAnimation.getValue() - targetScale) < 0.02 &&
                    Math.abs(alphaAnimation.getValue() - targetAlpha) < 0.02) {
                isAnimatingIn = false;
                targetScale = 1.0; // Ensure we settle at 1.0
                System.out.println("Scale in animation complete - Final scale: " + scaleAnimation.getValue());
            }
        } else if (isAnimatingOut) {
            // Smooth fade out
            scaleAnimation.interpolate(targetScale);
            alphaAnimation.interpolate(targetAlpha);
            slideAnimation.interpolate(targetSlide);

            // Check if animation is complete
            if (scaleAnimation.getValue() < 0.02 && alphaAnimation.getValue() < 0.02) {
                isAnimatingOut = false;
                System.out.println("Scale out animation complete - Final scale: " + scaleAnimation.getValue());
            }
        } else if (target != null || mc.currentScreen instanceof ChatScreen) {
            // Maintain visible state with smooth settling
            targetScale = 1.0;
            targetAlpha = 1.0;
            targetSlide = 0.0;
            
            scaleAnimation.interpolate(targetScale);
            alphaAnimation.interpolate(targetAlpha);
            slideAnimation.interpolate(targetSlide);
        }
    }

    private void drawSmooth(EventRender2D event, Runnable drawer) {
        MatrixStack matrices = event.getContext().getMatrices();
        matrices.push();

        float scale = (float) scaleAnimation.getValue();
        float alpha = (float) alphaAnimation.getValue();
        float slide = (float) slideAnimation.getValue();

        // Calculate center point for scaling
        float centerX = (float) (posXAnimation.getValue() + hudWidth / 2f);
        float centerY = (float) (posYAnimation.getValue() + hudHeight / 2f);

        // Apply smooth transformations
        matrices.translate(centerX + slide, centerY, 0);
        matrices.scale(scale, scale, 1);
        matrices.translate(-centerX, -centerY, 0);

        // Store original alpha for restoration
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Apply smooth alpha blending with easing
        float easedAlpha = easeInOutQuart(alpha);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, easedAlpha);

        drawer.run();

        // Restore alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }

    // Smooth easing function for better animation feel - faster but still smooth
    private float easeInOutQuart(float t) {
        return t < 0.5f ? 8 * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 4) / 2;
    }

    private void drawCompactTargetHud(EventRender2D event, PlayerEntity renderTarget) {
        if (renderTarget == null || mc.player == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();
        float alpha = (float) alphaAnimation.getValue();

        // Get player info
        String name = renderTarget.getGameProfile().getName();
        double health = Math.max(0, renderTarget.getHealth());
        double maxHealth = Math.max(1, renderTarget.getMaxHealth());
        double healthPct = Math.min(1.0, health / maxHealth);

        // Fonts
        FontRenderer nameFont = Client.INSTANCE.getFontManager().getSize(12, FontManager.Type.Tenacity);
        FontRenderer infoFont = Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.VERDANA);

        // Calculate text widths for centering
        int nameWidth = (int) nameFont.getStringWidth(name);
        String healthText = decimalFormat.format(health) + " HP";
        int healthWidth = (int) infoFont.getStringWidth(healthText);
        String wlStatus = getW_L(renderTarget);
        int wlWidth = (int) infoFont.getStringWidth(wlStatus);

        // Find the widest element for centering
        int maxWidth = Math.max(nameWidth, Math.max(healthWidth, wlWidth));
        int centerX = currentPosX + maxWidth / 2;

        // Draw name (centered, white)
        Color nameColor = new Color(255, 255, 255, (int)(255 * alpha));
        nameFont.drawString(event.getContext().getMatrices(), name, 
            centerX - nameWidth / 2, currentPosY, nameColor);

        // Draw health (centered, color based on health percentage)
        Color healthColor = getCompactHealthColor(healthPct, alpha);
        infoFont.drawString(event.getContext().getMatrices(), healthText, 
            centerX - healthWidth / 2, currentPosY + 15, healthColor);

        // Draw W/L status (centered, color based on status)
        Color wlColor = getCompactWLColor(renderTarget, alpha);
        infoFont.drawString(event.getContext().getMatrices(), wlStatus, 
            centerX - wlWidth / 2, currentPosY + 27, wlColor);

        // Draw armor (centered horizontally)
        drawCompactArmor(event.getContext(), renderTarget, centerX, currentPosY + 40, 0.7f, alpha);
    }

    private Color getCompactHealthColor(double healthPct, float alpha) {
        if (healthPct > 0.6) {
            return new Color(100, 255, 100, (int)(255 * alpha)); // Green for healthy
        } else if (healthPct > 0.3) {
            return new Color(255, 255, 100, (int)(255 * alpha)); // Yellow for injured
        } else {
            return new Color(255, 100, 100, (int)(255 * alpha)); // Red for critical
        }
    }

    private Color getCompactWLColor(PlayerEntity renderTarget, float alpha) {
        if (renderTarget.getHealth() < mc.player.getHealth()) {
            return new Color(100, 255, 100, (int)(255 * alpha)); // Green for winning
        } else if (renderTarget.getHealth() > mc.player.getHealth()) {
            return new Color(255, 100, 100, (int)(255 * alpha)); // Red for losing
        } else {
            return new Color(255, 255, 100, (int)(255 * alpha)); // Yellow for draw
        }
    }

    private void drawCompactArmor(DrawContext context, PlayerEntity target, int centerX, int startY, float scale, float alpha) {
        // Count armor pieces to center them
        int armorCount = 0;
        for (int i = 0; i < 4; i++) {
            if (!target.getInventory().armor.get(3 - i).isEmpty()) {
                armorCount++;
            }
        }

        if (armorCount == 0) return;

        final float spacing = 14.0f * scale;
        float totalWidth = (armorCount - 1) * spacing + (16 * scale);
        float startX = centerX - totalWidth / 2;

        // Apply alpha to rendering
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        float currentX = startX;
        for (int i = 0; i < 4; i++) {
            if (!target.getInventory().armor.get(3 - i).isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(currentX, startY, 0);
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

    private void drawCoolTargetHud(EventRender2D event, PlayerEntity renderTarget) {
        if (renderTarget == null || mc.player == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        MatrixStack matrices = event.getContext().getMatrices();

        int boxWidth = 180;
        int boxHeight = 75;
        int avatarSize = 45;

        int currentPosX = (int) posXAnimation.getValue();
        int currentPosY = (int) posYAnimation.getValue();
        float alpha = (float) alphaAnimation.getValue();

        // Main background with solid color
        Color bgColor = new Color(20, 20, 30, (int)(opacity.getValueInt() * alpha));
        DrawUtils.drawRoundedRect(matrices, currentPosX, currentPosY, currentPosX + boxWidth, currentPosY + boxHeight, 8, bgColor);

        // Accent border
        Color accentColor = new Color(ThemeUtils.getMainColor().getRed(), ThemeUtils.getMainColor().getGreen(), ThemeUtils.getMainColor().getBlue(), (int)(150 * alpha));
        DrawUtils.drawRoundedRect(matrices, currentPosX - 1, currentPosY - 1, currentPosX + boxWidth + 1, currentPosY + boxHeight + 1, 9, accentColor);

        // Avatar with rounded background
        Color avatarBg = new Color(35, 35, 45, (int)(200 * alpha));
        DrawUtils.drawRoundedRect(matrices, currentPosX + 8, currentPosY + 8, currentPosX + 8 + avatarSize + 4, currentPosY + 8 + avatarSize + 4, 6, avatarBg);
        
        // Draw avatar with proper alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 10, currentPosY + 10, avatarSize);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Name with cool font
        String name = renderTarget.getGameProfile().getName();
        FontRenderer nameFont = Client.INSTANCE.getFontManager().getSize(14, FontManager.Type.Tenacity);
        Color nameColor = new Color(255, 255, 255, (int)(255 * alpha));
        nameFont.drawString(matrices, name, currentPosX + avatarSize + 20, currentPosY + 12, nameColor);

        // W/L Status with colored background
        String wlStatus = getW_L(renderTarget);
        Color wlColor = getWLColor(renderTarget, alpha);
        Color wlBgColor = new Color(wlColor.getRed(), wlColor.getGreen(), wlColor.getBlue(), (int)(50 * alpha));
        
        FontRenderer statusFont = Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.VERDANA);
        int statusWidth = (int) statusFont.getStringWidth(wlStatus) + 8;
        int statusHeight = 16;
        int statusX = currentPosX + boxWidth - statusWidth - 10;
        int statusY = currentPosY + 10;
        
        DrawUtils.drawRoundedRect(matrices, statusX, statusY, statusX + statusWidth, statusY + statusHeight, 3, wlBgColor);
        statusFont.drawString(matrices, wlStatus, statusX + 4, statusY + 4, wlColor);

        // Health info
        double health = Math.max(0, renderTarget.getHealth());
        double maxHealth = Math.max(1, renderTarget.getMaxHealth());
        double pct = Math.min(1.0, health / maxHealth);

        String healthText = decimalFormat.format(health) + " / " + decimalFormat.format(maxHealth) + " HP";
        FontRenderer healthFont = Client.INSTANCE.getFontManager().getSize(11, FontManager.Type.VERDANA);
        Color healthTextColor = new Color(200, 200, 200, (int)(255 * alpha));
        healthFont.drawString(matrices, healthText, currentPosX + avatarSize + 20, currentPosY + 30, healthTextColor);

        // Health Bar with glow effect
        int barX = currentPosX + avatarSize + 20;
        int barY = currentPosY + 45;
        int barWidth = boxWidth - avatarSize - 35;
        int barHeight = 6;

        // Health bar background
        Color barBg = new Color(40, 40, 50, (int)(200 * alpha));
        DrawUtils.drawRoundedRect(matrices, barX, barY, barX + barWidth, barY + barHeight, 3, barBg);

        // Animated health bar
        int filledWidth = (int) (barWidth * pct);
        healthBarAnimation.interpolate(barX + filledWidth);

        // Health bar with gradient
        Color healthColor1 = getHealthColor(pct, alpha, true);
        Color healthColor2 = getHealthColor(pct, alpha, false);
        DrawUtils.drawRoundedHorizontalGradientRect(matrices, barX, barY, (int) healthBarAnimation.getValue(), barY + barHeight, 3, healthColor1, healthColor2);

        // Armor display
        drawCoolArmor(event.getContext(), renderTarget, currentPosX + 10, currentPosY + boxHeight - 20, 0.8f, alpha);

        // Damage indicator
        String damageText = getDamageIndicator(renderTarget);
        if (!damageText.isEmpty()) {
            FontRenderer damageFont = Client.INSTANCE.getFontManager().getSize(9, FontManager.Type.VERDANA);
            Color damageColor = getDamageColor(renderTarget, alpha);
            int damageX = currentPosX + boxWidth - (int) damageFont.getStringWidth(damageText) - 10;
            int damageY = currentPosY + boxHeight - 15;
            damageFont.drawString(matrices, damageText, damageX, damageY, damageColor);
        }
    }

    private Color getWLColor(PlayerEntity renderTarget, float alpha) {
        if (renderTarget.getHealth() < mc.player.getHealth()) {
            return new Color(100, 255, 100, (int)(255 * alpha)); // Green for winning
        } else if (renderTarget.getHealth() > mc.player.getHealth()) {
            return new Color(255, 100, 100, (int)(255 * alpha)); // Red for losing
        } else {
            return new Color(255, 255, 100, (int)(255 * alpha)); // Yellow for draw
        }
    }

    private Color getHealthColor(double healthPct, float alpha, boolean isFirst) {
        if (healthPct > 0.6) {
            return isFirst ? new Color(100, 255, 100, (int)(255 * alpha)) : new Color(50, 200, 50, (int)(255 * alpha));
        } else if (healthPct > 0.3) {
            return isFirst ? new Color(255, 255, 100, (int)(255 * alpha)) : new Color(200, 200, 50, (int)(255 * alpha));
        } else {
            return isFirst ? new Color(255, 100, 100, (int)(255 * alpha)) : new Color(200, 50, 50, (int)(255 * alpha));
        }
    }

    private String getDamageIndicator(PlayerEntity renderTarget) {
        double healthDiff = mc.player.getHealth() - renderTarget.getHealth();
        if (Math.abs(healthDiff) < 0.1) return "";
        
        if (healthDiff > 0) {
            return "+" + decimalFormat.format(healthDiff);
        } else {
            return decimalFormat.format(healthDiff);
        }
    }

    private Color getDamageColor(PlayerEntity renderTarget, float alpha) {
        double healthDiff = mc.player.getHealth() - renderTarget.getHealth();
        if (healthDiff > 0) {
            return new Color(100, 255, 100, (int)(255 * alpha)); // Green for advantage
        } else {
            return new Color(255, 100, 100, (int)(255 * alpha)); // Red for disadvantage
        }
    }

    private void drawCoolArmor(DrawContext context, PlayerEntity target, float posX, float posY, float scale, float alpha) {
        final float spacing = 18.0f * scale;
        float currentX = posX;

        // Apply alpha to rendering
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        for (int i = 0; i < 4; i++) {
            if (!target.getInventory().armor.get(3 - i).isEmpty()) {
                // Draw armor slot background
                Color slotBg = new Color(35, 35, 45, (int)(150 * alpha));
                DrawUtils.drawRoundedRect(context.getMatrices(), (int)currentX - 1, (int)posY - 1, 
                    (int)(currentX + 16 * scale + 1), (int)(posY + 16 * scale + 1), 2, slotBg);

                context.getMatrices().push();
                context.getMatrices().translate(currentX, posY, 0);
                context.getMatrices().scale(scale, scale, scale);

                context.drawItem(target.getInventory().armor.get(3 - i), 0, 0);
                context.drawItemInSlot(mc.textRenderer, target.getInventory().armor.get(3 - i), 0, 0);

                context.getMatrices().pop();
            }
            currentX += spacing;
        }

        // Restore alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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
            case "Cool" -> {
                hudWidth = 180;
                hudHeight = 75;
            }
            case "Compact" -> {
                hudWidth = 100;
                hudHeight = 55;
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

        // Draw avatar with proper alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 2, currentPosY + 2, headSize);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

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

        // Check if PostProcessing module exists and is enabled
        try {
            PostProcessing postProcessing = Client.INSTANCE.getModuleManager().getModule(PostProcessing.class);
            if (postProcessing != null && postProcessing.isEnabled() && PostProcessing.shouldBlurTargetHud()) {
                Color glowColor = new Color((int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha));
                ShaderUtils.drawGlow(event.getContext().getMatrices(), currentPosX, currentPosY, 3 + 24 + 3 + maxxL + 3, 3 + 24 + 8 + 3, 30, glowColor);
            }
        } catch (Exception e) {
            // PostProcessing module not available, skip glow effect
        }

        Color bgColor = new Color(43, 43, 43, (int)(opacity.getValueInt() * alpha));
        DrawUtils.drawRect(event.getContext().getMatrices(), currentPosX, currentPosY, currentPosX + 3 + 24 + 3 + maxxL + 3, currentPosY + 3 + 24 + 8 + 3, bgColor);

        // Draw avatar with proper alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 3, currentPosY + 3, 24);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

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

        // Draw avatar with proper alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 5, currentPosY + (boxHeight - avatarSize) / 2, avatarSize);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

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

        // Check if PostProcessing module exists and is enabled
        try {
            PostProcessing postProcessing = Client.INSTANCE.getModuleManager().getModule(PostProcessing.class);
            if (postProcessing != null && postProcessing.isEnabled() && PostProcessing.shouldBlurTargetHud()) {
                Color glowColor = new Color((int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha), (int)(opacity.getValueInt() * alpha));
                ShaderUtils.drawGlow(event.getContext().getMatrices(), currentPosX, currentPosY, 3 + 32 + 3 + maxxL, 3 + 32 + 3, 30, glowColor);
            }
        } catch (Exception e) {
            // PostProcessing module not available, skip glow effect
        }

        Color bgColor = new Color(43, 43, 43, (int)(opacity.getValueInt() * alpha));
        DrawUtils.drawRoundedRect(event.getContext().getMatrices(), currentPosX, currentPosY, currentPosX + 3 + 32 + 3 + maxxL + 3, currentPosY + 3 + 32 + 3, 3, bgColor);

        // Draw avatar with proper alpha
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) renderTarget).getSkinTextures(), currentPosX + 3, currentPosY + 3, 32);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

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