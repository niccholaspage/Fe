package org.melonbrew.fe.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;

import com.niccholaspage.Metro.base.player.players.BukkitPlayer;

public class FeBukkitPlayerListener implements Listener {
	private final Fe fe;
	
	public FeBukkitPlayerListener(JavaPlugin plugin, Fe fe){
		this.fe = fe;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event){
		fe.getAPI().createAccount(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		
		if (!fe.isUpdated() && player.hasPermission("fe.notify")){
			Phrase.FE_OUTDATED.sendWithPrefix(new BukkitPlayer(player), fe.getLatestVersionString());
		}
	}
}
