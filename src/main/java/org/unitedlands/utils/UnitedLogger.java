package org.unitedlands.utils;

import org.unitedlands.UnitedLib;

public class UnitedLogger {

    UnitedLogger() {}

    public void info(String message)                { resolveLogger().info(message);    }
    public void info(String message, String prefix) { resolveLogger().info("[" + prefix + "] " + message); }

    public void warning(String message)                { resolveLogger().warning(message); }
    public void warning(String message, String prefix) { resolveLogger().warning("[" + prefix + "] " + message); }

    public void error(String message)                { resolveLogger().severe(message); }
    public void error(String message, String prefix) { resolveLogger().severe("[" + prefix + "] " + message); }

    public void debug(String message)                { resolveLogger().info(message); }
    public void debug(String message, String prefix) { resolveLogger().info("[" + prefix + "] " + message); }

    private java.util.logging.Logger resolveLogger() {
        var plugin = PluginResolver.resolveCallingPlugin();
        return (plugin != null ? plugin : UnitedLib.getInstance()).getLogger();
    }

}