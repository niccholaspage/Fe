package org.melonbrew.fe.database.converter;

public interface Converter {
	public String getName();
	
	public boolean convert(ConverterType type);
	
	public ConverterType[] getConverterTypes();
}
