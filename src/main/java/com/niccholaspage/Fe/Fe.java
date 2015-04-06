package com.niccholaspage.Fe;

import com.niccholaspage.Fe.API.API;
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
import com.niccholaspage.Fe.API.Account;
import com.niccholaspage.Fe.API.Database;
import com.niccholaspage.Fe.Databases.MySQLDB;
import com.niccholaspage.Fe.Databases.SQLiteDB;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Fe extends JavaPlugin
{
	private final API api = new API(this);
	private final Set<Database> databases = new HashSet<>();
	private final FeCommands commands = new FeCommands(this);
	private final FePlayerListener listener = new FePlayerListener(this);
	private Database database;
	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		Phrase.init(this);
		databases.add(new MySQLDB(this));
		databases.add(new SQLiteDB(this));
	}
	@Override
	public void onEnable()
	{
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
		if(!setupDatabase())
			return;
		getCommand("fe").setExecutor(commands);
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(listener, this);
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
	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		getCurrentDatabase().close();
		getServer().getServicesManager().unregisterAll(this);
	}
	public void log(String message)
	{
		getLogger().info(message);
	}
	public void log(Phrase phrase, String... args)
	{
		log(phrase.parse(args));
	}
	public Database getCurrentDatabase()
	{
		return database;
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
		Phrase.setupPhrases(new File(getDataFolder(), "phrases.yml"));
		saveConfig();
	}
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
			databaseGraph.addPlotter(new Plotter(getCurrentDatabase().getName())
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
