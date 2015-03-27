package com.niccholaspage.Fe;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;
import com.niccholaspage.Fe.database.Account;
import com.niccholaspage.Fe.database.Database;
import com.niccholaspage.Fe.database.databases.MySQLDB;
import com.niccholaspage.Fe.database.databases.SQLiteDB;
import com.niccholaspage.Fe.listeners.FePlayerListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Fe extends JavaPlugin
{
	private final Set<Database> databases;
	private API api;
	private Database database;
	public Fe()
	{
		databases = new HashSet<>();
	}
	@Override
	public void onEnable()
	{
		getDataFolder().mkdirs();
		Phrase.init(this);
		databases.add(new MySQLDB(this));
		databases.add(new SQLiteDB(this));
		for(Database db : databases)
		{
			String name = db.getConfigName();
			ConfigurationSection section = getConfig().getConfigurationSection(name);
			if(section == null)
				section = getConfig().createSection(name);
			db.getConfigDefaults(section);
			if(section.getKeys(false).isEmpty())
				getConfig().set(name, null);
		}
		getConfig().options().copyDefaults(true);
		getConfig().options().header("Fe Config - loyloy.io\n"
		+ "holdings - The amount of money that players will start out with\n"
		+ "prefix - The message prefix\n"
		+ "currency - The single and multiple names for the currency\n"
		+ "type - The type of database used (sqlite, mysql, or mongo)");
		saveConfig();
		api = new API(this);
		if(!setupDatabase())
			return;
		getCommand("fe").setExecutor(new FeCommand(this));
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new FePlayerListener(this), this);
		setupVault();
		loadMetrics();
		reloadConfig();
		// Auto Clean On Startup
		if(api.isAutoClean())
		{
			api.clean();
			log(Phrase.ACCOUNT_CLEANED);
		}
	}
	public void log(String message)
	{
		getLogger().info(message);
	}
	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		getFeDatabase().close();
	}
	public void log(Phrase phrase, String... args)
	{
		log(phrase.parse(args));
	}
	public Database getFeDatabase()
	{
		return database;
	}
	public boolean addDatabase(Database database)
	{
		return databases.add(database);
	}
	public Set<Database> getDatabases()
	{
		return new HashSet<>(databases);
	}
	public API getAPI()
	{
		return api;
	}
	private boolean setupDatabase()
	{
		String type = getConfig().getString("type");
		database = null;
		for(Database db : databases)
			if(type.equalsIgnoreCase(db.getConfigName()))
			{
				this.database = db;
				break;
			}
		if(database == null)
		{
			log(Phrase.DATABASE_TYPE_DOES_NOT_EXIST);
			return false;
		}
		if(!database.init())
		{
			log(Phrase.DATABASE_FAILURE_DISABLE);
			setEnabled(false);
			return false;
		}
		return true;
	}
	private void setupPhrases()
	{
		File phrasesFile = new File(getDataFolder(), "phrases.yml");
		for(Phrase phrase : Phrase.values())
			phrase.reset();
		if(!phrasesFile.exists())
			return;
		YamlConfiguration phrasesConfig = YamlConfiguration.loadConfiguration(phrasesFile);
		for(Phrase phrase : Phrase.values())
		{
			String phraseConfigName = phrase.getConfigName();
			String phraseMessage = phrasesConfig.getString(phraseConfigName);
			if(phraseMessage == null)
				phraseMessage = phrase.parse();
			phrase.setMessage(phraseMessage);
		}
	}
	@Override
	public void reloadConfig()
	{
		super.reloadConfig();
		String oldCurrencySingle = getConfig().getString("currency.single");
		String oldCurrencyMultiple = getConfig().getString("currency.multiple");
		if(oldCurrencySingle != null)
		{
			getConfig().set("currency.major.single", oldCurrencySingle);
			getConfig().set("currency.single", null);
		}
		if(oldCurrencyMultiple != null)
		{
			getConfig().set("currency.major.multiple", oldCurrencyMultiple);
			getConfig().set("currency.multiple", null);
		}
		if(!getConfig().isSet("autoclean"))
			getConfig().set("autoclean", true);
		// Temporarily remove cache and updates.
		if(getConfig().isSet("cacheaccounts"))
			getConfig().set("cacheaccounts", null);
		if(getConfig().getBoolean("updatecheck"))
			getConfig().set("updatecheck", null);
		setupPhrases();
		saveConfig();
	}
	@SuppressWarnings("deprecation")
	public Account getShortenedAccount(String name)
	{
		Account account = getAPI().getAccount(name, null);
		if(account == null)
		{
			Player player = getServer().getPlayer(name);
			if(player != null)
				account = getAPI().getAccount(player.getName(), null);
		}
		return account;
	}
	public String getMessagePrefix()
	{
		String third = Phrase.TERTIARY_COLOR.parse();
		return third + "[" + Phrase.PRIMARY_COLOR.parse() + "$1" + third + "] " + Phrase.SECONDARY_COLOR.parse();
	}
	public String getEqualMessage(String inBetween, int length)
	{
		return getEqualMessage(inBetween, length, length);
	}
	public String getEqualMessage(String inBetween, int length, int length2)
	{
		String equals = getEndEqualMessage(length);
		String end = getEndEqualMessage(length2);
		String third = Phrase.TERTIARY_COLOR.parse();
		return equals + third + "[" + Phrase.PRIMARY_COLOR.parse() + inBetween + third + "]" + end;
	}
	public String getEndEqualMessage(int length)
	{
		String message = Phrase.SECONDARY_COLOR.parse() + "";
		for(int i = 0; i < length; i ++)
			message += "=";
		return message;
	}
	private void loadMetrics()
	{
		try
		{
			Metrics metrics = new Metrics(this);
			Graph databaseGraph = metrics.createGraph("Database Engine");
			databaseGraph.addPlotter(new Plotter(getFeDatabase().getName())
			{
				@Override
				public int getValue()
				{
					return 1;
				}
			});
			Graph defaultHoldings = metrics.createGraph("Default Holdings");
			defaultHoldings.addPlotter(new Plotter(getAPI().getDefaultHoldings() + "")
			{
				@Override
				public int getValue()
				{
					return 1;
				}
			});
			Graph maxHoldings = metrics.createGraph("Max Holdings");
			String maxHolding = getAPI().getMaxHoldings() + "";
			if(getAPI().getMaxHoldings() == -1)
				maxHolding = "Unlimited";
			maxHoldings.addPlotter(new Plotter(maxHolding)
			{
				@Override
				public int getValue()
				{
					return 1;
				}
			});
			metrics.start();
		} catch(IOException e) {
		}
	}
	private void setupVault()
	{
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if(vault == null)
			return;
		getServer().getServicesManager().register(Economy.class, new VaultHandler(this), this, ServicePriority.Highest);
	}
}
