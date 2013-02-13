package com.echancrure.petrolusetracker.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = DatabaseHelper.class.getName();    
	private static final int DATABASE_VERSION = 1;	// If you change the database schema, you must increment the database version.
	private static final String DATABASE_NAME = "PetrolApp.db";

	    public DatabaseHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }
	    
	    /**
	    * Describes the contents of the FillUp database
	    * @author Christophe Meudec
	    */
	   public abstract class FillUpEntry implements BaseColumns {
	   	    public static final String TABLE_NAME = "fillup";
	   	    public static final String DATE_TIME_NAME = "dateTime";
	   		public static final String ODOMETER_NAME = "odometer";
	   		public static final String PRICE_NAME = "price";
	   		public static final String VOLUME_NAME = "volume";
	   		public static final String PARTIAL_NAME = "partial";
	   		public static final String LONGITUDE_NAME = "longitude";
	   		public static final String LATITUDE_NAME = "latitude";
	   }

	    
	    @Override
	    public void onCreate(SQLiteDatabase db) {
	    	db.execSQL("CREATE TABLE IF NOT EXISTS " + FillUpEntry.TABLE_NAME + " ("
                    + FillUpEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FillUpEntry.DATE_TIME_NAME + " INTEGER,"
                    + FillUpEntry.ODOMETER_NAME + " INTEGER,"
                    + FillUpEntry.PRICE_NAME + " INTEGER,"
                    + FillUpEntry.VOLUME_NAME + " REAL,"
                    + FillUpEntry.PARTIAL_NAME + " INTEGER," //Integer because SQLite has no Boolean type
                    + FillUpEntry.LATITUDE_NAME + " REAL DEFAULT -1.0,"
                    + FillUpEntry.LONGITUDE_NAME + " REAL DEFAULT -1.0"
                    + ");");
	    	Log.d(TAG, "Database created");
	    }
	    
	    /** Called when the version number of the database changed. Performs a upgrade to the new version. */
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    	Log.w(TAG, "Upgrading database from Version" + oldVersion + " to Version" + newVersion);
	    	db.execSQL("DROP TABLE IF EXISTS " + FillUpEntry.TABLE_NAME);
            onCreate(db);
	    }		
	}
