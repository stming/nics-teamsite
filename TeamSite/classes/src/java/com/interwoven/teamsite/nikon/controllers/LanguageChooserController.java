package com.interwoven.teamsite.nikon.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.nikon.components.ComponentHelper;

/**
 * @author nbamford
 * 
 * This class is a intended as a site wide pre-controller so that the language cookie is set for all other components
 *
 */
public class LanguageChooserController {
	
	private ComponentHelper ch = new ComponentHelper();
    private Log log = LogFactory.getLog(LanguageChooserController.class);

	/**
	 * Method to call to set the Site Language to an appropriate value in the cookie
	 * @param context Request Context containing information
	 * @return null as we don't want to forward to any other page
	 */
	public ForwardAction setSiteLangauge(RequestContext context) 
	{
		log.debug("Entering ForwardAction setSiteLangauge(RequestContext context)");
		ch.setLanguage(context);
		log.debug("Exiting ForwardAction setSiteLangauge(RequestContext context)");
		return null;
	}
}
