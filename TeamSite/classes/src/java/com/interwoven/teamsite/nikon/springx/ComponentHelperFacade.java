package com.interwoven.teamsite.nikon.springx;

import org.dom4j.Document;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.spring.ApplicationContextUtils;

public class ComponentHelperFacade implements ComponentHelperI {


	//Return ComponentHelperImpl from Spring
	private ComponentHelperI getComponentHelper()
	{
		ComponentHelperI componentHelper = (ComponentHelperI)ApplicationContextUtils.getApplicationContext().getBean(springCtxName);
		return componentHelper;
	}

	public Document getDCR(RequestContext requestContext) {
		return getComponentHelper().getDCR(requestContext);
	}

	public Document getFromUrl(RequestContext requestContext) {
		return getComponentHelper().getFromUrl(requestContext);
	}

	public Document getLocalisedDCR(RequestContext requestContext) {
		return getComponentHelper().getLocalisedDCR(requestContext);
	}

	public Document getLocalisedDCR(RequestContext requestContext, String dcrName) {
		return getComponentHelper().getLocalisedDCR(requestContext, dcrName);
	}

	public Document getLocalisedDCR(RequestContext requestContext, String dcrName, String rootElement, String path) {
		return getComponentHelper().getLocalisedDCR(requestContext, dcrName, rootElement, path);
	}

	public Document getLocalisedDictionary(RequestContext requestContext) {
		return getComponentHelper().getLocalisedDictionary(requestContext);
	}

	public Document getPageName(RequestContext requestContext) {
		return getComponentHelper().getPageName(requestContext);
	}

	public Document setLanguage(RequestContext requestContext) {
		return getComponentHelper().setLanguage(requestContext);
	}

	public Document getRightNow(RequestContext context) {
		// TODO Auto-generated method stub
		return getComponentHelper().getRightNow(context);
	}
}
