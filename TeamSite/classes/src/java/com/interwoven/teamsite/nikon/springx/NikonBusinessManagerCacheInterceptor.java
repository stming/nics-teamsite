package com.interwoven.teamsite.nikon.springx;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.businessrules.CacheKeyManager;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;

/**
 * This class is used as a Before Advise for caching
 * It will intercept on methods and depending on the method choose
 * the correct caching strategy. It is configured via the Spring ProxyFactoryBean
 * class and applies only to Runtime instances.
 * @author nbamford
 *
 */
public class NikonBusinessManagerCacheInterceptor 
implements MethodInterceptor
{
	private Log log = LogFactory.getLog(NikonBusinessManagerCacheInterceptor.class);
	private HBN8QueryParamDTO param;
	private String key;
	private String cacheRegion;
	

	public Object invoke(MethodInvocation arg0) throws Throwable {
		log.debug("Entering public void before(Method arg0, Object[] arg1, Object arg2) throws Throwable");
		Method method = arg0.getMethod();
		String methodName = method.getName();
		Object cacheObj = null;

		param = (HBN8QueryParamDTO)arg0.getArguments()[0];
		
		//If we're not in runtime then just proceed with cutpoint
		if(!param.getRequestContext().isRuntime())
		{
			return arg0.proceed();
		}
		
		if(NikonBusinessManager.listProductDetails.equals(methodName))
		{
			log.debug(FormatUtils.mFormat("Dealing with method {0}", methodName));
			cacheRegion = NikonDomainConstants.JCS_REGION_PROD_DTO;
			key = CacheKeyManager.productdtoKey(param, method);
		}
		else if(NikonBusinessManager.listNavCatProducts.equals(methodName))
		{
			log.debug(FormatUtils.mFormat("Dealing with method {0}", methodName));
			cacheRegion = NikonDomainConstants.JCS_REGION_PROD_DTO_NAV;
			key = CacheKeyManager.productdtoNavKey(param, method);
		}
		else if(NikonBusinessManager.listCatalogueProducts.equals(methodName))
		{
			log.debug(FormatUtils.mFormat("Dealing with method {0}", methodName));
			cacheRegion = NikonDomainConstants.JCS_REGION_PROD_DTO_CAT;
			key = CacheKeyManager.productdtoCatKey(param, method);
		}
		else if(NikonBusinessManager.listProductMetaDataFromDCRPath.equals(methodName)){
			log.debug(FormatUtils.mFormat("Dealing with method {0}", methodName));
			cacheRegion = NikonDomainConstants.JCS_REGION_PROD_DTO;
			key = CacheKeyManager.productdtoDCRPathKey(param, method);
		}
		else if(NikonBusinessManager.listPressLibrayByCategoryNav.equals(methodName)){
			log.debug(FormatUtils.mFormat("Dealing with method {0}", methodName));
			cacheRegion = NikonDomainConstants.JCS_REGION_PRESS_LIBRARY_DTO;
			key = CacheKeyManager.presslibrarydtoByCatNav(param, method);
		}
		else
		{
			log.info(FormatUtils.mFormat("No Caching strategy implemented for method {0}", methodName));
			return arg0.proceed();
		}
				

		//Look in the cache
		cacheObj = getCacheObj(cacheRegion, key);
		
		//At this point depending on what we may have found in the cache, either we return the value
		//or we let the method call go through
		if(cacheObj == null)
		{
			return arg0.proceed();
		}
		else
		{
			return cacheObj;
		}
	}

	//Helper method to look in the cache
	private Object getCacheObj(String cacheRegion, Object key)
	{
		Object retVal = null;
		JCS cache = null;
		try 
		{
			cache = JCS.getInstance(cacheRegion);
		} 
		catch (CacheException e) {
			log.warn(e);
		}

		//We should only have this configured for Runtime, but will put in just
		//in case it sneaks into Dev
		if((cache != null))
		{	
			log.info(FormatUtils.mFormat("Looking in cache region:{0} for key:{1}", cacheRegion, key));
			retVal = cache.get(key);
			
			//If we found our object the use it
			if(retVal != null)
			{
				log.info(FormatUtils.mFormat("Key:{0} found", key));
			}

			log.info(FormatUtils.mFormat("Returning a {0} null object from cache", retVal == null?"":"non"));
		}
		return retVal;
	}
}
