package com.interwoven.teamsite.nikon.springx;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.businessrules.CacheKeyManager;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.dto.ProductDTO;

/**
 * This is a patch class to exclude any requests to Product details call where the Prod ID = 'NA'
 * @author nbamford
 *
 */
public class NikonBusinessManagerCacheInterceptorV2
implements MethodInterceptor
{
	private Log log = LogFactory.getLog(NikonBusinessManagerCacheInterceptor.class);
	private HBN8QueryParamDTO param;
//	private MetricLog metric;
	private String key;
	private String cacheRegion;


	public Object invoke(MethodInvocation arg0) throws Throwable {
		log.debug("Entering public void before(Method arg0, Object[] arg1, Object arg2) throws Throwable");
		Method method = arg0.getMethod();
		String methodName = method.getName();
		Object cacheObj = null;

		param = (HBN8QueryParamDTO)arg0.getArguments()[0];

		// No request context, we are probably running this outside livesite
		if (param.getRequestContext() == null){
			return arg0.proceed();
		}
		//If we're not in runtime then just proceed with cutpoint
		if(!param.getRequestContext().isRuntime())
		{
			return arg0.proceed();
		}

//		metric = param.getMetricLog() != null?param.getMetricLog():new MetricLog();

		if(NikonBusinessManager.listProductDetails.equals(methodName))
		{
			//If we've got a paramater object where the prod_id = NA then return an empty product and do some
			//logging
			if(NikonDomainConstants.NT_APPLCBL.equals(param.getProductId()))
			{
				log.warn(FormatUtils.mFormat("Encountered an instance of {0} with a prodId equal to {1}. This shouldn't get passed to method {2}", HBN8QueryParamDTO.class.getName(), NikonDomainConstants.NT_APPLCBL, NikonBusinessManager.listProductDetails));
				RequestContext lrc = param.getRequestContext();
				String requestURL  = lrc.getRequest().getRequestURL().toString();
				String requestURI  = lrc.getRequest().getRequestURI();
				String queryString = lrc.getRequest().getQueryString();
				log.info(FormatUtils.mFormat("requestURL :{0}", requestURL));
				log.info(FormatUtils.mFormat("requestURI :{0}", requestURI));
				log.info(FormatUtils.mFormat("queryString:{0}", queryString));
				return new ProductDTO();
			}
			
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
//			metric.recordTime(FormatUtils.mFormat("Object with key:{0} not found in cache region {1} proceeding to business manager impl", key, cacheRegion));
//			metric.indent();
			return arg0.proceed();
		}
		else
		{
//			metric.recordTime(FormatUtils.mFormat("Object with key:{0} found in cache region {1}.", key, cacheRegion));
//			metric.exdent();
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
//			metric.recordTime(FormatUtils.mFormat("Start looking in cache refion {1} for {0}", key, cacheRegion));

			log.info(FormatUtils.mFormat("Looking in cache region:{0} for key:{1}", cacheRegion, key));
			retVal = cache.get(key);

			//If we found our object the use it
			if(retVal != null)
			{
				log.info(FormatUtils.mFormat("Key:{0} found", key));
			}
//			metric.recordTime(FormatUtils.mFormat("End looking in cache for {0}", key));
			log.info(FormatUtils.mFormat("Returning a {0} null object from cache", retVal == null?"":"non"));
		}
		return retVal;
	}
}
