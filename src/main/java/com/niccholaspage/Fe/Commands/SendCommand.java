package com.niccholaspage.Fe.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.API.Account;

public class SendCommand extends SubCommand {
    private final Fe plugin;

    public SendCommand(Fe plugin) {
        super("send,pay,give", "fe.send", "send [name] [amount]", Phrase.COMMAND_SEND, CommandType.PLAYER);

        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length < 2) {
            return false;
        }

        double money;

        try {
            money = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (money <= 0.0) {
            return false;
        }

        Account receiver = plugin.getShortenedAccount(args[0]);

        if (receiver == null) {
            Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);

            return true;
        }

        Account account = plugin.getAPI().getAccount(sender.getName(), null);

        if (!account.has(money)) {
            Phrase.NOT_ENOUGH_MONEY.sendWithPrefix(sender);

            return true;
        }

        if (!receiver.canReceive(money)) {
            Phrase.MAX_BALANCE_REACHED.sendWithPrefix(sender, receiver.getName());

            return true;
        }

        String formattedMoney = plugin.getAPI().format(money);

        account.withdraw(money);

        receiver.deposit(money);

        Phrase.MONEY_SENT.sendWithPrefix(sender, formattedMoney, receiver.getName());

        Player receiverPlayer = plugin.getServer().getPlayerExact(receiver.getName());

        if (receiverPlayer != null) {
            Phrase.MONEY_RECEIVE.sendWithPrefix(receiverPlayer, formattedMoney, sender.getName());
        }

        return true;
    }
}
