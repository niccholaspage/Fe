package com.niccholaspage.Fe.API;

import com.niccholaspage.Fe.Fe;

public class Account implements Comparable<Account>
{
	private final Fe plugin;
	private final Database database;
	private String name;
	private final String uuid;
	private Double money;
	public Account(Fe plugin, String name, String uuid, Database database)
	{
		this.plugin = plugin;
		this.database = database;
		this.name = name;
		this.uuid = uuid;
		this.money = null;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
		if(money == null)
			this.money = getMoney();
		database.saveAccount(name, uuid, money);
	}
	public String getUUID()
	{
		return uuid;
	}
	public Double getMoney()
	{
		if(money != null)
			return money;
		String money_string = database.loadAccountData(name, uuid).get("money");
		Double parseMoney = 0D;
		try
		{
			parseMoney = Double.parseDouble(money_string);
		} catch(Exception e) {
		}
		if(database.cacheAccounts())
			this.money = parseMoney;
		return parseMoney;
	}
	public void setMoney(double money)
	{
		Double currentMoney = getMoney();
		if(currentMoney != null && currentMoney == money)
			return;
		if(money < 0 && !plugin.getAPI().isCurrencyNegative())
			money = 0;
		currentMoney = plugin.getAPI().getMoneyRounded(money);
		if(plugin.getAPI().getMaxHoldings() > 0 && currentMoney > plugin.getAPI().getMaxHoldings())
			currentMoney = plugin.getAPI().getMoneyRounded(plugin.getAPI().getMaxHoldings());
		if(!database.cacheAccounts() || plugin.getServer().getPlayerExact(getName()) == null)
			save(currentMoney);
		else
			this.money = currentMoney;
	}
	public void withdraw(double amount)
	{
		setMoney(getMoney() - amount);
	}
	public void deposit(double amount)
	{
		setMoney(getMoney() + amount);
	}
	public boolean canReceive(double amount)
	{
		return plugin.getAPI().getMaxHoldings() == -1 || amount + getMoney() < plugin.getAPI().getMaxHoldings();
	}
	public boolean has(double amount)
	{
		return getMoney() >= amount;
	}
	public void save(double money)
	{
		database.saveAccount(name, uuid, money);
	}
	public boolean equals(Account other)
	{
		return other.getName().equals(getName());
	}
	@Override
	public int compareTo(Account other)
	{
		return (int)(other.getMoney() - getMoney());
	}
}
