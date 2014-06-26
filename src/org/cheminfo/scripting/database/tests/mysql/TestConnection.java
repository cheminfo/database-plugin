package org.cheminfo.scripting.database.tests.mysql;

import org.cheminfo.scripting.database.mysql.MySQL;
import org.cheminfo.scripting.database.mysql.MySQLDatabase;
import org.cheminfo.scripting.database.mysql.MySQLTable;
import org.json.JSONArray;

public class TestConnection {

	public static void main(String[] args) {
		
		MySQL plugin = new MySQL();
		
		MySQLDatabase conn = plugin.getDb("localhost","test","","");
		
		MySQLTable table = conn.selectTable("table1");
		
		JSONArray result = table.selectQuery("field1 in (1,2)");
		
		System.out.println(result);
	}

}
