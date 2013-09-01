package org.melonbrew.fe.database.converter;

import org.melonbrew.fe.Phrase;

public enum ConverterType {
	FLAT_FILE(Phrase.FLAT_FILE),
	MYSQL(Phrase.MYSQL),
	MONGO(Phrase.MONGO);
	
	private final Phrase phrase;
	
	private ConverterType(Phrase phrase){
		this.phrase = phrase;
	}
	
	public Phrase getPhrase(){
		return phrase;
	}
	
	public static ConverterType getType(String name){
		for (ConverterType type : values()){
			if (type.phrase.parse().replace(" ", "").equalsIgnoreCase(name)){
				return type;
			}
		}
		
		return null;
	}
}
