package org.melonbrew.fe.database.converter.converters;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.databases.*;

public class Converter_Fe extends Converter {
	private final Fe plugin;
	
	public Converter_Fe(Fe plugin){
		this.plugin = plugin;
	}
	
	public String getName(){
		return "Fe";
	}
	
	public boolean isFlatFile(){
		return true;
	}
	
	public boolean isMySQL(){
		return true;
	}
	
	public boolean mySQLtoFlatFile(){
		return true;
	}
	
	public boolean convert(Database database){
		if (!database.init()){
			return false;
		}
		
		for (Account account : database.getAccounts()){
			plugin.getAPI().createAccount(account.getName()).setMoney(account.getMoney());
		}
		
		return true;
	}
	
	public boolean convertFlatFile(){
		if (plugin.getFeDatabase() instanceof SQLiteDB){
			return false;
		}
		
		return convert(new MySQLDB(plugin));
	}
	
	public boolean convertMySQL(){
		if (plugin.getFeDatabase() instanceof MySQLDB){
			return false;
		}
		
		return convert(new SQLiteDB(plugin));
	}
}
