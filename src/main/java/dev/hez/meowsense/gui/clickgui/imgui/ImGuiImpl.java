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
        setColColor(ImGuiCol.Text, new Color(255, 255, 255, 255)); // White text
        setColColor(ImGuiCol.TextDisabled, new Color(128, 128, 128, 255)); // Gray disabled text
        setColColor(ImGuiCol.WindowBg, new Color(18, 18, 24, 255)); // Dark background
        setColColor(ImGuiCol.ChildBg, new Color(0, 0, 0, 0)); // Transparent child background
        setColColor(ImGuiCol.PopupBg, new Color(30, 30, 40, 255)); // Dark popup background
        setColColor(ImGuiCol.Border, new Color(80, 80, 100, 255)); // Light purple border
        setColColor(ImGuiCol.BorderShadow, new Color(0, 0, 0, 0)); // No border shadow

        // Frame colors
        setColColor(ImGuiCol.FrameBg, new Color(40, 40, 50, 255)); // Dark frame background
        setColColor(ImGuiCol.FrameBgHovered, new Color(90, 70, 130, 255)); // Purple hovered frame
        setColColor(ImGuiCol.FrameBgActive, new Color(110, 90, 150, 255)); // Purple active frame

        // Title bar colors
        setColColor(ImGuiCol.TitleBg, new Color(50, 40, 70, 255)); // Dark purple title bar
        setColColor(ImGuiCol.TitleBgActive, new Color(70, 50, 90, 255)); // Brighter purple active title bar
        setColColor(ImGuiCol.TitleBgCollapsed, new Color(30, 20, 50, 255)); // Darker collapsed title bar

        // Scrollbar colors
        setColColor(ImGuiCol.ScrollbarBg, new Color(20, 20, 30, 255)); // Dark scrollbar background
        setColColor(ImGuiCol.ScrollbarGrab, new Color(90, 70, 130, 255)); // Purple scrollbar grab
        setColColor(ImGuiCol.ScrollbarGrabHovered, new Color(110, 90, 150, 255)); // Brighter purple hovered grab
        setColColor(ImGuiCol.ScrollbarGrabActive, new Color(130, 110, 170, 255)); // Brightest purple active grab

        // Button colors
        setColColor(ImGuiCol.Button, new Color(90, 70, 130, 255)); // Purple button
        setColColor(ImGuiCol.ButtonHovered, new Color(110, 90, 150, 255)); // Brighter purple hovered button
        setColColor(ImGuiCol.ButtonActive, new Color(130, 110, 170, 255)); // Brightest purple active button

        // Header colors
        setColColor(ImGuiCol.Header, new Color(90, 70, 130, 255)); // Purple header
        setColColor(ImGuiCol.HeaderHovered, new Color(110, 90, 150, 255)); // Brighter purple hovered header
        setColColor(ImGuiCol.HeaderActive, new Color(130, 110, 170, 255)); // Brightest purple active header

        // Separator colors
        setColColor(ImGuiCol.Separator, new Color(80, 80, 100, 255)); // Light purple separator
        setColColor(ImGuiCol.SeparatorHovered, new Color(110, 90, 150, 255)); // Brighter purple hovered separator
        setColColor(ImGuiCol.SeparatorActive, new Color(130, 110, 170, 255)); // Brightest purple active separator

        // Resize grip colors
        setColColor(ImGuiCol.ResizeGrip, new Color(90, 70, 130, 255)); // Purple resize grip
        setColColor(ImGuiCol.ResizeGripHovered, new Color(110, 90, 150, 255)); // Brighter purple hovered grip
        setColColor(ImGuiCol.ResizeGripActive, new Color(130, 110, 170, 255)); // Brightest purple active grip

        // Tab colors
        setColColor(ImGuiCol.Tab, new Color(50, 40, 70, 255)); // Dark purple tab
        setColColor(ImGuiCol.TabHovered, new Color(90, 70, 130, 255)); // Purple hovered tab
        setColColor(ImGuiCol.TabActive, new Color(110, 90, 150, 255)); // Brighter purple active tab
        setColColor(ImGuiCol.TabUnfocused, new Color(30, 20, 50, 255)); // Darker unfocused tab
        setColColor(ImGuiCol.TabUnfocusedActive, new Color(70, 50, 90, 255)); // Brighter unfocused active tab

        // Plot colors
        setColColor(ImGuiCol.PlotLines, new Color(90, 70, 130, 255)); // Purple plot lines
        setColColor(ImGuiCol.PlotLinesHovered, new Color(110, 90, 150, 255)); // Brighter purple hovered plot lines
        setColColor(ImGuiCol.PlotHistogram, new Color(90, 70, 130, 255)); // Purple histogram
        setColColor(ImGuiCol.PlotHistogramHovered, new Color(110, 90, 150, 255)); // Brighter purple hovered histogram

        // Text selection background
        setColColor(ImGuiCol.TextSelectedBg, new Color(90, 70, 130, 100)); // Semi-transparent purple text selection

        // Navigation highlight
        setColColor(ImGuiCol.NavHighlight, new Color(110, 90, 150, 255)); // Brighter purple navigation highlight
    }

    private static void setColColor(int imGuiCol, Color color) {
        imGuiStyle.setColor(imGuiCol, (float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
    }
}
