package org.unitedlands.registrars.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface UnitedCommandExecutor {
    void handleCommand(CommandSender sender, String[] args);

    default List<String> handleTab(CommandSender sender, String[] args) {
        return List.of();
    }
}
