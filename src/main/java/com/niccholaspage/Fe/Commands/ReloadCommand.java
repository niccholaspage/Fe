package com.niccholaspage.Fe.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import com.niccholaspage.Fe.API.CommandType;
import com.niccholaspage.Fe.API.SubCommand;
import org.bukkit.plugin.PluginManager;

public class ReloadCommand extends SubCommand
{
	private final Fe plugin;
	public ReloadCommand(Fe plugin)
	{
		super("reload", "fe.reload", "reload", Phrases.COMMAND_RELOAD, CommandType.CONSOLE);
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		final PluginManager pluginManager = plugin.getServer().getPluginManager();
		pluginManager.disablePlugin(plugin);
		pluginManager.enablePlugin(plugin);
		Phrases.CONFIG_RELOADED.sendWithPrefix(sender);
		return true;
	}
}
