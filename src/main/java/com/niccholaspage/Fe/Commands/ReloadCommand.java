package com.niccholaspage.Fe.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;

public class ReloadCommand extends SubCommand
{
	private final Fe plugin;
	public ReloadCommand(Fe plugin)
	{
		super("reload", "fe.reload", "reload", Phrases.COMMAND_RELOAD, CommandType.CONSOLE);
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		plugin.reloadConfig();
		Phrases.CONFIG_RELOADED.sendWithPrefix(sender);
		return true;
	}
}
