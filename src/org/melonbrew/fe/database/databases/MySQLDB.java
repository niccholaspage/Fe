package org.melonbrew.fe.database.databases;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.SQLibrary.Database;
import org.melonbrew.fe.SQLibrary.MySQL;

public class MySQLDB extends SQLDB {
	private final Fe plugin;
	
	public MySQLDB(Fe plugin){
		super(plugin, true);
		
		this.plugin = plugin;
	}
	
	protected Database getNewDatabase(){
		ConfigurationSection config = getConfigSection();

		MySQL mySQL = new MySQL(plugin.getLogger(), "Fe", config.getString("host"), config.getString("port"), config.getString("database"), config.getString("user"), config.getString("password"));
		
		setAccountTable(config.getString("tables.accounts"));
		
		return mySQL;
	}
	
	public void getConfigDefaults(ConfigurationSection section){
		section.addDefault("host", "localhost");
		
		section.addDefault("port", 3306);
		
		section.addDefault("user", "root");
		
		section.addDefault("password", "minecraft");
		
		section.addDefault("database", "Fe");
		
		ConfigurationSection tables = section.getConfigurationSection("tables");
		
		if (tables == null){
			tables = section.createSection("tables");
		}
		
		tables.addDefault("accounts", "fe_accounts");
		tables.addDefault("pname", "name");
		tables.addDefault("pbalance", "money");
	}
	
	public String getName(){
		return "MySQL";
	}
}
