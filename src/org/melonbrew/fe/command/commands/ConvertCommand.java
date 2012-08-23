package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;

public class ConvertCommand extends SubCommand {
	private final Fe plugin;
	
	public ConvertCommand(Fe plugin){
		super("convert", "fe.convert", "convert", Phrase.COMMAND_CONVERT, CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		return true;
	}
}
