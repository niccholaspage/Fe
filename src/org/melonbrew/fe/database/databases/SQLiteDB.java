package org.melonbrew.fe.database.databases;

import java.io.File;
import java.sql.Connection;

import org.melonbrew.fe.Fe;

import com.niccholaspage.Metro.base.config.ConfigSection;
import com.niccholaspage.nSQL.connection.SQLiteConnection;

public class SQLiteDB extends SQLDB {
	private final Fe plugin;
	
	public SQLiteDB(Fe plugin){
		super(plugin, false);
		
		this.plugin = plugin;
	}
	
	public Connection getNewConnection(){
		return new SQLiteConnection(new File(plugin.getDataFolder().getPath(), "database.db")).getConnection();
	}
	
	public void getConfigDefaults(ConfigSection section){
		
	}
	
	public String getName(){
		return "SQLite";
	}
}
