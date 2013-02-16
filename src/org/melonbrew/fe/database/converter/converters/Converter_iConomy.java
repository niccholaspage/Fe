package org.melonbrew.fe.database.converter.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.melonbrew.fe.Fe;
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
		SQLDB database = ((SQLDB) plugin.getFeDatabase());
		
		try {
			database.query("CREATE TABLE temp_fe_accounts LIKE iconomy;");
			database.query("INSERT temp_fe_accounts SELECT * FROM iconomy;");
			database.query("ALTER TABLE temp_fe_accounts DROP COLUMN id;");
			database.query("ALTER TABLE temp_fe_accounts DROP COLUMN status;");
			database.query("ALTER TABLE temp_fe_accounts CHANGE username name varchar(64) NOT NULL;");
			database.query("ALTER TABLE temp_fe_accounts CHANGE balance money double NOT NULL;");
			
			database.query("DROP TABLE IF EXISTS fe_accounts");

			database.query("RENAME TABLE temp_fe_accounts TO fe_accounts;");
		}catch (Exception e){
			return false;
		}

		return true;
	}
}
