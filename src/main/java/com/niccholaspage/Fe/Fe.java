package com.niccholaspage.Fe;

import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.Database;
import com.niccholaspage.Fe.API.FeAPI;
import com.niccholaspage.Fe.Databases.MySQLDB;
import com.niccholaspage.Fe.Databases.SQLiteDB;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

public class Fe extends JavaPlugin
{
	public  final FeAPI api = new FeAPI(this);
	public  final FeSettings settings = new FeSettings(this);
	private final FeListener listener = new FeListener(this);
	private final FeCommands commands = new FeCommands(this);
	private final Set<Database> databases = new HashSet<>();
	private Database database;
	@Override
	public void onLoad()
	{
		settings.onLoad();
		Phrases.initialize(this);
	}
	@Override
	public void onEnable()
	{
		settings.onEnable();
		Phrases.setupPhrases(new File(getDataFolder(), "phrases.yml"));
		databases.add(new MySQLDB(this));
		databases.add(new SQLiteDB(this));
		for(Database db : databases)
			db.getConfigDefaults(db.getConfigSection());
		getConfig().options().copyDefaults(true);
		saveConfig();
		if(!setupDatabase())
		{
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		getCommand("fe").setExecutor(commands);
		getServer().getPluginManager().registerEvents(listener, this);
		setupVault();
		loadMetrics();
		// Load all accounts
		database.loadAccounts();
		// Autoclean on startup
		if(api.isAutoClean())
		{
			api.clean();
			log(Phrases.ACCOUNT_CLEANED);
		}
	}
	private boolean setupDatabase()
	{
		String type = settings.getDatabase();
		database = null;
		for(Database db : databases)
			if(type.equalsIgnoreCase(db.getConfigName()))
			{
				this.database = db;
				break;
			}
		if(database == null)
		{
			log(Phrases.DATABASE_TYPE_DOES_NOT_EXIST);
			return false;
		}
		log("Using " + database.getName());
		if(!database.initialize())
		{
			log(Phrases.DATABASE_FAILURE_DISABLE);
			setEnabled(false);
			return false;
		}
		return true;
	}
	private void setupVault()
	{
		if(getServer().getPluginManager().getPlugin("Vault") != null)
			getServer().getServicesManager().register(Economy.class, new VaultHandler(this), this, ServicePriority.Highest);
	}
	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		getDB().close();
		getServer().getServicesManager().unregisterAll(this);
	}
	public FeAPI getAPI()
	{
		return api;
	}
	public Database getDB()
	{
		return database;
	}
	public Database findDB(String targetName)
	{
		for(Database db : databases)
			if(db.getConfigName().equals(targetName))
				return db;
		return null;
	}
	public void log(String message)
	{
		getLogger().info(message);
	}
	public void log(Phrases phrase, String... args)
	{
		log(phrase.parse(args));
	}
	public Account getShortenedAccount(String name)
	{
		Account account = api.getAccount(name);
		if(account == null)
		{
			final Player player = getServer().getPlayer(name);
			if(player != null)
			{
				account = api.getAccount(player.getUniqueId().toString());
				if(account == null)
					account = api.getAccount(player.getName());
			}
		}
		return account;
	}
	public String getMessagePrefix()
	{
		String third = Phrases.TERTIARY_COLOR.parse();
		return third + "[" + Phrases.PRIMARY_COLOR.parse() + "$1" + third + "] " + Phrases.SECONDARY_COLOR.parse();
	}
	public String getEqualMessage(String inBetween, int length)
	{
		return getEqualMessage(inBetween, length, length);
	}
	public String getEqualMessage(String inBetween, int length, int length2)
	{
		String equals = getEndEqualMessage(length);
		String end = getEndEqualMessage(length2);
		String third = Phrases.TERTIARY_COLOR.parse();
		return equals + third + "[" + Phrases.PRIMARY_COLOR.parse() + inBetween + third + "]" + end;
	}
	public String getEndEqualMessage(int length)
	{
		String message = Phrases.SECONDARY_COLOR.parse() + "";
		for(int i = 0; i < length; i ++)
			message += "=";
		return message;
	}
	private void loadMetrics()
	{
		try
		{
			Metrics metrics = new Metrics(this);
			final Graph databaseGraph = metrics.createGraph("Database Engine");
			databaseGraph.addPlotter(new Plotter(getDB().getName())
			{
				@Override
				public int getValue()
				{
					return 1;
				}
			});
			final Graph defaultHoldings = metrics.createGraph("Default Holdings");
			defaultHoldings.addPlotter(new Plotter(Double.toString(api.getDefaultHoldings()))
			{
				@Override
				public int getValue()
				{
					return 1;
				}
			});
			final Graph maxHoldings = metrics.createGraph("Max Holdings");
			String maxHolding = Double.toString(api.getMaxHoldings());
			if(api.getMaxHoldings() == -1)
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
}
