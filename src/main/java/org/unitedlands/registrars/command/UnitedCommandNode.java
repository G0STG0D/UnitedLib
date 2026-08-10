package org.unitedlands.registrars.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class UnitedCommandNode {

    final String   name;
    final String[] aliases;
    final String   description;
    final String   usage;
    final String   permission;
    final boolean  playerOnly;
    final boolean  catchAll;
    final int      cooldown;
    final String   cooldownPermission;

    final UnitedCommandExecutor executor;

    private final Map<String, UnitedCommandNode> children = new HashMap<>();
    UnitedCommandNode catchAllChild = null;

    private final Map<UUID, Long> cooldowns;

    UnitedCommandNode(String name, String[] aliases, String description, String usage, String permission, boolean playerOnly,
                      boolean catchAll, int cooldown, String cooldownPermission, UnitedCommandExecutor executor) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.catchAll = catchAll;
        this.cooldown = cooldown;
        this.cooldownPermission = cooldownPermission;
        this.executor = executor;

        this.cooldowns = cooldown > 0 ? new HashMap<>() : null;
    }

    UnitedCommandNode(String name, String[] aliases, String description, String usage, String permission, boolean playerOnly,
                      int cooldown, String cooldownPermission, UnitedCommandExecutor executor) {
        this(name, aliases, description, usage, permission, playerOnly, false, cooldown, cooldownPermission, executor);
    }

    void addChild(UnitedCommandNode child) {
        if (child.catchAll && child.name.isEmpty()) {
            catchAllChild = child;
            return;
        }

        children.put(child.name, child);
        for (var alias : child.aliases)
            children.put(alias, child);
    }

    UnitedCommandNode find(String arg) {
        return children.get(arg.toLowerCase());
    }

    Set<String> childNames() {
        return children.keySet();
    }

    boolean isOnCooldown(UUID player) {
        if (cooldowns == null)
            return false;

        var last = cooldowns.get(player);
        return last != null && (System.currentTimeMillis() - last) < cooldown * 1000L;
    }

    long remainingCooldown(UUID player) {
        if (cooldowns == null)
            return 0;

        var last = cooldowns.get(player);
        if (last == null)
            return 0;

        return (cooldown * 1000L - (System.currentTimeMillis() - last)) / 1000;
    }

    void applyCooldown(UUID player) {
        if (cooldowns != null)
            cooldowns.put(player, System.currentTimeMillis());
    }

}
