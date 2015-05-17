package com.niccholaspage.Fe.API;

import com.niccholaspage.Fe.Databases.DatabaseGeneric;
import com.niccholaspage.Fe.Fe;
import java.util.UUID;
import org.bukkit.entity.Player;

public class Account implements Comparable<Account>
{
	private final Fe plugin;
	private final Database database;
	private final UUID uuid;
	private String name;
	private double money;
	private Player player;
	public Account(Fe plugin, DatabaseGeneric database, String name, UUID uuid, double money)
	{
		this.plugin = plugin;
		this.database = database;
		this.name = name;
		this.uuid = uuid;
		this.money = money;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
		database.saveAccount(this);
	}
	public UUID getUUID()
	{
		return uuid;
	}
	public double getMoney()
	{
		return money;
	}
	public void setMoney(double money)
	{
		if(money < 0 && !plugin.api.isCurrencyNegative())
			money = 0;
		money = plugin.api.getMoneyRounded(money);
		final double maxHoldings = plugin.api.getMaxHoldings();
		if(plugin.api.getMaxHoldings() > 0.0 && money > maxHoldings)
			money = plugin.api.getMoneyRounded(maxHoldings);
		this.money = money;
		database.saveAccount(this);
	}
	public void withdraw(double amount)
	{
		setMoney(money - amount);
	}
	public void deposit(double amount)
	{
		setMoney(money + amount);
	}
	public boolean has(double amount)
	{
		return getMoney() >= amount;
	}
	public boolean canReceive(double amount)
	{
		final double maxHoldings = plugin.api.getMaxHoldings();
		return maxHoldings < 0.0 || getMoney() + amount <= maxHoldings;
	}
	public boolean isConnected()
	{
		return player != null;
	}
	public Player getPlayer()
	{
		return player;
	}
	public void connected(Player player)
	{
		this.player = player;
	}
	public void disconnected()
	{
		this.player = null;
	}
	@Override
	public int compareTo(Account other)
	{
		return (int)(other.getMoney() - getMoney());
	}
}
