package com.interwoven.teamsite.ext.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.dto.ProductDTO;

/**
 * @author nbamford
 *
 * Class to hold useful formatting methods
 */

public class FormatUtils
{

	static Map<String, Locale> ccLcMap = new HashMap<String, Locale>();

	static
	{
		ccLcMap.put("frCH", Locale.FRENCH);
		ccLcMap.put("deCH", Locale.GERMAN);
		ccLcMap.put("itCH", Locale.ITALIAN);
		ccLcMap.put("enGB", Locale.ENGLISH);
	}

	/**
	 * Format a language country code in the format lc_CC
	 * @param languageCode
	 * @param countryCode
	 * @return languageCode_countryCode
	 */
	public static String languageCountryCode(String languageCode, String countryCode)
	{
		return mFormat("{0}_{1}", languageCode, countryCode);
	}

	/**
	 * Method to localise currency format
	 * @param languageCode
	 * @param countryCode
	 * @return String
	 */
	public static NumberFormat getNF4CountryCode(String languageCode, String countryCode) {
		return ccLcMap.get(languageCode+countryCode)!=null?NumberFormat.getInstance(ccLcMap.get(languageCode+countryCode)):NumberFormat.getInstance();
	}

	/**
	 * Method to localise currency format
	 * @param param
	 * @return String
	 */
	public static NumberFormat getNF4CountryCode(HBN8QueryParamDTO param) { return getNF4CountryCode(param.getLanguageCode(), param.getCountryCode());
	}


	private static void _p(String o)
	{
		if(false)
		{
			System.out.println(o); 
		}
	}

	private static void _e(String o)
	{
		if(false)
		{
			System.err.println(o); 
		}
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @return String
	 */
	public static String mFormat(String formatMask)
	{
		return formatMask;
	}
	
	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1)
	{
		return MessageFormat.format(formatMask, new Object[]{o1});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5, o6});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5, o6, o7});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5, o6, o7, o8});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 * @param o9
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5, o6, o7, o8, o9});
	}

	/**
	 * Method to wrap the java.text.MessageFormat class
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 * @param o9
	 * @param o10
	 * @return String
	 */
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5, o6, o7, o8, o9, o10});
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 */
	public static void pFormat(String message)
	{
		_p(message);
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 */
	public static void pFormat(String formatMask, Object o1)
	{
		_p(mFormat(formatMask, o1));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 */
	public static void pFormat(String formatMask, Object o1, Object o2)
	{
		_p(mFormat(formatMask, o1, o2));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3)
	{
		_p(mFormat(formatMask, o1, o2, o3));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3, Object o4)
	{
		_p(mFormat(formatMask, o1, o2, o3, o4));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5)
	{
		_p(mFormat(formatMask, o1, o2, o3, o4, o5));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
	{
		_p(mFormat(formatMask, o1, o2, o3, o4, o5, o6));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7)
	{
		_p(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8)
	{
		_p(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7, o8));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 * @param o9
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
	{
		_p(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7, o8, o9));
	}

	/**
	 * Method to wrap System.out.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 * @param o9
	 * @param o10
	 */
	public static void pFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10)
	{
		_p(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param message
	 */
	public static void peFormat(String message)
	{
		_e(message);
	}
	
	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 */
	public static void peFormat(String formatMask, Object o1)
	{
		_e(mFormat(formatMask, o1));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 */
	public static void peFormat(String formatMask, Object o1, Object o2)
	{
		_e(mFormat(formatMask, o1, o2));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3)
	{
		_e(mFormat(formatMask, o1, o2, o3));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3, Object o4)
	{
		_e(mFormat(formatMask, o1, o2, o3, o4));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5)
	{
		_e(mFormat(formatMask, o1, o2, o3, o4, o5));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
	{
		_e(mFormat(formatMask, o1, o2, o3, o4, o5, o6));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7)
	{
		_e(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8)
	{
		_e(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7, o8));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 * @param o9
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9)
	{
		_e(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7, o8, o9));
	}

	/**
	 * Method to wrap System.err.prinln method with formatted output
	 * @param formatMask
	 * @param o1
	 * @param o2
	 * @param o3
	 * @param o4
	 * @param o5
	 * @param o6
	 * @param o7
	 * @param o8
	 * @param o9
	 * @param o10
	 */
	public static void peFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9, Object o10)
	{
		_e(mFormat(formatMask, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10));
	}


	/**
	 * "Pretty prints" and XML org.dom4j.Document
	 * @param doc
	 * @return XML as String
	 */
	public static final String prettyPrint(Document doc)
	{
		String retVal = "";
		try
		{
			ByteArrayOutputStream bab = new ByteArrayOutputStream();
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter( bab, format );

			writer.write(doc);
			bab.flush();

			bab.close();
			retVal = bab.toString();
		}
		catch(IOException ioException)
		{
			retVal = ioException.getMessage();
		}
		return retVal;
	}


	/**
	 * Method to return the Hours, Minutes and Seconds as a String
	 * @param timeInSeconds
	 * @return Formatted output
	 */
	public static String calculateHMSMS(long startTime, long endTime) {
		String retVal = "End Time not set";
		int ms = (int)((endTime - startTime));
		int hours;
		int minutes;
		int seconds;

		hours = ms   / 3600000;
		ms = ms - (hours   * 3600000);
		minutes = ms / 60000;
		ms = ms - (minutes * 60000);
		seconds = ms / 1000;
		ms = ms - (seconds * 1000);

		if(endTime != 0)
		{
			retVal = mFormat("{0} hour{1}, {2} minute{3}, {4} second{5}, {6} millisecond{7}",hours, hours == 1?"":"s", minutes, minutes==1?"":"s", seconds, seconds==1?"":"s", ms, ms==1?"":"s");
		}
		return retVal;
	}

	/**
	 * Repeates a pattern a number of times
	 * @param count
	 * @param pattern
	 * @return
	 */
	public static String repeatPattern(int count, String pattern)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < count; i++)
		{
			sb.append(pattern);
		}

		return sb.toString();
	}

	/**
	 * Null Value method
	 * @param value
	 * @param nullValue
	 * @return String
	 */
	public static String nvl(String value, String nullValue)
	{
		return  value != null ? value : nullValue;
	}

	/**
	 * Returns the country code part from a lang_Country code String
	 * @param languageCountryCode
	 * @return
	 */
	public static String countryCode(String languageCountryCode)
	{
		return languageCountryCode.indexOf("_")>0?languageCountryCode.substring(languageCountryCode.indexOf("_") + 1):languageCountryCode;
	}

	/**
	 * Returns the language part from a lang_Country code String
	 * @param languageCountryCode
	 * @return
	 */
	public static String languageCode(String languageCountryCode)
	{
		return languageCountryCode.indexOf("_")>0?languageCountryCode.substring(0,languageCountryCode.indexOf("_")):languageCountryCode;
	}

	/**
	 * Returns a Map as a String
	 * @param map
	 * @return
	 */
	public static String mapToString(Map map) {
		return mapToString(null, map);
	}

	/**
	 * Returns a Map as a String
	 * @param title
	 * @param describe
	 * @return String value
	 */
	public static String mapToString(String title, Map describe) {
		TreeMap map = new TreeMap(describe); 
		StringBuffer sb = new StringBuffer();
		sb.append("\n-- ").append(title == null?"not specified":title).append(" --");

		sb.append("\n[");
		for(Object key : map.keySet()){
			sb.append(mFormat("{0}:{1}", key, describe.get(key))).append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	//TODO Remove from here. Shouldn't be here
	/**
	 * Static class method to list out details about the ProductDTO
	 * Could extend to have a String[] or fields
	 * @param name
	 * @param list
	 * @return
	 */
	public static String listProductDTO(String name, List<ProductDTO> list)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(name).append("\n");
		sb.append(FormatUtils.repeatPattern(name.length(), "=")).append("\n");

		for(ProductDTO p: list)
		{
			sb.append(p.getProdDevCode()).append("-[").append(p.getNikonLocale()).append("],");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));

		return sb.toString();
	}

	
	public static String collection2String(Collection<String> collection)
	{
		return collection2String(null, collection, ",");
	}
	
	public static String collection2String(Collection<String> collection, String delimeter)
	{
		return collection2String(null, collection, delimeter);
	}
	
	public static String collection2String(String name, Collection<String> collection)
	{
		return collection2String(name, collection, ",");
	}
	
	public static String collection2String(String name, Collection<String> collection, String delimeter)
	{
		StringBuffer sb = new StringBuffer();
		if(name != null)
		{
			sb.append(name).append("\n");
			sb.append(FormatUtils.repeatPattern(name.length(), "=")).append("\n");
		}

		for(String s: collection)
		{
			sb.append(s).append(delimeter);
		}

		sb.deleteCharAt(sb.lastIndexOf(delimeter));
		return sb.toString();
	}

	private static SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm.ss");
	private static SimpleDateFormat wwaDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final String formatWWADate(Date date)
	{
		return date!=null?wwaDateFormat.format(date):"";
	}

	public static final Date parseWWADate(String dateAsString) 
	throws ParseException
	{
		return wwaDateFormat.parse(dateAsString);
	}

	public static String formatDate(Date date)
	{
		return FormatUtils.nvl(sdf1.format(date),"");
	}

	public static String formatDateTime(Date date)
	{

		return FormatUtils.nvl(sdf2.format(date),"");
	}

	public static String byte2String(byte byteVal)
	{
		return "" + byteVal;
	}    

	public static String short2String(short shortVal)
	{
		return "" + shortVal;
	}    

	public static String int2String(int intVal)
	{
		return "" + intVal;
	}    

	public static String long2String(long longVal)
	{
		return "" + longVal;
	}    

	public static String float2String(float floatVal)
	{
		return "" + floatVal; 
	}    

	public static String double2String(double doubleVal)
	{
		return "" + doubleVal;
	}    

	public static String boolean2String(boolean booleanVal)
	{
		return "" + booleanVal;
	}    

	public static boolean string2Boolean(String stringVal)
	{
		return new Boolean(stringVal).booleanValue();
	}    

	public static String char2String(char charVal)
	{
		return "" + charVal;
	}    

	public static String allBSlash(String string)
	{
		return string.replaceAll("/", "\\\\");
	}

	public static String allFSlash(String string)
	{
		return string.replaceAll("\\\\", "/");
	}

}
