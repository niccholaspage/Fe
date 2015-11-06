package org.melonbrew.fe.database;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.UUIDFetcher;

import java.util.*;

public abstract class Database {
    private final Fe plugin;
    private final HashMap<UUID, Account> cachedAccountsUUID;
    private final HashMap<String, Account> cachedAccountsUsername;
    protected boolean cacheAccounts;

    public Database(Fe plugin) {
        this.plugin = plugin;

        this.cachedAccountsUUID = new HashMap<UUID, Account>();
        this.cachedAccountsUsername = new HashMap<String, Account>();
    }

    public boolean init() {
        this.cacheAccounts = plugin.getAPI().getCacheAccounts();

        return false;
    }

    public List<Account> getTopAccounts(int size) {
        List<Account> topAccounts = this.loadTopAccounts(size * 2);
        if (!this.cachedAccountsUUID.isEmpty()) {
            final Account lowest = topAccounts.get(topAccounts.size() - 1);
            final List<Account> cachedTopAccounts = new ArrayList<Account>();
            for (final Account account : this.cachedAccountsUUID.values()) {
                topAccounts.remove(account);
                if (lowest.getMoney() <= account.getMoney()) {
                    cachedTopAccounts.add(account);
                }
            }
            topAccounts.addAll(cachedTopAccounts);
        }
        Collections.sort(topAccounts, new Comparator<Account>() {
            @Override
            public int compare(final Account account1, final Account account2) {
                return (int)(account2.getMoney() - account1.getMoney());
            }
        });
        if (topAccounts.size() > size) {
            topAccounts = topAccounts.subList(0, size);
        }
        return topAccounts;
    }

    public abstract List<Account> loadTopAccounts(int size);

    public abstract List<Account> getAccounts();

    public abstract Double loadAccountMoney(String name, String uuid);

    protected abstract void saveAccount(String name, String uuid, double money);

    public void removeAccount(String name, String uuid) {
        Account account = getCachedAccount(name, uuid);

        if (account != null) {
            removeCachedAccount(account);
        }
    }

    public abstract void getConfigDefaults(ConfigurationSection section);

    public abstract void clean();

    public void removeAllAccounts() {
        cachedAccountsUUID.clear();
        cachedAccountsUsername.clear();
    }

    protected boolean convertToUUID() {
        if (!plugin.getServer().getOnlineMode()) {
            //Disable plugin?
        }

        plugin.log(Phrase.STARTING_UUID_CONVERSION);

        List<Account> accounts = getAccounts();

        Map<String, Double> accountMonies = new HashMap<String, Double>();

        for (Account account : accounts) {
            accountMonies.put(account.getName(), account.getMoney());
        }

        List<String> names = new ArrayList<String>();

        for (Account account : accounts) {
            names.add(account.getName());
        }

        UUIDFetcher fetcher = new UUIDFetcher(names);

        Map<String, UUID> response;

        try {
            response = fetcher.call();

            removeAllAccounts();

            for (String name : response.keySet()) {
                for (String accountName : new HashMap<String, Double>(accountMonies).keySet()) {
                    if (accountName.equalsIgnoreCase(name)) {
                        saveAccount(name, response.get(name).toString(), accountMonies.get(accountName));

                        accountMonies.remove(accountName);
                    }
                }
            }

            for (String accountName : accountMonies.keySet()) {
                saveAccount(accountName, null, accountMonies.get(accountName));
            }
        } catch (Exception e) {
            e.printStackTrace();

            plugin.log(Phrase.UUID_CONVERSION_FAILED);

            plugin.getServer().getPluginManager().disablePlugin(plugin);

            return false;
        }

        plugin.log(Phrase.UUID_CONVERSION_SUCCEEDED);

        return true;
    }

    public void close() {
        for (final Account account : this.cachedAccountsUUID.values()) {
            account.save(account.getMoney());
        }
        this.cachedAccountsUUID.clear();
        this.cachedAccountsUsername.clear();
    }

    public abstract String getName();

    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }

    public ConfigurationSection getConfigSection() {
        return plugin.getConfig().getConfigurationSection(getConfigName());
    }

    public Account getAccount(String name, String uuid) {
        Account account = getCachedAccount(name, uuid);

        if (account != null) {
            return account;
        }

        Double money = loadAccountMoney(name, uuid);

        if (money == null) {
            return null;
        } else {
            return createAndAddAccount(name, uuid, money);
        }
    }

    public Account createAccount(String name, String uuid) {
        Account account = getAccount(name, uuid);

        if (account == null) {
            account = createAndAddAccount(name, uuid, plugin.getAPI().getDefaultHoldings());
        }

        return account;
    }

    private Account createAndAddAccount(String name, String uuid, double money) {
        Account account = new Account(plugin, name, uuid, this);

        account.setMoney(money);

        if (cacheAccounts()) {
            Player player = plugin.getServer().getPlayerExact(name);

            if (player != null) {
                this.cachedAccountsUsername.put(name, account);
                this.cachedAccountsUUID.put(UUID.fromString(uuid), account);
            }
        }

        return account;
    }

    public boolean accountExists(String name, String uuid) {
        return getAccount(name, uuid) != null;
    }

    public boolean cacheAccounts() {
        return cacheAccounts;
    }

    public Account getCachedAccount(String name, String uuid) {
        if (uuid != null) {
            return this.cachedAccountsUUID.get(UUID.fromString(uuid));
        }
        return this.cachedAccountsUsername.get(name);
    }

    public boolean removeCachedAccount(Account account) {
        this.cachedAccountsUUID.remove(UUID.fromString(account.getUUID()));
        this.cachedAccountsUsername.remove(account.getName());
        return true;
    }

    public abstract int getVersion();

    public abstract void setVersion(int version);
}
