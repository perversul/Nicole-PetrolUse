package com.echancrure.petrolusetracker.gui;

import com.echancrure.petrolusetracker.R;
import com.echancrure.petrolusetracker.services.HttpService;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TestActivity  extends Activity { 
	private final static String TAG = "TestActivity";
	private TextView textbox = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textbox = (TextView) findViewById(R.id.mainDisplay);
        
    }
  
	public void onClick(View view){
    	Log.d(TAG, "onClick received");
    	switch (view.getId()) { 
    	case R.id.getButton:
    		handleGetButtonOnClick();
    		break;
    	default:
    		Log.d(TAG, "ignored click event on:" + view.getId());		
    	}
	}

	private void handleGetButtonOnClick() {
		HttpService connection = new HttpService(getBaseContext());
		String stockPrices = connection.getStockPrice();
		if (stockPrices != null) textbox.setText(stockPrices);
		else textbox.setText("nothing");
	}
}
