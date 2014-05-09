package org.melonbrew.fe;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.melonbrew.fe.database.Account;

import java.util.ArrayList;
import java.util.List;

public class VaultHandler extends AbstractEconomy {
    private final String name = "Fe";

    private Fe plugin;

    public VaultHandler(Fe plugin) {
        this.plugin = plugin;

        Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);

        plugin.log("Vault support enabled.");
    }

    @Override
    public boolean isEnabled() {
        return plugin != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String format(double amount) {
        return plugin.getAPI().formatNoColor(amount);
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getAPI().getCurrencyMajorSingle();
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getAPI().getCurrencyMajorMultiple();
    }

    @Override
    public double getBalance(String playerName) {
        if (plugin.getAPI().accountExists(playerName)) {
            return plugin.getAPI().getAccount(playerName).getMoney();
        } else {
            return 0;
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        if (!plugin.getAPI().accountExists(playerName)) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
        }

        Account account = plugin.getAPI().getAccount(playerName);

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
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Cannot deposit negative funds");
        }

        if (!plugin.getAPI().accountExists(playerName)) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account doesn't exist");
        }

        Account account = plugin.getAPI().getAccount(playerName);

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
        return plugin.getAPI().accountExists(playerName);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        if (hasAccount(playerName)) {
            return false;
        }

        plugin.getAPI().createAccount(playerName);

        return true;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    public class EconomyServerListener implements Listener {
        VaultHandler economy = null;

        public EconomyServerListener(VaultHandler economy) {
            this.economy = economy;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            if (plugin == null) {
                Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Fe");

                if (plugin != null && plugin.isEnabled()) {
                    VaultHandler.this.plugin = (Fe) plugin;

                    VaultHandler.this.plugin.log("Vault support enabled.");
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginDisable(PluginDisableEvent event) {
            if (plugin != null) {
                if (event.getPlugin().getDescription().getName().equals(name)) {
                    plugin = null;

                    Bukkit.getLogger().info("[Fe] Vault support disabled.");
                }
            }
        }
    }
}
