package com.niccholaspage.Fe;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Phrases
{
	DATABASE_TYPE_DOES_NOT_EXIST("That database type doesn't exist."),
	DATABASE_FAILURE_DISABLE    ("Database initialization has failed, disabling Fe."),
	COMMAND_NEEDS_ARGUMENTS     ("That command needs arguments."),
	COMMAND_NOT_CONSOLE         ("The command '$1' cannot be used in the console."),
	NO_PERMISSION_FOR_COMMAND   ("Sorry, you do not have permission to use that command."),
	ACCOUNT_HAS                 ("$1 has $2"),
	YOU_HAVE                    ("You have $1"),
	HELP                        ("Fe Help"),
	HELP_ARGUMENTS              ("$1 Required, $2 Optional"),
	RICH                        ("Rich List"),
	STARTING_UUID_CONVERSION    ("Starting UUID conversion."),
	UUID_CONVERSION_FAILED      ("UUID conversion failed, disabling Fe!"),
	UUID_CONVERSION_SUCCEEDED   ("UUID conversion has succeeded!"),
	CONFIG_RELOADED             ("The config has been reloaded."),
	DATABASE_MIGRATED           ("Database has been migrated."),
	DATABASE_THE_SAME           ("Please specify other target database type."),
	DATABASE_NOT_FOUND          ("Please specify one of existing dababase targets."),
	NOT_ENOUGH_MONEY            ("You don't have enough money."),
	ACCOUNT_DOES_NOT_EXIST      ("Sorry, that account does not exist."),
	YOUR_ACCOUNT_DOES_NOT_EXIST ("You don't have an account."),
	ACCOUNT_EXISTS              ("That account already exists."),
	ACCOUNT_CREATED             ("An account for $1 has been created."),
	ACCOUNT_REMOVED             ("An account for $1 has been removed."),
	MONEY_RECEIVE               ("You've received $1 from $2."),
	MONEY_SENT                  ("You've sent $1 to $2"),
	MAX_BALANCE_REACHED         ("$1 has reached the maximum balance."),
	PLAYER_SET_MONEY            ("You've set $1's balance to $2."),
	PLAYER_GRANT_MONEY          ("You've granted $1 to $2."),
	PLAYER_GRANTED_MONEY        ("$2 granted you $1."),
	PLAYER_DEDUCT_MONEY         ("You've deducted $1 from $2."),
	PLAYER_DEDUCTED_MONEY       ("$2 deducted $1 from your account."),
	ACCOUNT_CREATED_GRANT       ("Created an account for $1 and granted it $2"),
	NO_ACCOUNTS_EXIST           ("No accounts exist."),
	NAME_TOO_LONG               ("Sorry that name is too long."),
	ACCOUNT_CLEANED             ("All accounts with the default balance have been removed."),
	TRY_COMMAND                 ("Try $1"),
	PRIMARY_COLOR               (ChatColor.GOLD.toString()),
	SECONDARY_COLOR             (ChatColor.GRAY.toString()),
	TERTIARY_COLOR              (ChatColor.DARK_GRAY.toString()),
	ARGUMENT_COLOR              (ChatColor.YELLOW.toString()),
	DEBUG_STATUS                ("Debugging is $1 now."),
	COMMAND_BALANCE             ("Checks your balance", true),
	COMMAND_SEND                ("Sends another player money", true),
	COMMAND_TOP                 ("Checks the top 5 richest players", true),
	COMMAND_HELP                ("Gives you help", true),
	COMMAND_CREATE              ("Creates an account", true),
	COMMAND_REMOVE              ("Removes an account", true),
	COMMAND_SET                 ("Set a player's balance", true),
	COMMAND_GRANT               ("Grants a player money", true),
	COMMAND_DEDUCT              ("Deducts money from a player", true),
	COMMAND_CLEAN               ("Cleans the accounts with default balance", true),
	COMMAND_DEBUG               ("Toggles console debug info", true),
	COMMAND_RELOAD              ("Reloads the config", true),
	COMMAND_MIGRATE             ("Migrates from default database into specified one", true),
	;
	private static Fe plugin;
	private final String defaultMessage;
	private final boolean categorized;
	private String message;
	private Phrases(String defaultMessage)
	{
		this(defaultMessage, false);
	}
	private Phrases(String defaultMessage, boolean categorized)
	{
		this.defaultMessage = defaultMessage;
		this.categorized = categorized;
		message = defaultMessage + "";
	}
	public static void initialize(Fe instance)
	{
		plugin = instance;
	}
	private String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public void reset()
	{
		message = defaultMessage + "";
	}
	public String getConfigName()
	{
		String name = name();
		if(categorized)
			name = name.replaceFirst("_", ".");
		return name.toLowerCase();
	}
	public String parse(String... params)
	{
		String parsedMessage = getMessage();
		if(params != null)
			for(int i = 0; i < params.length; i ++)
				parsedMessage = parsedMessage.replace("$" + (i + 1), params[i]);
		return parsedMessage;
	}
	public String parseWithoutSpaces(String... params)
	{
		return parse(params).replace(" ", "");
	}
	private String parseWithPrefix(String... params)
	{
		return plugin.getMessagePrefix().replace("$1", plugin.settings.getPrefix()) + parse(params);
	}
	public void send(CommandSender sender, String... params)
	{
		sender.sendMessage(parse(params));
	}
	public void sendWithPrefix(CommandSender sender, String... params)
	{
		sender.sendMessage(parseWithPrefix(params));
	}
	public static void setupPhrases(File phrasesFile)
	{
		for(Phrases phrase : Phrases.values())
			phrase.reset();
		if(!phrasesFile.exists())
			return;
		YamlConfiguration phrasesConfig = YamlConfiguration.loadConfiguration(phrasesFile);
		for(Phrases phrase : Phrases.values())
		{
			String phraseConfigName = phrase.getConfigName();
			String phraseMessage = phrasesConfig.getString(phraseConfigName);
			if(phraseMessage == null)
				phraseMessage = phrase.parse();
			phrase.setMessage(phraseMessage);
		}
	}
}
