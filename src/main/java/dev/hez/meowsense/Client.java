package dev.hez.meowsense;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
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

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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
    public static String version = "1.0";

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
    }
}