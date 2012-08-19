package org.melonbrew.fe.database.databases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;
import org.melonbrew.fe.database.LogType;

public class FlatFile extends Database {
	private final Fe plugin;
	
	private final File storageFile;
	
	private final Properties storage;
	
	public FlatFile(Fe plugin){
		super(plugin);
		
		this.plugin = plugin;
		
		storageFile = new File(plugin.getDataFolder(), "flatdatabase.prop");
		
		storage = new Properties();
	}
	
	public boolean init(){
		try {
			FileInputStream inputStream = new FileInputStream(storageFile);
			
			storage.load(inputStream);
		} catch (Exception e){
			return false;
		}
		
		return true;
	}
	
	public void close(){
		
	}
	
	public double loadAccountMoney(String name){
		try {
			double money = Double.parseDouble(storage.getProperty(name));
			
			return money;
		} catch (Exception e){
			return -1;
		}
	}
	
	public void removeAccount(String name){
		storage.remove(name);
		
		saveFile();
	}
	
	private void saveFile(){
		storageFile.delete();
		
		try {
			storageFile.createNewFile();
			
			FileOutputStream outputStream = new FileOutputStream(storageFile);
			
			storage.store(outputStream, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void saveAccount(String name, double money){
		storage.setProperty(name, money + "");
		
		saveFile();
	}
	
	public List<Account> getTopAccounts(){
		List<Account> allAccounts = new ArrayList<Account>();
		
		for (String name : storage.stringPropertyNames()){
			Account account = getAccount(name);
			
			allAccounts.add(account);
		}
		
		return allAccounts;
	}
	
	public void log(String name, double money, LogType type) {
		
	}
	
	public void clean(){
		for (String name : storage.stringPropertyNames()){
			Account account = getAccount(name);
			
			if (plugin.getServer().getPlayerExact(name) != null){
				continue;
			}
			
			if (account.getMoney() == plugin.getAPI().getDefaultHoldings()){
				removeAccount(name);
			}
		}
	}
	
	public void getConfigDefaults(ConfigurationSection section){
		
	}
	
	public String getName(){
		return "Flat File";
	}
}
