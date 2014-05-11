package org.melonbrew.fe.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

public class FePlayerListener implements Listener {
    private final Fe plugin;

    public FePlayerListener(Fe plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        plugin.getAPI().createAccount(player.getName(), player.getUniqueId().toString());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.isUpdated() && player.hasPermission("fe.notify")) {
            Phrase.FE_OUTDATED.sendWithPrefix(player, plugin.getLatestVersionString());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Database database = plugin.getFeDatabase();

        Player player = event.getPlayer();

        Account account = database.getCachedAccount(player.getName(), player.getUniqueId().toString());

        if (account != null) {
            account.save(account.getMoney());

            database.removeCachedAccount(account);
        }
    }
}
