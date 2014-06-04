package org.cheminfo.scripting.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase {
	
	private Connection connection = null;
	
	public MySQLDatabase(String url) {

		try {
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public MySQLTable getTable(String name) {
		return new MySQLTable(name, connection);
	}

}
