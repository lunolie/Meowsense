package dev.hez.meowsense;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.hez.meowsense.anticheat.AntiCheatManager;
import dev.hez.meowsense.commands.CommandManager;
import dev.hez.meowsense.config.ConfigManager;
import dev.hez.meowsense.event.EventManager;
import dev.hez.meowsense.module.ModuleManager;
import dev.hez.meowsense.utils.font.FontManager;
import dev.hez.meowsense.utils.mc.DelayUtil;
import dev.hez.meowsense.utils.render.notifications.NotificationManager;
import dev.hez.meowsense.utils.rotation.manager.RotationManager;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;

@Getter
public final class Client {
    public static MinecraftClient mc;
    public static Client INSTANCE;
    public static final Logger LOGGER = LogManager.getLogger("Meowsense");

    private final EventManager eventManager;
    private final RotationManager rotationManager;
    private final ModuleManager moduleManager;

    private final NotificationManager notificationManager;
    private final AntiCheatManager antiCheatManager;

    private final FontManager fontManager;
    private final ConfigManager configManager;
    private final CommandManager commandManager;

    private final DelayUtil delayUtil;
    public static String version = "1.2";

    public Client() {
        INSTANCE = this;
        mc = MinecraftClient.getInstance();

        eventManager = new EventManager();
        notificationManager = new NotificationManager();
        antiCheatManager = new AntiCheatManager();
        rotationManager = new RotationManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        fontManager = new FontManager();
        delayUtil = new DelayUtil();

        eventManager.subscribe(notificationManager);
        eventManager.subscribe(rotationManager);
        eventManager.subscribe(delayUtil);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> setWindowIcon());
    }

    private void setWindowIcon() {
        Window window = mc.getWindow();
        if (window == null) {
            LOGGER.error("Minecraft window is not initialized yet!");
            return;
        }

        try {
            // Load icons
            ByteBuffer[] icons = new ByteBuffer[]{
                    loadIcon("assets/meowsense/icons/icon-16x16.png"),
                    loadIcon("assets/meowsense/icons/icon-32x32.png"),
                    loadIcon("assets/meowsense/icons/icon-64x64.png"),
                    loadIcon("assets/meowsense/icons/icon-128x128.png")
            };

            // Set window icons using GLFW
            try (MemoryStack stack = MemoryStack.stackPush()) {
                GLFWImage.Buffer iconBuffer = GLFWImage.malloc(icons.length, stack);
                for (int i = 0; i < icons.length; i++) {
                    GLFWImage icon = iconBuffer.get(i);
                    int width = (int) Math.sqrt(icons[i].remaining() / 4); // Calculate width from buffer size
                    int height = width; // Assuming square icons
                    icon.set(width, height, icons[i]);
                }

                // Apply the icons to the window
                GLFW.glfwSetWindowIcon(window.getHandle(), iconBuffer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to set window icon: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while setting window icon: " + e.getMessage(), e);
        }
    }

    private ByteBuffer loadIcon(String path) throws IOException {
        // Load the icon from the resource path
        try (InputStream inputStream = Client.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                LOGGER.error("Icon not found at path: " + path);
                throw new IOException("Could not find icon: " + path);
            }

            // Read the image
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                LOGGER.error("Failed to read icon from path: " + path);
                throw new IOException("Invalid or unsupported image format: " + path);
            }

            LOGGER.info("Successfully loaded icon: " + path);

            // Extract pixel data
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            // Convert pixel data to ByteBuffer
            ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                    buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                    buffer.put((byte) (pixel & 0xFF));         // Blue
                    buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
                }
            }
            buffer.flip(); // Prepare buffer for reading
            return buffer;
        } catch (IOException e) {
            LOGGER.error("Failed to load icon from path: " + path, e);
            throw e; // Re-throw the exception for handling in the caller
        }
    }
}