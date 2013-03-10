package org.melonbrew.fe.command.commands;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;

import com.niccholaspage.Metro.base.command.Command;
import com.niccholaspage.Metro.base.command.CommandSender;

public class CreateCommand extends SubCommand {
	private final Fe plugin;
	
	public CreateCommand(Fe plugin){
		super("create", "fe.create", "create [name]", Phrase.COMMAND_CREATE, CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length < 1){
			return false;
		}
		
		String name = args[0];
		
		if (plugin.getAPI().accountExists(name)){
			Phrase.ACCOUNT_EXISTS.sendWithPrefix(sender);
			
			return true;
		}
		
		if (name.length() > 16){
			Phrase.NAME_TOO_LONG.sendWithPrefix(sender);
			
			return true;
		}
		
		plugin.getAPI().createAccount(name);
		
		Phrase.ACCOUNT_CREATED.sendWithPrefix(sender, Phrase.PRIMARY_COLOR.parse() + plugin.getAPI().getReadName(name) + Phrase.SECONDARY_COLOR.parse());
		
		return true;
	}
}
