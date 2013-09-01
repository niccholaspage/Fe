package com.niccholaspage.nSQL.connection;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteConnection {
	private final File file;

	public SQLiteConnection(File file){
		this.file = file;
	}

	public Connection getConnection(){
		try {
			Class.forName("org.sqlite.JDBC");

			Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			
			return connection;
		} catch (Exception e){
			return null;
		}
	}
}
