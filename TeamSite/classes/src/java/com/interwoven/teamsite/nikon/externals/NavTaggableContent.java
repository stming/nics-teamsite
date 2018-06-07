package com.interwoven.teamsite.nikon.externals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.components.ComponentHelper;

// Comment

public class NavTaggableContent {

	private static final Logger logger = Logger.getLogger(NavTaggableContent.class);

	private ComponentHelper componentHelper = new ComponentHelper();

	@SuppressWarnings("unchecked")
	public Document constructNavTaggableContent(RequestContext requestContext) throws DocumentException {
		
		logger.debug("Entering Document constructNavTaggableContent(RequestContext requestContext)");
		
		ArrayList <String> title = new ArrayList<String>();

		Document doc  = componentHelper.getLocalisedDCR(requestContext, "Source DCR");
		
		String tcebFile = requestContext.getParameterString("Source DCR").toString();
		
		String localeLanguage = "";
		if (tcebFile.contains("templatedata")){
			String[] pathArr = tcebFile.split("/");
			localeLanguage = ("".equals(pathArr[0])) ? pathArr[2] : pathArr[1];
			logger.debug(FormatUtils.mFormat("Using DCR path to get the locale requested:{0}", localeLanguage));
		}		
		
		String categorySelected = requestContext.getParameterString("Category");
		String sectionSelected = requestContext.getParameterString("Section");
		
		doc.getRootElement().addElement("SelectedCategory").setText(categorySelected);
		doc.getRootElement().addElement("locale").setText(localeLanguage);
		doc.getRootElement().addElement("iwpreview").setText("" + requestContext.isPreview());
		
		//------------------------------------------------------------------
		// Split Section Into Subsections
		//------------------------------------------------------------------
		List <String> subsections = new ArrayList<String>();
		
		if (sectionSelected.contains("/")) { 
			
			subsections = Arrays.asList(sectionSelected.split("/"));
			
			for (int i = 0; i < subsections.size(); i++) {
				
				if (subsections.get(i) != null) {
				
					doc.getRootElement().addElement("SelectedSection" + (i + 1)).setText(subsections.get(i));
				}
			}
		
		} else {
		
			subsections.add(sectionSelected);
			
			doc.getRootElement().addElement("SelectedSection1").setText(sectionSelected);
		}
		
		
		//------------------------------------------------------------------
		// Determine If Show By Quarter
		//------------------------------------------------------------------
		if (doc.getRootElement().selectSingleNode("taggable_content_explorer/show_by_quarter") != null) {
		
			if (doc.getRootElement().selectSingleNode("taggable_content_explorer/show_by_quarter").getText().equals("true")) {
				
				String currentQuarter = "";
				
			    SimpleDateFormat formatterMonth = new SimpleDateFormat( "MM" );  
			    String monthDateXML = formatterMonth.format( new java.util.Date() ); 
			    
			    SimpleDateFormat formatterYear = new SimpleDateFormat( "yyyy" );  
			    String yearDateXML = formatterYear.format( new java.util.Date() ); 
			    
				if ((monthDateXML.toString().equals("01")) || (monthDateXML.toString().equals("02")) || (monthDateXML.toString().equals("03"))) {
					
					currentQuarter = "Q1";
				
				} else if ((monthDateXML.toString().equals("04")) || (monthDateXML.toString().equals("05")) || (monthDateXML.toString().equals("06"))) {
					
					currentQuarter = "Q2";
				
				} else if ((monthDateXML.toString().equals("07")) || (monthDateXML.toString().equals("08")) || (monthDateXML.toString().equals("09"))) {
					
					currentQuarter = "Q3";
				
				} else if ((monthDateXML.toString().equals("10")) || (monthDateXML.toString().equals("11")) || (monthDateXML.toString().equals("12"))) {
					
					currentQuarter = "Q4";
				}
				
				doc.getRootElement().addElement("CurrentQuarter").setText(currentQuarter);
				doc.getRootElement().addElement("CurrentYear").setText(yearDateXML);
			}
		}
		
		//------------------------------------------------------------------
		// Retrieve Titles From DCR
		//------------------------------------------------------------------
		
		// Category
		if (doc.getRootElement().selectSingleNode("taggable_content_explorer/name") != null) {
			
			title.add(doc.getRootElement().selectSingleNode("taggable_content_explorer/name").getText());
		}
		
		// Section One
		if (doc.getRootElement().selectSingleNode("taggable_content_explorer/section_one") != null) {
			
			List <Node> sectionOneNodes = doc.getRootElement().selectNodes("taggable_content_explorer/section_one");
			  
			  if (sectionOneNodes != null) {
				  
				  for ( Node son : sectionOneNodes ) {
					  
					  if (son.selectSingleNode("identifier") != null) {
					  
						  String identifierOne = son.selectSingleNode("identifier").getText();
						  
						  if (subsections.get(0) != null && identifierOne.equals(subsections.get(0))) {
							  
							  title.add(son.selectSingleNode("title").getText());
							  
							  // Section Two
							  if (son.selectSingleNode("section_two") != null) {
								  
								  List <Node> sectionTwoNodes = son.selectNodes("section_two");
								  
								  if (sectionTwoNodes != null) {
									  
									  for ( Node stn : sectionTwoNodes ) {
										  
										  if (stn.selectSingleNode("identifier") != null) {
											  
											  String identifierTwo = stn.selectSingleNode("identifier").getText();
											  
											  if (subsections.size() >= 2) {
											  
												  if (identifierTwo.equals(subsections.get(1))) {
												  
													  title.add(stn.selectSingleNode("title").getText());
													  
													  // Section Three
													  if (stn.selectSingleNode("section_three") != null) {
														  
														  List <Node> sectionThreeNodes = stn.selectNodes("section_three");
														  
														  if (sectionThreeNodes != null) {
															  
															  for ( Node sthn : sectionThreeNodes ) {
																  
																  if (sthn.selectSingleNode("identifier") != null) {
																	  
																	  String identifierThree = sthn.selectSingleNode("identifier").getText();
																	  
																	  if (subsections.size() >= 3) {
																		  
																		  if (identifierThree.equals(subsections.get(2))) {
																			  
																			  title.add(sthn.selectSingleNode("title").getText());
																		  }
																	  }
																  }
															  }
														  }
													  }
												  }
											  }
										  }
									  }
								  }
							  }
						  }
					  }
				  }
			  }
		}
		
		//------------------------------------------------------------------
		// Build Title
		//------------------------------------------------------------------
		String existingTitle = requestContext.getPageTitle();
		
		if (existingTitle != null) {
		
			title.add(existingTitle);
		}
		
		String pageTitle = "";
		
		for ( String titlePart : title )
		{
			if (!pageTitle.equals("") && pageTitle != null)
			{
				pageTitle += " - ";
			}
			
			pageTitle += titlePart;
		}		
		
		requestContext.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, pageTitle);
		
		return doc;
	}
}
