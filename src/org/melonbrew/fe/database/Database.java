package org.melonbrew.fe.database;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;

import java.util.*;

public abstract class Database {
    private final Fe plugin;
    private final Set<Account> cachedAccounts;
    protected boolean cacheAccounts;

    public Database(Fe plugin) {
        this.plugin = plugin;

        this.cachedAccounts = new HashSet<Account>();
    }

    public boolean init() {
        this.cacheAccounts = plugin.getAPI().getCacheAccounts();

        return false;
    }

    public List<Account> getTopAccounts(int size) {
        List<Account> topAccounts = loadTopAccounts(size * 2);

        if (!cachedAccounts.isEmpty()) {
            for (Account account : cachedAccounts) {
                topAccounts.remove(account);
            }

            List<Account> cachedTopAccounts = new ArrayList<Account>(cachedAccounts);

            Collections.sort(cachedTopAccounts, new Comparator<Account>() {
                public int compare(Account account1, Account account2) {
                    return (int) (account2.getMoney() - account1.getMoney());
                }
            });

            if (cachedAccounts.size() > size) {
                cachedTopAccounts = cachedTopAccounts.subList(0, size);
            }

            topAccounts.addAll(cachedTopAccounts);
        }

        Collections.sort(topAccounts, new Comparator<Account>() {
            public int compare(Account account1, Account account2) {
                return (int) (account2.getMoney() - account1.getMoney());
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

    public void close() {
        for (Account account : new HashSet<Account>(cachedAccounts)) {
            account.save(account.getMoney());

            removeCachedAccount(account);
        }
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
        Account account = new Account(name, uuid, plugin, this);

        account.setMoney(money);

        if (cacheAccounts()) {
            cachedAccounts.add(account);
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
        for (Account account : cachedAccounts) {
            if (account.getName().equals(name)) {
                return account;
            }
        }

        return null;
    }

    public boolean removeCachedAccount(Account account) {
        return cachedAccounts.remove(account);
    }
}
