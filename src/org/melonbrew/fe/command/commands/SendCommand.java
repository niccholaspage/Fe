package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class SendCommand extends SubCommand {
	private final Fe plugin;

	public SendCommand(Fe plugin){
		super("send,pay,give", "fe.send", "send [name] [amount]", Phrase.COMMAND_SEND, CommandType.PLAYER);

		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
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

		if (money <= 0.0){
			return false;
		}

		Account receiver = plugin.getShortenedAccount(args[0]);

		if (receiver == null){
			Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);

			return true;
		}

		Account account = plugin.getAPI().getAccount(sender.getName());

		if (!account.has(money)){
			Phrase.NOT_ENOUGH_MONEY.sendWithPrefix(sender);

			return true;
		}

		String receiverName = plugin.getAPI().getReadName(receiver);

		if (!receiver.canReceive(money)){
			Phrase.MAX_BALANCE_REACHED.sendWithPrefix(sender, receiverName);

			return true;
		}

		String formattedMoney = plugin.getAPI().format(money);

		account.withdraw(money);

		receiver.deposit(money);

		Phrase.MONEY_SENT.sendWithPrefix(sender, formattedMoney, receiverName);

		Player receiverPlayer = plugin.getServer().getPlayerExact(receiver.getName());

		if (receiverPlayer != null){
			Phrase.MONEY_RECEIVE.sendWithPrefix(receiverPlayer, formattedMoney, sender.getName());
		}

		return true;
	}
}
