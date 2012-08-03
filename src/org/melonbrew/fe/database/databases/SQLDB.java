package org.melonbrew.fe.database.databases;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lib.PatPeter.SQLibrary.Database;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;

public abstract class SQLDB extends org.melonbrew.fe.database.Database {
	private final Fe plugin;
	
	private Database database;
	
	private final String accounts;
	
	public SQLDB(Fe plugin){
		super(plugin);
		
		this.plugin = plugin;
		
		accounts = "fe_accounts";
	}
	
	public boolean init(){
		database = getNewDatabase();
		
		database.open();
		
		if (!database.checkConnection()){
			return false;
		}
		
		
		if (!database.createTable("CREATE TABLE IF NOT EXISTS " + accounts + "(name varchar(16), money double);")){
			return false;
		}
		
		return true;
	}
	
	protected abstract Database getNewDatabase();
	
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
		String sql = "SELECT name FROM " + accounts + " WHERE money=" + plugin.getAPI().getDefaultHoldings();
		
		ResultSet set = database.query(sql);
		
		String deleteQuery = "DELETE FROM " + accounts + " WHERE name IN (";
		
		try {
			while (set.next()){
				String name = set.getString("name");
				
				if (plugin.getServer().getPlayerExact(name) != null){
					continue;
				}
				
				deleteQuery += "'" + name + "', ";
			}
			
			deleteQuery = deleteQuery.substring(0, deleteQuery.length() - 2) + ")";
			
			database.query(deleteQuery);
		} catch (SQLException e){
			
		}
	}
}
