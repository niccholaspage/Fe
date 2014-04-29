package org.melonbrew.fe.database.databases;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class SQLDB extends Database {
	private final Fe plugin;

	private final boolean supportsModification;

	private Connection connection;

	private String accountsName;

	private String accountsColumnUser;

	private String accountsColumnMoney;

	private String accountsColumnUUID;

	public SQLDB(Fe plugin, boolean supportsModification) {
		super(plugin);

		this.plugin = plugin;

		this.supportsModification = supportsModification;

		accountsName = "fe_accounts";

		accountsColumnUser = "name";

		accountsColumnMoney = "money";

		accountsColumnUUID = "uuid";

		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					if (connection != null && !connection.isClosed()) {
						connection.createStatement().execute("/* ping */ SELECT 1");
					}
				} catch (SQLException e) {
					connection = getNewConnection();
				}
			}
		}, 60 * 20, 60 * 20);
	}

	public void setAccountTable(String accountsName) {
		this.accountsName = accountsName;
	}

	public String getAccountsName() {
		return accountsName;
	}

	public void setAccountsColumnUser(String accountsColumnUser) {
		this.accountsColumnUser = accountsColumnUser;
	}

	public void setAccountsColumnMoney(String accountsColumnMoney) {
		this.accountsColumnMoney = accountsColumnMoney;
	}

	public void setAccountsColumnUUID(String accountsColumnUUID) {
		this.accountsColumnUUID = accountsColumnUUID;
	}

	public boolean init() {
		if (!checkConnection()) {
			return false;
		}

		return true;
	}

	public boolean checkConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				connection = getNewConnection();

				if (connection == null || connection.isClosed()) {
					return false;
				}

				query("CREATE TABLE IF NOT EXISTS " + accountsName + " (" + accountsColumnUser + " varchar(64) NOT NULL, " + accountsColumnMoney + " double NOT NULL)");

				if (supportsModification) {
					query("ALTER TABLE " + accountsName + " MODIFY " + accountsColumnUser + " varchar(64) NOT NULL");

					query("ALTER TABLE " + accountsName + " MODIFY " + accountsColumnMoney + " double NOT NULL");
				}

				try {
					query("ALTER TABLE " + accountsName + " ADD " + accountsColumnUUID + " varchar(128);");
				} catch (Exception e){

				}
			}
		} catch (SQLException e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	protected abstract Connection getNewConnection();

	public boolean query(String sql) throws SQLException {
		return connection.createStatement().execute(sql);
	}

	public Connection getConnection() {
		return connection;
	}

	public void close() {
		super.close();

		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Account> getTopAccounts(int size) {
		checkConnection();

		String sql = "SELECT * FROM " + accountsName + " ORDER BY money DESC limit " + size;

		List<Account> topAccounts = new ArrayList<Account>();

		try {
			ResultSet set = connection.createStatement().executeQuery(sql);

			while (set.next()) {
				Account account = new Account(set.getString(accountsColumnUser).toLowerCase(), plugin, this);

				account.setMoney(set.getDouble(accountsColumnMoney));

				topAccounts.add(account);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return topAccounts;
	}

	public List<Account> getAccounts() {
		checkConnection();

		List<Account> accounts = new ArrayList<Account>();

		try {
			ResultSet set = connection.createStatement().executeQuery("SELECT * from " + accountsName);

			while (set.next()) {
				Account account = new Account(set.getString(accountsColumnUser).toLowerCase(), plugin, this);

				account.setMoney(set.getDouble(accountsColumnMoney));

				accounts.add(account);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accounts;
	}

	public Double loadAccountMoney(String name) {
		checkConnection();

		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + accountsName + " WHERE " + accountsColumnUser + "=?");

			statement.setString(1, name);

			ResultSet set = statement.executeQuery();

			Double money = null;

			while (set.next()) {
				money = set.getDouble(accountsColumnMoney);
			}

			set.close();

			return money;
		} catch (SQLException e) {
			e.printStackTrace();

			return null;
		}
	}

	public void removeAccount(String name) {
		checkConnection();

		PreparedStatement statement;
		try {
			statement = connection.prepareStatement("DELETE FROM " + accountsName + " WHERE " + accountsColumnUser + "=?");

			statement.setString(1, name);

			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void saveAccount(String name, double money) {
		checkConnection();

		try {
			PreparedStatement statement = connection.prepareStatement("UPDATE " + accountsName + " SET " + accountsColumnMoney + "=? WHERE " + accountsColumnUser + "=?");

			statement.setDouble(1, money);

			statement.setString(2, name);

			if (statement.executeUpdate() == 0) {
				statement = connection.prepareStatement("INSERT INTO " + accountsName + " (" + accountsColumnUser + ", " + accountsColumnMoney + ") VALUES (?, ?)");

				statement.setString(1, name);

				statement.setDouble(2, money);

				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void clean() {
		checkConnection();

		try {
			ResultSet set = connection.prepareStatement("SELECT * from " + accountsName + " WHERE " + accountsColumnMoney + "=" + plugin.getAPI().getDefaultHoldings()).executeQuery();

			boolean executeQuery = false;

			StringBuilder builder = new StringBuilder("DELETE FROM " + accountsName + " WHERE " + accountsColumnUser + " IN (");

			while (set.next()) {
				String name = set.getString(accountsColumnUser);

				if (plugin.getServer().getPlayerExact(name) != null) {
					continue;
				}

				executeQuery = true;

				builder.append("'").append(name).append("', ");
			}

			set.close();

			builder.delete(builder.length() - 2, builder.length()).append(")");

			if (executeQuery) {
				query(builder.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
