package org.cheminfo.scripting.database.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

public class MySQLTable {
	
	private Connection connection;
	private String name;

	public MySQLTable(String name, Connection connection) {
		this.name = name;
		this.connection = connection;  
	}
	
	public void selectQuery() {
		selectQuery("",null);
	}
	
	public void selectQuery(String query) {
		selectQuery(query,null);
	}
	/**
	 * Inject me please
	 * @param query
	 */
	public void selectQuery(String query, JSONObject options) {
					
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = connection.createStatement();
			if(query!=null&&query.length()>0)
				rs = stmt.executeQuery("SELECT * FROM "+name+" WHERE "+query);
			else
				rs = stmt.executeQuery("SELECT * FROM "+name);
				
			ResultSetMetaData md = rs.getMetaData();
			
			int i=0;
			while(!rs.isLast()){
				rs.next();
				i++;
			}
			
			System.out.println("Found "+md.getColumnCount()+" columns."+" "+i+" rows" );
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
}
