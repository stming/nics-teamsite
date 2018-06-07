package com.interwoven.teamsite.nikon.util;

import java.util.Collection;

import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author nbamford
 * This is a set of general use utilities not logic or format
 * based
 */
public class NikonUtils {

	private static Log log = LogFactory.getLog(NikonUtils.class);
	/**
	 * Will take a comma separated list of dev codes and clean them up
	 * @param csvProdDevCodes
	 * @return
	 */
	public static String cleanCSVProdDevCode(String csvProdDevCodes)
	{
		return csvProdDevCodes.replaceAll(",( )*", ",").trim();
	}
	
	/**
	 * Returns a String[] of a comma sepearted list of prod dev codes
	 * @param csvProdDevCodes
	 * @return
	 */
	public static String[] csvProdDevCode2StringArr(String csvProdDevCodes)
	{
		return cleanCSVProdDevCode(csvProdDevCodes).split(",");
	}
	
	/**
	 * Returns a Collection<String> from a csvProdDevCodeList
	 * @param csvProdDevCodes
	 * @return
	 */
	public static Collection<String> csvProdDevCode2StringCollection(String csvProdDevCodes)
	{
		return Utils.stringArr2StringCollection(csvProdDevCode2StringArr(csvProdDevCodes));
	}

	/**
	 * Class method to return the CSVPath from an array of CSWorkareas
	 * @param csWAArr
	 * @param regEx
	 * @return CSVPath which matched the regex. Null if not matched
	 */
	public static CSVPath vPathFromCSWorkAreaArrayFirstRegexMatch(CSWorkarea[] csWAArr, String regEx)
	{
		CSVPath retVal = null;
		for(CSWorkarea wa: csWAArr)
		{
			String vPathString = wa.getVPath().toString();
			if(vPathString.matches(regEx))
			{
				retVal = wa.getVPath();;
				log.debug(FormatUtils.mFormat("vPath {0} matched regex {1}", retVal, regEx));
				break;
			}
		}
		return retVal;
	}
    public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }	

}
