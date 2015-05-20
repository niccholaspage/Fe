package com.niccholaspage.Fe.Commands;

import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DebugCommand extends SubCommand
{
	public DebugCommand(Fe plugin)
	{
		super(plugin, "debug", "fe.debug", "debug", Phrases.COMMAND_DEBUG, CommandType.CONSOLE);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		final boolean enable = !plugin.settings.debug();
		plugin.settings.debug(enable);
		Phrases.DEBUG_STATUS.sendWithPrefix(sender, enable ? "enabled" : "disabled");
		return true;
	}
}
