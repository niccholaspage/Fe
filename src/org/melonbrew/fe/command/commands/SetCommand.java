package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class SetCommand extends SubCommand {
	private final Fe plugin;
	
	public SetCommand(Fe plugin){
		super("set", "fe.set", "set [name] [amount]", Phrase.COMMAND_SET, CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length < 2){
			return false;
		}
		
		double money;
		
		try {
			money = Double.parseDouble(args[1]);
		} catch (NumberFormatException e){
			return false;
		}
		
		Account victim = plugin.getAPI().getAccount(args[0]);
		
		if (victim == null){
			sender.sendMessage(Phrase.ACCOUNT_DOES_NOT_EXIST.parseWithPrefix());
			
			return true;
		}
		
		victim.setMoney(money);
		
		sender.sendMessage(Phrase.PLAYER_SET_MONEY.parseWithPrefix(plugin.getReadName(victim), plugin.getAPI().format(victim)));
		
		return true;
	}
}
