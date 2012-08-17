package org.melonbrew.fe.database.databases;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.SQLite;

import org.melonbrew.fe.Fe;

public class SQLiteDB extends SQLDB {
	private final Fe plugin;
	
	public SQLiteDB(Fe plugin){
		super(plugin, false);
		
		this.plugin = plugin;
	}
	
	public Database getNewDatabase(){
		return new SQLite(plugin.getLogger(), "Fe", "database", plugin.getDataFolder().getPath());
	}
}
