package com.echancrure.petrolusetracker.services;

import com.echancrure.petrolusetracker.services.DatabaseHelper.FillUpEntry;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

/**
 * Attempt to retrieve a location: this may be an old location if it is not out of date or a fresh location
 */
public class LocationService extends IntentService {
    private static final String TAG = "LocationService";
    /** The lookup key for the time the intent was requested */
    public static final String EXTRA_TIME_OF_REQUEST = "time_of_request";
    /** The lookup key for row id to update */
    public static final String EXTRA_INSERTED_ROW_ID = "row_id";
    /** The minimum accuracy required in meters */
    public static final int MINIMUM_ACCURACY = 50;
    /** The threshold beyond which accuracy is sufficient and the location is accepted */
    public static final int ACCEPTABLE_ACCURACY_THRESHOLD = 25;
    /** Time to define if an old location is not too old to be compared to the new one. */
    public static final int TWO_MINUTES = 1000 * 60 * 2;
    /** The minimum time (in millis) between location notifications from a location provider. */
    private static final int TIME_INTERVAL_GPS = 1000 * 15; // 15 seconds
    /** The maximum time (in millis) for reusing an old location update. */
    public static final int FIVE_MINUTES = 1000 * 5 * 60;
    /** the row in the fillup table to update  */
    private int rowId = -1;
    /** the time at which this location service was requested */
    private long timeOfRequest;
    private LocationManager locationManager = null;
    Location currentBestLocation = null;
    /** Used to identify which notification should be displayed */
    private static final int NOTE_ID = 1111;

    public LocationService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        if (intent != null) {
            this.timeOfRequest = intent.getLongExtra(LocationService.EXTRA_TIME_OF_REQUEST, -1);
            this.rowId = intent.getIntExtra(LocationService.EXTRA_INSERTED_ROW_ID, -1);
        } else {
            throw new IllegalArgumentException(TAG + ": in onHandleIntent the intent is null");
        }
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                 && !this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // NO PROVIDER is available for requesting update: a notification is created that informs the user about the failed location update
            Log.d(LocationService.TAG, "No location provider activated on the phone");
            NotificationManager noteMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            @SuppressWarnings("deprecation")
			Notification info_message = new Notification(android.R.drawable.stat_notify_error, "Geo-location is disabled", System.currentTimeMillis()); //TODO see the new way of doing things ad remove deprecation
            PendingIntent infoIntent = PendingIntent.getActivity(getApplicationContext(),
                    0,
                    new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), // start the phone's settings if clicked (location part)
                    0);
            info_message.setLatestEventInfo(getApplicationContext(), "Geo-Location is disabled", "Change your phone's location parameters", infoIntent);
            info_message.flags |= Notification.FLAG_AUTO_CANCEL;// deletes the notification automatically when it is clicked
            noteMgr.notify(NOTE_ID, info_message);
        } else {
            Log.d(LocationService.TAG, "At least one location provider activated on the phone");
            Location oldLoc = getLastGoodKnownLocation();
            if (oldLoc != null) { // we use the previous last good known location found that is not too old
                updateFillUpWithLocation(oldLoc);
            } else {// we really need a new location
                Log.d(TAG, "launching get new location thread");
                LocationThread getNewLocationThread = new LocationThread(this.locationManager);
                getNewLocationThread.start();
                try {
                    getNewLocationThread.join(FIVE_MINUTES);// 5 minutes here but the thread will finish after two so we will be out of here within 2 minutes at the latest
                } catch (InterruptedException e) {
                    // nothing
                }
                Log.d(TAG, "join finished: the location thread should be terminated, isAlive should be false:" + getNewLocationThread.isAlive());
            }
        }
        //We upload the fillup to the cloud whether or not a geo-location has been found
        Database db = new Database(getApplicationContext());
		db.openForQuery();
		Cursor cur = db.queryFillUp(this.rowId);
		int dateTime = cur.getInt(cur.getColumnIndex(FillUpEntry.DATE_TIME_NAME));
		//TODO continue retrieving all the elements of the fillup, build a json object and upload to the cloud
		cur.close();
		db.close();
		//upload to the cloud app
		//and if successful delete the entry from the database, if not add listener to new netwrok connection event or could also try later (20 minutes?)
    }

    /**
     * Looks for a previous Last Good Known GPS or Network location that is not too old for us 
     * @return the previous location or null if none can be found or if it is too old
     */
    public Location getLastGoodKnownLocation() {
        Location prevLoc = null, prevLocGps = null, prevLocNetwork = null;
        // Acquire a reference to the system Location Manager
        if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            prevLocGps = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (prevLocGps != null && (Math.abs(this.timeOfRequest - prevLocGps.getTime()) > FIVE_MINUTES // it is too old
                    || getSanitizedAccuracy(prevLocGps) > MINIMUM_ACCURACY) // it is not accurate enough
            ) prevLocGps = null; // it is discarded
        }
        if (this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            prevLocNetwork = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (prevLocNetwork != null && (Math.abs(this.timeOfRequest - prevLocNetwork.getTime()) > FIVE_MINUTES // it is too old
                    || getSanitizedAccuracy(prevLocNetwork) > MINIMUM_ACCURACY) // it is not accurate enough
            ) prevLocNetwork = null; // it is discarded
        }
        if (prevLocGps != null && prevLocNetwork != null) { // Keep the GPS update if less than 5 minutes old, else keep the network update if more recent
            // START TEST CODE Everything here is just for testing purposes (except the return statement)
            long oldFixLongUTC = prevLocGps.getTime();
            long newTimeLongUTC = System.currentTimeMillis();
            long duration = (newTimeLongUTC - oldFixLongUTC) / 1000; // The duration in seconds
            String timeToPrint;
            if (duration > 59) {
                timeToPrint = duration / 60 + "mins, " + duration % 60 + "secs.";
            } else timeToPrint = duration + "secs.";
            Log.d(LocationService.TAG, "Time since the last fix (" + prevLocGps.getProvider() + "): " + timeToPrint);
            oldFixLongUTC = prevLocNetwork.getTime();
            newTimeLongUTC = System.currentTimeMillis();
            duration = (newTimeLongUTC - oldFixLongUTC) / 1000; // The duration in seconds
            if (duration > 59) {
                timeToPrint = duration / 60 + "mins, " + duration % 60 + "secs.";
            } else timeToPrint = duration + "secs.";
            Log.d(LocationService.TAG, "Time since the last fix (" + prevLocNetwork.getProvider() + "): " + timeToPrint);
            // END TEST CODE

            if (getSanitizedAccuracy(prevLocGps) < getSanitizedAccuracy(prevLocNetwork)) {
                prevLoc = prevLocGps;
            } else if (getSanitizedAccuracy(prevLocNetwork) < getSanitizedAccuracy(prevLocGps))
                prevLoc = prevLocNetwork;
        } else if (prevLocGps != null) prevLoc = prevLocGps;
        else if (prevLocNetwork != null) prevLoc = prevLocNetwork;
        if (prevLoc != null) {
            final long locationTimeUTC = prevLoc.getTime();
            Log.d(LocationService.TAG, "Found a, " + (this.timeOfRequest - locationTimeUTC) + " milliseconds old previous location with an accuracy of " + getSanitizedAccuracy(prevLoc) + " meters: we reuse it");
            return prevLoc;
        }
        return null; // previous location does could not be retrieved or is too old or is not accurate
    }

    /**
     * Called when we have found a location: can be a previous last good known or a fresh one
     * The row of the fillup concerned is updated
     * @param location The new location retrieved
     */
    protected void updateFillUpWithLocation(Location location) {
        if (location != null) {
            String providerString = location.getProvider();
            String providerToDisplay = "unknown";
            if (providerString != null) {
                providerToDisplay = providerString + "(" + getSanitizedAccuracy(location) + " meters)";
            }
            if (this.rowId != -1) { // we update the fillup
                Log.d(TAG, "Will update fillup row: " + this.rowId + " with this provider " + providerToDisplay);
                Database db = new Database(getApplicationContext());
    			db.open();
    			db.updateFillUp(this.rowId, location.getLatitude(), location.getLongitude());
    			db.close();
            }
        }
    }

    /**
     * Thread that keeps asking for a location from the location manager
     */
    private class LocationThread extends Thread implements LocationListener {
        private static final String _THREAD_TAG = "LocationThread";
        private LocationManager threadLocationManager = null;
        private long threadListeningStartTimeUTC = 0;

        public LocationThread(LocationManager locationManager) {
            super("UploaderService-Uploader");
            this.threadLocationManager = locationManager;
        }

        @Override
        public void run() {
            Log.d(LocationThread._THREAD_TAG, "Thread.run");
            this.threadListeningStartTimeUTC = System.currentTimeMillis();
            Looper.prepare();
            if (this.threadLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                this.threadLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            if (this.threadLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                this.threadLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_INTERVAL_GPS, 0, this);
            Looper.loop(); // the above statements are executed and the thread handles the generated messages in a loop until quit is called
            Log.d(LocationThread._THREAD_TAG, "after loop()");
        }

        public void onThreadStop() {
            Log.d(LocationThread._THREAD_TAG, "Should stop thread execution here !");
            this.threadLocationManager.removeUpdates(this); // we do not listen anymore to location updates
            if (LocationService.this.currentBestLocation != null) {
                updateFillUpWithLocation(LocationService.this.currentBestLocation);
            }
            Looper.myLooper().quit();
            Log.d(LocationThread._THREAD_TAG, "quit has been called");
        }

        public void onLocationChanged(Location newLocation) { // Called when a new location is found by the network location provider.
            boolean stop = false;
            if ((System.currentTimeMillis() - this.threadListeningStartTimeUTC) > TWO_MINUTES) stop = true; // we stop after 2 minutes of listening
            Log.d(LocationThread._THREAD_TAG, "Location changed !");
            if (newLocation != null) {
                Log.d(LocationThread._THREAD_TAG, "A " + newLocation.getProvider().toUpperCase() + " location has been found with an accuracy of " + getSanitizedAccuracy(newLocation) + " meters");
                if (isBetterLocation(newLocation, LocationService.this.currentBestLocation)) {
                    Log.d(LocationThread._THREAD_TAG, "New location is better than current best");
                    LocationService.this.currentBestLocation = newLocation;
                    if (ACCEPTABLE_ACCURACY_THRESHOLD > getSanitizedAccuracy(newLocation)) stop = true;
                } else Log.d(LocationThread._THREAD_TAG, "New location is NOT better than current best or is not accurate enough: we keep listening for updates");
            }
            if (stop) onThreadStop();
        }

        public void onProviderDisabled(String arg0) {
            onThreadStop();
        }

        public void onProviderEnabled(String arg0) {
            // Do nothing
        }

        public void onStatusChanged(String provider, int status, Bundle arg2) {
            boolean stop = false;
            if ((System.currentTimeMillis() - this.threadListeningStartTimeUTC) > TWO_MINUTES) stop = true; // we stop after 2 minutes of listening
            if (status == LocationProvider.OUT_OF_SERVICE) stop = true;
            if (stop) onThreadStop();
        }

        /** Determines whether one Location reading is better than the current Location fix
         * @param newLocation  The new Location that you want to evaluate
         * @param currentBestLocation1  The current Location fix, to which you want to compare the new one
         */
        protected boolean isBetterLocation(Location newLocation, Location currentBestLocation1) {
            if (MINIMUM_ACCURACY <= getSanitizedAccuracy(newLocation)) { // we are not interested in locations with poor accuracy
                return false;
            }
            if (currentBestLocation1 == null) { // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = newLocation.getTime() - currentBestLocation1.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
            } else if (isSignificantlyOlder) {// If the new location is more than two minutes older, it must be worse
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (getSanitizedAccuracy(newLocation) - getSanitizedAccuracy(currentBestLocation1));
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > MINIMUM_ACCURACY / 2;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentBestLocation1.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
            return false;
        }

        /** Checks whether two providers are the same */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying service...");
    }

    /**
     * Returns the accuracy of a location taking into account that locations with no accuracy 
     * return a default value of 0.0 (which is pretty dangerous for our purpose!!)
     * @param loc the location 
     * @return the accuracy in meters of the location
     */
    static public float getSanitizedAccuracy(Location loc) {
        if (loc.hasAccuracy()) return loc.getAccuracy();
        return 20000 * 1000; // the location we got if of unknown accuracy we give it a value of half the circumference of the earth ... which is pretty useless, on earth at least
    }
}