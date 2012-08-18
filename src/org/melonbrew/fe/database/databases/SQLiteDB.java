package org.melonbrew.fe.database.databases;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.SQLibrary.Database;
import org.melonbrew.fe.SQLibrary.SQLite;

public class SQLiteDB extends SQLDB {
	private final Fe plugin;
	
	public SQLiteDB(Fe plugin){
		super(plugin, false);
		
		this.plugin = plugin;
	}
	
	public Database getNewDatabase(){
		return new SQLite(plugin.getLogger(), "Fe", "database", plugin.getDataFolder().getPath());
	}
	
	public void getConfigDefaults(ConfigurationSection section){
		
	}
}
