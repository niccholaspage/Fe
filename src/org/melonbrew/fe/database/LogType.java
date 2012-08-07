package org.melonbrew.fe.database;

public enum LogType {
	DEPOSIT(0),
	WITHDRAW(1),
	SET(2),
	GRANT(3);
	
	private final int id;
	
	private LogType(int id){
		this.id = id;
	}
	
	public int getID(){
		return id;
	}
}
