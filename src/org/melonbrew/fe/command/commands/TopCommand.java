package org.melonbrew.fe.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class TopCommand extends SubCommand {
	private final Fe plugin;
	
	public TopCommand(Fe plugin){
		super("top", "fe.top", "top", "Checks the top 5 richest players", CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		List<Account> topAccounts = plugin.getAPI().getTopAccounts();
		
		if (topAccounts.size() < 1){
			sender.sendMessage(ChatColor.RED + Phrase.NO_ACCOUNTS_EXIST.parse());
			
			return true;
		}
		
		sender.sendMessage(plugin.getEqualMessage(Phrase.RICH.parse(), 10));
		
		for (int i = 0; i < topAccounts.size(); i++){
			Account account = topAccounts.get(i);
			
			sender.sendMessage(ChatColor.GRAY.toString() + (i + 1) + ". " + ChatColor.GOLD + plugin.getReadName(account) + ChatColor.GRAY + " - " + plugin.getAPI().format(account));
		}
		
		sender.sendMessage(plugin.getEndEqualMessage(31));
		
		return true;
	}
}
