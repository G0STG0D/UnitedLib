package org.unitedlands.registrars.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.annotations.UnitedListener;
import org.unitedlands.utils.Logger;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarFile;

@SuppressWarnings("DuplicatedCode")
public class UnitedListenerRegistrar {

    private static final Map<JavaPlugin, List<Listener>> registered = new HashMap<>();

    public static void registerAll(JavaPlugin plugin) {
        try {
            var url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();

            try (var jar = new JarFile(new File(url.toURI()))) {
                jar.stream()
                        .filter(entry  -> entry.getName().endsWith(".class")
                                && !entry.getName().contains("$")
                                && !entry.getName().startsWith("META-INF/"))
                        .forEach(entry -> tryRegister(plugin, entry.getName()));
            }
        } catch (Exception e) {
            Logger.logError("JAR-Scan failed for package: " + plugin.getClass().getPackageName());
            Logger.logError(e.getMessage());
        }

    }

    private static void tryRegister(JavaPlugin plugin, String entryName) {
        var className = entryName.replace('/', '.').replace(".class", "");

        try {
            var clazz = Class.forName(className, true, plugin.getClass().getClassLoader());

            if (!clazz.isAnnotationPresent(UnitedListener.class)) return;
            if (!Listener.class.isAssignableFrom(clazz)) return;
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) return;

            for (var dep : clazz.getAnnotation(UnitedListener.class).requirePlugins()) {
                var depPlugin = Bukkit.getPluginManager().getPlugin(dep);
                if (depPlugin == null || !depPlugin.isEnabled())
                    return;
            }

            var instance = (Listener) clazz.getDeclaredConstructor().newInstance();
            Bukkit.getPluginManager().registerEvents(instance, plugin);
            registered.computeIfAbsent(plugin, k -> new ArrayList<>()).add(instance);

        } catch (Throwable e) {
            Logger.logError("Could not register listener: " + className);
            Logger.logError(e.getMessage());
        }
    }

    public static void unregisterAll(JavaPlugin plugin) {
        var listeners = registered.remove(plugin);
        if (listeners == null)
            return;

        listeners.forEach(HandlerList::unregisterAll);
    }

}
