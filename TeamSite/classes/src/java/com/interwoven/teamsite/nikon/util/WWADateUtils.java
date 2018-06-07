package com.interwoven.teamsite.nikon.util;

import java.util.Calendar;

/**
 * This class is responsible for WWA Date releated Java code
 * 
 * We can override it if the WWA Date is to come from other source
 * than the Server time
 * 
 * @author nbamford
 *
 */
public class WWADateUtils {

	
	/**
	 * Returns the current time in millis from the sever it is run from
	 * @return Current time in ms as long 
	 */
	public static long getCurrentWWATime()
	{
		Calendar cal = Calendar.getInstance();
		return cal.getTimeInMillis();
	}
}
