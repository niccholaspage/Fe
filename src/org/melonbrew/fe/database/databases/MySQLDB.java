package org.melonbrew.fe.database.databases;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;

public class MySQLDB extends SQLDB {
	private final Fe plugin;
	
	public MySQLDB(Fe plugin){
		super(plugin);
		
		this.plugin = plugin;
	}
	
	protected Database getNewDatabase(){
		ConfigurationSection config = plugin.getMySQLConfig();

		MySQL mySQL = new MySQL(plugin.getLogger(), "Fe", config.getString("host"), config.getString("port"), config.getString("database"), config.getString("user"), config.getString("password"));
		
		return mySQL;
	}
}
