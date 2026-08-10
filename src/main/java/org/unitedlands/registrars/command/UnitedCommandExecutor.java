package org.unitedlands.registrars.command;

import org.bukkit.command.CommandSender;
import org.unitedlands.utils.Messenger;

import java.util.List;

public interface UnitedCommandExecutor {

    void handleCommand(CommandSender sender, String[] args);

    default List<String> handleTab(CommandSender sender, String[] args) {
        return List.of();
    }

    default void sendUsage(CommandSender sender) {
        var plugin = UnitedCommandRegistrar.getPluginForExecutor(this);
        Messenger.sendUsage(sender, this, plugin);
    }

}
