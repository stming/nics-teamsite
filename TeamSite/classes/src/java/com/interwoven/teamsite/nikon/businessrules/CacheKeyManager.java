package com.interwoven.teamsite.nikon.businessrules;

import java.lang.reflect.Method;

import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;

/**
 * This class is responsible for the structure of Cache keys for the various
 * JCS Cache regions and for a set of varying inputs
 * @author nbamford
 *
 */
public class CacheKeyManager {

	
	/**
	 * Cache key from a .page file name (absaloute path)
	 * @param fileName
	 * @return
	 */
	public static String pageFileKey(String fileName)
	{
		String retVal = null;
		//Turn all the \s to /s
		fileName = FormatUtils.allFSlash(fileName);
		
        String regex1 = ".*/(([A-Z][A-Z]|Asia))/((.*))";
        String regex2 = ".page$";
        retVal  = fileName.replaceAll(regex1, "$2:$3");
        retVal  = retVal.replaceAll(regex2, "");
		
		return retVal;
	}
	
	/**
	 * Cache Key for a Product
	 * @param param
	 * @param method
	 * @return
	 */
	public static String productdtoKey(HBN8QueryParamDTO param, Method method)
	{
		return productdtoKey(param.getLanguageCountryCode(), param.getProductId(), method.getName());
	}
	
	/**
	 * Cache Key for a Product
	 * @param langCountryCode
	 * @param productId
	 * @param methodName
	 * @return
	 */
	public static String productdtoKey(String langCountryCode, String productId, String methodName)
	{
		return FormatUtils.mFormat("{0}_{1}_{2}", langCountryCode, productId, methodName);
	}
	
	/**
	 * Cache Key for a Product
	 * @param param
	 * @param method
	 * @return
	 */
	public static String productdtoDCRPathKey(HBN8QueryParamDTO param, Method method)
	{
		return productdtoDCRPathKey(param, method.getName());
	}
	
	public static String productdtoDCRPathKey(HBN8QueryParamDTO param, String methodName)
	{
		return FormatUtils.mFormat("{0}_{1}_{2}", param.getLanguageCountryCode(), param.getPath(), methodName);
	}
	


	/**
	 * Cache Key for a Product Catalogue
	 * @param param
	 * @param method
	 * @return
	 */
	public static String productdtoCatKey(HBN8QueryParamDTO param, Method method)
	{
		return productdtoCatKey(param, method.getName());
	}
	
	/**
	 * Cache Key for a Product Catalogue
	 * @param param
	 * @param methodName
	 * @return
	 */
	public static String productdtoCatKey(HBN8QueryParamDTO param, String methodName)
	{
		String retVal = null;
		
		retVal =  FormatUtils.mFormat("{0}_{1}_{2}", param.getLanguageCountryCode(), param.getCategory(), methodName);

		if(!param.getNavCat1().equals("0")){
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}", param.getLanguageCountryCode(), param.getCategory(), param.getNavCat1(), methodName);
		}

		if(!param.getNavCat2().equals("0")){
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}", param.getLanguageCountryCode(), param.getCategory(), param.getNavCat1(), param.getNavCat2(), methodName);
		}

		if(!param.getNavCat3().equals("0")){
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}_{5}", param.getLanguageCountryCode(), param.getCategory(), param.getNavCat1(), param.getNavCat2(), param.getNavCat3(), methodName);
		}
		return retVal;
	}
	
	/**
	 * Cache Key for a Product Nav
	 * @param param
	 * @param method
	 * @return
	 */
	public static String productdtoNavKey(HBN8QueryParamDTO param, Method method)
	{
		return productdtoNavKey(param, method.getName());
	}
	
	
	/**
	 * Cache Key for a Product Nav
	 * @param param
	 * @param methodName
	 * @return
	 */
	public static String productdtoNavKey(HBN8QueryParamDTO param, String methodName)
	{
		String retVal = null;
		if(NikonDomainConstants.RN_QRY_VAL_LVL_1.equals(param.getRunQuery()))
		{
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}", param.getLanguageCountryCode(), param.getCategory(), param.getRunQuery(), methodName);
		}
		else if(NikonDomainConstants.RN_QRY_VAL_LVL_2.equals(param.getRunQuery()))
		{
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}", param.getLanguageCountryCode(), param.getCategory(), param.getRunQuery(), param.getNavCat1(), methodName);
		}
		else if(NikonDomainConstants.RN_QRY_VAL_LVL_3.equals(param.getRunQuery()))
		{
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}_{5}", param.getLanguageCountryCode(), param.getCategory(), param.getRunQuery(), param.getNavCat1(), param.getNavCat2(), methodName);
		}
		else if(NikonDomainConstants.RN_QRY_VAL_LVL_4.equals(param.getRunQuery()))
		{
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}_{5}_{6}", param.getLanguageCountryCode(), param.getCategory(), param.getRunQuery(), param.getNavCat1(), param.getNavCat2(), param.getNavCat3(), methodName);
		}
		retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}_{5}_{6}", param.getLanguageCountryCode(), param.getCategory(), param.getRunQuery(), param.getNavCat1(), param.getNavCat2(), param.getNavCat3(), methodName);
		
		return retVal;
	}
	
	/**
	 * Cache Key for Award Testimonials
	 * @param param
	 * @param method
	 * @return
	 */
	public static String awardTestimonialdtoKey(HBN8QueryParamDTO param, Method method)
	{
		return awardTestimonialdtoKey(param, method.getName());
	}
	
	/**
	 * Cache Key for Award Testimonials
	 * @param param
	 * @param methodName
	 * @return
	 */
	public static String awardTestimonialdtoKey(HBN8QueryParamDTO param, String methodName)
	{
		String retVal = null;
		if(param.isSingleAwardQry())
		{
			retVal = FormatUtils.mFormat("{0}_{1}_{2}_{3}", param.getLanguageCountryCode(), "SingleAward", param.getAwardTestId(), methodName);	
		}
		else if(param.isMultipleAwardsQry())
		{
			retVal = FormatUtils.mFormat("{0}_{1}_{2}_{3}", param.getLanguageCountryCode(), "MultipleAwards", FormatUtils.collection2String(param.getAwardTestIds(),":"), methodName);	
		}
		else if(param.isAwardYearQry())
		{
			retVal = FormatUtils.mFormat("{0}_{1}_{2}_{3}", param.getLanguageCountryCode(), "AwardYear", param.getAwardTestYear(), methodName);	
		}
			
		return retVal;
	}
	
	/**
	 * Cache Key for Press Image Library
	 * @param param
	 * @param method
	 * @return
	 */
	public static String presslibrarydtoByCatNav(HBN8QueryParamDTO param, Method method)
	{
		return presslibrarydtoByCatNav(param, method.getName());
	}
	
	/**
	 * Cache Key for Press Image Library
	 * @param param
	 * @param methodName
	 * @return
	 */
	public static String presslibrarydtoByCatNav(HBN8QueryParamDTO param, String methodName)
	{
		String retVal = null;
		
		retVal =  FormatUtils.mFormat("{0}_{1}_{2}", param.getLanguageCountryCode(), param.getCategory(), methodName);

		if(!param.getNavCat1().equals("0")){
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}", param.getLanguageCountryCode(), param.getCategory(), param.getNavCat1(), methodName);
		}

		if(!param.getNavCat2().equals("0")){
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}", param.getLanguageCountryCode(), param.getCategory(), param.getNavCat1(), param.getNavCat2(), methodName);
		}

		if(!param.getNavCat3().equals("0")){
			retVal =  FormatUtils.mFormat("{0}_{1}_{2}_{3}_{4}_{5}", param.getLanguageCountryCode(), param.getCategory(), param.getNavCat1(), param.getNavCat2(), param.getNavCat3(), methodName);
		}
		return retVal;
	}
}
