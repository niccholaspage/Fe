package com.niccholaspage.Fe.Commands;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GrantCommand extends SubCommand
{
	public GrantCommand(Fe plugin)
	{
		super(plugin, "grant", "fe.grant", "grant [name] [amount]", Phrases.COMMAND_GRANT, CommandType.CONSOLE);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(args.length < 2)
			return false;
		double money;
		try
		{
			money = Double.parseDouble(args[1]);
		} catch(NumberFormatException e) {
			return false;
		}
		Account victim = plugin.getShortenedAccount(args[0]);
		if(victim == null)
		{
			Phrases.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
			return true;
		}
		if(!victim.canReceive(money))
		{
			Phrases.MAX_BALANCE_REACHED.sendWithPrefix(sender, victim.getName());
			return true;
		}
		String formattedMoney = plugin.api.format(money);
		victim.deposit(money);
		Phrases.PLAYER_GRANT_MONEY.sendWithPrefix(sender, formattedMoney, victim.getName());
		Player receiverPlayer = plugin.getServer().getPlayerExact(victim.getName());
		if(receiverPlayer != null)
			Phrases.PLAYER_GRANTED_MONEY.sendWithPrefix(receiverPlayer, formattedMoney, sender.getName());
		return true;
	}
}
