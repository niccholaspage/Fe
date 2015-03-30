package com.niccholaspage.Fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.niccholaspage.Fe.Fe2;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.command.CommandType;
import com.niccholaspage.Fe.command.SubCommand;

public class ReloadCommand extends SubCommand {
    private final Fe2 plugin;

    public ReloadCommand(Fe2 plugin) {
        super("reload", "fe.reload", "reload", Phrase.COMMAND_RELOAD, CommandType.CONSOLE);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();

        Phrase.CONFIG_RELOADED.sendWithPrefix(sender);

        return true;
    }
}
