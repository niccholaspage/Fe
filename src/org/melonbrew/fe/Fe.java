package org.melonbrew.fe;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
import org.melonbrew.fe.database.databases.MySQLDB;
import org.melonbrew.fe.database.databases.SQLiteDB;
import org.melonbrew.fe.listeners.FePlayerListener;

public class Fe extends JavaPlugin {
	private Logger log;
	
	private API api;
	
	private Database database;
	
	private Set<Database> databases;
	
	private double currentVersion;
	
	private double latestVersion;
	
	private String latestVersionString;
	
	public void onEnable(){
		log = getServer().getLogger();
		
		getDataFolder().mkdirs();
		
		new FePlayerListener(this);
		
		databases = new HashSet<Database>();
		
		databases.add(new MySQLDB(this));
		databases.add(new SQLiteDB(this));
		
		getConfig().options().copyDefaults(true);
		
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
		
		String currentVersionString = getDescription().getVersion();
		
		currentVersion = versionToDouble(currentVersionString);
		
		setLatestVersion(currentVersion);
		
		setLatestVersionString(currentVersionString);
		
		getCommand("fe").setExecutor(new FeCommand(this));
		
		if (getConfig().getBoolean("updatecheck")){
			getServer().getScheduler().scheduleAsyncDelayedTask(this, new UpdateCheck(this));
		}
		
		loadMetrics();
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
	
	private void loadMetrics(){
		try {
			Metrics metrics = new Metrics(this);
			
			Graph databaseGraph = metrics.createGraph("Database Engine");
			
			databaseGraph.addPlotter(new Plotter(database.getName()){
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
            
            metrics.start();
		} catch (IOException e){
			
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
	
	public void log(Phrase phrase, String... args){
		log(phrase.parse(args));
	}
	
	public Database getFeDatabase(){
		return database;
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
