package org.cheminfo.scripting.database.mongo;

import java.net.UnknownHostException;
import java.util.List;

import org.cheminfo.function.Function;
//import org.cheminfo.script.utility.Shared;
//import org.cheminfo.script.utility.URLFileManager;
import org.mozilla.javascript.NativeObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.rhino.BSON;

public class Mongo extends Function {
	
	public MongoConnection connect(String db, NativeObject options){
		if(NativeObject.hasProperty(options, "method")){
			double method = (double)NativeObject.getProperty(options, "method");
			if(method == 1){
				String key = (String)NativeObject.getProperty(options, "key");
				return new MongoConnection(db, key, this);
			}
			if(method == 2){
				String path = (String)NativeObject.getProperty(options, "path");
				String key = (String)NativeObject.getProperty(options, "key");
				return new MongoConnection(db, path, key, this);
			}
			if (method == 3){
				String uri = (String)NativeObject.getProperty(options, "uri");
				return new MongoConnection(uri, this);
			}
		}
		return null;
	}
	
	public String create(String dbName, String basedir, String basedirkey) {
		if(!MongoConnection.isSuperUser(basedir, basedirkey))
			return "{\"result\":\"nok\", \"message\":\"Database creation is only available to super users\"}";
		if(!dbName.matches("[a-zA-Z0-9_]{3,20}"))
			return "{\"result\":\"nok\",\"message\":\"Invalid database name\"}";
		try {
			MongoClientURI mongoURI = new MongoClientURI("");//hared.getProperty("MONGO_URI","mongodb://localhost"));
			
			MongoClient client = new MongoClient(mongoURI);
			List<String> names = client.getDatabaseNames();
			for (String name : names) {
				if(name.equals(dbName)) {
					return "{\"result\":\"nok\",\"message\":\"Database name already in use\"}";
				}
			}
			DB database = client.getDB(dbName);
			DBCollection collection = database.getCollection("test");
			collection.insert(new BasicDBObject("test","test"));
			String rKey="";//URLFileManager.getFileKey(dbName, false);
			String wKey="";//URLFileManager.getFileKey(dbName, true);
			client.close();
			return "{\"result\":\"ok\",\"read\":\""+rKey+"\",\"write\":\""+wKey+"\"}";
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "{\"result\":\"nok\", \"message\":\"Java unknown host exception\"}";
		}
	}
	
	/*public String drop(String host, int port, String dbName, String key, String basedir, String basedirkey){
		if(!MongoConnection.isSuperUser(basedir, basedirkey))
			return "{\"result\":\"nok\", \"message\":\"Database deletion is only available to super users\"}";
		if(!dbName.matches("[a-zA-Z0-9_]{3,20}"))
			return "{\"result\":\"nok\",\"message\":\"Invalid database name\"}";
		String wKey=URLFileManager.getFileKey(dbName, true);
		if(!wKey.equals(key))
			return "{\"result\":\"nok\",\"message\":\"Invalid write key\"}";
		try {
			MongoClient client = new MongoClient(host, port);
			client.dropDatabase(dbName);
			return "{\"result\":\"ok\"}";
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "{\"result\":\"nok\", \"message\":\"Java unknown host exception\"}";
		}
	}*/
	
	public Object bsonToJs(Object object){
		return BSON.from(object, true);
	}
}
