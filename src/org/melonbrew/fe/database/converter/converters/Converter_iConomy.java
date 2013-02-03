package org.melonbrew.fe.database.converter.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.SQLibrary.Database;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.databases.SQLDB;

public class Converter_iConomy extends Converter {
	private final Fe plugin;
	
	public Converter_iConomy(Fe plugin){
		this.plugin = plugin;
	}
	
	public String getName(){
		return "iConomy";
	}

	public boolean isFlatFile(){
		return true;
	}

	public boolean isMySQL(){
		return true;
	}

	public boolean convertFlatFile(){
		File accountsFile = new File("plugins/iConomy/accounts.mini");

		if (!accountsFile.exists()){
			return false;
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(accountsFile));

			String line = null;

			while ((line = reader.readLine()) != null){
				String[] args = line.split(" ");

				StringBuilder builder = new StringBuilder();

				double money = -1;

				for (int i = 0; i < args.length; i++){
					if (args[i].startsWith("balance:")){
						money = Double.parseDouble(args[i].replace("balance:", ""));

						break;
					}

					builder.append(args[i]).append(" ");
				}

				builder.deleteCharAt(builder.length() - 1);

				plugin.getAPI().createAccount(builder.toString()).setMoney(money);
			}

			reader.close();
		} catch (Exception e){
			return false;
		}

		return true;
	}

	public boolean convertMySQL(){
		Database database = ((SQLDB) plugin.getFeDatabase()).getDatabase();

		try {
			database.query("ALTER TABLE iconomy DROP COLUMN id;");
			database.query("ALTER TABLE iconomy DROP COLUMN status;");
			database.query("ALTER TABLE iconomy CHANGE username name varchar(64);");
			database.query("ALTER TABLE iconomy CHANGE balance money double;");

			database.query("RENAME TABLE iconomy TO fe_accounts;");
		}catch (Exception e){
			return false;
		}

		return true;
	}
}
