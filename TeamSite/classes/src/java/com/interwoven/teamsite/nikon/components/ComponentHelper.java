package com.interwoven.teamsite.nikon.components;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.interwoven.livesite.common.xml.XmlEmittable;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.ExternalUtils;
import com.interwoven.livesite.external.PropertyContext;
import com.interwoven.livesite.file.FileDALIfc;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.externals.NikonLiveSiteHBN8ExternalDelegate;
import com.interwoven.teamsite.nikon.springx.ComponentHelperI;

/**
 * Helper class for components. Only methods with a single parameter of RequestContext can
 * be called from a Livesite Component as the method has to conform to a specific signature.
 * 
 * The rest can be called from other Java code within livesite
 * 
 * Helper methods include :
 * 
 * Returning a localised version of a requested DCR
 * based on requested language and fallback rules
 * 
 * Returning an localised dictionary based on
 * requested language and fallback rules
 * 
 *
 * @author nbamford
 *
 */
/**
 * @author nbamford
 *
 */
@SuppressWarnings("deprecation")
public class ComponentHelper implements ComponentHelperI
{

	private static Log log = LogFactory.getLog(ComponentHelper.class);


	/**
	 * Default constructor 
	 */
	public ComponentHelper(){}

	/**
	 * Method returns a langCountry code specific dictionary
	 * will return the dictionary it can based on the fallback rules  
	 * @param context
	 * @return dictionary as a Document object
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Document getLocalisedDictionary(RequestContext context){

		//Put the name and location of the default dictionary into the context
		context.getParameters().put(NikonDomainConstants.DICTIONARY_DCR_NAME, NikonDomainConstants.DEFAULT_DICTIONARY);

		Document retDoc = getLocalisedDCR(context, NikonDomainConstants.DICTIONARY_DCR_NAME, "locale_dictionary", "DictionaryDCRPath"); 

		return retDoc;
	}

	//Business logic to provide the dictionary for a specific lanCountryCode
	//used when adding a dictionary to a localised DCR. May be able to get rid of it though
	//need to do some more investigation
	private Document getLocalisedDictionary(RequestContext context, String langCountryCode) {
		
		log.debug("Entering Document getLocalisedDictionary(RequestContext context, String langCountryCode)");
		String requestedLanguageCountryCode = LocaleResolver.getRequestedLanguageCountryCode(context);

		//Dictionary
		String defaultLangGlossary = NikonDomainConstants.DEFAULT_DICTIONARY; 

		Collection<String> possibleDictionaryLocales = LocaleResolver.resolvePossibleLocales(context);

		String fullyLocalisedDCRPath = null;
		Document localisedDictionaryDocument = null;
		for(String resolvedLangCountryCode : possibleDictionaryLocales)
		{
			LocalisedDCRTO lDCRTo = localisedPathToDCR(context, defaultLangGlossary, resolvedLangCountryCode);
			fullyLocalisedDCRPath = lDCRTo.getFullyLocalisedDCRPath();
			log.debug(FormatUtils.mFormat("search for dictionary at fullyLocalisedPath:{0}", fullyLocalisedDCRPath));
			File f = new File(fullyLocalisedDCRPath);

			//If the file exists and 
			if(f.exists() && f.isFile())
			{

				InputStream is = context.getFileDal().getStream(fullyLocalisedDCRPath);

				localisedDictionaryDocument = Dom4jUtils.newDocument(is);
				log.debug(FormatUtils.mFormat("Resolved for {1} @ {0}", fullyLocalisedDCRPath, langCountryCode));
				break;
			}
		}

		// create Result doc
		Document doc = Dom4jUtils.newDocument("<locale_dictionary/>");
		Element root = doc.getRootElement();
		root.addAttribute("DictionaryDCRPath", fullyLocalisedDCRPath);
		root.addAttribute("requestedLocale", requestedLanguageCountryCode);
		root.addAttribute("resolvedLocale", langCountryCode);
		root.appendContent(localisedDictionaryDocument);

		if( localisedDictionaryDocument != null ) {
			
			root.addAttribute("Status", "Success");

		}
		else{
			
			root.addAttribute("Status", "Failure");
		}

		log.debug("Exiting Document getLocalisedDictionary(RequestContext context, String langCountryCode)");
		
		return doc;
	}

	public LocalisedDCRTO localisedPathToDCR(RequestContext context, String localisableDCRVal, String langCountryCode)
	{
		FileDALIfc fileDal = context.getFileDAL();
		String localisedDCRVal = localisableDCRVal.replaceFirst("(.*)/([a-z][a-z]_[A-Z][A-Z]|en_Asia)/(.*)", FormatUtils.mFormat("$1/{0}/$3", langCountryCode));

		String leadingPath = fileDal.getRoot();

		log.debug(FormatUtils.mFormat("leadingPath:{0}", leadingPath));
		log.debug(FormatUtils.mFormat("localisedDCRVal:{0}", localisedDCRVal));

		Document doc = null;
		
		try
		{
			doc = ExternalUtils.readXmlFile(context, localisedDCRVal);
		}
		//If there's an error we need to return a document of some time
		catch(Throwable e)
		{
			doc = Utils.dom4JErrorDocument(FormatUtils.mFormat("Unable to load DCR {0}", localisedDCRVal));
		}

		String localisedLeadingPath = leadingPath;

		//Change the COUNTRY code if not in runtime
		if(!context.isRuntime())
		{
			localisedLeadingPath = leadingPath.replaceFirst("(.*)/([A-Z][A-Z]|Asia)/(.*)", FormatUtils.mFormat("$1/{0}/$3", FormatUtils.countryCode(langCountryCode))); 
		}


		String fullyLocalisedDCRPath = FormatUtils.mFormat("{0}/{1}", localisedLeadingPath, localisedDCRVal);
		LocalisedDCRTO retTo = new LocalisedDCRTO();
		retTo.setLocalisedDCRVal(localisedDCRVal);
		log.debug("retTo.setLocalisedDCRVal(localisedDCRVal):" + retTo.getLocalisedDCRVal());
		retTo.setFullyLocalisedDCRPath(fullyLocalisedDCRPath);
		
		return retTo;
	}



	/**
	 * Method to return a LocalisedDCR with a set name
	 * as described in {@link}com.interwoven.teamsite.nikon.common.NikonDomainConstants.LCLZBL_DCR
	 * 
	 * @param context - Request Context
	 * @return Localised version of DCR if available or en_Asia which should exist
	 * @throws Exception
	 */
	public Document getLocalisedDCR(RequestContext context)
	{
		return getLocalisedDCR(context, NikonDomainConstants.LCLZBL_DCR); 
	}

	/**
	 * Method to return a named DCR
	 * as described in {@link}com.interwoven.teamsite.nikon.common.NikonDomainConstants.LCLZBL_DCR
	 * 
	 * @param context - Request Context
	 * @param dcrName - Name of DCR in context
	 * @return Localised version of DCR if available or en_EU which should exist
	 * @throws Exception
	 */
	public Document getLocalisedDCR(RequestContext context, String dcrName)
	{
		return getLocalisedDCR(context, dcrName, "staticcontent", null);
	}

	public Document getLocalisedDCR(RequestContext context, String dcrName, String rootElementString, String pathAttName)
	{
		log.debug("Entering Document getLocalisedDCR(RequestContext context, String dcrName)");
		String requestedLanguageCountryCode = LocaleResolver.getRequestedLanguageCountryCode(context);
		String x = "----------------------->";
		FormatUtils.peFormat("{0} requestedLanguageCountryCode:{1}",x ,requestedLanguageCountryCode);
		//Get a hold of the dcr from the parameter
		FormatUtils.peFormat("{0} dcrName:{1}",x ,dcrName);
		log.debug("dcrName:" + dcrName);
		
		FormatUtils.peFormat("{0} dcrName:{1}",x ,dcrName);
		String localisableDCRVal = context.getParameterString(dcrName);
		FormatUtils.peFormat("{0} localisableDCRVal:{1}",x ,localisableDCRVal);
		log.debug("localisableDCRVal:" + localisableDCRVal);

		log.debug(FormatUtils.mFormat("localisableDCRVal:{0}", localisableDCRVal));
		log.debug(FormatUtils.mFormat("siteCountryCode  :{0}", LocaleResolver.getSiteCountryCode(context)));
			

		Document doc = DocumentFactory.getInstance().createDocument();
		Element rootElement = null;
		if((!"".equals(rootElementString)) && (rootElementString != null))
		{
			FormatUtils.peFormat("{0} Adding root element",x);
			rootElement = DocumentFactory.getInstance().createElement(rootElementString);
			doc.setRootElement(rootElement);
		}
		
		if(localisableDCRVal !=  null)
		{
			if (localisableDCRVal.contains("templatedata")){
				String[] pathArr = localisableDCRVal.split("/");
				requestedLanguageCountryCode = ("".equals(pathArr[0])) ? pathArr[2] : pathArr[1];
				log.debug(FormatUtils.mFormat("Using DCR path to get the locale requested:{0}", requestedLanguageCountryCode));
			}			
			FormatUtils.peFormat("{0} Adding localised DCR ",localisableDCRVal);
			LocalisedDCRTO lczTo = localiseDCRFromListOfPossibles(buildPossibleListOfDCRsFromFallbackRules(requestedLanguageCountryCode, localisableDCRVal, context));

			//If we have a root element then add some attributes otherwise just append
			if(rootElement != null)
			{
				rootElement.addAttribute("requestedLocale", lczTo.getRequestedLanguageCountryCode());
				rootElement.addAttribute("resolvedLocale", lczTo.getLanguageCountryCode());
				//If there is an attribute to add for the path to the dcr, then set it
				if(pathAttName != null)
				{
					rootElement.addAttribute(pathAttName, lczTo.getFullyLocalisedDCRPath());
				}
				rootElement.appendContent(lczTo.getLocalisedDCRDoc());
			}
			//Just append the content
			else
			{
				doc = (lczTo.getLocalisedDCRDoc());
			}

			//If we want the dictionary and we resolved the locale for the DCR then add it to the root document
			log.debug("load dictionary" + (NikonDomainConstants.VAL_TRUE.equalsIgnoreCase(context.getParameterString(NikonDomainConstants.LD_DIC)) && (lczTo.getLanguageCountryCode() != null)));
			if(NikonDomainConstants.VAL_TRUE.equalsIgnoreCase(context.getParameterString(NikonDomainConstants.LD_DIC)) && (lczTo.getLanguageCountryCode() != null))
			{
				if(rootElement != null)
				{
					rootElement.appendContent(getLocalisedDictionary(context, lczTo.getLanguageCountryCode()));
				}
				else
				{
					doc.getRootElement().add(getLocalisedDictionary(context, lczTo.getLanguageCountryCode()));
				}
			}


			//Change to load metadata for a product or other things tba i.e. not done but probably needed
			if(NikonDomainConstants.VAL_TRUE.equalsIgnoreCase(context.getParameterString(NikonDomainConstants.LD_MT_DT)))
			{
				context.getParameters().append(NikonDomainConstants.MTDT_DCR_PTH, lczTo.getLocalisedDCRVal());
				Document doc2 = getNikonLiveSiteHBN8ExternalDelegate().listProductMetaDataFromDCRPath(context);
				doc.getRootElement().appendContent(doc2);
			}
		}
		log.debug("Exiting Document getLocalisedDCR(RequestContext context, String dcrName)");
		FormatUtils.pFormat(FormatUtils.prettyPrint(doc));
		return doc;
	}

	/**
	 * Method to call from a component via a PREFIX call to set the language cookie in the client's browser
	 * so that the we have a coherent strategy. The logic is
	 * Check for cookie, check for default for site (if the cookie value is valid then we get it back) if cookie is
	 * null or not the same as what's brought back set the cookie
	 * @param context
	 * @return DOM4J Document - states language cookie set to if not already set
	 */
	public Document setLanguage(RequestContext context)
	{
		log.debug("Entering Document setLanguage(RequestContext context)");
		Document retDoc = DocumentFactory.getInstance().createDocument();
		Element rootElement = DocumentFactory.getInstance().createElement("Language");
		retDoc.setRootElement(rootElement);

		Cookie langCookie = Utils.getCookie(context, NikonDomainConstants.CKIE_LANG_CODE, NikonDomainConstants.CKIE_DEF_PATH);
		String cookieLanguage = Utils.getCookieValue(context, NikonDomainConstants.CKIE_LANG_CODE, NikonDomainConstants.CKIE_DEF_PATH);
		String resolvedLanguage = LocaleResolver.getRequestedLanguageCountryCode(context);

		//If the cookie is null or not equal to resolved language then set to resolved language
		if((cookieLanguage == null) || (!resolvedLanguage.equals(cookieLanguage)))
		{
			log.info(FormatUtils.mFormat("Cookie was either null or not valid (cookieValue:{0}, resolvedLanguageForSite:{1})", cookieLanguage , resolvedLanguage));
			setCookieValue(context, NikonDomainConstants.CKIE_LANG_CODE, resolvedLanguage);

			rootElement.addAttribute("Langauge", resolvedLanguage);
		}
		else
		{
			log.info(FormatUtils.mFormat("Language cookie (cookieValue:{0}) is valid", cookieLanguage));
			rootElement.addAttribute("Langauge", cookieLanguage);
		}

		log.debug("Exiting Document setLanguage(RequestContext context)");
		return retDoc;
	}	

	/**
	 * Migrated method from com.nikon.utils.NavTop 
	 * @param context
	 * @return Document containing the XML for the Shopping basket
	 */
	public Document getFromUrl(RequestContext context) {
		Document doc = Dom4jUtils.newDocument();
		Element response = doc.addElement("Response");
		response.addElement("FromUrl").addText(Utils.getCurrentUrl(context));
		return doc;
	}

	/**
	 * Migrated from com.nikon.utils.genericdcrcollector
	 * @param context
	 * @return
	 * @throws DocumentException
	 */
	public Document getDCR(RequestContext context){

		{
			Document doc = getLocalisedDCR(context, "dcr");

			log.debug(FormatUtils.prettyPrint(doc));

			return doc;
		}
	}

	/**
	 * Method to return addition values required by the RightNow page snippets
	 * 
	 * @param context - Request Context
	 * @return Document object set with required elements and attribute values
	 */
	public Document getRightNow(RequestContext context)
	{

		Document doc = Dom4jUtils.newDocument();
		Element right_now = doc.addElement("right_now");

		String requestURI		= context.getRequest().getRequestURI();
		StringBuffer requestURL	= context.getRequest().getRequestURL();
		String protocol 		= context.getRequest().getProtocol();
		String serverName 		= context.getRequest().getServerName();
		int port 				= context.getRequest().getServerPort();
		String urlPrefix		= context.getUrlPrefix();
		boolean isRuntime 		= context.isRuntime();
		
		right_now.addAttribute("requestURI", requestURI);
		right_now.addAttribute("requestURL", requestURL.toString());
		right_now.addAttribute("protocol", protocol);
		right_now.addAttribute("serverName", serverName);
		right_now.addAttribute("port", new Integer(port).toString());
		right_now.addAttribute("urlPrefix", urlPrefix);
		right_now.addAttribute("isRuntime", new Boolean(isRuntime).toString());
		
		if (log.isDebugEnabled()) {
			log.debug("right_now element : " + right_now.toString());
		}
		
		return doc; 
	}

	//Method for getting a cookie value from the RequestContext
	public static String getCookieValue(RequestContext context, String cookieName)
	{
		return getCookieValue(context, cookieName, null);
	}
	
	public static String getCookieValue(RequestContext context, String cookieName, String path)
	{
		path = path == null?NikonDomainConstants.CKIE_DEF_PATH:path;
		Cookie retValCookie = Utils.getCookie(context, cookieName, path);
		return Utils.getCookieValue(context, cookieName, path);
	}

	//Method for setting a cookie value on the client
	public static void setCookieValue(RequestContext context, String cookieName, String cookieValue)
	{
		log.debug("Entering public static void setCookieValue(RequestContext context, String cookieName, String cookieValue)");
		Utils.setCookieValue(context, NikonDomainConstants.CKIE_DEF_PATH, cookieName, cookieValue, NikonDomainConstants.CKIE_LANG_CODE_EXPR_INT);
		log.debug("Exiting public static void setCookieValue(RequestContext context, String cookieName, String cookieValue)");
	}

	// Method to return an instance of NikonNikonLiveSiteHBN8ExternalDelegate could include some caching
	private NikonLiveSiteHBN8ExternalDelegate getNikonLiveSiteHBN8ExternalDelegate()
	{
		return new NikonLiveSiteHBN8ExternalDelegate();
	}

	//	Build a collection of LocalisedDCRTOs 
	private Collection<LocalisedDCRTO> buildPossibleListOfDCRsFromFallbackRules(String requestedLanguageCountryCode, String localisableDCRVal, RequestContext context){
		log.debug("Entering Collection<LocalisedDCRTO> buildPossibleListOfDCRsFromFallbackRules(String localisableDCRVal, RequestContext context)");
		Collection<LocalisedDCRTO> retCol = new LinkedList<LocalisedDCRTO>();
		Collection<String> resolvedLocales = LocaleResolver.resolvePossibleLocales(context);

		for(String langCountryCode: resolvedLocales)
		{
			log.debug(FormatUtils.mFormat("Adding country code {0} to possible fallback list", langCountryCode));
			LocalisedDCRTO lDCRTo = localisedPathToDCR(context, localisableDCRVal, langCountryCode);
			lDCRTo.setFileDal(context.getFileDAL());
			lDCRTo.setLanguageCountryCode(langCountryCode);
			lDCRTo.setRequestedLanguageCountryCode(requestedLanguageCountryCode);
			retCol.add(lDCRTo);
		}
		log.debug("Entering Collection<LocalisedDCRTO> buildPossibleListOfDCRsFromFallbackRules(String localisableDCRVal, RequestContext context)");
		return retCol;
	}

	//	Build all possible localised values locations of requested DCR
	private LocalisedDCRTO localiseDCRFromListOfPossibles(Collection<LocalisedDCRTO> localisedDCRLocations)
	{
		log.debug("Entering LocalisedDCRTO localiseDCRFromListOfPossibles(Collection<LocalisedDCRTO> localisedDCRLocations)");
		LocalisedDCRTO retTo = null;


		log.info(FormatUtils.mFormat("SIZE OF localisedDCRLocations:{0}", localisedDCRLocations.size()));

		for(LocalisedDCRTO to: localisedDCRLocations)
		{
			log.info(to.getFullyLocalisedDCRPath());
		}

		for(LocalisedDCRTO lczdDCRTo : localisedDCRLocations)
		{
			File f = new File(lczdDCRTo.getFullyLocalisedDCRPath());
			log.debug(FormatUtils.mFormat("Looking for file {0} if found we'll use it", f.getAbsolutePath()));
			FormatUtils.peFormat("Looking for file {0} if found we'll use it", f.getAbsolutePath());
			//If the file exists and 
			if(f.exists() && f.isFile())
			{
				retTo = lczdDCRTo;


				InputStream is = lczdDCRTo.getFileDal().getStream(lczdDCRTo.getFullyLocalisedDCRPath());

				Document doc1 = Dom4jUtils.newDocument(is);
				retTo.setLocalisedDCRDoc(doc1);
				log.debug(FormatUtils.mFormat("Resolved for {1} @ {0}", lczdDCRTo.getFullyLocalisedDCRPath(), retTo.getLanguageCountryCode()));
				break;
			}
		}

		if(retTo == null)
		{
			retTo = new LocalisedDCRTO();
			Document doc1 = Dom4jUtils.newDocument("<error/>");
			doc1.getRootElement().setText("Unable to locate DCR");
			doc1.getRootElement().appendContent(doc1);
			retTo.setLocalisedDCRDoc(doc1);
		}

		Document doc = DocumentFactory.getInstance().createDocument();
		doc.setRootElement(retTo.toElement());
		log.debug(FormatUtils.prettyPrint(doc));
		log.debug("Exiting LocalisedDCRTO localiseDCRFromListOfPossibles(Collection<LocalisedDCRTO> localisedDCRLocations)");
		return retTo;
	}

	/**
	 * Migrated from the class com.nikon.utils.langParam 
	 * @param requestContext
	 * @return
	 */
	public Document getPageName(RequestContext requestContext)
	{
		log.debug("Entering Document getPageName(RequestContext requestContext)");
		Document doc = Dom4jUtils.newDocument();
		Element responseElement = doc.addElement("Response");
		responseElement.addElement("PageName").addText(requestContext.getPageName());
		log.debug("Exiting Document getPageName(RequestContext requestContext)");
		return doc;
	}

	public Document getFastXSLT(RequestContext requestContext)
	{
		DocumentFactory docFac = DocumentFactory.getInstance();
		Document retDoc = docFac.createDocument();
		Element rootElement = docFac.createElement("HTML");
		retDoc.setRootElement(rootElement);

		CDATA cData = docFac.createCDATA("<div><b>HELLO WORLD</b></div>");
		rootElement.add(cData);



		return retDoc;
	}
	
	public Document testParamOptions(PropertyContext propertyContext)
	{
		String title = "" + Calendar.getInstance().getTimeInMillis();
		FileDALIfc fDal = propertyContext.getFileDAL();
		String root = fDal.getRoot();
		Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
		
		paramMap.put("FileDALIfc", fDal);
		paramMap.put("root", root);
		
		log.debug(FormatUtils.prettyPrint(Utils.dom4JDocumentFromMap(paramMap)));
		
		return Utils.dom4JDocumentFromMap(paramMap);
	}

	// Simpler local container class for data transfer
	public class LocalisedDCRTO
	implements XmlEmittable
	{
		public LocalisedDCRTO(){};

		public LocalisedDCRTO(FileDALIfc fileDal, String localisedDCRVal,
				String languageCountryCode) {
			super();
			this.fileDal = fileDal;
			this.localisedDCRVal = localisedDCRVal;
			this.languageCountryCode = languageCountryCode;
		}
		private FileDALIfc fileDal;


		private Document localisedDCRDoc;
		private String localisedDCRVal;
		private String fullyLocalisedDCRPath;
		private String languageCountryCode;
		private String requestedLanguageCountryCode;

		public Document getLocalisedDCRDoc() {
			return localisedDCRDoc;
		}
		public void setLocalisedDCRDoc(Document localisedDCRDoc) {
			this.localisedDCRDoc = localisedDCRDoc;
		}
		public String getLanguageCountryCode() {
			return languageCountryCode;
		}
		public void setLanguageCountryCode(String languageCountryCode) {
			this.languageCountryCode = languageCountryCode;
		}
		public String getLocalisedDCRVal() {
			return localisedDCRVal;
		}
		public void setLocalisedDCRVal(String localisedDCRVal) {
			this.localisedDCRVal = localisedDCRVal;
		}
		public FileDALIfc getFileDal() {
			return fileDal;
		}
		public void setFileDal(FileDALIfc fileDal) {
			this.fileDal = fileDal;
		}

		public String getFullyLocalisedDCRPath() {
			return fullyLocalisedDCRPath;
		}

		public void setFullyLocalisedDCRPath(String fullyLocalisedDCRPath) {
			this.fullyLocalisedDCRPath = fullyLocalisedDCRPath;
		}

		public String getRequestedLanguageCountryCode() {
			return requestedLanguageCountryCode;
		}

		public void setRequestedLanguageCountryCode(String requestedLanguageCountryCode) {
			this.requestedLanguageCountryCode = requestedLanguageCountryCode;
		}

		public Element toElement() {
			return toElement(null);
		}

		public Element toElement(String arg0) {

			Element retElement = DocumentFactory.getInstance().createElement("LocalisedDCR");
			retElement.addAttribute("languageCountryCode", languageCountryCode);
			retElement.addAttribute("fullyLocalisedDCRPath", fullyLocalisedDCRPath);
			return retElement;
		}
	}
}
