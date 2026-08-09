package org.unitedlands.registrars;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.registrars.command.UnitedCommandRegistrar;
import org.unitedlands.registrars.listener.UnitedListenerRegistrar;

public class UnitedLifecycleListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (!(event.getPlugin() instanceof JavaPlugin plugin))
            return;

        var depencencies     = plugin.getPluginMeta().getPluginDependencies();
        var softDependencies = plugin.getPluginMeta().getPluginSoftDependencies();
        if (!depencencies.contains("UnitedLib") && !softDependencies.contains("UnitedLib"))
            return;

        UnitedListenerRegistrar.registerAll(plugin);
        UnitedCommandRegistrar.registerAll(plugin);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (!(event.getPlugin() instanceof JavaPlugin plugin))
            return;

        UnitedListenerRegistrar.unregisterAll(plugin);
        UnitedCommandRegistrar.unregisterAll(plugin);
    }

}
