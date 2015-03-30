package com.niccholaspage.Fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.niccholaspage.Fe.Fe2;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.command.CommandType;
import com.niccholaspage.Fe.command.SubCommand;
import com.niccholaspage.Fe.database.Account;

public class GrantCommand extends SubCommand {
    private final Fe2 plugin;

    public GrantCommand(Fe2 plugin) {
        super("grant", "fe.grant", "grant [name] [amount]", Phrase.COMMAND_GRANT, CommandType.CONSOLE);

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

        Account victim = plugin.getShortenedAccount(args[0]);

        if (victim == null) {
            Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);
            return true;
        }

        if (!victim.canReceive(money)) {
            Phrase.MAX_BALANCE_REACHED.sendWithPrefix(sender, victim.getName());
            return true;
        }

        String formattedMoney = plugin.getAPI().format(money);

        victim.deposit(money);

        Phrase.PLAYER_GRANT_MONEY.sendWithPrefix(sender, formattedMoney, victim.getName());

        Player receiverPlayer = plugin.getServer().getPlayerExact(victim.getName());

        if (receiverPlayer != null) {
            Phrase.PLAYER_GRANTED_MONEY.sendWithPrefix(receiverPlayer, formattedMoney, sender.getName());
        }

        return true;
    }
}   
