package org.unitedlands.registrars.command;

import org.bukkit.command.CommandSender;
import org.unitedlands.utils.United;

import java.util.List;

public interface UnitedCommandExecutor {

    void handleCommand(CommandSender sender, String[] args);

    default List<String> handleTab(CommandSender sender, String[] args) {
        return List.of();
    }

    default void sendUsage(CommandSender sender) {
        United.messenger().sendUsage(sender, this, UnitedCommandRegistrar.getPluginForExecutor(this));
    }

    default void sendNoPermission(CommandSender sender) {
        United.messenger().sendNoPermission(sender, UnitedCommandRegistrar.getPluginForExecutor(this));
    }

    default void sendPlayerOnly(CommandSender sender) {
        United.messenger().sendPlayerOnly(sender, UnitedCommandRegistrar.getPluginForExecutor(this));
    }

    default void sendPlayerNotFound(CommandSender sender, String playerName) {
        United.messenger().sendPlayerNotFound(sender, playerName, UnitedCommandRegistrar.getPluginForExecutor(this));
    }

}
