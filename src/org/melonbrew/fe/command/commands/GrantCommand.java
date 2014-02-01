package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class GrantCommand extends SubCommand {
	private final Fe plugin;

	public GrantCommand(Fe plugin){
		super("grant", "fe.grant", "grant [name] [amount]", Phrase.COMMAND_GRANT, CommandType.CONSOLE);

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

		String victimName = plugin.getAPI().getReadName(victim);

		if (!victim.canRecieve(money)){
			Phrase.MAX_BALANCE_REACHED.sendWithPrefix(sender, victimName);
			return true;
		}

		String formattedMoney = plugin.getAPI().format(money);

		victim.deposit(money);

		Phrase.PLAYER_GRANT_MONEY.sendWithPrefix(sender, formattedMoney, plugin.getAPI().getReadName(victim));

		Player recieverPlayer = plugin.getServer().getPlayerExact(victimName);

		if (recieverPlayer != null){
			Phrase.PLAYER_GRANTED_MONEY.sendWithPrefix(recieverPlayer, formattedMoney, sender.getName());
		}

		return true;
	}
}	
