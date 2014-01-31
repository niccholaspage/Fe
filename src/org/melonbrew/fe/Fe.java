package org.melonbrew.fe;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.melonbrew.fe.Metrics.Graph;
import org.melonbrew.fe.Metrics.Plotter;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;
import org.melonbrew.fe.database.databases.FlatDB;
import org.melonbrew.fe.database.databases.ItemDB;
import org.melonbrew.fe.database.databases.MongoDB;
import org.melonbrew.fe.database.databases.MySQLDB;
import org.melonbrew.fe.database.databases.SQLiteDB;
import org.melonbrew.fe.database.databases.SQLDB;
import org.melonbrew.fe.listeners.FePlayerListener;

public class Fe extends JavaPlugin {
	private API api;

	private Database database;

	private Set<Database> databases;

	private double currentVersion;

	private double latestVersion;

	private String latestVersionString;

	public Fe(){
		databases = new HashSet<Database>();
	}

	public void onEnable(){
		getDataFolder().mkdirs();

		Phrase.init(this);

		databases.add(new MySQLDB(this));
		databases.add(new SQLiteDB(this));
		databases.add(new MongoDB(this));
		databases.add(new ItemDB(this));
		databases.add(new FlatDB(this));

		for (Database database : databases){
			String name = database.getConfigName();

			ConfigurationSection section = getConfig().getConfigurationSection(name);

			if (section == null){
				section = getConfig().createSection(name);
			}

			database.getConfigDefaults(section);

			if (section.getKeys(false).isEmpty()){
				getConfig().set(name, null);
			}
		}

		getConfig().options().copyDefaults(true);

		getConfig().options().header("Fe Config - meloncraft.com\n" +
				"holdings - The amount of money that players will start out with\n" +
				"prefix - The message prefix\n" +
				"currency - The single and multiple names for the currency\n" +
				"type - The type of database used (sqlite, mysql, mongo, or item)\n");

		saveConfig();

		api = new API(this);

		if (!setupDatabase()){
			return;
		}

		String currentVersionString = getDescription().getVersion();

		currentVersion = versionToDouble(currentVersionString);

		setLatestVersion(currentVersion);

		setLatestVersionString(currentVersionString);

		getCommand("fe").setExecutor(new FeCommand(this));

		new FePlayerListener(this);

		setupVault();

		loadMetrics();

		if (getConfig().getBoolean("updatecheck")){
			getServer().getScheduler().runTaskAsynchronously(this, new UpdateCheck(this));
		}

		if (database instanceof SQLDB){
			getServer().getScheduler().runTaskTimer(this, new Runnable(){
				public void run(){
					((SQLDB) database).checkConnection();
				}
			}, 60 * 20, 60 * 20);
		}
	}

	public void log(String message){
		getLogger().info("[Fe] " + message);
	}

	public double versionToDouble(String version){
		boolean isSnapshot = version.endsWith("-SNAPSHOT");

		version = version.replace("-SNAPSHOT", "");

		String fixed = "";

		boolean doneFirst = false;

		for (int i = 0; i < version.length(); i++){
			char c = version.charAt(i);

			if (c == '.'){
				if (doneFirst){
					continue;
				}else {
					doneFirst = true;
				}
			}

			fixed += c;
		}

		try {
			double ret = Double.parseDouble(fixed);

			if (isSnapshot){
				ret -= 0.001;
			}

			return ret;
		}catch (NumberFormatException e){
			return -1;
		}
	}

	protected void setLatestVersion(double latestVersion){
		this.latestVersion = latestVersion;
	}

	protected void setLatestVersionString(String latestVersionString){
		this.latestVersionString = latestVersionString;
	}

	public double getLatestVersion(){
		return latestVersion;
	}

	public String getLatestVersionString(){
		return latestVersionString;
	}

	public boolean isUpdated(){
		return currentVersion >= latestVersion;
	}

	public void onDisable(){
		getServer().getScheduler().cancelTasks(this);

		getFeDatabase().close();
	}

	public void log(Phrase phrase, String... args){
		log(phrase.parse(args));
	}

	public Database getFeDatabase(){
		return database;
	}

	public Set<Database> getDatabases(){
		return databases;
	}

	public API getAPI(){
		return api;
	}

	private boolean setupDatabase(){
		String type = getConfig().getString("type");

		database = null;

		for (Database database : databases){
			if (type.equalsIgnoreCase(database.getConfigName())){
				try {
					this.database = database;

					break;
				} catch (Exception e){

				}
			}
		}

		if (database == null){
			log(Phrase.DATABASE_TYPE_DOES_NOT_EXIST);

			return false;
		}

		if (!database.init()){
			log(Phrase.DATABASE_FAILURE_DISABLE);

			getServer().getPluginManager().disablePlugin(this);

			return false;
		}

		return true;
	}

	private void setupPhrases(){
		File phrasesFile = new File(getDataFolder(), "phrases.yml");

		for (Phrase phrase : Phrase.values()){
			phrase.reset();
		}

		if (!phrasesFile.exists()){
			return;
		}

		YamlConfiguration phrasesConfig = YamlConfiguration.loadConfiguration(phrasesFile);

		for (Phrase phrase : Phrase.values()){
			String phraseConfigName = phrase.getConfigName();

			String phraseMessage = phrasesConfig.getString(phraseConfigName);

			if (phraseMessage == null){
				phraseMessage = phrase.parse();
			}

			phrase.setMessage(phraseMessage);
		}
	}

	public void reloadConfig(){
		super.reloadConfig();

		String oldCurrencySingle = getConfig().getString("currency.single");

		String oldCurrencyMultiple = getConfig().getString("currency.multiple");

		if (oldCurrencySingle != null){
			getConfig().set("currency.major.single", oldCurrencySingle);

			getConfig().set("currency.single", null);
		}

		if (oldCurrencyMultiple != null){
			getConfig().set("currency.major.multiple", oldCurrencyMultiple);

			getConfig().set("currency.multiple", null);
		}

		setupPhrases();

		saveConfig();
	}

	public Account getShortenedAccount(String name){
		Account account = getAPI().getAccount(name);

		if (account == null){
			Player player = getServer().getPlayer(name);

			if (player != null){
				account = getAPI().getAccount(player.getName());
			}
		}

		return account;
	}

	public String getMessagePrefix(){
		String third = Phrase.TERTIARY_COLOR.parse();

		return third  + "[" + Phrase.PRIMARY_COLOR.parse() + "$1" + third + "] " + Phrase.SECONDARY_COLOR.parse();
	}

	public String getEqualMessage(String inBetween, int length){
		return getEqualMessage(inBetween, length, length);
	}

	public String getEqualMessage(String inBetween, int length, int length2){
		String equals = getEndEqualMessage(length);

		String end = getEndEqualMessage(length2);

		String third = Phrase.TERTIARY_COLOR.parse();

		return equals + third + "[" + Phrase.PRIMARY_COLOR.parse() + inBetween + third + "]" + end;
	}

	public String getEndEqualMessage(int length){
		String message = Phrase.SECONDARY_COLOR.parse() + "";

		for (int i = 0; i < length; i++){
			message += "=";
		}

		return message;
	}

	private void loadMetrics(){
		try {
			Metrics metrics = new Metrics(this);

			Graph databaseGraph = metrics.createGraph("Database Engine");

			databaseGraph.addPlotter(new Plotter(getFeDatabase().getName()){
				public int getValue(){
					return 1;
				}
			});

			Graph defaultHoldings = metrics.createGraph("Default Holdings");

			defaultHoldings.addPlotter(new Plotter(getAPI().getDefaultHoldings() + ""){
				public int getValue(){
					return 1;
				}
			});

			Graph maxHoldings = metrics.createGraph("Max Holdings");

			String maxHolding = getAPI().getMaxHoldings() + "";

			if (getAPI().getMaxHoldings() == -1){
				maxHolding = "Unlimited";
			}

			maxHoldings.addPlotter(new Plotter(maxHolding){
				public int getValue(){
					return 1;
				}
			});

			metrics.start();
		} catch (IOException e){

		}
	}

	private void setupVault(){
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");

		if (vault == null){
			return;
		}

		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

		if (economyProvider != null){
			getServer().getServicesManager().unregister(economyProvider.getProvider());
		}

		getServer().getServicesManager().register(Economy.class, new Economy_Fe(this), this, ServicePriority.Highest);
	}
}
