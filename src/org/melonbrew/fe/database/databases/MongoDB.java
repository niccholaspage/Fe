package org.melonbrew.fe.database.databases;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MongoDB extends Database {
	private final Fe plugin;

	private MongoClient mongoClient;

	private static final String ACCOUNTS_COLLECTION = "accounts";

	public MongoDB(Fe plugin) {
		super(plugin);

		this.plugin = plugin;
	}

	@Override
	public boolean init() {
		try {
			mongoClient = new MongoClient(getConfigSection().getString("host"), getConfigSection().getInt("port"));
		} catch (UnknownHostException e){
			return false;
		}

		if (getDatabase() == null || !getDatabase().isAuthenticated()){
			return false;
		}

		return true;
	}

	public DB getDatabase(){
		DB database = mongoClient.getDB(getConfigSection().getString("database"));

		database.authenticate(getConfigSection().getString("user"), getConfigSection().getString("password").toCharArray());

		return database;
	}

	public Double loadAccountMoney(String name){
		DBCollection collection = getDatabase().getCollection(ACCOUNTS_COLLECTION);

		DBObject userObject = collection.findOne(new BasicDBObject("name", name));

		if (userObject == null){
			return null;
		}

		return ((BasicDBObject) userObject).getDouble("money");
	}

	public void removeAccount(String name){
		DBCollection collection = getDatabase().getCollection(ACCOUNTS_COLLECTION);

		DBObject oldUserObject = collection.findOne(new BasicDBObject("name", name));

		if (oldUserObject != null){
			collection.remove(oldUserObject);
		}
	}

	public void saveAccount(String name, double money){
		DBCollection collection = getDatabase().getCollection(ACCOUNTS_COLLECTION);

		DBObject oldUserObject = collection.findOne(new BasicDBObject("name", name));

		if (oldUserObject != null){
			collection.remove(oldUserObject);
		}

		collection.insert(new BasicDBObject("name", name).append("money", money));
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
	public List<Account> getTopAccounts(int size) {
		DBCursor cursor = getDatabase().getCollection(ACCOUNTS_COLLECTION).find().sort(new BasicDBObject("money", -1)).limit(size);

		List<Account> topAccounts = new ArrayList<Account>();

		Iterator<DBObject> iterator = cursor.iterator();

		while (iterator.hasNext()){
			BasicDBObject topAccountObject = (BasicDBObject) iterator.next();

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

		Iterator<DBObject> iterator = cursor.iterator();

		while (iterator.hasNext()){
			BasicDBObject accountObject = (BasicDBObject) iterator.next();

			Account account = new Account(accountObject.getString("name"), plugin, this);

			account.setMoney(accountObject.getDouble("money"));

			accounts.add(account);
		}

		return accounts;
	}

	public void clean() {
		DBCursor cursor = getDatabase().getCollection(ACCOUNTS_COLLECTION).find((new BasicDBObject("money", plugin.getAPI().getDefaultHoldings())));

		Iterator<DBObject> iterator = cursor.iterator();

		while (iterator.hasNext()){
			getDatabase().getCollection(ACCOUNTS_COLLECTION).remove(iterator.next());
		}
	}

	@Override
	public void close() {

	}
}
