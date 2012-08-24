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
	
	public boolean convertFlatFile(Fe plugin){
		return false;
	}
	
	public boolean convertMySQL(Fe plugin){
		return false;
	}
	
	public boolean mySQLtoFlatFile(){
		return false;
	}
}
