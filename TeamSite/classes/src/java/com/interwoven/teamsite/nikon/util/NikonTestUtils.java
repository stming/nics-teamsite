package com.interwoven.teamsite.nikon.util;

import java.util.Enumeration;
import java.util.Properties;

import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;



/**
 * @author nbamford
 * This is a set of general use utilities not logic or format
 * based
 */
public class NikonTestUtils {

	public NikonTestUtils (){}
	
	public String areDevCodesInWWA(String propsName, String csvProdDevCodes)
	{
		return areDevCodesInWWA(Utils.loadProperties(propsName), csvProdDevCodes);
	}
	
	/**
	 * Method to test if DevCode are in WWA based on a Properties Object where the key is the QCode and the value
	 * = True or False. If the Dev code isn't found then return false
	 * @param props
	 * @param csvProdDevCodes
	 * @return
	 */
	public String areDevCodesInWWA(Properties props, String csvProdDevCodes)
	{
		String retVal = "True";
		
		csvProdDevCodes = NikonUtils.cleanCSVProdDevCode(csvProdDevCodes);
		
		for(String devCode: NikonUtils.csvProdDevCode2StringCollection(csvProdDevCodes))
		{
			Object val = props.get(devCode);
			if((val == null) 
            || (NikonDomainConstants.VAL_FALSE.equalsIgnoreCase(val.toString())))
			{
				retVal = "False";
				break;
			}
		}
		props = null;
		return retVal;
		
	}

}
