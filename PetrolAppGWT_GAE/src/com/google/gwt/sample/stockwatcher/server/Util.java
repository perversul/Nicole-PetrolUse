package com.google.gwt.sample.stockwatcher.server;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;


/**
 * This is the utility class for all servlets. It provides method for inserting,
 * deleting, searching the entity from data store. Also contains method for
 * displaying the entity in JSON format.
 * Adapted from original from Google tutorial http://googcloudlabs.appspot.com/codelabexercise1.html
 */
public class Util {

  private static final Logger logger = Logger.getLogger(Util.class.getCanonicalName());
  private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  

/**
 * 
 * @param entity  : entity to be persisted
 */
  public static void persistEntity(Entity entity) {
  	logger.log(Level.INFO, "Saving entity");
  	datastore.put(entity);  	
  }


	/**
	 * Search and return the entity from datastore.
	 * @param key : key to find the entity
	 * @return  entity
	 */
 
  public static Entity findEntity(Key key) {
  	logger.log(Level.INFO, "Search the entity");
  	try {	  
  	  return datastore.get(key);
  	} catch (EntityNotFoundException e) {
  	  return null;
  	}
  }
 

	/***
	 * Search entities based on search criteria
	 * @param kind
	 * @return List all entities of a kind from the cache or datastore (if not
	 *         in cache) with the specified properties
	 */
  public static Iterable<Entity> listEntities(String kind) {
  	logger.log(Level.INFO, "Search entities based on search criteria");
  	Query q = new Query(kind);
  	PreparedQuery pq = datastore.prepare(q);
  	return pq.asIterable();
  }

	/**
	 * List the entities in JSON format
	 * 
	 * @param entities  entities to return as JSON strings
	 */
  public static String writeJSON(Iterable<Entity> entities) {
    logger.log(Level.INFO, "creating JSON format object");
  	StringBuilder sb = new StringBuilder();
  	
  	int i = 0;
  	//sb.append("{\"" + FillupServlet.APIPetrolTrackerObject + "\": [");
  	sb.append("{[");
  	for (Entity anEntity : entities) {
  	  sb.append("{");
  	  if (anEntity.getKey().getName() == null)
  		sb.append("\"name\" : \"" + anEntity.getKey().getId() + "\",");
  	  else
  		sb.append("\"name\" : \"" + anEntity.getKey().getName() + "\",");
  	  Map<String, Object> properties = anEntity.getProperties();
  	  for (String key : properties.keySet()) {
  		sb.append("\"" + key + "\" : \"" + properties.get(key) + "\",");
  	  }
  	  sb.deleteCharAt(sb.lastIndexOf(","));
  	  sb.append("},");
  	  i++;
  	}
  	if (i > 0) {
  	  sb.deleteCharAt(sb.lastIndexOf(","));
  	}  
  	sb.append("]}");
  	return sb.toString();
  }
 
	/**
	 * Utility method to send the error back to UI
	 * @param data
	 * @param resp
	 * @throws IOException 
	 */
  public static String getErrorMessage(Exception ex) throws IOException{
    return "Error:"+ex.toString();
  }
 
  /**
   * get DatastoreService instance
   * @return DatastoreService instance
   */
  public static DatastoreService getDatastoreServiceInstance(){
	  return datastore;
  }
}