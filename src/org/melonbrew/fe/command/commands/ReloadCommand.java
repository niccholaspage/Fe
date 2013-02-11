package org.melonbrew.fe.command.commands;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;

import com.niccholaspage.Metro.base.command.Command;
import com.niccholaspage.Metro.base.command.CommandSender;

public class ReloadCommand extends SubCommand {
	private final Fe plugin;
	
	public ReloadCommand(Fe plugin){
		super("reload", "fe.reload", "reload", Phrase.COMMAND_RELOAD, CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		plugin.reloadConfig();
		
		Phrase.CONFIG_RELOADED.sendWithPrefix(sender);
		
		return true;
	}
}
