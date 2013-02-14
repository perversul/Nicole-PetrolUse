package com.echancrure.petrolusetracker.services;

import org.json.JSONException;
import org.json.JSONObject;

import com.echancrure.petrolusetracker.domain.FillUp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Deals with communicating with the cloud including whenever no network is available
 * @author Christophe Meudec
 */
public class StorageService {
	private static final String TAG = "StorageService";
	private Context context;
	/** Boolean answer */
	static public final String STATUS = "status";
	/** Indicates the type of the communication request */
	static public enum communicationType {
        REPORT_FILLUP, LOGIN
    }
	
	
	public StorageService(Context context) {
		this.context = context;
	}
	
	/**
	 * For the moment just upload the data to the cloud, fails if no network: eventually we must deal with no network
	 * @param communicationType the type of the communication to be performed, login, report fillup etc. 
	 * @param data the JSON object encoding the object to be communicated
	 */
	public JSONObject upload(communicationType type, FillUp fillUp) {
		Log.d(TAG, "Requesting communication:" + type.toString());
		JSONObject answer = new JSONObject();
		try {
			answer.put(STATUS, false);	//we assume the worse, error is returned by default
		} catch (JSONException e) {
			Utils.raiseRunTimeException(TAG, e, "Will never occur");
		}
		switch (type) {
		case REPORT_FILLUP: //add to database; launch geo-location service
			Database db = new Database(this.context);
			db.open();
			long rowId = db.insertFillUp(fillUp); //all this should be put into an asynchronous task if we feel that it takes more than 0.2s not the case at the moment so we don't
			db.close();
			if (rowId == -1) {	//an error occurred when inserting a row
				return answer; 
			}
			try {
				answer.put(STATUS, true);
			} catch (JSONException e) {
				Utils.raiseRunTimeException(TAG, e, "Will never occur");		//TODO doing this is very ugly: just return a boolean, or a enum for more flexibility if necessary 
			}
			Intent getLocationIntent = new Intent(this.context, LocationService.class);
			getLocationIntent.putExtra(LocationService.EXTRA_TIME_OF_REQUEST, System.currentTimeMillis());
			getLocationIntent.putExtra(LocationService.EXTRA_INSERTED_ROW_ID, rowId);
            this.context.startService(getLocationIntent); // We start the service that will add the location to the fillup; eventually...
			break;
		case LOGIN:
			break;
		default:
			Utils.raiseRunTimeException(TAG, new RuntimeException("Unexpected communication type"), "A pure coding mistake exception");
			break;
		}
		return answer;
	}
	
	void uploadFillUp() {
		/*JSONObject answer = new JSONObject();
		HttpService connection = new HttpService(this.context);
		boolean localAnswer = connection.reportFillup(data);
		try {
			answer.put(STATUS, localAnswer);
		} catch (JSONException e) {
			Utils.raiseRunTimeException(TAG, e, "Will never occur");
		}*/
	//and if successful delete the entry from the database, if not add listener to new netwrok connection event or could also try later (20 minutes?)
	}
}
