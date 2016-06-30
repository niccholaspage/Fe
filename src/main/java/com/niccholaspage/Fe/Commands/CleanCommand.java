package com.niccholaspage.Fe.Commands;

import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CleanCommand extends SubCommand
{
	public CleanCommand(Fe plugin)
	{
		super(plugin, "clean", "fe.clean", "clean", Phrases.COMMAND_CLEAN, CommandType.CONSOLE);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		plugin.api.clean();
		Phrases.ACCOUNT_CLEANED.sendWithPrefix(sender);
		return true;
	}
}
