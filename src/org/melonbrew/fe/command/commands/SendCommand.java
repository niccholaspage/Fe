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
		super("send,pay,give", "fe.send", "send [name] [amount]", Phrase.COMMAND_SEND, CommandType.PLAYER);
		
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
		
		if (reciever == null){
			sender.sendMessage(Phrase.ACCOUNT_DOES_NOT_EXIST.parseWithPrefix());
			
			return true;
		}
		
		Account account = plugin.getAPI().getAccount(sender.getName());
		
		if (!account.has(money)){
			sender.sendMessage(Phrase.NOT_ENOUGH_MONEY.parseWithPrefix());
			
			return true;
		}
		
		String recieverName = plugin.getReadName(reciever);
		
		if (!reciever.canRecieve(money)){
			sender.sendMessage(Phrase.MAX_BALANCE_REACHED.parseWithPrefix(recieverName));
			
			return true;
		}
		
		String formattedMoney = plugin.getAPI().format(money);
		
		account.withdraw(money);
		
		reciever.deposit(money);
		
		sender.sendMessage(Phrase.MONEY_SENT.parseWithPrefix(formattedMoney, recieverName));
		
		Player recieverPlayer = plugin.getServer().getPlayerExact(reciever.getName());
		
		if (recieverPlayer != null){
			recieverPlayer.sendMessage(Phrase.MONEY_RECIEVE.parseWithPrefix(formattedMoney, sender.getName()));
		}
		
		return true;
	}
}
