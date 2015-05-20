package com.niccholaspage.Fe.Databases;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.Database;
import com.niccholaspage.Fe.Fe;
import java.util.UUID;
import org.bukkit.entity.Player;

public class AccountInt implements Account
{
	private final Fe plugin;
	private final Database database;
	protected UUID   uuid;
	protected String name;
	protected double money;
	private Player player;
	public AccountInt(Fe plugin, DatabaseGeneric database, String name, UUID uuid, double money)
	{
		this.plugin = plugin;
		this.database = database;
		this.name = name;
		this.uuid = uuid;
		this.money = money;
	}
	@Override
	public UUID getUUID()
	{
		return uuid;
	}
	@Override
	public String getName()
	{
		return name;
	}
	@Override
	public void setName(String name)
	{
		database.onRenameAccount(this, this.name, name);
		this.name = name;
		database.saveAccount(this);
	}
	@Override
	public double getMoney()
	{
		return money;
	}
	@Override
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
	@Override
	public void withdraw(double amount)
	{
		setMoney(money - amount);
	}
	@Override
	public void deposit(double amount)
	{
		setMoney(money + amount);
	}
	@Override
	public boolean has(double amount)
	{
		return getMoney() >= amount;
	}
	@Override
	public boolean canReceive(double amount)
	{
		final double maxHoldings = plugin.api.getMaxHoldings();
		return maxHoldings < 0.0 || getMoney() + amount <= maxHoldings;
	}
	@Override
	public int compareTo(Account other)
	{
		return (int)(other.getMoney() - getMoney());
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
}
