package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.Database;
import org.bukkit.configuration.ConfigurationSection;
import com.niccholaspage.Fe.Fe;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	public List<Account> getTopAccounts(int size)
	{
		List<Account> topAccounts = new LinkedList<>(loadAccounts());
		Collections.sort(topAccounts);
		return topAccounts.subList(0, size);
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
	public boolean accountExists(UUID uuid)
	{
		return accounts.containsKey(uuid.toString());
	}
	@Override
	public boolean accountExists(String name)
	{
		return accounts.containsKey(name);
	}
	/*
	protected boolean convertToUUID()
	{
		if(!plugin.getServer().getOnlineMode())
		{
			// Disable plugin?
		}
		plugin.log(Phrases.STARTING_UUID_CONVERSION);
		List<Account> accounts = loadAccounts();
		Map<String, Double> accountMonies = new HashMap<>();
		for(Account account : accounts)
			accountMonies.put(account.getName(), account.getMoney());
		List<String> names = new ArrayList<>();
		for(Account account : accounts)
			names.add(account.getName());
		try
		{
			UUIDFetcher fetcher = new UUIDFetcher(names);
			Map<String, UUID> response = fetcher.call();
			removeAllAccounts();
			for(String name : response.keySet())
				for(String accountName : new HashMap<>(accountMonies).keySet())
					if(accountName.equalsIgnoreCase(name))
					{
						saveAccount(name, response.get(name).toString(), accountMonies.get(accountName));
						accountMonies.remove(accountName);
					}
			for(String accountName : accountMonies.keySet())
				saveAccount(accountName, null, accountMonies.get(accountName));
		} catch(Exception e)
		{
			System.out.println(e);
			plugin.log(Phrases.UUID_CONVERSION_FAILED);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return false;
		}
		plugin.log(Phrases.UUID_CONVERSION_SUCCEEDED);
		return true;
	}
	*/
}
