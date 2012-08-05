package org.melonbrew.fe.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.melonbrew.fe.Fe;

public class FePlayerListener implements Listener {
	private final Fe plugin;
	
	public FePlayerListener(Fe plugin){
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event){
		plugin.getAPI().createAccount(event.getPlayer().getName());
	}
}
