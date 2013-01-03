package org.melonbrew.fe.database.databases;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.melonbrew.fe.Fe;
import org.melonbrew.fe.SQLibrary.Database;
import org.melonbrew.fe.database.Account;

public abstract class SQLDB extends org.melonbrew.fe.database.Database {
	private final Fe plugin;
	
	private final boolean supportsModification;
	
	private Database database;
	
	private String accounts;
	
	public SQLDB(Fe plugin, boolean supportsModification){
		super(plugin);
		
		this.plugin = plugin;
		
		this.supportsModification = supportsModification;
		
		accounts = "fe_accounts";
	}
	
	public void setAccountTable(String accounts){
		this.accounts = accounts;
	}
	
	public boolean init(){
		database = getNewDatabase();
		
		if (!openConnection()){
			return false;
		}
		
		return true;
	}
	
	public boolean openConnection(){
		if (!database.checkConnection()){
			database.open();
			
			if (!database.checkConnection()){
				return false;
			}
			
			if (!database.createTable("CREATE TABLE IF NOT EXISTS " + accounts + "(name varchar(64), money double);")){
				return false;
			}
			
			if (supportsModification){
				database.query("ALTER TABLE " + accounts + " MODIFY name varchar(64)");
			}
		}
		
		return true;
	}
	
	protected abstract Database getNewDatabase();
	
	public Database getDatabase(){
		return database;
	}
	
	public void close(){
		database.close();
	}
	
	public List<Account> getTopAccounts(){
		String sql = "SELECT name FROM " + accounts + " ORDER BY money DESC limit 5";
		
		List<Account> topAccounts = new ArrayList<Account>();
		
		ResultSet set = database.query(sql);
		
		try {
			while (set.next()){
				topAccounts.add(getAccount(set.getString("name")));
			}
		} catch (SQLException e){

		}
		
		return topAccounts;
	}
	
	public double loadAccountMoney(String name){
		String sql = "SELECT * FROM " + accounts + " WHERE name=?";
		
		double money = -1;
		
		try {
			PreparedStatement prest = database.prepare(sql);
			
			prest.setString(1, name);
			
			ResultSet set = prest.executeQuery();
			
			while (set.next()){
				money = set.getDouble("money");
			}
			
			prest.close();
		} catch (SQLException e){
			e.printStackTrace();
			
			return -1;
		}
		
		return money;
	}
	
	public void removeAccount(String name){
		String sql = "DELETE FROM " + accounts + " WHERE name=?";
		
		try {
			PreparedStatement prest = database.prepare(sql);
			
			prest.setString(1, name);
			
			prest.execute();
			
			prest.close();
		}catch (SQLException e){
			
		}
	}
	
	protected void saveAccount(String name, double money){
		if (accountExists(name)){
			String sql = "UPDATE " + accounts + " SET money=? WHERE name=?";
			
			try {
				PreparedStatement prest = database.prepare(sql);
				
				prest.setDouble(1, money);
				
				prest.setString(2, name);
				
				prest.executeUpdate();
				
				prest.close();
			}catch (SQLException e){
				
			}
		}else {
			String sql = "INSERT INTO " + accounts + " (name, money) VALUES (?, ?)";
			
			try {
				PreparedStatement prest = database.prepare(sql);
				
				prest.setString(1, name);
				
				prest.setDouble(2, money);
				
				prest.executeUpdate();
				
				prest.close();
			}catch (SQLException e){
				
			}
		}
	}
	
	public void clean(){
		String sql = "SELECT * FROM " + accounts + " WHERE money=?";
		
		try {
			PreparedStatement prest = database.prepare(sql);
			
			prest.setDouble(1, plugin.getAPI().getDefaultHoldings());
			
			ResultSet set = prest.executeQuery();
			
			boolean executeQuery = false;
			
			String deleteQuery = "DELETE FROM " + accounts + " WHERE name IN (";
			
			while (set.next()){
				String name = set.getString("name");
				
				if (plugin.getServer().getPlayerExact(name) != null){
					continue;
				}
				
				executeQuery = true;
				
				deleteQuery += "'" + name + "', ";
			}
			
			prest.close();
			
			deleteQuery = deleteQuery.substring(0, deleteQuery.length() - 2) + ")";
			
			if (executeQuery){
				database.query(deleteQuery);
			}
		} catch (SQLException e){
			
		}
	}
}
