package org.unitedlands.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

public class UnitedMenuItem {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ItemStack item;
    private UnitedMenuClickAction action;

    private UnitedMenuItem(ItemStack item) {
        this.item = item.clone();
    }

    public static UnitedMenuItem of(Material material) {
        return new UnitedMenuItem(new ItemStack(material));
    }

    public static UnitedMenuItem of(ItemStack item) {
        return new UnitedMenuItem(item != null ? item : new ItemStack(Material.BARRIER));
    }

    public UnitedMenuItem name(String miniMessage) {
        item.editMeta(meta -> meta.displayName(noItalic(MM.deserialize(miniMessage))));
        return this;
    }

    public UnitedMenuItem texture(String textureUrl) {
        item.editMeta(SkullMeta.class, meta -> {
            var profile  = Bukkit.createProfile(UUID.randomUUID());
            var textures = profile.getTextures();
            try {
                textures.setSkin(new URI(textureUrl).toURL());
            } catch (Exception ignored) {}

            profile.setTextures(textures);
            meta.setPlayerProfile(profile);
        });

        return this;
    }

    public UnitedMenuItem lore(String... lines) {
        item.editMeta(meta -> meta.lore(Arrays.stream(lines).map(l -> noItalic(MM.deserialize(l))).toList()));
        return this;
    }

    public UnitedMenuItem amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public UnitedMenuItem onClick(UnitedMenuClickAction action) {
        this.action = action;
        return this;
    }

    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

    public ItemStack build() {
        return item.clone();
    }

    public UnitedMenuClickAction action() {
        return action;
    }

}
