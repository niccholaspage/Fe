package com.niccholaspage.Fe.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;

public class RemoveCommand extends SubCommand
{
	private final Fe plugin;
	public RemoveCommand(Fe plugin)
	{
		super("remove", "fe.remove", "remove [name]", Phrase.COMMAND_REMOVE, CommandType.CONSOLE);
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(args.length < 1)
			return false;
		String name = args[0];
		if(!plugin.getAPI().accountExists(name, null))
		{
			Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
			return true;
		}
		plugin.getAPI().removeAccount(name, null);
		Phrase.ACCOUNT_REMOVED.sendWithPrefix(sender, Phrase.PRIMARY_COLOR.parse() + name + Phrase.SECONDARY_COLOR.parse());
		return true;
	}
}
