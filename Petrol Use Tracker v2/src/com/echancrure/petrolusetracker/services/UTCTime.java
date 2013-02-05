package com.echancrure.petrolusetracker.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Time formatting services
 * @author Christophe Meudec
 */
public class UTCTime {
    /** Our default time string format */
    private SimpleDateFormat timeAndDateWebFormatUTC = null;
    /** The default formating string */
    static final private String DISPLAY_DATE_FORMAT_NO_SECONDS = "HH:mm - dd-MM-yyyy";


    /**
     * Constructor ensures we are using UTC time and default string format
     */
    public UTCTime() {
        this.timeAndDateWebFormatUTC = new SimpleDateFormat(DISPLAY_DATE_FORMAT_NO_SECONDS);
        this.timeAndDateWebFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * transforms a long UTC time in milliseconds to our default string format
     * @param longTimeUTC the UNIX UTC time in milliseconds
     * @return the string representing the same time 
     */
    public String getString(long longTimeUTC) {
        return this.timeAndDateWebFormatUTC.format(new Date(longTimeUTC));
    }
}
