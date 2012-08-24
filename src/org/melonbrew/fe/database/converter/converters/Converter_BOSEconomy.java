package org.melonbrew.fe.database.converter.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.converter.Converter;

public class Converter_BOSEconomy extends Converter {
	public String getName(){
		return "BOSEconomy";
	}
	
	public boolean isFlatFile(){
		return true;
	}
	
	public boolean convertFlatFile(Fe plugin){
		File accountsFile = new File("plugins/BOSEconomy/accounts.txt");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(accountsFile));
			
			String line = null;
			
			String name = null;
			
			while ((line = reader.readLine()) != null){
				if (line.endsWith(" {")){
					name = line.replace(" {", "");
				}else if (line.equalsIgnoreCase("}")){
					name = null;
				}
				
				if (name != null && line.startsWith("money ")){
					double money = Double.parseDouble(line.replace("money ", ""));
					
					plugin.getAPI().createAccount(name).setMoney(money);
				}
			}
			
			reader.close();
		} catch (Exception e){
			return false;
		}
		
		return true;
	}
}
