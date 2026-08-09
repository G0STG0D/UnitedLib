package org.unitedlands.annotations;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.unitedlands.menu.UnitedMenuAlign;
import org.unitedlands.menu.UnitedMenuSize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UnitedMenu {

    String title();

    UnitedMenuSize size()      default UnitedMenuSize.THREE_ROWS;
    InventoryType  type()      default InventoryType.CHEST;
    boolean        closeable() default true;

    boolean  borders()        default false;
    Material borderMaterial() default Material.GRAY_STAINED_GLASS_PANE;
    String   borderName()     default " ";

    boolean         pageable()     default false;
    int[]           contentSlots() default {};
    UnitedMenuAlign prevSlot()     default UnitedMenuAlign.BOTTOM_LEFT;
    UnitedMenuAlign nextSlot()     default UnitedMenuAlign.BOTTOM_RIGHT;

    // TODO: Add scrollable support

}
