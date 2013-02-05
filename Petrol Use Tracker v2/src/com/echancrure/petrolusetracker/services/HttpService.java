package com.echancrure.petrolusetracker.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Offers methods to send and receive HTTP Messages from the cloud app
 */
public class HttpService {
    private final static String TAG = "HttpService";
    
    /** The URL for sending events API */
    private final static String APIFillup = "fillup";
    /** The name of the petrol tracker mapping from the web API */
    private final static String APIPetrolTrackerObject = "petroltracker";
    /** The error number mapping from the web API */
    private final static String APIErrorNum = "errorNum";
    /** The address of the cloud app */
    @SuppressWarnings("unused")
	private final static String CloudAppURL = (false) ? "http://10.0.2.2:8080/petroltracker/" : "http://guest6756.appspot.com/petroltracker/"; //switch to false before deploying!!! //TODO is there a better way?
    //the above is not working: emulator is not able to see GAE development server: firewall on machine is switched off, router firewall?????
    /** Error code for everything is fine */
    private final static int NOERROR = -1;
    /** Error code for insufficient privilege to execute that API request */
    private final static int NOPRIVILEGE = 103;
    Context context;

    static public enum ConnectionAnswer {
        NO_CONNECTION_AT_ALL, USING_WIFI, USING_3G, NO_CONNECTION_3G_AVAILABLE_BUT_NOT_ALLOWED, UNKNOWN_CONNECTION
    }

    public HttpService(Context context) {
        this.context = context;
    }

    /**
     * return our parameterised http client 
     * @return parameterised http client with time outs
     */
    private HttpClient getMyDefaultHttpClient() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 3 * 1000);// Set the timeout in milliseconds until a connection is established.
        HttpConnectionParams.setSoTimeout(httpParameters, 5 * 1000); // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        return httpClient;
    }
    
    /**
     * Use a http connection to make a POST REST request to the cloud app
     * @param fillUp the fillup to upload
     * @return
     */
    public boolean reportFillup(JSONObject fillUp) {
    	ConnectivityManager conMng = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE); // get ConnectivityManager from System to access information over Network information
        NetworkInfo netInfo = conMng.getActiveNetworkInfo();
        //TODO connection checking will have to be more advanced: e.g. are we allowed to use 3G ? => should depend on user preferences...
        if (netInfo == null) { // there is no connection at all
            Message.obtain(this.mToastHandler, 0, "Petrol cloud app could not be reached, no internet connection").sendToTarget();
            return false;
        } else { // there is a connection
        	HttpPost httpPost = null;
            HttpResponse response = null;
            HttpClient httpClient = getMyDefaultHttpClient();
            final String toSend = fillUp.toString();
            Log.d(TAG, "Trying to send fillup: " + toSend);
            try {
            	httpPost = new HttpPost(HttpService.CloudAppURL+HttpService.APIFillup);
                try {
                	httpPost.setEntity(new StringEntity(toSend, HTTP.UTF_8));
                } catch (OutOfMemoryError e) {
                	Message.obtain(this.mToastHandler, 0, "Out of Memory error while sending a fill up; Try again later.").sendToTarget();
                    return false;
                }
                response = httpClient.execute(httpPost); // executes request and stores response
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 8 * 1024); // Get the data of the response
                String resString = "";
                String line = "";
                while ((line = rd.readLine()) != null) {
                    resString = resString.concat(line);
                }
                int status = response.getStatusLine().getStatusCode(); // check HTTP status code
                if (status == 200) {//TODO constant
                    Log.d(TAG, "fill up transmission successful" + resString);
                    JSONObject petrolTrackerJSONAnswerObj = new JSONObject();
                    try {
                    	JSONObject rawEventJSONAnswerObj = new JSONObject(resString);
                        petrolTrackerJSONAnswerObj = rawEventJSONAnswerObj.getJSONObject(APIPetrolTrackerObject);
                    } catch (JSONException e) {
                        Log.d(TAG, "Unexpected answer: " + resString);
                        Message.obtain(this.mToastHandler, 0, "Petrol cloud app could not be reached, unexpected answer while trying to upload a fillup.").sendToTarget();
                        return false;
                    }
                    int errorNum = petrolTrackerJSONAnswerObj.optInt(APIErrorNum, -1);
                    if (errorNum == NOERROR) {
                        Log.d(TAG, "No errors sending the fillup");
                        return true;
                    } else {
                        String failureCause = null;
                        switch (errorNum) {
                            case NOPRIVILEGE:
                                failureCause = "insufficient privilege to execute that API request";
                                break;
                            default:
                                String errorDesc = petrolTrackerJSONAnswerObj.optString("errorDesc", "unknown");
                                failureCause = "error number " + errorNum + " (" + errorDesc + ")";
                                break;
                        }
                        Message.obtain(this.mToastHandler, 0, failureCause).sendToTarget();
                        return false;
                    }
                } else { // returns null by default
                	Message.obtain(this.mToastHandler, 0, "Petrol Tracker cannot connect to the web app to upload a fillup; Status error returned:" + status).sendToTarget();
                    Log.d(TAG, "Transmission failed " + resString);
                    return false;
                }
            } catch (InterruptedIOException e) {
                Message.obtain(this.mToastHandler, 0, "Petrol Tracker cannot connect to the web app to upload a fillup, the connection timed out.").sendToTarget();
                return false;
            } catch (UnknownHostException e) {
                Message.obtain(this.mToastHandler, 0, "Petrol Tracker cannot connect to the web app to upload a fillup, it is offline.").sendToTarget();
                return false;
            } catch (Exception e) {
                Message.obtain(this.mToastHandler, 0, "Petrol Tracker cannot connect to the web app to upload a fillup, due to:" + e.toString()).sendToTarget();
                return false;
            }
        }
        }

    public String getStockPrice() {
    	ConnectivityManager conMng = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE); // get ConnectivityManager from System to access information over Network information
        NetworkInfo netInfo = conMng.getActiveNetworkInfo();
        if (netInfo == null) { // there is no connection at all
            Log.d(TAG, "No connection at all");
            return "no Connection";
        } else { // there is a connection
    	String returnValue = "";
    	String getStockPrice_URL = CloudAppURL + "stockPrices?q=";
    	HttpClient httpClient = getMyDefaultHttpClient();
    	HttpGet httpget = new HttpGet(getStockPrice_URL + "ECHANCRURE");
    	HttpResponse response;
        try {
            response = httpClient.execute(httpget);
            BufferedReader rd;
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 8 * 1024);
            String resString = "";
            String line = "";
            while ((line = rd.readLine()) != null) {
                resString = resString.concat(line);
            }
            int status = response.getStatusLine().getStatusCode(); // check HTTP status code
            if (status == 200) {
                Log.d(TAG, "Get stocks HTTP transmission successful" + resString);
                new JSONObject();
                new JSONObject();
                JSONArray JSONStockArray = null;
                try {
                	JSONStockArray = new JSONArray(resString);
                } catch (JSONException e) {
                    Log.d(TAG, "Unexpected answer: " + resString);
                    Message.obtain(this.mToastHandler, 0, "StockWatcher web app could not be reached, unexpected answer while retrieving stocks.").sendToTarget();
                    return null;
                }
                if (JSONStockArray != null) {
                        String stockName;
                        double stockPrice;
                        double stockChange;
                        new ArrayList<Integer>();
                        this.context.getContentResolver();
                        for (int i = 0; i < JSONStockArray.length()-1; i++)
                        {
                            try {
                                JSONObject JSONStock = JSONStockArray.getJSONObject(i);
                                stockName = JSONStock.getString("symbol");
                                stockPrice = JSONStock.getDouble("price");
                                stockChange = JSONStock.getDouble("change");
                            } catch (JSONException e) {
                                Log.d(TAG, "Unexpected stock: " + i);
                                Message.obtain(this.mToastHandler, 0, "Unexpected answer while retrieving a stock.").sendToTarget();
                                return null;
                            }
                            returnValue += "Stock: " + stockName + " is at " + stockPrice + " with a change of " + stockChange + "\n";
                        }
                }
                return returnValue; 
            } else {
            	Log.d(TAG, "HTTP error " + status);
                Message.obtain(this.mToastHandler, 0, "StockWatcher web app could not be reached due to a network error: " + status).sendToTarget();
                return null;
            }
        } catch (InterruptedIOException e) {
            Message.obtain(this.mToastHandler, 0, "TaggMobile cannot connect to the web app to retrieve the projects, the connection timed out.").sendToTarget();
            return null;
        } catch (UnknownHostException e) {
            Message.obtain(this.mToastHandler, 0, "TaggMobile cannot connect to the web app to retrieve the projects, it is offline.").sendToTarget();
            return null;
        } catch (Exception e) {
            Message.obtain(this.mToastHandler, 0, "TaggMobile cannot connect to the web app to retrieve the projects, due to:" + e.toString()).sendToTarget();
            return null;
        }
        }
    }

    Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, (String) msg.obj);
            Toast.makeText(HttpService.this.context, (String) msg.obj, Toast.LENGTH_LONG).show();
        }
    };

	
}
