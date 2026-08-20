package org.unitedlands.factories.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Logger;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class NexoFactory extends BaseItemFactory {

    @Override
    public boolean isItem(ItemStack item1, ItemStack item2) {

        ItemBuilder customStack1 = NexoItems.builderFromItem(item1);
        ItemBuilder customStack2 = NexoItems.builderFromItem(item2);

        if (customStack1 != null) {
            if (customStack2 == null) {
                return false;
            } else {
                return NexoItems.isSameId(item1, item2);
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
        ItemBuilder customStack = NexoItems.itemFromId(material);
        if (customStack != null) {
            var itemStack = customStack.build();
            itemStack.setAmount(amount);
            return itemStack;
        } else {
            return getVanillaItemStack(material, amount);
        }
    }

    @Override
    public ItemStack getItemStack(String material, int minAmount, int maxAmount) {
        ItemBuilder customStack = NexoItems.itemFromId(material);
        if (customStack != null) {
            var itemStack = customStack.build();
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
    public List<String> getItemList() {
        var items = Arrays.stream(Material.values())
                .map(Enum::name) // gets the name as a String
                .collect(Collectors.toList());
        var customItems = NexoItems.items().stream().map(i -> NexoItems.idFromItem(i)).collect(Collectors.toList());
        items.addAll(customItems);
        items.addAll(getVanillaPotions());
        return items;
    }

    @Override
    public boolean isValidItem(String itemName) {
        ItemBuilder customItem = NexoItems.itemFromId(itemName);
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
        ItemBuilder customItem = NexoItems.builderFromItem(itemStack);
        if (customItem != null) {
            return NexoItems.idFromItem(customItem);
        } else {
            return getVanillaFilterName(itemStack);
        }
    }

    @Override
    public String getDisplayName(ItemStack itemStack) {
        ItemBuilder customItem = NexoItems.builderFromItem(itemStack);
        // Set amount to 1 to avoid parsing errors for stacks that exceed max stack size
        if (customItem != null) {
            customItem.setAmount(1);
            return Formatter.removeLegacyFormatting(
                    PlainTextComponentSerializer.plainText().serialize(customItem.getItemName()));
        } else {
            return getVanillaDisplayName(itemStack);
        }
    }

    @Override
    public boolean isItemInInventory(Inventory inventory, ItemStack item) {
        ItemBuilder customItem = NexoItems.builderFromItem(item);
        for (var contentItem : inventory.getContents()) {
            if (contentItem == null || contentItem.getType() == Material.AIR)
                continue;
            var contentCustomItem = NexoItems.builderFromItem(contentItem);
            if (customItem != null && contentCustomItem != null) {
                return NexoItems.isSameId(contentItem, item);
            } else if (customItem == null && contentCustomItem == null) {
                if (getFilterName(item).equals(getFilterName(contentItem)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return NexoItems.exists(item);
    }

    @Override
    public String getId(ItemStack itemStack) {
        ItemBuilder customItem = NexoItems.builderFromItem(itemStack);
        if (customItem != null) {
            return NexoItems.idFromItem(customItem);
        } else {
            return itemStack.getType().toString();
        }
    }

    @Override
    public void placeBlock(String id, Location location) {
        try {
            if (NexoBlocks.isCustomBlock(id)) {
                NexoBlocks.place(id, location);
                return;
            } else {
                if (NexoFurniture.isFurniture(id)) {
                    NexoFurniture.place(id, location, 0, BlockFace.DOWN);
                    return;
                } else {
                    var vanillaMaterial = Material.valueOf(id);
                    if (vanillaMaterial != null) {
                        location.getBlock().setType(vanillaMaterial);
                    }

                }
            }

        } catch (Exception ex) {
            Logger.logError("Could not place block " + id);
        }

    }

    @Override
    public void removeBlock(Block block) {
        try {
            if (NexoBlocks.isCustomBlock(block)) {
                NexoBlocks.remove(block.getLocation());
            } else {
                if (NexoFurniture.isFurniture(block.getLocation())) {
                    NexoFurniture.remove(block.getLocation());
                } else {
                    block.setType(Material.AIR);
                }
            }
        } catch (Exception ex) {
            Logger.logError("Could not remove block at " + block.getLocation());
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
