package org.cheminfo.scripting.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MySQLDatabase {
	
	private Connection connection = null;
	
	public MySQLDatabase(String url) {

		try {
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public MySQLTable selectTable(String name) {
		return new MySQLTable(name, connection);
	}
	
	public boolean createTable(String tableName, JSONArray structure){
		
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = connection.createStatement();
			if(tableName!=null&&tableName.length()>0)
				rs = stmt.executeQuery("CREATE TABLE "+tableName+"("+this.structure2SQLDefinition(structure)+")");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	/**
	 * This function creates a new table in the current database.
	 * @param structure
	 * @return
	 */
	private String structure2SQLDefinition(JSONArray structure) {
		String def = " ";
		try {
			for(int i=0;i<structure.length();i++){
				JSONObject column = structure.getJSONObject(i);
				if(column.has("name")&&column.has("type")){
					//Here you can put as many MYSQL options as you want. For now I want something that works.
					def+=column.getString("name")+" "+column.getDouble("type");
					if(column.has("notnull")){
						def+=" NOT NULL";
					}
					if(column.optBoolean("autoincrement",false)){
						def+=" AUTO_INCREMENT";
					}
					if(column.optBoolean("uniquekey",false)){
						def+=" UNIQUE KEY";
					}
					if(column.optBoolean("unsigned",false)){
						def+=" UNSIGNED";
					}
					if(column.optBoolean("zerofill",false)){
						def+=" ZEROFILL";
					}
					if(column.has("character")){
						def+=" CHARACTER SET "+column.optString("character","");
					}
					if(column.has("collate")){
						def+=" COLLATE "+column.optString("collate","");
					}
					if(column.optBoolean("primarykey",false)){
						def+=" PRIMARY KEY";
					}
					if(column.has("storage")){
						def+=" "+column.optString("storage","DEFAULT");
					}
					def+=",";
				}
				if(def.endsWith(","))
					def.substring(0, def.length()-2);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public boolean dropTable(String tableName, JSONObject options){
		Statement stmt;
		ResultSet rs;
		if(options==null)
			options=new JSONObject();
		String post = "";
		if(options.optBoolean("ifExists")){	
			post+=" IF EXISTS";
		}

		if(options.optBoolean("restrict")){	
			post+=" RESTRICT";
		}
		else{
			if(options.optBoolean("cascade")){	
				post+=" CASCADE";
			}
		}
		try {
			stmt = connection.createStatement();
			if(tableName!=null&&tableName.length()>0){
				rs = stmt.executeQuery("DROP TABLE "+tableName+" "+post);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		return true;
	}
	
	public boolean createIndex(String tableName, String columnName){
		return true;
	}
	/**
	 * This function inserts a new entry in the tableName and return its corresponding primary key.
	 * @param tableName
	 * @param info
	 * @return null, if the object could not be inserted.
	 */
	public Object insert(String tableName, JSONObject info){
		return 0;
	} 
	
	public boolean update(String tableName, JSONObject info){
		return true;
	}
	
	public boolean delete(String tableName, JSONObject info){
		return true;
	}
	
	public JSONArray select(String tableName, String where, JSONObject options){
		return null;
	}
	
	public JSONArray select(String query){
		return null;
	}
	

}
