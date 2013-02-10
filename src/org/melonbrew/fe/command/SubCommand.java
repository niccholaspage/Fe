package org.melonbrew.fe.command;

import org.melonbrew.fe.Phrase;

import com.niccholaspage.Metro.base.command.Command;
import com.niccholaspage.Metro.base.command.CommandSender;

public abstract class SubCommand {
	private final String name;
	
	private final String permission;
	
	private final String usage;
	
	private final Phrase description;
	
	private final CommandType commandType;
	
	public SubCommand(String name, String permission, String usage, Phrase description, CommandType commandType){
		this.name = name;
		
		this.permission = permission;
		
		this.usage = usage;
		
		this.description = description;
		
		this.commandType = commandType;
	}
	
	public String getFirstName(){
		return name.split(",")[0];
	}
	
	public String getName(){
		return name;
	}
	
	public String getPermission(){
		return permission;
	}
	
	public String getUsage(){
		return usage;
	}
	
	public Phrase getDescription(){
		return description;
	}
	
	public CommandType getCommandType(){
		return commandType;
	}
	
	public abstract boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args);
}
