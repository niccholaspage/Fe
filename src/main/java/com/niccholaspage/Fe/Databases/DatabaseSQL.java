package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.API.Account;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DatabaseSQL extends DatabaseGeneric
{
	private final boolean supportsModification;
	private final long pingDelay       = 20 * 60;
	private String tableAccounts       = "fe_accounts";
	private String tableVersion        = "fe_version";
	private String columnAccountsUser  = "name";
	private String columnAccountsUUID  = "uuid";
	private String columnAccountsMoney = "money";
	private Connection connection;
	protected abstract Connection getNewConnection();
	public DatabaseSQL(Fe plugin, boolean supportsModification)
	{
		super(plugin);
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
	@Override
	public boolean init()
	{
		return checkConnection();
	}
	@Override
	public void close()
	{
		try
		{
			if(connection != null)
				connection.close();
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public List<Account> loadAccounts()
	{
		checkConnection();
		try(ResultSet set = connection.createStatement().executeQuery("SELECT * FROM `" + tableAccounts + "`;"))
		{
			while(set.next())
				fromResultSet(set);
		} catch(SQLException e) {
			System.out.println(e);
		}
		return new ArrayList(accounts.values());
	}
	private Account fromResultSet(ResultSet set) throws SQLException
	{
		final String name = set.getString(columnAccountsUser);
		final String uuid = set.getString(columnAccountsUUID);
		final Account account = new Account(plugin, this,
			name, uuid != null ? UUID.fromString(uuid) : null,
			set.getDouble(columnAccountsMoney));
		if(name != null && !"".equals(name))
			accounts.put(name, account);
		if(uuid != null && !"".equals(uuid))
			accounts.put(uuid, account);
		return account;
	}
	@Override
	public int getVersion()
	{
		checkConnection();
		int version = 0;
		try(ResultSet set = connection.prepareStatement("SELECT * from " + tableVersion).executeQuery())
		{
			if(set.next())
				version = set.getInt("version");
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
			query("DELETE FROM `" + tableVersion + "`;");
			query("INSERT INTO `" + tableVersion + "` (`version`) VALUES ('" + version + "');");
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public void saveAccount(Account account)
	{
		checkConnection();
		final String strname = account.getName();
		final String strblnc = String.valueOf(account.getMoney());
		String query;
		if(account.getUUID() != null)
		{
			final String struuid = account.getUUID().toString();
			query = "INSERT INTO `" + tableAccounts + "` (`" + columnAccountsUser + "`, `" + columnAccountsUUID + "`, `" + columnAccountsMoney + "`) "
				+ "VALUES ('" + strname + "', '" + struuid + "', '" + strblnc + "') ON DUPLICATE KEY UPDATE "
				+ "`" + columnAccountsUser  + "` = VALUES(`" + columnAccountsUser  + "`), "
				+ "`" + columnAccountsUUID  + "` = VALUES(`" + columnAccountsUUID  + "`), "
				+ "`" + columnAccountsMoney + "` = VALUES(`" + columnAccountsMoney + "`);";
		} else {
			query = "INSERT INTO `" + tableAccounts + "` (`" + columnAccountsUser + "`, `" + columnAccountsMoney + "`) "
				+ "VALUES ('" + strname + "', '" + strblnc + "') ON DUPLICATE KEY UPDATE "
				+ "`" + columnAccountsUser  + "` = VALUES(`" + columnAccountsUser  + "`), "
				+ "`" + columnAccountsMoney + "` = VALUES(`" + columnAccountsMoney + "`);";
		}
		try
		{
			query(query);
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public void reloadAccount(Account account)
	{
		checkConnection();
		final UUID uuid = account.getUUID();
		final String query = uuid != null
			? "SELECT * FROM `" + tableAccounts + "` WHERE `" + columnAccountsUUID + "` = '" + uuid.toString()   + "';"
			: "SELECT * FROM `" + tableAccounts + "` WHERE `" + columnAccountsUser + "` = '" + account.getName() + "';";
		try(ResultSet set = connection.createStatement().executeQuery(query))
		{
			if(set.next())
				fromResultSet(set);
		} catch(SQLException ex) {
		}
	}
	@Override
	public void removeAccount(Account account)
	{
		super.removeAccount(account);
		checkConnection();
		try
		{
			query(account.getUUID() != null
				? "DELETE FROM `" + tableAccounts + "` WHERE `" + columnAccountsUUID + "` = '" + account.getUUID().toString() + "';"
				: "DELETE FROM `" + tableAccounts + "` WHERE `" + columnAccountsUser + "` = '" + account.getName() + "';");
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public void cleanAccountsWithDefaultHoldings()
	{
		checkConnection();
		try
		{
			query("DELETE FROM `" + tableAccounts + "` WHERE `" + columnAccountsMoney + "` = '" + plugin.api.getDefaultHoldings() + "';");
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	@Override
	public void removeAllAccounts()
	{
		checkConnection();
		try
		{
			query("DELETE FROM `" + tableAccounts + "`;");
		} catch(SQLException e) {
			System.out.println(e);
		}
	}
	protected void setAccountTable(String accountsName)
	{
		this.tableAccounts = accountsName;
	}
	protected void setVersionTable(String versionName)
	{
		this.tableVersion = versionName;
	}
	protected void setAccountsColumnUser(String accountsColumnUser)
	{
		this.columnAccountsUser = accountsColumnUser;
	}
	protected void setAccountsColumnUUID(String accountsColumnUUID)
	{
		this.columnAccountsUUID = accountsColumnUUID;
	}
	protected void setAccountsColumnMoney(String accountsColumnMoney)
	{
		this.columnAccountsMoney = accountsColumnMoney;
	}
	private synchronized boolean checkConnection()
	{
		try
		{
			if(connection == null || connection.isClosed())
			{
				connection = getNewConnection();
				if(connection == null || connection.isClosed())
					return false;
				boolean newDatabase;
				try(ResultSet set = connection.prepareStatement(supportsModification
					? "SHOW TABLES LIKE '" + tableAccounts + "';"
					: "SELECT `name` FROM `sqlite_master` WHERE `type` = 'table' AND `name` = '" + tableAccounts + "';")
					.executeQuery())
				{
					newDatabase = set.next();
				}
				query("CREATE TABLE IF NOT EXISTS `" + tableAccounts + "` (`" + columnAccountsUser + "` VARCHAR(16) NOT NULL, `" + columnAccountsUUID + "` CHAR(36), `" + columnAccountsMoney + "` DOUBLE NOT NULL);");
				query("CREATE TABLE IF NOT EXISTS `" + tableVersion  + "` (`version` INT NOT NULL);");
				if(newDatabase)
				{
					int version = getVersion();
					if(version == 0)
					{
						if(supportsModification)
						{
							query("ALTER TABLE `" + tableAccounts + "` MODIFY `" + columnAccountsUser  + "` VARCHAR(16) NOT NULL");
							query("ALTER TABLE `" + tableAccounts + "` MODIFY `" + columnAccountsMoney + "` DOUBLE NOT NULL");
						}
						try
						{
							query("ALTER TABLE `" + tableAccounts + "` ADD `" + columnAccountsUUID + "` CHAR(36);");
						} catch(Exception e) {
						}
						/*
						if(!convertToUUID())
							return false;
						*/
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
	private synchronized boolean query(String sql) throws SQLException
	{
		return connection.createStatement().execute(sql);
	}
}
