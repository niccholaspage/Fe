package com.niccholaspage.Fe.Commands;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand extends SubCommand
{
	public BalanceCommand(Fe plugin)
	{
		super(plugin, "balance,bal", "fe.balance", "(name)", Phrases.COMMAND_BALANCE, CommandType.CONSOLE_WITH_ARGUMENTS);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(args.length > 0 && sender.hasPermission("fe.balance.other"))
		{
			Account account = plugin.getShortenedAccount(args[0]);
			if(account == null)
			{
				Phrases.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
				return true;
			}
			Phrases.ACCOUNT_HAS.sendWithPrefix(sender, account.getName(), plugin.api.format(account));
		} else {
			Player player = plugin.getServer().getPlayer(sender.getName());
			Account account = player != null
				? plugin.api.getAccount(player.getUniqueId())
				: plugin.api.getAccount(sender.getName());
			if(account == null)
			{
				Phrases.YOUR_ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
				return true;
			}
			Phrases.YOU_HAVE.sendWithPrefix(sender, plugin.api.format(account));
		}
		return true;
	}
}
