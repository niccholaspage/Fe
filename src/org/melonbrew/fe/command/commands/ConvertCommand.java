package org.melonbrew.fe.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.converter.ConverterType;
import org.melonbrew.fe.database.converter.converters.*;

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

			for (ConverterType type : converter.getConverterTypes()){
				message += type.getPhrase().parse() + ", ";
			}

			message = message.substring(0, message.length() - 2);

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

		Converter converter = getConverter(args[0]);

		if (converter == null){
			Phrase.CONVERTER_DOES_NOT_EXIST.sendWithPrefix(sender);

			return true;
		}

		ConverterType type = ConverterType.getType(args[1]);

		if (type == null){
			return false;
		}

		boolean supported = false;

		for (ConverterType converterType : converter.getConverterTypes()){
			if (type == converterType){
				supported = true;

				break;
			}
		}

		if (!supported){
			Phrase.CONVERTER_DOES_NOT_SUPPORT.sendWithPrefix(sender, type.getPhrase().parse());

			return true;
		}

		boolean success = converter.convert(type);

		if (success){
			Phrase.CONVERSION_SUCCEEDED.sendWithPrefix(sender);
		}else {
			Phrase.CONVERSION_FAILED.sendWithPrefix(sender);
		}

		return true;
	}
}
