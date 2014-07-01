package org.cheminfo.scripting.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.cheminfo.function.Function;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * 
 * @author acastillo and mzasso
 *
 */
public class MySQLDatabase extends Function{
	
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
		Statement stmt;
		ResultSet rs;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SHOW TABLES");
			ResultSetParser parser = new ResultSetParser();
			return  parser.toJSON(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}
	/**
	 * This function return 
	 * @param tableName
	 * @return
	 */
	public JSONArray describeTable(String tableName) {
		Statement stmt;
		ResultSet rs;
		try {
			stmt = connection.createStatement();
			if(tableName!=null&&tableName.length()>0){
				rs = stmt.executeQuery("DESCRIBE "+tableName);
				ResultSetParser parser = new ResultSetParser();
				return  parser.toJSON(rs);
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
	public boolean createTable(String tableName, Object structureO){
		JSONArray structure = this.checkJSONArray(structureO);
		Statement stmt;
		try {
			stmt = connection.createStatement();
			if(tableName!=null&&tableName.length()>0){
				String query = "CREATE TABLE "+tableName+"("+this.structure2SQLDefinition(structure)+")";
				///System.out.println(query);
				return stmt.execute(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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
				//System.out.println(column);
				if(column.has("name")&&column.has("type")){
					//Here you can include as many MYSQL options as you want. 
					//For now I only want something that works.
					def+=column.getString("name")+" "+column.getString("type");
					if(column.optBoolean("notnull",false)){
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
			}
			if(def.endsWith(","))
				def=def.substring(0, def.length()-1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return def;
	}
	
	/**
	 * This function destroy a database table.
	 * @param tableName
	 * @param options
	 * @return
	 */
	public boolean dropTable(String tableName){
		return dropTable(tableName, null);
	}
	
	/**
	 * This function destroy a database table.
	 * @param tableName
	 * @param options
	 * @return
	 */
	public boolean dropTable(String tableName, Object optionsO){
		JSONObject options = this.checkParameter(optionsO);
		Statement stmt;
		ResultSet rs;
		if(options==null)
			options=new JSONObject();
		String post = "";
		if(options.optBoolean("ifExists")){	
			tableName=" IF EXISTS "+tableName;
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
				String query = "DROP TABLE "+tableName+" "+post;
				//System.out.println(query);
				return stmt.execute(query);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		return false;
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
	public boolean createIndex(String tableName, String columnName, String indexName, Object optionO){
		JSONObject options = this.checkParameter(optionO);
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
				query+= indexName+" ON "+tableName+"("+columnName+") ";
			else
				return false;
			if(options.has("using")){//{BTREE | HASH}
				query+=" USING "+options.optString("using", "BTREE");
			}
			
			return stmt.execute(query);
			
		} catch (SQLException e) {
			System.out.println(" Problems executing the query: "+query);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * This function inserts a new entry in the tableName and return its corresponding primary key.
	 * @param tableName
	 * @param info
	 * @return null, if the object could not be inserted.
	 */
	public boolean insert(String tableName, Object infoO){
		JSONObject info = this.checkParameter(infoO);
		 return insert(tableName, info, null);
	}
	/**
	 * This function inserts a new entry in the tableName and returns its corresponding primary key.
	 * @param tableName
	 * @param info
	 * @return null, if the object could not be inserted.
	 */
	public boolean insert(String tableName, Object infoO, Object optionsO){
		JSONObject info = this.checkParameter(infoO);
		JSONObject options = this.checkParameter(optionsO);
		
		if(options==null)
			options=new JSONObject();
		
		String query = "INSERT INTO "+tableName+" (";
		Iterator<String> keys = info.keys();
		String names ="";
		String values = "";
		String key;
		Object value;
		while(keys.hasNext()){
			key = keys.next();
			names+=key+",";
			try {
				value = info.get(key);
				if(value instanceof String)
					values+="\""+info.get(key)+"\",";
				else
					values+=info.get(key)+",";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("names "+names);
		if(names.endsWith(",")){
			names=names.substring(0, names.length()-1);
			values=values.substring(0, values.length()-1);
		}
		query+=names+") VALUES ("+values+")";
		
		try {
			Statement stmt;
			stmt = connection.createStatement();
			//System.out.println(query);
			return stmt.execute(query);
			//return true;
		} catch (Exception e) {
			System.out.println(" Problems executing the query: "+query);
			e.printStackTrace();
		}

		return false;
	} 
	
	/**
	 * This function updates a given entry in the corresponding table. This function will use
	 * only the primary key for this process, so, it should be used carefully, cause for now, 
	 * the database integrity could be affected, moreover with databases created with this plug-in. 
	 * @param tableName
	 * @param info
	 * @return
	 */
	public boolean update(String tableName, Object infoO){
		JSONObject info = this.checkParameter(infoO);
		return true;
	}
	/**
	 * This function deletes the entry in tableName, that has the same primary as the object info.
	 * @param tableName
	 * @param info
	 * @return
	 */
	public boolean delete2(String tableName, Object infoO){
		JSONObject info = this.checkParameter(infoO);
		return delete2(tableName, info, null);
	}
	
	
	/**
	 * This function deletes the entry in tableName, that has the same primary as the object info.
	 * @param tableName
	 * @param info
	 * @return
	 */
	public boolean delete2(String tableName, Object infoO, Object optionsO){
		JSONObject info = this.checkParameter(infoO);
		JSONObject options = this.checkParameter(optionsO);
		if(options==null)
			options=new JSONObject();
		//Here we can include some mysql options. 
		String query="";
		try {
			query = "DELETE FROM "+tableName+" WHERE "+info.getString("key")+"=";
			Object value = info.get("value");
			if(value instanceof String)
				query+="\""+value+"\"";
			else
				query+=value;
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		
		try {
			Statement stmt;
			stmt = connection.createStatement();
			return stmt.execute(query);
		} catch (SQLException e) {
			System.out.println(" Problems executing the query: "+query);
			e.printStackTrace();
		}

		return false;
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
	public JSONArray select(String tableName, String where, Object options){
		//JSONObject options = this.checkParameter(optionsO);
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
	public JSONArray select(String query, Object optionsO){
		JSONObject options = this.checkParameter(optionsO);
		
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
	
	public void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			System.out.println("Connection could not be closed");
			e.printStackTrace();
		}
	}

}
