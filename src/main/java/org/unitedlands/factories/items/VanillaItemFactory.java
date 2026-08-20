package org.unitedlands.factories.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Logger;

public class VanillaItemFactory extends BaseItemFactory {

    @Override
    public boolean isItem(ItemStack item1, ItemStack item2) {
        return item1.isSimilar(item2);
    }

    @Override
    public ItemStack getItemStack(String material, int amount) {
        return getVanillaItemStack(material, amount);
    }

    @Override
    public ItemStack getItemStack(String material, int minAmount, int maxAmount) {
        try {
            var itemStack = getVanillaItemStack(material, minAmount);
            if (itemStack != null) {
                itemStack.setAmount(ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1));
                return itemStack;
            }
        } catch (Exception ignore) {
            return null;
        }
        return null;
    }

    @Override
    public boolean isItemInInventory(Inventory inventory, ItemStack item) {
        // return inventory.contains(item.getType());
        for (var contentItem : inventory.getContents()) {
            if (contentItem == null || contentItem.getType() == Material.AIR)
                continue;
            if (getFilterName(item).equals(getFilterName(contentItem)))
                return true;
        }
        return false;
    }

    @Override
    public boolean isValidItem(String itemName) {
        if (itemName.contains("POTION")) {
            var potion = itemName.split(":");
            if (potion.length == 2) {
                var mat = Material.getMaterial(potion[0]);
                return mat != null;
            }
        } else {
            var vanillaMaterial = Material.getMaterial(itemName);
            return vanillaMaterial != null;
        }
        return false;
    }

    @Override
    public String getFilterName(ItemStack itemStack) {
        return getVanillaFilterName(itemStack);
    }

    @Override
    public String getDisplayName(ItemStack itemStack) {
        return getVanillaDisplayName(itemStack);
    }

    private String getVanillaDisplayName(ItemStack itemStack) {
        var type = itemStack.getType().toString();
        if (type.contains("POTION")) {
            if (itemStack.getItemMeta() instanceof PotionMeta potionMeta && potionMeta.hasBasePotionType()) {
                type = Formatter.formatReadable(type) + " (" +
                        Formatter.formatReadable(potionMeta.getBasePotionType().toString())
                        + ")";
            }
            return type;
        }
        return Formatter.formatReadable(itemStack.getType().toString());
    }

    @Override
    public List<String> getItemList() {
        var itemList = Arrays.stream(Material.values())
                .map(Enum::name) // gets the name as a String
                .collect(Collectors.toList());
        itemList.addAll(getVanillaPotions());
        return itemList;
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return false;
    }

    @Override
    public String getId(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

    @Override
    public void placeBlock(String id, Location location) {
        var material = Material.getMaterial(id);
        if (material == null) {
            Logger.logError("Could not place block " + id);
            return;
        }
        location.getBlock().setType(material);
    }

    

    // Helpers

    @Override
    public void removeBlock(Block block) {
        block.setType(Material.AIR);
    }

    private String getVanillaFilterName(ItemStack itemStack) {
        var type = itemStack.getType().toString();
        if (!type.contains("POTION")) {
            return type;
        } else {
            if (itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
                if (potionMeta.hasBasePotionType()) {
                    return type + ":" + potionMeta.getBasePotionType().toString();
                } else {
                    return type;
                }
            } else {
                return type;
            }
        }
    }

    private ItemStack getVanillaItemStack(String material, int amount) {

        try {
            if (material.contains("POTION")) {
                var potion = material.split(":");
                if (potion.length == 2) {
                    var mat = Material.getMaterial(potion[0]);
                    if (mat != null) {
                        var itemStack = new ItemStack(mat, amount);
                        if (itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
                            var baseType = PotionType.valueOf(potion[1]);
                            potionMeta.setBasePotionType(baseType);
                            itemStack.setItemMeta(potionMeta);
                        }
                        return itemStack;
                    }
                }
            } else {
                var mat = Material.getMaterial(material);
                if (mat != null) {
                    return new ItemStack(mat, amount);
                }
            }

        } catch (Exception ignore) {
            return null;
        }
        return null;
    }

    private List<String> getVanillaPotions() {
        var potionsNames = List.of("POTION", "SPLASH_POTION", "LINGERING_POTION");
        var potionTypes = PotionType.values();
        List<String> potions = new ArrayList<>();
        for (var name : potionsNames) {
            for (var type : potionTypes) {
                potions.add(name + ":" + type);
            }
        }
        return potions;
    }

}
