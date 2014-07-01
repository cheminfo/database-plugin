package org.cheminfo.scripting.database.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.cheminfo.function.Function;
import org.json.JSONArray;
import org.json.JSONObject;

public class MySQLTable extends Function{
	
	private Connection connection;
	private String name;

	public MySQLTable(String name, Connection connection) {
		this.name = name;
		this.connection = connection;  
	}
	
	public JSONArray selectQuery() {
		return selectQuery("",null);
	}
	
	public JSONArray selectQuery(String query) {
		return selectQuery(query,null);
	}
	/**
	 * Inject me please
	 * @param query
	 */
	public JSONArray selectQuery(String query, Object optionsO) {
		JSONObject options = this.checkParameter(optionsO);
					
		Statement stmt;
		ResultSet rs;
		  
		try {
			if(options==null)
				options = new JSONObject();
			stmt = connection.createStatement();
			if(query!=null&&query.length()>0){
				query="SELECT * FROM "+name+" WHERE "+query;
			}
			else{
				query="SELECT * FROM "+name;
			}
			
			System.out.println(query);
			
			rs = stmt.executeQuery(query);	
			
			ResultSetParser parser = new ResultSetParser();
			if(options.has("format")){
				String format = options.optString("format", "");
				if(format.compareTo("table")==0)
					return parser.toTable(rs);
				if(format.compareTo("json")==0)
					return parser.toJSON(rs);
					
			}else
				return parser.toJSON(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
