package org.unitedlands.registrars.command;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.unitedlands.annotations.UnitedCommand;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.utils.United;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarFile;

public class UnitedCommandRegistrar {

    private static final Map<JavaPlugin, List<UnitedCommandRouting>> registered = new HashMap<>();
    private static final Map<UnitedCommandExecutor, JavaPlugin> executorPlugins = new HashMap<>();

    public static void registerAll(JavaPlugin plugin) {
        var nodes = new LinkedHashMap<Class<?>, UnitedCommandNode>();

        try {
            var url  = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            var path = plugin.getClass().getPackageName().replace('.', '/') + '/';

            try (var jar = new JarFile(new File(url.toURI()))) {
                jar.stream()
                        .filter(entry -> entry.getName().endsWith(".class")
                                && !entry.getName().contains("$")
                                && entry.getName().startsWith(path))
                        .forEach(entry -> collectNode(plugin, entry.getName(), nodes));
            }
        } catch(Throwable e) {
            United.logger().error("Command registration failed for package: " + plugin.getClass().getPackageName());
            United.logger().error(e.getMessage());
        }

        var roots      = buildTree(nodes);
        var commandMap = Bukkit.getServer().getCommandMap();
        var prefix     = plugin.getName().toLowerCase();

        roots.forEach(root -> {
            var cmd = new UnitedCommandRouting(root, plugin);
            commandMap.register(prefix, cmd);
            registered.computeIfAbsent(plugin, k -> new ArrayList<>()).add(cmd);
        });
    }

    private static void collectNode(JavaPlugin plugin, String entryName, Map<Class<?>, UnitedCommandNode> nodes) {
        var className = entryName.replace('/', '.').replace(".class", "");
        Class<?> clazz;

        try {
            clazz = Class.forName(className, false, plugin.getClass().getClassLoader());
        } catch(Throwable e) {
            return;
        }

        var isCmd = clazz.isAnnotationPresent(UnitedCommand.class);
        var isSub = clazz.isAnnotationPresent(UnitedSubCommand.class);
        if (!isCmd && !isSub)
            return;

        if (!UnitedCommandExecutor.class.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            United.logger().error("Command has to implement UnitedCommandExecutor: " + className);
            return;
        }

        try {
            var executor = (UnitedCommandExecutor) clazz.getDeclaredConstructor().newInstance();
            var cmdNode  = getUnitedCommandNode(isCmd, clazz, executor);

            nodes.put(clazz, cmdNode);
            executorPlugins.put(executor, plugin);
        } catch(Exception e) {
            United.logger().error("Could not load command: " + className);
            United.logger().error(e.getMessage());
        }
    }

    private static @NonNull UnitedCommandNode getUnitedCommandNode(boolean isCmd, Class<?> clazz, UnitedCommandExecutor executor) {
        UnitedCommandNode node;

        if (isCmd) {
            var ann = clazz.getAnnotation(UnitedCommand.class);
            node = new UnitedCommandNode(ann.name(), ann.aliases(), ann.description(), ann.usage(), ann.permission(),
                    ann.playerOnly(), ann.cooldown(), ann.cooldownPermission(), executor);
        } else {
            var ann = clazz.getAnnotation(UnitedSubCommand.class);
            node = new UnitedCommandNode(ann.name(), ann.aliases(), ann.description(), ann.usage(), ann.permission(),
                    ann.playerOnly(), ann.catchAll(), ann.cooldown(), ann.cooldownPermission(), executor);
        }

        return node;
    }

    private static List<UnitedCommandNode> buildTree(Map<Class<?>, UnitedCommandNode> nodes) {
        var roots = new ArrayList<UnitedCommandNode>();

        nodes.forEach((clazz, node) -> {
            if (clazz.isAnnotationPresent(UnitedCommand.class)) {
                roots.add(node);
            } else {
                var parent = nodes.get(clazz.getAnnotation(UnitedSubCommand.class).parent());
                if (parent == null) {
                    United.logger().error("Parent command not found for subcommand: " + clazz.getName());
                    return;
                }

                parent.addChild(node);
            }
        });

        return roots;
    }

    public static void unregisterAll(JavaPlugin plugin) {
        var commands = registered.remove(plugin);
        if (commands == null)
            return;

        var commandMap = Bukkit.getServer().getCommandMap();
        commands.forEach(cmd -> cmd.unregister(commandMap));

        executorPlugins.entrySet().removeIf(entry -> entry.getValue() == plugin);
    }

    public static JavaPlugin getPluginForExecutor(UnitedCommandExecutor executor) {
        return executorPlugins.get(executor);
    }

}
