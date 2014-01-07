package org.melonbrew.fe.database;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;

public abstract class Database {
	private final Fe plugin;
	
	public Database(Fe plugin){
		this.plugin = plugin;
	}
	
	public abstract boolean init();
	
	public abstract List<Account> getTopAccounts(int size);
	
	public abstract List<Account> getAccounts();
	
	public abstract Double loadAccountMoney(String name);
	
	protected abstract void saveAccount(String name, double money);
	
	public abstract void removeAccount(String name);
	
	public abstract void getConfigDefaults(ConfigurationSection section);
	
	public abstract void clean();
	
	public abstract void close();
	
	public abstract String getName();
	
	public String getConfigName(){
		return getName().toLowerCase().replace(" ", "");
	}
	
	public ConfigurationSection getConfigSection(){
		return plugin.getConfig().getConfigurationSection(getConfigName());
	}
	
	public Account getAccount(String name){
		Double money = loadAccountMoney(name);
		
		if (money == null){
			return null;
		}else {
			Account account = new Account(name, plugin, this);
			
			account.setMoney(money);
			
			return account;
		}
	}
	
	public Account createAccount(String name){
		Account account = getAccount(name);
		
		if (account == null){
			account = new Account(name, plugin, this);
			
			account.setMoney(plugin.getAPI().getDefaultHoldings());
		}
		
		return account;
	}
	
	public boolean accountExists(String name){
		return getAccount(name) != null;
	}
}
