package org.melonbrew.fe.database.databases;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.bukkit.configuration.ConfigurationSection;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.database.Account;
import org.melonbrew.fe.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLDB extends Database {
    private final Fe plugin;
    private DataSource pool;
    private String table;
    private String usernameField;
    private String balanceField;
    private String uuidField;

    public MySQLDB(Fe plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void getConfigDefaults(ConfigurationSection section) {
        section.addDefault("host", "localhost");

        section.addDefault("port", 3306);

        section.addDefault("user", "root");

        section.addDefault("password", "minecraft");

        section.addDefault("database", "Fe");

        ConfigurationSection tables = getSection(section, "tables");

        tables.addDefault("accounts", "fe_accounts");

        ConfigurationSection columns = getSection(section, "columns");

        ConfigurationSection columnsAccounts = getSection(columns, "accounts");

        columnsAccounts.addDefault("username", "name");

        columnsAccounts.addDefault("money", "money");

        columnsAccounts.addDefault("uuid", "uuid");
    }

    private ConfigurationSection getSection(ConfigurationSection parent, String childName) {
        ConfigurationSection child = parent.getConfigurationSection(childName);

        if (child == null) {
            child = parent.createSection(childName);
        }

        return child;
    }

    public boolean init() {
        ConfigurationSection config = getConfigSection();
        String connectionString = "jdbc:mysql://" + config.getString("host") + ":" + config.getString("port") + "/" + config.getString("database");

        try {
            // Force driver to load if not yet loaded
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        table = config.getString("tables.accounts");

        usernameField = config.getString("columns.accounts.username");

        balanceField = config.getString("columns.accounts.money");

        uuidField = config.getString("columns.accounts.uuid");
        
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDriverClassName("com.mysql.jdbc.Driver");
        poolProperties.setUrl(connectionString);
        poolProperties.setUsername(config.getString("user"));
        poolProperties.setPassword(config.getString("password"));
        poolProperties.setMaxIdle(5);
        poolProperties.setMaxActive(5);
        poolProperties.setInitialSize(0);
        poolProperties.setMaxWait(-1);
        poolProperties.setRemoveAbandoned(true);
        poolProperties.setRemoveAbandonedTimeout(60);
        poolProperties.setTestOnBorrow(true);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setValidationInterval(30000);
        pool = new DataSource(poolProperties);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("SHOW TABLES LIKE '" + table + "'");
            rs = statement.executeQuery();

            if (rs.next()) {
                return true;
            }

            rs.close();
            statement.close();

            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (" + usernameField + " varchar(64) NOT NULL, " + uuidField + " varchar(36) PRIMARY KEY, " + balanceField + " double NOT NULL)");
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(rs);
            tryClose(statement);
            tryClose(connection);
        }
        return false;
    }

    private void tryClose(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
        }
    }

    public void close() {
        super.close();
        pool.close(true);
    }

    public List<Account> loadTopAccounts(int size) {
        List<Account> topAccounts = new ArrayList<Account>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("SELECT " + usernameField + ", " + uuidField + ", " + balanceField + " FROM " + table + " ORDER BY " + balanceField + " DESC limit " + size);
            rs = statement.executeQuery();
            while (rs.next()) {
                Account account = new Account(plugin, rs.getString(1), rs.getString(2), this);

                account.setMoney(rs.getDouble(3));

                topAccounts.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(rs);
            tryClose(statement);
            tryClose(connection);
        }

        return topAccounts;
    }

    public List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<Account>();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("SELECT " + usernameField + ", " + uuidField + ", " + balanceField + " from " + table + "");
            rs = statement.executeQuery();
            while (rs.next()) {
                Account account = new Account(plugin, rs.getString(1), rs.getString(2), this);

                account.setMoney(rs.getDouble(3));

                accounts.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(rs);
            tryClose(statement);
            tryClose(connection);
        }

        return accounts;
    }

    public Double loadAccountMoney(String name, String uuid) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("SELECT " + balanceField + " from " + table + " WHERE UPPER(" + uuidField + ") = UPPER(?) OR (? IS NULL AND UPPER(" + usernameField + ") = UPPER(?))");
            statement.setString(1, uuid);
            statement.setString(2, uuid);
            statement.setString(3, name);
            rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(rs);
            tryClose(statement);
            tryClose(connection);
        }
        return null;
    }

    public void removeAccount(String name, String uuid) {
        super.removeAccount(name, uuid);

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("DELETE FROM " + table + " WHERE UPPER(" + uuidField + ") = UPPER(?) OR (? IS NULL AND UPPER(" + usernameField + ") = UPPER(?))");
            statement.setString(1, uuid);
            statement.setString(2, uuid);
            statement.setString(3, name);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(statement);
            tryClose(connection);
        }
    }

    protected void saveAccount(String name, String uuid, double money) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("UPDATE " + table + " SET " + balanceField + "=?, " + usernameField + "=? WHERE UPPER(" + uuidField + ") = UPPER(?) OR (? IS NULL AND UPPER(" + usernameField + ") = UPPER(?))");

            statement.setDouble(1, money);
            statement.setString(2, name);
            statement.setString(3, uuid);
            statement.setString(4, uuid);
            statement.setString(5, name);

            if (statement.executeUpdate() == 0) {
                statement.close();
                statement = connection.prepareStatement("INSERT INTO " + table + " (" + usernameField + ", " + uuidField + ", " + balanceField + ") VALUES (?, ?, ?)");
                statement.setString(1, name);
                statement.setString(2, uuid);
                statement.setDouble(3, money);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(statement);
            tryClose(connection);
        }
    }

    public void clean() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("DELETE FROM " + table + " WHERE " + balanceField + " = " + plugin.getAPI().getDefaultHoldings());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(statement);
            tryClose(connection);
        }
    }

    public void removeAllAccounts() {
        super.removeAllAccounts();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = pool.getConnection();
            statement = connection.prepareStatement("DELETE FROM " + table);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            tryClose(statement);
            tryClose(connection);
        }
    }

    public String getName() {
        return "MySQL";
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void setVersion(int version) { }
}
