package com.niccholaspage.Fe.API;

import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;

public interface Database
{
	public String  getName();
	public boolean isAsync();
	public boolean initialize();
	public void    close();
	public String               getConfigName();
	public ConfigurationSection getConfigSection();
	public void                 getConfigDefaults(ConfigurationSection section);
	public void                 onRenameAccount(Account account, String oldName, String newName);
	public List<Account> loadAccounts();
	public List<Account> getAccounts();
	public List<Account> getTopAccounts(int size);
	public boolean accountExists(UUID uuid);
	public boolean accountExists(String name);
	public Account createAccount(UUID uuid);
	public Account createAccount(String name);
	public Account createAccount(UUID uuid, String name);
	public Account getAccount(UUID uuid);
	public Account getAccount(String name);
	public void saveAccount(Account account);
	public void reloadAccount(Account account);
	public void removeAccount(Account account);
	public void cleanAccountsWithDefaultHoldings();
	public void removeAllAccounts();
}
