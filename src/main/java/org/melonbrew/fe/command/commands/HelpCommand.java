package org.melonbrew.fe.command.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.FeCommand;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;

public class HelpCommand extends SubCommand {
    private final Fe plugin;

    private final FeCommand command;

    public HelpCommand(Fe plugin, FeCommand command) {
        super("help,?", "fe.?", "help", Phrase.COMMAND_HELP, CommandType.CONSOLE);

        this.plugin = plugin;

        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        sender.sendMessage(plugin.getEqualMessage(Phrase.HELP.parse(), 10));

        String operatorColor = Phrase.TERTIARY_COLOR.parse();

        String textColor = Phrase.SECONDARY_COLOR.parse();

        sender.sendMessage(textColor + Phrase.HELP_ARGUMENTS.parse(operatorColor + "[]" + textColor, operatorColor + "()" + textColor));

        for (SubCommand command : this.command.getCommands()) {
            if (command.getName().equalsIgnoreCase(getName())) {
                continue;
            }

            if (!sender.hasPermission(command.getPermission())) {
                continue;
            }

            if (!(sender instanceof Player) && command.getCommandType() == CommandType.PLAYER) {
                continue;
            }

            sender.sendMessage(this.command.parse(commandLabel, command) + textColor + " - " + command.getDescription().parse());
        }

        sender.sendMessage(plugin.getEndEqualMessage(27));

        return true;
    }
}
