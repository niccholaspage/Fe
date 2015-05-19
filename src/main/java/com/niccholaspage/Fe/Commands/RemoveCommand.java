package com.niccholaspage.Fe.Commands;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RemoveCommand extends SubCommand
{
	public RemoveCommand(Fe plugin)
	{
		super(plugin, "remove", "fe.remove", "remove [name]", Phrases.COMMAND_REMOVE, CommandType.CONSOLE);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(args.length < 1)
			return false;
		String name = args[0];
		final Account account = plugin.api.getAccount(name);
		if(account != null)
		{
			plugin.api.removeAccount(account);
			Phrases.ACCOUNT_REMOVED.sendWithPrefix(sender, Phrases.PRIMARY_COLOR.parse() + name + Phrases.SECONDARY_COLOR.parse());
		} else
			Phrases.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
		return true;
	}
}
