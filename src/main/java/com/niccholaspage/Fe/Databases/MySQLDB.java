package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.API.Account;
import org.bukkit.configuration.ConfigurationSection;
import com.niccholaspage.Fe.Fe;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MySQLDB extends DatabaseSQL implements Runnable
{
	private final static int defaultGranularity = 30000;
	private final ConcurrentLinkedQueue<DefferedTask> queue = new ConcurrentLinkedQueue<>();
	private Thread dispatcher;
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
	public boolean init()
	{
		if(super.init())
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
		super.close();
	}
	@Override
	public void run()
	{
		try
		{
			final int granularity = getConfigSection().getInt("saving-granularity", defaultGranularity);
			for(;;)
				if(doAllTasks() == 0)
					Thread.sleep(granularity);
		} catch(InterruptedException e) {
		}
	}
	@Override
	public void saveAccount(Account account)
	{
		queue.add(new DefferedTask(account)
		{
			@Override
			public void run()
			{
				MySQLDB.super.saveAccount(getAccount());
			}
		});
	}
	@Override
	public void reloadAccount(Account account)
	{
		doAllTasks();
		queue.add(new DefferedTask(account)
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
		doAllTasks();
		queue.add(new DefferedTask(account)
		{
			@Override
			public void run()
			{
				MySQLDB.super.removeAccount(getAccount());
			}
		});
	}
	private synchronized int doAllTasks()
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
		section.addDefault("saving-granularity", defaultGranularity);
		section.addDefault("connection.database", "localhost:3306/minecraft");
		section.addDefault("connection.username", "user");
		section.addDefault("connection.password", "pass");
		final ConfigurationSection tables = getSection(section, "tables");
		tables.addDefault("accounts", "fe_accounts");
		final ConfigurationSection columns = getSection(section, "columns");
		final ConfigurationSection columnsAccounts = getSection(columns, "accounts");
		columnsAccounts.addDefault("username", "name");
		columnsAccounts.addDefault("money", "money");
		columnsAccounts.addDefault("uuid", "uuid");
	}
	private ConfigurationSection getSection(ConfigurationSection parent, String childName)
	{
		ConfigurationSection child = parent.getConfigurationSection(childName);
		if(child == null)
			child = parent.createSection(childName);
		return child;
	}
}
