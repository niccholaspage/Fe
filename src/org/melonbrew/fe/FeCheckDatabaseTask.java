package org.melonbrew.fe;

public class FeCheckDatabaseTask implements Runnable {
	private final API api;
	
	public FeCheckDatabaseTask(Fe plugin){
		api = plugin.getAPI();
	}
	
	public void run(){
		api.accountExists("gaben");
	}
}
