package org.melonbrew.fe.database.databases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

public class FlatDB extends Database {
	private Fe plugin;
	private HashMap<String, Double> accounts = new HashMap<String, Double>();
	private File file;
	private boolean write = false;

	public FlatDB(Fe plugin){
		super(plugin);

		this.plugin = plugin;
	}

	@Override
	public boolean init() {
		file = new File(plugin.getDataFolder(), "accounts.flat");
		if (!file.exists()) {
			plugin.log("No database flat file existing, creating it... (accounts.flat)");
			try {
				file.createNewFile();
			} catch (IOException e) {
				plugin.log("Error while loading flat database (file creation): " + e.getMessage());
				return false;
			}
		}
		else {
			try {
				Scanner sc = new Scanner(file);
				int i = 0;
				while(sc.hasNextLine()){
					String str = sc.nextLine();
					if (!str.isEmpty()) {
						String[] split = str.split(":");
						accounts.put(split[0], Double.valueOf(split[1]));
						i++;
					}
				}
				plugin.log(i + " accounts loaded");
				sc.close();
			} catch (FileNotFoundException e) {
				plugin.log("Error while loading flat database (file reading): " + e.getMessage());
				return false;
			}
		}
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
			public void run() {
				if (write) {
					write = false;
					if (file.canWrite()) {
						BufferedWriter out = null;
						try {
							out = new BufferedWriter(new FileWriter(file, false));
							for (Entry<String,Double> account: accounts.entrySet()) {
								out.write(account.getKey() + ":" + account.getValue());
								out.newLine();
							}
						} catch (IOException e) {
							plugin.log("Error while loading flat database (file writing): " + e.getMessage());
						} finally {
							if(out != null) {
								try {
									out.close();
								} catch (IOException e) {
									plugin.log("Error while loading flat database (file closing): " + e.getMessage());
								}
							}
						}

					}
				}
			}
		}
		, 100L, 100L);
		return true;
	}

	public double loadAccountMoney(String name){
		if (!accounts.containsKey(name))
			return -1;
		return accounts.get(name);
	}

	public void removeAccount(String name){
		if (accounts.containsKey(name)) {
			accounts.remove(name);
			write = true;
		}
	}

	public void saveAccount(String name, double money){
		accounts.put(name, money);
		write = true;
	}

	@Override
	public void getConfigDefaults(ConfigurationSection section) {

	}

	@Override
	public String getName() {
		return "Flat";
	}

	@Override
	public List<Account> getTopAccounts(int size) {
		List<Account> topAccounts = getAccounts();

		Collections.sort(topAccounts, new Comparator<Account>(){
			public int compare(Account account1, Account account2){
				return ((Double) account2.getMoney()).compareTo(account1.getMoney());
			}});

		if (topAccounts.size() > size){
			return topAccounts.subList(0, size);
		}

		return topAccounts;
	}

	@Override
	public List<Account> getAccounts() {
		List<Account> accountsList = new ArrayList<Account>();
		Iterator<Entry<String, Double>> iterator = accounts.entrySet().iterator();
		while (iterator.hasNext()){
			Entry<String, Double> topAccount = iterator.next();
			Account account = new Account(topAccount.getKey(), plugin, this);
			account.setMoney(topAccount.getValue());
			accountsList.add(account);
		}

		return accountsList;
	}

	public void clean() {
		accounts.clear();
	}

	@Override
	public void close() {

	}
}
