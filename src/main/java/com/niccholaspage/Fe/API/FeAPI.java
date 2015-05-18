package com.niccholaspage.Fe.API;

import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.ChatColor;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FeAPI
{
	private final Fe plugin;
	public FeAPI(Fe plugin)
	{
		this.plugin = plugin;
	}
	public double getDefaultHoldings()
	{
		return plugin.settings.getDefaultHoldings();
	}
	public double getMaxHoldings()
	{
		return plugin.settings.getMaximumHoldings();
	}
	public boolean isAutoClean()
	{
		return plugin.settings.isAutoClean();
	}
	public String getCurrencyPrefix()
	{
		return plugin.settings.getCurrencyPrefix();
	}
	public boolean isCurrencyNegative()
	{
		return plugin.settings.isCurrencyNegative();
	}
	public String getCurrencyMajorSingle()
	{
		return plugin.settings.getCurrencyMajorSingle();
	}
	public String getCurrencyMajorMultiple()
	{
		return plugin.settings.getCurrencyMajorMultiple();
	}
	public boolean isMinorCurrencyEnabled()
	{
		return plugin.settings.isMinorCurrencyEnabled();
	}
	public String getCurrencyMinorSingle()
	{
		return plugin.settings.getCurrencyMinorSingle();
	}
	public String getCurrencyMinorMultiple()
	{
		return plugin.settings.getCurrencyMinorMultiple();
	}
	public List<Account> getAccounts()
	{
		return plugin.getDB().loadAccounts();
	}
	public List<Account> getTopAccounts()
	{
		return plugin.getDB().getTopAccounts(plugin.settings.getShowTop());
	}
	public List<Account> getTopAccounts(int size)
	{
		return plugin.getDB().getTopAccounts(size);
	}
	public boolean accountExists(UUID uuid)
	{
		return plugin.getDB().accountExists(uuid);
	}
	public boolean accountExists(String name)
	{
		return plugin.getDB().accountExists(name);
	}
	public Account createAccount(UUID uuid)
	{
		return plugin.getDB().createAccount(uuid);
	}
	public Account createAccount(String name)
	{
		return plugin.getDB().createAccount(name);
	}
	public Account createAccount(UUID uuid, String name)
	{
		return plugin.getDB().createAccount(uuid, name);
	}
	public Account getAccount(UUID uuid)
	{
		return plugin.getDB().getAccount(uuid);
	}
	public Account getAccount(String name)
	{
		return plugin.getDB().getAccount(name);
	}
	public void renameAccount(Account account, String newName)
	{
		plugin.getDB().renameAccount(account, newName);
	}
	public void removeAccount(Account account)
	{
		plugin.getDB().removeAccount(account);
	}
	public void clean()
	{
		plugin.getDB().cleanAccountsWithDefaultHoldings();
	}
	public String formatNoColor(double amount)
	{
		return ChatColor.stripColor(format(amount));
	}
	private String formatValue(double value)
	{
		boolean isWholeNumber = value == Math.round(value);
		DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		formatSymbols.setDecimalSeparator('.');
		String pattern = isWholeNumber ? "###,###.###" : "###,##0.00";
		DecimalFormat df = new DecimalFormat(pattern, formatSymbols);
		return df.format(value);
	}
	public String format(double amount)
	{
		amount = getMoneyRounded(amount);
		String suffix = " ";
		if(isMinorCurrencyEnabled() && amount > 0.00 && amount < 1.0)
		{
			if(amount == 0.01)
				suffix += getCurrencyMinorSingle();
			else if(amount < 1.0)
				suffix += getCurrencyMinorMultiple();
			amount *= 100;
		} else
			if(amount == 1.0)
				suffix += getCurrencyMajorSingle();
			else
				suffix += getCurrencyMajorMultiple();
		if(suffix.equalsIgnoreCase(" "))
			suffix = "";
		return Phrases.SECONDARY_COLOR.parse() + getCurrencyPrefix() + Phrases.PRIMARY_COLOR.parse() + formatValue(amount) + Phrases.SECONDARY_COLOR.parse() + suffix;
	}
	public double getMoneyRounded(double amount)
	{
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		String formattedAmount = twoDForm.format(amount);
		formattedAmount = formattedAmount.replace(",", ".");
		return Double.valueOf(formattedAmount);
	}
	public String formatNoColor(Account account)
	{
		return ChatColor.stripColor(format(account));
	}
	public String format(Account account)
	{
		return format(account.getMoney());
	}
}
