package org.melonbrew.fe;

import java.text.DecimalFormat;
import java.util.List;

import org.bukkit.ChatColor;
import org.melonbrew.fe.database.Account;

public class API {
	private final Fe plugin;

	private final DecimalFormat moneyFormat;

	public API(Fe plugin){
		this.plugin = plugin;

		moneyFormat = new DecimalFormat("###,###.###");
	}

	public List<Account> getTopAccounts(){
		return plugin.getFeDatabase().getTopAccounts();
	}

	public double getDefaultHoldings(){
		return plugin.getConfig().getDouble("holdings");
	}

	public double getMaxHoldings(){
		return plugin.getConfig().getDouble("maxholdings");
	}

	public String getCurrencyPrefix(){
		return plugin.getConfig().getString("currency.prefix");
	}

	public String getCurrencySingle(){
		return plugin.getConfig().getString("currency.single");
	}

	public String getCurrencyMultiple(){
		return plugin.getConfig().getString("currency.multiple");
	}

	public Account createAccount(String name){
		return plugin.getFeDatabase().createAccount(name.toLowerCase());
	}

	public void removeAccount(String name){
		plugin.getFeDatabase().removeAccount(name.toLowerCase());
	}

	public Account getAccount(String name){
		return plugin.getFeDatabase().getAccount(name.toLowerCase());
	}

	public boolean accountExists(String name){
		return plugin.getFeDatabase().accountExists(name.toLowerCase());
	}

	public String formatNoColor(double amount){
		return ChatColor.stripColor(format(amount));
	}

	public String format(double amount){
		amount = getMoneyRounded(amount);
		
		String suffix = " ";
		
		if (amount == 1.0){
			suffix += getCurrencySingle();
		}else {
			suffix += getCurrencyMultiple();
		}
		
		if (suffix.equalsIgnoreCase(" ")){
			suffix = "";
		}

		return Phrase.SECONDARY_COLOR.parse() + getCurrencyPrefix() + Phrase.PRIMARY_COLOR.parse() + moneyFormat.format(amount) + Phrase.SECONDARY_COLOR.parse() + suffix;
	}

	public double getMoneyRounded(double amount){
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		String formattedAmount = twoDForm.format(amount);

		formattedAmount = formattedAmount.replace(",", ".");

		return Double.valueOf(formattedAmount);
	}

	public String formatNoColor(Account account){
		return ChatColor.stripColor(format(account));
	}

	public String format(Account account){
		return format(account.getMoney());
	}

	public void clean(){
		plugin.getFeDatabase().clean();
	}
}
