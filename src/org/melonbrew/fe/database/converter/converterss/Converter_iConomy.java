package org.melonbrew.fe.database.converter.converterss;

import org.melonbrew.fe.Fe;
import org.melonbrew.fe.SQLibrary.Database;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.converter.Response;
import org.melonbrew.fe.database.converter.ResponseType;
import org.melonbrew.fe.database.databases.SQLDB;

public class Converter_iConomy extends Converter {
	public String getName(){
		return "iConomy";
	}
	
	public boolean isFlatFile(){
		return false;
	}
	
	public boolean isMySQL(){
		return true;
	}
	
	public Response convertFlatFile(Fe plugin){
		return new Response(ResponseType.NOT_SUPPORTED);
	}
	
	public Response convertMySQL(Fe plugin){
		Database database = ((SQLDB) plugin.getFeDatabase()).getDatabase();
		
		try {
			database.query("ALTER TABLE iconomy DROP COLUMN id;");
			database.query("ALTER TABLE iconomy DROP COLUMN status;");
			database.query("ALTER TABLE iconomy CHANGE username name varchar(64);");
			database.query("ALTER TABLE iconomy CHANGE balance money double;");

			database.query("RENAME TABLE iconomy TO fe_accounts;");
		}catch (Exception e){
			return new Response(ResponseType.FAILED);
		}
		
		return new Response(ResponseType.SUCCESS);
	}
	
	public boolean mySQLtoFlatFile(){
		return true;
	}
}
