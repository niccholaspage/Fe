package com.niccholaspage.Fe.API;

import java.util.UUID;

public interface Account extends Comparable<Account>
{
	public UUID    getUUID();
	public String  getName();
	public void    setName(String name);
	public double  getMoney();
	public void    setMoney(double money);
	public boolean has(double amount);
	public boolean canReceive(double amount);
	public void    deposit(double amount);
	public void    withdraw(double amount);
}
