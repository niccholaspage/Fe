package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.Database;
import com.niccholaspage.Fe.Fe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;

public abstract class DatabaseGeneric implements Database
{
	protected final Fe plugin;
	protected final Map<String, Account> accounts = new HashMap<>();
	public DatabaseGeneric(Fe plugin)
	{
		this.plugin = plugin;
	}
	@Override
	public String getConfigName()
	{
		return getName().replace(" ", "").toLowerCase();
	}
	@Override
	public ConfigurationSection getConfigSection()
	{
		return plugin.settings.getDatabaseSection(getConfigName());
	}
	@Override
	public void onRenameAccount(Account account, String oldName, String newName)
	{
		accounts.remove(oldName);
		if(newName != null && !"".equals(newName))
			accounts.put(newName, account);
	}
	@Override
	public abstract List<Account> loadAccounts();
	@Override
	public List<Account> getAccounts()
	{
		return new ArrayList<>(accounts.values());
	}
	@Override
	public List<Account> getTopAccounts(int size)
	{
		final LinkedList<Account> topList = new LinkedList<>(new LinkedHashSet<>(accounts.values()));
		Collections.sort(topList);
		return topList.subList(0, Math.min(size, topList.size()));
	}
	@Override
	public boolean accountExists(UUID uuid)
	{
		return accounts.containsKey(uuid.toString());
	}
	@Override
	public boolean accountExists(String name)
	{
		return accounts.containsKey(name);
	}
	@Override
	public Account createAccount(UUID uuid, String name)
	{
		final Account existByUUID = accounts.get(uuid.toString());
		if(existByUUID != null)
			return existByUUID;
		final Account existByName = accounts.get(name);
		if(existByName != null)
			return existByName;
		final Account account = new AccountInt(plugin, this, name, uuid, plugin.api.getDefaultHoldings());
		accounts.put(uuid.toString(), account);
		if(name != null && !"".equals(name))
			accounts.put(name, account);
		saveAccount(account);
		return account;
	}
	@Override
	public Account getAccount(UUID uuid)
	{
		return accounts.get(uuid.toString());
	}
	@Override
	public Account getAccount(String name)
	{
		return accounts.get(name);
	}
	@Override
	public abstract void saveAccount(Account account);
	@Override
	public void removeAccount(Account account)
	{
		accounts.remove(account.getName());
		UUID uuid = account.getUUID();
		if(uuid != null)
			accounts.remove(uuid.toString());
	}
	@Override
	public abstract void cleanAccountsWithDefaultHoldings();
	@Override
	public abstract void removeAllAccounts();
}
