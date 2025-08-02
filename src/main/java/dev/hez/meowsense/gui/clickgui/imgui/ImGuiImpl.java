package dev.hez.meowsense.gui.clickgui.imgui;

import dev.hez.meowsense.utils.render.ColorUtils;
import dev.hez.meowsense.utils.render.ThemeUtils;
import imgui.*;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImGuiImpl {

    private final static ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final static ImGuiImplGl3 gl3 = new ImGuiImplGl3();

    private static ImGuiStyle imGuiStyle;

    public static void initialize(final long windowId) {
        ImGui.createContext();
        ImPlot.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        ImFontAtlas fontAtlas = io.getFonts();
        ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setMergeMode(false);
        fontConfig.setPixelSnapH(true);

        String fontPath = "assets/meowsense/fonts/tenacity-bold.ttf";
        float fontSize = 18.0f;

        File tempFontFile = null;
        try (InputStream is = ImGuiImpl.class.getClassLoader().getResourceAsStream(fontPath)) {
            if (is == null) {
                throw new IOException("Failed to find font: " + fontPath);
            }

            tempFontFile = File.createTempFile("tempFont", ".ttf");
            try (FileOutputStream os = new FileOutputStream(tempFontFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            ImFont customFont = fontAtlas.addFontFromFileTTF(tempFontFile.getAbsolutePath(), fontSize, fontConfig);
            if (customFont == null) {
                System.err.println("Failed to load custom font from extracted file.");
            }
        } catch (IOException e) {
            System.err.println("Failed to load custom font: " + e.getMessage());
        } finally {
            if (tempFontFile != null) {
                tempFontFile.deleteOnExit();
            }
        }

        io.setFontGlobalScale(1f);

        io.setConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setConfigFlags(ImGuiConfigFlags.ViewportsEnable);

        glfw.init(windowId, true);
        gl3.init();

        imGuiStyle = ImGui.getStyle();
        imGuiStyle.setWindowRounding(10.0f);
        imGuiStyle.setChildRounding(10.0f);
        imGuiStyle.setFrameRounding(10.0f);
        imGuiStyle.setPopupRounding(10.0f);
        imGuiStyle.setScrollbarRounding(10.0f);
        imGuiStyle.setGrabRounding(10.0f);
        setDarkTheme();
    }

    public static void render(final ImGuiRenderer renderer) {
        setDarkTheme();
        glfw.newFrame();
        ImGui.newFrame();

        renderer.render(ImGui.getIO());

        ImGui.render();
        gl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long pointer = GLFW.glfwGetCurrentContext();

            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();

            GLFW.glfwMakeContextCurrent(pointer);
        }
    }

    private static void setDarkTheme() {
        int alpha = 240;

        Color darkBg = new Color(18, 18, 18, alpha);
        Color darkerBg = new Color(12, 12, 12, alpha);
        Color lightText = new Color(240, 240, 240, 255);
        Color mutedText = new Color(180, 180, 180, 255);
        Color border = new Color(45, 45, 45, 255);

        Color tabBg = new Color(35, 35, 35, 255);
        Color frameBg = new Color(30, 30, 30, 255);
        Color frameHovered = new Color(45, 45, 45, 255);
        Color frameActive = new Color(35, 35, 35, 255);
        Color titleBg = new Color(20, 20, 20, 255);
        Color titleActive = new Color(25, 25, 25, 255);
        Color titleCollapsed = new Color(15, 15, 15, 255);
        Color scrollbarBg = new Color(15, 15, 15, 255);
        Color scrollbarGrab = new Color(50, 50, 50, 255);
        Color scrollbarGrabHovered = new Color(60, 60, 60, 255);
        Color scrollbarGrabActive = new Color(70, 70, 70, 255);
        Color header = new Color(40, 40, 40, 255);

        // Fallback cherryColor (e.g. if not dynamically retrieved from ClickGuiModule)
        Color cherryColor = new Color(160, 50, 50); // Dark red accent

        // Apply ImGui colors
        setColColor(ImGuiCol.Text, lightText);
        setColColor(ImGuiCol.TextDisabled, mutedText);

        setColColor(ImGuiCol.WindowBg, darkBg);
        setColColor(ImGuiCol.ChildBg, darkerBg);
        setColColor(ImGuiCol.PopupBg, darkBg);

        setColColor(ImGuiCol.Border, border);
        setColColor(ImGuiCol.BorderShadow, new Color(0, 0, 0, 0));

        setColColor(ImGuiCol.FrameBg, frameBg);
        setColColor(ImGuiCol.FrameBgHovered, frameHovered);
        setColColor(ImGuiCol.FrameBgActive, frameActive);

        setColColor(ImGuiCol.Button, tabBg);
        setColColor(ImGuiCol.ButtonHovered, cherryColor);
        setColColor(ImGuiCol.ButtonActive, cherryColor);

        setColColor(ImGuiCol.Header, header);
        setColColor(ImGuiCol.HeaderHovered, cherryColor);
        setColColor(ImGuiCol.HeaderActive, cherryColor);

        setColColor(ImGuiCol.TitleBg, titleBg);
        setColColor(ImGuiCol.TitleBgActive, titleActive);
        setColColor(ImGuiCol.TitleBgCollapsed, titleCollapsed);

        setColColor(ImGuiCol.ScrollbarBg, scrollbarBg);
        setColColor(ImGuiCol.ScrollbarGrab, scrollbarGrab);
        setColColor(ImGuiCol.ScrollbarGrabHovered, scrollbarGrabHovered);
        setColColor(ImGuiCol.ScrollbarGrabActive, scrollbarGrabActive);

        setColColor(ImGuiCol.Tab, tabBg);
        setColColor(ImGuiCol.TabHovered, cherryColor);
        setColColor(ImGuiCol.TabActive, tabBg);

        setColColor(ImGuiCol.CheckMark, cherryColor);
        setColColor(ImGuiCol.SliderGrab, cherryColor);
        setColColor(ImGuiCol.SliderGrabActive, cherryColor);

        setColColor(ImGuiCol.TextSelectedBg, new Color(cherryColor.getRed(), cherryColor.getGreen(), cherryColor.getBlue(), 50));
        setColColor(ImGuiCol.NavHighlight, new Color(cherryColor.getRed(), cherryColor.getGreen(), cherryColor.getBlue(), 100));
        setColColor(ImGuiCol.ModalWindowDimBg, new Color(10, 10, 10, 180));
    }

    private static void setColColor(int imGuiCol, Color color) {
        imGuiStyle.setColor(imGuiCol, (float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
    }
}
