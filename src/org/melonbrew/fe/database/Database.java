package org.melonbrew.fe.database;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Database {
	private final Fe plugin;

	private final boolean cacheAccounts;

	private final Set<Account> accounts;

	public Database(Fe plugin){
		this(plugin, plugin.getAPI().getCacheAccounts());
	}

	public Database(Fe plugin, boolean cacheAccounts){
		this.plugin = plugin;

		this.cacheAccounts = cacheAccounts;

		this.accounts = new HashSet<Account>();
	}

	public abstract boolean init();

	public abstract List<Account> getTopAccounts(int size);

	public abstract List<Account> getAccounts();

	public abstract Double loadAccountMoney(String name);

	protected abstract void saveAccount(String name, double money);

	public abstract void removeAccount(String name);

	public abstract void getConfigDefaults(ConfigurationSection section);

	public abstract void clean();

	public void close(){
		for (Account account : accounts){
			account.save(account.getMoney());
		}
	}

	public abstract String getName();

	public String getConfigName(){
		return getName().toLowerCase().replace(" ", "");
	}

	public ConfigurationSection getConfigSection(){
		return plugin.getConfig().getConfigurationSection(getConfigName());
	}

	public Account getAccount(String name){
		for (Account account : accounts){
			if (account.getName().equals(name)){
				return account;
			}
		}

		Double money = loadAccountMoney(name);

		if (money == null){
			return null;
		}else {
			Account account = new Account(name, plugin, this);

			account.setMoney(money);

			accounts.add(account);

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

	public boolean cacheAccounts(){
		return cacheAccounts;
	}
}
