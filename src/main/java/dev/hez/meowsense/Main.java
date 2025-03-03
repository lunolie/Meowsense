package dev.hez.meowsense;

import net.fabricmc.api.ModInitializer;
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
import net.minecraft.client.MinecraftClient;


public final class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        new Client();
        System.out.println("Loading Meowsense...");
    }
}
