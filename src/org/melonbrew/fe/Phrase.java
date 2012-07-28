package org.melonbrew.fe;

public enum Phrase {
	DATABASE_FAILURE_DISABLE("Database initialization has failed, disabling Fe."),
	COMMAND_NEEDS_ARGUMENTS("That command needs arguments."),
	COMMAND_NOT_CONSOLE("The command '$1' cannot be used in the console."),
	NO_PERMISSION_FOR_COMMAND("You do not have permission to use that command."),
	ACCOUNT_HAS("$1 has "),
	YOU_HAVE("You have "),
	HELP("Fe Help"),
	HELP_ARGUMENTS("$1 Required, $2 Optional"),
	RICH("Fe Rich List"),
	CONFIG_RELOADED("The config has been reloaded."),
	NOT_ENOUGH_MONEY("You don't have enough money."),
	ACCOUNT_DOES_NOT_EXIST("Sorry, that account does not exist."),
	ACCOUNT_EXISTS("That account already exists."),
	ACCOUNT_CREATED("An account for $1 has been created."),
	ACCOUNT_REMOVED("An account for $1 has been removed."),
	MONEY_RECIEVE("You've recieved $1 from $2."),
	PLAYER_SET_MONEY("You've set $1's balance to $2."),
	PLAYER_GRANT_MONEY("You've granted $1 to $2."),
	ACCOUNT_CREATED_GRANT("Created an account for $1 and granted it $2"),
	NO_ACCOUNTS_EXIST("No accounts exist."),
	NAME_TOO_LONG("Sorry that name is too long."),
	ACCOUNT_CLEANED("All accounts with the default balance have been cleared.");

	private String defaultMessage;

	private String message;

	private Phrase(String defaultMessage){
		this.defaultMessage = defaultMessage;

		message = defaultMessage + "";
	}

	public void setMessage(String message){
		this.message = message;
	}

	private String getMessage(){
		return message;
	}

	public void reset(){
		message = defaultMessage + "";
	}

	public String getConfigName(){
		return name().toLowerCase();
	}

	public String parse(String... params){
		String parsedMessage = getMessage();

		if (params != null){
			for (int i = 0; i < params.length; i++){
				parsedMessage = parsedMessage.replace("$" + (i + 1), params[i]);
			}
		}

		return parsedMessage;
	}
}