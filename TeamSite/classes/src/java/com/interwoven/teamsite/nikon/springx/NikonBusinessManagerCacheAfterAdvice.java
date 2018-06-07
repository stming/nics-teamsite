package com.interwoven.teamsite.nikon.springx;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.springframework.aop.AfterReturningAdvice;

import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.businessrules.CacheKeyManager;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.dto.ProductDTO;

/**
 * Class responsible for the storing of objects in the cache after a certain method
 * has run. Implemented via the Spring configuration
 * @author nbamford
 *
 */
public class NikonBusinessManagerCacheAfterAdvice implements AfterReturningAdvice
{

	private Log log = LogFactory.getLog(NikonBusinessManagerCacheAfterAdvice.class);
	private HBN8QueryParamDTO param;
//	private MetricLog metric = new MetricLog();

	/* (non-Javadoc)
	 * @see org.springframework.aop.AfterReturningAdvice#afterReturning(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
	 */
	public void afterReturning(Object arg0, Method method, Object[] arg2, Object arg3) 
	throws Throwable {
		log.debug("Entering public void afterReturning(Object arg0, Method arg1, Object[] arg2, Object arg3)");
		String methodName = method.getName();
		param = (HBN8QueryParamDTO) arg2[0];
		//If we're sent a metric then use it otherwise use a default one. We're not going to use it anyway
//		metric = param.getMetricLog() != null?param.getMetricLog():metric; 

		if (arg0 == null){
			log.debug("Arg0 is null, will not cache for method " + methodName);
		}else{
			if(NikonBusinessManager.listProductDetails.equals(methodName))
			{
				log.debug(FormatUtils.mFormat("Caching after method {0}", methodName));

				ProductDTO prodDTO = (ProductDTO)arg0;
				if(!"".equals(prodDTO.getProdId()))
				{
					String key = CacheKeyManager.productdtoKey(param, method);
					storeInCache(NikonDomainConstants.JCS_REGION_PROD_DTO, key, prodDTO);
				}
			}
			else if(NikonBusinessManager.listNavCatProducts.equals(methodName))
			{
				log.debug(FormatUtils.mFormat("Caching after method {0}", methodName));
				//Only store at the lowest level for now
				List<ProductDTO> listOfProdDTO = (List<ProductDTO>)arg0;
				String key = CacheKeyManager.productdtoNavKey(param, method);
				storeInCache(NikonDomainConstants.JCS_REGION_PROD_DTO_NAV, key, listOfProdDTO);
			}
			else if(NikonBusinessManager.listCatalogueProducts.equals(methodName))
			{
				log.debug(FormatUtils.mFormat("Caching after method {0}", methodName));
				//Only store at the lowest level for now
				List<ProductDTO> listOfProdDTO = (List<ProductDTO>)arg0;
				String key = CacheKeyManager.productdtoCatKey(param, method);
				storeInCache(NikonDomainConstants.JCS_REGION_PROD_DTO_CAT, key, listOfProdDTO);
			}
			else if(NikonBusinessManager.listProductMetaDataFromDCRPath.equals(methodName)){
				log.debug(FormatUtils.mFormat("Caching after method {0}", methodName));
				ProductDTO prodDTO = (ProductDTO)arg0;
				if(!"".equals(prodDTO.getProdId()))
				{
					String key = CacheKeyManager.productdtoDCRPathKey(param, method);
					storeInCache(NikonDomainConstants.JCS_REGION_PROD_DTO, key, prodDTO);
				}
			}		
		}

		
		log.debug("Exiting public void afterReturning(Object arg0, Method arg1, Object[] arg2, Object arg3)");
	}

	
	/**
	 * Helper method to store an instance of productDTO in the cache
	 * @param param
	 * @param prodDTO
	 * @param method
	 */
	
	private void storeInCache(String cacheRegion, Object key, Object value)
	{
		log.debug("Entering private void storeCachedProductDTO(HBN8QueryParamDTO param, ProductDTO prodDTO)");
		//If we've enabled caching then store
		JCS cache = null;
		try 
		{
			cache = JCS.getInstance(cacheRegion);
		} 
		catch (CacheException e) {
			log.warn(e);
		}

		// RequestContext may be null when we are running outside LiveSite
		if (param.getRequestContext() != null){
		//Look in the cache if we're in runtime and we found the JCS
			if((param.getRequestContext().isRuntime()) && (cache != null))
			{
				try
				{
					cache.put(key, value);
				} 
				catch (CacheException e) 
				{
					log.warn("Unable to put item in cache");
				}
				log.info(FormatUtils.mFormat("Putting Object with key:{0} in cache region:{1}", key, cacheRegion));
			}
		}
		log.debug("Exiting private void storeCachedProductDTO(HBN8QueryParamDTO param, ProductDTO prodDTO)");
	}

}
