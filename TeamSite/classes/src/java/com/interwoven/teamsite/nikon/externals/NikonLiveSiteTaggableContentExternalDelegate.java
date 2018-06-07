package com.interwoven.teamsite.nikon.externals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;

public class NikonLiveSiteTaggableContentExternalDelegate extends NikonLiveSiteBaseDelegate {
	
	private static final Logger logger = Logger.getLogger(NikonLiveSiteTaggableContentExternalDelegate.class);
	private int pageSize = 10;
	
	/**
	 * Method to query SOLR for taggable content
	 * @param  requestContext
	 * @return XMLDocument
	 * @throws UnsupportedEncodingException 
	 */
	public Document retrieveTaggableContent (RequestContext requestContext) throws UnsupportedEncodingException {
		
		logger.debug("Entering Document retrieveTaggableContent(RequestContext requestContext)");
		Document doc = null;
		try {
			doc = _retrieveTaggableContent(requestContext);
		
		// Determine Pagination
		int totalResults = 0;
		int currentPage = 1;
		
		if (doc.getRootElement().selectSingleNode("SolrResults/response/result") != null) {
		
			totalResults = Integer.parseInt(doc.getRootElement().selectSingleNode("SolrResults/response/result/@numFound").getText());
		}
		
		if (doc.getRootElement().selectSingleNode("SolrResults/response/result") != null) {
			
			currentPage =  Integer.parseInt(doc.getRootElement().selectSingleNode("SolrResults/response/result/@start").getText());
		}
		
		int totalPages = (int) Math.ceil((double)totalResults / (double)pageSize);
		
		doc.getRootElement().addAttribute("totalResults", "" + totalResults);
		doc.getRootElement().addAttribute("totalPages", "" + totalPages);
		doc.getRootElement().addAttribute("currentPage", "" + currentPage / pageSize);

		} catch (Exception e){
			logger.error("Error in Taggable content", e);
			doc = Dom4jUtils.newDocument();
			StringWriter writer = new StringWriter();
			PrintWriter pWriter = new PrintWriter(writer);
			e.printStackTrace(pWriter);
		    doc.addElement("errors").addText(writer.toString());
		}
		return doc;
	}
	
	/**
	 * Method to build the XML response for taggable content based on given tagging categories
	 * @param  param
	 * @param  requestContext
	 * @return XMLDocument
	 * @throws UnsupportedEncodingException 
	 */
	@SuppressWarnings({ "unchecked" })
	private Document _retrieveTaggableContent(RequestContext requestContext) throws UnsupportedEncodingException
	{
		//-----------------------------------------------------------
		// Initialise Variables
		//-----------------------------------------------------------
		Document solrResults = Dom4jUtils.newDocument("<TaggableContent/>");
		boolean localeFallbackEnabled = false;
		
		String categorySelected = requestContext.getParameterString("Category");
		String sectionSelected = requestContext.getParameterString("Section");
		String localeLanguage = "";
		

		
		
		//------------------------------------------------------------------
		// Split Section Into Subsections
		//------------------------------------------------------------------
		List <String> subsections = new ArrayList<String>();
		
		if (sectionSelected.contains("/")) { 
			
			subsections = Arrays.asList(sectionSelected.split("/"));
			
			for (int i = 0; i < subsections.size(); i++) {
				
				if (subsections.get(i) != null) {
				
					solrResults.getRootElement().addElement("SelectedSection" + (i + 1)).setText(subsections.get(i));
				}
			}
		
		} else {
		
			solrResults.getRootElement().addElement("SelectedSection1").setText(sectionSelected);
		}
		
		//-----------------------------------------------------------
		// Retrieve Full Article DCR If ID Passed In
		//-----------------------------------------------------------
		if ((requestContext.getParameterString("ID") != null) && (!requestContext.getParameterString("ID").equals("")))
		{
			String dcrFile = requestContext.getParameterString("ID").toString();
			
			Document dcrDoc = null;
			if (dcrFile.startsWith("templatedata") && dcrFile.contains("taggable_content") && !dcrFile.contains("..")){
				dcrDoc = requestContext.getLiveSiteDal().readXmlFile(dcrFile);
			}else{
				logger.warn("Reject because it does not contains the correct path:" + dcrFile);
			}
			if (dcrDoc == null && requestContext.isPreview()){
				logger.debug("dcrDoc null and it is in preview (TeamSite), will try to look up in fallback path");
				FileDal fileDal = requestContext.getFileDal();
				String fallbackFilePath = fileDal.getRoot() + "/" + dcrFile;
				String[] pathArr = dcrFile.split("/");
				String locale = pathArr[1];
				String[] localeArr = locale.split("_");
				String lang = localeArr[0];
				String country = localeArr[1];
				fallbackFilePath = fallbackFilePath.replaceAll("(.*)/main/Nikon/([a-zA-Z]{2,4})/(.*)", "$1/main/Nikon/" + country + "/$3");
				logger.debug("fallbackFilePath:" + fallbackFilePath);
				dcrDoc = Dom4jUtils.newDocument(fileDal.getStream(fallbackFilePath));
			}
			if (dcrDoc != null){
				
				solrResults.getRootElement().addElement("ResultType").setText("full");
				solrResults.getRootElement().addElement("ContentDCR").add(dcrDoc.getRootElement());
			}else{
				logger.error("Failed to load ContentDCR from ID " + dcrFile);
			}
			if ((requestContext.getParameterString("Source DCR") != null) && (!requestContext.getParameterString("Source DCR").equals(""))) {
				
			
				String tcebFile = requestContext.getParameterString("Source DCR").toString();
			
				
				Document tcebDoc = null;
				
				if (tcebFile.startsWith("templatedata") && tcebFile.contains("taggable_content_explorer_block") && !tcebFile.contains("..")){		
					String[] pathArr = tcebFile.split("/");
					localeLanguage = ("".equals(pathArr[0])) ? pathArr[2] : pathArr[1];
					logger.debug(FormatUtils.mFormat("Using DCR path to get the locale requested:{0}", localeLanguage));
					tcebDoc = requestContext.getLiveSiteDal().readXmlFile(tcebFile);
				}else{
					logger.warn("Reject because it does not contains the correct path:" + tcebFile);
				}
				
				if (tcebDoc != null){
					solrResults.getRootElement().addElement("ExplorerDCR").add(tcebDoc.getRootElement());
				}else{
					logger.error("Failed to load ExplorerDCR from Source DCR" + tcebFile);
				}
				
			}
		}
		//-----------------------------------------------------------
		// Retrieve TCEB DCR
		//-----------------------------------------------------------
		else if ((requestContext.getParameterString("Source DCR") != null) && (!requestContext.getParameterString("Source DCR").equals("")))
		{
			String tcebFile = requestContext.getParameterString("Source DCR").toString();
			
			Document tcebDoc = null;
			if (tcebFile.startsWith("templatedata") && tcebFile.contains("taggable_content_explorer_block") && !tcebFile.contains("..")){		
				tcebDoc = requestContext.getLiveSiteDal().readXmlFile(tcebFile);
			}else{
				logger.warn("Reject because it does not contains the correct path:" + tcebFile);
			}
			
			if (tcebDoc != null){
				solrResults.getRootElement().addElement("ResultType").setText("list");
				solrResults.getRootElement().addElement("ExplorerDCR").add(tcebDoc.getRootElement());
			}else{
				logger.error("Failed to load ExplorerDCR from Source DCR" + tcebFile);
			}
			
			if (tcebDoc != null)
			{ 
				//-----------------------------------------------------------
				// Determine If Locale Fallback Is Checked
				//-----------------------------------------------------------
				if (tcebDoc.getRootElement().selectSingleNode("locale_fallback") != null) {
					
					localeFallbackEnabled = Boolean.valueOf(tcebDoc.getRootElement().selectSingleNode("locale_fallback").getText());
				}
				
				//-----------------------------------------------------------
				// Determine Category / Section / Page Size Based On Query String
				//-----------------------------------------------------------
				if (categorySelected.equals("")) {
					
					if (tcebDoc.getRootElement().selectSingleNode("identifier") != null) {
						
						categorySelected = tcebDoc.getRootElement().selectSingleNode("identifier").getText();
					}
					
				}
				
				pageSize = Integer.parseInt(requestContext.getParameterString("Page Size"));
				
				if (pageSize != 0) {
					
					if (tcebDoc.getRootElement().selectSingleNode("items_per_page") != null) {
					
						if (!tcebDoc.getRootElement().selectSingleNode("items_per_page").getText().equals("")) {
						
							pageSize = Integer.parseInt(tcebDoc.getRootElement().selectSingleNode("items_per_page").getText());
						
						} else {
							
							pageSize = 10;
						}
						
						
					} else {
					
						pageSize = 10;
					}
				}
				
				String pageOffset = requestContext.getParameterString("Page Offset");
				
				if (pageOffset.equals("")) {
					
					pageOffset = "0";
				}
				
				//-----------------------------------------------------------
				// Determine Locale
				//-----------------------------------------------------------
				localeLanguage = LocaleResolver.getRequestedLanguageCountryCode(requestContext); //tcebFile.replaceAll("^templatedata/(.*)/taggable_content_explorer_block/data/.*$", "$1");

				if (tcebFile.contains("templatedata")){
					String[] pathArr = tcebFile.split("/");
					localeLanguage = ("".equals(pathArr[0])) ? pathArr[2] : pathArr[1];
					logger.debug(FormatUtils.mFormat("Using DCR path to get the locale requested:{0}", localeLanguage));
				}
				
				
				//-----------------------------------------------------------
				// Retrieve Fallback Locales
				//-----------------------------------------------------------
				String[] countryLangArr = localeLanguage.split("_");
				Collection<String> locales = LocaleResolver.resolvePossibleLocales(requestContext);
				
				//-----------------------------------------------------------
				// Query Master (If Fallback Enabled)
				//-----------------------------------------------------------
				if (localeFallbackEnabled) {
					
					locales.remove("en_Asia");
					log.debug("Possible locales are (after remove en_Asia): " + locales);
					
					// Variables For Later Use
					LinkedList<String> masterIds = new LinkedList<String>();
					
					// Build Query
					String masterSolrQuery = buildSolrPrimaryQuery(true, categorySelected, sectionSelected, pageSize, pageOffset, localeLanguage);
				
					// Retrieve Results
					Document masterResults = querySolr(masterSolrQuery, "127.0.0.1");
					
					// Process Results
					if (masterResults.getRootElement().selectSingleNode("result") != null) {
						
						Node resultElement = masterResults.getRootElement().selectSingleNode("result");
						
						if (resultElement.selectSingleNode("doc") != null) {
							
							List <Node> docNodes = resultElement.selectNodes("doc");
		   				  
		   				  	for ( Node dn : docNodes )
		   				  	{
		   				  		if (dn.selectSingleNode("str") != null) {
		   				  	
		   				  			String id = dn.selectSingleNode("str[@name='id']").getText();
		   				  			String locale = dn.selectSingleNode("str[@name='locale_s']").getText();
		   				  			if ("en_Asia".equals(locale)){
		   				  				masterIds.add(id);
		   				  			}		   				  			
		   				  			masterIds.add(id);
		   				  		}
		   				  	}
						}
					}
					
					// If Results Exits, Query For Fallback
					if (masterIds.size() > 0 && !locales.isEmpty()) {
               		 	
						// Build Query
						String fallbackSolrQuery = buildSolrFallbackQuery(masterIds, locales);
					
						// Retrieve Results
						Document fallbackResults = querySolr(fallbackSolrQuery, "127.0.0.1");
						
						// Process Results For Master Versus Locale
						Document combinedResults = processFallbackResults(masterResults, fallbackResults, locales);
								
						// Add Results To Return Document
						solrResults.getRootElement().addElement("SolrResults").add(combinedResults.getRootElement());
					
					} else {
						
						// Add Results To Return Document
						solrResults.getRootElement().addElement("SolrResults").add(masterResults.getRootElement());
						
					}
				} 
				//-----------------------------------------------------------
				// Query Locale (If Fallback Not Enabled)
				//-----------------------------------------------------------
				else {
					
					// Build Query
					String localeSolrQuery = buildSolrPrimaryQuery(false, categorySelected, sectionSelected, pageSize, pageOffset, localeLanguage);
				
					// Retrieve Results
					Document localeResults = querySolr(localeSolrQuery, "127.0.0.1");
					
					// Add Results To Return Document
					solrResults.getRootElement().addElement("SolrResults").add(localeResults.getRootElement());
				}
			}
		}
		solrResults.getRootElement().addElement("Section").setText(sectionSelected);
		solrResults.getRootElement().addElement("Category").setText(categorySelected);
		solrResults.getRootElement().addElement("locale").setText(localeLanguage);
		solrResults.getRootElement().addElement("iwpreview").setText("" + requestContext.isPreview());
		return solrResults;
	}
	
	private Document processFallbackResults(Document masterDoc, Document localeDoc, Collection<String> possibleLocales) {

		Node mResultNode = masterDoc.getRootElement().selectSingleNode("result");
		Element mResultElement = (Element) mResultNode;
		
		if (mResultElement.selectSingleNode("doc") != null) {
		
			//List <Node> mDocNodes = mResultElement.selectNodes("doc");
			List <Node> mDocNodes = mResultElement.content();
			  
			for ( int i=0;i<mDocNodes.size();i++ )
			{
				Node mdn = mDocNodes.get(i);
				if ("doc".equals(mdn.getName())){
					if (mdn.selectSingleNode("str[@name='id']") != null) {
	   				  	
			  			String id = mdn.selectSingleNode("str[@name='id']").getText();
			  			
			  			String locale = mdn.selectSingleNode("str[@name='locale_s']").getText();
			  			
			  			logger.debug("Master Document found the following: " + id + ", locale: " + locale);
			  			Element localeElement = null;
			  			if ("en_Asia".equals(locale)){
			  				localeElement = extractLocaleXML(id, localeDoc, possibleLocales);
			  			}
			  			if (localeElement != null) {
			  				
			  				mdn.detach();
			  				mDocNodes.add(i, localeElement);
			  				//mResultElement.add(localeElement);
			  			}
					}					
				}

			}
		}
		
		return masterDoc;
	}
	
	@SuppressWarnings("unchecked")
	private Element extractLocaleXML(String mid, Document localeDoc, Collection<String> possibleLocales) {
		
		Element retElement = null;
		
		Node lResultNode = localeDoc.getRootElement().selectSingleNode("result");
		Element lResultElement = (Element) lResultNode;
		
		String dcrPath = mid.replaceAll("templatedata/en_Asia/(.*)", "$1");
		
		for(String langCountryCode: possibleLocales) {
			logger.debug("Looking into locale document for [" + dcrPath + "] contains [" + langCountryCode + "]");
			boolean found = false;
			if (lResultElement.selectSingleNode("doc") != null) {
				
				List <Node> lDocNodes = lResultElement.selectNodes("doc");
				
				for ( Node ldn : lDocNodes )
				{
					if (ldn.selectSingleNode("str[@name='id']") != null) {
	   				  	
			  			String lid = ldn.selectSingleNode("str[@name='id']").getText();
			  			String lLocale = ldn.selectSingleNode("str[@name='locale_s']").getText();
			  			if (lid.contains(dcrPath) && langCountryCode.equals(lLocale)) {
			  				logger.debug("Found id : [" + lid + "] for fallback, breaking");
			  				
			  				retElement = (Element)ldn.detach();
			  				found = true;
			  				break;
			  			}
					}
				}
			}
			if (found)
				break;
		}

		
		return retElement;
	}
	
	private String buildSolrPrimaryQuery(boolean masterQuery, String categorySelected, String sectionSelected, int pageSize, String pageOffset, String localeLanguage) throws UnsupportedEncodingException {
		
		String query = "";
		String qQuery = "";
		
		qQuery += "tags_m_s:" + categorySelected;
		
		if (!sectionSelected.equals("")) {
			
			qQuery += "/" + sectionSelected + " AND ";
		}
		else {
			
			qQuery += "* AND ";
		}
		
		if (masterQuery) {
		
			qQuery += "((locale_s:en_Asia AND ";
			qQuery += "!locale_optout_m_s:" + localeLanguage;
			if (!"en_Asia".equals(localeLanguage)){
				qQuery += " AND !local_article_s:true";
			}
			qQuery += ") OR ";
			qQuery += "(locale_s:" + localeLanguage + " AND local_article_s:true))";
			
		} else {
		
			qQuery += "locale_s:" + localeLanguage;
		}
		
		query += "?q=(" + URLEncoder.encode(qQuery, "UTF-8") + ")";
		query += "&rows=" + pageSize;
		query += "&start=" + (Integer.parseInt(pageOffset) * pageSize);
		query += "&sort=" + URLEncoder.encode("date_made_d desc", "UTF-8");

		
		return query;
	}
	
	private String buildSolrFallbackQuery(Collection<String> ids, Collection<String> locales) throws UnsupportedEncodingException {
		
		String query = "";
		String qQuery = "";;
		
		// Add Ids To Query
		Iterator<String> itrIds = ids.iterator();

		while (itrIds.hasNext()) {
			
			qQuery += "id:(" + itrIds.next().replace("/en_Asia/","/*/") + ")";
			
			if (itrIds.hasNext()) {
				
				qQuery += " OR ";
			}
		}
		
		// Add Possible Locales To Query
		Iterator<String> itrLocales = locales.iterator();
		
		String localeFallbackQuery = "locale_s:(";
		
		boolean hasLocale = false;
		


		while (itrLocales.hasNext()) {
			
			String locale = itrLocales.next();
			if (!"en_Asia".equals(locale)) {
				hasLocale = true;
				localeFallbackQuery += locale;
				
				if (itrLocales.hasNext()) {
					
					localeFallbackQuery += " OR ";
				}
			}
		}
		
		if (hasLocale){
			localeFallbackQuery += ")";
		}
		
		query += "?q=(" + URLEncoder.encode(qQuery, "UTF-8") + ")";
		if (hasLocale){
			query += URLEncoder.encode(" AND " + localeFallbackQuery, "UTF-8");
		}
		
		query += "&sort=" + URLEncoder.encode("date_made_d desc", "UTF-8");
		
		return query;
	}
	
	private Document querySolr(String queryXML, String serverUrl) {
		
		Document results = null;
		
		String solrServerUrl = "http://" + serverUrl + ":8983/solr/common_content/select/";
		
		logger.debug("Solr URL: " + solrServerUrl + ", Query: " + queryXML);
		
		try {
			
			URL urlForInfWebSvc = new URL(solrServerUrl + queryXML);

            URLConnection UrlConnInfWebSvc = urlForInfWebSvc.openConnection();
            
            HttpURLConnection httpUrlConnInfWebSvc = (HttpURLConnection) UrlConnInfWebSvc;
			            httpUrlConnInfWebSvc.setDoOutput( true );
			            httpUrlConnInfWebSvc.setRequestProperty( "Content-Type", "application/xml; charset=utf-8" );
			            httpUrlConnInfWebSvc.setRequestProperty( "Accept", "application/xml; charset=utf-8" );
			
		    BufferedReader infWebSvcReplyReader = new BufferedReader( new InputStreamReader( httpUrlConnInfWebSvc.getInputStream(), "UTF8" ) );
            
            String line;
            String infWebSvcReplyString = "";
            
            while (( line = infWebSvcReplyReader.readLine() ) != null )
            {
                infWebSvcReplyString = infWebSvcReplyString.concat( line );
            }

            infWebSvcReplyReader.close();
            httpUrlConnInfWebSvc.disconnect();
		
		
			results = Dom4jUtils.newDocument( infWebSvcReplyString );
			results.setXMLEncoding( "UTF-8" );
		
		}
		catch (MalformedURLException e) {
			
			System.out.println( "Error has occurred: " + e.toString() );
		
		} catch (IOException e) {

			System.out.println( "Error has occurred: " + e.toString() );
		}
		
		return results;
	}
}
