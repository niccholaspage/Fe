package com.niccholaspage.Fe.Commands;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.Database;
import com.niccholaspage.Fe.API.SubCommand;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MigrateCommand extends SubCommand
{
	public MigrateCommand(Fe plugin)
	{
		super(plugin, "migrate", "fe.migrate", "migrate", Phrases.COMMAND_MIGRATE, CommandType.CONSOLE);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(args != null && args.length > 0 && args[0] != null && !"".equals(args[0]))
		{
			final String targetName = args[0].replace(" ", "").toLowerCase();
			final Database source = plugin.getDB();
			final Database target = plugin.findDB(targetName);
			if(target != null)
			{
				if(target != source && target.initialize())
				{
					target.removeAllAccounts();
					for(Account account : source.getAccounts())
						target.saveAccount(account);
					target.close();
					Phrases.DATABASE_MIGRATED.sendWithPrefix(sender);
				} else
					Phrases.DATABASE_THE_SAME.sendWithPrefix(sender);
			} else
				Phrases.DATABASE_NOT_FOUND.sendWithPrefix(sender);
		}
		return true;
	}
}
