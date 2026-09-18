package org.unitedlands.utils;

import java.util.Collection;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.unitedlands.UnitedLib;
import org.unitedlands.annotations.UnitedCommand;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.config.UnitedLibConfig;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.registrars.messages.UnitedMessagesRegistrar;
import org.unitedlands.services.UnitedLanguageService;

public class UnitedMessenger {

    UnitedMessenger() {}

    // ────────────────────────────────────────────
    //   Message sending
    // ────────────────────────────────────────────

    public void send(Audience target, String path, Object... values) {
        send(target, path, true, values);
    }

    public void send(Audience target, String path, boolean withPrefix, Object... values) {
        if (target == null)
            return;

        target.sendMessage(buildComponent(resolveMessage(target, path), values, withPrefix));
    }

    public void send(Collection<? extends Audience> targets, String path, Object... values) {
        send(targets, path, true, values);
    }

    public void send(Collection<? extends Audience> targets, String path,  boolean withPrefix, Object... values) {
        if (targets == null || targets.isEmpty())
            return;

        for (var target : targets)
            send(target, path, withPrefix, values);
    }

    // ────────────────────────────────────────────
    //   Broadcasting
    // ────────────────────────────────────────────

    public void broadcast(String path, Object... values) {
        broadcast(path, true, values);
    }

    public void broadcast(String path, boolean withPrefix, Object... values) {
        send(Bukkit.getOnlinePlayers(), path, withPrefix, values);
        send(Bukkit.getConsoleSender(), path, withPrefix, values);
    }

    public void broadcast(Audience ignore, String path, Object... values) {
        broadcast(ignore, path, true, values);
    }

    public void broadcast(Audience ignore, String path, boolean withPrefix, Object... values) {
        var players = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != ignore)
                .toList();

        send(players, path, withPrefix, values);

        if (ignore != Bukkit.getConsoleSender())
            send(Bukkit.getConsoleSender(), path, withPrefix, values);
    }

    // ────────────────────────────────────────────
    //   Component sending
    // ────────────────────────────────────────────

    public void send(Audience target, Component component) {
        if (target != null)
            target.sendMessage(component);
    }

    public void send(Collection<? extends Audience> targets, Component component) {
        if (targets != null && !targets.isEmpty())
            Audience.audience(targets).sendMessage(component);
    }

    // ────────────────────────────────────────────
    //   Raw message sending
    // ────────────────────────────────────────────

    public void sendRaw(Audience target, String message, Object... values) {
        sendRaw(target, message, true, values);
    }

    public void sendRaw(Audience target, String message, boolean withPrefix, Object... values) {
        if (target != null)
            target.sendMessage(buildComponent(message, values, withPrefix));
    }

    // ────────────────────────────────────────────
    //   Send utils
    // ────────────────────────────────────────────

    public void sendNoPermission(Audience target) {
        sendFrameworkMessage(target, "no-permission", resolvePrefix());
    }

    public void sendNoPermission(Audience target, JavaPlugin plugin) {
        sendFrameworkMessage(target, "no-permission", plugin != null ? getUnitedPrefix(plugin) : null);
    }

    public void sendPlayerOnly(Audience target) {
        sendFrameworkMessage(target, "player-only", resolvePrefix());
    }

    public void sendPlayerOnly(Audience target, JavaPlugin plugin) {
        sendFrameworkMessage(target, "player-only", plugin != null ? getUnitedPrefix(plugin) : null);
    }

    public void sendPlayerNotFound(Audience target, String playerName) {
        sendFrameworkMessage(target, "player-not-found", resolvePrefix(), playerName);
    }

    public void sendPlayerNotFound(Audience target, String playerName, JavaPlugin plugin) {
        sendFrameworkMessage(target, "player-not-found", plugin != null ? getUnitedPrefix(plugin) : null, playerName);
    }

    // ────────────────────────────────────────────
    //   Raw message get
    // ────────────────────────────────────────────

    public String get(String path, Object... values) {
        return get(null, path, values);
    }

    public String get(Audience target, String path, Object... values) {
        return applyReplacements(resolveMessage(target, path), values);
    }

    // ────────────────────────────────────────────
    //   Explicit-prefix sending
    // ────────────────────────────────────────────

    public void sendRawWithPrefix(Audience target, String message, String rawPrefix, Object... values) {
        if (target != null)
            target.sendMessage(buildComponentRaw(message, values, rawPrefix));
    }

    // ────────────────────────────────────────────
    //   Command usage
    // ────────────────────────────────────────────

    public void sendUsage(Audience target, UnitedCommandExecutor command, JavaPlugin plugin) {
        var clazz = command.getClass();
        var usage = "";

        if (clazz.isAnnotationPresent(UnitedCommand.class))
            usage = clazz.getAnnotation(UnitedCommand.class).usage();
        else if (clazz.isAnnotationPresent(UnitedSubCommand.class))
            usage = clazz.getAnnotation(UnitedSubCommand.class).usage();

        if (usage.isEmpty())
            return;

        var locale  = resolveLocale(target);
        var message = UnitedMessagesRegistrar.resolve(UnitedLib.getInstance(), locale, "usage");
        var prefix  = plugin != null ? getUnitedPrefix(plugin) : null;

        target.sendMessage(buildComponentRaw(message, new Object[]{ usage }, prefix));
    }

    // ────────────────────────────────────────────
    //   Utilities
    // ────────────────────────────────────────────

    public String getUnitedPrefix(JavaPlugin plugin) {
        var messages = UnitedLibConfig.get().messages();
        var name     = plugin.getName().replace("United", "");

        if (name.equalsIgnoreCase("Lands"))
            return messages.prefixUL();

        return messages.prefix().replace("{name}", name);
    }

    public Locale resolveLocale(Audience target) {
        if(!(target instanceof Player player))
            return null;

        var service = Bukkit.getServicesManager().load(UnitedLanguageService.class);
        if (service != null) {
            var locale = service.getLocale(player.getUniqueId());
            if (locale != null)
                return locale;
        }

        return player.locale();
    }

    // ────────────────────────────────────────────
    //   Internal
    // ────────────────────────────────────────────

    private String resolveMessage(Audience target, String path) {
        var plugin = PluginResolver.resolveCallingPlugin();
        var locale = resolveLocale(target);

        return UnitedMessagesRegistrar.resolve(plugin, locale, path);
    }

    private Component buildComponent(String message, Object[] values, boolean withPrefix) {
        return buildComponentRaw(message, values, withPrefix ? resolvePrefix() : null);
    }

    Component buildComponentRaw(String message, Object[] values, String prefix) {
        var text = message;
        if (prefix != null && !prefix.isEmpty())
            text = prefix + text;

        text = applyReplacements(text, values);

        var component = MiniMessage.miniMessage().deserialize(text);
        return applyComponentReplacements(component, values);
    }

    private String applyReplacements(String input, Object[] values) {
        if (values == null || values.length == 0)
            return input;

        var output = input;
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Component)
                continue;

            output = output.replace("{" + (i + 1) + "}", values[i] != null ? String.valueOf(values[i]) : "");
        }

        return output;
    }

    private Component applyComponentReplacements(Component component, Object[] values) {
        if (values == null || values.length == 0)
            return component;

        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Component value) {
                var placeholder = "{" + (i + 1) + "}";
                component = component.replaceText(b -> b.matchLiteral(placeholder).replacement(value));
            }
        }

        return component;
    }


    private String resolvePrefix() {
        var plugin = PluginResolver.resolveCallingPlugin();
        return plugin != null ? getUnitedPrefix(plugin) : null;
    }

    private void sendFrameworkMessage(Audience target, String path, String prefix, Object... values) {
        if (target == null)
            return;

        var locale  = resolveLocale(target);
        var message = UnitedMessagesRegistrar.resolve(UnitedLib.getInstance(), locale, path);

        target.sendMessage(buildComponentRaw(message, values, prefix));
    }

}