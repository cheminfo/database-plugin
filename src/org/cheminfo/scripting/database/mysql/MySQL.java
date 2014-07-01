package org.cheminfo.scripting.database.mysql;

import java.sql.SQLException;

import org.cheminfo.function.Function;

public class MySQL extends Function {
	
	public MySQL() {
		// try to load the MySQL driver 
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public MySQLDatabase getDb(String url) {
		try {
			return new MySQLDatabase(url);
		} catch (SQLException e) {
			return null;
		}
	}
	
	public MySQLDatabase getDb(String url, String dbName, String username, String password) {
		return getDb("jdbc:mysql://"+url+"/"+dbName+"?user="+username+"&password="+password);
	}
	
}
