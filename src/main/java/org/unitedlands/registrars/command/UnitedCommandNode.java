package org.unitedlands.registrars.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class UnitedCommandNode {

    final String   name;
    final String[] aliases;
    final String   description;
    final String   usage;
    final String   permission;
    final boolean  playerOnly;
    final boolean  catchAll;

    final UnitedCommandExecutor executor;

    private final Map<String, UnitedCommandNode> children = new HashMap<>();

    UnitedCommandNode catchAllChild = null;

    UnitedCommandNode(String name, String[] aliases, String description, String usage, String permission, boolean playerOnly, boolean catchAll, UnitedCommandExecutor executor) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.catchAll = catchAll;
        this.executor = executor;
    }

    UnitedCommandNode(String name, String[] aliases, String description, String usage, String permission, boolean playerOnly, UnitedCommandExecutor executor) {
        this(name, aliases, description, usage, permission, playerOnly, false, executor);
    }

    void addChild(UnitedCommandNode child) {
        if (child.catchAll) {
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

}
