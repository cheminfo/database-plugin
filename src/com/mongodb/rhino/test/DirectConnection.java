package com.mongodb.rhino.test;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class DirectConnection {

	public static void main(String[] args) throws UnknownHostException {
		MongoClient mongo = new MongoClient();
		DB db = mongo.getDB("test");
		DBCollection collection = db.getCollection("test");

		BasicDBObject test=new BasicDBObject();
		test.put("ab","cd");
		collection.insert(test);

		
	}

}
