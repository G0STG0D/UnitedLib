package org.unitedlands.registrars.messages;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.utils.United;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;

public class UnitedMessagesRegistrar {

    private static final String DEFAULT_LOCALE = "en";
    private static final String MISSING_FORMAT = "<red><b>[missing message: %s]</b></red>";

    private static final Map<JavaPlugin, Map<String, YamlConfiguration>> entries = new HashMap<>();

    private UnitedMessagesRegistrar() {}

    public static void registerAll(JavaPlugin plugin) {
        var locales = new HashMap<String, YamlConfiguration>();

        try {
            var url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            try (var jar = new JarFile(new File(url.toURI()))) {
                jar.stream()
                        .filter(entry -> entry.getName().startsWith("messages/") && entry.getName().endsWith(".yml"))
                        .forEach(entry -> {
                            var fileName = entry.getName().substring("messages/".length());
                            var locale   = fileName.substring(0, fileName.length() - ".yml".length());

                            locales.put(locale, loadFile(plugin, entry.getName()));
                        });
            }
        } catch(Exception e) {
            United.logger().error("Message scan failed for " + plugin.getName() + ": " + e.getMessage());
        }

        if (!locales.isEmpty())
            entries.put(plugin, locales);
    }

    public static void unregisterAll(JavaPlugin plugin) {
        entries.remove(plugin);
    }

    public static void reload(JavaPlugin plugin) {
        var locales = entries.get(plugin);
        if (locales == null)
            return;

        locales.forEach((locale, config) -> {
            try {
                config.load(new File(plugin.getDataFolder(), "messages/" + locale + ".yml"));
            } catch (Exception e) {
                United.logger().error("Could not reload " + locale + " messages for " + plugin.getName() + ": " + e.getMessage());
            }
        });
    }

    public static String resolve(JavaPlugin plugin, Locale locale, String path) {
        if (plugin == null)
            return MISSING_FORMAT.formatted(path);

        var locales = entries.get(plugin);
        if (locales == null) {
            registerAll(plugin);
            locales = entries.get(plugin);
        }

        if (locales == null)
            return MISSING_FORMAT.formatted(path);

        var language = locale != null ? locale.getLanguage() : DEFAULT_LOCALE;

        var config = locales.getOrDefault(language, locales.get(DEFAULT_LOCALE));
        if (config == null)
            return MISSING_FORMAT.formatted(path);

        var value = config.getString(path);
        if (value == null) {
            United.logger().warning("Missing message for " + plugin.getName() + " in locale " + language + ": " + path);
            return MISSING_FORMAT.formatted(path);
        }

        return value;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static YamlConfiguration loadFile(JavaPlugin plugin, String file) {
        var configFile = new File(plugin.getDataFolder(), file);

        configFile.getParentFile().mkdirs();
        if (!configFile.exists()) {
            try {
                plugin.saveResource(file, false);
            } catch(Exception e) {
                United.logger().error("Message file " + file + " for " + plugin.getName() + " could not be created: " + e.getMessage());
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

}
