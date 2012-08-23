package org.melonbrew.fe.database.converter;

import org.melonbrew.fe.Fe;

public class Converter {
	public String getName(){
		return "Converter";
	}
	
	public boolean isFlatFile(){
		return false;
	}
	
	public boolean isMySQL(){
		return false;
	}
	
	public Response convertFlatFile(Fe plugin){
		return null;
	}
	
	public Response convertMySQL(Fe plugin){
		return null;
	}
	
	public boolean mySQLtoFlatFile(){
		return false;
	}
}
