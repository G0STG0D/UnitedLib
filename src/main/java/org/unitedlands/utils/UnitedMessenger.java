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
        var text = applyReplacements(message, values);
        if (prefix != null && !prefix.isEmpty())
            text = prefix + text;
        return MiniMessage.miniMessage().deserialize(text);
    }

    private String applyReplacements(String input, Object[] values) {
        if (values == null || values.length == 0)
            return input;

        var output = input;
        for (int i = 0; i < values.length; i++)
            output = output.replace("{" + (i + 1) + "}", values[i] != null ? String.valueOf(values[i]) : "");

        return output;
    }

    private String resolvePrefix() {
        var plugin = PluginResolver.resolveCallingPlugin();
        return plugin != null ? getUnitedPrefix(plugin) : null;
    }

}