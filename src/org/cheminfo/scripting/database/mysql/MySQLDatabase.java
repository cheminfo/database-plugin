package org.cheminfo.scripting.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * 
 * @author acastillo and mzasso
 *
 */
public class MySQLDatabase {
	
	private Connection connection = null;
	
	public MySQLDatabase(String url) {
		//System.out.println(url);
		try {
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This function returns a avatar to manipulate the objects of this table.
	 * @param name
	 * @return
	 */
	public JSONArray getTableNames() {
		//TODO Implement this function
		return new JSONArray();
	}
	
	public JSONArray describeTable(String tableName) {
		Statement stmt;
		ResultSet rs;
		try {
			stmt = connection.createStatement();
			if(tableName!=null&&tableName.length()>0){
				rs = stmt.executeQuery("DESCRIBE "+tableName);
				ResultSetParser parser = new ResultSetParser();
				return parser.toTable(rs);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}
	
	/**
	 * This function returns a avatar to manipulate the objects of this table.
	 * @param name
	 * @return
	 */
	public MySQLTable selectTable(String name) {
		return new MySQLTable(name, connection);
	}
	
	/**
	* This function creates a new table in the current database.
	 * @param tableName
	 * @param structure
	 * @return
	 */
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
	 * This function converts the given JSONArray in the string specifying the table structure.
	 * @param structure
	 * @return
	 */
	private String structure2SQLDefinition(JSONArray structure) {
		String def = " ";
		try {
			for(int i=0;i<structure.length();i++){
				JSONObject column = structure.getJSONObject(i);
				if(column.has("name")&&column.has("type")){
					//Here you can include as many MYSQL options as you want. 
					//For now I only want something that works.
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
	
	/**
	 * This function destroy a database table.
	 * @param tableName
	 * @param options
	 * @return
	 */
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
	
	/**
	 * This function index the selected column by creating a HASH or a BTREE map on it. It will
	 * speed up all the operations that requires a look up over that column. 
	 * @param tableName
	 * @param columnName
	 * @param indexName
	 * @param options
	 * @return
	 */
	public boolean createIndex(String tableName, String columnName, String indexName, JSONObject options){
		Statement stmt;
		ResultSet rs;
		String query = "";
		try {
			stmt = connection.createStatement();
			if(options.has("type"))//[UNIQUE|FULLTEXT|SPATIAL]
				query="CREATE "+options.optString("type", "UNIQUE")+" INDEX ";
			else
				query="CREATE INDEX ";
			if(tableName!=null&&tableName.length()>0&&columnName!=null&&indexName!=null)
				query+= indexName+"ON "+tableName+"("+columnName+") ";
			else
				return false;
			if(options.has("using")){//{BTREE | HASH}
				query+=" USING "+options.optString("using", "BTREE");
			}
			
			rs = stmt.executeQuery(query);
			
		} catch (SQLException e) {
			System.out.println(" Problems executing the query: "+query);
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * This function inserts a new entry in the tableName and return its corresponding primary key.
	 * @param tableName
	 * @param info
	 * @return null, if the object could not be inserted.
	 */
	public Object insert(String tableName, JSONObject info){
		return insert(tableName, info, info);
	}
	/**
	 * This function inserts a new entry in the tableName and return its corresponding primary key.
	 * @param tableName
	 * @param info
	 * @return null, if the object could not be inserted.
	 */
	public Object insert(String tableName, JSONObject info, JSONObject options){
		
		if(options==null)
			options=new JSONObject();
		
		String query = "INSERT INTO "+tableName+" (";
		Iterator<String> keys = info.keys();
		String names ="";
		String values = "";
		String key;
		
		ResultSet rs=null;
			
		while(keys.hasNext()){
			key = keys.next();
			names+=key+",";
			try {
				values+=info.get(key)+", ";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(names.endsWith(",")){
			query.substring(0, query.length()-2);
			values.substring(0, values.length()-2);
		}
		query+=names+") VALUES ("+values+")";
		
		try {
			Statement stmt;
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
			System.out.println(" Problems executing the query: "+query);
			e.printStackTrace();
		}

		return rs;
	} 
	
	/**
	 * This function updates a given entry in the corresponding table. This function will use
	 * only the primary key for this process, so, it should be used carefully, cause for now, 
	 * the database integrity could be affected, moreover with databases created with this plug-in. 
	 * @param tableName
	 * @param info
	 * @return
	 */
	public boolean update(String tableName, JSONObject info){
		return true;
	}
	/**
	 * This function deletes the entry in tableName, that has the same primary as the object info.
	 * @param tableName
	 * @param info
	 * @return
	 */
	public boolean delete(String tableName, JSONObject info){
		return delete(tableName, info, null);
	}
	
	
	/**
	 * This function deletes the entry in tableName, that has the same primary as the object info.
	 * @param tableName
	 * @param info
	 * @return
	 */
	public boolean delete(String tableName, JSONObject info, JSONObject options){
		if(options==null)
			options=new JSONObject();
		//Here we can include some mysql options.
		String query="";
		try {
			query = "DELETE FROM "+tableName+" WHERE "+info.getString("key")+"="+info.getString("value");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		
		try {
			Statement stmt;
			stmt = connection.createStatement();
			stmt.executeQuery(query);
		} catch (SQLException e) {
			System.out.println(" Problems executing the query: "+query);
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	/**
	 * This function performs a query on the given table name. The result of this query will
	 * be a set of table entries that could be used afterward on the update or delete functions 
	 * defined on this plug-in 
	 * @param tableName
	 * @param where
	 * @param options
	 * @return
	 */
	public JSONArray select(String tableName, String where, JSONObject options){
		MySQLTable table = new MySQLTable(tableName, connection);
		return table.selectQuery(where, options);
	}
	
	/**
	 * This function performs any query on the database. 
	 * @param query
	 * @return
	 */
	public JSONArray select(String query){
		return select(query,null);
	}
	
	/**
	 * This function performs any query on the database. 
	 * @param query
	 * @return
	 */
	public JSONArray select(String query, JSONObject options){
		
		Statement stmt;
		ResultSet rs;
		
		try {
			if(options==null)
				options=new JSONObject();
			stmt = connection.createStatement();
			if(query!=null&&query.length()>0){
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
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
