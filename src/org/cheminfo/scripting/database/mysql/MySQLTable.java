package org.cheminfo.scripting.database.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLTable {
	
	private Connection connection;
	private String name;

	public MySQLTable(String name, Connection connection) {
		this.name = name;
		this.connection = connection;  
	}
	
	public void selectQuery() {
		selectQuery("");
	}
	public void selectQuery(String query) {
					
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT * FROM "+name);
			
			ResultSetMetaData md = rs.getMetaData();
			
			System.out.println("Found "+md.getColumnCount()+" columns.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
}
