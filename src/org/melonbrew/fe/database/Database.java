package org.melonbrew.fe.database;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Database {
	private final Fe plugin;

	protected boolean cacheAccounts;

	private final Set<Account> accounts;

	public Database(Fe plugin){
		this.plugin = plugin;

		this.accounts = new HashSet<Account>();
	}

	public boolean init(){
		this.cacheAccounts = plugin.getAPI().getCacheAccounts();

		return false;
	}

	public List<Account> getTopAccounts(int size){
		List<Account> topAccounts = loadTopAccounts(size);

		if (!accounts.isEmpty()){
			List<Account> cachedTopAccounts = new ArrayList<Account>(accounts);

			Collections.sort(cachedTopAccounts, new Comparator<Account>(){
				public int compare(Account account1, Account account2) {
					return (int) (account2.getMoney() - account1.getMoney());
				}
			});

			if (accounts.size() > size){
				cachedTopAccounts = cachedTopAccounts.subList(0, size);
			}

			topAccounts.addAll(cachedTopAccounts);
		}

		Collections.sort(topAccounts, new Comparator<Account>(){
			public int compare(Account account1, Account account2) {
				return (int) (account2.getMoney() - account1.getMoney());
			}
		});

		if (topAccounts.size() > size){
			topAccounts = topAccounts.subList(0, size);
		}

		return topAccounts;
	}

	public abstract List<Account> loadTopAccounts(int size);

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
		Account account = getCachedAccount(name);

		if (account != null){
			return account;
		}

		Double money = loadAccountMoney(name);

		if (money == null){
			return null;
		}else {
			account = new Account(name, plugin, this);

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

	public Account getCachedAccount(String name){
		for (Account account : accounts){
			if (account.getName().equals(name)){
				return account;
			}
		}

		return null;
	}

	public boolean removeCachedAccount(Account account){
		return accounts.remove(account);
	}
}
