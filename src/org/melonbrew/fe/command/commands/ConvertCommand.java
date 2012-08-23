package org.melonbrew.fe.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.converter.converterss.Converter_iConomy;

public class ConvertCommand extends SubCommand {
	private final Fe plugin;
	
	private final List<Converter> converters;
	
	public ConvertCommand(Fe plugin){
		super("convert", "fe.convert", "convert plugin database", Phrase.COMMAND_CONVERT, CommandType.CONSOLE);
		
		this.plugin = plugin;
		
		converters = new ArrayList<Converter>();
		
		converters.add(new Converter_iConomy());
	}
	
	private void sendConversionList(CommandSender sender){
		String message = plugin.getEqualMessage(Phrase.CONVERSION.parse(), 7);
		
		sender.sendMessage(message);
		
		for (Converter converter : converters){
			message = ChatColor.GOLD + converter.getName();
			
			message += " " + ChatColor.DARK_GRAY + "(" + ChatColor.YELLOW;
			
			if (converter.isFlatFile()){
				message += Phrase.FLAT_FILE.parse();
			}
			
			if (converter.isMySQL()){
				message += ", " + Phrase.MYSQL.parse();
			}
			
			message += ChatColor.DARK_GRAY + ")";
			
			sender.sendMessage(message);
		}
		
		sender.sendMessage(plugin.getEndEqualMessage(message.length()));
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length < 1){
			sendConversionList(sender);
			
			return true;
		}
		
		return true;
	}
}
