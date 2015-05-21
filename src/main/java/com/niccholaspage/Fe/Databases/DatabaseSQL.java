package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.Fe;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DatabaseSQL extends DatabaseGeneric
{
	private final boolean supportsModification;
	private final long pingDelay         = 20 * 60;
	protected String tableAccounts       = "fe_accounts";
	protected String tableVersion        = "fe_version";
	protected String columnAccountsUser  = "name";
	protected String columnAccountsUUID  = "uuid";
	protected String columnAccountsMoney = "money";
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
	public boolean initialize()
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
		final AccountInt account = new AccountInt(plugin, this,
			name, uuid != null ? UUID.fromString(uuid) : null,
			set.getDouble(columnAccountsMoney));
		if(name != null && !"".equals(name))
			accounts.put(name, account);
		if(uuid != null && !"".equals(uuid))
			accounts.put(uuid, account);
		return account;
	}
	protected abstract String getSaveAccountQuery(Account account);
	@Override
	public void saveAccount(Account account)
	{
		checkConnection();
		try
		{
			query(getSaveAccountQuery(account));
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
			{
				final AccountInt internal = (AccountInt)account;
				final String dbuuid = set.getString(columnAccountsUUID);
				final String dbname = set.getString(columnAccountsUser);
				internal.uuid  = dbuuid != null ? UUID.fromString(dbuuid) : internal.uuid;
				internal.name  = dbname != null ? dbname : internal.name;
				internal.money = set.getDouble(columnAccountsMoney);
				if(internal.name != null && !"".equals(internal.name))
					accounts.put(dbname, account);
				if(internal.uuid != null)
					accounts.put(internal.uuid.toString(), account);
			}
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
				// Add `id` column
				try
				{
					query("ALTER TABLE `" + tableAccounts + "` ADD COLUMN `id` INT UNSIGNED NOT NULL AUTO_INCREMENT FIRST, ADD PRIMARY KEY (`id`), ADD UNIQUE INDEX `id_UNIQUE` (`id` ASC);");
				} catch(SQLException e) {
					// `id` already exists
				}
				// Add UNIQUE KEY for UUID
				try
				{
					query("ALTER TABLE `" + tableAccounts + "` ADD UNIQUE INDEX `uuid_UNIQUE` (`" + columnAccountsUUID + "` ASC);");
				} catch(SQLException e) {
					// `uuid_UNIQUE` already exists
				}
				// Add UNIQUE KEY for nickname
				try
				{
					query("ALTER TABLE `" + tableAccounts + "` ADD UNIQUE INDEX `name_UNIQUE` (`" + columnAccountsUser + "` ASC);");
				} catch(SQLException e) {
					// `name_UNIQUE` already exists
				}
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
	private int getVersion()
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
	private void setVersion(int version)
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
}
