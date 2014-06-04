package org.cheminfo.scripting.database.tests.mysql;

import org.cheminfo.scripting.database.mysql.MySQL;
import org.cheminfo.scripting.database.mysql.MySQLDatabase;
import org.cheminfo.scripting.database.mysql.MySQLTable;

public class TestConnection {

	public static void main(String[] args) {
		
		MySQL plugin = new MySQL();
		
		MySQLDatabase conn = plugin.getDb("server","dbname","user","password");
		
		MySQLTable table = conn.getTable("table");
		
		table.selectQuery();

	}

}
