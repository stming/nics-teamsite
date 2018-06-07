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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;

public class NikonLiveSiteOtherNewsContentExternalDelegate extends NikonLiveSiteBaseDelegate {
	
	private static final Logger logger = Logger.getLogger(NikonLiveSiteOtherNewsContentExternalDelegate.class);
	
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
		
		String currentDCR = requestContext.getParameterString("ID");
		String localeLanguage = "";
		logger.debug("Current ID: " + currentDCR);
		
		//-----------------------------------------------------------
		// Retrieve TCEB DCR
		//-----------------------------------------------------------
		logger.debug("Source DCR = " + requestContext.getParameterString("Source DCR"));
		if ((requestContext.getParameterString("Source DCR") != null) && (!requestContext.getParameterString("Source DCR").equals("")))
		{
			String tcebFile = requestContext.getParameterString("Source DCR").toString();
			
			Document tcebDoc = requestContext.getLiveSiteDal().readXmlFile(tcebFile);
			
			if (tcebDoc != null)
			{ 
				//-----------------------------------------------------------
				// Determine if locale fallback is checked
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
				
				String pageSize = requestContext.getParameterString("Page Size");
				
				if (pageSize.equals("")) {
					
					pageSize = "10";
				}
				
				//-----------------------------------------------------------
				// Determine Locale
				//-----------------------------------------------------------
				localeLanguage = LocaleResolver.getRequestedLanguageCountryCode(requestContext); 

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
					String masterSolrQuery = buildSolrPrimaryQuery(true, categorySelected, pageSize, localeLanguage, currentDCR);
					logger.debug("masterSolrQuery = "+masterSolrQuery);
					
				
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
						String fallbackSolrQuery = buildSolrFallbackQuery(masterIds, locales, currentDCR);
					
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
					String localeSolrQuery = buildSolrPrimaryQuery(false, categorySelected, pageSize, localeLanguage, currentDCR);
					logger.debug("localeSolrQuery = "+localeSolrQuery);
				
					// Retrieve Results
					Document localeResults = querySolr(localeSolrQuery, "127.0.0.1");
					
					// Add Results To Return Document
					solrResults.getRootElement().addElement("SolrResults").add(localeResults.getRootElement());
				}
			}
		}
		solrResults.getRootElement().addElement("Category").setText(categorySelected);
		solrResults.getRootElement().addElement("locale").setText(localeLanguage);
		solrResults.getRootElement().addElement("iwpreview").setText("" + requestContext.isPreview());		
		return solrResults;
	}
	
	@SuppressWarnings("unchecked")
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
	
	
	private String buildSolrPrimaryQuery(boolean masterQuery, String categorySelected, String pageSize, String localeLanguage, String currentDCR) throws UnsupportedEncodingException {
		
		String query = "";
		String qQuery = "";
		
		qQuery += "tags_m_s:" + categorySelected;
		qQuery += "* AND !id:(" + currentDCR + ") ";
		
		if (masterQuery) {
		
			qQuery += "AND ((locale_s:en_Asia AND ";
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
		query += "&start=0";
		query += "&sort=" + URLEncoder.encode("date_made_d desc", "UTF-8");
		
		return query;
	}
	
	private String buildSolrFallbackQuery(Collection<String> ids, Collection<String> locales, String currentDCR) throws UnsupportedEncodingException {
		
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
		
		logger.debug("Solr URL: " + solrServerUrl + "Query: " + queryXML);
		
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
