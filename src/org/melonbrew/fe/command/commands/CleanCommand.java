package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;

public class CleanCommand extends SubCommand {
	private final Fe plugin;
	
	public CleanCommand(Fe plugin){
		super("clean", "fe.clean", "clean", "Cleans the accounts with default balance", CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String[] args){
		plugin.getAPI().clean();
		
		return true;
	}
}
