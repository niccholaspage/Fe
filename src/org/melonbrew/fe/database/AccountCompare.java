package org.melonbrew.fe.database;

import java.util.Comparator;

public class AccountCompare implements Comparator<Account>{
    public int compare(Account account, Account account2){
    	return (int) (account.getMoney() - account2.getMoney());
    }
}