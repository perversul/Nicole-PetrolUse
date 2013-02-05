package com.google.gwt.sample.stockwatcher.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.gwt.sample.stockwatcher.server.Fillup;
import com.google.gwt.sample.stockwatcher.server.Util;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Services fillup requests Answer is in the format: { APIPetrolTrackerObject : { APIErrorNum : <int> } } as in {"petroltracker" : { "errorNum", "-1" } }
 * 
 * @author Christophe Meudec
 * 
 */
@SuppressWarnings("serial")
public class FillupServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(FillupServlet.class.getName());
	/** The name of the petrol tracker mapping from the web API */
	public final static String APIPetrolTrackerObject = "petroltracker";
	/** The error number mapping from the web API */
	private final static String APIErrorNum = "errorNum";
	/** Error code for everything is fine */
	private final static int NOERROR = -1;
	/** Error code for insufficient privilege to execute that API request */
	private final static int NOPRIVILEGE = 103;

	// TODO the above should be shared java class between clients and server
	// TODO most of constants above should be factored out in a shared class
	// between the cloud and the client apps

	// TODO what's the best way of handling exceptions in Google App engine?
	/**
	 * POST a new fillup to be stored
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int errorNum = NOERROR;
		PrintWriter out = resp.getWriter();
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = req.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			String msg = Util.getErrorMessage(e);
			out.print(msg);
		}
		String received = jb.toString();
		log.info("POST fillup request received:" + received);
		JSONObject jsonFillup = null;
		try {
			jsonFillup = new JSONObject(received);
		} catch (JSONException e) {
			String msg = Util.getErrorMessage(e);
			out.print(msg);
		}
		// TODO check privilege
		try {
			long dateTime = jsonFillup.getLong(Fillup.DATE_TIME_NAME);
			int odometer = jsonFillup.getInt(Fillup.ODOMETER_NAME);
			int price = jsonFillup.getInt(Fillup.PRICE_NAME);
			double volume = jsonFillup.getDouble(Fillup.VOLUME_NAME);
			boolean partial = jsonFillup.getBoolean(Fillup.PARTIAL_NAME);
			Fillup.create(dateTime, odometer, price, volume, partial);	//store to GAE datastore
		} catch (Exception e) {
			String msg = Util.getErrorMessage(e);
			out.print(msg);
		}
		JSONObject answerJsonObject = null;
		try {
			answerJsonObject = new JSONObject().put(APIPetrolTrackerObject, new JSONObject().put(APIErrorNum, errorNum));
		} catch (JSONException e) {
			String msg = Util.getErrorMessage(e);
			out.print(msg);
		}
		out.write(answerJsonObject.toString());
		out.flush();
	}

	/**
	 * Get all the fillups
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("Obtaining fillup listing");
		PrintWriter out = resp.getWriter();
		Iterable<Entity> entities = null;
		entities = Fillup.getAllFillups();
		String output = Util.writeJSON(entities);
		log.info("returned: "+ output);
		out.println(output);
	}
}
