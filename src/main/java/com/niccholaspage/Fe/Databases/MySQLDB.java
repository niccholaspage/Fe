package com.niccholaspage.Fe.Databases;

import org.bukkit.configuration.ConfigurationSection;
import com.niccholaspage.Fe.Fe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDB extends DatabaseSQL
{
	public MySQLDB(Fe plugin)
	{
		super(plugin, true);
	}
	@Override
	public String getName()
	{
		return "MySQL";
	}
	@Override
	public boolean isAsync()
	{
		return true;
	}
	@Override
	protected Connection getNewConnection()
	{
		final ConfigurationSection config = getConfigSection();
		setAccountTable       (config.getString("tables.accounts"));
		setAccountsColumnUser (config.getString("columns.accounts.username"));
		setAccountsColumnMoney(config.getString("columns.accounts.money"));
		setAccountsColumnUUID (config.getString("columns.accounts.uuid"));
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection(
				"jdbc:mysql://" + config.getString("connection.database"),
				config.getString("connection.username"),
				config.getString("connection.password"));
		} catch(ClassNotFoundException | SQLException e) {
			return null;
		}
	}
	private ConfigurationSection getSection(ConfigurationSection parent, String childName)
	{
		ConfigurationSection child = parent.getConfigurationSection(childName);
		if(child == null)
			child = parent.createSection(childName);
		return child;
	}
	@Override
	public void getConfigDefaults(ConfigurationSection section)
	{
		section.addDefault("host", "localhost");
		section.addDefault("port", 3306);
		section.addDefault("user", "root");
		section.addDefault("password", "minecraft");
		section.addDefault("database", "Fe");
		ConfigurationSection tables = getSection(section, "tables");
		tables.addDefault("accounts", "fe_accounts");
		ConfigurationSection columns = getSection(section, "columns");
		ConfigurationSection columnsAccounts = getSection(columns, "accounts");
		columnsAccounts.addDefault("username", "name");
		columnsAccounts.addDefault("money", "money");
		columnsAccounts.addDefault("uuid", "uuid");
	}
}
