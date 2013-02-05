package com.google.gwt.sample.stockwatcher.client;

import com.google.gwt.core.client.JavaScriptObject;

class Fillup extends JavaScriptObject {                              // (1)
  // Overlay types always have protected, zero argument constructors.
  protected Fillup() {}                                              // (2)

  // JSNI methods to get fillup data
  public final native String getDateTime() /*-{ return this.dateTime; }-*/;
  public final native int getOdometer() /*-{ return this.odometer; }-*/;
  public final native int getPrice() /*-{ return this.price; }-*/;
  public final native double getVolume() /*-{ return this.volume; }-*/;
  public final native boolean getPartial() /*-{ return this.partial; }-*/;

}
