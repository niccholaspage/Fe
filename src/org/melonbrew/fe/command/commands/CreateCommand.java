package org.melonbrew.fe.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;

public class CreateCommand extends SubCommand {
	private final Fe plugin;
	
	public CreateCommand(Fe plugin){
		super("create", "fe.create", "create [name]", "Creates an account", CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length < 1){
			return false;
		}
		
		String name = args[0];
		
		if (plugin.getAPI().accountExists(name)){
			sender.sendMessage(plugin.getMessagePrefix() + Phrase.ACCOUNT_EXISTS.parse());
			
			return true;
		}
		
		if (name.length() > 16){
			sender.sendMessage(plugin.getMessagePrefix() + Phrase.NAME_TOO_LONG.parse());
			
			return true;
		}
		
		plugin.getAPI().createAccount(name);
		
		sender.sendMessage(plugin.getMessagePrefix() + Phrase.ACCOUNT_CREATED.parse(ChatColor.GOLD + plugin.getReadName(name) + ChatColor.GRAY));
		
		return true;
	}
}
