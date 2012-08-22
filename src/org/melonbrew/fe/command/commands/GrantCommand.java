package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class GrantCommand extends SubCommand {
	private final Fe plugin;
	
	public GrantCommand(Fe plugin){
		super("grant", "fe.grant", "grant [name] [amount]", "Grants a player more Fe", CommandType.CONSOLE);
		
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
		
		Account victim = plugin.getAPI().getAccount(args[0]);
		
		if (victim == null){
			sender.sendMessage(plugin.getMessagePrefix() + Phrase.ACCOUNT_DOES_NOT_EXIST.parse());
			
			return true;
		}
		
		victim.deposit(money);
		
		String message = plugin.getMessagePrefix() + Phrase.PLAYER_GRANT_MONEY.parse(plugin.getAPI().format(money), plugin.getReadName(victim));
		
		sender.sendMessage(message);
		
		return true;
	}
}
