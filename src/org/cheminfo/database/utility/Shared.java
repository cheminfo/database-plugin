/*
* $Header: /usr/local/cvs/cvsrep/databasePlugin/src/org/cheminfo/database/utility/Shared.java,v 1.2 2013/10/24 08:24:59 mzasso Exp $
*/

package org.cheminfo.database.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.servlet.ServletContext;


/**
 *  Shared used the SingleTon principle. For more information check:
 * http://www.javaworld.com/javaworld/jw-04-2003/jw-0425-designpatterns.html
 */

public class Shared {
	private final static boolean DEBUG=false;
	
	final static String DEFAULT_PROPERTY_FILES="/usr/local/databasePlugin/database.properties";
	
	private static Shared instance = null;
	private ServletContext servletContext=null;
	
	// Tells if the Properties have been initialised
	private Properties properties = null;
	
	protected Shared() {
		// Exists only to defeat instantiation.
	}
	
	public static synchronized Shared getInstance() {
		if (instance == null) {
			instance = new Shared();
		}
		return instance;
	}
	

	public static void setServletContext(ServletContext servletContext) {
		getInstance().servletContext=servletContext;
	}
	
	
	public static String getServletRealPath() {
		return getInstance().servletContext.getRealPath("");
	}
	
	public static String getContextProperty(String propertyName, String defaultValue) {
		try {
			String value=getInstance().servletContext.getInitParameter(propertyName);
			if (value!=null) return value;
		} catch (Exception e) {
			return defaultValue;
		}
		return defaultValue;
	}
      
	public static String getProperty(String propertyName, String defaultValue) {
		initProperties();
		return getInstance().properties.getProperty(propertyName,defaultValue);
	}

	
	private static void initProperties() {
		if (getInstance().properties!=null) return;
		String filename=getInstance().getContextProperty("PROPERTY_FILENAME",DEFAULT_PROPERTY_FILES);
		try {
			InputStream is=null;
			is = new FileInputStream(filename);
			getInstance().properties=new Properties();
			getInstance().properties.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getKey(String name, boolean mayWrite) {
		String type="read";
		if (mayWrite) {
			type="write";
		}
		return getUrlSafeDigest(name+Shared.getProperty("SEED_KEY","")+type);
	}
	
	public static String getUrlSafeDigest(String toDigest) {
		String toReturn=getDigest(toDigest.getBytes()).replaceAll("[^a-zA-Z0-9]","").substring(0,10);
		return toReturn;
	}
	
	public static String getDigest(byte[] toDigest) {
		if ((toDigest==null) || (toDigest.length==0)) return null;
		String digest="";
		try {				
			// we create a hash for the id
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			digest = Base64.encodeBytes(algorithm.digest(toDigest));
		} catch (NoSuchAlgorithmException e) {throw new RuntimeException (e.toString());}
		return digest;
	}
}
