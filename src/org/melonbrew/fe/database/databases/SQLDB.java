package org.melonbrew.fe.database.databases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

public abstract class SQLDB extends Database {
	private final Fe plugin;

	private final boolean supportsModification;

	private Connection connection;

	private String accountsName;

	public SQLDB(Fe plugin, boolean supportsModification){
		super(plugin);

		this.plugin = plugin;

		this.supportsModification = supportsModification;

		accountsName = "fe_accounts";
	}

	public void setAccountTable(String accountsName){
		this.accountsName = accountsName;
	}

	public String getAccountsName(){
		return accountsName;
	}

	public boolean init(){
		if (!checkConnection()){
			return false;
		}

		return true;
	}

	public boolean checkConnection(){
		try {
			if (connection == null || connection.isClosed()){
				connection = getNewConnection();

				if (connection == null || connection.isClosed()){
					return false;
				}

				query("CREATE TABLE IF NOT EXISTS " + accountsName + " (name varchar(64) NOT NULL, money double NOT NULL)");

				if (supportsModification){
					query("ALTER TABLE " + accountsName + " MODIFY name varchar(64) NOT NULL");

					query("ALTER TABLE " + accountsName + " MODIFY money double NOT NULL");
				}
			}
		} catch (SQLException e){
			e.printStackTrace();

			return false;
		}

		return true;
	}

	protected abstract Connection getNewConnection();

	public boolean query(String sql){
		try {
			return connection.createStatement().execute(sql);
		} catch (SQLException e){
			e.printStackTrace();

			return false;
		}
	}

	public Connection getConnection(){
		return connection;
	}

	public void close(){
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	public List<Account> getTopAccounts(int size){
		checkConnection();

		String sql = "SELECT * FROM " + accountsName + " ORDER BY money DESC limit " + size;

		List<Account> topAccounts = new ArrayList<Account>();

		try {
			ResultSet set = connection.createStatement().executeQuery(sql);

			while (set.next()){
				Account account = new Account(set.getString("name").toLowerCase(), plugin, this);

				account.setMoney(set.getDouble("money"));

				topAccounts.add(account);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}

		return topAccounts;
	}

	public List<Account> getAccounts(){
		checkConnection();

		List<Account> accounts = new ArrayList<Account>();

		try {
			ResultSet set = connection.createStatement().executeQuery("SELECT * from " + accountsName);

			while (set.next()){
				Account account = new Account(set.getString("name").toLowerCase(), plugin, this);

				account.setMoney(set.getDouble("money"));

				accounts.add(account);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}

		return accounts;
	}

	public Double loadAccountMoney(String name){
		checkConnection();

		double money = -1;

		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + accountsName + " WHERE name=?");

			statement.setString(1, name);

			ResultSet set = statement.executeQuery();

			while (set.next()){
				money = set.getDouble("money");
			}

			set.close();
		} catch (SQLException e){
			e.printStackTrace();

			return null;
		}

		return money;
	}

	public void removeAccount(String name){
		checkConnection();

		PreparedStatement statement;
		try {
			statement = connection.prepareStatement("DELETE FROM " + accountsName + " WHERE name=?");

			statement.setString(1, name);

			statement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void saveAccount(String name, double money){
		checkConnection();

		try {
			if (accountExists(name)){
				PreparedStatement statement = connection.prepareStatement("UPDATE " + accountsName + " SET name=?, money=?");

				statement.setString(1, name);

				statement.setDouble(2, money);

				statement.execute();
			}else {
				PreparedStatement statement = connection.prepareStatement("INSERT INTO " + accountsName + " (name, money) VALUES (?, ?)");

				statement.setString(1, name);

				statement.setDouble(2, money);

				statement.execute();
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	public void clean(){
		checkConnection();

		try {
			ResultSet set = connection.prepareStatement("SELECT * from " + accountsName + " WHERE money=" + plugin.getAPI().getDefaultHoldings()).executeQuery();

			boolean executeQuery = false;

			StringBuilder builder = new StringBuilder("DELETE FROM " + accountsName + " WHERE name IN (");

			while (set.next()){
				String name = set.getString("name");

				if (plugin.getServer().getPlayerExact(name) != null){
					continue;
				}

				executeQuery = true;

				builder.append("'").append(name).append("', ");
			}

			set.close();

			builder.delete(builder.length() - 2, builder.length()).append(")");

			if (executeQuery){
				query(builder.toString());
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
}
