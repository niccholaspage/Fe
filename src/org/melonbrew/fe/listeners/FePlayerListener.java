package org.melonbrew.fe.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;

public class FePlayerListener implements Listener {
	private final Fe plugin;

	public FePlayerListener(Fe plugin){
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event){
		plugin.getAPI().createAccount(event.getPlayer().getDisplayName());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		if (!plugin.isUpdated() && player.hasPermission("fe.notify")){
			Phrase.FE_OUTDATED.sendWithPrefix(player, plugin.getLatestVersionString());
		}
	}
}
