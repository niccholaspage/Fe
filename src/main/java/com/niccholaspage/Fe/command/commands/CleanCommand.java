package com.niccholaspage.Fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.niccholaspage.Fe.Fe2;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.command.CommandType;
import com.niccholaspage.Fe.command.SubCommand;

public class CleanCommand extends SubCommand {
    private final Fe2 plugin;

    public CleanCommand(Fe2 plugin) {
        super("clean", "fe.clean", "clean", Phrase.COMMAND_CLEAN, CommandType.CONSOLE);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.getAPI().clean();

        Phrase.ACCOUNT_CLEANED.sendWithPrefix(sender);

        return true;
    }
}
