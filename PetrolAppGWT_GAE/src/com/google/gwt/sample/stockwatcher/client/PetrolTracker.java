package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import java.util.ArrayList;
import java.util.Date;

public class PetrolTracker implements EntryPoint {

  private static final int REFRESH_INTERVAL = 5000; // ms
  private VerticalPanel mainPanel = new VerticalPanel();
  private FlexTable fillupFlexTable = new FlexTable();
  private HorizontalPanel addPanel = new HorizontalPanel();
  private Button refreshButton = new Button("Refresh");
  private Label lastUpdatedLabel = new Label();
  private ArrayList<String> stocks = new ArrayList<String>();
  private static final String JSON_URL = GWT.getModuleBaseURL() + "fillup";
  private Label errorMsgLabel = new Label();

  /**
   * Entry point method.
   */
  public void onModuleLoad() {
    // Create table for fillup data.
    fillupFlexTable.setText(0, 0, "Date and Time");
    fillupFlexTable.setText(0, 1, "Odometer");
    fillupFlexTable.setText(0, 2, "Volume");
    fillupFlexTable.setText(0, 3, "Price");
    fillupFlexTable.setText(0, 4, "Full Tank?");

    // Add styles to elements in the stock list table.
    fillupFlexTable.setCellPadding(6);
    fillupFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
    fillupFlexTable.addStyleName("watchList");
    fillupFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
    fillupFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
    fillupFlexTable.getCellFormatter().addStyleName(0, 3, "watchListNumericColumn");
    fillupFlexTable.getCellFormatter().addStyleName(0, 4, "watchListNumericColumn");

    // Assemble Add Stock panel.
    addPanel.add(refreshButton);
    addPanel.addStyleName("addPanel");

    // Assemble Main panel.
    errorMsgLabel.setStyleName("errorMessage");
    errorMsgLabel.setVisible(false);
    mainPanel.add(errorMsgLabel);
    mainPanel.add(fillupFlexTable);
    mainPanel.add(addPanel);
    mainPanel.add(lastUpdatedLabel);

    // Associate the Main panel with the HTML host page.
    RootPanel.get("stockList").add(mainPanel);

    // Move cursor focus to the refresh button.
    refreshButton.setFocus(true);

    // Listen for mouse events on the Refresh button.
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        refreshFillups();
      }
    });

  }

  /**
   * Add stock to FlexTable. Executed when the user clicks the addStockButton or
   * presses enter in the newSymbolTextBox.
   
  private void addFillups() {
    final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
    newSymbolTextBox.setFocus(true);

    // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
    if (!symbol.matches("^[0-9a-zA-Z\\.]{1,10}$")) {
      Window.alert("'" + symbol + "' is not a valid symbol.");
      newSymbolTextBox.selectAll();
      return;
    }

    newSymbolTextBox.setText("");

    // Don't add the stock if it's already in the table.
    if (stocks.contains(symbol))
      return;

    // Add the stock to the table.
    int row = fillupFlexTable.getRowCount();
    stocks.add(symbol);
    fillupFlexTable.setText(row, 0, symbol);
    fillupFlexTable.setWidget(row, 2, new Label());
    fillupFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
    fillupFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
    fillupFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");

    // Add a button to remove this stock from the table.
    Button removeStockButton = new Button("x");
    removeStockButton.addStyleDependentName("remove");
    removeStockButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        int removedIndex = stocks.indexOf(symbol);
        stocks.remove(removedIndex);
        fillupFlexTable.removeRow(removedIndex + 1);
      }
    });
    fillupFlexTable.setWidget(row, 3, removeStockButton);

    // Get the stock price.
    refreshWatchList();

  }
*/
  private void refreshFillups() {
	    String url = JSON_URL;

	    final String encodedUrl = URL.encode(url);
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, encodedUrl);
	    try {
	      @SuppressWarnings("unused")
		Request request = builder.sendRequest(null, new RequestCallback() {
	        public void onError(Request request, Throwable exception) {
	          displayError("Couldn't retrieve JSON on url:" + encodedUrl);
	        }

	        public void onResponseReceived(Request request, Response response) {
	          if (200 == response.getStatusCode()) {
	        	  String jsonString = response.getText();
	            updateTable(asArrayOfFillupData(jsonString));
	          } else {
	            displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
	          }
	        }
	      });
	    } catch (RequestException e) {
	      displayError("Couldn't retrieve JSON");
	    }

	  }
  /**
   * If can't get JSON, display error message.
   * @param error
   */
  private void displayError(String error) {
    errorMsgLabel.setText("Error: " + error);
    errorMsgLabel.setVisible(true);
  }

/**
   * Update the Price and Change fields for all rows in the stock table.
   *
   * @param fillups fillup data for all rows.
   */
  private void updateTable(JsArray<Fillup> fillups) {
    for (int i = 0; i < fillups.length(); i++) {
      updateTable(fillups.get(i), i+1);
    }

    // Display timestamp showing last refresh.
    lastUpdatedLabel.setText("Last update : " + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
    
    // Clear any errors.
    errorMsgLabel.setVisible(false);
  }

  /**
   * Update a single row in the fillup table.
   *
   * @param fillup Stock data for a single row.
   */
  private void updateTable(Fillup fillup, int row) {
	  fillupFlexTable.setText(row, 0, fillup.getDateTime());
	  fillupFlexTable.setText(row, 1, NumberFormat.getFormat("#,##0.00").format(fillup.getOdometer()));
	    fillupFlexTable.setText(row, 2, NumberFormat.getFormat("#,##0.00").format(fillup.getVolume()));
	    fillupFlexTable.setText(row, 3, NumberFormat.getFormat("#,##0.00").format(fillup.getPrice()));
	  boolean partial = fillup.getPartial(); 
	    fillupFlexTable.setText(row, 4, (partial)?"no":"yes");
  }
  
  /**
   * Convert the string of JSON into JavaScript object.
   */
  private final native JsArray<Fillup> asArrayOfFillupData(String json) /*-{
    return eval(json);
  }-*/;

}
