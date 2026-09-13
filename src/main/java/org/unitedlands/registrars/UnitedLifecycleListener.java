package org.unitedlands.registrars;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.registrars.command.UnitedCommandRegistrar;
import org.unitedlands.registrars.config.UnitedConfigRegistrar;
import org.unitedlands.registrars.listener.UnitedListenerRegistrar;
import org.unitedlands.registrars.messages.UnitedMessagesRegistrar;

public class UnitedLifecycleListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (!(event.getPlugin() instanceof JavaPlugin plugin))
            return;

        var deps     = plugin.getPluginMeta().getPluginDependencies();
        var softDeps = plugin.getPluginMeta().getPluginSoftDependencies();
        if (!deps.contains("UnitedLib") && !softDeps.contains("UnitedLib"))
            return;

        UnitedConfigRegistrar.registerAll(plugin);
        UnitedMessagesRegistrar.registerAll(plugin);
        UnitedListenerRegistrar.registerAll(plugin);
        UnitedCommandRegistrar.registerAll(plugin);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (!(event.getPlugin() instanceof JavaPlugin plugin))
            return;

        UnitedConfigRegistrar.unregisterAll(plugin);
        UnitedMessagesRegistrar.unregisterAll(plugin);
        UnitedListenerRegistrar.unregisterAll(plugin);
        UnitedCommandRegistrar.unregisterAll(plugin);
    }

}
