package com.niccholaspage.Fe.API;

import com.niccholaspage.Fe.Fe;
import com.niccholaspage.Fe.Phrases;
import org.bukkit.ChatColor;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class API
{
	private final Fe plugin;
	public API(Fe plugin)
	{
		this.plugin = plugin;
	}
	public List<Account> getTopAccounts()
	{
		return plugin.getCurrentDatabase().getTopAccounts(plugin.settings.getShowTop());
	}
	public List<Account> getTopAccounts(int size)
	{
		return plugin.getCurrentDatabase().getTopAccounts(size);
	}
	public List<Account> getAccounts()
	{
		return plugin.getCurrentDatabase().getAccounts();
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
	@Deprecated
	public Account createAccount(String name, String uuid)
	{
		return updateAccount(name, uuid);
	}
	public Account updateAccount(String name, String uuid)
	{
		return plugin.getCurrentDatabase().updateAccount(name, uuid);
	}
	public void removeAccount(String name, String uuid)
	{
		plugin.getCurrentDatabase().removeAccount(name, uuid);
	}
	public Account getAccount(String name, String uuid)
	{
		return plugin.getCurrentDatabase().getAccount(name, uuid);
	}
	public boolean accountExists(String name, String uuid)
	{
		return plugin.getCurrentDatabase().accountExists(name, uuid);
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
	public void clean()
	{
		plugin.getCurrentDatabase().clean();
	}
}
