/**
 * @object DB
 * Library that provides methods to connect to and interact with databases
 */
var DB = DB ? DB : {};

/**
 * @object DB.Couch
 * Library that provides methods for interaction with Couch.
 * Create an object for further manipulation
 * @constructor
 * Opens a connection to any database
 * 
 * @param uri:string The mongodb:// URI of the database
 * @param databaseName:string The name of the collection to select (Default: test).
 * 
 * @return +DB.Couch
 */

DB.Couch = function(url, databaseName, options) {
	this.url = url;
	this.databaseName = databaseName.replace(/[ _]+/g, "");

	// We will try to either create the database or to check if it exists

	// this.info=JSON.parse(COUCHAPI.httpAction(this.url+"/"+databaseName,
	// "PUT", "", {}));
};

/**
 * @object DB.Couch.prototype
 * Methods of the Couch object
 */
DB.Couch.prototype = {
	/**
	 * @function getUUIDs(number)
	 * Get some "number" of UUIDs
	 * 
	 * @param number:number Number of UUIDs to retrieve.
	 * @return +Object
	 */
	getUUIDs : function(number) {
		var number = number || 1;
		return JSON.parse(COUCHAPI.get(this.url + "/_uuids", {count:number}, {}));
	},

	
	
	/**
	 * @function get(id)
	 * Retrieve a specific record in the current specified database
	 * 
	 * @param id:string ID of the entry.
	 * @return +Object Selected record
	 */
	get : function(id) {
		return JSON.parse(COUCHAPI.get(this.url + "/" + this.databaseName + "/" + id, {}, {}));
	},

	/**
	 * @function revisionsInfo(id)
	 * Get information about all the available revisions of a document
	 * 
	 * @param id:string ID of the entry.
	 * @return +Object Object containing an array of available revisions
	 */
	revisionsInfo : function(id) {
		return JSON.parse(COUCHAPI.get(this.url + "/" + this.databaseName + "/" + id, {revs_info:true}, {}));
	},
	
	/**
	 * @function allDocs(id)
	 * Returns all the documents available in the database
	 * @define allDocs {"total_rows":"number","offset":"number","rows":"[Object]"}
	 * @return allDocs Object containing an array of all the documents
	 */
	allDocs: function() {
		return JSON.parse(COUCHAPI.get(this.url + "/" + this.databaseName + "/_all_docs", {}, {}));
	},
	
	/**
	 * @function put(id, myObject)
	 * Add or modify a record in the selected database with the specified unique ID
	 * 
	 * @param id:string ID of the entry
	 * @param myObject:object Javascript object to save
	 * @return +Object Result
	 */
	put : function(id, myObject) {
		var toSend=JSON.stringify(myObject);
		var result=COUCHAPI.put(
	        this.url+"/"+this.databaseName+"/"+id,
	        toSend,
	        {}
	        );
		return JSON.parse(result);
	},
	
	/**
	 * @function post(myObject)
	 * Add a record in the selected database and create a "_id"
	 * 
	 * @param id:string ID of the entry.
	 * @return +Object Result
	 */
	post : function(myObject) {
		var toSend=JSON.stringify(myObject);
		var result=COUCHAPI.post(
	        this.url+"/"+this.databaseName,
	        toSend,
	        {}
	        );
		return JSON.parse(result);
	},


	/**
	 * @function remove(id)
	 * Remove a specific record in the current specified database
	 * 
	 * @param id:string ID of the entry to delete
	 * @return +Object Result
	 */
	remove : function(id, revision) {
		return JSON.parse(COUCHAPI.remove(this.url + "/" + this.databaseName + "/" + id, {rev:revision}, {}));
	},
	
	
	/**
	 * @function create()
	 * Create the database
	 * @return +Object Selected record
	 */
	create : function(id) {
		return JSON.parse(COUCHAPI.put(this.url + "/" + this.databaseName,{},{}));
	},
	
	/**
	 * @function destroy()
	 * Delete the currently open database
	 * 
	 * @return +Object Result
	 */
	destroy : function() {
		return JSON.parse(COUCHAPI.remove(this.url + "/" + this.databaseName, {}, {}));
	}
};