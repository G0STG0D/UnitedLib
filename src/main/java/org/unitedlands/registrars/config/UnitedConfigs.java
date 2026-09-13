package org.unitedlands.registrars.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.annotations.UnitedConfig;
import org.unitedlands.annotations.UnitedSection;
import org.unitedlands.annotations.UnitedSetting;
import org.unitedlands.utils.United;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({ "unchecked", "ResultOfMethodCallIgnored" })
public class UnitedConfigs {

    private record ConfigEntry(JavaPlugin plugin, YamlConfiguration config, UnitedConfigHandler proxy) {}

    private static final Map<Class<?>, ConfigEntry> entries = new HashMap<>();

    private UnitedConfigs() {}

    public static <T extends UnitedConfigHandler> T get(Class<T> clazz) {
        var entry = entries.get(clazz);

        if (entry == null) {
            try {
                register(JavaPlugin.getProvidingPlugin(clazz), clazz);
                entry = entries.get(clazz);
            } catch(IllegalArgumentException e) {
                return null;
            }
        }

        return entry == null ? null : clazz.cast(entry.proxy);
    }

    static void register(JavaPlugin plugin, Class<? extends UnitedConfigHandler> clazz) {
        var ann = clazz.getAnnotation(UnitedConfig.class);

        var config = loadFile(plugin, ann.file());
        entries.put(clazz, new ConfigEntry(plugin, config, (UnitedConfigHandler) buildProxy(clazz, config, "")));
    }

    static void unregisterAll(JavaPlugin plugin) {
        entries.entrySet().removeIf(entry -> entry.getValue().plugin().equals(plugin));
    }

    static void reload(Class<? extends UnitedConfigHandler> clazz) {
        var entry = entries.get(clazz);
        if (entry == null)
            return;

        var ann = clazz.getAnnotation(UnitedConfig.class);
        var file = new File(entry.plugin().getDataFolder(), ann.file());
        try {
            entry.config().load(file);
        } catch (Exception e) {
            United.logger().error("Failed to reload " + clazz.getSimpleName() + ": " + e.getMessage());
        }
    }

    private static Object build(Class<?> clazz, YamlConfiguration config, String prefix) {
        return clazz.isRecord()
                ? buildRecord(clazz, config, prefix)
                : buildProxy(clazz, config, prefix);
    }

    private static Object buildRecord(Class<?> clazz, YamlConfiguration config, String prefix) {
        var components = clazz.getRecordComponents();
        var paramTypes = new Class<?>[components.length];
        var args       = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            var component = components[i];
            paramTypes[i] = component.getType();

            var setting = component.getAnnotation(UnitedSetting.class);
            var section = component.getAnnotation(UnitedSection.class);

            if (setting != null) {
                args[i] = resolveValue(config, fullKey(prefix, setting.key()), setting.def(),
                        component.getType(), component.getGenericType());
            } else if (section != null) {
                var key = fullKey(prefix, section.key());
                args[i] = component.getType() == UnitedDynamicSection.class
                        ? dynamicSection((Class<?>) ((ParameterizedType) component.getGenericType()).getActualTypeArguments()[0], config, key)
                        : build(component.getType(), config, key);
            }

            if (args[i] == null && component.getType().isPrimitive())
                throw new IllegalStateException(clazz.getSimpleName() + "." + component.getName()
                        + "() is " + component.getType().getSimpleName() + " but resolved to null - "
                        + "missing @UnitedSetting/@UnitedSection on that record component?");
        }

        try {
            return clazz.getDeclaredConstructor(paramTypes).newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build record " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static Object buildProxy(Class<?> clazz, YamlConfiguration config, String prefix) {
        return Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{ clazz },
                (proxy, method, args) -> handle(clazz, config, prefix, proxy, method, args)
        );
    }

    private static Object handle(Class<?> clazz, YamlConfiguration config, String prefix, Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class)
            return switch (method.getName()) {
                case "toString" -> clazz.getSimpleName() + "[" + prefix + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals"   -> proxy == args[0];
                default         -> null;
            };

        if (method.isDefault())
            return InvocationHandler.invokeDefault(proxy, method, args);

        if (method.getName().equals("reload") && (args == null || args.length == 0)) {
            reload((Class<? extends UnitedConfigHandler>) clazz);
            return null;
        }

        var setting = method.getAnnotation(UnitedSetting.class);
        if (setting != null)
            return resolveValue(
                    config,
                    fullKey(prefix, setting.key()), setting.def(),
                    method.getReturnType(),
                    method.getGenericReturnType()
            );

        var section = method.getAnnotation(UnitedSection.class);
        if (section != null) {
            var key = fullKey(prefix, section.key());
            if (method.getReturnType() == UnitedDynamicSection.class) {
                var typeArg = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                return dynamicSection(typeArg, config, key);
            }

            return build(method.getReturnType(), config, key);
        }

        return null;
    }

    private static Object resolveValue(YamlConfiguration config, String key, String def, Class<?> type, Type generic) {
        if (!config.contains(key))
            United.logger().warning("Missing config key '" + key + "', using default '" + def + "'");

        if (type.isEnum())
            return resolveEnum(config, key, def, type);

        return switch (type.getTypeName()) {

            case "java.lang.String"             -> config.getString(key,  def);
            case "java.util.List"               -> resolveList(config, key, generic);
            case "int",     "java.lang.Integer" -> config.getInt   (key,  def.isEmpty() ? 0  : Integer.parseInt(def));
            case "long",    "java.lang.Long"    -> config.getLong  (key,  def.isEmpty() ? 0L : Long.parseLong(def));
            case "double",  "java.lang.Double"  -> config.getDouble(key,  def.isEmpty() ? 0.0 : Double.parseDouble(def));
            case "float",   "java.lang.Float"   -> (float) config.getDouble(key, def.isEmpty() ? 0f : Float.parseFloat(def));
            case "boolean", "java.lang.Boolean" -> config.getBoolean(key, !def.isEmpty() && Boolean.parseBoolean(def));

            default -> {
                var raw = config.get(key, def);
                if (raw instanceof ConfigurationSection)
                    throw new IllegalStateException("'" + key + "' is a nested config section, but " + type.getSimpleName() + " was declared with @UnitedSetting. Did you mean @UnitedSection instead?");

                yield raw;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private static Object resolveEnum(YamlConfiguration config, String key, String def, Class<?> type) {
        var value = config.getString(key, def);
        if (value == null || value.isEmpty())
            return null;

        try {
            return Enum.valueOf((Class<Enum>) type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            United.logger().warning("Invalid value '" + value + "' for '" + key + "', expected one of: " + type.getSimpleName());
            return null;
        }
    }

    private static List<?> resolveList(YamlConfiguration config, String key, Type generic) {
        if (!(generic instanceof ParameterizedType pt))
            return config.getList(key, List.of());

        var elementType = pt.getActualTypeArguments()[0];
        if (elementType instanceof Class<?> clazz && clazz.isEnum())
            return resolveEnumList(config, key, clazz);

        return switch (pt.getActualTypeArguments()[0].getTypeName()) {

            case "java.lang.String"  -> config.getStringList(key);
            case "java.lang.Integer" -> config.getIntegerList(key);
            case "java.lang.Long"    -> config.getLongList(key);
            case "java.lang.Double"  -> config.getDoubleList(key);
            case "java.lang.Float"   -> config.getDoubleList(key).stream().map(Double::floatValue).toList();
            case "java.lang.Boolean" -> config.getBooleanList(key);

            default -> config.getList(key, List.of());
        };
    }

    @SuppressWarnings("rawtypes")
    private static List<?> resolveEnumList(YamlConfiguration config, String key, Class<?> type) {
        return config.getStringList(key).stream()
                .map(value -> {
                    try {
                        return Enum.valueOf((Class<Enum>) type, value.toUpperCase());
                    } catch(IllegalArgumentException e) {
                        United.logger().warning("Invalid value '" + value + "' in list '" + key + "', expected one of: " + type.getSimpleName());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static <V> UnitedDynamicSection<V> dynamicSection(Class<V> valueType, YamlConfiguration config, String prefix) {
        return new UnitedDynamicSection<>() {
            @Override public V get(String key) {
                return (V) build(valueType, config, prefix + "." + key);
            }

            @Override public boolean has(String key) {
                return config.contains(prefix + "." + key);
            }

            @Override public Set<String> keys() {
                var s = config.getConfigurationSection(prefix);
                return s != null ? s.getKeys(false) : Set.of();
            }

            @Override public Collection<V> values() {
                return keys().stream().map(this::get).toList();
            }

            @Override public Map<String, V> toMap() {
                return keys().stream().collect(Collectors.toMap(k -> k, this::get));
            }
        };
    }

    private static YamlConfiguration loadFile(JavaPlugin plugin, String file) {
        var fileName   = file.isEmpty() ? "config.yml" : file;
        var configFile = new File(plugin.getDataFolder(), fileName);

        plugin.getDataFolder().mkdirs();
        if (!configFile.exists()) {
            try {
                plugin.saveResource(fileName, false);
            } catch (Exception e) {
                United.logger().error("Config file " + fileName + " for " + plugin.getName() + " could not be created: " + e.getMessage());
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    private static String fullKey(String prefix, String key) {
        return prefix.isEmpty() ? key : prefix + "." + key;
    }

}
