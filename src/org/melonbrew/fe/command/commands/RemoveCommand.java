package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;

public class RemoveCommand extends SubCommand {
    private final Fe plugin;

    public RemoveCommand(Fe plugin) {
        super("remove", "fe.remove", "remove [name]", Phrase.COMMAND_REMOVE, CommandType.CONSOLE);

        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 1) {
            return false;
        }

        String name = args[0];

        if (!plugin.getAPI().accountExists(name, null)) {
            Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);

            return true;
        }

        plugin.getAPI().removeAccount(name, null);

        Phrase.ACCOUNT_REMOVED.sendWithPrefix(sender, Phrase.PRIMARY_COLOR.parse() + name + Phrase.SECONDARY_COLOR.parse());

        return true;
    }
}
