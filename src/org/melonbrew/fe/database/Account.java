package org.melonbrew.fe.database;

import java.text.DecimalFormat;

public class Account {
	private final String name;
	
	private final Database database;
	
	public Account(String name, Database database){
		this.name = name;
		
		this.database = database;
	}
	
	public String getName(){
		return name;
	}
	
	public double getMoney(){
		return database.loadAccountMoney(name);
	}
	
	public void withdraw(double amount){
		setMoney(getMoney() - amount);
	}
	
	public void deposit(double amount){
		setMoney(getMoney() + amount);
	}
	
	public double getMoneyRounded(double amount){
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		return Double.valueOf(twoDForm.format(amount));
	}
	
	public void setMoney(double money){
		double currentMoney = getMoney();
		
		if (currentMoney == money){
			return;
		}
		
		if (money < 0){
			money = 0;
		}
		
		currentMoney = getMoneyRounded(money);
		
		save(currentMoney);
	}
	
	public boolean has(double amount){
		return getMoney() >= amount;
	}
	
	public void save(double money){
		database.saveAccount(name, money);
	}
}
