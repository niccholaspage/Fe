package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class SendCommand extends SubCommand {
	private final Fe plugin;
	
	public SendCommand(Fe plugin){
		super("send,pay,give", "fe.send", "send [name] [amount]", "Sends another player Fe", CommandType.PLAYER);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length < 2){
			return false;
		}
		
		double money;
		
		try {
			money = Double.parseDouble(args[1]);
		} catch (NumberFormatException e){
			return false;
		}
		
		if (money <= 0.0){
			return false;
		}
		
		Account reciever = plugin.getShortenedAccount(args[0]);
		
		String messagePrefix = plugin.getMessagePrefix();
		
		if (reciever == null){
			sender.sendMessage(messagePrefix + Phrase.ACCOUNT_DOES_NOT_EXIST.parse());
			
			return true;
		}
		
		Account account = plugin.getAPI().getAccount(sender.getName());
		
		if (!account.has(money)){
			sender.sendMessage(messagePrefix + Phrase.NOT_ENOUGH_MONEY.parse());
			
			return true;
		}
		
		String formattedMoney = plugin.getAPI().format(money);
		
		account.withdraw(money);
		
		reciever.deposit(money);
		
		sender.sendMessage(messagePrefix + "You've sent " + formattedMoney + " to " + plugin.getReadName(reciever));
		
		Player recieverPlayer = plugin.getServer().getPlayerExact(reciever.getName());
		
		if (recieverPlayer != null){
			recieverPlayer.sendMessage(messagePrefix + Phrase.MONEY_RECIEVE.parse(formattedMoney, sender.getName()));
		}
		
		return true;
	}
}
