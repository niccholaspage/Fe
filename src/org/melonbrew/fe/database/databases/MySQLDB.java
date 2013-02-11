package org.melonbrew.fe.database.databases;

import java.sql.Connection;

import org.melonbrew.fe.Fe;

import com.niccholaspage.Metro.base.config.ConfigSection;
import com.niccholaspage.nSQL.connection.MySQLConnection;

public class MySQLDB extends SQLDB {
	public MySQLDB(Fe plugin){
		super(plugin, true);
	}
	
	protected Connection getNewConnection(){
		ConfigSection config = getConfigSection();
		
		MySQLConnection mySQL = new MySQLConnection(config.getString("host"), config.getString("port"), config.getString("database"), config.getString("user"), config.getString("password"));
		
		setAccountTable(config.getString("tables.accounts"));
		
		return mySQL.getConnection();
	}
	
	public void getConfigDefaults(ConfigSection section){
		section.addDefault("host", "localhost");
		
		section.addDefault("port", 3306);
		
		section.addDefault("user", "root");
		
		section.addDefault("password", "minecraft");
		
		section.addDefault("database", "Fe");
		
		ConfigSection tables = section.getConfigSection("tables");
		
		if (tables == null){
			tables = section.createConfigSection("tables");
		}
		
		tables.addDefault("accounts", "fe_accounts");
	}
	
	public String getName(){
		return "MySQL";
	}
}
