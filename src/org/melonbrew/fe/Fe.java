package org.melonbrew.fe;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;
import org.melonbrew.fe.database.databases.MySQLDB;
import org.melonbrew.fe.database.databases.SQLiteDB;

import com.niccholaspage.Metro.base.MetroPlugin;
import com.niccholaspage.Metro.base.config.Config;
import com.niccholaspage.Metro.base.config.ConfigSection;
import com.niccholaspage.Metro.base.player.Player;

public class Fe extends MetroPlugin {
	private API api;
	
	private Database database;
	
	private Set<Database> databases;
	
	private double currentVersion;
	
	private double latestVersion;
	
	private String latestVersionString;
	
	public void onEnable(){
		getDataFolder().mkdirs();
		
		Phrase.init(this);
		
		databases = new HashSet<Database>();
		
		databases.add(new MySQLDB(this));
		databases.add(new SQLiteDB(this));
		
		for (Database database : databases){
			String name = database.getConfigName();
			
			ConfigSection section = getConfig().getConfigSection(name);
			
			if (section == null){
				section = getConfig().createConfigSection(name);
			}
			
			database.getConfigDefaults(section);
			
			if (section.getKeys(false).isEmpty()){
				getConfig().setValue(name, null);
			}
		}
		
		getConfig().setHeader("Fe Config - meloncraft.com\n" +
				"holdings - The amount of money that players will start out with\n" +
				"prefix - The message prefix\n" +
				"currency - The single and multiple names for the currency\n" +
				"type - The type of database used (sqlite or mysql)\n");
		
		getConfig().save();
		
		api = new API(this);
		
		if (!setupDatabase()){
			return;
		}
		
		String currentVersionString = getResources().getVersion();
		
		currentVersion = versionToDouble(currentVersionString);
		
		setLatestVersion(currentVersion);
		
		setLatestVersionString(currentVersionString);
		
		getResources().registerCommand("fe", new FeCommand(this));
		
		if (getConfig().getBoolean("updatecheck")){
			getServer().getScheduler().runTaskAsynchronously(this, new UpdateCheck(this));
		}
	}
	
	@Override
	public void log(String message){
		super.log("[Fe] " + message);
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
		getFeDatabase().close();
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
			
			getServer().disablePlugin(this);
			
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

		Config phrasesConfig = getResources().newConfig(phrasesFile);

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
		getConfig().reload();
		
		String oldCurrencySingle = getConfig().getString("currency.single");
		
		String oldCurrencyMultiple = getConfig().getString("currency.multiple");
		
		if (oldCurrencySingle != null){
			getConfig().setValue("currency.major.single", oldCurrencySingle);
			
			getConfig().setValue("currency.single", null);
		}
		
		if (oldCurrencyMultiple != null){
			getConfig().setValue("currency.major.multiple", oldCurrencyMultiple);
			
			getConfig().setValue("currency.multiple", null);
		}
		
		setupPhrases();
		
		getConfig().save();
	}
	
	public Account getShortenedAccount(String name){
		Account account = getAPI().getAccount(name);
		
		if (account == null){
			Player player = getServer().getOnlinePlayer(name, false);
			
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
}
