package dev.hez.meowsense.module.modules.client;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.event.bus.Listener;
import dev.hez.meowsense.event.bus.annotations.EventLink;
import dev.hez.meowsense.event.impl.render.EventRender2D;
import dev.hez.meowsense.module.Module;
import dev.hez.meowsense.module.ModuleCategory;
import dev.hez.meowsense.module.setting.impl.BooleanSetting;
import dev.hez.meowsense.module.setting.impl.ModeSetting;
import dev.hez.meowsense.module.setting.impl.NumberSetting;
import dev.hez.meowsense.module.setting.impl.StringSetting;
import dev.hez.meowsense.utils.font.FontManager;
import dev.hez.meowsense.utils.font.fonts.FontRenderer;
import dev.hez.meowsense.utils.mc.ChatFormatting;
import dev.hez.meowsense.utils.mc.PlayerUtil;
import dev.hez.meowsense.utils.render.DrawUtils;
import dev.hez.meowsense.utils.render.ThemeUtils;
import dev.hez.meowsense.utils.render.shaders.ShaderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.hez.meowsense.utils.render.ThemeUtils.getThemeColor;

public class Interface extends Module {
    public static final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    public static final ModeSetting watermarkMode = new ModeSetting("Watermark Mode", "Simple", "Simple", "Gamesense", "Maxwell");
    public static final StringSetting watermarkText = new StringSetting("Watermark Text", "Meow");
    public static final ModeSetting watermarkSimpleFontMode = new ModeSetting("Watermark Font", "MC", "MC", "Product Sans Regular", "Product Sans Medium", "Product Sans Bold", "Verdana", "SFUI", "Tenacity", "Hack");
    public static final BooleanSetting arrayList = new BooleanSetting("ArrayList", true);
    public static final ModeSetting colorMode = new ModeSetting("Color Mode", "Astolfo", "Astolfo", "Theme");
    public static final ModeSetting fontMode = new ModeSetting("ArrayList Font", "MC", "MC", "Product Sans Regular", "Product Sans Medium", "Product Sans Bold", "Verdana", "SFUI", "Tenacity", "Hack");
    public static final ModeSetting suffixMode = new ModeSetting("Suffix", "Space", "Space", "-", ">", "[ ]");
    public static final BooleanSetting hideVisuals = new BooleanSetting("Hide visuals", true);
    public static final BooleanSetting lowercase = new BooleanSetting("Lowercase", false);
    public static final ModeSetting backBarMode = new ModeSetting("Backbar Mode", "None", "None", "Full", "Rise");
    public static final NumberSetting padding = new NumberSetting("Offset", 0, 40, 5, 1);
    public static final NumberSetting opacity = new NumberSetting("BG Opacity", 0, 255, 80, 1);
    public static final BooleanSetting info = new BooleanSetting("Info", true);
    public static final BooleanSetting bpsCounter = new BooleanSetting("BPS", true);
    public static final BooleanSetting fpsCounter = new BooleanSetting("FPS", true);

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private final List<Identifier> maxwellFrames = new ArrayList<>();
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DELAY = 100;

    public Interface() {
        super("Interface", "Clients HUD", 0, ModuleCategory.CLIENT);
        addSettings(watermark, watermarkMode, watermarkText, watermarkSimpleFontMode, arrayList, colorMode, fontMode, suffixMode, hideVisuals, lowercase, backBarMode, padding, opacity, info, bpsCounter, fpsCounter);
        watermarkMode.addDependency(watermark, true);
        watermarkText.addDependency(watermark, true);
        watermarkSimpleFontMode.addDependency(watermarkMode, "Simple");
        colorMode.addDependency(arrayList, true);
        fontMode.addDependency(arrayList, true);
        suffixMode.addDependency(arrayList, true);
        hideVisuals.addDependency(arrayList, true);
        lowercase.addDependency(arrayList, true);
        backBarMode.addDependency(arrayList, true);
        padding.addDependency(arrayList, true);
        opacity.addDependency(arrayList, true);
        bpsCounter.addDependency(info, true);
        fpsCounter.addDependency(info, true);

        for (int i = 1; i <= 189; i++) {
            maxwellFrames.add(Identifier.of("meowsense", "cha/watermark/frame" + String.format("%03d", i) + ".png"));
        }
    }

    @EventLink
    public final Listener<EventRender2D> eventRender2DListener = event -> {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        if (watermark.getValue()) {
            switch (watermarkMode.getMode()) {
                case "Simple":
                    if (!watermarkText.getValue().isEmpty()) {
                        String watermark = watermarkText.getValue();
                        char firstCharacter = watermark.charAt(0);
                        String restOfString = watermark.substring(1);
                        String totalWatermarkText = firstCharacter + ChatFormatting.WHITE + restOfString;
                        switch (watermarkSimpleFontMode.getMode()) {
                            case "MC":
                                event.getContext().drawText(mc.textRenderer, totalWatermarkText, 3, 3, ThemeUtils.getMainColor().getRGB(), true);
                                break;
                            default:
                                getCustomFontRenderer(watermarkSimpleFontMode.getMode()).drawString(event.getContext().getMatrices(), totalWatermarkText, 3, 3, ThemeUtils.getMainColor());
                                break;
                        }
                    }
                    break;

                case "Gamesense":
                    String text = "§f" + watermarkText.getValue() + "§rsense §8| §f " + ("user") + "§7 (" + ("1.1") + ") §8 | §f " + getIP();
                    int padding = 2;
                    int offsetX = 4;
                    int offsetY = 4;
                    int textWidth = (int) Client.INSTANCE.getFontManager().getSize(9, FontManager.Type.PRODUCT_SANS_MEDIUM).getStringWidth(text);
                    int textHeight = (int) Client.INSTANCE.getFontManager().getSize(9, FontManager.Type.PRODUCT_SANS_MEDIUM).getStringHeight(text);
                    int backgroundWidth = textWidth + padding * 2;
                    int backgroundHeight = textHeight + padding * 2;
                    event.getContext().fill(offsetX - padding - 1, offsetY - padding - 1, offsetX + backgroundWidth + 1, offsetY + backgroundHeight + 1, new Color(96, 96, 96).getRGB());
                    if (PostProcessing.shouldBlurWaterMark()) {
                        ShaderUtils.drawGradientBlur(event.getContext().getMatrices(), offsetX - padding - 2, offsetY - padding - 2, offsetX + backgroundWidth - (offsetX - padding) + 2, offsetY + backgroundHeight - (offsetY - padding) + 2, 30, ThemeUtils.getMainColor(), ThemeUtils.getSecondColor(), ThemeUtils.getSecondColor(), ThemeUtils.getMainColor());
                    }
                    event.getContext().fill(offsetX - padding, offsetY - padding, offsetX + backgroundWidth, offsetY + backgroundHeight, new Color(25, 25, 25).getRGB());
                    DrawUtils.drawHorizontalGradientRect(event.getContext().getMatrices(), offsetX - padding + 1, offsetY + backgroundHeight - 3, offsetX + backgroundWidth - 1, offsetY + backgroundHeight - 1, ThemeUtils.getMainColor(), ThemeUtils.getSecondColor());
                    Client.INSTANCE.getFontManager().getSize(9, FontManager.Type.PRODUCT_SANS_MEDIUM).drawString(event.getContext().getMatrices(), text, offsetX, offsetY, ThemeUtils.getMainColor());
                    break;

                case "Maxwell":
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastFrameTime >= FRAME_DELAY) {
                        currentFrame = (currentFrame + 1) % maxwellFrames.size();
                        lastFrameTime = currentTime;
                    }
                    Identifier currentFrameTexture = maxwellFrames.get(currentFrame);
                    int imageWidth = 100;
                    int imageHeight = 100;
                    int x = 3;
                    int y = 3;
                    DrawUtils.drawImage(event.getContext().getMatrices(), currentFrameTexture, x, y, imageWidth, imageHeight);
                    break;
            }
        }

        if (arrayList.getValue()) {
            List<Module> enabledModules;
            switch (fontMode.getMode()) {
                case "MC":
                    enabledModules = Client.INSTANCE.getModuleManager().getEnabledModules().stream()
                            .sorted(Comparator.comparingDouble(ri -> -mc.textRenderer.getWidth(getFullName(ri))))
                            .collect(Collectors.toList());
                    break;
                default:
                    enabledModules = Client.INSTANCE.getModuleManager().getEnabledModules().stream()
                            .sorted(Comparator.comparingDouble(ri -> -getCustomFontRenderer(fontMode.getMode()).getStringWidth(getFullName(ri))))
                            .collect(Collectors.toList());
                    break;
            }

            int i = this.padding.getValueInt();
            int totalWidth = event.getWidth();

            for (Module m : enabledModules) {
                if (hideVisuals.getValue() && m.getModuleCategory() == ModuleCategory.RENDER) continue;

                int color = switch (colorMode.getMode()) {
                    case "Astolfo" -> getAstolfo(i * 3);
                    case "Theme" -> getThemeColor(i).getRGB();
                    default -> 0;
                };

                int moduleWidth;
                int moduleHeight;

                switch (fontMode.getMode()) {
                    case "MC":
                        moduleWidth = mc.textRenderer.getWidth(getFullName(m));
                        moduleHeight = mc.textRenderer.fontHeight;
                        break;
                    default:
                        moduleWidth = (int) getCustomFontRenderer(fontMode.getMode()).getStringWidth(getFullName(m));
                        moduleHeight = (int) getCustomFontRenderer(fontMode.getMode()).getStringHeight(getFullName(m));
                        break;
                }

                if (PostProcessing.shouldBlurArrayList()) {
                    ShaderUtils.drawGlow(event.getContext().getMatrices(), totalWidth - moduleWidth - this.padding.getValueInt() - 2, i - 2, totalWidth - this.padding.getValueInt() + 2 - (totalWidth - moduleWidth - this.padding.getValueInt() - 2), i + moduleHeight + 1 - (i - 1), 30, new Color(color));
                }

                i += moduleHeight + 3;
            }

            i = this.padding.getValueInt();

            for (Module m : enabledModules) {
                if (hideVisuals.getValue() && m.getModuleCategory() == ModuleCategory.RENDER) continue;

                int color = switch (colorMode.getMode()) {
                    case "Astolfo" -> getAstolfo(i * 3);
                    case "Theme" -> getThemeColor(i).getRGB();
                    default -> 0;
                };

                int moduleWidth;
                int moduleHeight;

                switch (fontMode.getMode()) {
                    case "MC":
                        moduleWidth = mc.textRenderer.getWidth(getFullName(m));
                        moduleHeight = mc.textRenderer.fontHeight;
                        break;
                    default:
                        moduleWidth = (int) getCustomFontRenderer(fontMode.getMode()).getStringWidth(getFullName(m));
                        moduleHeight = (int) getCustomFontRenderer(fontMode.getMode()).getStringHeight(getFullName(m));
                        break;
                }

                event.getContext().fill(totalWidth - moduleWidth - this.padding.getValueInt() - 2, i - 2, totalWidth - this.padding.getValueInt() + 2, i + moduleHeight + 1, new Color(0, 0, 0, this.opacity.getValueInt()).getRGB());

                switch (backBarMode.getMode()) {
                    case "Full":
                        event.getContext().fill(totalWidth - this.padding.getValueInt() + 3, i - 2, totalWidth - this.padding.getValueInt() + 5, i + moduleHeight + 1, color);
                        break;
                    case "Rise":
                        event.getContext().fill(totalWidth - this.padding.getValueInt() + 3, i - 1, totalWidth - this.padding.getValueInt() + 5, i + moduleHeight, color);
                        break;
                }

                switch (fontMode.getMode()) {
                    case "MC":
                        event.getContext().drawText(mc.textRenderer, getFullName(m), totalWidth - moduleWidth - this.padding.getValueInt(), i, color, true);
                        break;
                    default:
                        getCustomFontRenderer(fontMode.getMode()).drawString(event.getContext().getMatrices(), getFullName(m), totalWidth - moduleWidth - this.padding.getValueInt(), i, new Color(color));
                        break;
                }
                i += moduleHeight + 3;
            }
        }

        if (info.getValue()) {
            String bps = String.valueOf(decimalFormat.format(Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * 20.0F * PlayerUtil.timer()));
            String fps = String.valueOf(MinecraftClient.getInstance().getCurrentFps());
            int x = 10;
            int lineHeight = mc.textRenderer.fontHeight;
            int y = event.getHeight() - (fpsCounter.getValue() && bpsCounter.getValue() ? 2 * lineHeight : lineHeight) - 10;

            if (fpsCounter.getValue()) {
                int color = switch (colorMode.getMode()) {
                    case "Astolfo" -> getAstolfo(3);
                    case "Theme" -> getThemeColor(1).getRGB();
                    default -> 0;
                };
                event.getContext().drawText(mc.textRenderer, "FPS: " + fps, x, y, color, true);
                y -= lineHeight;
            }

            if (bpsCounter.getValue()) {
                int color = switch (colorMode.getMode()) {
                    case "Astolfo" -> getAstolfo(6);
                    case "Theme" -> getThemeColor(2).getRGB();
                    default -> 0;
                };
                event.getContext().drawText(mc.textRenderer, "BPS: " + bps, x, y, color, true);
            }
        }
    };

    private FontRenderer getCustomFontRenderer(String name) {
        return switch (name) {
            case "Product Sans Regular" -> Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.PRODUCT_SANS_REGULAR);
            case "Product Sans Medium" -> Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.PRODUCT_SANS_MEDIUM);
            case "Product Sans Bold" -> Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.PRODUCT_SANS_BOLD);
            case "SFUI" -> Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.SFUI);
            case "Verdana" -> Client.INSTANCE.getFontManager().getSize(10, FontManager.Type.VERDANA);
            case "Tenacity" -> Client.INSTANCE.getFontManager().getSize(9, FontManager.Type.Tenacity);
            case "Hack" -> Client.INSTANCE.getFontManager().getSize(9, FontManager.Type.Hack);
            default -> null;
        };
    }

    private String getIP() {
        if (mc.world == null) return "NULL";
        if (mc.isInSingleplayer()) return "Singleplayer";
        String ip = mc.getCurrentServerEntry().address;
        return ip.contains("liquidproxy") ? "meow" : ip;
    }

    private String getFullName(Module m) {
        if (lowercase.getValue()) {
            if (m.getSuffix() == null) return m.getName().toLowerCase();
            switch (suffixMode.getMode()) {
                case "Space": return (m.getName() + " " + ChatFormatting.GRAY + m.getSuffix()).toLowerCase();
                case "-": return (m.getName() + ChatFormatting.GRAY + " - " + m.getSuffix()).toLowerCase();
                case ">": return (m.getName() + ChatFormatting.GRAY + " > " + m.getSuffix()).toLowerCase();
                case "[ ]": return (m.getName() + ChatFormatting.DARK_GRAY + " [" + ChatFormatting.GRAY + m.getSuffix() + ChatFormatting.DARK_GRAY + "]").toLowerCase();
            }
        } else {
            if (m.getSuffix() == null) return m.getName();
            switch (suffixMode.getMode()) {
                case "Space": return m.getName() + " " + ChatFormatting.GRAY + m.getSuffix();
                case "-": return m.getName() + ChatFormatting.GRAY + " - " + m.getSuffix();
                case ">": return m.getName() + ChatFormatting.GRAY + " > " + m.getSuffix();
                case "[ ]": return m.getName() + ChatFormatting.DARK_GRAY + " [" + ChatFormatting.GRAY + m.getSuffix() + ChatFormatting.DARK_GRAY + "]";
            }
        }
        return "";
    }

    public static int getAstolfo(int offset) {
        int i = (int) ((System.currentTimeMillis() / 11 + offset) % 360);
        i = (i > 180 ? 360 - i : i) + 180;
        return Color.HSBtoRGB(i / 360f, 0.55f, 1f);
    }
}