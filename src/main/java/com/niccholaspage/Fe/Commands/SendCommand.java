package com.niccholaspage.Fe.Commands;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand extends SubCommand
{
	public SendCommand(Fe plugin)
	{
		super(plugin, "send,pay,give", "fe.send", "send [name] [amount]", Phrases.COMMAND_SEND, CommandType.PLAYER);
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
		if(money <= 0.0)
			return false;
		Account receiver = plugin.getShortenedAccount(args[0]);
		if(receiver == null)
		{
			Phrases.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
			return true;
		}
		Account account = plugin.api.getAccount(sender.getName());
		if(!account.has(money))
		{
			Phrases.NOT_ENOUGH_MONEY.sendWithPrefix(sender);
			return true;
		}
		if(!receiver.canReceive(money))
		{
			Phrases.MAX_BALANCE_REACHED.sendWithPrefix(sender, receiver.getName());
			return true;
		}
		String formattedMoney = plugin.api.format(money);
		account.withdraw(money);
		receiver.deposit(money);
		Phrases.MONEY_SENT.sendWithPrefix(sender, formattedMoney, receiver.getName());
		Player receiverPlayer = plugin.getServer().getPlayerExact(receiver.getName());
		if(receiverPlayer != null)
			Phrases.MONEY_RECEIVE.sendWithPrefix(receiverPlayer, formattedMoney, sender.getName());
		return true;
	}
}
