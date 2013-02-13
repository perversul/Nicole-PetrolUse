package com.echancrure.petrolusetracker.services;

import org.json.JSONException;
import org.json.JSONObject;

import com.echancrure.petrolusetracker.domain.FillUp;

import android.content.Context;
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
			answer.put(STATUS, false);
		} catch (JSONException e) {
			Utils.raiseRunTimeException(TAG, e, "Will never occur");
		}
		switch (type) {
		case REPORT_FILLUP:
			Database db = new Database(this.context);
			db.open();
			db.insertFillUp(fillUp);
			/*
			HttpService connection = new HttpService(this.context);
			boolean localAnswer = connection.reportFillup(data);
			try {
				answer.put(STATUS, localAnswer);
			} catch (JSONException e) {
				Utils.raiseRunTimeException(TAG, e, "Will never occur");
			}*/
			break;
		case LOGIN:
			break;
		default:
			Utils.raiseRunTimeException(TAG, new RuntimeException("Unexpected communication type"), "A pure coding mistake exception");
			break;
		}
		return answer;
	}
}
