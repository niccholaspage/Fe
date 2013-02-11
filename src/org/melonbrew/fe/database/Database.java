package org.melonbrew.fe.database;

import java.util.List;

import org.melonbrew.fe.Fe;

import com.niccholaspage.Metro.base.config.ConfigSection;

public abstract class Database {
	private final Fe plugin;
	
	public Database(Fe plugin){
		this.plugin = plugin;
	}
	
	public abstract boolean init();
	
	public abstract List<Account> getTopAccounts(int size);
	
	public abstract List<Account> getAccounts();
	
	public abstract double loadAccountMoney(String name);
	
	protected abstract void saveAccount(String name, double money);
	
	public abstract void removeAccount(String name);
	
	public abstract void getConfigDefaults(ConfigSection section);
	
	public abstract void clean();
	
	public abstract void close();
	
	public abstract String getName();
	
	public String getConfigName(){
		return getName().toLowerCase().replace(" ", "");
	}
	
	public ConfigSection getConfigSection(){
		return plugin.getConfig().getConfigSection(getConfigName());
	}
	
	public Account getAccount(String name){
		double money = loadAccountMoney(name);
		
		if (money == -1){
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
