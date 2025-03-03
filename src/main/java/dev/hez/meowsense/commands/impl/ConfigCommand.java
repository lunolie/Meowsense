package dev.hez.meowsense.commands.impl;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.commands.Command;
import dev.hez.meowsense.utils.mc.ChatUtils;
import dev.hez.meowsense.utils.mc.PlayerUtil;

import java.io.File;
import java.util.Objects;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", new String[]{"<save/load/list>", "<configName>"});
    }

    @Override
    public void execute(String[] args) {
        if (args[0].equalsIgnoreCase("save")) {
            Client.INSTANCE.getConfigManager().saveConfig(args[1]);
        }

        if (args[0].equalsIgnoreCase("load")) {
            Client.INSTANCE.getConfigManager().loadConfig(args[1]);
        }

        if (args[0].equalsIgnoreCase("list")) {
            sendMessage("Configs:");

            boolean found = false;
            for (File file : Objects.requireNonNull(Client.INSTANCE.getConfigManager().getConfigDir().listFiles())) {
                if (file.getName().endsWith(".json")) {
                    sendMessage(" - " + file.getName());
                    found = true;
                }
            }
            if (!found) {
                sendMessage("No configs found.");
            }
        }
    }
}
