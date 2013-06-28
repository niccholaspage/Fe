package org.melonbrew.fe.command.commands;

import java.util.List;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.Account;

import com.niccholaspage.Metro.base.command.Command;
import com.niccholaspage.Metro.base.command.CommandSender;

public class TopCommand extends SubCommand {
	private final Fe plugin;
	
	public TopCommand(Fe plugin){
		super("top", "fe.top", "top", Phrase.COMMAND_TOP, CommandType.CONSOLE);
		
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		List<Account> topAccounts;
		
		switch(args.length){
		case 0:
			topAccounts = plugin.getAPI().getTopAccounts();
			break;
		case 1:
			try {
				Integer currenttopsize = Integer.parseIntereg(args[1]);
				topAccounts = plugin.getAPI().getTopAccounts(currenttopsize);
			} catch (NumberFormatException e){
				return false;
			}
			break;
		default:
			return false;
		}
		
		if (topAccounts.size() < 1){
			Phrase.NO_ACCOUNTS_EXIST.sendWithPrefix(sender);
			return true;
		}
		
		sender.sendMessage(plugin.getEqualMessage(Phrase.RICH.parse(), 10));
		
		for (int i = 0; i < topAccounts.size(); i++){
			Account account = topAccounts.get(i);
			
			String two = Phrase.SECONDARY_COLOR.parse();
			
			sender.sendMessage(two + (i + 1) + ". " + Phrase.PRIMARY_COLOR.parse() + plugin.getAPI().getReadName(account) + two + " - " + plugin.getAPI().format(account));
		}
		
		sender.sendMessage(plugin.getEndEqualMessage(28));
		
		return true;
	}
}
