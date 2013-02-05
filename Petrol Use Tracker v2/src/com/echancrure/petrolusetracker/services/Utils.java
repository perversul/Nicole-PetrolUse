package com.echancrure.petrolusetracker.services;

import android.util.Log;

public class Utils {
	/**
     * Raises a RuntimeException from an existing exception and logs it.
     * Only use when a suspected programming error occurs otherwise the original exception should be handled
     * @param TAG the class' TAG
     * @param e the original exception
     * @param msg to help localising the exception
     */
    public static void raiseRunTimeException(String localTAG, Throwable e, String msg) throws RuntimeException {
        Log.d(localTAG, msg + " with message: " + e.getMessage());
        throw new RuntimeException(msg, e); 
    }
}
