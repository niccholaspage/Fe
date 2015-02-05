package org.melonbrew.fe.database;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.UUIDFetcher;

import java.util.*;

public abstract class Database
{
    private final Fe plugin;
    private final Set<Account> cachedAccounts;
    protected boolean cacheAccounts;

    public Database( Fe plugin )
    {
        this.plugin = plugin;

        this.cachedAccounts = new HashSet<Account>();
    }

    public boolean init()
    {
        this.cacheAccounts = plugin.getAPI().getCacheAccounts();

        return false;
    }

    public List<Account> getTopAccounts( int size )
    {
        List<Account> topAccounts = loadTopAccounts( size * 2 );

        if( !cachedAccounts.isEmpty() )
        {
            for( Account account : cachedAccounts )
            {
                topAccounts.remove( account );
            }

            List<Account> cachedTopAccounts = new ArrayList<Account>( cachedAccounts );

            Collections.sort( cachedTopAccounts, new Comparator<Account>()
            {
                public int compare( Account account1, Account account2 )
                {
                    return ( int ) ( account2.getMoney() - account1.getMoney() );
                }
            } );

            if( cachedAccounts.size() > size )
            {
                cachedTopAccounts = cachedTopAccounts.subList( 0, size );
            }

            topAccounts.addAll( cachedTopAccounts );
        }

        Collections.sort( topAccounts, new Comparator<Account>()
        {
            public int compare( Account account1, Account account2 )
            {
                return ( int ) ( account2.getMoney() - account1.getMoney() );
            }
        } );

        if( topAccounts.size() > size )
        {
            topAccounts = topAccounts.subList( 0, size );
        }

        return topAccounts;
    }

    public abstract List<Account> loadTopAccounts( int size );

    public abstract List<Account> getAccounts();

    public abstract HashMap<String, String> loadAccountData( String name, String uuid );

    protected abstract void saveAccount( String name, String uuid, double money );

    public void removeAccount( String name, String uuid )
    {
        Account account = getCachedAccount( name, uuid );

        if( account != null )
        {
            removeCachedAccount( account );
        }
    }

    public abstract void getConfigDefaults( ConfigurationSection section );

    public abstract void clean();

    public void removeAllAccounts()
    {
        for( Account account : new HashSet<Account>( cachedAccounts ) )
        {
            cachedAccounts.remove( account );
        }
    }

    protected boolean convertToUUID()
    {
        if( !plugin.getServer().getOnlineMode() )
        {
            //Disable plugin?
        }

        plugin.log( Phrase.STARTING_UUID_CONVERSION );

        List<Account> accounts = getAccounts();

        Map<String, Double> accountMonies = new HashMap<String, Double>();

        for( Account account : accounts )
        {
            accountMonies.put( account.getName(), account.getMoney() );
        }

        List<String> names = new ArrayList<String>();

        for( Account account : accounts )
        {
            names.add( account.getName() );
        }

        UUIDFetcher fetcher = new UUIDFetcher( names );

        Map<String, UUID> response;

        try
        {
            response = fetcher.call();

            removeAllAccounts();

            for( String name : response.keySet() )
            {
                for( String accountName : new HashMap<String, Double>( accountMonies ).keySet() )
                {
                    if( accountName.equalsIgnoreCase( name ) )
                    {
                        saveAccount( name, response.get( name ).toString(), accountMonies.get( accountName ) );

                        accountMonies.remove( accountName );
                    }
                }
            }

            for( String accountName : accountMonies.keySet() )
            {
                saveAccount( accountName, null, accountMonies.get( accountName ) );
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();

            plugin.log( Phrase.UUID_CONVERSION_FAILED );

            plugin.getServer().getPluginManager().disablePlugin( plugin );

            return false;
        }

        plugin.log( Phrase.UUID_CONVERSION_SUCCEEDED );

        return true;
    }

    public void close()
    {
        Iterator<Account> iterator = cachedAccounts.iterator();

        while( iterator.hasNext() )
        {
            Account account = iterator.next();

            account.save( account.getMoney() );

            iterator.remove();
        }
    }

    public abstract String getName();

    public String getConfigName()
    {
        return getName().toLowerCase().replace( " ", "" );
    }

    public ConfigurationSection getConfigSection()
    {
        return plugin.getConfig().getConfigurationSection( getConfigName() );
    }

    public Account getAccount( String name, String uuid )
    {
        Account account = getCachedAccount( name, uuid );

        if( account != null )
        {
            return account;
        }

        HashMap<String, String> data = loadAccountData( name, uuid );

        String money_string = data.get( "money" );
        Double data_money;

        try
        {
            data_money = Double.parseDouble( money_string );
        }
        catch( Exception e )
        {
            data_money = null;
        }

        String data_name = data.get( "name" );

        if( data_money == null )
        {
            return null;
        }
        else
        {
            return createAndAddAccount( data_name, uuid, data_money );
        }
    }

    public Account updateAccount( String name, String uuid )
    {
        Account account = getAccount( name, uuid );

        if( account == null )
        {
            account = createAndAddAccount( name, uuid, plugin.getAPI().getDefaultHoldings() );
        }

        if( ! account.getName().equals( name ) )
        {
            account.setName( name );
        }

        return account;
    }

    private Account createAndAddAccount( String name, String uuid, double money )
    {
        Account account = new Account( plugin, name, uuid, this );

        account.setMoney( money );

        if( cacheAccounts() )
        {
            Player player = plugin.getServer().getPlayerExact( name );

            if( player != null )
            {
                cachedAccounts.add( account );
            }
        }

        return account;
    }

    public boolean accountExists( String name, String uuid )
    {
        return getAccount( name, uuid ) != null;
    }

    public boolean cacheAccounts()
    {
        return cacheAccounts;
    }

    public Account getCachedAccount( String name, String uuid )
    {
        for( Account account : cachedAccounts )
        {
            if( account.getName().equals( name ) )
            {
                return account;
            }
        }

        return null;
    }

    public boolean removeCachedAccount( Account account )
    {
        return cachedAccounts.remove( account );
    }

    public abstract int getVersion();

    public abstract void setVersion( int version );
}
