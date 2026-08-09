package org.unitedlands;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.factories.items.IItemFactory;
import org.unitedlands.factories.items.ItemsAdderFactory;
import org.unitedlands.factories.items.NexoFactory;
import org.unitedlands.factories.items.VanillaItemFactory;
import org.unitedlands.factories.mobs.IMobFactory;
import org.unitedlands.factories.mobs.MythicMobFactory;
import org.unitedlands.factories.mobs.VanillaMobFactory;
import org.unitedlands.menu.UnitedMenuListener;
import org.unitedlands.registrars.UnitedLifecycleListener;
import org.unitedlands.utils.Logger;

public class UnitedLib extends JavaPlugin {

    private static UnitedLib instance;

    private IItemFactory itemFactory;
    private IMobFactory mobFactory;

    public UnitedLib() {
        instance = this;
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        loadFactories();
        loadListeners();

        Logger.log("UnitedLib initialized.");

    }

    private void loadFactories() {

        Plugin itemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        Plugin nexo = Bukkit.getPluginManager().getPlugin("Nexo");

        if (nexo != null && nexo.isEnabled()) {
            Logger.log("Nexo found, using custom item factory.");
            itemFactory = new NexoFactory();
        } else if (itemsAdder != null && itemsAdder.isEnabled()) {
            Logger.log("ItemsAdder found, using custom item factory.");
            itemFactory = new ItemsAdderFactory();
        } else {
            Logger.log("ItemsAdder not found, using vanilla item factory.");
            itemFactory = new VanillaItemFactory();
        }

        Plugin mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (mythicMobs != null && mythicMobs.isEnabled()) {
            Logger.log("MythicMobs found, using custom mob factory.");
            mobFactory = new MythicMobFactory();
        } else {
            Logger.log("MythicMobs not found, using vanilla mob factory.");
            mobFactory = new VanillaMobFactory();
        }

    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new UnitedLifecycleListener(), this);
        Bukkit.getPluginManager().registerEvents(new UnitedMenuListener(), this);
    }

    public static UnitedLib getInstance() {
        return instance;
    }

    public IItemFactory getItemFactory() {
        return itemFactory;
    }

    public IMobFactory getMobFactory() {
        return mobFactory;
    }

}
