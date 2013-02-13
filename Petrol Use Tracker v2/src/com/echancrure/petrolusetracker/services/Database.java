package com.echancrure.petrolusetracker.services;

import com.echancrure.petrolusetracker.domain.FillUp;
import com.echancrure.petrolusetracker.services.DatabaseHelper.FillUpEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public  class Database {
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

	public long insertFillUp(FillUp fillUp) {
	    ContentValues values = new ContentValues();
	    values.put(FillUpEntry.DATE_TIME_NAME, fillUp.getDateTime());
	    values.put(FillUpEntry.ODOMETER_NAME, fillUp.getOdometer());
	    values.put(FillUpEntry.PARTIAL_NAME, fillUp.isPartial());
	    values.put(FillUpEntry.PRICE_NAME, fillUp.getPrice());
	    values.put(FillUpEntry.VOLUME_NAME, fillUp.getVolume());
	    long rowId = database.insert(FillUpEntry.TABLE_NAME, null, values);
	    return rowId;
	  }

	  public void deleteComment(Comment comment) {
	    long id = comment.getId();
	    System.out.println("Comment deleted with id: " + id);
	    database.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID
	        + " = " + id, null);
	  }

}
