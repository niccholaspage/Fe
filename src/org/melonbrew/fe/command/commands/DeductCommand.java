package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class DeductCommand extends SubCommand {
	private final Fe plugin;

	public DeductCommand(Fe plugin){
		super("deduct", "fe.deduct", "deduct [name] [amount]", Phrase.COMMAND_DEDUCT, CommandType.CONSOLE);

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

		Account victim = plugin.getShortenedAccount(args[0]);

		if (victim == null){
			Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
			return true;
		}

		String victimName = plugin.getReadName(victim);

		if (!victim.canRecieve(money)){
			Phrase.MAX_BALANCE_REACHED.sendWithPrefix(sender, victimName);
			return true;
		}

		String formattedMoney = plugin.getAPI().format(money);
		victim.withdraw(money);
		Phrase.PLAYER_DEDUCT_MONEY.sendWithPrefix(sender, formattedMoney, plugin.getReadName(victim));

		return true;
	}
}	
