package com.niccholaspage.Fe.Databases;

import org.bukkit.configuration.ConfigurationSection;
import com.niccholaspage.Fe.Fe;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDB extends DatabaseSQL
{
	public SQLiteDB(Fe plugin)
	{
		super(plugin, false);
	}
	@Override
	public String getName()
	{
		return "SQLite";
	}
	@Override
	public boolean isAsync()
	{
		return false;
	}
	@Override
	public Connection getNewConnection()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection(
				"jdbc:sqlite:" + new File(plugin.getDataFolder(), "database.db").getAbsolutePath());
		} catch(ClassNotFoundException | SQLException e) {
			return null;
		}
	}
	@Override
	public void getConfigDefaults(ConfigurationSection section)
	{
	}
}
