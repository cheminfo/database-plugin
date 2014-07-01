/**
 * @object DB
 * Library that provides methods to connect to and interact with databases
 */
var DB = DB ? DB : {};

/**
 * @object DB.MySQL
 * Library that provides methods for interaction with MySQL
 */

DB.MySQL = function (host,user,pass,dbname) {
	
	var db = MYSQLAPI.getDb(host,user,pass,dbname);
	if(db) {
		this.DBMySQL = db;
	} else {
		throw Error("Problems connecting to the database: "+dbname);
	}
		
};

DB.MySQL.prototype = {
		getTableNames : function(){
			return JSON.parse(this.DBMySQL.getTableNames());
		},
		
		describeTable : function(tableName){
			return JSON.parse(this.DBMySQL.describeTable(tableName));
		},
		
		createTable : function(tableName, structure){
			return this.DBMySQL.createTable(tableName, structure);
		},
		
		dropTable : function(tableName, options){
			return this.DBMySQL.dropTable(tableName, options);
		},
		
		createIndex : function(tableName, columnName, indexName, options){
			return this.DBMySQL.createIndex(tableName, columnName, indexName, options);
		},
		
		insert : function(tableName, structure){
			var rs = this.DBMySQL.insert(tableName, structure);
			return true; 
		},
		
		update : function(tableName, structure){
			return JSON.parse(this.DBMySQL.update(tableName, structure));
		},
		
		delete2 : function(tableName, structure, options){
			return this.DBMySQL.delete2(tableName, structure, options);
		},
		
		selectTable : function(tableName, where, options){
			return JSON.parse(this.DBMySQL.select(tableName, where, options));
		},
		
		select : function(query,options){
			return JSON.parse(this.DBMySQL.select(query,options));
		},
		
		close : function(){
			this.DBMySQL.close();
		}
};