package com.niccholaspage.Fe.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;

public class CleanCommand extends SubCommand
{
	private final Fe plugin;
	public CleanCommand(Fe plugin)
	{
		super("clean", "fe.clean", "clean", Phrases.COMMAND_CLEAN, CommandType.CONSOLE);
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		plugin.api.clean();
		Phrases.ACCOUNT_CLEANED.sendWithPrefix(sender);
		return true;
	}
}
