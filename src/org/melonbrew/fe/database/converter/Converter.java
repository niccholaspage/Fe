package org.melonbrew.fe.database.converter;

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
	
	public boolean convertFlatFile(){
		return false;
	}
	
	public boolean convertMySQL(){
		return false;
	}
	
	public boolean mySQLtoFlatFile(){
		return false;
	}
}
