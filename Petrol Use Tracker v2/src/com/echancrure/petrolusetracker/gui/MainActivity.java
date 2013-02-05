package com.echancrure.petrolusetracker.gui;

import com.echancrure.petrolusetracker.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

/**
 * First Activity seen by the user: list the main options Record Fill Up, Change Vehicle etc.
 * @author Christophe Meudec
 */
public class MainActivity extends Activity {
	private final static String TAG = "MainActivity";


    /**
     * call on creation of the activity: entry point
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    /**
     * Listener attached to the buttons on the main screen
     * @param view
     */
    public void onClick(View view){
    	Log.d(TAG, "onClick received");
    	Intent newActivity = null;
    	switch (view.getId()) {	//depending on the button pressed the correct activity is launched 
    	case R.id.recordFillUpButton:
    		newActivity = new Intent(getApplicationContext(), RecordFillUpActivity.class);
    		break;
    	case R.id.testButton:
    		newActivity = new Intent(getApplicationContext(), TestActivity.class); 
    		break;
    	default:
    		Log.d(TAG, "ignored click event on:" + view.getId());
    	}
    	if (newActivity != null) startActivity(newActivity);
    }
}
