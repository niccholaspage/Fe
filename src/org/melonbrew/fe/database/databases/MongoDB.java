package org.melonbrew.fe.database.databases;

import com.mongodb.*;
import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MongoDB extends Database {
    private static final String ACCOUNTS_COLLECTION = "accounts";
    private static final String VERSION_COLLECTION = "version";
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

        if (getDatabase() == null || !getDatabase().isAuthenticated()) {
            return false;
        }

        if (getVersion() == 0) {
            if (!convertToUUID()) {
                return false;
            }

            setVersion(1);
        }

        return true;
    }

    private DB getDatabase() {
        DB database = mongoClient.getDB(getConfigSection().getString("database"));

        database.authenticate(getConfigSection().getString("user"), getConfigSection().getString("password").toCharArray());

        return database;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Double loadAccountMoney(String name, String uuid) {
        DBObject userObject = getDatabase().getCollection(ACCOUNTS_COLLECTION).findOne(new BasicDBObject(uuid != null ? "uuid" : "name", uuid != null ? uuid : name));

        if (userObject == null) {
            return null;
        }

        return ((BasicDBObject) userObject).getDouble("money");
    }

    @Override
    public void removeAccount(String name, String uuid) {
        super.removeAccount(name, uuid);

        DBCollection collection = getDatabase().getCollection(ACCOUNTS_COLLECTION);

        DBObject oldUserObject = collection.findOne(new BasicDBObject(uuid != null ? "uuid" : "name", uuid != null ? uuid : name));

        if (oldUserObject != null) {
            collection.remove(oldUserObject);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void saveAccount(String name, String uuid, double money) {
        DBCollection collection = getDatabase().getCollection(ACCOUNTS_COLLECTION);

        DBObject query = collection.findOne(new BasicDBObject(uuid != null ? "uuid" : "name", uuid != null ? uuid : name));

        if (query != null) {
            collection.remove(query);
        }

        collection.insert(new BasicDBObject("name", name).append("uuid", uuid).append("money", money));
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
    public int getVersion() {
        DBCollection collection = getDatabase().getCollection(VERSION_COLLECTION);

        BasicDBObject query = (BasicDBObject) collection.findOne(new BasicDBObject());

        return query.getInt("version", 0);
    }

    @Override
    public void setVersion(int version) {
        DBCollection collection = getDatabase().getCollection(VERSION_COLLECTION);

        collection.findAndRemove(new BasicDBObject());
    }

    @Override
    public List<Account> loadTopAccounts(int size) {
        DBCursor cursor = getDatabase().getCollection(ACCOUNTS_COLLECTION).find().sort(new BasicDBObject("money", -1)).limit(size);

        List<Account> topAccounts = new ArrayList<Account>();

        for (DBObject aCursor : cursor) {
            BasicDBObject topAccountObject = (BasicDBObject) aCursor;

            Account account = new Account(plugin, topAccountObject.getString("name"), topAccountObject.getString("uuid"), this);

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

            Account account = new Account(plugin, accountObject.getString("name"), accountObject.getString("uuid"), this);

            account.setMoney(accountObject.getDouble("money"));

            accounts.add(account);
        }

        return accounts;
    }

    @Override
    public void clean() {
        DBCursor cursor = getDatabase().getCollection(ACCOUNTS_COLLECTION).find((new BasicDBObject("money", plugin.getAPI().getDefaultHoldings())));

        for (DBObject aCursor : cursor) {
            getDatabase().getCollection(ACCOUNTS_COLLECTION).remove(aCursor);
        }
    }

    @Override
    public void removeAllAccounts() {
        getDatabase().getCollection(VERSION_COLLECTION).findAndRemove(new BasicDBObject());
    }
}
