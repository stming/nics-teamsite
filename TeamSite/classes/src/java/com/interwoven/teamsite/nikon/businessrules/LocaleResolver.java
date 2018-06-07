package com.interwoven.teamsite.nikon.businessrules;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.CookieHash;
import com.interwoven.livesite.file.FileDALIfc;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.components.ComponentHelper;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.externals.NikonLiveSiteTaggableContentExternalDelegate;

/**
 * 
 * @author nbamford
 *
 * Class containing the Locale resolutions. That is if for instance fr_CH is not found
 * it may return fr_FR instead. Moved the functionality in TS/LS to come from a DCR
 */
final public class LocaleResolver
{
	private static Log log = LogFactory.getLog(LocaleResolver.class);
	private static Map<String, Map<String, LinkedList<String>>> localeResolverMap;
	private static int mapLoadCount = 0;
	
	ComponentHelper ch = new ComponentHelper();


	// Helper method to add data to the locale resolver map derived from the DCR
	private static void addToMap(boolean isRuntime, String siteCountryCode, String rootLC, LinkedList<String> fbLC)
	{
		Map<String, LinkedList<String>> map;

		if(localeResolverMap.containsKey(siteCountryCode))
		{
			map = localeResolverMap.get(siteCountryCode);
		}
		else
		{
			map = new LinkedHashMap<String, LinkedList<String>>();
			localeResolverMap.put(siteCountryCode, map);
		}
		map.put(rootLC, fbLC);
	}

	/**
	 * Method to return a collection of lang_country codes for a request context
	 * The rules are
	 * 
	 * cookie
	 * browser setting
	 * default for site
	 * 
	 * if cookie or browser and is in the list of valid languages for site
	 * then use else default for site
	 * 
	 * @param context
	 * @return
	 */
	public static Collection<String> resolvePossibleLocales(RequestContext context)
	{
		log.debug("Entering public static Collection<String> resolvePossibleLocales(RequestContext context)");
		
		buildLocaleResolverMapFromDCR(context);

		Collection<String> retCol = resolvePossibleLocales(getSiteCountryCode(context), getRequestedLanguageCountryCode(context));

		log.debug("Exiting public static Collection<String> resolvePossibleLocales(RequestContext context)");

		return retCol;
	}

	/**
	 * Method to return a collection of lang_country codes for a given
	 * instance of HBN8QueryParamDTO. Was used as a wrapper when testing
	 * so may be able to take it out
	 * @param paramDto
	 * @return Collection of langCountry codes for a given country code
	 */
	public static Collection<String> resolvePossibleLocales(HBN8QueryParamDTO paramDto)
	{	
		log.debug("Entering public static Collection<String> resolvePossibleLocales(HBN8QueryParamDTO paramDto)");
		
		Collection<String> retCol = null;
		
		if (NikonHBN8ParamConstants.MODE_GENERATE.equals(paramDto.getMode()) || NikonHBN8ParamConstants.MODE_READ_STATIC.equals(paramDto.getMode())){
			
			log.debug("In generation or static file mode, will try to retrieve the locale fallback from paramDto");
			buildLocaleResolverMapFromDCR(paramDto.getRepo().retrieveLocaleFallbackDocument(), NikonHBN8ParamConstants.MODE_GENERATE.equals(paramDto.getMode()) ? false : true);
			retCol = resolvePossibleLocales(paramDto.getSiteCountryCode(), paramDto.getLanguageCountryCode());
		
		} else {
			
			// If we're passing the context around within teamsite then do it on Context and get fallback rules with browser language
			if(paramDto.getRequestContext() != null)
			{
				log.debug("Request Context is not null");
				retCol = resolvePossibleLocales(paramDto.getContext());
			}
			// Else we might be in testing
			else
			{
				log.debug("Request Context is null");
				retCol = resolvePossibleLocales(paramDto.getSiteCountryCode(), paramDto.getLanguageCountryCode());
			}			
		}

		log.debug("Exiting public static Collection<String> resolvePossibleLocales(HBN8QueryParamDTO paramDto)");
		
		return retCol;
	}	

	/**
	 * For a lang_Country code value, returns the possible resolutions in order
	 * the data should be set up to always default to en_Asia
	 * @param langCountry lang_Country code
	 * @return Collection<String> of lang_Country codes
	 */
	public static Collection<String> resolvePossibleLocales(String siteCountryCode, String langCountry)
	{
		log.debug("Entering Collection<String> resolvePossibleLocales(String siteCountryCode, String langCountry)");

		LinkedList<String> retCol = new LinkedList<String>();

		Map<String, LinkedList<String>> map = localeResolverMap.get(siteCountryCode);

		//If we can't find the country then return default language_country
		if(map == null)
		{
			retCol.add(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY);
		}
		//Else look for language_country within country
		else
		{
			LinkedList<String> languageListForSiteCountryCode = map.get(langCountry);

			//If no language country code within country return default language country
			if(languageListForSiteCountryCode == null)
			{
				retCol.add(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY);
			}
			else
			{
				retCol = languageListForSiteCountryCode;
			}
		}

		log.debug("Exiting Collection<String> resolvePossibleLocales(String siteCountryCode, String langCountry)");
		
		return (Collection<String>) retCol.clone();
	}

	public static synchronized void buildLocaleResolverMapFromDCR(RequestContext context)
	{
		log.debug("Entering static synchronized void buildLocaleResolverMapFromDCR(RequestContext context)");
		Document docToParse = readLocalisationFallbackRulesDCR(context);
		buildLocaleResolverMapFromDCR(docToParse, context.isRuntime());
		log.debug("Exiting static synchronized void buildLocaleResolverMapFromDCR(RequestContext context)");
	}

	@SuppressWarnings("unchecked")
	public static synchronized void buildLocaleResolverMapFromDCR(Document docToParse, boolean runtime)
	{
		log.debug("Entering static synchronized void buildLocaleResolverMapFromDCR(Document docToParse, boolean runtime)");

		//If we're in preview and the resolverMap is null then build it
		if((!runtime) || (localeResolverMap == null))
		{
			log.debug("Either this is not runtime or the localeResolverMap is null, will build the map");
			localeResolverMap = new LinkedHashMap<String, Map<String, LinkedList<String>>>();
			
			List<Node> countryList = docToParse.selectNodes("/language_fallback_rules/countries");

			//Loop through Countries
			for(Node countryNode : countryList)
			{
				Node countryCodeNode = countryNode.selectSingleNode("country_code");
				String countryCode   = countryCodeNode.getText();

				List<Node> rootLanguages = countryNode.selectNodes("root_language");

				for(Node rootLanguageNode : rootLanguages)
				{
					Node languageCodeNode = rootLanguageNode.selectSingleNode("@language_code");

					String rootLanguage = languageCodeNode.getText();

					List<Node> fallbackLanguages = rootLanguageNode.selectNodes("fallback_languages/language_code");
					LinkedList<String> lfbList = new LinkedList<String>();

					// Add the starting language
					lfbList.add(rootLanguage);
					for(Node fallbackLanguageNode : fallbackLanguages)
					{
						String fallbackLanguage = fallbackLanguageNode.getText();
						//Any in here in the order parsed
						lfbList.add(fallbackLanguage);
					}
					// Plus Fallback language
					if (!rootLanguage.equals(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY))
					{
						lfbList.add(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY);
					}
					
					addToMap(runtime, countryCode, rootLanguage, lfbList);
				}
			}				
		}
		log.debug("Exiting static synchronized void buildLocaleResolverMapFromDCR(Document docToParse, boolean runtime)");
	}


	// Method to return the DCR templatedata/Admin/FallbackRules/data/localisation_fallback_rules which is constant and should always exist
	@SuppressWarnings("deprecation")
	private static Document readLocalisationFallbackRulesDCR(RequestContext context) {
		log.debug("Entering public static Document readLocalisationFallbackRulesDCR(RequestContext context)");
		FileDALIfc fileDal = context.getFileDAL();
		String root = fileDal.getRoot();
		String alteredRoot = "";
		log.debug(FormatUtils.mFormat("isRuntime:{0}", context.isRuntime()));
		if(context.isRuntime())
		{
			alteredRoot = root + "/";
		}
		else
		{
			alteredRoot = root.replaceFirst("(.*)/([A-Z][A-Z]|Asia)/(.*)", FormatUtils.mFormat("$1/Asia/$3", ""));
			//make sure we change anything after Workarea to main_wa
			alteredRoot = alteredRoot.replaceFirst("WORKAREA/.*", "WORKAREA/main_wa");
		}

		String fullPath = FormatUtils.mFormat("{0}/{1}", alteredRoot, NikonDomainConstants.FALLBACK_RULES_DCR);
		log.debug(FormatUtils.mFormat("fullPath:{0}", fullPath));

		InputStream is = context.getFileDal().getStream(fullPath);
		Document doc = Dom4jUtils.newDocument(is);		

		log.debug("Exiting public static Document readLocalisationFallbackRulesDCR(RequestContext context)");
		return doc;
	}


	private static String defaultLanguageForCountry(String siteCountryCode) {
		log.debug("Entering String defaultLanguageForCountry(String siteCountryCode)");
		String retVal = "";
		List<String> c = new LinkedList<String>(defaultForSiteCountryCode(siteCountryCode));
		if(c.size() > 1)
		{
			retVal = c.get(c.size() - 2);
		}
		log.debug(FormatUtils.mFormat("Found default language country code for {0} : {1}", siteCountryCode, retVal));
		log.debug("Exiting String defaultLanguageForCountry(String siteCountryCode)");

		return retVal;
	}

	/**
	 * For a given two character country code, return the default language
	 * plus the ultimate fallback
	 * @param siteCountryCode
	 * @return
	 */
	public static Collection<String> defaultForSiteCountryCode(String siteCountryCode)
	{
		log.debug("Entering public static Collection<String> defaultForSiteCountryCode(String siteCountryCode)");
		Collection<String> retCol = new LinkedList<String>();

		//Get the country specific HashMap of langcountrycodes
		Map<String, LinkedList<String>> map = localeResolverMap.get(siteCountryCode);
		log.debug("LocaleResolverMap: " + map);

		if(map == null)
		{
			retCol.add(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY);
		}
		else
		{
			LinkedList<String> languageList = new LinkedList<String>(map.keySet());

			retCol = map.get(languageList.getLast());
			
		}
		
		log.debug("Exiting public static Collection<String> defaultForSiteCountryCode(String siteCountryCode)");
		
		return retCol;
	}

	/**
	 * Method to return the xx_XX country code from a RequestContext. The rules are
	 * look for cookie setting, look for browser setting, determine from the site (branch)
	 * From each it should be valid for this country based on the fallback rules.
	 * i.e. we may be coming in with an invalid cookie for the country or an invalid
	 * browser local for the cookie. So if we do in each case for the country code
	 * determined from the branch we should deliver the default xx_XX for the country
	 * based on the fallback rules
	 * @param context
	 * @return xx_XX lang_Country code
	 */
	public static String getRequestedLanguageCountryCode(RequestContext context)
	{
		log.debug("Entering public static String getRequestedLanguageCountryCode(RequestContext context)");
		//we need to call this to build the map from the fallback rules DCR
		buildLocaleResolverMapFromDCR(context);
		//Get it from the cookie
		String retVal = null;

		String countryCode = getSiteCountryCode(context);
		log.debug(FormatUtils.mFormat("countryCode:{0}", countryCode));

		boolean validate = true;

		//Look in the Cookie cache for the language cookie
		log.debug("Look in the cookie cache for the language");
		retVal = ComponentHelper.getCookieValue(context, NikonDomainConstants.CKIE_LANG_CODE); 
		log.debug(FormatUtils.mFormat("found:{0}", retVal));

		if((retVal == null) && (context.getLocale() != null))
		{
			log.debug("Look at the browser setting for the language");
			retVal = context.getLocale().toString();
			log.debug(FormatUtils.mFormat("found:{0}", retVal));
		}
		
		log.debug("Looking into locale value using site directory");
		String directory = context.getSite().getDirectory();
		if (null != directory && !"".equals(directory)){
			log.debug("Directory is not empty: ["+directory+"]");
			String[] dirs = directory.split("_");
			if (dirs.length >= 2){
				retVal = dirs[0] + "_" + dirs[1];
				log.debug("Found locale value using directory: " + retVal);				
			}
		}

		//At this point it's not in cookie or browser determine the country
		//code for the Branch and return the default language for that country
		if(retVal == null)
		{
			log.debug(FormatUtils.mFormat("No browser setting getting default for Site with countryCode :{0}", countryCode));
			validate = false;
			log.debug(FormatUtils.mFormat("Browser language setting not found, using {0}.", retVal));
			Collection<String> col = defaultForSiteCountryCode(countryCode);

			/**
			 * At this point we have either 2 or 1 language code
			 * default for country + default for all countries
			 * or default for all countries
			 * 
			 * Should never be null
			 * 
			 */

			retVal = col.iterator().next();
			log.debug(FormatUtils.mFormat("Default for Site with countryCode :{0}", countryCode));
		}		

		//validate the retVal against the country code for the branch if we need to
		if(validate)
		{
			log.debug(FormatUtils.mFormat("Found a browser setting {0}, validating it", retVal));
			retVal = validateReturnedValueForCountry(retVal, countryCode);
			log.debug(FormatUtils.mFormat("Validated value:{0}", retVal));
		}

		log.debug(FormatUtils.mFormat("Resolved Requested Language Code to {0}", retVal));
		log.debug("Exiting public static String getRequestedLanguageCountryCode(RequestContext context)");

		return retVal;
	}


	//Will return a valid value from the fallback rules and country code
	public static String validateReturnedValueForCountry(String langCountry, String countryCode) {
		log.debug("Entering String validateReturnedValueForCountry(String langCountry, String countryCode)");

		log.debug(FormatUtils.mFormat("langCountry:{0}", langCountry));
		log.debug(FormatUtils.mFormat("countryCode:{0}", countryCode));

		String retVal = langCountry;
		Map<String, LinkedList<String>> map = localeResolverMap.get(countryCode);
		if(map == null)
		{
			retVal = NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY;
		}
		else
		{
			List<String> possibleValues =  new LinkedList<String>(map.keySet());

			//If our languageCountry code xx_XX is not in this list then return the default value in this list

			//Because there's no such locale en_EU (default made up one)
			//we have to treat it differently because en_EU exists in ALL possibleValues
			log.debug(FormatUtils.mFormat("!langCountry.equals(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY):{0}", !langCountry.equals(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY)));

			if(!langCountry.equals(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY))
			{
				if(!possibleValues.contains(langCountry))
				{
					log.debug(FormatUtils.mFormat("{0} not found in possible values list:{0}", possibleValues.toString()));
					Collection<String> col = defaultForSiteCountryCode(countryCode);
					retVal = defaultForSiteCountryCode(countryCode).iterator().next();
				}
			}
			//WE need the default for the country
			else
			{
				retVal = defaultForSiteCountryCode(countryCode).iterator().next();
			}
		}
		// TODO Auto-generated method stub
		log.debug(FormatUtils.mFormat("retVal:{0}", retVal));
		log.debug("Exiting String validateReturnedValueForCountry(String langCountry, String countryCode)");
		return retVal;
	}

	/**
	 * This method should return the two character country code
	 * which it needs to resolve either from the branch in dev/preview
	 * or some other means TBC in runtime
	 * @param context Request Context
	 * @return two letter country code or the default country code
	 */
	public static String getSiteCountryCode(RequestContext context)
	{
		log.debug("Entering public static String getSiteCountryCode(RequestContext context)");
		String retVal = NikonDomainConstants.DEFAULT_COUNTRY;
		log.debug("context.getComponentId():" + context.getComponentId());

		log.debug("context == null:" + (context == null));
		log.debug("context.getSite() == null:" + (context.getSite() == null));
		log.debug("context.getSite().getBranch() == null:" + (context.getSite().getBranch() == null));
		String branch = context.getSite().getBranch();

		//Take of an erroneous trailing /
		if(branch.charAt(branch.length()-1) == '/')
		{
			branch = branch.substring(0, branch.length() - 2);
		}
		retVal = branch.substring(branch.lastIndexOf("/") + 1);
		log.debug("Resolved Site Country Code to " + retVal);
		log.debug("Exiting public static String getSiteCountryCode(RequestContext context)");
		return retVal;
	}

	static Map<String, List<String>> dctCallOutLocaleMap = new TreeMap<String, List<String>>();

	static{
		for(Locale locale : Locale.getAvailableLocales())
		{
			String countryCode = locale.getCountry();
			List<String> localeList = null;
			if(!dctCallOutLocaleMap.containsKey(countryCode))
			{
				localeList = new LinkedList<String>();
				dctCallOutLocaleMap.put(countryCode, localeList);
			}
			localeList = dctCallOutLocaleMap.get(countryCode);
			String languageCode = locale.getLanguage();
			localeList.add(FormatUtils.languageCountryCode(languageCode, countryCode));
		}

		List<String> list = new LinkedList<String>();
		list.add(NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY);
		dctCallOutLocaleMap.put(NikonDomainConstants.DEFAULT_COUNTRY, list);


	}
	//	NB 20090617 NOT NEEDED    
	//    public static String listValuesToOptionList(String countryCode){
	//		log.debug("Entering public static String listValuesToOptionList(String countryCode)");
	//    	List<String> list = dctCallOutLocaleMap.get(countryCode);
	//    	StringBuffer sb = new StringBuffer();
	//    	
	//    	sb.append("<substitution>");
	//    	for(String languageCountryCode : list)
	//    	{
	//    		sb.append("<option label=\"").append(languageCountryCode).append("\" />");
	//    	}
	//    	sb.append("</substitution>");
	//    	
	//		log.debug("Exiting public static String listValuesToOptionList(String countryCode)");
	//    	return sb.toString();
	//    }	
	public static int getMapLoadCount() {
		return mapLoadCount;
	}
}
