package com.interwoven.teamsite.nikon.solr.commoncontent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.livesite.dom4j.Dom4jUtils;

public class GenerateSolrXMLImpl implements GenerateSolrXML {

	private static final Log logger = LogFactory.getLog(GenerateSolrXMLImpl.class);
	
	public String generate(CSClient client, String locale, String filePath) {
	
		String localeInputXML = "";
		String outputXML = "";
		
		try 
		{
			// Locale DCR
			CSVPath csVpath = new CSVPath(filePath);
			CSSimpleFile localeFile = (CSSimpleFile) client.getFile(csVpath);
			
			logger.info("Locale DCR: " + filePath);
			
			if ((localeFile != null) && 
				(localeFile.isValid()) && 
				(localeFile.getKind() == CSSimpleFile.KIND)) {
				
				BufferedInputStream in = null;
				Reader reader = null;
				BufferedReader bReader = null;
					
				// Locale DCR
				try {
						
					in = localeFile.getBufferedInputStream(false);
					reader = new InputStreamReader(in, "UTF-8");
					bReader = new BufferedReader(reader);
					
					StringBuffer inputString = new StringBuffer();

					String thisLine;
					
					while ((thisLine = bReader.readLine()) != null) {
					    inputString.append(thisLine + "\n");
					}
					
					localeInputXML = inputString.toString();
						
				} catch (IOException ex) {
					ex.printStackTrace();
			    } finally {
			            
			        	try {
			                if (in != null)
			                    in.close();
			            } catch (IOException ex) {
			                ex.printStackTrace();
			            }
			    }
					
				Document solrXML = retrieveData(localeFile, localeFile.getVPath().getAreaRelativePath().toString(), locale, localeInputXML);
		        	     solrXML.setXMLEncoding("UTF-8");
		        	 
		        outputXML = solrXML.getRootElement().selectSingleNode("add/doc").asXML();
		}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
					
		return outputXML;
	}
	
	@SuppressWarnings("unchecked")
	private Document retrieveData(CSSimpleFile dcrFile, String dcrRelPath, String locale, String localeXML) throws ParseException {

		// Create Output Document
		Document solrXML = Dom4jUtils.newDocument("<update/>");
	    		 solrXML.setXMLEncoding("UTF-8");
	    		 solrXML.getRootElement().addAttribute("type", "COMMON_CONTENT");
			    		 
	    // Parse Locale DCR Document
	    Document lProductXML = Dom4jUtils.newDocument(localeXML);
	    Element lRootElement = lProductXML.getRootElement();
	
	    // DCR Variables
	    String contentTitle = null;
	    String contentSubtitle = null;
	    String contentPublishDate = null;
	    String contentLocation = null;
	    String contentSummary = null;
	    String contentThumbnail = null;
	    String contentIsNews = null;
	    String contentSticky = null;
	    String contentLocalArticle = "false";
	    String contentExtURL = null;
	    String contentExtTarget = null;
	    
	    // Metadata Variables
	    String metaWWADate = null;
	    String metaDateMade = null;
	    
	    // Show By Quarter (News)
	    String contentQuarter = null;
	    String contentYear = null;
	    
	    //==================================================================
  		// Process Metadata Fields
  		//==================================================================
  	    try {
  			
  			if ((dcrFile != null) && 
  				(dcrFile.isValid()) && 
  				(dcrFile.getKind() == CSSimpleFile.KIND)) {
  			
  				    CSExtendedAttribute[] extAttrs = dcrFile.getExtendedAttributes(null);
  									
  					for (int i = 0; i < extAttrs.length; i++) {
  						
  						String name = extAttrs[i].name;
  						String value = extAttrs[i].value;
  						
  						if (name.equals("TeamSite/Metadata/prod_wwa_date") && value != null && !"".equals(value)) {
  							
  							metaWWADate = value;
  							
  							TimeZone hkt = TimeZone.getTimeZone("HKT");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											 sdf.setTimeZone(hkt);
							java.util.Date metaDate = sdf.parse(metaWWADate);
							
							TimeZone utc = TimeZone.getTimeZone("UTC");
							SimpleDateFormat sdfOut = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
											 sdfOut.setTimeZone(utc);
										 							
							String wwaDateXML = sdfOut.format(metaDate);
								
							metaWWADate = wwaDateXML.toString();
  						
  						} else if (name.equals("TeamSite/Metadata/DateMade")) {
  							
  							metaDateMade = value;
  							
  							TimeZone hkt = TimeZone.getTimeZone("HKT");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											 sdf.setTimeZone(hkt);
							java.util.Date metaDate = sdf.parse(metaDateMade);
							
							TimeZone utc = TimeZone.getTimeZone("UTC");
							SimpleDateFormat sdfOut = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
											 sdfOut.setTimeZone(utc);
											 
							String metaDateMadeXML = sdfOut.format(metaDate);
								
							metaDateMade = metaDateMadeXML.toString();
							
							SimpleDateFormat sdfYear = new SimpleDateFormat( "yyyy" );
							sdfYear.setTimeZone(utc);
							 
							SimpleDateFormat sdfMonth = new SimpleDateFormat( "MM" );
							sdfMonth.setTimeZone(utc);
							 
							String monthDateXML = sdfMonth.format(metaDate);
							String yearDateXML = sdfYear.format(metaDate);
								
							
							if ((monthDateXML.toString().equals("01")) || (monthDateXML.toString().equals("02")) || (monthDateXML.toString().equals("03"))) {
								
								contentQuarter = "Q1";
							
							} else if ((monthDateXML.toString().equals("04")) || (monthDateXML.toString().equals("05")) || (monthDateXML.toString().equals("06"))) {
								
								contentQuarter = "Q2";
							
							} else if ((monthDateXML.toString().equals("07")) || (monthDateXML.toString().equals("08")) || (monthDateXML.toString().equals("09"))) {
								
								contentQuarter = "Q3";
							
							} else if ((monthDateXML.toString().equals("10")) || (monthDateXML.toString().equals("11")) || (monthDateXML.toString().equals("12"))) {
								
								contentQuarter = "Q4";
							}
							
							contentYear = yearDateXML.toString();							
							
  						}
  					}
  			}
  			else {
  											
  				logger.info("ERROR Missing DCR: " + dcrFile.getVPath().toString());
  			}
  						
  		} catch (NullPointerException e) {
  			e.printStackTrace();
  		} catch (CSAuthorizationException e) {
  			e.printStackTrace();
  		} catch (CSExpiredSessionException e) {
  			e.printStackTrace();
  		} catch (CSRemoteException e) {
  			e.printStackTrace();
  		} catch (CSException e) {
  			e.printStackTrace();;
  		}
	  	    
  	    //==================================================================
  		// Process DCR Fields
  		//==================================================================
	    if (lRootElement.selectSingleNode("title") != null) {
	    	
	    	contentTitle = lRootElement.selectSingleNode("title").getText();
	    }
	    
	    if (lRootElement.selectSingleNode("subtitle") != null) {
	    	
	    	contentSubtitle = lRootElement.selectSingleNode("subtitle").getText();
	    }
	    
	    if (lRootElement.selectSingleNode("date") != null) {
	    	
	    	contentPublishDate = lRootElement.selectSingleNode("date").getText();
	    }
	    
	    if (lRootElement.selectSingleNode("location") != null) {
	    	
	    	contentLocation = lRootElement.selectSingleNode("location").getText();
	    }
 
	    if (lRootElement.selectSingleNode("summary") != null) {
	    	
	    	contentSummary = lRootElement.selectSingleNode("summary").getText();
	    }
	    
	    if (lRootElement.selectSingleNode("thumbnail_image") != null) {
	    	
	    	contentThumbnail = lRootElement.selectSingleNode("thumbnail_image").getText();
	    }
	    
	    if (lRootElement.selectSingleNode("isNews") != null) {
	    	
	    	contentIsNews = lRootElement.selectSingleNode("isNews").getText();
	    }

	    if (lRootElement.selectSingleNode("sticky") != null) {
	    	
	    	contentSticky = lRootElement.selectSingleNode("sticky").getText();
	    }
	    
	    if (lRootElement.selectSingleNode("local_article") != null) {
	    	
	    	contentLocalArticle = lRootElement.selectSingleNode("local_article").getText();
	    }
	    
	    if (lRootElement.selectSingleNode("link") != null) {
	    	
	    	contentExtURL = lRootElement.selectSingleNode("link").getText();
	    }

	    if (lRootElement.selectSingleNode("target") != null) {
	    	
	    	contentExtTarget = lRootElement.selectSingleNode("target").getText();
	    }

	    //==================================================================
	    // Output SOLR XML Fields
	    //==================================================================
	    Element solrAddElement = solrXML.getRootElement().addElement("add");
	    Element solrDocElement = solrAddElement.addElement("doc");
	  		    solrDocElement.addAttribute("boost", "1.0");
	  		
  		//------------------------------------------------------------------
  		// Product Id (If Does Not Exist... Do Not Output Anything)
  		//------------------------------------------------------------------
  	    if ((dcrRelPath != null) && (!dcrRelPath.equals("")))	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "id").setText(dcrRelPath);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Locale
  		//------------------------------------------------------------------
  	    if (locale != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "locale_s").setText(locale);
  	    	solrDocElement.addElement("field").addAttribute("name", "locale_ss").setText(locale);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Title
  		//------------------------------------------------------------------
  	    if (contentTitle != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "title_s").setText(contentTitle);
  	    	solrDocElement.addElement("field").addAttribute("name", "title_ss").setText(contentTitle);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Subtitle
  		//------------------------------------------------------------------
  	    if (contentSubtitle != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "subtitle_s").setText(contentSubtitle);
  	    	solrDocElement.addElement("field").addAttribute("name", "subtitle_ss").setText(contentSubtitle);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Publish Date
  		//------------------------------------------------------------------
  	    if (contentPublishDate != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "publish_date_s").setText(contentPublishDate);
  	    	solrDocElement.addElement("field").addAttribute("name", "publish_date_ss").setText(contentPublishDate);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Date Made
  		//------------------------------------------------------------------
  	    if (metaDateMade != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "date_made_d").setText(metaDateMade);
  		}
  	    
  	    //----------------------------------------
       	// WWA Date
       	//----------------------------------------
		if ((metaWWADate != null) && (!metaWWADate.equals("")))
		{
			solrDocElement.addElement("field").addAttribute("name", "pub_d").setText(metaWWADate);	
		}
		
		//----------------------------------------
       	// Content Year
       	//----------------------------------------
		if ((contentYear != null) && (!contentYear.equals("")))
		{
			solrDocElement.addElement("field").addAttribute("name", "year_s").setText(contentYear);
			solrDocElement.addElement("field").addAttribute("name", "year_ss").setText(contentYear);
		}
		
		//----------------------------------------
       	// Content Quarter
       	//----------------------------------------
		if ((contentQuarter!= null) && (!contentQuarter.equals("")))
		{
			solrDocElement.addElement("field").addAttribute("name", "quarter_s").setText(contentQuarter);
			solrDocElement.addElement("field").addAttribute("name", "quarter_ss").setText(contentQuarter);
		}
  	    
  	    //------------------------------------------------------------------
  		// Location
  		//------------------------------------------------------------------
  	    if (contentLocation != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "location_s").setText(contentLocation);
  	    	solrDocElement.addElement("field").addAttribute("name", "location_ss").setText(contentLocation);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Summary
  		//------------------------------------------------------------------
  	    if (contentSummary != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "summary_s").setText(contentSummary);
  	    	solrDocElement.addElement("field").addAttribute("name", "summary_ss").setText(contentSummary);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Thumbnail Image
  		//------------------------------------------------------------------
  	    if (contentThumbnail != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "thumbnail_image_s").setText(contentThumbnail);
  	    	solrDocElement.addElement("field").addAttribute("name", "thumbnail_image_ss").setText(contentThumbnail);
  		}
  	    
  	    //------------------------------------------------------------------
  		// Sticky
  		//------------------------------------------------------------------
  	    if (contentSticky != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "sticky_s").setText(contentSticky);
  	    	solrDocElement.addElement("field").addAttribute("name", "sticky_ss").setText(contentSticky);
  		}

  	    //------------------------------------------------------------------
  		// Local Article
  		//------------------------------------------------------------------
  	    if (contentLocalArticle != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "local_article_s").setText(contentLocalArticle);
  	    	solrDocElement.addElement("field").addAttribute("name", "local_article_ss").setText(contentLocalArticle);
  		}

  	    //------------------------------------------------------------------
  		// External URL
  		//------------------------------------------------------------------
  	    if (contentExtURL != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "ext_url_s").setText(contentExtURL);
  	    	solrDocElement.addElement("field").addAttribute("name", "ext_url_ss").setText(contentExtURL);
  		}

  	    //------------------------------------------------------------------
  		// External Target
  		//------------------------------------------------------------------
  	    if (contentExtTarget != null)	
  		{
  	    	solrDocElement.addElement("field").addAttribute("name", "ext_target_s").setText(contentExtTarget);
  	    	solrDocElement.addElement("field").addAttribute("name", "ext_target_ss").setText(contentExtTarget);
  		}

  	    
  	    //------------------------------------------------------------------
  	    // Locale Opt-out
  	    //------------------------------------------------------------------
  	    if ((lRootElement.selectSingleNode("locale_optout") != null) && 
  		   (!lRootElement.selectSingleNode("locale_optout/locale").getText().equals(""))) {
  		  
			  List <Node> localeNodes = lRootElement.selectNodes("locale_optout");
			  
			  if (localeNodes != null) {
				  
				  for ( Node n : localeNodes ) {
					
					  if (n.selectSingleNode("locale") != null) {
			  
						  String contentLocaleOptOut = n.selectSingleNode("locale").getText();
			  
						  solrDocElement.addElement("field").addAttribute("name", "locale_optout_m_s").setText(contentLocaleOptOut);
						  solrDocElement.addElement("field").addAttribute("name", "locale_optout_m_ss").setText(contentLocaleOptOut);
						  
					  }
				  }
			  }
  	    }
  	    
  	    //------------------------------------------------------------------
  	    // Tagging
  	    //------------------------------------------------------------------
  	    if ((lRootElement.selectSingleNode("tagging") != null) && 
  		   (!lRootElement.selectSingleNode("tagging/category").getText().equals(""))) {
  		  
			  List <Node> taggingNodes = lRootElement.selectNodes("tagging");
			  
			  if (taggingNodes != null) {
				  
				  for ( Node n : taggingNodes ) {
					
					  if (n.selectSingleNode("category") != null) {
			  
						  String contentTagging = n.selectSingleNode("category").getText();
						  
						  if ((n.selectSingleNode("section_one") != null) && (!n.selectSingleNode("section_one").getText().equals(""))) {
							  
							  contentTagging += "/" + n.selectSingleNode("section_one").getText();
						  }
						  
						  if ((n.selectSingleNode("section_two") != null) && (!n.selectSingleNode("section_two").getText().equals(""))) {
							  
							  
							  contentTagging += "/" + n.selectSingleNode("section_two").getText();
						  }
						  
						  if ((n.selectSingleNode("section_three") != null) && (!n.selectSingleNode("section_three").getText().equals(""))) {
							  
							  
							  contentTagging += "/" + n.selectSingleNode("section_three").getText();
						  }
			  
						  solrDocElement.addElement("field").addAttribute("name", "tags_m_s").setText(contentTagging);
						  solrDocElement.addElement("field").addAttribute("name", "tags_m_ss").setText(contentTagging);
						  
						  //modified by bchen 20130620
						  //if this content is news, add year and quarter tag
						  if ((contentIsNews.equals("true"))&&(contentTagging.endsWith("_news"))) {
					  	    	
					  	    	//solrDocElement.addElement("field").addAttribute("name", "tags_m_s").setText("news");
								//solrDocElement.addElement("field").addAttribute("name", "tags_m_ss").setText("news");
								
					  	    	//String yearTagging =  "news" + "/" + contentYear; 
							  	String yearTagging =  contentTagging + "/" + contentYear;
					  	    	
					  	    	solrDocElement.addElement("field").addAttribute("name", "tags_m_s").setText(yearTagging);
								solrDocElement.addElement("field").addAttribute("name", "tags_m_ss").setText(yearTagging);
								
					  	    	//String quarterTagging =  "news" + "/" + contentYear + "/" + contentQuarter;
					  	    	String quarterTagging =  contentTagging + "/" + contentYear + "/" + contentQuarter;
					  	    	
					  	    	solrDocElement.addElement("field").addAttribute("name", "tags_m_s").setText(quarterTagging );
								solrDocElement.addElement("field").addAttribute("name", "tags_m_ss").setText(quarterTagging );
						  }
					  }
				  }
			  }
  	    }
  	    
	    
	    return solrXML;
	}

}
