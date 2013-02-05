package com.google.gwt.sample.stockwatcher.server;

import com.google.appengine.api.datastore.Entity;

/**
 * This class handles all the CRUD operations related to a FillUp entity.
 * Adapted from original from Google tutorial http://googcloudlabs.appspot.com/codelabexercise1.html
 */
public class Fillup {
	static public final String DATE_TIME_NAME = "dateTime";
	static public final String ODOMETER_NAME = "odometer";
	static public final String PRICE_NAME = "price";
	static public final String VOLUME_NAME = "volume";
	static public final String PARTIAL_NAME = "partial";
	static public final String FILLUP_KIND = "fillup";


  /** 
   * create a new fillup entity and store it
   * @param dateTime
   * @param odometer
   * @param price
   * @param volume
   * @param partial
   */
  public static void create(long dateTime, int odometer, int price, double volume, boolean partial) {
	  Entity fillup = new Entity(FILLUP_KIND);
	  fillup.setProperty(DATE_TIME_NAME, dateTime);
	  fillup.setProperty(ODOMETER_NAME, odometer);
	  fillup.setProperty(PRICE_NAME, price);
	  fillup.setProperty(VOLUME_NAME, volume);
	  fillup.setProperty(PARTIAL_NAME, partial);
	  Util.persistEntity(fillup);
	}
  
  /**
   * Return all the fillups
   * @return  fillups
   */
  public static Iterable<Entity> getAllFillups() {
    return Util.listEntities(FILLUP_KIND);
  }

}
