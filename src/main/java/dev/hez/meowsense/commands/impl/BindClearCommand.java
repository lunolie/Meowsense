package dev.hez.meowsense.commands.impl;

import dev.hez.meowsense.Client;
import dev.hez.meowsense.commands.Command;
import dev.hez.meowsense.module.Module;

public class BindClearCommand extends Command {
    public BindClearCommand() {
        super("bindclear");
    }

    @Override
    public void execute(String[] commands) {
        for (Module module : Client.INSTANCE.getModuleManager().getModules()) {
            module.setKey(0);
        }
        sendMessage("Reset all binds");
    }
}
