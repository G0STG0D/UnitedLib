package org.unitedlands.registrars.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.annotations.UnitedConfig;
import org.unitedlands.utils.United;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.jar.JarFile;

@SuppressWarnings("DuplicatedCode")
public class UnitedConfigRegistrar {

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
        } catch(Exception e) {
            United.logger().error("Config scan failed for " + plugin.getName() + ": " + e.getMessage());
        }
    }

    private static void tryRegister(JavaPlugin plugin, String entryName) {
        var className = entryName.replace('/', '.').replace(".class", "");

        Class<?> clazz;

        try {
            clazz = Class.forName(className, false, plugin.getClass().getClassLoader());
        } catch(Throwable e) {
            return;
        }

        if (!clazz.isAnnotationPresent(UnitedConfig.class)) return;
        if (!clazz.isInterface()) return;
        if (!UnitedConfigHandler.class.isAssignableFrom(clazz)) return;

        try {
            var hasGet = Arrays.stream(clazz.getDeclaredMethods())
                    .anyMatch(m -> Modifier.isStatic(m.getModifiers()) && m.getName().equals("get") && m.getParameterCount() == 0);

            if (!hasGet)
                United.logger().warning(clazz.getSimpleName()
                        + " is missing: static " + clazz.getSimpleName()
                        + " get() { return UnitedConfigs.get(" + clazz.getSimpleName() + ".class); }");

            UnitedConfigs.register(plugin, clazz.asSubclass(UnitedConfigHandler.class));
        } catch(Throwable e) {
            United.logger().error("Could not register config: " + className);
            United.logger().error(e.getMessage());
        }

    }

    public static void unregisterAll(JavaPlugin plugin) {
        UnitedConfigs.unregisterAll(plugin);
    }

}
