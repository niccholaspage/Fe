package org.melonbrew.fe;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;
import org.melonbrew.fe.database.databases.FlatFile;
import org.melonbrew.fe.database.databases.MySQLDB;
import org.melonbrew.fe.database.databases.SQLiteDB;
import org.melonbrew.fe.listeners.FePlayerListener;

public class Fe extends JavaPlugin {
	private Logger log;
	
	private API api;
	
	private Database database;
	
	private Map<String, Class<? extends Database>> databases;
	
	public void onEnable(){
		log = getServer().getLogger();
		
		getDataFolder().mkdirs();
		
		new FePlayerListener(this);
		
		databases = new HashMap<String, Class<? extends Database>>();
		
		databases.put("mysql", MySQLDB.class);
		databases.put("sqlite", SQLiteDB.class);
		
		getConfig().options().copyDefaults(true);
		
		getConfig().options().header("Fe Config - melonbrew.org\n" +
				"holdings - The amount of money that the player will start out with\n" +
				"prefix - The message prefix\n" +
				"currency - The single and multiple names for the currency\n" +
				"type - The type of database used (sqlite or mysql)\n");
		
		saveConfig();
		
		api = new API(this);
		
		if (!setupDatabase()){
			return;
		}
		
		setupVault();
		
		getCommand("fe").setExecutor(new FeCommand(this));
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
	
	public void onDisable(){
		getServer().getServicesManager().unregisterAll(this);
		
		getFeDatabase().close();
	}
	
	public void log(String message){
		log.info("[Fe] " + message);
	}
	
	public void log(Phrase phrase){
		log(phrase.parse());
	}
	
	public Database getFeDatabase(){
		return database;
	}
	
	public API getAPI(){
		return api;
	}
	
	private boolean setupDatabase(){
		return setupDatabase(null);
	}
	
	private boolean setupDatabase(List<Account> accounts){
		String type = getConfig().getString("type");
		
		database = null;
		
		if (type.equalsIgnoreCase("flatfile")){
			database = new FlatFile(this);
			
			boolean convert = false;
			
			if (database.init()){
				log("Converting flat file into SQLite...");
				
				getConfig().set("type", "sqlite");
				
				saveConfig();
				
				convert = true;
			}
			
			getConfig().set("type", "sqlite");
			
			saveConfig();
			
			if (convert){
				return setupDatabase(database.getTopAccounts());
			}
		}
		
		for (String name : databases.keySet()){
			if (type.equalsIgnoreCase(name)){
				try {
					database = databases.get(name).getDeclaredConstructor(Fe.class).newInstance(this);
					
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
		
		if (accounts != null){
			for (Account account : accounts){
				database.createAccount(account.getName()).setMoney(account.getMoney());
			}
			
			log("Finished conversion.");
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

		Set<String> keys = phrasesConfig.getKeys(false);

		for (Phrase phrase : Phrase.values()){
			String phraseConfigName = phrase.getConfigName();

			if (keys.contains(phraseConfigName)){
				phrase.setMessage(phrasesConfig.getString(phraseConfigName));
			}
		}
	}
	
	public void reloadConfig(){
		super.reloadConfig();
		
		setupPhrases();
	}
	
	public ConfigurationSection getMySQLConfig(){
		return getConfig().getConfigurationSection("mysql");
	}
	
	public String getReadName(Account account){
		return getReadName(account.getName());
	}
	
	public String getReadName(String name){
		name = name.toLowerCase();
		
		OfflinePlayer player = getServer().getOfflinePlayer(name);
		
		if (player != null){
			name = player.getName();
		}
		
		return name;
	}
	
	public String getMessagePrefix(){
		return ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + getConfig().getString("prefix") + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
	}
	
	public String getCurrencySingle(){
		return getConfig().getString("currency.single");
	}
	
	public String getCurrencyMultiple(){
		return getConfig().getString("currency.multiple");
	}
	
	public String getEqualMessage(String inBetween, int length){
		return getEqualMessage(inBetween, length, length);
	}
	
	public String getEqualMessage(String inBetween, int length, int length2){
		String equals = getEndEqualMessage(length);
		
		String end = getEndEqualMessage(length2);
		
		return equals + ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + inBetween + ChatColor.DARK_GRAY + "]" + end;
	}
	
	public String getEndEqualMessage(int length){
		String message = ChatColor.GRAY + "";
		
		for (int i = 0; i < length; i++){
			message += "=";
		}
		
		return message;
	}
}
