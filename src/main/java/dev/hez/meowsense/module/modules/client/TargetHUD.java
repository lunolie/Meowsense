package dev.hez.meowsense.module.modules.client;

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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.text.DecimalFormat;

public class TargetHUD extends Module {
    public static final ModeSetting targethudmode = new ModeSetting("TargetHUD Mode", "Nexus", "Nexus", "Adjust", "Novo", "Meow");

    public static final BooleanSetting deb = new BooleanSetting("Debug", false);

    public static final NumberSetting opacity = new NumberSetting("BG Opacity", 0, 255, 80, 1);

    public TargetHUD() {
        super("TargetHUD", "Displays information about your targets", 0, ModuleCategory.CLIENT);
        this.addSettings(targethudmode, deb, opacity);
    }

    private PlayerEntity target;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private final MutableAnimation healthBarAnimation = new MutableAnimation(0, 1);

    @Override
    public void onEnable() {
        target = null;
        super.onEnable();
    }

    @EventLink
    public final Listener<EventTickPre> eventTickListener = event -> {
        if (isNull()) {
            return;
        }

        if (!deb.getValue()) {
            if (Client.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled()) {
                target = Client.INSTANCE.getModuleManager().getModule(KillAura.class).target;
            } else {
                target = null;
            }
        } else {
            target = mc.player;
        }
    };

    @EventLink
    public final Listener<EventRender2D> eventRender2DListener = event -> {
        if (target == null || isNull()) {
            return;
        }

        switch (targethudmode.getMode()) {
            case "Nexus" -> drawNexusTargetHud(event);
            case "Adjust" -> drawAdjustTargetHud(event);
            case "Novo" -> drawNovolineTargetHud(event);
            case "Astolfo" -> drawAstolfoTargetHud(event);
        }
    };

    private void drawNovolineTargetHud(EventRender2D event) {
        int height = event.getHeight();
        int width = event.getWidth();

        int startX = width / 2 + 15;
        int startY = height / 2 + 15;

        int maxxL = 100;

        int headSize = 24;

        String name = target.getGameProfile().getName();

        Color backgroundColor = new Color(45, 45, 45);
        Color backgroundColor2 = new Color(21, 21, 21);

        DrawUtils.drawRect(event.getContext().getMatrices(), startX - 1, startY - 1, startX + 2 + headSize + maxxL + 1, startY + 2 + headSize + 2 + 1, backgroundColor2);
        DrawUtils.drawRect(event.getContext().getMatrices(), startX, startY, startX + 2 + headSize + maxxL, startY + 2 + headSize + 2, backgroundColor);

        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) target).getSkinTextures(), startX + 2, startY + 2, headSize);

        event.getContext().drawText(mc.textRenderer, name, startX + 2 + headSize + 2, startY + 4, -1, true);

        int healthbarstartx = startX + 2 + headSize + 2;
        int healthbarstarty = startY + 4 + mc.textRenderer.fontHeight + 2;
        int healthbarendx = startX + 2 + headSize + maxxL - 2;
        int healthbarendy = healthbarstarty + mc.textRenderer.fontHeight + 2;

        double healthN = target.getHealth();
        double maxHealth = target.getMaxHealth();
        double healthPercentage = healthN / maxHealth;

        String healthPercentageText = decimalFormat.format(healthPercentage * 100) + "%";

        DrawUtils.drawRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, healthbarendx, healthbarendy, backgroundColor.darker());

        int filledHealthbarEndX = (int) (healthbarstartx + (healthbarendx - healthbarstartx) * healthPercentage);

        healthBarAnimation.interpolate(filledHealthbarEndX);

        DrawUtils.drawRectWithOutline(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, (int) healthBarAnimation.getValue(), healthbarendy, ThemeUtils.getMainColor(), Color.BLACK);

        int textWidth = mc.textRenderer.getWidth(healthPercentageText);
        int textHeight = mc.textRenderer.fontHeight;

        double rectCenterX = (healthbarstartx + healthbarendx) / 2.0;
        double rectCenterY = (healthbarstarty + healthbarendy) / 2.0;

        double textX = rectCenterX - textWidth / 2.0;
        double textY = rectCenterY - textHeight / 2.0;

        event.getContext().drawText(mc.textRenderer, healthPercentageText, (int) textX, (int) textY + 1, 0xFFFFFFFF, true);
    }

    private void drawAdjustTargetHud(EventRender2D event) {
        int height = event.getHeight();
        int width = event.getWidth();

        int startX = width / 2 + 15;
        int startY = height / 2 + 15;

        String name = target.getGameProfile().getName();

        int maxxL = 100;

        if (PostProcessing.shouldBlurTargetHud()) {
            ShaderUtils.drawGlow(event.getContext().getMatrices(), startX, startY, 3 + 24 + 3 + maxxL + 3, 3 + 24 + 8 + 3, 30, new Color(opacity.getValueInt(), opacity.getValueInt(), opacity.getValueInt()));
        }

        DrawUtils.drawRect(event.getContext().getMatrices(), startX, startY, startX + 3 + 24 + 3 + maxxL + 3, startY + 3 + 24 + 8 + 3, new Color(43, 43, 43, opacity.getValueInt()));

        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) target).getSkinTextures(), startX + 3, startY + 3, 24);

        Color gray = Color.WHITE.darker();
        FontRenderer fontRenderer = Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.VERDANA);
        fontRenderer.drawString(event.getContext().getMatrices(), name, startX + 3 + 24 + 3, startY + 2, gray);

        drawArmor(event.getContext(), target, startX + 3 + 24, startY + 3 + mc.textRenderer.fontHeight + 2, 1);

        int healthbarstartx = startX + 3;
        int healthbarstarty = startY + 3 + 24 + 3;
        int healthbarendx = startX + 3 + 24 + 3 + maxxL;
        int healthbarendy = startY + 3 + 24 + 8;

        DrawUtils.drawRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, healthbarendx, healthbarendy, new Color(0, 0, 0, 80));

        double healthN = target.getHealth();
        double maxHealth = target.getMaxHealth();
        double healthPercentage = healthN / maxHealth;

        int filledHealthbarEndX = (int) (healthbarstartx + (healthbarendx - healthbarstartx) * healthPercentage);

        healthBarAnimation.interpolate(filledHealthbarEndX);

        DrawUtils.drawRectWithOutline(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, (int) healthBarAnimation.getValue(), healthbarendy, ThemeUtils.getMainColor(), Color.DARK_GRAY);

        String sheesh = decimalFormat.format(Math.abs(mc.player.getHealth() - target.getHealth()));
        String healthDiff = mc.player.getHealth() < target.getHealth() ? "-" + sheesh : "+" + sheesh;

        int healthDiffWidth = (int) fontRenderer.getStringWidth(healthDiff);
        int healthDiffHeight = (int) fontRenderer.getStringHeight(healthDiff);

        fontRenderer.drawString(event.getContext().getMatrices(), healthDiff, startX + 3 + 24 + 3 + maxxL - healthDiffWidth, startY + 3 + 24 + 3 - healthDiffHeight, gray);
    }


    private void drawAstolfoTargetHud(EventRender2D event) {
        if (target == null || mc.player == null) return;

        int height = event.getHeight();
        int width = event.getWidth();

        int startX = width / 2 + 15;
        int startY = height / 2 + 15;

        String name = target.getGameProfile().getName();
        String health = decimalFormat.format(target.getHealth()) + " ❤";

        int nameL = mc.textRenderer.getWidth(name);
        int healthLenght = mc.textRenderer.getWidth(health);
        int maxxL = Math.max(nameL, healthLenght);

        // Draw background
        DrawUtils.drawRoundedRect(event.getContext().getMatrices(), startX, startY, startX + 3 + 32 + 3 + maxxL + 3, startY + 3 + 32 + 3, 3, new Color(0, 0, 0, (int) (0.6F * opacity.getValueInt())));

        // Draw health bar background
        int healthbarstartx = startX + 3 + 32 + 3;
        int healthbarstarty = startY + 3 + 2 * mc.textRenderer.fontHeight + 3;
        int healthbarendx = startX + 3 + 32 + 3 + maxxL;
        int healthbarendy = startY + 3 + 32;

        DrawUtils.drawRoundedRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, healthbarendx, healthbarendy, 3, new Color(0, 0, 0, 80));

        // Calculate health percentage
        double healthN = target.getHealth();
        double maxHealth = target.getMaxHealth();
        double healthPercentage = healthN / maxHealth;

        // Calculate filled health bar width
        int filledHealthbarEndX = (int) (healthbarstartx + (healthbarendx - healthbarstartx) * healthPercentage);
        filledHealthbarEndX = Math.clamp(filledHealthbarEndX, healthbarstartx, healthbarendx);

        // Animate health bar
        healthBarAnimation.interpolate(filledHealthbarEndX);

        // Draw filled health bar
        DrawUtils.drawRoundedHorizontalGradientRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, (int) healthBarAnimation.getValue(), healthbarendy, 3, ThemeUtils.getMainColor(), ThemeUtils.getSecondColor());

        // Draw player skin
        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) target).getSkinTextures(), startX + 3, startY + 3, 32);

        // Draw name
        event.getContext().drawText(mc.textRenderer, name, startX + 3 + 32 + 3, startY + 3, -1, true);

        // Draw health text
        float scale = 1.75F;
        MatrixStack matrixStack = event.getContext().getMatrices(); // Now this should work
        matrixStack.push();
        matrixStack.scale(scale, scale, scale);
        event.getContext().drawText(mc.textRenderer, health, (int) ((startX + 3 + 32 + 3) / scale), (int) ((startY + 3 + mc.textRenderer.fontHeight) / scale), ThemeUtils.getMainColor().getRGB(), true);
        matrixStack.pop();
    }
    private void drawNexusTargetHud(EventRender2D event) {
        int height = event.getHeight();
        int width = event.getWidth();

        int startX = width / 2 + 15;
        int startY = height / 2 + 15;

        String name = target.getGameProfile().getName();

        int nameL = mc.textRenderer.getWidth(name);

        String health = decimalFormat.format(target.getHealth()) + " HP" + " | " + getW_L();

        int healthLenght = mc.textRenderer.getWidth(health);

        int maxxL = Math.max(nameL, healthLenght);

        if (PostProcessing.shouldBlurTargetHud()) {
            ShaderUtils.drawGlow(event.getContext().getMatrices(), startX, startY, 3 + 32 + 3 + maxxL, 3 + 32 + 3, 30, new Color(opacity.getValueInt(), opacity.getValueInt(), opacity.getValueInt()));
        }

        DrawUtils.drawRoundedRect(event.getContext().getMatrices(), startX, startY, startX + 3 + 32 + 3 + maxxL + 3, startY + 3 + 32 + 3, 3, new Color(43, 43, 43, opacity.getValueInt()));

        PlayerSkinDrawer.draw(event.getContext(), ((AbstractClientPlayerEntity) target).getSkinTextures(), startX + 3, startY + 3, 32);

        event.getContext().drawText(mc.textRenderer, name, startX + 3 + 32 + 3, startY + 3, -1, true);
        event.getContext().drawText(mc.textRenderer, health, startX + 3 + 32 + 3, startY + 3 + mc.textRenderer.fontHeight, -1, true);

        int healthbarstartx = startX + 3 + 32 + 3;
        int healthbarstarty = startY + 3 + 2 * mc.textRenderer.fontHeight + 3;
        int healthbarendx = startX + 3 + 32 + 3 + maxxL;
        int healthbarendy = startY + 3 + 32;

        DrawUtils.drawRoundedRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, healthbarendx, healthbarendy, 3, new Color(0, 0, 0, 80));

        double healthN = target.getHealth();
        double maxHealth = target.getMaxHealth();
        double healthPercentage = healthN / maxHealth;

        int filledHealthbarEndX = (int) (healthbarstartx + (healthbarendx - healthbarstartx) * healthPercentage);

        filledHealthbarEndX = Math.clamp(filledHealthbarEndX, healthbarstartx, healthbarendx);

        healthBarAnimation.interpolate(filledHealthbarEndX);

        DrawUtils.drawRoundedHorizontalGradientRect(event.getContext().getMatrices(), healthbarstartx, healthbarstarty, (int) healthBarAnimation.getValue(), healthbarendy, 3, ThemeUtils.getMainColor(), ThemeUtils.getSecondColor());
    }

    private String getW_L() {
        if (target.getHealth() < mc.player.getHealth()) {
            return "Winning";
        } else if (target.getHealth() > mc.player.getHealth()) {
            return "Losing";
        } else {
            return "Draw";
        }
    }

    private void drawArmor(DrawContext context, PlayerEntity target, float posX, float posY, float scale) {
        final float spacing = 15.0f * scale;
        float currentX = posX;

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
    }
}
