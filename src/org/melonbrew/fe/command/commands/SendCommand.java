package org.melonbrew.fe.command.commands;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

import com.niccholaspage.Metro.base.command.Command;
import com.niccholaspage.Metro.base.command.CommandSender;
import com.niccholaspage.Metro.base.player.Player;

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
			Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
			
			return true;
		}
		
		Account account = plugin.getAPI().getAccount(sender.getName());
		
		if (!account.has(money)){
			Phrase.NOT_ENOUGH_MONEY.sendWithPrefix(sender);
			
			return true;
		}
		
		String recieverName = plugin.getAPI().getReadName(reciever);
		
		if (!reciever.canRecieve(money)){
			Phrase.MAX_BALANCE_REACHED.sendWithPrefix(sender, recieverName);
			
			return true;
		}
		
		String formattedMoney = plugin.getAPI().format(money);
		
		account.withdraw(money);
		
		reciever.deposit(money);
		
		Phrase.MONEY_SENT.sendWithPrefix(sender, formattedMoney, recieverName);
		
		Player recieverPlayer = plugin.getServer().getOnlinePlayer(reciever.getName(), true);
		
		if (recieverPlayer != null){
			Phrase.MONEY_RECIEVE.sendWithPrefix(recieverPlayer, formattedMoney, sender.getName());
		}
		
		return true;
	}
}
