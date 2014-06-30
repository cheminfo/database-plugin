package org.cheminfo.scripting.database.mysql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class converts a given mysql ResultSet in a javascript convenient object.
 * Not sure if it should be a static class or not. 
 * @author acastillo
 *
 */
public class ResultSetParser {
	private static boolean DEBUG = false;
	
	public ResultSetParser(){
		
	}
	
	/**
	 * This function returns an array of JSONObjects representing all the entries in the result set.
	 * @param rs
	 * @return
	 */
	public JSONArray toJSON(ResultSet rs){
		//JSONArray   toReturn = new JSONArray();
		JSONArray table = new JSONArray();
		try {
			ResultSetMetaData md = rs.getMetaData();
			
			
			JSONArray rowNames = new JSONArray();
			JSONArray rowTypes = new JSONArray();
			int nColumns = md.getColumnCount();
			int i=0;
			//To get the column types
			for(i=0;i<nColumns;i++){
				rowNames.put(i,md.getColumnName(i+1));
			}
			// For simplicity, we are going to deal only with mysql data types [Integer->int, Floating->float],[Char->Char, (Varchar, BLOB, TEXT)->String] 
			// and [DATE, DATETIME, TIMESTAMP]->DateTime;
			if(DEBUG)System.out.println("Data types: ");
			for(i=0;i<nColumns;i++){
				if(DEBUG)	System.out.print(md.getColumnTypeName(i+1)+" ");
				rowTypes.put(i,md.getColumnTypeName(i+1));
			}
			if(DEBUG)System.out.println();
			
			JSONObject row = null;// new JSONObject();
			while(rs.next()){
				row = new JSONObject();
				for(i=0;i<nColumns;i++){
					//TODO cast to the given types
					row.put(rowNames.getString(i),rs.getObject(i+1));
				}
				table.put(row);
			}
			
			if(DEBUG) System.out.println("Found "+md.getColumnCount()+" columns."+" "+table.length()+" rows" );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}
	
	/**
	 * This function returns a table containing all the information in the given result set.
	 * The first column will provide the information about the table structure(names and types)
	 * @param rs
	 * @return
	 */
	public JSONArray toTable(ResultSet rs){
		JSONArray table = new JSONArray();
		try {
			ResultSetMetaData md = rs.getMetaData();
			
			JSONArray row = new JSONArray();
			int nColumns = md.getColumnCount();
			int i=0;
			//To get the column names
			for(i=0;i<nColumns;i++){
				row.put(i,md.getColumnName(i+1));
			}
			table.put(row);
			row = new JSONArray();
			//To get the column types as java class names
			for(i=0;i<nColumns;i++){
				row.put(i,md.getColumnClassName(i+1));
			}
			table.put(row);
			//To retrieve the data
			while(rs.next()){
				row = new JSONArray();
				for(i=0;i<nColumns;i++){
					row.put(i,rs.getObject(i+1));
				}
				table.put(row);
			}
			
			if(DEBUG) System.out.println("Found "+md.getColumnCount()+" columns."+" "+table.length()+" rows" );

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

}
