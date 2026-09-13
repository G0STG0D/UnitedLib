package org.unitedlands.registrars.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.unitedlands.UnitedLib;
import org.unitedlands.registrars.messages.UnitedMessagesRegistrar;
import org.unitedlands.utils.United;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class UnitedCommandRouting extends Command {

    private final UnitedCommandNode root;
    private final String prefix;

    UnitedCommandRouting(UnitedCommandNode root, JavaPlugin plugin) {
        super(root.name, root.description, root.usage, List.of(root.aliases));

        this.root   = root;
        this.prefix = United.messenger().getUnitedPrefix(plugin);

        if (!root.permission.isEmpty())
            setPermission(root.permission);
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NotNull String label, String @NonNull [] args) {
        route(sender, label, root, args, 0);
        return true;
    }

    private void route(CommandSender sender, String label, UnitedCommandNode node, String[] args, int depth) {

        if (!node.permission.isEmpty() && !sender.hasPermission(node.permission)) {
            sendFrameworkMessage(sender, "no-permission");
            return;
        }

        if (node.playerOnly && !(sender instanceof Player)) {
            sendFrameworkMessage(sender, "player-only");
            return;
        }

        if (node.cooldown > 0 && sender instanceof Player player && node.isOnCooldown(player.getUniqueId()) && !player.hasPermission(node.cooldownPermission)) {
            sendFrameworkMessage(sender, "cooldown", node.remainingCooldown(player.getUniqueId()));
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
                sendFrameworkMessage(sender, "usage", node.usage);
                return;
            }
        }

        if (node.catchAllChild != null && !node.usage.isEmpty()) {
            sendFrameworkMessage(sender, "usage", node.usage);
            return;
        }

        applyCooldownIfNeeded(node, sender);
        node.executor.handleCommand(sender, Arrays.copyOfRange(args, depth, args.length));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String @NotNull [] args) throws IllegalArgumentException {
        return routeTab(sender, alias, root, args, 0);
    }

    private void sendFrameworkMessage(CommandSender sender, String path, Object... values) {
        var locale  = United.messenger().resolveLocale(sender);
        var message = UnitedMessagesRegistrar.resolve(UnitedLib.getInstance(), locale, path);
        United.messenger().sendRawWithPrefix(sender, message, prefix, values);
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
