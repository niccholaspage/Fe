package com.niccholaspage.Fe;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FeSettings
{
	private final JavaPlugin fe;
	public FeSettings(JavaPlugin plugin)
	{
		this.fe = plugin;
	}
	public void onLoad()
	{
		fe.getDataFolder().mkdirs();
		fe.saveDefaultConfig();
	}
	public void onEnable()
	{
		fe.reloadConfig();
		final FileConfiguration config = fe.getConfig();
		int version = config.getInt("internal.version", 0);
		switch(version)
		{
			case 0:
				fe.getLogger().info("[Fe] I will migrate config.yml to the newest format now.");
				try
				{
					final File source = new File(fe.getDataFolder(), "config.yml");
					final File target = new File(fe.getDataFolder(), "config.yml.v0-saved");
					if(!target.isFile())
					{
						Files.copy(source, target);
						fe.getLogger().info("[Fe] Backup of config.yml saved into config.yml.v0-saved.");
					}
				} catch(IOException ex) {
				}
				config.options().header(null);
				migrate_v0_to_v1(config);
				config.set("internal.version", 1);
				fe.getLogger().info("[Fe] Configuration has been migrated successfully.");
				// Insert new version updaters RIGHT HERE (using new "case #:" statements)
				fe.saveConfig();
				fe.reloadConfig();
			default:
				// Configuration is up-to-date
				break;
		}
	}
	public boolean debug()
	{
		return fe.getConfig().getBoolean("settings.debug", false);
	}
	public void debug(boolean enable)
	{
		fe.getConfig().set("settings.debug", enable);
		fe.saveConfig();
	}
	public double getDefaultHoldings()
	{
		return fe.getConfig().getDouble("settings.new-player-holdings", 10.00);
	}
	public double getMaximumHoldings()
	{
		return fe.getConfig().getDouble("settings.maximum-holdings", -1.0);
	}
	public String getPrefix()
	{
		return fe.getConfig().getString("settings.prefix", "Fe");
	}
	public int getShowTop()
	{
		return fe.getConfig().getInt("settings.show-top", 10);
	}
	public boolean isAutoClean()
	{
		return fe.getConfig().getBoolean("settings.autoclean", true);
	}
	public String getCurrencyPrefix()
	{
		return fe.getConfig().getString("settings.currency.prefix");
	}
	public boolean isCurrencyNegative()
	{
		return fe.getConfig().getBoolean("settings.currency.negative");
	}
	public String getCurrencyMajorSingle()
	{
		return fe.getConfig().getString("settings.currency.major.single");
	}
	public String getCurrencyMajorMultiple()
	{
		return fe.getConfig().getString("settings.currency.major.multiple");
	}
	public boolean isMinorCurrencyEnabled()
	{
		return fe.getConfig().getBoolean("settings.currency.minor.enabled");
	}
	public String getCurrencyMinorSingle()
	{
		return fe.getConfig().getString("settings.currency.minor.single");
	}
	public String getCurrencyMinorMultiple()
	{
		return fe.getConfig().getString("settings.currency.minor.multiple");
	}
	public String getDatabase()
	{
		return fe.getConfig().getString("settings.database-type", "sqlite");
	}
	public ConfigurationSection getDatabaseSection(String name)
	{
		ConfigurationSection result = fe.getConfig().getConfigurationSection("settings." + name);
		if(result == null)
			result = fe.getConfig().createSection("settings." + name);
		return result;
	}
	private void migrate_v0_to_v1(FileConfiguration config)
	{
		config.set("internal.do-not-touch", null);
		final double new_player_holdings = config.getDouble("holdings", 10.00);
		config.set("holdings", null);
		config.set("settings.new-player-holdings", new_player_holdings);
		final double maximum_holdings = config.getDouble("maxholdings", -1.0);
		config.set("maxholdings", null);
		config.set("settings.maximum-holdings", maximum_holdings);
		final String prefix = config.getString("prefix", "Fe");
		config.set("prefix", null);
		config.set("settings.prefix", prefix);
		final int show_top = config.getInt("topsize", 10);
		config.set("topsize", null);
		config.set("settings.show-top", show_top);
		final ConfigurationSection currency = config.getConfigurationSection("currency");
		config.set("currency", null);
		config.set("settings.currency", currency);
		final String database_type = config.getString("type", "sqlite");
		config.set("type", null);
		config.set("settings.database-type", database_type);
		final boolean autoclean = config.getBoolean("autoclean", true);
		config.set("autoclean", null);
		config.set("settings.autoclean", autoclean);
		ConfigurationSection mysql = config.getConfigurationSection("mysql");
		if(mysql == null)
			mysql = config.createSection("mysql");
		mysql_v0_to_v1(mysql);
		config.set("mysql", null);
		config.set("settings.mysql", mysql);
		config.set("sqlite", null);
		// Remove old unused config nodes
		config.set("cacheaccounts", null);
		config.set("updatecheck", null);
	}
	private void mysql_v0_to_v1(ConfigurationSection mysql)
	{
		final String url = new StringBuilder()
			.append(mysql.getString("host", "localhost"))
			.append(":")
			.append(mysql.getInt("port", 3306))
			.append("/")
			.append(mysql.getString("database", "minecraft"))
			.toString();
		mysql.set("host", null);
		mysql.set("port", null);
		mysql.set("database", null);
		mysql.set("connection.database", url);
		final String username = mysql.getString("user", "user1");
		mysql.set("user", null);
		mysql.set("connection.username", username);
		final String password = mysql.getString("password", "pass1");
		mysql.set("password", null);
		mysql.set("connection.password", password);
	}
}
