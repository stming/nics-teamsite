package com.interwoven.teamsite.nikon.controllers;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;

/**
 * @author nbamford
 * 
 * This class is a intended as a site wide pre-controller so that the language cookie is set for all other components
 *
 */
public class LanguagePatcherController {
	
    private Log log = LogFactory.getLog(LanguagePatcherController.class);

	/**
	 * Method to call to set the Site Language to an appropriate value in the cookie
	 * @param context Request Context containing information
	 * @return null as we don't want to forward to any other page
	 */
	public ForwardAction setSiteLangauge(RequestContext context) 
	{
		log.debug("Entering ForwardAction setSiteLangauge(RequestContext context)");
		
		//TODO Move to Nikon Constants
		
		String cookieValue = context.getParameterString(NikonDomainConstants.CKIE_LANG_PATCH_VAL).trim();
		if(cookieValue == null)
		{
			cookieValue = NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY;
		}
		log.debug(FormatUtils.mFormat("CKIE_LANG_PATCH_VAL:{0}", cookieValue));
		
		Utils.setCookieValue(context, NikonDomainConstants.CKIE_DEF_PATH, NikonDomainConstants.CKIE_LANG_CODE, cookieValue, NikonDomainConstants.CKIE_LANG_CODE_EXPR_INT);
		log.debug("Exiting ForwardAction setSiteLangauge(RequestContext context)");
		return null;
	}
	
	public Document getLanguageCookieValue(RequestContext requestContext)
	{
		DocumentFactory docFac = DocumentFactory.getInstance();
		Document doc = docFac.createDocument();
		
		String languageCookieValue = Utils.getCookieValue(requestContext, NikonDomainConstants.CKIE_LANG_CODE, NikonDomainConstants.CKIE_DEF_PATH);
		
		Element cookieElement = docFac.createElement("Cookie");
		Element cookieNameElement = docFac.createElement("Name");
		Element utilsCookieValueElement = docFac.createElement("UtilsValue"); 
		Element requestContextCookieValueElement = docFac.createElement("RequestContextValue");
		doc.setRootElement(cookieElement);
		cookieElement.add(requestContext.getCookies().toElement());
		cookieElement.add(cookieNameElement);
		cookieElement.add(utilsCookieValueElement);
		cookieElement.add(requestContextCookieValueElement);
		String cookieName = "null";
		String utilsCookieValue = "null";;
		String requestContextCookieValue = "null";
		
		if(languageCookieValue != null)
		{
			cookieName = NikonDomainConstants.CKIE_LANG_CODE;
			utilsCookieValue = languageCookieValue;
			requestContextCookieValue = requestContext.getCookies() != null ? requestContext.getCookies().getValue(NikonDomainConstants.CKIE_LANG_CODE) :"null";
		}
			
		cookieNameElement.setText(cookieName);
		utilsCookieValueElement.setText(utilsCookieValue);
		requestContextCookieValueElement.setText(requestContextCookieValue);
			
		
		return doc;
	}
}
