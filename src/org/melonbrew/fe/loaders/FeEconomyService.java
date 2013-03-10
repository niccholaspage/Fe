package org.melonbrew.fe.loaders;

import java.util.ArrayList;
import java.util.List;

import org.melonbrew.fe.API;
import org.melonbrew.fe.database.Account;
import org.spout.api.plugin.services.EconomyService;

public class FeEconomyService extends EconomyService {
	private final API api;
	
	public FeEconomyService(API api){
		this.api = api;
	}
	
	public boolean canHold(String name, double amount){
		if (!api.accountExists(name)){
			return false;
		}
		
		Account account = api.getAccount(name);
		
		if (api.getMaxHoldings() != -1 && account.getMoney() + amount > api.getMaxHoldings()){
			return false;
		}
		
		return true;
	}

	public boolean canHold(String name, double money, String currency) throws UnknownCurrencyException {
		return canHold(name, money);
	}
	
	public boolean canWithdraw(String name, double amount){
		if (!api.accountExists(name)){
			return false;
		}
		
		return true;
	}
	
	public boolean canWithdraw(String name, double amount, String currency) throws UnknownCurrencyException {
		return canWithdraw(name, amount);
	}

	public boolean create(String name){
		return api.createAccount(name) != null;
	}

	public boolean exists(String name){
		return api.accountExists(name);
	}

	public String format(double amount){
		return api.formatNoColor(amount);
	}

	public String format(String currency, double amount){
		return format(amount);
	}

	
	public String formatShort(double amount){
		return format(amount);
	}

	
	public String formatShort(String currency, double amount){
		return format(amount);
	}

	
	public double get(String name){
		if (!api.accountExists(name)){
			return -1;
		}
		
		return api.getAccount(name).getMoney();
	}

	
	public double get(String name, String currency) throws UnknownCurrencyException {
		return get(name);
	}
	
	public String getCurrencyNamePlural(){
		return api.getCurrencyMajorMultiple();
	}

	
	public String getCurrencyNamePlural(String currency) throws UnknownCurrencyException {
		return getCurrencyNamePlural();
	}

	
	public String getCurrencyNameSingular(){
		return api.getCurrencyMajorSingle();
	}

	
	public List<String> getCurrencyNames(){
		List<String> currencyNames = new ArrayList<String>();
		
		currencyNames.add(getCurrencyNamePlural());
		
		return currencyNames;
	}

	
	public String getCurrencySymbol(){
		return "";
	}

	
	public String getCurrencySymbol(String currency) throws UnknownCurrencyException {
		return getCurrencySymbol();
	}

	
	public double getExchangeRate(String currencyFrom, String currencyTo) throws UnknownCurrencyException {
		return 0;
	}

	
	public List<String> getTopAccounts(int start, int end, boolean playersOnly){
		List<String> topAccounts = new ArrayList<String>();
		
		for (Account account : api.getTopAccounts(5)){
			topAccounts.add(api.getReadName(account));
		}
		
		return topAccounts;
	}

	
	public List<String> getTopAccounts(int start, int end, String currency, boolean playersOnly) throws UnknownCurrencyException {
		return getTopAccounts(start, end, playersOnly);
	}

	public boolean has(String name, double amount){
		return api.getAccount(name).has(amount);
	}

	
	public boolean has(String name, double amount, String currency) throws UnknownCurrencyException {
		return has(name, amount);
	}

	public boolean hasMulticurrencySupport(){
		return false;
	}

	
	public int numSignificantDigits(){
		return 2;
	}

	
	protected boolean onDeposit(String name, double amount, String currency) throws UnknownCurrencyException {
		if (!api.accountExists(name)){
			return false;
		}
		
		api.getAccount(name).deposit(amount);
		
		return true;
	}

	
	protected boolean onWithdraw(String name, double amount, String currency) throws UnknownCurrencyException {
		if (!api.accountExists(name)){
			return false;
		}
		
		api.getAccount(name).withdraw(amount);
		
		return true;
	}

	
	public boolean remove(String name){
		if (!api.accountExists(name)){
			return false;
		}
		
		api.removeAccount(name);
		
		return true;
	}

}
