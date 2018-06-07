package com.interwoven.teamsite.nikon.dealerfinder.update;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.teamsite.nikon.dealerfinder.AdditionalData;
import com.interwoven.teamsite.nikon.dealerfinder.Dealer;
import com.interwoven.teamsite.nikon.dealerfinder.DealerDAO;
import com.interwoven.teamsite.nikon.dealerfinder.HibernateUtil;
import com.interwoven.teamsite.nikon.dealerfinder.exceptions.DAOException;

/**
 * This class performs the solr importer
 *
 * @author Mike
 */

public class RunSolrImporter {

	static final Logger oLogger = Logger.getLogger(RunSolrImporter.class);
		
	public static String SOLR_ENVIRONMENT;
	public static String SOLR_CORE_INSTANCE;

	public static void main(String[] args) {
		
		// Cmd-line options
		Options options = new Options();
		options.addOption("target", true, "Target Environment");
		options.addOption("debug", true, "Debug Logging?");
		
		CommandLineParser parser = new BasicParser();
    	CommandLine cmd = null;
    	ApplicationContext context = new FileSystemXmlApplicationContext("props/app-context.xml");
    	HibernateUtil.init(context);        	
    	try {
    		cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		// Logging
		BasicConfigurator.configure();
					
		if (cmd.hasOption("debug") && cmd.getOptionValue("debug").equals("true")) {
			
			Logger.getRootLogger().setLevel(Level.DEBUG);
		
		} else {
		
			Logger.getRootLogger().setLevel(Level.INFO);
		}
		
		// Props file configuration
		Configuration propsFile = null;
		
		try {
			
			propsFile = new PropertiesConfiguration("props/solr.properties");
			
			SOLR_CORE_INSTANCE = propsFile.getString("SOLR_CORE_INSTANCE");
			
		} catch (ConfigurationException e) {
			
			e.printStackTrace();
		}
	    
		// Main code block
	    try 
		{
	    	SOLR_ENVIRONMENT = cmd.getOptionValue("target");
	    	
	    	// Does target exist?
	    	if (cmd.hasOption("target")) {
	    		
	    		boolean solrUpdateStatus = false;
				
	    		Session oSession = HibernateUtil.getSessionFactory().openSession();
	    		
				DealerDAO oDealerDAO = new DealerDAO();
				List<Dealer> olDealers = new ArrayList<Dealer>();
				List<Dealer> olDealersForUpdate = new ArrayList<Dealer>();
				List<Long> olDealersForDeletion = new ArrayList<Long>();
				
				olDealers = oDealerDAO.listAllDealers(oSession);
				
				// Create the base XML for SOLR
	    		Document solrOutputXML = Dom4jUtils.newDocument("<update/>");
			    solrOutputXML.setXMLEncoding("UTF-8");
			    solrOutputXML.getRootElement().addAttribute("type", "DEALER");
					
			    Element solrAddElement = solrOutputXML.getRootElement().addElement("add");
			    Element solrDeleteElement = solrOutputXML.getRootElement().addElement("delete");
        		
		        // Process the dealers and create XML
                for (Dealer olDealer : olDealers) {
                	
                	String olDealerStatus = olDealer.getStatus();
                	
                	long olDealerModDate = olDealer.getModifiedDate();
                	long olDealerABFDate = olDealer.getAbfDate();
                	long olDealerProdDate = olDealer.getProdDate();
                	
                	Date now = new Date();
                	long todaysDate = now.getTime();
                	
                	oLogger.info("Processing dealer " + olDealer.getId() + " for update in SOLR...");
                	
                	// Publish to ABF
                	if (olDealerStatus.equalsIgnoreCase("PUBLISH") && SOLR_ENVIRONMENT.equals("abfEnv")) {
                		
                		if (olDealerModDate >= olDealerABFDate) {
                		
                			Document dealerXML = Dom4jUtils.newDocument(processDealer(olDealer));
                			dealerXML.setXMLEncoding("UTF-8");
                			
                			// Add dealer XML for SOLR
                			solrAddElement.add(dealerXML.getRootElement());
                			
                			// Update flag
                			solrUpdateStatus = true;
                			
                			// Add to dealers to update in DB
                			olDealersForUpdate.add(olDealer);
                		}
                		
                	} 
                	// Publish to Prod
                	else if (olDealerStatus.equalsIgnoreCase("PUBLISHED_ABF") && SOLR_ENVIRONMENT.equals("runtime")) {
                		
                		if (olDealerModDate >= olDealerProdDate) {
                		
                			// Process dealer
                			Document dealerXML = Dom4jUtils.newDocument(processDealer(olDealer));
                			dealerXML.setXMLEncoding("UTF-8");
                			
                			// Add dealer XML for SOLR
                			solrAddElement.add(dealerXML.getRootElement());
                			
                			// Update flag
                			solrUpdateStatus = true;
                			
                			// Add to dealers to update in DB
                			olDealersForUpdate.add(olDealer);
                		}
                		
                	} 
                	// Delete from ABF
                	else if (olDealerStatus.equalsIgnoreCase("DELETE") && SOLR_ENVIRONMENT.equals("abfEnv")) {
                		
                			// Add id to delete tag
                			solrDeleteElement.addElement("id").setText(olDealer.getId().toString());
                			
                			// Update flag
                			solrUpdateStatus = true;
                			
                			// Add to dealers to update in DB
                			olDealersForUpdate.add(olDealer);
                	}
                	// Delete from Prod
                	else if (olDealerStatus.equalsIgnoreCase("DELETED_ABF") && SOLR_ENVIRONMENT.equals("runtime")) {
                		
                			// Add id to delete tag
                			solrDeleteElement.addElement("id").setText(olDealer.getId().toString());
                			
                			// Update flag
                			solrUpdateStatus = true;
                			
                			// Add to dealers to update in DB
                			olDealersForUpdate.add(olDealer);
                	}
                	// Delete from DB
                	else if (olDealerStatus.equalsIgnoreCase("DELETED_PROD") && SOLR_ENVIRONMENT.equals("runtime")) {
                		
                			long daysInMillisRetention = 2592000000L;
                			
                			// Not correct *******************
                			if ((todaysDate - olDealerProdDate) > daysInMillisRetention) {
                				
                				// Add to dealers to delete from the DB
                				olDealersForDeletion.add(olDealer.getId());
                			}
                	}
                	
                }

		        // Update SOLR (if flag set to true)
				if (solrUpdateStatus) {
			        	
		        	// Set SOLR servers from com-line option
					ArrayList<String> targetSolrServers = new ArrayList<String>();
					
					if (SOLR_ENVIRONMENT.equals("preview")) {
						
						String[] pieces = propsFile.getStringArray("PREVIEW_SOLR_ADDRESS");
						
						for (int i = pieces.length - 1; i >= 0; i--) {
						
							targetSolrServers.add(pieces[i].trim());
						
						}
						
					} else if (SOLR_ENVIRONMENT.equals("abfEnv")) {
						
						String[] pieces = propsFile.getStringArray("ABF_SOLR_ADDRESS");
						
						for (int i = pieces.length - 1; i >= 0; i--) {
						
							targetSolrServers.add(pieces[i].trim());
						
						}
					
					} else if (SOLR_ENVIRONMENT.equals("runtime")) {
						
						String[] pieces = propsFile.getStringArray("PRODUCTION_SOLR_ADDRESS");
						
						for (int i = pieces.length - 1; i >= 0; i--) {
						
							targetSolrServers.add(pieces[i].trim());
						
						}
					}
					
					boolean hasFailure = false;
					           
		        	// Update SOLR servers
					for (String solrServer : targetSolrServers) {
					

						String solrServerUrl = "http://" + solrServer + "/solr/" + SOLR_CORE_INSTANCE;
						oLogger.info("Updating SOLR using the following URL: " + solrServerUrl);						
						oLogger.debug(solrOutputXML.asXML());
						
						// Update SOLR
						boolean solrSuccess = updateSolr(solrServerUrl, solrOutputXML.asXML());
						
						// SOLR updated... process changes to the DB
						if (!solrSuccess) {
							oLogger.error("Fail to update server with URL : ["+solrServerUrl+"], please check log");
							hasFailure = true;
							break;

						}
					}
					if (!hasFailure){
						
						oLogger.info("Solr Updated, will update the Dealer Records");
						
						solrOutputXML = null;
						olDealers = null;
						
						for (Dealer olDealerForUpdate : olDealersForUpdate) {
							
							processDealerForUpdate(oSession, oDealerDAO, olDealerForUpdate);
						}
						
						// Clean up database
						oDealerDAO.removeDealer(oSession, olDealersForDeletion);
					}else{
						oLogger.error("SOLR update has error, will not update the DB records");
					}
					
					oSession.close();
		        }
	    		
	    	} else {
	    		
	    		System.out.println("");
	    		System.out.println("java -jar runSolrImport.jar -target <TARGET> -debug true");
	    		System.out.println("");
	    		System.out.println("Options:");
	    		System.out.println("          -target      Target Environment (preview, abfEnv, runtime)");
	    		System.out.println("          -debug       Show Debug (true)");
	    		System.out.println("");
	    	}
		
		} catch (NullPointerException e) {
			oLogger.debug(e.getMessage());
	    } catch (DAOException e) {
			oLogger.debug(e.getMessage());
		}
	}
	
	
	public static void processDealerForUpdate(Session oSession, DealerDAO dealerDAO, Dealer dealer) {
		
		String dealerStatus = dealer.getStatus();
    	
    	long dealerModDate = dealer.getModifiedDate();
    	long dealerABFDate = dealer.getAbfDate();
    	long dealerProdDate = dealer.getProdDate();
    	
    	Date now = new Date();
    	long todaysDate = now.getTime();
		
    	oLogger.info("Processing dealer " + dealer.getId() + " for update in database...");
    	
		// Publish to ABF
    	if (dealerStatus.equalsIgnoreCase("PUBLISH") && SOLR_ENVIRONMENT.equals("abfEnv")) {
    		
    		if (dealerModDate >= dealerABFDate) {
    		
    			dealer.setAbfDate(todaysDate);
    			dealer.setStatus("PUBLISHED_ABF");
    		}
    	} 
    	// Publish to Prod
    	else if (dealerStatus.equalsIgnoreCase("PUBLISHED_ABF") && SOLR_ENVIRONMENT.equals("runtime")) {
    		
    		if (dealerModDate >= dealerProdDate) {
    			
    			dealer.setProdDate(todaysDate);
    			dealer.setStatus("PUBLISHED_PROD");
    		}
    	}
    	// Delete from ABF
    	else if (dealerStatus.equalsIgnoreCase("DELETE") && SOLR_ENVIRONMENT.equals("abfEnv")) {
    		
    		dealer.setAbfDate(todaysDate);
    		dealer.setStatus("DELETED_ABF");
    		
    		
    	}
    	// Delete from Prod
    	else if (dealerStatus.equalsIgnoreCase("DELETED_ABF") && SOLR_ENVIRONMENT.equals("runtime")) {
    		
    		dealer.setProdDate(todaysDate);
    		dealer.setStatus("DELETED_PROD");
    	}
    	
    	// Update dealer
    	try {
    		dealerDAO.updateDealer(oSession, dealer);
		} catch (DAOException e) {
			oLogger.error(e.getMessage());
		}
	}
	
	public static boolean updateSolr(String solrServerUrl, String solrUpdateXML) {
		
		boolean success = false;
		
		oLogger.info("Solr Server - Update Process Start");
		
		try {
				oLogger.info("Solr Server - Address: " + solrServerUrl);
			
				HttpSolrServer solrServer = new HttpSolrServer(solrServerUrl);			
				
				DirectXmlRequest up = new DirectXmlRequest( "/update", solrUpdateXML); 
			
				solrServer.request(up);
				
				oLogger.info("Solr Server - Update Sent");
				
				solrServer.commit();
				
				oLogger.info("Solr Server - Commit Executed");
				
				oLogger.info("Solr Server - Update Process End");
				
				success = true;
				
		} catch (MalformedURLException e) {
			oLogger.error(e.getMessage());
		} catch (SolrServerException e) {
			oLogger.error(e.getMessage());
		} catch (IOException e) {
			oLogger.error(e.getMessage());
		}
		
		return success;
	}
	
	public static String processDealer(Dealer dealer) {
		
		Document solrXML = Dom4jUtils.newDocument("<update/>");
		solrXML.setXMLEncoding("UTF-8");

		Element solrAddElement = solrXML.getRootElement().addElement("add");
		
		Element solrDocElement = solrAddElement.addElement("doc");
	    		solrDocElement.addAttribute("boost", "1.0");
	    		
	    // Process core dealer data
	    solrDocElement.addElement("field").addAttribute("name", "id").setText(dealer.getId().toString());
	    solrDocElement.addElement("field").addAttribute("name", "name_s").setText(dealer.getName());
	    solrDocElement.addElement("field").addAttribute("name", "name_ss").setText(dealer.getName());
	    solrDocElement.addElement("field").addAttribute("name", "description_s").setText(dealer.getDescription());
	    solrDocElement.addElement("field").addAttribute("name", "description_ss").setText(dealer.getDescription());
	    solrDocElement.addElement("field").addAttribute("name", "street_s").setText(dealer.getStreet());
	    solrDocElement.addElement("field").addAttribute("name", "street_ss").setText(dealer.getStreet());
	    solrDocElement.addElement("field").addAttribute("name", "town_s").setText(dealer.getTown());
	    solrDocElement.addElement("field").addAttribute("name", "town_ss").setText(dealer.getTown());
	    solrDocElement.addElement("field").addAttribute("name", "state_s").setText(dealer.getState());
	    solrDocElement.addElement("field").addAttribute("name", "state_ss").setText(dealer.getState());
	    solrDocElement.addElement("field").addAttribute("name", "country_s").setText(dealer.getCountry());
	    solrDocElement.addElement("field").addAttribute("name", "country_ss").setText(dealer.getCountry());
	    solrDocElement.addElement("field").addAttribute("name", "country_code_s").setText(dealer.getCountryCode());
	    solrDocElement.addElement("field").addAttribute("name", "country_code_ss").setText(dealer.getCountryCode());
	    solrDocElement.addElement("field").addAttribute("name", "post_code_s").setText(dealer.getPostCode());
	    solrDocElement.addElement("field").addAttribute("name", "post_code_ss").setText(dealer.getPostCode());
	    solrDocElement.addElement("field").addAttribute("name", "tel_s").setText(dealer.getTel());
	    solrDocElement.addElement("field").addAttribute("name", "tel_ss").setText(dealer.getTel());
	    solrDocElement.addElement("field").addAttribute("name", "fax_s").setText(dealer.getFax());
	    solrDocElement.addElement("field").addAttribute("name", "fax_ss").setText(dealer.getFax());
	    solrDocElement.addElement("field").addAttribute("name", "email_s").setText(dealer.getEmail());
	    solrDocElement.addElement("field").addAttribute("name", "email_ss").setText(dealer.getEmail());
	    solrDocElement.addElement("field").addAttribute("name", "url_s").setText(dealer.getUrl());
	    solrDocElement.addElement("field").addAttribute("name", "url_ss").setText(dealer.getUrl());
	    solrDocElement.addElement("field").addAttribute("name", "opening_hours_s").setText(dealer.getOpeningHours());
	    solrDocElement.addElement("field").addAttribute("name", "opening_hours_ss").setText(dealer.getOpeningHours());
	    solrDocElement.addElement("field").addAttribute("name", "location").setText(dealer.getLatitude() + "," + dealer.getLongitude());
	    solrDocElement.addElement("field").addAttribute("name", "lat_coordinate").setText(dealer.getLatitude() + "");
	    solrDocElement.addElement("field").addAttribute("name", "lon_coordinate").setText(dealer.getLongitude() + "");
	    solrDocElement.addElement("field").addAttribute("name", "moddate_s").setText(dealer.getModifiedDate() + "");
	    solrDocElement.addElement("field").addAttribute("name", "user_group_s").setText(dealer.getGroup());
	    solrDocElement.addElement("field").addAttribute("name", "user_group_ss").setText(dealer.getGroup());
	    
	    // Process additional data
	    Set<AdditionalData> stAddData = dealer.getAdditionalData();
	    
	    for (AdditionalData addData : stAddData) {
	    	if ("true".equals(addData.getFieldValue())){
		    	solrDocElement.addElement("field").addAttribute("name", "c" + addData.getFieldId() + "_s").setText(addData.getFieldValue());
		    	solrDocElement.addElement("field").addAttribute("name", "c" + addData.getFieldId() + "_ss").setText(addData.getFieldValue());
	    	}
	    }
	    		
		return solrXML.getRootElement().selectSingleNode("add/doc").asXML();
	}
}
