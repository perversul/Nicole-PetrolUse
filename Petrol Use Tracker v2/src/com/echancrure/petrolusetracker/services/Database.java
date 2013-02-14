package com.echancrure.petrolusetracker.services;

import com.echancrure.petrolusetracker.domain.FillUp;
import com.echancrure.petrolusetracker.services.DatabaseHelper.FillUpEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public  class Database {
	private static final String TAG = "Database";
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	
	public Database(Context context) {
	    dbHelper = new DatabaseHelper(context);
	}

	public void open() throws SQLException {
	   database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}

	/**
	 * insert a new fillup into our local database
	 * @param fillUp the fillup to insert into the database
	 * @return the rowId of the netry inserted or -1 in case of error
	 */
	public long insertFillUp(FillUp fillUp) {
	    ContentValues values = new ContentValues();
	    values.put(FillUpEntry.DATE_TIME_NAME, fillUp.getDateTime());
	    values.put(FillUpEntry.ODOMETER_NAME, fillUp.getOdometer());
	    values.put(FillUpEntry.PARTIAL_NAME, fillUp.isPartial());
	    values.put(FillUpEntry.PRICE_NAME, fillUp.getPrice());
	    values.put(FillUpEntry.VOLUME_NAME, fillUp.getVolume());
	    long rowId = database.insert(FillUpEntry.TABLE_NAME, null, values);
	    Log.d(TAG, "New fillUp inserted with rowId:" + rowId);
	    if (rowId == -1) Log.d(TAG, "Inserting a fillup failed. DateTime of the fill up is:" + fillUp.getDateTime());
	    return rowId;
	  }
	
	public void updateFillUp(long rowId, double latitude, double longitude) {
		//TODO
	}

	  public void deleteFillup(long rowId) {   
	    database.delete(FillUpEntry.TABLE_NAME, FillUpEntry._ID + " = " + rowId, null);
	  }

}
