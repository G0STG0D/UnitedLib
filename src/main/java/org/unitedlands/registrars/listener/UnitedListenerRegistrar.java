package org.unitedlands.registrars.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.annotations.UnitedListener;
import org.unitedlands.utils.United;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarFile;

@SuppressWarnings("DuplicatedCode")
public class UnitedListenerRegistrar {

    private static final Map<JavaPlugin, List<Listener>> registered = new HashMap<>();

    public static void registerAll(JavaPlugin plugin) {
        try {
            var url  = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            var path = plugin.getClass().getPackageName().replace('.', '/') + '/';

            try (var jar = new JarFile(new File(url.toURI()))) {
                jar.stream()
                        .filter(entry  -> entry.getName().endsWith(".class")
                                && !entry.getName().contains("$")
                                && entry.getName().startsWith(path))
                        .forEach(entry -> tryRegister(plugin, entry.getName()));
            }
        } catch (Exception e) {
            United.logger().error("JAR-Scan failed for package: " + plugin.getClass().getPackageName());
            United.logger().error(e.getMessage());
        }

    }

    private static void tryRegister(JavaPlugin plugin, String entryName) {
        var className = entryName.replace('/', '.').replace(".class", "");
        Class<?> clazz;

        try {
            clazz = Class.forName(className, false, plugin.getClass().getClassLoader());
        } catch (Throwable e) {
            return;
        }

        if (!clazz.isAnnotationPresent(UnitedListener.class)) return;
        if (!Listener.class.isAssignableFrom(clazz)) return;
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) return;

        try {
            for (var dep : clazz.getAnnotation(UnitedListener.class).requirePlugins()) {
                var depPlugin = Bukkit.getPluginManager().getPlugin(dep);
                if (depPlugin == null || !depPlugin.isEnabled())
                    return;
            }

            var instance = (Listener) clazz.getDeclaredConstructor().newInstance();
            Bukkit.getPluginManager().registerEvents(instance, plugin);
            registered.computeIfAbsent(plugin, k -> new ArrayList<>()).add(instance);

        } catch (Throwable e) {
            United.logger().error("Could not register listener: " + className);
            United.logger().error(e.getMessage());
        }
    }

    public static void unregisterAll(JavaPlugin plugin) {
        var listeners = registered.remove(plugin);
        if (listeners == null)
            return;

        listeners.forEach(HandlerList::unregisterAll);
    }

}
