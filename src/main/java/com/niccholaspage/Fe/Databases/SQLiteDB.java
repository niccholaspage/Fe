package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.Fe;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.configuration.ConfigurationSection;

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
	@Override
	protected String getSaveAccountQuery(Account account)
	{
		final String strName = account.getName();
		final String strBlnc = String.valueOf(account.getMoney());
		if(account.getUUID() != null)
		{
			final String struuid = account.getUUID().toString();
			return "INSERT OR REPLACE INTO `" + tableAccounts + "` (`" + columnAccountsUser + "`, `" + columnAccountsUUID + "`, `" + columnAccountsMoney + "`) "
				+ "VALUES ('" + strName + "', '" + struuid + "', '" + strBlnc + "');";
		}
		return "INSERT OR REPLACE INTO `" + tableAccounts + "` (`" + columnAccountsUser + "`, `" + columnAccountsMoney + "`) "
			+ "VALUES ('" + strName + "', '" + strBlnc + "');";
	}
}
