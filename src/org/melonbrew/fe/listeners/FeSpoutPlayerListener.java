package org.melonbrew.fe.listeners;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.player.PlayerJoinEvent;
import org.spout.api.event.player.PlayerLoginEvent;
import org.spout.api.plugin.CommonPlugin;

import com.niccholaspage.Metro.base.player.players.SpoutPlayer;

public class FeSpoutPlayerListener implements Listener {
	private final Fe fe;
	
	public FeSpoutPlayerListener(CommonPlugin plugin, Fe fe){
		this.fe = fe;
		
		plugin.getEngine().getEventManager().registerEvents(this, plugin);
	}
	
	@EventHandler(order = Order.EARLIEST)
	public void onPlayerLogin(PlayerLoginEvent event){
		fe.getAPI().createAccount(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		
		if (!fe.isUpdated() && player.hasPermission("fe.notify")){
			Phrase.FE_OUTDATED.sendWithPrefix(new SpoutPlayer(player), fe.getLatestVersionString());
		}
	}
}
