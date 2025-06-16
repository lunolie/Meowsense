package dev.hez.meowsense.commands.impl;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.commands.Command;
import dev.hez.meowsense.module.Module;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("t");
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length < 1) {
            sendMessage(Formatting.RED + "Usage: .t <module>");
            return;
        }

        String moduleName = commands[0];
        Module module = Client.INSTANCE.getModuleManager().getModuleByName(moduleName);

        if (module == null) {
            sendMessage(Formatting.RED + "Module \"" + moduleName + "\" not found.");
            return;
        }

        module.toggle();
        sendMessage((module.isEnabled() ? Formatting.GREEN : Formatting.RED) +
                "Toggled " + module.getName() + " " +
                (module.isEnabled() ? "on" : "off"));
    }

    @Override
    public String[] getCommands() {
        return new String[] { "<module>" };
    }

    public List<String> autocomplete(String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (Module module : Client.INSTANCE.getModuleManager().getModules()) {
                if (module.getName().toLowerCase().startsWith(input)) {
                    suggestions.add(module.getName());
                }
            }
        }

        return suggestions;
    }
}
