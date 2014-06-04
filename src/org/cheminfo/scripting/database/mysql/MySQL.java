package org.cheminfo.scripting.database.mysql;

import java.sql.SQLException;

import org.cheminfo.function.Function;
import com.mysql.jdbc.Driver;

public class MySQL extends Function {
	
	public MySQL() {
		// try to load the MySQL driver
		try {
			Object a = new com.mysql.jdbc.Driver();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
		
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
