package org.cheminfo.scripting.database.mongo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.cheminfo.function.Function;
import org.cheminfo.function.scripting.SecureFileManager;
import org.cheminfo.script.utility.Shared;
import org.cheminfo.script.utility.URLFileManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.rhino.BSON;

public class MongoConnection {

	private MongoClient client = null;
	
	private DB database = null;
	private DBCollection collection = null;
	private GridFS gridfs = null;
	private final static BasicDBObject emptyObject = new BasicDBObject();
	
	private final static int ACCESS_NO = 0b00000000; //No access
	private final static int ACCESS_RO = 0b00000001; //Read-only
	private final static int ACCESS_RW = 0b00000011; //Read-write
	private int access = ACCESS_NO;

	private Function function=null;
	
	private boolean status = false;
	
	private final static String ERROR1 = "Insufficient permissions";
	private final static boolean DEBUG = false;
	

	public MongoConnection(String db, String key, Function function) {
		this.function=function;
		// Mode : db - key
		if(DEBUG)System.out.println("mode db-key");
		
		MongoClientURI mongoURI = new MongoClientURI(Shared.getProperty("MONGO_URI","mongodb://localhost"));

		if(!db.matches("[a-zA-Z0-9_]{3,20}")) { // Abort if database name is invalid
			if(DEBUG)System.out.println("MongoConnection refused: invalid database name");
			this.function.appendError("MongoConnection", "Connection refused: invalid database name");
			return;
		}

		this.access = getAccess(db,key); // Check database key and assign access
		if(this.access == ACCESS_NO) { // Abort if user has no access to the database
			if(DEBUG)System.out.println("MongoConnection refused: invalid key");
			this.function.appendError("MongoConnection", "Connection refused: invalid key");
			return;
		}

		try{
			this.client = new MongoClient(mongoURI);
			this.database = client.getDB(db);
			this.collection = database.getCollection("test");
			this.status = true;
		} catch(UnknownHostException e){
			this.function.appendError("MongoConnection", "Connection failed: "+e.toString());
			if(DEBUG) e.printStackTrace();
		}
	}
	
	public MongoConnection(String db, String basedir, String basedirkey, Function function){
		this.function=function;
		// Mode : path - key
		if(DEBUG)System.out.println("mode path-key");
		
		MongoClientURI mongoURI = new MongoClientURI(Shared.getProperty("MONGO_URI","mongodb://localhost"));

		if(!isSuperUser(basedir, basedirkey)) { // Only super users have access to a personal database
			if(DEBUG)System.out.println("MongoConnection refused: not super user");
			this.function.appendError("MongoConnection", "Connection refused: not super user");
			return;
		}

		String secureDbName = basedir.replaceAll("^.*/([^/]+@[^/]+).*","$1").replace('.', '_'); // We are sure that it a has not been modified because it matches the key
		if(!db.equals(secureDbName)) {
			if(DEBUG)System.out.println("MongoConnection refused: database name from javascript ("+db+") is different from secure java name ("+secureDbName+")");
			this.function.appendError("MongoConnection", "Connection refused: basedir and key do not match");
			return;
		}

		this.access = ACCESS_RW;
		try{
			this.client = new MongoClient(mongoURI);
			this.database = client.getDB(db);
			this.collection = database.getCollection("test");
			this.status = true;
		} catch(Exception e){
			this.function.appendError("MongoConnection", "Connection failed: "+e.toString());
			if(DEBUG) e.printStackTrace();
		}
	}
	
	public MongoConnection(String uri, Function function){
		this.function = function;
		// Mode : URI
		if(DEBUG)System.out.println("mode URI");
		//this.function.appendError("MongoConnection", "Connection failed: username-password mode is disabled");
		//return; // this mode is disabled for now
		
		MongoClientURI mongoURI = new MongoClientURI(uri);
		String db = mongoURI.getDatabase();

		this.access = ACCESS_RW;
		try{
			this.client = new MongoClient(mongoURI);
			this.database = client.getDB(db);
			this.collection = database.getCollection("test");
			this.status = true;
		} catch(Exception e){
			this.function.appendError("MongoConnection", "Connection failed: "+e.toString());
			e.printStackTrace();
		}
	}

	public void close(){
		this.client.close();
		this.client=null;
		this.collection=null;
		this.database=null;
		this.access=ACCESS_NO;
		this.status=false;
	}

	public boolean createIndex(Object key) {
		if((1 & (access >> 1)) == 1) {
			try{
				collection.createIndex((BasicDBObject)BSON.to(key));
				return true;
			} catch (Exception e){
				this.function.appendError("createIndex", e.toString());
				return false;
			}
		}
		this.function.appendError("createIndex", ERROR1);
		return false;
	}
	
	public boolean deleteIndex(Object key) {
		if((1 & (access >> 1)) == 1) {
			try{
				collection.dropIndex((BasicDBObject)BSON.to(key));
				return true;
			} catch (Exception e){
				this.function.appendError("deleteIndex", e.toString());
				return false;
			}
		}
		this.function.appendError("deleteIndex", ERROR1);
		return false;
	}

	public Object listIndex() {
		if((1 & access) == 1) {
			List<DBObject> list = collection.getIndexInfo();
			return BSON.from(list);
		}
		this.function.appendError("listIndex", ERROR1);
		return -1;
	}
	
	public boolean deleteCollection(){
		if((0b00000011 & access) == 0b00000011) {
			this.collection.drop();
			return true;
		}
		this.function.appendError("deleteCollection", ERROR1);
		return false;
	}

	public Object find() {
		return find(emptyObject, null);
	}
	public Object find(Object query, Object fields) {
		if((1 & access) == 1){
			try{
				BasicDBObject dbQuery = (BasicDBObject)BSON.to(query);
				BasicDBObject dbFields = null;
				if(fields != null)
					dbFields = (BasicDBObject)BSON.to(fields);
				DBCursor cursor = collection.find(dbQuery, dbFields);
				return cursor;
			} catch (Exception e){
				e.printStackTrace(System.out);
				this.function.appendError("find", e.toString());
				return -1;
			}
		}
		this.function.appendError("find", ERROR1);
		return -1;
	}
	public Object findOne() {
		return findOne(emptyObject, null);
	}
	public Object findOne(Object query, Object fields) {
		if((1 & access) == 1){
			try {
				BasicDBObject dbQuery = (BasicDBObject)BSON.to(query);
				BasicDBObject dbFields = null;
				if(fields != null)
					dbFields = (BasicDBObject)BSON.to(fields);
				return BSON.from(collection.findOne(dbQuery,dbFields), true);
			} catch (Exception e) {
				this.function.appendError("findOne", e.toString());
				return -1;
			}
		}
		this.function.appendError("findOne", ERROR1);
		return -1;
	}

	private int getAccess(String dbName, String key) {
		String rKey=URLFileManager.getFileKey(dbName, false);
		String wKey=URLFileManager.getFileKey(dbName, true);

		if (key.equals(rKey))
			return ACCESS_RO;
		else if (key.equals(wKey))
			return ACCESS_RW;
		else
			return ACCESS_NO;
	}
	
	public String getCollectionName(){
		return this.collection.getName();
	}
	public Object getCollectionNames(){
		if((1 & access) == 1) {
			Set<String> names = this.database.getCollectionNames();
			Object result = BSON.from(names);
			return result;
		}
		this.function.appendError("getCollectionNames", ERROR1);
		return -1;
	}

	public long getCount(){
		return getCount(emptyObject);
	}
	public long getCount(Object query){
		if((1 & access) == 1) {
			try {
				return this.collection.getCount((BasicDBObject)BSON.to(query));
			} catch (Exception e) {
				this.function.appendError("getCount", e.toString());
				return -1;
			}
		}
		this.function.appendError("getCount", ERROR1);
		return -1;
	}
	
	public String getDBName(){
		return this.database.getName();
	}

	public boolean getStatus() {
		return this.status;
	}
	
	public boolean insert(Object value) {
		if((1 & (access >> 1)) == 1) {
			try{
				collection.insert((BasicDBObject)BSON.to(value));
				return true;
			} catch (Exception e){
				this.function.appendError("insert", e.toString());
				return false;
			}
		}
		this.function.appendError("insert", ERROR1);
		return false;
	}
	
	public boolean remove(Object value) {
		if((1 & (access >> 1)) == 1) {
			try{
				collection.remove((BasicDBObject)BSON.to(value));
				return true;
			} catch (Exception e){
				this.function.appendError("remove", e.toString());
				return false;
			}
		}
		this.function.appendError("remove", ERROR1);
		return false;
	}
	
	public boolean save(Object value){
		if((0b00000011 & access) == 0b00000011) {
			try{
				this.collection.save((BasicDBObject)BSON.to(value));
				return true;
			} catch (Exception e){
				this.function.appendError("save", e.toString());
				return false;
			}
		}
		this.function.appendError("save", ERROR1);
		return false;
	}

	public void setCollection(String collectionName){
		this.collection = this.database.getCollection(collectionName);
	}

	public boolean update(Object query, Object value, boolean upsert, boolean multi){
		if((0b00000011 & access) == 0b00000011) {
			BasicDBObject toInsert=(BasicDBObject)BSON.to(value);
			BasicDBObject toUpdate=(BasicDBObject)BSON.to(query);
			try{
				collection.update(toUpdate, toInsert, upsert, multi);
				return true;
			} catch (Exception e){
				this.function.appendError("update", e.toString());
				return false;
			}
		}
		this.function.appendError("update", ERROR1);
		return false;
	}
	
	public void setGridFS() {
		setGridFS(null);
	}
	public void setGridFS(String bucket) {
		if(bucket == null)
			this.gridfs = new GridFS(this.database);
		else
			this.gridfs = new GridFS(this.database, bucket);
	}
	
	public ObjectId saveFile(String url) {
		if((1 & (access >> 1)) == 1) {
			if(this.gridfs == null) {
				this.function.appendError("saveFile", "GridFS is not initialized. Use setGridFS first.");
				return null;
			}
			try {
				URL goodUrl = new URL(url);
				GridFSInputFile file = this.gridfs.createFile(goodUrl.openStream(), true);
				file.save();
				Object id = file.getId();
				return (ObjectId)id;
			} catch (MalformedURLException e) {
				this.function.appendError("saveFile", "The URL ("+url+") is not valid");
				return null;
			} catch (IOException e) {
				this.function.appendError("saveFile", "Error reading the file : "+e.getMessage());
				return null;
			}
		} else {
			this.function.appendError("saveFile", ERROR1);
			return null;
		}
	}
	
	/**
	 * This function checks if the user (identified by his base directory and the associated key) is a super user.
	 * A super user lives in a first level directory. He has access to a personal database and can create other databases. 
	 * @param basedir
	 * @param basedirkey
	 * @return
	 */
	public static boolean isSuperUser(String basedir, String basedirkey) {
		if(SecureFileManager.getValidatedFilename(basedir,basedirkey, "") == null)
			return false;
		if(!Shared.isFirstLevel(basedir))
			return false;
		return true;

	}
}
