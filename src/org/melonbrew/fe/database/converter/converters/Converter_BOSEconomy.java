package org.melonbrew.fe.database.converter.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.converter.ConverterType;

public class Converter_BOSEconomy implements Converter {
	private final Fe plugin;

	public Converter_BOSEconomy(Fe plugin){
		this.plugin = plugin;
	}

	public String getName(){
		return "BOSEconomy";
	}

	public boolean convert(ConverterType converterType){
		File accountsFile = new File("plugins/BOSEconomy/accounts.txt");

		if (!accountsFile.exists()){
			return false;
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(accountsFile));

			String line = null;

			String name = null;

			String type = null;

			while ((line = reader.readLine()) != null){
				line = line.trim();

				if (line.endsWith(" {")){
					name = line.replace(" {", "");
				}else if (line.equalsIgnoreCase("}")){
					name = null;

					type = null;
				}

				if (name != null){
					if (line.startsWith("type ")){
						type = line.replace("type ", "");
					}

					if (line.startsWith("money ") && type != null && type.equalsIgnoreCase("player")){
						double money = Double.parseDouble(line.replace("money ", ""));

						plugin.getAPI().createAccount(name).setMoney(money);
					}
				}
			}

			reader.close();
		} catch (Exception e){
			return false;
		}

		return true;
	}

	public ConverterType[] getConverterTypes() {
		return new ConverterType[]{ConverterType.FLAT_FILE};
	}
}
