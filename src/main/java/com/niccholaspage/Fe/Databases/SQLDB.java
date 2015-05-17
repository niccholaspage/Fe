package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class SQLDB extends Database
{
	protected final Fe plugin;
	private final boolean supportsModification;
	private final long pingDelay       = 60 * 20;
	private String tableAccounts       = "fe_accounts";
	private String tableVersion        = "fe_version";
	private String columnAccountsUser  = "name";
	private String columnAccountsMoney = "money";
	private String columnAccountsUUID  = "uuid";
	private Connection connection;
	public SQLDB(Fe plugin, boolean supportsModification)
	{
		super(plugin);
		this.plugin = plugin;
		this.supportsModification = supportsModification;
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(connection != null && !connection.isClosed())
						connection.createStatement().execute("/* ping */ SELECT 1;");
				} catch(SQLException e) {
					connection = getNewConnection();
				}
			}
		}, pingDelay, pingDelay);
	}
	public void setAccountTable(String accountsName)
	{
		this.tableAccounts = accountsName;
	}
	public void setVersionTable(String versionName)
	{
		this.tableVersion = versionName;
	}
	public void setAccountsColumnUser(String accountsColumnUser)
	{
		this.columnAccountsUser = accountsColumnUser;
	}
	public void setAccountsColumnMoney(String accountsColumnMoney)
	{
		this.columnAccountsMoney = accountsColumnMoney;
	}
	public void setAccountsColumnUUID(String accountsColumnUUID)
	{
		this.columnAccountsUUID = accountsColumnUUID;
	}
	@Override
	public boolean init()
	{
		super.init();
		return checkConnection();
	}
	public boolean checkConnection()
	{
		try
		{
			if(connection == null || connection.isClosed())
			{
				connection = getNewConnection();
				if(connection == null || connection.isClosed())
					return false;
				ResultSet set = connection.prepareStatement(supportsModification ? ("SHOW TABLES LIKE '" + tableAccounts + "'") : "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableAccounts + "'").executeQuery();
				boolean newDatabase = set.next();
				set.close();
				query("CREATE TABLE IF NOT EXISTS " + tableAccounts + " (" + columnAccountsUser + " varchar(64) NOT NULL, " + columnAccountsUUID + " varchar(36), " + columnAccountsMoney + " double NOT NULL)");
				query("CREATE TABLE IF NOT EXISTS " + tableVersion + " (version int NOT NULL)");
				if(newDatabase)
				{
					int version = getVersion();
					if(version == 0)
					{
						if(supportsModification)
						{
							query("ALTER TABLE " + tableAccounts + " MODIFY " + columnAccountsUser + " varchar(64) NOT NULL");
							query("ALTER TABLE " + tableAccounts + " MODIFY " + columnAccountsMoney + " double NOT NULL");
						}
						try
						{
							query("ALTER TABLE " + tableAccounts + " ADD " + columnAccountsUUID + " char(36);");
						} catch(Exception e) {
						}
						if(!convertToUUID())
							return false;
						setVersion(1);
					}
				} else
					setVersion(1);
			}
		} catch(SQLException e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	protected abstract Connection getNewConnection();
	public boolean query(String sql) throws SQLException
	{
		return connection.createStatement().execute(sql);
	}
	@Override
	public void close()
	{
		super.close();
		try
		{
			if(connection != null)
				connection.close();
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public int getVersion()
	{
		checkConnection();
		int version = 0;
		try
		{
			ResultSet set = connection.prepareStatement("SELECT * from " + tableVersion).executeQuery();
			if(set.next())
				version = set.getInt("version");
			set.close();
			return version;
		} catch(Exception e) {
			System.out.println(e);
			return version;
		}
	}
	@Override
	public void setVersion(int version)
	{
		checkConnection();
		try
		{
			connection.prepareStatement("DELETE FROM " + tableVersion).executeUpdate();
			connection.prepareStatement("INSERT INTO " + tableVersion + " (version) VALUES (" + version + ")").executeUpdate();
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public List<Account> loadTopAccounts(int size)
	{
		checkConnection();
		String sql = "SELECT * FROM " + tableAccounts + " ORDER BY money DESC limit " + size;
		List<Account> topAccounts = new ArrayList<>();
		try
		{
			ResultSet set = connection.createStatement().executeQuery(sql);
			while(set.next())
			{
				Account account = new Account(plugin, set.getString(columnAccountsUser), set.getString(columnAccountsUUID), this);
				account.setMoney(set.getDouble(columnAccountsMoney));
				topAccounts.add(account);
			}
		} catch(SQLException e) {
			System.out.println(e);
		}
		return topAccounts;
	}
	@Override
	public List<Account> getAccounts()
	{
		checkConnection();
		List<Account> accounts = new ArrayList<>();
		try
		{
			ResultSet set = connection.createStatement().executeQuery("SELECT * from " + tableAccounts);
			while(set.next())
			{
				Account account = new Account(plugin, set.getString(columnAccountsUser), set.getString(columnAccountsUUID), this);
				account.setMoney(set.getDouble(columnAccountsMoney));
				accounts.add(account);
			}
		} catch(SQLException e) {
			System.out.println(e);
		}
		return accounts;
	}
	@Override
	public HashMap<String, String> loadAccountData(String name, String uuid)
	{
		checkConnection();
		try
		{
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableAccounts + " WHERE UPPER(" + (uuid != null ? columnAccountsUUID : columnAccountsUser) + ") LIKE UPPER(?)");
			statement.setString(1, uuid != null ? uuid : name);
			try(ResultSet set = statement.executeQuery())
			{
				HashMap<String, String> data = new HashMap<>();
				while(set.next())
				{
					data.put("money", set.getString(columnAccountsMoney));
					data.put("name", set.getString(columnAccountsUser));
				}
			return data;
			}
		} catch(SQLException e) {
			System.out.println(e);
			return null;
		}
	}
	@Override
	public void removeAccount(String name, String uuid)
	{
		super.removeAccount(name, uuid);
		checkConnection();
		PreparedStatement statement;
		try
		{
			statement = connection.prepareStatement("DELETE FROM " + tableAccounts + " WHERE UPPER(" + (uuid != null ? columnAccountsUUID : columnAccountsUser) + ") LIKE UPPER(?)");
			statement.setString(1, uuid != null ? uuid : name);
			statement.execute();
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public void saveAccount(String name, String uuid, double money)
	{
		checkConnection();
		try
		{
			PreparedStatement statement = connection.prepareStatement(
				"UPDATE " + tableAccounts + " SET " + columnAccountsMoney + "=?, " + columnAccountsUser + "=? WHERE UPPER("
				+ (uuid != null ? columnAccountsUUID : columnAccountsUser)
				+ ") LIKE UPPER(?)");
			statement.setDouble(1, money);
			statement.setString(2, name);
			statement.setString(3, uuid != null ? uuid : name);
			if(statement.executeUpdate() == 0)
			{
				statement = connection.prepareStatement(
					"INSERT INTO " + tableAccounts + " (" + columnAccountsUser + ", " + columnAccountsUUID + ", " + columnAccountsMoney + ") VALUES (?, ?, ?)");
				statement.setString(1, name);
				statement.setString(2, uuid);
				statement.setDouble(3, money);
				statement.execute();
			}
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public void clean()
	{
		checkConnection();
		try
		{
			ResultSet set = connection.prepareStatement("SELECT * from " + tableAccounts + " WHERE " + columnAccountsMoney + "=" + plugin.getAPI().getDefaultHoldings()).executeQuery();
			boolean executeQuery = false;
			StringBuilder builder = new StringBuilder("DELETE FROM " + tableAccounts + " WHERE " + columnAccountsUser + " IN (");
			while(set.next())
			{
				String name = set.getString(columnAccountsUser);
				if(plugin.getServer().getPlayerExact(name) != null)
					continue;
				executeQuery = true;
				builder.append("'").append(name).append("', ");
			}
			set.close();
			builder.delete(builder.length() - 2, builder.length()).append(")");
			if(executeQuery)
				query(builder.toString());
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public void removeAllAccounts()
	{
		super.removeAllAccounts();
		checkConnection();
		try
		{
			connection.prepareStatement("DELETE FROM " + tableAccounts).executeUpdate();
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
}
