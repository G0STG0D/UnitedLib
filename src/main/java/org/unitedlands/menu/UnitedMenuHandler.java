package org.unitedlands.menu;

import org.bukkit.entity.Player;

public interface UnitedMenuHandler {

    void build(UnitedMenuBuilder menu);

    default void open(Player player) {
        UnitedMenus.open(player, this);
    }

}
