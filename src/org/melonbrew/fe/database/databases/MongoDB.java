package org.melonbrew.fe.database.databases;

import com.mongodb.*;
import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MongoDB extends Database {
    private static final String ACCOUNTS_COLLECTION = "accounts";
    private final Fe plugin;
    private MongoClient mongoClient;

    public MongoDB(Fe plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public boolean init() {
        super.init();

        try {
            mongoClient = new MongoClient(getConfigSection().getString("host"), getConfigSection().getInt("port"));
        } catch (UnknownHostException e) {
            return false;
        }

        return !(getDatabase() == null || !getDatabase().isAuthenticated());

    }

    private DB getDatabase() {
        DB database = mongoClient.getDB(getConfigSection().getString("database"));

        database.authenticate(getConfigSection().getString("user"), getConfigSection().getString("password").toCharArray());

        return database;
    }

    @SuppressWarnings("deprecation")
    public Double loadAccountMoney(String name) {
        UUID uuid = plugin.getUUID(name);

        DBObject query = getQueryFromNameOrUUID(name, uuid);

        if (query == null) {
            return null;
        }

        return ((BasicDBObject) query).getDouble("money");
    }

    private DBObject getQueryFromNameOrUUID(String name, UUID uuid) {
        DBObject nameClause = new BasicDBObject("name", name);

        DBObject uuidClause = new BasicDBObject("uuid", uuid.toString());

        BasicDBList or = new BasicDBList();

        or.add(nameClause);

        or.add(uuidClause);

        return getDatabase().getCollection(ACCOUNTS_COLLECTION).findOne(new BasicDBObject("$or", or));
    }

    public void removeAccount(String name) {
        super.removeAccount(name);

        DBCollection collection = getDatabase().getCollection(ACCOUNTS_COLLECTION);

        DBObject oldUserObject = collection.findOne(new BasicDBObject("name", name));

        if (oldUserObject != null) {
            collection.remove(oldUserObject);
        }
    }

    @SuppressWarnings("deprecation")
    public void saveAccount(String name, double money) {
        UUID uuid = plugin.getUUID(name);

        DBObject query = getQueryFromNameOrUUID(name, uuid);

        DBCollection collection = getDatabase().getCollection(ACCOUNTS_COLLECTION);

        if (query != null) {
            collection.remove(query);
        }

        collection.insert(new BasicDBObject("name", name).append("uuid", uuid.toString()).append("money", money));
    }

    @Override
    public void getConfigDefaults(ConfigurationSection section) {
        section.addDefault("host", "localhost");

        section.addDefault("port", 27017);

        section.addDefault("user", "root");

        section.addDefault("password", "minecraft");

        section.addDefault("database", "Fe");
    }

    @Override
    public String getName() {
        return "Mongo";
    }

    @Override
    public List<Account> loadTopAccounts(int size) {
        DBCursor cursor = getDatabase().getCollection(ACCOUNTS_COLLECTION).find().sort(new BasicDBObject("money", -1)).limit(size);

        List<Account> topAccounts = new ArrayList<Account>();

        for (DBObject aCursor : cursor) {
            BasicDBObject topAccountObject = (BasicDBObject) aCursor;

            Account account = new Account(topAccountObject.getString("name"), plugin, this);

            account.setMoney(topAccountObject.getDouble("money"));

            topAccounts.add(account);
        }

        return topAccounts;
    }

    @Override
    public List<Account> getAccounts() {
        DBCursor cursor = getDatabase().getCollection(ACCOUNTS_COLLECTION).find().sort(new BasicDBObject("money", -1));

        List<Account> accounts = new ArrayList<Account>();

        for (DBObject aCursor : cursor) {
            BasicDBObject accountObject = (BasicDBObject) aCursor;

            Account account = new Account(accountObject.getString("name"), plugin, this);

            account.setMoney(accountObject.getDouble("money"));

            accounts.add(account);
        }

        return accounts;
    }

    public void clean() {
        DBCursor cursor = getDatabase().getCollection(ACCOUNTS_COLLECTION).find((new BasicDBObject("money", plugin.getAPI().getDefaultHoldings())));

        for (DBObject aCursor : cursor) {
            getDatabase().getCollection(ACCOUNTS_COLLECTION).remove(aCursor);
        }
    }
}
