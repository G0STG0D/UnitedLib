package org.unitedlands.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.unitedlands.interfaces.ICommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.annotations.UnitedSubCommand;

/**
 * @deprecated Use @{@link UnitedSubCommand} instead.
 */
@Deprecated(since = "2.0", forRemoval = true)
public abstract class BaseSubcommandHandler<P extends Plugin> extends BaseCommandHandler<P> {

    protected final Map<String, ICommandHandler> subHandlers = new HashMap<>();

    public BaseSubcommandHandler(P plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
        registerSubHandlers();
    }

    protected abstract void registerSubHandlers();

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 0)
            return null;

        List<String> options = null;
        if (args.length == 1) {
            options = new ArrayList<>(subHandlers.keySet());
        } else {
            String subcommand = args[0].toLowerCase();
            ICommandHandler subhandler = subHandlers.get(subcommand);

            if (subhandler != null) {
                options = subhandler.handleTab(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return options;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length == 0)
            return;

        String subcommand = args[0].toLowerCase();
        ICommandHandler subhandler = subHandlers.get(subcommand);

        if (subhandler == null) {
            //Messenger.sendMessage(sender, "invalid-command", true);
            return;
        }

        subhandler.handleCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        return;

    }

}
