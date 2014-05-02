package org.melonbrew.fe.database.databases;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;

public class SQLiteDB extends SQLDB {
	private final Fe plugin;

	public SQLiteDB(Fe plugin){
		super(plugin, false);

		this.plugin = plugin;
	}

	public Connection getNewConnection(){
		try {
			Class.forName("org.sqlite.JDBC");

			return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "database.db").getAbsolutePath());
		} catch (Exception e){
			return null;
		}
	}

	public void getConfigDefaults(ConfigurationSection section){

	}

	public String getName(){
		return "SQLite";
	}
}
