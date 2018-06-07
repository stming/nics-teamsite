package com.interwoven.teamsite.nikon.externals;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.file.FileDALIfc;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.components.ComponentHelper;

/**
 *
 * @author epayne
 */
@SuppressWarnings("deprecation")
public class HeaderBuilder {

	private Log log = LogFactory.getLog(HeaderBuilder.class);
	
	ComponentHelper ch = new ComponentHelper();
    
	public HeaderBuilder() {
    }

    @SuppressWarnings("unchecked")
	public Document buildHeader(RequestContext context) throws DocumentException {

        // get the DCR and inject it into the returned XML
        Document doc = DocumentFactory.getInstance().createDocument("<staticcontent/>");

        Document doc1;
		
        try {
		
			doc1 = ch.getLocalisedDCR(context, NikonDomainConstants.HDR_DCR);
			doc = doc1;
		
		} catch (Exception e) {
			
			log.error(e.getMessage());
			Element errorEl = DocumentFactory.getInstance().createElement("error");
			
			errorEl.setText(e.getMessage());
			doc = DocumentFactory.getInstance().createDocument();
			doc.setRootElement(DocumentFactory.getInstance().createElement("staticcontent"));
			log.debug(FormatUtils.prettyPrint(doc));
			doc.getRootElement().add(errorEl);
		}

		//---------------------------------------------------------------------------------
		// Home Page Title
		//---------------------------------------------------------------------------------
		
		// Current Page URL
		String currentPageURL = context.getRequest().getRequestURI();
			   currentPageURL = currentPageURL.replaceAll("^/iw-preview(.*)$", "$1");
		
		// If A Home Page
		if ((currentPageURL.contains("home.page")) && (!currentPageURL.matches(".*(_N1|_N1/).*"))) {
			
			String langValue = null;
			String countryValue = null;
			
			//get lang from session
			Cookie tcookie = context.getCookies().getCookie("langCookie");

			if (tcookie != null) {
				
				langValue = tcookie.getValue().toString();
			}
			else
			{
				langValue = NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY;
			}
			
			countryValue = langValue;
			countryValue = countryValue.replaceAll(".*_(.*)", "$1");
			
			FileDALIfc fileDal = context.getFileDAL();

			boolean titleFallback = false;
						
			// Initialize The Breadcrumb Array
			ArrayList <String> breadCrumb = new ArrayList<String>();

			log.debug("Country Code: " + langValue);

			String dcrPath = fileDal.getRoot() + "/templatedata/" + langValue + "/page_titles/data/page_titles";
			
			String fallbackDCRPath = fileDal.getRoot() + "/templatedata/en_Asia/page_titles/data/page_titles";

			if (context.isPreview()) {
				
				fallbackDCRPath = fallbackDCRPath.replaceAll("^(.*)/" + countryValue + "/(.*)$", "$1/en_Asia/$2");
				
				log.debug("Fallback DCR: " + fallbackDCRPath);
			}
			
			java.io.InputStream is = null;
			Document localisedTitles = null;
			
			try {
			
				if (fileDal.exists(dcrPath)) {
				
					// Get Localised Page Title DCR
					is = fileDal.getStream(dcrPath);
				
				}
				else {
					
					is = fileDal.getStream(fallbackDCRPath);
					
					titleFallback = true;
				}

				localisedTitles = Dom4jUtils.newDocument(is);

			} catch (Exception e) {
				
				log.debug("Error: " + e.toString());

			}
			
			// Get NSO Site Title
			String nsoSiteTitle = localisedTitles.selectSingleNode("/page_titles/nso_title").getText();

			log.debug("NSO Site Title: " + nsoSiteTitle);
			
			breadCrumb.add(nsoSiteTitle);

			// Get All Titles
			List<Node> pageTitleList = localisedTitles.selectNodes("/page_titles/titles");

			log.debug("Current Page URL: " + currentPageURL);

			// Loop Through Titles
			for(Node pageNode : pageTitleList)
			{
				Node pageURLNode = pageNode.selectSingleNode("page");
				String pageURL   = pageURLNode.getText();
				
				if (titleFallback) {
					
					pageURL = pageURL.replaceAll("en_Asia", langValue);
				}

				log.debug("Loop Page URL: " + pageURL);

				// If We Match On Current URL
				if (pageURL.equals(currentPageURL)) {

					Node pageTitleNode = pageNode.selectSingleNode("title");
					String localisedPageTitle = pageTitleNode.getText();

					log.debug("Match Page Title: " + localisedPageTitle);
					
					breadCrumb.add(localisedPageTitle);
				}
			}
			
			String pageTitle = "";
			
			// Output Breadcrumb
			for ( String bcPart : breadCrumb )
			{
				if ( ! pageTitle.equals("") )
				{
					pageTitle += " - ";
				}
				pageTitle += bcPart;
			}		

			// output this for debug
			doc.getRootElement().addElement("pageTitle").addText(pageTitle);
			
			// now set the title
			context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, pageTitle);
		
		}
		
        // return the XML to the component
        return doc;
    }
}
