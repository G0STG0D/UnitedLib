package org.unitedlands.classes;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.unitedlands.interfaces.ICommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.annotations.UnitedCommand;

/**
 * @deprecated Use @{@link UnitedCommand} instead.
 */
@Deprecated(since = "2.0", forRemoval = true)
public abstract class BaseCommandHandler<P extends Plugin>  implements ICommandHandler {

    protected final P plugin;
    protected final IMessageProvider messageProvider;

    public BaseCommandHandler(P plugin, IMessageProvider messageProvider)
    {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @Override
    public abstract List<String> handleTab(CommandSender sender, String[] args);
    @Override
    public abstract void handleCommand(CommandSender sender, String[] args);

}
