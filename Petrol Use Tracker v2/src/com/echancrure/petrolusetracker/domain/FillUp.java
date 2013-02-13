package com.echancrure.petrolusetracker.domain;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.echancrure.petrolusetracker.services.StorageService;
import com.echancrure.petrolusetracker.services.Utils;

/**
 * Represents a fill up by the user
 */
public class FillUp {
	Context context;
	static public final String DATE_TIME_NAME = "dateTime";
	static public final String ODOMETER_NAME = "odometer";
	static public final String PRICE_NAME = "price";
	static public final String VOLUME_NAME = "volume";
	static public final String PARTIAL_NAME = "partial";
	private static final String TAG = "FillUp";
	
	/** UTC of the fill up */
	private final long dateTime;
	/** odometer reading at the time of the fill up */
	private final int odometer;
	/** price per litre at the time of the fill up in local currency */
	private final int price;
	/** amount of litres of the fill up */
	private final float volume;
	/** indicates if the fill up is partial (i.e. not full) or not */
	private final boolean partial;
	
	/**
	 * A fillup represents the basic record of a fuel fillup at a petrol station 
	 * @param context 
	 * @param dateTime
	 * @param odometer
	 * @param price
	 * @param volume
	 * @param partial
	 */
	public FillUp(Context context, long dateTime, int odometer, int price, float volume, boolean partial) {
		this.context = context;
		if (odometer <= 0) {
            throw new IllegalArgumentException("Odometer must be positive:" + odometer);
        }
		if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive:" + price);
        }
		if (volume <= 0) {
			throw new IllegalArgumentException("Volume must be positive:" + volume);
		}
		this.dateTime = dateTime;
		this.odometer = odometer;
		this.price = price;
		this.volume = volume;
		this.partial = partial;
	}
	
	/**
	 * send the fillup to be uploaded
	 * @return
	 */
	public boolean upload() {
		JSONObject answer;
		Boolean status = false;
		try {
			StorageService communicationService = new StorageService(this.context);
			answer = communicationService.upload(StorageService.communicationType.REPORT_FILLUP, this);
			status = answer.getBoolean(StorageService.STATUS);
		} catch (JSONException e) {
			Utils.raiseRunTimeException(TAG, e, "When trying to up load a fill up");
		}
		Log.d(TAG, "status of upload of fillup:" + status);
		return status;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public long getDateTime() {
		return dateTime;
	}

	public int getOdometer() {
		return odometer;
	}

	public int getPrice() {
		return price;
	}

	public float getVolume() {
		return volume;
	}

	public boolean isPartial() {
		return partial;
	}

	/**
	 * encode a fill up as a JSON object
	 * @return the encode fillup as a JSON object
	 * @throws JSONException
	 */
	private JSONObject toJSONObject() throws JSONException {
		JSONObject jSonObject = new JSONObject();
		jSonObject.put(DATE_TIME_NAME, this.dateTime);
		jSonObject.put(ODOMETER_NAME, this.odometer);
		jSonObject.put(PRICE_NAME, this.price);
		jSonObject.put(VOLUME_NAME, this.volume);
		jSonObject.put(PARTIAL_NAME, this.partial);
		return jSonObject;
	}
	
}