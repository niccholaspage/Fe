package com.niccholaspage.Fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.command.CommandType;
import com.niccholaspage.Fe.command.SubCommand;
import com.niccholaspage.Fe.database.Account;

public class SetCommand extends SubCommand {
    private final Fe plugin;

    public SetCommand(Fe plugin) {
        super("set", "fe.set", "set [name] [amount]", Phrase.COMMAND_SET, CommandType.CONSOLE);

        this.plugin = plugin;
    }

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

        victim.setMoney(money);

        Phrase.PLAYER_SET_MONEY.sendWithPrefix(sender, victim.getName(), formattedMoney);

        return true;
    }
}
