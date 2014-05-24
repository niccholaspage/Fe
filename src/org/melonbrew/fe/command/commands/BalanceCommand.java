package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

public class BalanceCommand extends SubCommand {
    private final Fe plugin;

    public BalanceCommand(Fe plugin) {
        super("balance,bal", "fe.balance", "(name)", Phrase.COMMAND_BALANCE, CommandType.CONSOLE_WITH_ARGUMENTS);

        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Account account;

        if (args.length > 0 && sender.hasPermission("fe.balance.other")) {
            account = plugin.getShortenedAccount(args[0]);

            if (account == null) {
                Phrase.ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);

                return true;
            }

            Phrase.ACCOUNT_HAS.sendWithPrefix(sender, account.getName(), plugin.getAPI().format(account));
        } else {
            Player player = plugin.getServer().getPlayer(sender.getName());

            account = plugin.getAPI().getAccount(sender.getName(), player != null ? player.getUniqueId().toString() : null);

            if (account == null) {
                Phrase.YOUR_ACCOUNT_DOES_NOT_EXIST.sendWithPrefix(sender);

                return true;
            }

            Phrase.YOU_HAVE.sendWithPrefix(sender, plugin.getAPI().format(account));
        }

        return true;
    }
}
