package org.unitedlands.registrars.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.unitedlands.UnitedLib;
import org.unitedlands.utils.Messenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class UnitedCommandRouting extends Command {

    private final UnitedCommandNode root;
    private final String prefix;

    UnitedCommandRouting(UnitedCommandNode root, JavaPlugin plugin) {
        super(root.name, root.description, root.usage, List.of(root.aliases));

        this.root   = root;
        this.prefix = Messenger.getUnitedPrefix(plugin);

        if (!root.permission.isEmpty())
            setPermission(root.permission);
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NotNull String label, String @NonNull [] args) {
        route(sender, label, root, args, 0);
        return true;
    }

    private void route(CommandSender sender, String label, UnitedCommandNode node, String[] args, int depth) {
        var config = UnitedLib.getInstance().getConfig();

        if (!node.permission.isEmpty() && !sender.hasPermission(node.permission)) {
            Messenger.sendMessage(sender, config.getString("messages.no-permission"), null, prefix);
            return;
        }

        if (node.playerOnly && !(sender instanceof Player)) {
            Messenger.sendMessage(sender, config.getString("messages.player-only"), null, prefix);
            return;
        }

        if (node.cooldown > 0 && sender instanceof Player player && node.isOnCooldown(player.getUniqueId()) && !player.hasPermission(node.cooldownPermission)) {
            Messenger.sendMessage(sender, config.getString("messages.cooldown"), Map.of("time", String.valueOf(node.remainingCooldown(player.getUniqueId()))), prefix);
            return;
        }

        if (node.catchAll) {
            applyCooldownIfNeeded(node, sender);
            node.executor.handleCommand(sender, Arrays.copyOfRange(args, depth, args.length));
            return;
        }

        if (depth < args.length) {
            var child = node.find(args[depth]);
            if (child != null) {
                route(sender, label, child, args, depth + 1);
                return;
            }

            if (node.catchAllChild != null) {
                route(sender, label, node.catchAllChild, args, depth);
                return;
            }
            
            if (!node.childNames().isEmpty() && !node.usage.isEmpty()) {
                Messenger.sendMessage(sender, config.getString("messages.usage"), Map.of("usage", node.usage), prefix);
                return;
            }
        }

        if (node.catchAllChild != null && !node.usage.isEmpty()) {
            Messenger.sendMessage(sender, config.getString("messages.usage"), Map.of("usage", node.usage), prefix);
            return;
        }

        applyCooldownIfNeeded(node, sender);
        node.executor.handleCommand(sender, Arrays.copyOfRange(args, depth, args.length));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String @NotNull [] args) throws IllegalArgumentException {
        return routeTab(sender, alias, root, args, 0);
    }

    private List<String> routeTab(CommandSender sender, String alias, UnitedCommandNode node, String[] args, int depth) {
        if (depth >= args.length)
            return List.of();

        var current = args[depth].toLowerCase();

        if (node.catchAll) {
            var custom = node.executor.handleTab(sender, Arrays.copyOfRange(args, depth, args.length));
            if (custom == null)
                return List.of();

            var typing = args[args.length - 1].toLowerCase();
            return custom.stream()
                    .filter(sug -> sug.toLowerCase().startsWith(typing))
                    .toList();
        }

        if (depth == args.length - 1) {
            var completions = node.childNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (node.catchAllChild != null) {
                var suggestions = node.catchAllChild.executor.handleTab(sender, new String[]{ args[depth] });
                if (suggestions != null)
                    suggestions.stream()
                            .filter(sug -> sug.toLowerCase().startsWith(current))
                            .forEach(completions::add);
            }

            var custom = node.executor.handleTab(sender, Arrays.copyOfRange(args, depth, args.length));
            if (custom != null)
                custom.stream()
                        .filter(sug -> sug.toLowerCase().startsWith(current))
                        .forEach(completions::add);

            return completions;
        }

        var child = node.find(args[depth]);
        if (child != null)
            return routeTab(sender, alias, child, args, depth + 1);

        if (node.catchAllChild != null)
            return routeTab(sender, alias, node.catchAllChild, args, depth);

        return node.executor.handleTab(sender, Arrays.copyOfRange(args, depth, args.length));
    }

    private void applyCooldownIfNeeded(UnitedCommandNode node, CommandSender sender) {
        if (node.cooldown > 0 && sender instanceof Player player && !player.hasPermission(node.cooldownPermission))
            node.applyCooldown(player.getUniqueId());
    }

}
