package org.melonbrew.fe.database.databases;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

import com.niccholaspage.nSQL.Table;
import com.niccholaspage.nSQL.query.SelectQuery;

public abstract class SQLDB extends Database {
	private final Fe plugin;
	
	private final boolean supportsModification;
	
	private Connection connection;
	
	private String accountsName;
	
	private Table accounts;
	
	public SQLDB(Fe plugin, boolean supportsModification){
		super(plugin);
		
		this.plugin = plugin;
		
		this.supportsModification = supportsModification;
		
		accountsName = "fe_accounts";
	}
	
	public void setAccountTable(String accountsName){
		this.accountsName = accountsName;
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
				
				if (connection.isClosed()){
					return false;
				}
				
				accounts = new Table(connection, accountsName);
				
				accounts.create().create("name varchar(64) NOT NULL").create("money double NOT NULL").execute();

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
		
		ResultSet set = this.accounts.select().execute();
		
		try {
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
	
	public double loadAccountMoney(String name){
		checkConnection();
		
		double money = -1;
		
		try {
			SelectQuery query = accounts.select().where("name", name);
			
			ResultSet set = query.execute();
			
			while (set.next()){
				money = set.getDouble("money");
			}
			
			query.close();
		} catch (SQLException e){
			e.printStackTrace();
			
			return -1;
		}
		
		return money;
	}
	
	public void removeAccount(String name){
		checkConnection();
		
		accounts.delete().where("name", name).execute();
	}
	
	protected void saveAccount(String name, double money){
		checkConnection();
		
		if (accountExists(name)){
			accounts.update().set("money", money).where("name", name).execute();
		}else {
			accounts.insert().insert("name").insert("money").value(name).value(money).execute();
		}
	}
	
	public void clean(){
		checkConnection();
		
		try {
			SelectQuery query = accounts.select().where("money", plugin.getAPI().getDefaultHoldings());
			
			ResultSet set = query.execute();
			
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
			
		}
	}
}
