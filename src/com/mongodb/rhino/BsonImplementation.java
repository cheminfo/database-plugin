/**
 * Copyright 2010-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Apache License
 * version 2.0: http://www.opensource.org/licenses/apache2.0.php
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.mongodb.rhino;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.BSONObject;
import org.bson.types.Symbol;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.regexp.NativeRegExp;

import com.mongodb.BasicDBObject;
//import sun.org.mozilla.javascript.internal.NativeObject;
import com.mongodb.util.JSON;
import com.threecrickets.rhino.util.NativeRhinoUtil;

/**
 * Conversion between native Rhino objects and BSON.
 * <p>
 * This class can be used directly in Rhino.
 * 
 * @author Tal Liron
 */
public class BsonImplementation
{
	//
	// Operations
	//

	/**
	 * Recursively convert from native JavaScript to BSON-compatible types.
	 * <p>
	 * Recognizes JavaScript objects, arrays, Date objects, RegExp objects and
	 * primitives.
	 * <p>
	 * Also recognizes JavaScript objects adhering to MongoDB's extended JSON,
	 * converting them to BSON types: {$oid:'objectid'},
	 * {$binary:'base64',$type:'hex'}, {$ref:'collection',$id:'objectid'}.
	 * <p>
	 * Note that the {$date:timestamp} and {$regex:'pattern',$options:'options'}
	 * extended JSON formats are recognized as well as native JavaScript Date
	 * and RegExp objects.
	 * 
	 * @param object
	 *        A Rhino native object
	 * @return A BSON-compatible object
	 */
	public Object to( Object object )
	{
		if( object instanceof NativeJavaObject )
		{
			// This happens either because the developer purposely creates a
			// Java object, or because it was returned from a Java call and
			// wrapped by Rhino.
			return ( (NativeJavaObject) object ).unwrap();
		}
		else if( object instanceof NativeRegExp )
		{
			String[] regExp = NativeRhinoUtil.from( (NativeRegExp) object );

			// Note: JVM pattern does not support a "g" flag. Also, compiling
			// the pattern here is a waste of time. In short, better to use a
			// DBObject than a Pattern, even though the MongoDB driver supports
			// Pattern instances

			BasicDBObject bson = new BasicDBObject();
			bson.put( "$regex", regExp[0] );
			bson.put( "$options", regExp[1] );
			return bson;
		}

		else if( object instanceof NativeArray )
		{
			// Convert Rhino array to list

			NativeArray array = (NativeArray) object;
			int length = (int) array.getLength();
			ArrayList<Object> bson = new ArrayList<Object>( length );

			for( int i = 0; i < length; i++ )
				bson.add( to( ScriptableObject.getProperty( array, i ) ) );
			return bson;
		}
		else if( object instanceof ScriptableObject )
		{
			ScriptableObject scriptable = (ScriptableObject) object;

			// Is it in extended JSON format?
			Object r = mongoJsonExtender.from( scriptable, false );
			if( r != null )
				return r;

			r = NativeRhinoUtil.from( scriptable );
			if( r != null )
				return r;

			// Convert regular Rhino object

			BasicDBObject bson = new BasicDBObject();

			Object[] ids = scriptable.getAllIds();
			for( Object id : ids )
			{
				String key = id.toString();
				Object value = to( ScriptableObject.getProperty( scriptable, key ) );
				bson.put( key, value );
			}
			return bson;
		}
		else if( object instanceof Undefined )
		{
			return null;
		}
		else if( object instanceof CharSequence )
		{
			// This helps overcome an apparent bug in Rhino, whereby
			// org.mozilla.javascript.ConsString is not properly serializable
			// (see issue #6)
			return object.toString();
		}
		else
		{
			return object;
		}
	}

	/**
	 * Recursively convert from BSON to native JavaScript values.
	 * <p>
	 * Converts to JavaScript objects, arrays, Date objectss and primitives. The
	 * result is JSON-compatible.
	 * <p>
	 * Note that special MongoDB types (ObjectIds, Binary and DBRef) are not
	 * converted, but {@link MongoJsonExtender#to(Object,boolean,boolean)}
	 * recognizes them, so they can still be considered JSON-compatible in this
	 * limited sense.
	 * 
	 * @param object
	 *        A BSON object
	 * @return A JSON-compatible Rhino object
	 */
	public Object from( Object object )
	{
		return from( object, false );
	}

	/**
	 * Recursively convert from BSON to native JavaScript values.
	 * <p>
	 * Converts to JavaScript objects, arrays, Date objects, RegExp objects and
	 * primitives. The result is JSON-compatible.
	 * <p>
	 * Can optionally convert MongoDB's types to extended JSON:
	 * {$oid:'objectid'}, {$binary:'base64',$type:'hex'},
	 * {$ref:'collection',$id:'objectid'}.
	 * <p>
	 * Note that even if they are not converted, {@link JSON#to(Object)}
	 * recognizes them, so they can still be considered JSON-compatible in this
	 * limited sense.
	 * 
	 * @param object
	 *        A BSON object
	 * @param extendedJSON
	 *        Whether to convert extended JSON objects
	 * @return A JSON-compatible Rhino object
	 */
	public Object from( Object object, boolean extendedJSON )
	{
		if( object instanceof Collection<?> )
		{
			// Convert collection to NativeArray
			Collection<?> collection = (Collection<?>) object;
			
			Scriptable array = NativeRhinoUtil.newArray( collection.size() );

			int index = 0;
			for( Object item : collection )
				ScriptableObject.putProperty( array, index++, from( item, extendedJSON ) );

			return array;
		}
		else if( object instanceof Map<?,?> )
		{
			// Convert Map to NativeObject

			Map<?,?> map = (Map<?,?>) object;
			Scriptable nativeObject = NativeRhinoUtil.newObject();

			for( Object key : map.keySet() )
			{
				Object value = from( map.get( key ), extendedJSON );
				ScriptableObject.putProperty( nativeObject, key.toString(), value );
			}

			return nativeObject;
		}
		/*if( object instanceof List<?> )
		{
			// Convert list to NativeArray

			List<?> list = (List<?>) object;
			Scriptable array = NativeRhinoUtil.newArray( list.size() );

			int index = 0;
			for( Object item : list )
				ScriptableObject.putProperty( array, index++, from( item, extendedJSON ) );

			return array;
		}
		else if( object instanceof Set<?> )
		{
			// Convert set to NativeArray

			Set<?> set = (Set<?>) object;
			Scriptable array = NativeRhinoUtil.newArray( set.size() );

			int index = 0;
			for( Object item : set )
				ScriptableObject.putProperty( array, index++, from( item, extendedJSON ) );

			return array;
		}*/
		else if( object instanceof BSONObject )
		{
			// Convert BSON object to NativeObject

			BSONObject bsonObject = (BSONObject) object;
			Scriptable nativeObject = NativeRhinoUtil.newObject();

			for( String key : bsonObject.keySet() )
			{
				Object value = from( bsonObject.get( key ), extendedJSON );
				ScriptableObject.putProperty( nativeObject, key, value );
			}

			return nativeObject;
		}
		else if( object instanceof Symbol )
		{
			return ( (Symbol) object ).getSymbol();
		}
		else if( object instanceof Date )
		{
			return NativeRhinoUtil.to( (Date) object );
		}
		else if( object instanceof Pattern )
		{
			return NativeRhinoUtil.to( (Pattern) object );
		}
		else if( object instanceof Long )
		{
			// Wrap Long so to avoid conversion into a NativeNumber (which would
			// risk losing precision!)

			return NativeRhinoUtil.wrap( (Long) object );
		}
		else
		{
			if( extendedJSON )
			{
				Object r = mongoJsonExtender.to( object, true, false );
				if( r != null )
					return r;
			}

			return object;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final MongoJsonExtender mongoJsonExtender = new MongoJsonExtender();
}
