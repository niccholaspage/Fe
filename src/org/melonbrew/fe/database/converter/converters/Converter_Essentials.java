package org.melonbrew.fe.database.converter.converters;

import java.io.File;
import java.io.FilenameFilter;

import org.bukkit.configuration.file.YamlConfiguration;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.converter.Converter;

public class Converter_Essentials extends Converter {
	public boolean isFlatFile(){
		return true;
	}
	
	public boolean convertFlatFile(Fe plugin){
		File accountsFolder = new File("plugins/Essentials/userdata/");
		
		if (!accountsFolder.isDirectory()){
			return false;
		}
		
		File[] accounts = accountsFolder.listFiles(new FilenameFilter(){
			public boolean accept(File file, String name){
				return name.toLowerCase().endsWith(".yml");
			}
		});
		
		for (File account : accounts){
			YamlConfiguration config = YamlConfiguration.loadConfiguration(account);
			
			String name = account.getName().replace(".yml", "");
			
			double money = config.getDouble("money", -1);
			
			if (money != -1){
				plugin.getAPI().createAccount(name).setMoney(money);
			}
		}
		
		return true;
	}
}
