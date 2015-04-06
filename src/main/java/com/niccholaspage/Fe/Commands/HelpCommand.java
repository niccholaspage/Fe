package com.niccholaspage.Fe.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.FeCommands;
import com.niccholaspage.Fe.Phrase;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;

public class HelpCommand extends SubCommand {
    private final Fe plugin;

    private final FeCommands command;

    public HelpCommand(Fe plugin, FeCommands command) {
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
