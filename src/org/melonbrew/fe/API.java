package org.melonbrew.fe;

import org.bukkit.ChatColor;
import org.melonbrew.fe.database.Account;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class API {
    private final Fe plugin;

    public API(Fe plugin) {
        this.plugin = plugin;
    }

    public List<Account> getTopAccounts() {
        return plugin.getFeDatabase().getTopAccounts(plugin.getConfig().getInt("topsize"));
    }

    public List<Account> getTopAccounts(int size) {
        return plugin.getFeDatabase().getTopAccounts(size);
    }

    public List<Account> getAccounts() {
        return plugin.getFeDatabase().getAccounts();
    }

    public double getDefaultHoldings() {
        return plugin.getConfig().getDouble("holdings");
    }

    public double getMaxHoldings() {
        return plugin.getConfig().getDouble("maxholdings");
    }

    public String getCurrencyPrefix() {
        return plugin.getConfig().getString("currency.prefix");
    }

    public boolean isCurrencyNegative() {
        return plugin.getConfig().getBoolean("currency.negative");
    }

    public String getCurrencyMajorSingle() {
        return plugin.getConfig().getString("currency.major.single");
    }

    public String getCurrencyMajorMultiple() {
        return plugin.getConfig().getString("currency.major.multiple");
    }

    public boolean isMinorCurrencyEnabled() {
        return plugin.getConfig().getBoolean("currency.minor.enabled");
    }

    public String getCurrencyMinorSingle() {
        return plugin.getConfig().getString("currency.minor.single");
    }

    public String getCurrencyMinorMultiple() {
        return plugin.getConfig().getString("currency.minor.multiple");
    }

    public boolean getCacheAccounts() {
        return plugin.getConfig().getBoolean("cacheaccounts");
    }

    public Account createAccount(String name, String uuid) {
        return plugin.getFeDatabase().createAccount(name, uuid);
    }

    public void removeAccount(String name, String uuid) {
        plugin.getFeDatabase().removeAccount(name, uuid);
    }

    public Account getAccount(String name, String uuid) {
        return plugin.getFeDatabase().getAccount(name, uuid);
    }

    public boolean accountExists(String name, String uuid) {
        return plugin.getFeDatabase().accountExists(name, uuid);
    }

    public String formatNoColor(double amount) {
        return ChatColor.stripColor(format(amount));
    }

    private String formatValue(double value) {
        boolean isWholeNumber = value == Math.round(value);

        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);

        formatSymbols.setDecimalSeparator('.');

        String pattern = isWholeNumber ? "###,###.###" : "###,##0.00";

        DecimalFormat df = new DecimalFormat(pattern, formatSymbols);

        return df.format(value);
    }

    public String format(double amount) {
        amount = getMoneyRounded(amount);

        String suffix = " ";

        if (isMinorCurrencyEnabled() && amount > 0.00 && amount < 1.0) {
            if (amount == 0.01) {
                suffix += getCurrencyMinorSingle();
            } else if (amount < 1.0) {
                suffix += getCurrencyMinorMultiple();
            }

            amount *= 100;
        } else if (amount == 1.0) {
            suffix += getCurrencyMajorSingle();
        } else {
            suffix += getCurrencyMajorMultiple();
        }

        if (suffix.equalsIgnoreCase(" ")) {
            suffix = "";
        }

        return Phrase.SECONDARY_COLOR.parse() + getCurrencyPrefix() + Phrase.PRIMARY_COLOR.parse() + formatValue(amount) + Phrase.SECONDARY_COLOR.parse() + suffix;
    }

    public double getMoneyRounded(double amount) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");

        String formattedAmount = twoDForm.format(amount);

        formattedAmount = formattedAmount.replace(",", ".");

        return Double.parseDouble(formattedAmount);
    }

    public String formatNoColor(Account account) {
        return ChatColor.stripColor(format(account));
    }

    public String format(Account account) {
        return format(account.getMoney());
    }

    public void clean() {
        plugin.getFeDatabase().clean();
    }
}
