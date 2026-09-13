package org.unitedlands.menu;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.unitedlands.annotations.UnitedMenu;
import org.unitedlands.utils.United;

public class UnitedMenus {

    private UnitedMenus() {}

    public static void open(Player player, UnitedMenuHandler handler) {
        var ann = handler.getClass().getAnnotation(UnitedMenu.class);
        if (ann == null) {
            United.logger().error("Missing @UnitedMenu annotation on " + handler.getClass().getName());
            return;
        }

        var menu  = new UnitedMenuBuilder(ann);
        var title = MiniMessage.miniMessage().deserialize(ann.title());
        var inv   = ann.type() == InventoryType.CHEST
                ? Bukkit.createInventory(menu, ann.size().slots, title)
                : Bukkit.createInventory(menu, ann.type(), title);

        menu.init(inv, handler, player);
        player.openInventory(inv);
    }

}
