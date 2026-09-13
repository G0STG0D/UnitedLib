package org.unitedlands.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@Deprecated(forRemoval = true)
public class Messenger {

    private Messenger() {}

    @Deprecated(forRemoval = true)
    public static void send(Collection<? extends Audience> targets, List<String> lines, Map<String, String> replacements, String prefix) {
        if (targets == null || targets.isEmpty())
            return;
        United.messenger().sendRawWithPrefix(Audience.audience(targets), resolve(String.join("\n", lines), replacements), prefix);
    }

    @Deprecated(forRemoval = true)
    public static void send(Collection<? extends Audience> targets, Component messageComponent) {
        if (targets != null && !targets.isEmpty())
            Audience.audience(targets).sendMessage(messageComponent);
    }

    @Deprecated(forRemoval = true)
    public static void send(Audience target, Component messageComponent) {
        if (target != null)
            target.sendMessage(messageComponent);
    }

    @Deprecated(forRemoval = true)
    public static Component getMessage(String line) {
        return United.messenger().buildComponentRaw(line, null, null);
    }

    @Deprecated(forRemoval = true)
    public static Component getMessage(String line, Map<String, String> replacements) {
        return United.messenger().buildComponentRaw(resolve(line, replacements), null, null);
    }

    @Deprecated(forRemoval = true)
    public static Component getMessage(String line, Map<String, String> replacements, String prefix) {
        return United.messenger().buildComponentRaw(resolve(line, replacements), null, prefix);
    }

    @Deprecated(forRemoval = true)
    public static Component getMessage(List<String> lines) {
        return United.messenger().buildComponentRaw(String.join("\n", lines), null, null);
    }

    @Deprecated(forRemoval = true)
    public static Component getMessage(List<String> lines, Map<String, String> replacements) {
        return United.messenger().buildComponentRaw(resolve(String.join("\n", lines), replacements), null, null);
    }

    @Deprecated(forRemoval = true)
    public static Component getMessage(List<String> lines, Map<String, String> replacements, String prefix) {
        return United.messenger().buildComponentRaw(resolve(String.join("\n", lines), replacements), null, prefix);
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Audience target, String message) {
        United.messenger().sendRaw(target, message);
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Audience target, String message, Map<String, String> replacements) {
        United.messenger().sendRaw(target, resolve(message, replacements));
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Audience target, String message, Map<String, String> replacements, String prefix) {
        United.messenger().sendRawWithPrefix(target, resolve(message, replacements), prefix);
    }

    @Deprecated(forRemoval = true)
    public static void sendUsage(Audience target, UnitedCommandExecutor command, JavaPlugin plugin) {
        United.messenger().sendUsage(target, command, plugin);
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Collection<? extends Audience> targets, String message) {
        if (targets != null && !targets.isEmpty())
            United.messenger().sendRaw(Audience.audience(targets), message);
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Collection<? extends Audience> targets, String message, Map<String, String> replacements) {
        if (targets != null && !targets.isEmpty())
            United.messenger().sendRaw(Audience.audience(targets), resolve(message, replacements));
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Collection<? extends Audience> targets, String message, Map<String, String> replacements, String prefix) {
        if (targets != null && !targets.isEmpty())
            United.messenger().sendRawWithPrefix(Audience.audience(targets), resolve(message, replacements), prefix);
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Audience target, List<String> lines) {
        United.messenger().sendRaw(target, String.join("\n", lines));
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Audience target, List<String> lines, Map<String, String> replacements) {
        United.messenger().sendRaw(target, resolve(String.join("\n", lines), replacements));
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Audience target, List<String> lines, Map<String, String> replacements, String prefix) {
        United.messenger().sendRawWithPrefix(target, resolve(String.join("\n", lines), replacements), prefix);
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Collection<? extends Audience> targets, List<String> lines) {
        if (targets != null && !targets.isEmpty())
            United.messenger().sendRaw(Audience.audience(targets), String.join("\n", lines));
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Collection<? extends Audience> targets, List<String> lines, Map<String, String> replacements) {
        if (targets != null && !targets.isEmpty())
            United.messenger().sendRaw(Audience.audience(targets), resolve(String.join("\n", lines), replacements));
    }

    @Deprecated(forRemoval = true)
    public static void sendMessage(Collection<? extends Audience> targets, List<String> lines, Map<String, String> replacements, String prefix) {
        if (targets != null && !targets.isEmpty())
            United.messenger().sendRawWithPrefix(Audience.audience(targets), resolve(String.join("\n", lines), replacements), prefix);
    }

    @Deprecated(forRemoval = true)
    public static String getUnitedPrefix(JavaPlugin plugin) {
        return United.messenger().getUnitedPrefix(plugin);
    }

    private static String resolve(String input, Map<String, String> replacements) {
        if (replacements == null || replacements.isEmpty())
            return input;

        var output = input;
        for (var entry : replacements.entrySet())
            if (entry.getValue() != null)
                output = output.replace("{" + entry.getKey() + "}", entry.getValue());

        return output;
    }

}