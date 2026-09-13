package org.unitedlands.utils;

import org.bukkit.plugin.java.JavaPlugin;

final class PluginResolver {

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private PluginResolver() {}

    static JavaPlugin resolveCallingPlugin() {
        var frame = WALKER.walk(stream -> stream
                .filter(f -> !f.getClassName().startsWith("org.unitedlands.utils."))
                .findFirst());

        if (frame.isEmpty())
            return null;

        try {
            return JavaPlugin.getProvidingPlugin(frame.get().getDeclaringClass());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return null;
        }

    }

}
