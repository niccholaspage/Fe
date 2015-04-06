package com.niccholaspage.Fe;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import com.niccholaspage.Fe.API.Account;
import java.util.ArrayList;
import java.util.List;

public class VaultHandler implements Economy
{
	private final String name = "Fe";
	private Fe plugin;
	public VaultHandler(Fe plugin)
	{
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(), plugin);
		plugin.log("Vault support enabled.");
	}
	@Override
	public boolean isEnabled()
	{
		return plugin != null;
	}
	@Override
	public String getName()
	{
		return name;
	}
	@Override
	public String format(double amount)
	{
		return plugin.getAPI().formatNoColor(amount);
	}
	@Override
	public String currencyNameSingular()
	{
		return plugin.getAPI().getCurrencyMajorSingle();
	}
	@Override
	public String currencyNamePlural()
	{
		return plugin.getAPI().getCurrencyMajorMultiple();
	}
	@Override
	public double getBalance(String playerName)
	{
		return getAccountBalance(playerName, null);
	}
	@Override
	public double getBalance(OfflinePlayer offlinePlayer)
	{
		return getAccountBalance(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString());
	}
	private double getAccountBalance(String playerName, String uuid)
	{
		Account account = plugin.getAPI().getAccount(playerName, uuid);
		if(account == null)
			return 0;
		return account.getMoney();
	}
	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount)
	{
		return withdraw(playerName, null, amount);
	}
	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount)
	{
		return withdraw(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString(), amount);
	}
	private EconomyResponse withdraw(String playerName, String uuid, double amount)
	{
		if(amount < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds");
		Account account = plugin.getAPI().getAccount(playerName, uuid);
		if(account == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
		if(account.has(amount))
		{
			account.withdraw(amount);
			return new EconomyResponse(amount, account.getMoney(), ResponseType.SUCCESS, "");
		} else
			return new EconomyResponse(0, account.getMoney(), ResponseType.FAILURE, "Insufficient funds");
	}
	@Override
	public EconomyResponse depositPlayer(String playerName, double amount)
	{
		return deposit(playerName, null, amount);
	}
	@Override
	public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount)
	{
		return deposit(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString(), amount);
	}
	private EconomyResponse deposit(String playerName, String uuid, double amount)
	{
		if(amount < 0)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot deposit negative funds");
		Account account = plugin.getAPI().getAccount(playerName, uuid);
		if(account == null)
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
		account.deposit(amount);
		return new EconomyResponse(amount, account.getMoney(), ResponseType.SUCCESS, "");
	}
	@Override
	public boolean has(String playerName, double amount)
	{
		return getBalance(playerName) >= amount;
	}
	@Override
	public boolean has(OfflinePlayer offlinePlayer, double amount)
	{
		return getBalance(offlinePlayer) >= amount;
	}
	@Override
	public EconomyResponse createBank(String name, String player)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse createBank(String name, OfflinePlayer offlinePlayer)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse deleteBank(String name)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse bankHas(String name, double amount)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse bankWithdraw(String name, double amount)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse bankDeposit(String name, double amount)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse isBankOwner(String name, String playerName)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse isBankOwner(String name, OfflinePlayer offlinePlayer)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse isBankMember(String name, String playerName)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse isBankMember(String name, OfflinePlayer offlinePlayer)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public EconomyResponse bankBalance(String name)
	{
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}
	@Override
	public List<String> getBanks()
	{
		return new ArrayList<>();
	}
	@Override
	public boolean hasBankSupport()
	{
		return false;
	}
	@Override
	public boolean hasAccount(String playerName)
	{
		return plugin.getAPI().accountExists(playerName, null);
	}
	@Override
	public boolean hasAccount(OfflinePlayer offlinePlayer)
	{
		return plugin.getAPI().accountExists(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString());
	}
	@Override
	public boolean createPlayerAccount(String playerName)
	{
		return createAccount(playerName, null);
	}
	@Override
	public boolean createPlayerAccount(OfflinePlayer offlinePlayer)
	{
		return createAccount(offlinePlayer.getName(), offlinePlayer.getUniqueId().toString());
	}
	private boolean createAccount(String playerName, String uuid)
	{
		if(hasAccount(playerName, uuid))
			return false;
		plugin.getAPI().updateAccount(playerName, uuid);
		return true;
	}
	@Override
	public int fractionalDigits()
	{
		return -1;
	}
	@Override
	public boolean hasAccount(String playerName, String worldName)
	{
		return hasAccount(playerName);
	}
	@Override
	public boolean hasAccount(OfflinePlayer offlinePlayer, String worldName)
	{
		return hasAccount(offlinePlayer);
	}
	@Override
	public double getBalance(String playerName, String worldName)
	{
		return getBalance(playerName);
	}
	@Override
	public double getBalance(OfflinePlayer offlinePlayer, String worldName)
	{
		return getBalance(offlinePlayer);
	}
	@Override
	public boolean has(String playerName, String worldName, double amount)
	{
		return has(playerName, amount);
	}
	@Override
	public boolean has(OfflinePlayer offlinePlayer, String worldName, double amount)
	{
		return has(offlinePlayer, amount);
	}
	@Override
	public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount)
	{
		return withdrawPlayer(playerName, amount);
	}
	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String worldName, double amount)
	{
		return withdrawPlayer(offlinePlayer, amount);
	}
	@Override
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount)
	{
		return depositPlayer(playerName, amount);
	}
	@Override
	public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String worldName, double amount)
	{
		return depositPlayer(offlinePlayer, amount);
	}
	@Override
	public boolean createPlayerAccount(String playerName, String worldName)
	{
		return createPlayerAccount(playerName);
	}
	@Override
	public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String worldName)
	{
		return createPlayerAccount(offlinePlayer);
	}
	public class EconomyServerListener implements Listener
	{
		@EventHandler(priority = EventPriority.MONITOR)
		public void onPluginEnable(PluginEnableEvent event)
		{
			if(plugin == null)
			{
				Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Fe");
				if(plugin != null && plugin.isEnabled())
				{
					VaultHandler.this.plugin = (Fe)plugin;
					VaultHandler.this.plugin.log("Vault support enabled.");
				}
			}
		}
		@EventHandler(priority = EventPriority.MONITOR)
		public void onPluginDisable(PluginDisableEvent event)
		{
			if(plugin != null)
				if(event.getPlugin().getDescription().getName().equals(name))
				{
					plugin = null;
					Bukkit.getLogger().info("[Fe] Vault support disabled.");
				}
		}
	}
}
