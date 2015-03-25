package org.melonbrew.fe.database;

import org.melonbrew.fe.API;
import org.melonbrew.fe.Fe;

public class Account
{
    private final Fe plugin;

    private String name;

    private final String uuid;

    private final API api;

    private final Database database;

    private Double money;

    public Account( Fe plugin, String name, String uuid, Database database )
    {
        this.plugin = plugin;

        this.name = name;

        this.uuid = uuid;

        this.api = plugin.getAPI();

        this.database = database;

        this.money = null;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;

        if( money == null )
        {
            this.money = getMoney();
        }

        database.saveAccount( name, uuid, money );
    }

    public String getUUID()
    {
        return uuid;
    }

    public Double getMoney()
    {
        if( money != null )
        {
            return money;
        }

        String money_string = database.loadAccountData( name, uuid ).get( "money" );
        Double money;

        try
        {
            money = Double.parseDouble( money_string );
        }
        catch( Exception e )
        {
            return 0D;
        }

        if( database.cacheAccounts() )
        {
            this.money = money;
        }

        return money;
    }

    public void setMoney( double money )
    {
        Double currentMoney = getMoney();

        if( currentMoney != null && currentMoney == money )
        {
            return;
        }

        if( money < 0 && !api.isCurrencyNegative() )
        {
            money = 0;
        }

        currentMoney = api.getMoneyRounded( money );

        if( api.getMaxHoldings() > 0 && currentMoney > api.getMaxHoldings() )
        {
            currentMoney = api.getMoneyRounded( api.getMaxHoldings() );
        }

        if( !database.cacheAccounts() || plugin.getServer().getPlayerExact( getName() ) == null )
        {
            save( currentMoney );
        }
        else
        {
            this.money = currentMoney;
        }
    }

    public void withdraw( double amount )
    {
        setMoney( getMoney() - amount );
    }

    public void deposit( double amount )
    {
        setMoney( getMoney() + amount );
    }

    public boolean canReceive( double amount )
    {
        return api.getMaxHoldings() == -1 || amount + getMoney() < api.getMaxHoldings();

    }

    public boolean has( double amount )
    {
        return getMoney() >= amount;
    }

    public void save( double money )
    {
        database.saveAccount( name, uuid, money );
    }

    @Override
    public boolean equals( Object object )
    {
        if( !( object instanceof Account ) )
        {
            return false;
        }

        Account account = ( Account ) object;

        return account.getName().equals( getName() );
    }
}
