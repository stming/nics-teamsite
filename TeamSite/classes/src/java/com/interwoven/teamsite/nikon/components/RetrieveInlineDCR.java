package com.interwoven.teamsite.nikon.components;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;
import com.interwoven.teamsite.nikon.components.ComponentHelper.LocalisedDCRTO;

public class RetrieveInlineDCR {

	private static Log log = LogFactory.getLog(RetrieveInlineDCR.class);
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public Document retrieveDCR(RequestContext requestContext)
	{
		//-----------------------------------------------------------
		// Initialise Variables
		//-----------------------------------------------------------
		Document returnDocument = Dom4jUtils.newDocument();
		
		String nestedDCR = requestContext.getParameterString("Nested_DCR");
		
		log.debug("Nested DCR: " + nestedDCR);
		
		if ((nestedDCR != null) && (!nestedDCR.equals(""))) {
			
			ComponentHelper ch = new ComponentHelper();
			
			String requestedLanguageCountryCode = LocaleResolver.getRequestedLanguageCountryCode(requestContext);
			
			if (nestedDCR.contains("templatedata")){
				String[] pathArr = nestedDCR.split("/");
				requestedLanguageCountryCode = pathArr[1];
			}
			
			log.debug("Requested Language country/locale:" + requestedLanguageCountryCode);
			
			
			
			LocalisedDCRTO lDCRTo = ch.localisedPathToDCR(requestContext, nestedDCR, requestedLanguageCountryCode);
			
			String fullyLocalisedDCRPath = lDCRTo.getFullyLocalisedDCRPath();
		
			log.debug("Nested Localised DCR Path: " + fullyLocalisedDCRPath);
				
			File f = new File(fullyLocalisedDCRPath);
		
			Document xml = Dom4jUtils.newDocument();
			
			if (f.exists() && f.isFile())
			{
				try {
					
					InputStream is = requestContext.getFileDal().getStream(fullyLocalisedDCRPath);
					
					xml = Dom4jUtils.newDocument(is);
			
					List<Node> nodes = xml.getRootElement().selectNodes("//*[text()[contains(.,'templatedata')]]");
		    
					for (Node node : nodes)
					{
						log.debug("Node Name: " + node.getName());
						
						String nodeDCRRelPath = node.getText();
							
						log.debug("Node Text: " + nodeDCRRelPath);
							
						// Check for DCR and try to fix NPE
						if (nodeDCRRelPath.startsWith("/templatedata/")){							
							
							Document nodeDCRXML = retrieveSingleDCR(requestContext, ch, nodeDCRRelPath, requestedLanguageCountryCode);
								
							log.debug("Node XML: " + nodeDCRXML);
							
							if (nodeDCRXML != null) {
								
								node.getParent().addElement("inline_dcr").add(nodeDCRXML.getRootElement());
							}
						}
					
					}
					
					returnDocument.addElement("Nested_DCR").add(xml.getRootElement());
					
					is.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
					
					returnDocument.addElement("error").setText(e.toString());
				}
			}
		}
		
		return returnDocument;
	}
	
	@SuppressWarnings({ "deprecation" })
	public Document retrieveSingleDCR(RequestContext requestContext, ComponentHelper ch, String dcrName, String nikonLocale)
	{
		Document xml = Dom4jUtils.newDocument();
		
		LocalisedDCRTO lDCRTo = ch.localisedPathToDCR(requestContext, dcrName, nikonLocale);
		
		String fullyLocalisedDCRPath = lDCRTo.getFullyLocalisedDCRPath();
	
		log.debug("Nested Inline Localised DCR Path: " + fullyLocalisedDCRPath);
			
		File f = new File(fullyLocalisedDCRPath);
	
		if (f.exists() && f.isFile())
		{
			try {
				
				InputStream is = requestContext.getFileDal().getStream(fullyLocalisedDCRPath);
				
				xml = Dom4jUtils.newDocument(is);
				
				is.close();
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
				xml.addElement("error").setText(e.toString());
			}
		}
		
		return xml;
	}
}
