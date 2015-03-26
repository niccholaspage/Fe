package com.niccholaspage.Fe.database.databases;

import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.database.Account;
import com.niccholaspage.Fe.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class SQLDB extends Database
{
	private final Fe plugin;

	private final boolean supportsModification;

	private Connection connection;

	private String accountsName;

	private String versionName;

	private String accountsColumnUser;

	private String accountsColumnMoney;

	private String accountsColumnUUID;

	public SQLDB(Fe plugin, boolean supportsModification)
	{
		super(plugin);

		this.plugin = plugin;

		this.supportsModification = supportsModification;

		accountsName = "fe_accounts";

		versionName = "fe_version";

		accountsColumnUser = "name";

		accountsColumnMoney = "money";

		accountsColumnUUID = "uuid";

		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(connection != null &&  ! connection.isClosed())
						connection.createStatement().execute("/* ping */ SELECT 1");
				} catch(SQLException e)
				{
					connection = getNewConnection();
				}
			}
		}, 60 * 20, 60 * 20);
	}

	public void setAccountTable(String accountsName)
	{
		this.accountsName = accountsName;
	}

	public void setVersionTable(String versionName)
	{
		this.versionName = versionName;
	}

	public void setAccountsColumnUser(String accountsColumnUser)
	{
		this.accountsColumnUser = accountsColumnUser;
	}

	public void setAccountsColumnMoney(String accountsColumnMoney)
	{
		this.accountsColumnMoney = accountsColumnMoney;
	}

	public void setAccountsColumnUUID(String accountsColumnUUID)
	{
		this.accountsColumnUUID = accountsColumnUUID;
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

				ResultSet set = connection.prepareStatement(supportsModification ? ("SHOW TABLES LIKE '" + accountsName + "'") : "SELECT name FROM sqlite_master WHERE type='table' AND name='" + accountsName + "'").executeQuery();

				boolean newDatabase = set.next();

				set.close();

				query("CREATE TABLE IF NOT EXISTS " + accountsName + " (" + accountsColumnUser + " varchar(64) NOT NULL, " + accountsColumnUUID + " varchar(36), " + accountsColumnMoney + " double NOT NULL)");

				query("CREATE TABLE IF NOT EXISTS " + versionName + " (version int NOT NULL)");

				if(newDatabase)
				{
					int version = getVersion();

					if(version == 0)
					{
						if(supportsModification)
						{
							query("ALTER TABLE " + accountsName + " MODIFY " + accountsColumnUser + " varchar(64) NOT NULL");

							query("ALTER TABLE " + accountsName + " MODIFY " + accountsColumnMoney + " double NOT NULL");
						}

						try
						{
							query("ALTER TABLE " + accountsName + " ADD " + accountsColumnUUID + " varchar(36);");
						} catch(Exception e)
						{

						}

						if( ! convertToUUID())
							return false;

						setVersion(1);
					}
				} else
					setVersion(1);
			}
		} catch(SQLException e)
		{
			e.printStackTrace();

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
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public int getVersion()
	{
		checkConnection();

		int version = 0;

		try
		{
			ResultSet set = connection.prepareStatement("SELECT * from " + versionName).executeQuery();

			if(set.next())
				version = set.getInt("version");

			set.close();

			return version;
		} catch(Exception e)
		{
			e.printStackTrace();

			return version;
		}
	}

	@Override
	public void setVersion(int version)
	{
		checkConnection();

		try
		{
			connection.prepareStatement("DELETE FROM " + versionName).executeUpdate();

			connection.prepareStatement("INSERT INTO " + versionName + " (version) VALUES (" + version + ")").executeUpdate();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public List<Account> loadTopAccounts(int size)
	{
		checkConnection();

		String sql = "SELECT * FROM " + accountsName + " ORDER BY money DESC limit " + size;

		List<Account> topAccounts = new ArrayList<>();

		try
		{
			ResultSet set = connection.createStatement().executeQuery(sql);

			while(set.next())
			{
				Account account = new Account(plugin, set.getString(accountsColumnUser), set.getString(accountsColumnUUID), this);

				account.setMoney(set.getDouble(accountsColumnMoney));

				topAccounts.add(account);
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
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
			ResultSet set = connection.createStatement().executeQuery("SELECT * from " + accountsName);

			while(set.next())
			{
				Account account = new Account(plugin, set.getString(accountsColumnUser), set.getString(accountsColumnUUID), this);

				account.setMoney(set.getDouble(accountsColumnMoney));

				accounts.add(account);
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
		}

		return accounts;
	}

	@SuppressWarnings("deprecation")
	@Override
	public HashMap<String, String> loadAccountData(String name, String uuid)
	{
		checkConnection();

		try
		{
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + accountsName + " WHERE UPPER(" + (uuid != null ? accountsColumnUUID : accountsColumnUser) + ") LIKE UPPER(?)");

			statement.setString(1, uuid != null ? uuid : name);

			ResultSet set = statement.executeQuery();

			HashMap<String, String> data = new HashMap<String, String>();

			while(set.next())
			{
				data.put("money", set.getString(accountsColumnMoney));
				data.put("name", set.getString(accountsColumnUser));
			}

			set.close();

			return data;
		} catch(SQLException e)
		{
			e.printStackTrace();

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
			statement = connection.prepareStatement("DELETE FROM " + accountsName + " WHERE UPPER(" + (uuid != null ? accountsColumnUUID : accountsColumnUser) + ") LIKE UPPER(?)");

			statement.setString(1, uuid != null ? uuid : name);

			statement.execute();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void saveAccount(String name, String uuid, double money)
	{
		checkConnection();

		try
		{
			String sql = "UPDATE " + accountsName + " SET " + accountsColumnMoney + "=?, " + accountsColumnUser + "=? WHERE UPPER(";

			if(uuid != null)
				sql += accountsColumnUUID;
			else
				sql += accountsColumnUser;

			PreparedStatement statement = connection.prepareStatement(sql + ") LIKE UPPER(?)");

			statement.setDouble(1, money);

			statement.setString(2, name);

			if(uuid != null)
				statement.setString(3, uuid);
			else
				statement.setString(3, name);

			if(statement.executeUpdate() == 0)
			{
				statement = connection.prepareStatement("INSERT INTO " + accountsName + " (" + accountsColumnUser + ", " + accountsColumnUUID + ", " + accountsColumnMoney + ") VALUES (?, ?, ?)");

				statement.setString(1, name);

				statement.setString(2, uuid);

				statement.setDouble(3, money);

				statement.execute();
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void clean()
	{
		checkConnection();

		try
		{
			ResultSet set = connection.prepareStatement("SELECT * from " + accountsName + " WHERE " + accountsColumnMoney + "=" + plugin.getAPI().getDefaultHoldings()).executeQuery();

			boolean executeQuery = false;

			StringBuilder builder = new StringBuilder("DELETE FROM " + accountsName + " WHERE " + accountsColumnUser + " IN (");

			while(set.next())
			{
				String name = set.getString(accountsColumnUser);

				if(plugin.getServer().getPlayerExact(name) != null)
					continue;

				executeQuery = true;

				builder.append("'").append(name).append("', ");
			}

			set.close();

			builder.delete(builder.length() - 2, builder.length()).append(")");

			if(executeQuery)
				query(builder.toString());
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public void removeAllAccounts()
	{
		super.removeAllAccounts();

		checkConnection();

		try
		{
			connection.prepareStatement("DELETE FROM " + accountsName).executeUpdate();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
