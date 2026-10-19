package org.unitedlands.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.UUID;

public class UnitedEconomy {

    UnitedEconomy() {}

    public String format(double amount) {
        var economy = provider();
        return economy != null ? economy.format(amount) : String.valueOf(amount);
    }

    /** TO BE IMPLEMENTED WITH NEW VAULT
    public void createEconomyAccount(OfflinePlayer player, String name) {
        var economy = provider();
        if (economy != null)
            economy.createPlayerAccount(player, name);
    }

    public void deleteEconomyAccount(UUID uuid) {
        var economy = provider();
        if (economy != null)
            economy.deleteAccount(caller(), uuid);
    }
     */

    public double getBalance(OfflinePlayer player) {
        var economy = provider();
        return economy != null ? economy.getBalance(player) : 0.0;
    }

    public boolean has(OfflinePlayer player, double amount) {
        var economy = provider();
        return economy != null && economy.has(player, amount);
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        var economy = provider();
        return economy != null && economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        var economy = provider();
        return economy != null && economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    private Economy provider() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            United.logger().warning("Vault is not enabled, economy features will not work.");
            return null;
        }

        return Bukkit.getServicesManager().load(Economy.class);
    }

}
