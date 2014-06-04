/**
 * @object DB
 * Library that provides methods to connect to and interact with databases
 */
var DB = DB ? DB : {};

/**
 * @object DB.Mongo
 * Library that provides methods for interaction with MongoDB
 */

DB.Mongo = function(db,collectionName,options){
	this.mongoConnection = MONGOAPI.connect(db,options);
	if(this.mongoConnection.getStatus()){
		this.mongoConnection.setCollection(collectionName);
	}
	else{
		console.error("Error during MongoDB connection.");
		this.mongoConnection = null;
	}
};

/**
 * @function connect(uri, [collectionName])
 * Opens a connection to any database
 * 
 * @param	uri:string				The mongodb:// URI of the database
 * @param	collectionName:string	The name of the collection to select (Default: test).
 * 
 * @return	+DB.Mongo
 */
DB.Mongo.connect= function(uri,collectionName){
	var name = collectionName || "test";
	return new DB.Mongo("",name,{method:3, uri:uri});
};

/**
 * @function open([collectionName], options)
 * Opens a connection on a Mongo database
 * 
 * @param	collectionName:string	The name of the collection to select (Default: test).
 * 
 * @option	dbName					The name of the database to select (Default: personal database. Only super users have a personal database).
 * @option	dbKey					The key to the selected database
 * 
 * @return	+DB.Mongo
 */
DB.Mongo.open= function(collectionName, options){
	var name="test";
	var opt={};
	if(collectionName){
		if(typeof collectionName == "string")
			name=collectionName;
		else if(typeof collectionName == "object"){
			opt=collectionName;
		}
		if(options && (typeof options == "object"))
			opt=options;
	}
	if(opt.dbName && opt.dbKey){
		return new DB.Mongo(opt.dbName, name, {method:1, key: opt.dbKey});
	}
	var split = Global.basedir.split("/");
	var dbName = split[split.length-2].replace(".","_");
	return new DB.Mongo(dbName, name, {method:2, path: Global.basedir, key: Global.basedirkey});
};

/**
 * @function create(dbName)
 * Creates a new database. Only super users can use this function
 * 
 * @param	dbName:string	The name of the database to create
 * @define	DBMongoCredentials	{"dbName":"string","readKey":"string","writeKey":"string"}
 * @return	DBMongoCredentials
 */
DB.Mongo.create= function(dbName){
	var result = JSON.parse(MONGOAPI.create(dbName, Global.basedir, Global.basedirkey));
	if(!result)
		throw "Error in DB.Mongo.create";
	if(result.result=="ok"){
		console.log("The database "+dbName+" has been created. Your credentials are: ");
		console.log("Read-only key: "+result.read);
		console.log("Read-write key: "+result.write);
		return {dbName: dbName, readKey: result.read, writeKey: result.write};
	}
	if(result.result=="nok"){
		console.log("The database "+dbName+" could not be created.");
		console.log("Error: "+result.message);
		return;
	}
};

/**
 * @disabled (security issues)
 * function drop(dbName, key)
 * Drops the database. Only super users can use this function
 * 
 * param	dbName:string	The name of the database to create
 * param	key:string		The write key of the database
 * 
 */
/*drop: function(dbName, key){
		var hostPort = DB.Mongo.getHostPort(mongoHost);
		var result = JSON.parse(MONGOAPI.drop(hostPort.host, hostPort.port, dbName, key, Global.basedir, Global.basedirkey));
		if(!result)
			throw "Error in DB.Mongo.drop";
		if(result.result=="ok"){
			console.log("The database "+dbName+" has been deleted.");
			return;
		}
		if(result.result=="nok"){
			console.log("The database "+dbName+" could not be deleted.");
			console.log("Error: "+result.message);
			return;
		}
	},*/


DB.Mongo.error= function(methodName, type, option){
	if(type===1) {
		console.warn("Error in DB.Mongo."+methodName+": insufficient permissions.");
		return;
	}
	if(type===2) {
		console.warn("Error in DB.Mongo."+methodName+": parameter is not an object.");
		return;
	}
	if(type===3) {
		console.warn("Error in DB.Mongo."+methodName+": "+option);
		return;
	}
	if(type===4) {
		console.warn("Error in DB.Mongo."+methodName+": invalid query object.");
		return;
	}
};

/**
 * @object DB.Mongo.prototype
 * Methods of the Mongo object
 */
DB.Mongo.prototype = {
		/**
		 * @function useCollection(collectionName)
		 * Selects a new collection
		 * 
		 * @param	collectionName:string	The name of the collection to select.
		 */
		useCollection : function(collectionName){
			if(typeof collectionName == "string")
				this.mongoConnection.setCollection(collectionName);
		},

		/**
		 * @function currentCollection()
		 * Returns the name of the current collection
		 * 
		 * @return	string	Name of the collection
		 */
		currentCollection : function(){
			return this.mongoConnection.getCollectionName();
		},

		/**
		 * @function count([query])
		 * Returns the number of objects in the collection that match the query. If there is no query, the size of the collection is returned
		 * 
		 * @param	query:+Object	The query object (optional)
		 * @return	number
		 */
		count : function(query){
			var result;
			if(!query)
				result = this.mongoConnection.getCount();
			else if(typeof query != "object"){
				DB.Mongo.error("count", 2);
				return false;
			}
			else
				result = this.mongoConnection.getCount(query);
			return result;
		},

		/**
		 * @function createIndex(object)
		 * Creates an index in the collection
		 * 
		 * @param	object:+Object	Object describing the index
		 * 
		 * @example	mongo.createIndex({i:1})	Creates an index on "i", ascending
		 */
		createIndex : function(object){
			if(typeof object != "object") {
				DB.Mongo.error("createIndex", 2);
				return;
			}
			this.mongoConnection.createIndex(object);
		},

		/**
		 * @function deleteIndex(object)
		 * Deletes an index in the collection
		 * 
		 * @param	object:+Object	Object describing the index
		 * 
		 */
		deleteIndex : function(object){
			if(typeof object != "object") {
				DB.Mongo.error("deleteIndex", 2);
				return;
			}
			this.mongoConnection.deleteIndex(object);
		},

		/**
		 * @function listIndex()
		 * Returns an array containing the indices of the current collection
		 * 
		 * @return	[+Object]
		 */
		listIndex : function(){
			return this.mongoConnection.listIndex();
		},

		/**
		 * @function deleteCollection()
		 * Deletes the current collection
		 * 
		 */
		deleteCollection : function(){
			this.mongoConnection.deleteCollection();
		},

		/**
		 * @function insert(object)
		 * Adds a new object to the collection
		 * 
		 * @param	object:+Object	The JSON object to insert or an array of objects
		 */
		insert : function(object) {
			if(typeof object != "object") {
				DB.Mongo.error("insert", 2);
				return;
			}
			if(object instanceof Array) {
				for(var i=0; i<object.length; i++){
					if(!this.mongoConnection.insert(object[i])){
						DB.Mongo.error("insert", 3, "object "+i+" could not be inserted.");
					}
				}
				return;
			}
			if(!this.mongoConnection.insert(object)){
				DB.Mongo.error("insert", 3, "object could not be inserted.");
				return;
			}
		},

		/**
		 * @function remove(query)
		 * Removes all objects matching the query
		 * 
		 * @param	query:+Object	The query object
		 */
		remove : function(query) {
			if(typeof query != "object") {
				DB.Mongo.error("remove", 2);
				return;
			}
			if(!this.mongoConnection.remove(query)){
				DB.Mongo.error("remove", 3, "Remove action has failed.");
				return;
			}
		},

		/**
		 * @function findOne([query, [fields]])
		 * Returns an object from the collection that matches the query
		 * 
		 * @param	query:+Object	The query object
		 * @param	fields:+Object	Object describing which fields should be returned
		 * @return	+Object
		 * @example	mongo.findOne({age:22},{name:1,birthdate:1})
		 */
		findOne : function(query, fields) {
			if(!query)
				return this.mongoConnection.findOne();
			else if(typeof query != "object"){
				DB.Mongo.error("findOne", 2);
				return false;
			}
			if(fields && (typeof fields == "object"))
				return this.mongoConnection.findOne(query, fields);
			else
				return this.mongoConnection.findOne(query, null);
		},

		/**
		 * @function find([query, [fields]])
		 * Returns an iterator to access objects from the collection that match the query
		 * 
		 * @param	query:+Object		The query object (optional)
		 * @param	fields:+Object		Object describing which fields should be returned
		 * 
		 * @define	DBMongoIterator		{"next":"fn() -> +Object"}
		 * @return	DBMongoIterator		Iterator on the results. Its next function returns false when there is no more result.
		 * 
		 * @example	mongo.find({age:22},{name:1,birthdate:1})
		 */
		find : function(query, fields) {
			var result;
			if(!query)
				result = this.mongoConnection.find();
			else if(typeof query != "object"){
				DB.Mongo.error("find", 2);
				return false;
			}
			else if(fields && (typeof fields == "object"))
				result = this.mongoConnection.find(query, fields);
			else
				result = this.mongoConnection.find(query, null);

			var Iterator = function(cursor){
				this.cursor = cursor;
				this.next = function(){
					try{
						if(!this.cursor.hasNext())
							return false;
						else
							return MONGOAPI.bsonToJs(this.cursor.next());
					}
					catch(e){
						console.error("Error getting the next result: "+e,"find.next");
						return false;
					}
				};
			};

			return new Iterator(result);
		},

		/**
		 * @function update(query, object, options)
		 * Updates the object that matches the query with the value
		 * 
		 * @param	query:+Object	The query object 
		 * @param	object:+Object	The JSON object to add
		 * @param	options:+Object	Object containing the options
		 * 
		 * @option	upsert	if the database should create the element if it does not exist (Default: false)
		 * @option	multi	if the update should be applied to all objects matching (Default: false)
		 */
		update : function(query, object, options) {
			options = options ? options : {};
			var upsertO = options.upsert ? true : false;
			var multiO = options.multi ? true : false;

			if((typeof object != "object") || (typeof query != "object"))
				DB.Mongo.error("update", 2);
			this.mongoConnection.update(query,object,upsertO,multiO);
		},

		/**
		 * @function save(object)
		 * Saves the object in the collection. Performs an insert (objectID does not exist) or an update (objectID already in the DB)

		 * @param	object:+Object	The JSON object to save
		 * 
		 */
		save : function(object) {
			if(typeof object != "object")
				DB.Mongo.error("save", 2);
			this.mongoConnection.save(object);
		},

		/**
		 * @function close()
		 * Closes the connection. After using this function, the Mongo object cannot be used anymore
		 * 
		 */
		close :function() {
			this.mongoConnection.close();
			this.mongoConnection = null;
		}
};