package com.niccholaspage.Fe;

import com.niccholaspage.Fe.Databases.AccountInt;
import com.niccholaspage.Fe.API.Database;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FeListener implements Listener
{
	private final Fe plugin;
	public FeListener(Fe plugin)
	{
		this.plugin = plugin;
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		final Player player = event.getPlayer();
		final Database database = plugin.getDB();
		final AccountInt account = (AccountInt)database.createAccount(player.getUniqueId());
		database.reloadAccount(account);
		account.setName(player.getName());
		account.connected(player);
	}
	@EventHandler
	public void onPlayerQuit(PlayerKickEvent event)
	{
		final AccountInt account = (AccountInt)plugin.getDB().getAccount(event.getPlayer().getUniqueId());
		account.disconnected();
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		final AccountInt account = (AccountInt)plugin.getDB().getAccount(event.getPlayer().getUniqueId());
		account.disconnected();
	}
}
