package org.unitedlands.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.UnitedLib;
import org.unitedlands.annotations.UnitedMenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class UnitedMenuBuilder implements InventoryHolder {

    private Inventory inventory;
    private UnitedMenuHandler handler;
    private Player player;

    private final UnitedMenu ann;
    private final Map<Integer, UnitedMenuClickAction> actions = new HashMap<>();

    private List<UnitedMenuItem> pageItems = List.of();
    private int currentPage = 0;

    // TODO: Replace this with config
    private static final String TEXTURE_PREV = "http://textures.minecraft.net/texture/cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5";
    private static final String TEXTURE_NEXT = "http://textures.minecraft.net/texture/956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311";

    UnitedMenuBuilder(UnitedMenu ann) {
        this.ann = ann;
    }

    void init(Inventory inventory, UnitedMenuHandler handler, Player player) {
        this.inventory = inventory;
        this.handler   = handler;
        this.player    = player;

        rebuild();
    }

    private void rebuild() {
        inventory.clear();
        actions.clear();
        currentPage = 0;

        if (ann.borders())
            placeBorders();

        handler.build(this);

        if (ann.pageable())
            renderNavButtons();
    }

    private void rebuildPage() {
        var rows     = ann.size().rows();
        var prevSlot = ann.prevSlot().getSlot(rows);
        var nextSlot = ann.nextSlot().getSlot(rows);

        for (var slot : effectiveContentSlots()) {
            inventory.setItem(slot, null);
            actions.remove(slot);
        }

        var borderBlock = ann.borders()
                ? UnitedMenuItem.of(ann.borderMaterial()).name(ann.borderName()).build()
                : null;

        inventory.setItem(prevSlot, borderBlock); actions.remove(prevSlot);
        inventory.setItem(nextSlot, borderBlock); actions.remove(nextSlot);

        renderPageContent();
        renderNavButtons();
    }

    // ************************************************
    //   P U B L I C    A P I
    // ************************************************

    public void set(int slot, UnitedMenuItem item) {
        inventory.setItem(slot, item.build());

        if (item.action() != null)
            actions.put(slot, item.action());
        else
            actions.remove(slot);
    }

    public void set(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        actions.remove(slot);
    }

    public void fill(UnitedMenuItem item) {
        IntStream.range(0, inventory.getSize())
                .forEach(slot -> set(slot, item));
    }

    public void fillRow(int row, UnitedMenuItem item) {
        var start = row * 9;
        IntStream.range(start, start + 9)
                .forEach(slot -> set(slot, item));
    }

    public void fillColumn(int col, UnitedMenuItem item) {
        IntStream.range(0, ann.size().rows())
                .map(row -> row * 9 + col)
                .forEach(slot -> set(slot, item));
    }

    public void fillBorders(UnitedMenuItem item) {
        var rows = ann.size().rows();

        fillRow(0, item);
        fillRow(rows - 1, item);
        fillColumn(0, item);
        fillColumn(8, item);
    }

    public void setPageContent(List<UnitedMenuItem> items) {
        this.pageItems = items;
        renderPageContent();
    }

    public void nextPage() {
        if (!hasNextPage())
            return;

        currentPage++;
        rebuildPage();
    }

    public void prevPage() {
        if (!hasPrevPage())
            return;

        currentPage--;
        rebuildPage();
    }

    public boolean hasNextPage() {
        return currentPage + 1 < totalPages();
    }

    public boolean hasPrevPage() {
        return currentPage > 0;
    }

    public Player getPlayer() {
        return player;
    }

    public int currentPage() {
        return currentPage;
    }

    public int totalPages() {
        return (int) Math.ceil((double) pageItems.size() / effectiveContentSlots().length);
    }

    // ************************************************
    //   I N T E R N A L
    // ************************************************

    private void placeBorders() {
        fillBorders(UnitedMenuItem.of(ann.borderMaterial()).name(ann.borderName()));
    }

    private void renderPageContent() {
        var slots = effectiveContentSlots();
        var start = currentPage * slots.length;

        for (int i = 0; i < slots.length; i++) {
            var idx = start + i;

            if (idx < pageItems.size()) {
                set(slots[i], pageItems.get(idx));
            } else {
                inventory.setItem(slots[i], null);
                actions.remove(slots[i]);
            }
        }
    }

    private void renderNavButtons() {
        if (!ann.pageable())
            return;

        var rows     = ann.size().rows();
        var prevSlot = ann.prevSlot().getSlot(rows);
        var nextSlot = ann.nextSlot().getSlot(rows);

        if (hasPrevPage())
            set(prevSlot, UnitedMenuItem.of(Material.PLAYER_HEAD).texture(TEXTURE_PREV).name("<gray>◀ Previous").onClick(e -> prevPage()));

        if (hasNextPage())
            set(nextSlot, UnitedMenuItem.of(Material.PLAYER_HEAD).texture(TEXTURE_NEXT).name("<gray>Next ▶").onClick(e -> nextPage()));
    }

    private int[] effectiveContentSlots() {
        if (ann.contentSlots().length > 0)
            return ann.contentSlots();

        var rows     = ann.size().rows();
        var prevSlot = ann.pageable() ? ann.prevSlot().getSlot(rows) : -1;
        var nextSlot = ann.pageable() ? ann.nextSlot().getSlot(rows) : -1;

        return IntStream.range(0, rows * 9)
                .filter(slot -> {
                    if (ann.borders()) {
                        var row = slot / 9;
                        var col = slot % 9;
                        if (!(row > 0 && row < rows - 1 && col > 0 && col < 8))
                            return false;
                    }
                    if (slot == prevSlot || slot == nextSlot)
                        return false;

                    return true;
                })
                .toArray();
    }

    void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        var slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize())
            return;

        var action = actions.get(slot);
        if (action != null)
            action.onClick(event);
    }

    void handleClose(InventoryCloseEvent event) {
        if (ann.closeable() || !(event.getPlayer() instanceof Player player))
            return;

        Bukkit.getScheduler().runTaskLater(UnitedLib.getInstance(), () -> player.openInventory(inventory), 1L);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
