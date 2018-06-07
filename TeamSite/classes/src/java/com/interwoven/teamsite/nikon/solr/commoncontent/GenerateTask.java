package com.interwoven.teamsite.nikon.solr.commoncontent;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.dom4j.Dom4jUtils;

public class GenerateTask implements CSURLExternalTask {

	private static final Log logger = LogFactory.getLog(GenerateTask.class);
	
	@SuppressWarnings("rawtypes")
	public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
		
		try {

			CSAreaRelativePath[] jobFiles = task.getFiles();

			String SOLR_PROPERTIES_FILE = task.getVariable("SOLR_PROPERTIES_FILE");
			String SOLR_ENVIRONMENT = task.getVariable("SOLR_ENVIRONMENT");
			String SOLR_CORE = task.getVariable("SOLR_CORE");
			
			if (jobFiles.length > 0) {

				String templateMatch = "^templatedata/.*/taggable_content/data/.*$";
				
				boolean solrUpdateStatus = false;
				
				//------------------------------------------------------------------
				// Create The Output XML
				//------------------------------------------------------------------
				Document solrOutputXML = Dom4jUtils.newDocument("<update/>");
		                 solrOutputXML.setXMLEncoding("UTF-8");

		        Element solrOutputElemAdd = solrOutputXML.getRootElement().addElement("add");
		        
		        //------------------------------------------------------------------
				// Initialise The Generate Class
				//------------------------------------------------------------------
				GenerateSolrXML generateSolr = new GenerateSolrXMLImpl();

		        //------------------------------------------------------------------
				// Process Workflow DCRs
				//------------------------------------------------------------------
		        for (int i = 0; i < jobFiles.length; i++) {

					String fileRelPath = jobFiles[i].toString();
						   fileRelPath = fileRelPath.replaceAll("\\\\", "\\/");

					logger.info("File Relative Path: " + fileRelPath);

					//------------------------------------------------------------------
					// If taggable content...
					//------------------------------------------------------------------
					if (fileRelPath.matches(templateMatch)) {
						
						//------------------------------------------------------------------
						// Set Update Flag
						//------------------------------------------------------------------
						solrUpdateStatus = true;
						
						//------------------------------------------------------------------
						// Determine Locale
						//------------------------------------------------------------------
						String localeLanguage = fileRelPath.replaceAll("^templatedata/(.*)/taggable_content/data/.*$", "$1");
					
						//------------------------------------------------------------------
						// Get VPath
						//------------------------------------------------------------------
						String fileVPath = task.getArea().getVPath().toString() + "/" + fileRelPath;
						       fileVPath = fileVPath.replaceAll("\\\\", "\\/");
						       
						//------------------------------------------------------------------
						// Generate Locale Solr XML
						//------------------------------------------------------------------
						logger.info("Locale DCR... Generating Locale (" + localeLanguage + ")");

						String localeSolrXML = generateSolr.generate(client, localeLanguage, fileVPath);

   						//------------------------------------------------------------------
   						// Add XML To Solr Doc
   						//------------------------------------------------------------------
   						Document localeSolrReturnXML = Dom4jUtils.newDocument(localeSolrXML);
   			             		 localeSolrReturnXML.setXMLEncoding("UTF-8");

   			            solrOutputElemAdd.add(localeSolrReturnXML.getRootElement());
					}
		        }
						
				//------------------------------------------------------------------
				// Update Solr
				//------------------------------------------------------------------
		        if (solrUpdateStatus) {

		        	//------------------------------------------------------------------
					// Load Variables From Properties File
					//------------------------------------------------------------------
					Properties props = new Properties();
					           props.load(new FileInputStream(SOLR_PROPERTIES_FILE));

					//------------------------------------------------------------------
					// Set Solr Server(s) Based On Target Environment
					//------------------------------------------------------------------
					ArrayList<String> targetSolrServers = new ArrayList<String>();

					if (SOLR_ENVIRONMENT.equals("preview")) {

						String propsSolrServer = props.getProperty("PREVIEW_SOLR_ADDRESS");

						String[] pieces = propsSolrServer.split(",");

						for (int i = pieces.length - 1; i >= 0; i--) {

							targetSolrServers.add(pieces[i].trim());

						}

					} else if (SOLR_ENVIRONMENT.equals("staging")) {

						String propsSolrServer = props.getProperty("STAGING_SOLR_ADDRESS");

						String[] pieces = propsSolrServer.split(",");

						for (int i = pieces.length - 1; i >= 0; i--) {

							targetSolrServers.add(pieces[i].trim());

						}

					} else if (SOLR_ENVIRONMENT.equals("runtime")) {

						String propsSolrServer = props.getProperty("PRODUCTION_SOLR_ADDRESS");

						String[] pieces = propsSolrServer.split(",");

						for (int i = pieces.length - 1; i >= 0; i--) {

							targetSolrServers.add(pieces[i].trim());

						}
					}
					
					//------------------------------------------------------------------
					// Initialise The Update Class
					//------------------------------------------------------------------
					UpdateSolrServer updateSolrServer = new UpdateSolrServerImpl();

					//------------------------------------------------------------------
					// Update Solr Server(s) (Loop For Multiple Servers)
					//------------------------------------------------------------------
					for (String solrServer : targetSolrServers) {

						String solrServerUrl = "http://" + solrServer + "/solr/" + SOLR_CORE;

						logger.info(solrOutputXML.asXML());

						updateSolrServer.updateSolr(solrServerUrl, solrOutputXML.asXML());

						logger.info("Solr Updated.");
					}
					
					//------------------------------------------------------------------
					// Transition Task (Success)
					//------------------------------------------------------------------
			        task.chooseTransition("Solr Update Success", "SOLR Generated and Updated...");
		        
		        } else {
		        		        	
		        	//------------------------------------------------------------------
			        // Transition Task (No Update Needed)
			        //------------------------------------------------------------------
		        	task.chooseTransition("Solr Update Success", "No SOLR Update Needed...");
		        
		        }
		    }
		
		} catch (Exception e) {

			e.printStackTrace();

			//------------------------------------------------------------------
			// Transition Task (Failure)
			//------------------------------------------------------------------
			task.chooseTransition("Solr Update Failed", "SOLR Failure...");
		}
	}

}
