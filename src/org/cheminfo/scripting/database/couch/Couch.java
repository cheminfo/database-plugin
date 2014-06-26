package org.cheminfo.scripting.database.couch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cheminfo.function.Function;
import org.mozilla.javascript.NativeObject;


public class Couch extends Function{
	
	final static boolean DEBUG=false;
	
	
	public String get(String urlString, NativeObject parametersObject, NativeObject options) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
            HttpGet httpget = new HttpGet(urlString+encodeParameters(parametersObject));
        	httpget.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        	HttpResponse response = httpclient.execute(httpget);
        	
        	HttpEntity resEntity = response.getEntity();
        	return new java.util.Scanner(resEntity.getContent()).useDelimiter("\\A").next();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			appendError("Default.get","Error: "+e.toString());
			return e.toString();
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace(System.out);
				appendError("Default.get","Error: "+e.toString());
			}
        }
	}
	
	public String remove(String urlString, NativeObject parametersObject, NativeObject options) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
            HttpDelete httpdelete = new HttpDelete(urlString+encodeParameters(parametersObject));
        	httpdelete.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        	HttpResponse response = httpclient.execute(httpdelete);
        	
        	HttpEntity resEntity = response.getEntity();
        	return new java.util.Scanner(resEntity.getContent()).useDelimiter("\\A").next();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			appendError("Default.delete","Error: "+e.toString());
			return e.toString();
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace(System.out);
				appendError("Default.delete","Error: "+e.toString());
			}
        }
	}
	
	private String encodeParameters(NativeObject parametersObject) throws UnsupportedEncodingException {
		if (parametersObject==null) return "";
		String toReturn="?";
		for (Object key : parametersObject.keySet()) {
			if (toReturn.length()>1) toReturn+="&";
			toReturn+=URLEncoder.encode(key.toString(), "UTF-8")+"="+URLEncoder.encode(parametersObject.get(key).toString(), "UTF-8");
		}
		return toReturn;
	}
	
	public String put(String urlString, String content, NativeObject options) {
		return httpAction(urlString, "put", content, options);
	}
	
	public String post(String urlString, String content, NativeObject options) {
		return httpAction(urlString, "post", content, options);
	}
	
	// ACTION = PUT POST 
	private String httpAction(String urlString, String action, String urlEncodedContent, NativeObject options) {
		action=action.toLowerCase();
		CloseableHttpClient httpclient = HttpClients.createDefault();
        String toReturn = "";
        try {
        	StringEntity entity=null;
            if (urlEncodedContent.length()>0) {
              	entity=new StringEntity(urlEncodedContent);
            }
            
            HttpResponse response=null;
            if (action.equals("post")) {
            	HttpPost httppost = new HttpPost(urlString);
            	if (entity!=null) httppost.setEntity(entity);
            	httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            	response = httpclient.execute(httppost);
            } else if (action.equals("put")) {
            	HttpPut httpput = new HttpPut(urlString);
            	if (entity!=null) httpput.setEntity(entity);
            	httpput.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            	response = httpclient.execute(httpput);
             } else {
            	appendError("Default.httpAction","Unsupported action: "+action);
            	return "{'error':'Unsupported action: '"+action+"}";
            }
            
            HttpEntity resEntity = response.getEntity();
               
            if (resEntity != null) {
            	try {
            		toReturn = new java.util.Scanner(resEntity.getContent()).useDelimiter("\\A").next();
                } catch (java.util.NoSuchElementException e) {
                	e.printStackTrace(System.out);
                	appendError("Default.getURL","Error: "+e.toString());
                	return e.toString();
                }
            }
		} catch (Exception e) {
			e.printStackTrace(System.out);
			appendError("Default.getURL","Error: "+e.toString());
			return e.toString();
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace(System.out);
				appendError("Default.getURL","Error: "+e.toString());
			}
        }
		
		return toReturn;
	}
}
