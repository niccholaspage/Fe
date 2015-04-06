package com.niccholaspage.Fe.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.API.Account;

public class BalanceCommand extends SubCommand
{
	private final Fe plugin;
	public BalanceCommand(Fe plugin)
	{
		super("balance,bal", "fe.balance", "(name)", Phrase.COMMAND_BALANCE, CommandType.CONSOLE_WITH_ARGUMENTS);
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(args.length > 0 && sender.hasPermission("fe.balance.other"))
		{
			Account account = plugin.getShortenedAccount(args[0]);
			if(account == null)
			{
				Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
				return true;
			}
			Phrase.ACCOUNT_HAS.sendWithPrefix(sender, account.getName(), plugin.getAPI().format(account));
		} else {
			Player player = plugin.getServer().getPlayer(sender.getName());
			Account account = plugin.getAPI().getAccount(sender.getName(), player != null ? player.getUniqueId().toString() : null);
			if(account == null)
			{
				Phrase.YOUR_ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
				return true;
			}
			Phrase.YOU_HAVE.sendWithPrefix(sender, plugin.getAPI().format(account));
		}
		return true;
	}
}
