package com.interwoven.teamsite.nikon.externals;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.ParameterHash;
import com.interwoven.livesite.runtime.RequestContext;

public class NikonLiveSiteFeaturesExternalDelegate extends NikonLiveSiteBaseDelegate {

	private static final Logger logger = Logger.getLogger(NikonLiveSiteFeaturesExternalDelegate.class);
	
	/**
	 * Method to retrieve features explained XML for conversion
	 * @param  requestContext
	 * @return XMLDocument
	 * @throws UnsupportedEncodingException 
	 */
	public Document retrieveFeaturesExplained (RequestContext requestContext) throws UnsupportedEncodingException {
		
		logger.debug("Entering Document retrieveFeaturesExplained(RequestContext requestContext)");
		try {
			ParameterHash params = requestContext.getParameters();
			String pos = params.get("POS") == null ? "" : params.get("POS").toString();
			String prodId = params.get("ID") == null ? "" : params.get("ID").toString();
			logger.debug("pos: " + pos + ", prodId:" + prodId);
			if (!"".equals(pos) && !"".equals(prodId) && StringUtils.isNumeric(pos)){
				int posInt = Integer.valueOf(pos).intValue();
				NikonLiveSiteHBN8ExternalDelegate productDelegate = new NikonLiveSiteHBN8ExternalDelegate();
				Document productDoc = productDelegate.listProductDetails(requestContext);
				
				if (productDoc != null){
					Element root = productDoc.getRootElement();
					Element productDetails = (Element) root.selectSingleNode("//product_details");
					if (productDetails != null){
						Element featuresExplained = (Element) productDetails.selectSingleNode("//features_explained["+pos+"]/reference");
						if (featuresExplained != null){
							String xmlFile = featuresExplained.getText();
							if (xmlFile == null || xmlFile.trim().length() == 0) {
					         	logger.warn("XML Not Found");
					        }else{
					        	logger.debug("xmlFile " + xmlFile);
								Document xmlDoc = requestContext.getLiveSiteDal().readXmlFile(xmlFile);
								if (xmlDoc != null) {
									return xmlDoc;
								}		
					        }
						}
					}else{
						logger.debug("productDetails is null");
					}
				
				}else{
					logger.debug("Product Doc is null");
				}
				
			}
			/*
			ParameterHash params = requestContext.getParameters();
			
			String xmlFile = params.get("XML").toString();
	         
			if (xmlFile == null || xmlFile.trim().length() == 0) {
				
	         	logger.warn("XML Not Found");
	        }
	         
			Document xmlDoc = requestContext.getLiveSiteDal().readXmlFile(xmlFile);       
			
			if (xmlDoc != null) {
				
				return xmlDoc;
			}*/
			
		}catch(Exception e){
			logger.error("Exception", e);
		}
		
		return Dom4jUtils.newDocument("<XML/>");
	}
}
