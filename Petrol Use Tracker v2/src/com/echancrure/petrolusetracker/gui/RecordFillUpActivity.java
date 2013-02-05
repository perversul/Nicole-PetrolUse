package com.echancrure.petrolusetracker.gui;

import com.echancrure.petrolusetracker.R;
import com.echancrure.petrolusetracker.domain.PetrolTracker;
import com.echancrure.petrolusetracker.services.UTCTime;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity used to record a fill up
 * @author Christophe Meudec
 *
 */
public class RecordFillUpActivity extends Activity {
	private final static String TAG = "RecordFillUpActivity";
	/** time formatter */
	private UTCTime uTCTimeFormatter = new UTCTime();
	/** the UTC date and time of the fill up */
	private long uTCDateTime;
	/** the view to display the date and time of the fill up */
	private TextView dateTimeTextView;
	/** the view holding the odometer reading */
	private TextView odometerTextView;
	/** the view holding the price reading */
	private TextView priceTextView;
	/** the view holding the volume reading */
	private TextView volumeTextView;
	/** the odometer value to save */
	private int odometer;
	/** the price to save */
	private int price;
	/** the volume to save */
	private float volume;
	/** the partial indication of the fill up */
	private boolean partial;
	/** the fill up object created */
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_fill_up);
        this.uTCDateTime = System.currentTimeMillis();
        this.dateTimeTextView = (TextView) findViewById(R.id.dateTimeField);
        this.dateTimeTextView.setText(this.uTCTimeFormatter.getString(this.uTCDateTime)); //displays the current date and time in our default format
        this.odometerTextView = (TextView) findViewById(R.id.odometerField);
        this.volumeTextView = (TextView) findViewById(R.id.volumeField);
        this.priceTextView = (TextView) findViewById(R.id.priceField);
    }

    /**
     * handler for all the views on the screen
     * @param view the sender
     */
    public void onClick(View view){
    	Log.d(TAG, "onClick received");
    	switch (view.getId()) { 
    	case R.id.submitButton:
    		handleSubmitButtonOnClick();
    		break;
    	case R.id.cancelButton:
    		this.finish();
    		break;
    	case R.id.cameraButton:
    		Toast.makeText(RecordFillUpActivity.this, "Camera function not yet implemented", Toast.LENGTH_LONG).show();
    		break;
    	default:
    		Log.d(TAG, "ignored click event on:" + view.getId());
    	}
    }
    
    /**
     * handle a fillup submit: check that everything is ok and send to controller
     */
	private void handleSubmitButtonOnClick() {
		PetrolTracker petrolTracker = PetrolTracker.getInstance();
		if (standardiseFields()) {
			boolean success = petrolTracker.reportFillUp(this, uTCDateTime, this.odometer, this.price, this.volume, partial);
			if (success) {
				Toast.makeText(RecordFillUpActivity.this, "Fill Up successfully reported. Thank You.", Toast.LENGTH_LONG).show();
				this.finish();
			} else {
				Log.d(TAG, "unsuccessful API call to report a fill up");
				Toast.makeText(RecordFillUpActivity.this, "A problem occured. Please try again.", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * checks if the fields entered by the user are valid (i.e. present, in the right format and range)
	 * and transform them in default format  
	 * @return whether he operation was successful or not
	 */
	private boolean standardiseFields() {
		final String odometerString = (String) this.odometerTextView.getText().toString();
		final String priceString = (String) this.priceTextView.getText().toString();
		final String volumeString = (String) this.volumeTextView.getText().toString();
		try {
			this.odometer = Integer.parseInt(odometerString);
		} catch (NumberFormatException e) {
			Toast.makeText(RecordFillUpActivity.this, "The odometer field must be valid number", Toast.LENGTH_LONG).show();
			return false;
		}
		try {
			this.price = Integer.parseInt(priceString);
		} catch (NumberFormatException e) {
			Toast.makeText(RecordFillUpActivity.this, "The price field must be valid number, no dot", Toast.LENGTH_LONG).show();
			return false;
		}
		try {
			this.volume = Float.valueOf(volumeString);
		} catch (NumberFormatException e) {
			Toast.makeText(RecordFillUpActivity.this, "The volume field must be valid number", Toast.LENGTH_LONG).show();
			return false;
		}
		RadioButton partialButton = (RadioButton) findViewById(R.id.partialFillUpRadioButton);
		this.partial = partialButton.isChecked();
		return true;
	}
}
