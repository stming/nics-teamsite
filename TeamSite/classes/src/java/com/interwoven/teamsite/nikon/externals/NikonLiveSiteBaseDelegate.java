package com.interwoven.teamsite.nikon.externals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.dom4j.Document;
import org.springframework.context.ApplicationContext;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.UserSession;
import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.common.NikonTeamsiteEnvironmentX;
import com.interwoven.teamsite.nikon.springx.NikonBusinessManager;

public abstract class NikonLiveSiteBaseDelegate {

	protected RequestContext context;
	UserSession userSession;
	protected NikonBusinessManager dm;
	protected TeamsiteEnvironment environment;
	protected Log log = LogFactory.getLog(this.getClass());

	NikonBusinessManager getNikonHBN8DAOManager() {
		if(dm == null)
		{
			
			ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
			Object o = appCtx.getBean("nikon.hibernate.dao.manager");
			
			dm = (NikonBusinessManager)o;
		}
			
		return dm;
	}

	protected TeamsiteEnvironment getTeamsiteEnvironment() {
		if(environment == null)
		{
			environment = NikonTeamsiteEnvironmentX.getSpringApplicationContextInstance(); 
		}
		
		return environment;
	}

	/**
	 * Helper method to return the contents of a DCR as {@link Document}
	 * @param requestContext
	 * @param path
	 * @return
	 */
	String getSessionLanguageCode() {
		return userSession.getAttribute(NikonDomainConstants.LANGUAGE_CODE) == null?NikonDomainConstants.DEFAULT_LANGUAGE:(String)userSession.getAttribute(NikonDomainConstants.LANGUAGE_CODE);
	}

	String getSessionCountryCode() {
		return userSession.getAttribute(NikonDomainConstants.COUNTRY_CODE) == null?NikonDomainConstants.DEFAULT_COUNTRY:(String)userSession.getAttribute(NikonDomainConstants.COUNTRY_CODE);
	}

	Document loadDCRForDTOInLocale(RequestContext context, String path, String nikonLocale) 
	{
		return loadCachedDocument(context, nikonLocale + "_" + path, path.endsWith("\\")?"":"\\" + path, FormatUtils.countryCode(nikonLocale));
	}

	public void setEnvironment(TeamsiteEnvironment environment) {
		this.environment = environment;
	}

	// Helper method to load cachedDocument
	Document loadCachedDocument(RequestContext requestContext, String key, String path)
	{
		return loadCachedDocument(requestContext, key, path, null);
	}
	
	Document loadCachedDocument(RequestContext requestContext, String key, String path, String branchCountryCode)
	{
		Document docToAppend = null;
		//Only look in cache if we're in runtime
		if(requestContext.isRuntime() && true)
		{
	        //declare doc for return of headings
	        //add is stream to xml
	        //Document to retrieve from cache or load from the cache
	        JCS cache = null;
	        try 
	        {
				cache = JCS.getInstance(NikonDomainConstants.JCS_REGION_DCR_DOC);
			} 
	        catch (CacheException e) {
	        	log.warn(e);
			}
	        
	        //Look in the cache if we're in runtime and we found the JCS
	        if(cache != null)
	        {
	        	log.info(FormatUtils.mFormat("Looking for key:{0}", key));
	        	
	        	Object cacheObj = cache.get(key);
	        	
	        	if(cacheObj == null)
	        	{
	            	log.info(FormatUtils.mFormat("Key:{0} not found", key));
	        		cacheObj = Utils.dcrToXML(requestContext, path, branchCountryCode);
	        		try 
	        		{
						cache.put(key, cacheObj);
						log.info(FormatUtils.mFormat("Putting key:{0} value:{1} for branchCountryCode:{2} into cachce region:{3}", key, path, branchCountryCode, NikonDomainConstants.JCS_REGION_DCR_DOC));
					} 
	        		catch (CacheException e) {
	        			log.warn(e);
	                	docToAppend = Utils.dcrToXML(requestContext, path);
					}
	        	}
	        	else
	        	{
	            	log.info(FormatUtils.mFormat("Key:{0} found", key));
	        	}
	        	
	        	docToAppend = (Document)cacheObj;
			}
		}
        
		//If we're null here then load 
        if (docToAppend == null)
        {
        	docToAppend = Utils.dcrToXML(requestContext, path, branchCountryCode);
        }
        
        return docToAppend;
	}

}
