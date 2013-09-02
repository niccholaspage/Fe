package org.melonbrew.fe.database.converter.converters;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.converter.ConverterType;
import org.melonbrew.fe.database.databases.*;

public class Converter_Fe implements Converter {
	private final Fe plugin;

	public Converter_Fe(Fe plugin){
		this.plugin = plugin;
	}

	public String getName(){
		return "Fe";
	}

	public boolean convert(ConverterType type){
		if (type == ConverterType.FLAT_FILE){
			if (plugin.getFeDatabase() instanceof SQLiteDB){
				return false;
			}

			return convert(new SQLiteDB(plugin));
		}else if (type == ConverterType.MYSQL) {
			if (plugin.getFeDatabase() instanceof MySQLDB){
				return false;
			}

			return convert(new MySQLDB(plugin));
		}else {
			if (plugin.getFeDatabase() instanceof MongoDB){
				return false;
			}

			return convert(new MongoDB(plugin));
		}
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

	public ConverterType[] getConverterTypes(){
		return new ConverterType[]{ConverterType.FLAT_FILE, ConverterType.MYSQL, ConverterType.MONGO};
	}
}
