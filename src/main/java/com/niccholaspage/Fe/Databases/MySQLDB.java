package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.Fe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.configuration.ConfigurationSection;

public class MySQLDB extends DatabaseSQL implements Runnable
{
	private final LinkedBlockingQueue<DefferedTask> queue = new LinkedBlockingQueue<>();
	private Thread dispatcher;
	private boolean maximumConsistency = true;
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
	public boolean initialize()
	{
		maximumConsistency = getConfigSection().getBoolean("maximum-consistency", true);
		if(super.initialize())
		{
			dispatcher = new Thread(this);
			dispatcher.start();
			return true;
		}
		return false;
	}
	@Override
	public void close()
	{
		if(dispatcher != null)
		{
			try
			{
				dispatcher.interrupt();
				dispatcher.join();
				dispatcher = null;
			} catch(InterruptedException e) {
			}
		}
		processActiveTasks();
		super.close();
	}
	@Override
	public void run()
	{
		try
		{
			for(DefferedTask task = queue.take(); task != null; task = queue.take())
				task.run();
		} catch(InterruptedException e) {
		}
	}
	@Override
	public void saveAccount(Account account)
	{
		queue.offer(new DefferedTask(account)
		{
			@Override
			public void run()
			{
				MySQLDB.super.saveAccount(getAccount());
			}
		});
	}
	@Override
	public Account getAccount(UUID uuid)
	{
		final Account account = super.getAccount(uuid);
		if(maximumConsistency && account != null)
			super.reloadAccount(account);
		return account;
	}
	@Override
	public Account getAccount(String name)
	{
		final Account account = super.getAccount(name);
		if(maximumConsistency && account != null)
			super.reloadAccount(account);
		return account;
	}
	@Override
	public void reloadAccount(Account account)
	{
		processActiveTasks();
		queue.offer(new DefferedTask(account)
		{
			@Override
			public void run()
			{
				MySQLDB.super.reloadAccount(getAccount());
			}
		});
	}
	@Override
	public void removeAccount(Account account)
	{
		processActiveTasks();
		queue.offer(new DefferedTask(account)
		{
			@Override
			public void run()
			{
				MySQLDB.super.removeAccount(getAccount());
			}
		});
	}
	private synchronized int processActiveTasks()
	{
		int result = 0;
		for(DefferedTask task = queue.poll(); task != null; task = queue.poll())
		{
			task.run();
			result += 1;
		}
		return result;
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
	@Override
	public void getConfigDefaults(ConfigurationSection section)
	{
		final ConfigurationSection tables = getSection(section, "tables");
		tables.addDefault("accounts", "fe_accounts");
		final ConfigurationSection columns = getSection(section, "columns");
		final ConfigurationSection columnsAccounts = getSection(columns, "accounts");
		columnsAccounts.addDefault("username", "name");
		columnsAccounts.addDefault("money", "money");
		columnsAccounts.addDefault("uuid", "uuid");
		final ConfigurationSection connection = getSection(section, "connection");
		connection.addDefault("database", "localhost:3306/minecraft");
		connection.addDefault("username", "user");
		connection.addDefault("password", "pass");
		section.addDefault("maximum-consistency", true);
	}
	private ConfigurationSection getSection(ConfigurationSection parent, String childName)
	{
		ConfigurationSection child = parent.getConfigurationSection(childName);
		if(child == null)
			child = parent.createSection(childName);
		return child;
	}
	@Override
	protected String getSaveAccountQuery(Account account)
	{
		final String strName = account.getName();
		final String strBlnc = String.valueOf(account.getMoney());
		if(account.getUUID() != null)
		{
			final String struuid = account.getUUID().toString();
			return "INSERT INTO `" + tableAccounts + "` (`" + columnAccountsUser + "`, `" + columnAccountsUUID + "`, `" + columnAccountsMoney + "`) "
				+ "VALUES ('" + strName + "', '" + struuid + "', '" + strBlnc + "') ON DUPLICATE KEY UPDATE "
				+ "`" + columnAccountsUser  + "` = VALUES(`" + columnAccountsUser  + "`), "
				+ "`" + columnAccountsUUID  + "` = VALUES(`" + columnAccountsUUID  + "`), "
				+ "`" + columnAccountsMoney + "` = VALUES(`" + columnAccountsMoney + "`);";
		}
		return "INSERT INTO `" + tableAccounts + "` (`" + columnAccountsUser + "`, `" + columnAccountsMoney + "`) "
			+ "VALUES ('" + strName + "', '" + strBlnc + "') ON DUPLICATE KEY UPDATE "
			+ "`" + columnAccountsUser  + "` = VALUES(`" + columnAccountsUser  + "`), "
			+ "`" + columnAccountsMoney + "` = VALUES(`" + columnAccountsMoney + "`);";
	}
}
