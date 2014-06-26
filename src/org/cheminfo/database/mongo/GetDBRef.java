/*

General function that allows to search for an entry in mongodb

**/ 
package org.cheminfo.database.mongo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.cheminfo.database.utility.Shared;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class GetDBRef extends HttpServlet {

	private static final long serialVersionUID = -3099402519764596327L;
	final static boolean DEBUG=false;

	
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		Shared.setServletContext(config.getServletContext());
	}

    public void doGet(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException { 

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		
		String objectId=request.getParameter("id");
		String collection=request.getParameter("collection");
		String database=request.getParameter("database");
		String key=request.getParameter("key");
		
		if(objectId==null||collection==null||database==null||key==null){
			out.print("{error:\"Missing parameter\"}");
			return;
		}
		
		String realKey = Shared.getKey(objectId+collection+database, false);
		System.out.println(realKey);
		
		if(!key.equals(realKey)){
			out.print("{error:\"Invalid key\"}");
			return;
		}
		
		MongoClientURI mongoURI = new MongoClientURI(Shared.getProperty("MONGO_URI","mongodb://localhost/"));
		MongoClient client = new MongoClient(mongoURI);
		
		DB db = client.getDB(database);
		ObjectId id = new ObjectId(objectId);

		DBRef ref = new DBRef(db, collection, id);
		BasicDBObject result = (BasicDBObject) ref.fetch();
		
		if(result==null) {
			out.print("{error:\"This record does not exist\"}");
			return;
		}
		
		out.print(result.toString());
		client.close();

    }
 
    public void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }

}
