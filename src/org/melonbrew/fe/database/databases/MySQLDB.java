package org.melonbrew.fe.database.databases;

import java.sql.Connection;
import java.sql.DriverManager;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;

public class MySQLDB extends SQLDB {
	public MySQLDB(Fe plugin){
		super(plugin, true);
	}

	protected Connection getNewConnection(){
		ConfigurationSection config = getConfigSection();

		setAccountTable(config.getString("tables.accounts"));

		try {
			Class.forName("com.mysql.jdbc.Driver");

			String url = "jdbc:mysql://" + config.getString("host") + ":" + config.getString("port") + "/" + config.getString("database");

			return DriverManager.getConnection(url, config.getString("user"), config.getString("password"));
		} catch (Exception e){
			return null;
		}
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
	}

	public String getName(){
		return "MySQL";
	}
}
