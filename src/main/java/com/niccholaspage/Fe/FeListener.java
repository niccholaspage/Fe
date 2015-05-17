package com.niccholaspage.Fe;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.niccholaspage.Fe.API.Account;
import org.bukkit.event.player.PlayerKickEvent;

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
		final Account account = plugin.getDB().getAccount(player.getUniqueId());
		account.setName(player.getName());
		account.connected(player);
	}
	@EventHandler
	public void onPlayerQuit(PlayerKickEvent event)
	{
		final Account account = plugin.getDB().getAccount(event.getPlayer().getUniqueId());
		account.disconnected();
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		final Account account = plugin.getDB().getAccount(event.getPlayer().getUniqueId());
		account.disconnected();
	}
}
