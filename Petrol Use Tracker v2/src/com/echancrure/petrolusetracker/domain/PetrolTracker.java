package com.echancrure.petrolusetracker.domain;

import android.content.Context;
import android.util.Log;

/**
 * Controller for the domain layer: presents an API to the view layer
 * Implemented as a singleton (eager version)
 * @author Christophe Meudec
 */
public class PetrolTracker {
	private final static String TAG = "PetrolTracker";
	/**
	 * a fill up reported by the user of the app
	 */
	private FillUp reportedFillUp = null;
	/** The singleton controller for the entire app */
	static private final PetrolTracker petrolTracker = new PetrolTracker();
	
	private PetrolTracker() {
		//nothing for now, if this gets complicated or if all the resources are not ready at load up time, consider a lazy implementation of the singleton pattern
	}
	
	/**
	 * Obtain the app's controller
	 * @return the single controller instance for the entire app
	 */
	public static PetrolTracker getInstance() {
		return petrolTracker;
	}

	/**
	 * report a new fillup
	 * @param uTCDateTime the UTC date time of the fill up (not necessarily the same as the submission time: fill ups may be submitted later on from receipts for example) 
	 * @param odometer the odometer value at the time of the fill up
	 * @param price the price per volume 
	 * @param volume the volume put in
	 * @param partial indicates if the fill up was partial or whether the tank was full afterwards (preferred)
	 */
	public boolean reportFillUp(Context context, long uTCDateTime, int odometer, int price, float volume, boolean partial) {
		Log.d(TAG, "reportFillUp API call");
		reportedFillUp = new FillUp(context, uTCDateTime, odometer, price, volume, partial);
		return reportedFillUp.upload();
	}
}
