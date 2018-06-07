package com.interwoven.teamsite.nikon.components;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;
import com.interwoven.teamsite.nikon.components.ComponentHelper.LocalisedDCRTO;

public class RetrieveRichNavContent {

	private static Log log = LogFactory.getLog(RetrieveRichNavContent.class);
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public Document retrieveNestedDCRs(RequestContext requestContext)
	{
		
		//-----------------------------------------------------------
		// Initialise Variables
		//-----------------------------------------------------------
		Document returnDocument = Dom4jUtils.newDocument();
		
		String localeRichNavigationDCR = requestContext.getParameterString("Source DCR");
		
		System.out.println("Locale Rich Navigation DCR: " + localeRichNavigationDCR);
		
		ComponentHelper ch = new ComponentHelper();
		
		String requestedLanguageCountryCode = LocaleResolver.getRequestedLanguageCountryCode(requestContext);
		
		LocalisedDCRTO lDCRTo = ch.localisedPathToDCR(requestContext, localeRichNavigationDCR, requestedLanguageCountryCode);
		
		String fullyLocalisedDCRPath = lDCRTo.getFullyLocalisedDCRPath();
	
		System.out.println("retrieveGlanceSpecs - localisedDCRPath" + fullyLocalisedDCRPath);
			
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
					System.out.println("Node Name: " + node.getName());
					
					String nodeDCRRelPath = node.getText();
						
					System.out.println("Node Text: " + nodeDCRRelPath);
						
					Document nodeDCRXML = retrieveSingleDCR(requestContext, ch, nodeDCRRelPath, requestedLanguageCountryCode);
						
					System.out.println("Node XML: " + nodeDCRXML);
					
					node.getParent().add(nodeDCRXML.getRootElement());
				}
				
				returnDocument.addElement("RichNavigationDCR").add(xml.getRootElement());
				
				is.close();
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
				returnDocument.addElement("Error").setText(e.toString());
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
	
		log.debug("localisedDCRPath" + fullyLocalisedDCRPath);
			
		File f = new File(fullyLocalisedDCRPath);
	
		if (f.exists() && f.isFile())
		{
			try {
				
				InputStream is = requestContext.getFileDal().getStream(fullyLocalisedDCRPath);
				
				xml = Dom4jUtils.newDocument(is);
				
				is.close();
				
			} catch (IOException e) {
				
				e.printStackTrace();
				
				xml.addElement("ERROR").setText(e.toString());
			}
		}
		
		return xml;
	}
}
