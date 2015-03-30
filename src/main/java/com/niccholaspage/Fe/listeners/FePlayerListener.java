package com.niccholaspage.Fe.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.database.Account;
import com.niccholaspage.Fe.database.Database;

public class FePlayerListener implements Listener
{
	private final Fe plugin;
	public FePlayerListener(Fe plugin)
	{
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents((Listener)this, plugin);
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();
		plugin.getAPI().updateAccount(player.getName(), player.getUniqueId().toString());
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Database database = plugin.getFeDatabase();
		Player player = event.getPlayer();
		Account account = database.getCachedAccount(player.getName(), player.getUniqueId().toString());
		if(account != null)
		{
			account.save(account.getMoney());
			database.removeCachedAccount(account);
		}
	}
}
