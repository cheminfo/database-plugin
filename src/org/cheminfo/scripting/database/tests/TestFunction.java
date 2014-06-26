package org.cheminfo.scripting.database.tests;

import java.util.ArrayList;

import com.mongodb.rhino.BSON;


public class TestFunction {

	/**
	 * This gives the possibiliby to test directly the new functions from java
	 */
	public static void main(String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		list.add("test1");
		list.add("test2");
		list.add("test3");
		Object js = BSON.from(list);
	}
}
