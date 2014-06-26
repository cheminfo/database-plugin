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
		
		this.DBMySQL = MYSQLAPI.getDb(host,user,pass,dbname),
		
		this.getTableNames = function(){
			this.DBMySQL.getTableNames();
		},
		
		this.describeTable = function(tableName){
			console.log("describeTable XXX");
			this.DBMySQL.describeTable(tableName);
		},
		
		this.createTable = function(tableName, structure){
			this.DBMySQL.createTable(tableName, structure);
		},
		
		this.insert = function(tableName, structure){
			this.DBMySQL.insert(tableName, structure);
		},
		
		this.update = function(tableName, structure){
			this.DBMySQL.update(tableName, structure);
		},
		
		this.delete = function(tableName, structure){
			this.DBMySQL.delete(tableName, structure);
		},
		
		this.selectTable = function(tableName, where, options){
			this.DBMySQL.select(tableName, where, options);
		},
		
		this.select = function(query){
			this.DBMySQL.select(query);
		},
		
		return this;
};