package com.interwoven.teamsite.nikon.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.externals.NikonLiveSiteHBN8ExternalDelegate;

public class ProductDTOPageController {

	protected Log log = LogFactory.getLog(this.getClass());
	/**
	 * Method called via call from the page
	 * @param requestContext
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ForwardAction RetrieveProductDTO(RequestContext requestContext) 
	{
		String productId = requestContext.getParameterString("ID") + "";
		
		log.debug("RetrieveProductDTO() - Product Id: " + productId);
		
		if (!productId.equals("")) {
			
			NikonLiveSiteHBN8ExternalDelegate HBN8ExternalDelegate = new NikonLiveSiteHBN8ExternalDelegate();
			
			ProductDTO productDTO = HBN8ExternalDelegate.retrieveProductDTO(productId, requestContext);
			
			log.debug("RetrieveProductDTO() - Retrieved Product DTO (Locale) - " + productDTO.getNikonLocale());
			
			log.debug("RetrieveProductDTO() - Adding Product DTO to Scope Data - " + "prodDTO_" + productDTO.getNikonLocale() + "_" + productId);
			
			requestContext.getRequest().setAttribute("prodDTO_" + productDTO.getNikonLocale() + "_" + productId, productDTO);
		}
		
		return null;
	}
}
