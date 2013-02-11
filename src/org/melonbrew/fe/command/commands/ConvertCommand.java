package org.melonbrew.fe.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.converter.converters.*;
import org.melonbrew.fe.database.databases.MySQLDB;

import com.niccholaspage.Metro.base.command.Command;
import com.niccholaspage.Metro.base.command.CommandSender;

public class ConvertCommand extends SubCommand {
	private final Fe plugin;
	
	private final List<Converter> converters;
	
	public ConvertCommand(Fe plugin){
		super("convert", "fe.convert", "convert (plugin) (flatfile|mysql)", Phrase.COMMAND_CONVERT, CommandType.CONSOLE);
		
		this.plugin = plugin;
		
		converters = new ArrayList<Converter>();
		
		converters.add(new Converter_iConomy(plugin));
		converters.add(new Converter_Essentials(plugin));
		converters.add(new Converter_BOSEconomy(plugin));
		converters.add(new Converter_Fe(plugin));
	}
	
	private void sendConversionList(CommandSender sender){
		String message = plugin.getEqualMessage(Phrase.CONVERSION.parse(), 7);
		
		sender.sendMessage(message);
		
		for (Converter converter : converters){
			message = Phrase.PRIMARY_COLOR.parse() + converter.getName();
			
			message += " " + Phrase.TERTIARY_COLOR.parse() + "(" + Phrase.ARGUMENT_COLOR.parse();
			
			if (converter.isFlatFile()){
				message += Phrase.FLAT_FILE.parse();
			}
			
			if (converter.isMySQL()){
				if (converter.isFlatFile()){
					message += ", ";
				}
				
				message += Phrase.MYSQL.parse();
			}
			
			message += Phrase.TERTIARY_COLOR.parse() + ")";
			
			sender.sendMessage(message);
		}
		
		sender.sendMessage(plugin.getEndEqualMessage(31));
	}
	
	private Converter getConverter(String name){
		for (Converter converter : converters){
			if (converter.getName().equalsIgnoreCase(name)){
				return converter;
			}
		}
		
		return null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length < 1){
			sendConversionList(sender);
			
			return true;
		}
		
		if (args.length < 2){
			return false;
		}
		
		String type = args[1];
		
		String flatfile = Phrase.FLAT_FILE.parseWithoutSpaces();
		
		String mysql = Phrase.MYSQL.parseWithoutSpaces();
		
		if (!type.equalsIgnoreCase(flatfile) && !type.equalsIgnoreCase(mysql)){
			return false;
		}
		
		Converter converter = getConverter(args[0]);
		
		if (converter == null){
			Phrase.CONVERTER_DOES_NOT_EXIST.sendWithPrefix(sender);
			
			return true;
		}
		
		String supported = null;
		
		if (type.equalsIgnoreCase(flatfile) && !converter.isFlatFile()){
			supported = Phrase.FLAT_FILE.parse();
		}else if (type.equalsIgnoreCase(mysql)){
			if (!converter.mySQLtoFlatFile() && !(plugin.getFeDatabase() instanceof MySQLDB)){
				Phrase.CONVERTER_DOES_NOT_SUPPORT.sendWithPrefix(sender, Phrase.MYSQL_TO_FLAT_FILE.parse());
				
				return true;
			}
			
			if (!converter.isMySQL()){
				supported = Phrase.MYSQL.parse();
			}
		}
		
		if (supported != null){
			Phrase.CONVERTER_DOES_NOT_SUPPORT.sendWithPrefix(sender, supported);
			
			return true;
		}
		
		boolean success;
		
		if (type.equalsIgnoreCase(flatfile)){
			success = converter.convertFlatFile();
		}else {
			success = converter.convertMySQL();
		}
		
		if (success){
			Phrase.CONVERSION_SUCCEEDED.sendWithPrefix(sender);
		}else {
			Phrase.CONVERSION_FAILED.sendWithPrefix(sender);
		}
		
		return true;
	}
}
