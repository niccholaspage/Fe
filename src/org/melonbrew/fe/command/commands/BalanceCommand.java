package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class BalanceCommand extends SubCommand {
	private final Fe plugin;
	
	public BalanceCommand(Fe plugin){
		super("balance,bal", "fe.balance", "(name)", "Checks your balance", CommandType.CONSOLE_WITH_ARGUMENTS);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Account account;
		
		String message = plugin.getMessagePrefix();
		
		if (args.length > 0 && sender.hasPermission("fe.balance.other")){
			account = plugin.getAPI().getAccount(args[0]);
			
			if (account == null){
				sender.sendMessage(plugin.getMessagePrefix() + Phrase.ACCOUNT_DOES_NOT_EXIST.parse());
				
				return true;
			}
			
			message += Phrase.ACCOUNT_HAS.parse(plugin.getReadName(account));
		}else {
			account = plugin.getAPI().getAccount(sender.getName());
			
			message += Phrase.YOU_HAVE.parse();
		}
		
		sender.sendMessage(message + plugin.getAPI().format(account));
		
		return true;
	}
}
