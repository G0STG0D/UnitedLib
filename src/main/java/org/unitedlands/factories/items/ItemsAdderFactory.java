package org.unitedlands.factories.items;

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

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;

public class ItemsAdderFactory extends BaseItemFactory {

    @Override
    public boolean isItem(ItemStack item1, ItemStack item2) {
        CustomStack customStack1 = CustomStack.byItemStack(item1);
        CustomStack customStack2 = CustomStack.byItemStack(item2);

        if (customStack1 != null) {

            if (customStack2 == null) {
                return false;
            } else {
                return customStack1.matchNamespacedID(customStack2);
            }
        } else {
            if (customStack2 != null) {
                return false;
            } else {
                return getFilterName(item1).equals(getFilterName(item2));
            }
        }
    }

    @Override
    public ItemStack getItemStack(String material, int amount) {

        CustomStack customStack = CustomStack.getInstance(material);
        if (customStack != null) {
            var itemStack = customStack.getItemStack();
            itemStack.setAmount(amount);
            return itemStack;
        } else {
            return getVanillaItemStack(material, amount);
        }
    }

    @Override
    public ItemStack getItemStack(String material, int minAmount, int maxAmount) {

        CustomStack customStack = CustomStack.getInstance(material);
        if (customStack != null) {
            var itemStack = customStack.getItemStack();
            itemStack.setAmount(ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1));
            return itemStack;
        } else {
            var itemStack = getVanillaItemStack(material, minAmount);
            if (itemStack != null) {
                itemStack.setAmount(ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1));
                return itemStack;
            }
        }
        return null;
    }

    @Override
    public boolean isItemInInventory(Inventory inventory, ItemStack item) {
        CustomStack customItem = CustomStack.byItemStack(item);
        for (var contentItem : inventory.getContents()) {
            if (contentItem == null || contentItem.getType() == Material.AIR)
                continue;
            var contentCustomItem = CustomStack.byItemStack(contentItem);
            if (customItem != null && contentCustomItem != null) {
                if (customItem.getNamespacedID().equals(contentCustomItem.getNamespacedID()))
                    return true;
            } else if (customItem == null && contentCustomItem == null) {
                if (getFilterName(item).equals(getFilterName(contentItem)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidItem(String itemName) {
        CustomStack customItem = CustomStack.getInstance(itemName);
        if (customItem != null) {
            return true;
        } else {
            try {
                var vanillaMaterial = Material.valueOf(itemName);
                if (vanillaMaterial != null)
                    return true;
            } catch (Exception ignore) {
                return false;
            }
        }

        return false;
    }

    @Override
    public String getFilterName(ItemStack itemStack) {
        CustomStack customItem = CustomStack.byItemStack(itemStack);
        if (customItem != null) {
            return customItem.getNamespacedID();
        } else {
            return getVanillaFilterName(itemStack);
        }
    }

    @Override
    public String getDisplayName(ItemStack itemStack) {
        CustomStack customItem = CustomStack.byItemStack(itemStack);
        // Set amount to 1 to avoid parsing errors for stacks that exceed max stack size
        if (customItem != null) {
            return Formatter.removeLegacyFormatting(customItem.getDisplayName());
        } else {
            return getVanillaDisplayName(itemStack);
        }
    }

    @Override
    public List<String> getItemList() {
        var items = Arrays.stream(Material.values())
                .map(Enum::name) // gets the name as a String
                .collect(Collectors.toList());
        var customItems = ItemsAdder.getAllItems().stream().filter(i -> !i.getNamespace().startsWith("_"))
                .map(i -> i.getNamespacedID()).collect(Collectors.toList());
        items.addAll(customItems);
        return items;
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        CustomStack customItem = CustomStack.byItemStack(item);
        return customItem != null;
    }

    @Override
    public String getId(ItemStack itemStack) {
        CustomStack customItem = CustomStack.byItemStack(itemStack);
        if (customItem != null) {
            return customItem.getNamespacedID();
        } else {
            return itemStack.getType().toString();
        }
    }

    @Override
    public void placeBlock(String id, Location location) {
        var customBlock = CustomBlock.place(id, location);
        if (customBlock == null) {
            Logger.logError("Could not place block " + id);
            return;
        }
    }

    @Override
    public void removeBlock(Block block) {
        var customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock != null) {
            customBlock.remove();
            return;
        } else {
            block.setType(Material.AIR);
        }
    }

    // Helpers

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

}
