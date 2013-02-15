package org.melonbrew.fe;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.melonbrew.fe.API;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;

public class Economy_Fe implements Economy {
	private final String name = "Fe";
	private Plugin bukkitPlugin = null;
	private Fe fe = null;
	private API api = null;

	public Economy_Fe(Fe fe, Plugin bukkitPlugin) {
		this.bukkitPlugin = bukkitPlugin;
		
		Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), bukkitPlugin);
		
		if (this.fe == null) {
			Plugin efe = bukkitPlugin.getServer().getPluginManager().getPlugin("Fe");
			if (efe != null && efe.isEnabled()) {
				this.fe = fe;
				api = fe.getAPI();
				fe.log("Vault support enabled.");
			}
		}
	}

	public class EconomyServerListener implements Listener {
		Economy_Fe economy = null;

		public EconomyServerListener(Economy_Fe economy) {
			this.economy = economy;
		}

		@EventHandler(priority = EventPriority.MONITOR)
		public void onPluginEnable(PluginEnableEvent event) {
			if (fe == null) {
				Plugin efe = bukkitPlugin.getServer().getPluginManager().getPlugin("Fe");
				if (efe != null && efe.isEnabled()) {
					api = fe.getAPI();
					fe.log("Vault support enabled.");
				}
			}
		}

		@EventHandler(priority = EventPriority.MONITOR)
		public void onPluginDisable(PluginDisableEvent event) {
			if (fe != null) {
				if (event.getPlugin().getDescription().getName().equals("Fe")) {
					fe = null;
					api = null;
					Bukkit.getLogger().info("[Fe] Vault support disabled.");
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		if (api == null) {
			return false;
		} else {
			return bukkitPlugin.isEnabled();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String format(double amount) {
		return api.formatNoColor(amount);
	}

	@Override
	public String currencyNameSingular() {
		return api.getCurrencyMajorSingle();
	}

	@Override
	public String currencyNamePlural() {
		return api.getCurrencyMajorMultiple();
	}

	@Override
	public double getBalance(String playerName) {
		if (api.accountExists(playerName)) {
			return api.getAccount(playerName).getMoney();
		} else {
			return 0;
		}
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		if (amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds");
		}

		if (!api.accountExists(playerName)){
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
		}

		Account account = api.getAccount(playerName);
		if (account.has(amount)) {
			account.withdraw(amount);
			return new EconomyResponse(amount, account.getMoney(), ResponseType.SUCCESS, "");
		} else {
			return new EconomyResponse(0, account.getMoney(), ResponseType.FAILURE, "Insufficient funds");
		}
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		if (amount < 0) {
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot desposit negative funds");
		}

		if (!api.accountExists(playerName)){
			return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
		}

		Account account = api.getAccount(playerName);
		account.deposit(amount);
		return new EconomyResponse(amount, account.getMoney(), ResponseType.SUCCESS, "");
	}

	@Override
	public boolean has(String playerName, double amount) {
		return getBalance(playerName) >= amount;
	}

	@Override
	public EconomyResponse createBank(String name, String player) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public EconomyResponse deleteBank(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankHas(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankWithdraw(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankDeposit(String name, double amount) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public EconomyResponse isBankOwner(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public EconomyResponse bankBalance(String name) {
		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Fe does not support bank accounts!");
	}

	@Override
	public List<String> getBanks() {
		return new ArrayList<String>();
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public boolean hasAccount(String playerName) {
		return api.accountExists(playerName);
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		if (hasAccount(playerName)) {
			return false;
		}
		api.createAccount(playerName);
		return true;
	}

	@Override
	public int fractionalDigits(){
		return -1;
	}
}
