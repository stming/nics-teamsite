package com.interwoven.teamsite.nikon.springx;

import org.dom4j.Document;

import com.interwoven.livesite.runtime.RequestContext;

public interface ComponentHelperI {

	public final static String springCtxName = "com.interwoven.teamsite.nikon.components.ComponentHelper";
	public Document getDCR(RequestContext requestContext);
	public Document getFromUrl(RequestContext requestContext);
	public Document getLocalisedDCR(RequestContext requestContext);
	public Document getLocalisedDictionary(RequestContext requestContext);
	public Document getPageName(RequestContext requestContext);
	public Document setLanguage(RequestContext requestContext);
	
	//Should these be included?
	public Document getLocalisedDCR(RequestContext requestContext, String dcrName);
	public Document getLocalisedDCR(RequestContext requestContxt, String dcrName, String rootElementString, String path);
	
	public Document getRightNow(RequestContext context);

}
