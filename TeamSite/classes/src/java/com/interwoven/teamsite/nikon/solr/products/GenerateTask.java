package com.interwoven.teamsite.nikon.solr.products;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.dom4j.Dom4jUtils;

public class GenerateTask implements CSURLExternalTask {

	private static final Log logger = LogFactory.getLog(GenerateTask.class);

	//====================================================================
	// execute()
	//====================================================================
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {

		try {

			CSAreaRelativePath[] jobFiles = task.getFiles();

			String SOLR_PROPERTIES_FILE = task.getVariable("SOLR_PROPERTIES_FILE");
			String SOLR_ENVIRONMENT = task.getVariable("SOLR_ENVIRONMENT");
			String SOLR_CORE = task.getVariable("SOLR_CORE");
			String SOLR_LOCALISATION_DCR = task.getVariable("SOLR_LOCALISATION_DCR");

			if (jobFiles.length > 0) {

				String templateMatch = "^templatedata/.*/product_information_container/data/.*$";

				boolean solrUpdateStatus = false;

				HashMap<String,String> locales = new HashMap<String,String>();

				//====================================================================
			    // Determine Locales Based On Fallback Rules
			    //====================================================================
			    CSVPath csVpath = new CSVPath(SOLR_LOCALISATION_DCR);

				CSSimpleFile file = (CSSimpleFile) client.getFile(csVpath);

				if (file.isValid()) {

					String xml = retrieveXML(file);

			        Document localesXML = Dom4jUtils.newDocument(xml);
			        	     localesXML.setXMLEncoding("UTF-8");

			        Element rootElement = localesXML.getRootElement();

			        List<Node> countryNodes = rootElement.selectNodes("countries");

					for ( Node n : countryNodes )
					{
						if (n.selectSingleNode("root_language") != null) {

							if (!n.selectSingleNode("root_language").valueOf("@language_code").equals("en_Asia")) {

								List<Node> rootLangNodes = n.selectNodes("root_language");
								
								if (rootLangNodes != null && !rootLangNodes.isEmpty()){
									for (Node rootLangNode : rootLangNodes){
										String fallbackLanguage = "";
						
										if (rootLangNode.selectSingleNode("fallback_languages/language_code") != null) {
						
											fallbackLanguage = rootLangNode.selectSingleNode("fallback_languages/language_code").getText();
										}
						
										locales.put(rootLangNode.valueOf("@language_code"), fallbackLanguage);
										logger.info("Primary Language:  " + rootLangNode.valueOf("@language_code"));
										logger.info("Fallback Language: " + fallbackLanguage);
									}
									
								}
							}
						}
					}
				}

				//------------------------------------------------------------------
				// Create The Output XML
				//------------------------------------------------------------------
				//Document solrOutputXML = Dom4jUtils.newDocument("<update/>");
		        //         solrOutputXML.setXMLEncoding("UTF-8");

		        //Element solrOutputElemAdd = solrOutputXML.getRootElement().addElement("add");

		        //------------------------------------------------------------------
				// Initialise The Generate Class
				//------------------------------------------------------------------
				GenerateSolrXML generateSolr = new GenerateSolrXMLImpl();

				String updateContent = "";
		        //------------------------------------------------------------------
				// Process Workflow DCRs
				//------------------------------------------------------------------
		        for (int i = 0; i < jobFiles.length; i++) {

					String fileRelPath = jobFiles[i].toString();
						   fileRelPath = fileRelPath.replaceAll("\\\\", "\\/");

					logger.info("File Relative Path: " + fileRelPath);

					//------------------------------------------------------------------
					// If a Product or Accessory...
					//------------------------------------------------------------------
					if (fileRelPath.matches(templateMatch)) {

						//------------------------------------------------------------------
						// Set Update Flag
						//------------------------------------------------------------------
						solrUpdateStatus = true;

						//------------------------------------------------------------------
						// Determine Fallback Needs
						//------------------------------------------------------------------
						String fileVPath = task.getArea().getVPath().toString() + "/" + fileRelPath;
						       fileVPath = fileVPath.replaceAll("\\\\", "\\/");

						//------------------------------------------------------------------
						// If en_Asia DCR...
						//------------------------------------------------------------------
						if (fileRelPath.matches(".*/en_Asia/.*")) {

							//------------------------------------------------------------------
							// Generate en_Asia Solr XML
							//------------------------------------------------------------------
							logger.info("en_Asia DCR... Generating Locale (en_Asia)");

							String solrXML = generateSolr.generate(client, "en_Asia", fileVPath);

       						//------------------------------------------------------------------
       						// Add XML To Solr Doc
       						//------------------------------------------------------------------
       						Document solrReturnXML = Dom4jUtils.newDocument(solrXML);
       			             		 solrReturnXML.setXMLEncoding("UTF-8");

       			            updateContent = updateContent + solrReturnXML.getRootElement().asXML();
       			            //solrOutputElemAdd.add(solrReturnXML.getRootElement());
       			            
       			            solrReturnXML = null;

       			            //------------------------------------------------------------------
							// Check Each Locale
       			            //------------------------------------------------------------------
       			            Set s = locales.entrySet();

                   		 	Iterator it = s.iterator();

                   		 	while(it.hasNext())
                   		 	{
                   		 		Map.Entry m = (Map.Entry) it.next();

                   		 		//------------------------------------------------------------------
                   		 		// Locale Language & Country Code
                   		 		//------------------------------------------------------------------
                   		 		String localeLanguage = m.getKey().toString();
                   		 		String fallbackLanguage = m.getValue().toString();
                   		 		String countryCode = localeLanguage.substring(3, 5);

                   		 		//------------------------------------------------------------------
                   		 		// Get Locale Path
                   		 		//------------------------------------------------------------------
                   		 		String productRelPath = fileRelPath;
                   		 			   productRelPath = productRelPath.replaceAll("en_Asia", localeLanguage);

                   		 		String localeProductVPath = "/default/main/Nikon/" + countryCode + "/STAGING/" + productRelPath;
                   		 			   localeProductVPath = localeProductVPath.replaceAll("\\\\", "\\/");

                   		 		CSVPath localeCSVpath = new CSVPath(localeProductVPath);

                   		 		CSFile csFile = client.getFile(localeCSVpath);
                   		 		
                   		 		if (csFile != null && csFile.getKind() == CSSimpleFile.KIND){
                   		 			logger.info("DCR Exists... Skipping Locale (" + localeLanguage + ")");                  		 			
                   		 		}else{
                       		 		//------------------------------------------------------------------
                       		 		// If DCR Does Not Exist
                       		 		//------------------------------------------------------------------                   		 			
                   		 			logger.info("DCR Does Not Exist... Will Check Fallback locale ("+fallbackLanguage+")");
                   		 			boolean requireGenerate = false;
                   		 			if (!"".equals(fallbackLanguage)){
                   		 				logger.info("Fallback locale is not empty, will check whether DCR exist or not");
                   		 				String fallBackCountryCode = fallbackLanguage.substring(3, 5);
                   		 				String productFallbackRelPath = fileRelPath;
                   		 				productFallbackRelPath = productFallbackRelPath.replaceAll("en_Asia", fallbackLanguage);
                   		 				String localeFallbackProductVPath = "/default/main/Nikon/" + fallBackCountryCode + "/STAGING/" + productFallbackRelPath;
                   		 				localeFallbackProductVPath = localeFallbackProductVPath.replaceAll("\\\\", "\\/");
	                   		 			CSVPath localeFallbackCSVpath = new CSVPath(localeFallbackProductVPath);
	
	                       		 		CSSimpleFile localeFallbackFile = (CSSimpleFile) client.getFile(localeFallbackCSVpath);
	                       		 		if (localeFallbackFile == null){
	                       		 			logger.info("DCR Fallback locale Does Not Exist also... Will fallback to Asia");
	                       		 			requireGenerate = true;
	                       		 		}
                   		 				
                   		 			}else{
                   		 				logger.info("There are no locale fallback, Will fallback to Asia");
                   		 				requireGenerate = true;
                   		 			}
                   		 			
                   		 			if (requireGenerate){
                       		 			logger.info("DCR Does Not Exist... Generating Locale (" + localeLanguage + ") Based On Asia");

                       		 			//------------------------------------------------------------------
    	                   				// Generate Locale Solr XML
                       		 			//------------------------------------------------------------------
    	           						String localeSolrXML = generateSolr.generate(client, localeLanguage, fileVPath);

    	           						//------------------------------------------------------------------
    	           						// Add Locale XML To Solr Doc
    	           						//------------------------------------------------------------------
    	           						Document localeSolrReturnXML = Dom4jUtils.newDocument(localeSolrXML);
    	           			             		 localeSolrReturnXML.setXMLEncoding("UTF-8");

    	           			            updateContent = updateContent + localeSolrReturnXML.getRootElement().asXML();

    	           			            localeSolrReturnXML = null;                   		 				
                   		 			}else{
                   		 				logger.info("DCR Exists... Skipping Locale (" + localeLanguage + ")");
                   		 			}                  		 			
                   		 		}

                   		 	}
						}
						//------------------------------------------------------------------
						// If Locale DCR...
						//------------------------------------------------------------------
						else {

							//------------------------------------------------------------------
							// Determine Locale
							//------------------------------------------------------------------
							String localeLanguage = fileRelPath.replaceAll("^templatedata/(.*)/product_information_container/data/.*$", "$1");

               		 		//------------------------------------------------------------------
							// Generate Locale Solr XML
							//------------------------------------------------------------------
							logger.info("Locale DCR... Generating Locale (" + localeLanguage + ")");

							String localeSolrXML = generateSolr.generate(client, localeLanguage, fileVPath);

       						//------------------------------------------------------------------
       						// Add XML To Solr Doc
       						//------------------------------------------------------------------
							try {
           						
								Document localeSolrReturnXML = Dom4jUtils.newDocument(localeSolrXML);
  			             		         localeSolrReturnXML.setXMLEncoding("UTF-8");

  			             		//solrOutputElemAdd.add(localeSolrReturnXML.getRootElement());
  			             		
  			             		updateContent = updateContent + localeSolrReturnXML.getRootElement().asXML();
  			             		
  			             		localeSolrReturnXML = null;
							} catch (Exception e){
           		 				
								logger.error("Error while trying to parse the localeSolrXML response, will ignore. Response: ["+localeSolrXML+"]", e);
           		 			}

							//------------------------------------------------------------------
							// Check For Child Locales
       			            //------------------------------------------------------------------
       			            Set s = locales.entrySet();

                   		 	Iterator it = s.iterator();

                   		 	while(it.hasNext())
                   		 	{
                   		 		Map.Entry m = (Map.Entry)it.next();

                   		 		//------------------------------------------------------------------
                   		 		// Locale Language & Country Code
                   		 		//------------------------------------------------------------------
                   		 		String childLocaleLanguage = m.getKey().toString();
                   		 		String childParentLocaleLanguage = m.getValue().toString();

                   		 		if (childParentLocaleLanguage.equals(localeLanguage)) {

                   		 			logger.info("Locale DCR... Fallback Locale Language Found (" + childLocaleLanguage + ")");

                   		 			//------------------------------------------------------------------
                       		 		// Locale Language & Country Code
                       		 		//------------------------------------------------------------------
                       		 		String childCountryCode = childLocaleLanguage.substring(3, 5);

                       		 		//------------------------------------------------------------------
                       		 		// Get Locale Path
                       		 		//------------------------------------------------------------------
                       		 		String productRelPath = fileRelPath;
                       		 			   productRelPath = productRelPath.replaceAll(localeLanguage, childLocaleLanguage);

                       		 		String childLocaleProductVPath = "/default/main/Nikon/" + childCountryCode + "/STAGING/" + productRelPath;
                       		 			   childLocaleProductVPath = childLocaleProductVPath.replaceAll("\\\\", "\\/");

                       		 		CSVPath childLocaleCSVpath = new CSVPath(childLocaleProductVPath);

                       		 		CSFile csFile = client.getFile(childLocaleCSVpath);
                   		 		
	                   		 		if (csFile != null && csFile.getKind() == CSSimpleFile.KIND){
	                   		 			logger.info("Locale DCR exist, will not generate, continue to check for fallback...");
	                   		 		}else{
		                   		 		logger.info("Locale DCR... Generating Locale Fallback (" + childLocaleLanguage + ")");
	
	                   		 			String childLocaleSolrXML = generateSolr.generate(client, childLocaleLanguage, fileVPath);
	
		           						//------------------------------------------------------------------
		           						// Add Locale XML To Solr Doc
		           						//------------------------------------------------------------------
	                       		 		try {
	    	           					
	                       		 			Document childLocaleSolrReturnXML = Dom4jUtils.newDocument(childLocaleSolrXML);
	           								         childLocaleSolrReturnXML.setXMLEncoding("UTF-8");
	
	           								 //solrOutputElemAdd.add(childLocaleSolrReturnXML.getRootElement());
	           								 
	           								 updateContent = updateContent + childLocaleSolrReturnXML.getRootElement().asXML();
	           								 
	                       		 		} catch (Exception e){
	                   		 				
	                       		 			logger.error("Error while trying to parse the childLocaleSolrXML response, will ignore. Response: ["+childLocaleSolrXML+"]", e);
	                   		 			}	                   		 			
	                   		 		}
                   		 		}
                   		 	}
						}
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
					String solrOutputXML = "<update><add>" + updateContent + "</add></update>";
					for (String solrServer : targetSolrServers) {

						String solrServerUrl = "http://" + solrServer + "/solr/" + SOLR_CORE;

						logger.debug(solrOutputXML);

						updateSolrServer.updateSolr(solrServerUrl, solrOutputXML);

						logger.info("Solr Updated.");
					}
		        }

		        //------------------------------------------------------------------
				// Transition Task
				//------------------------------------------------------------------
		        //task.chooseTransition("Solr Update Success", "SOLR Generated...");  //modified on 20140415 due to workflow bug in NICS
		        task.chooseTransition(task.getTransitions()[0], "SOLR Generated...");
		        
			}

		} catch (Exception e) {

			e.printStackTrace();

			task.chooseTransition("Solr Update Failed", "SOLR Failure...");
		}
	}

	//====================================================================
	// retrieveXML()
	//====================================================================
	public String retrieveXML(CSSimpleFile file) {

		String xml = "";

		BufferedInputStream in = null;
		Reader reader = null;

		try {

			in = file.getBufferedInputStream(false);
			reader = new InputStreamReader(in, "UTF-8");

			int bytesRead;

			while ((bytesRead = reader.read()) != -1) {

				xml += (char) bytesRead;
            }

		} catch (IOException ex) {
            ex.printStackTrace();
        } catch (CSAuthorizationException e) {
			e.printStackTrace();
		} catch (CSObjectNotFoundException e) {
			e.printStackTrace();
		} catch (CSExpiredSessionException e) {
			e.printStackTrace();
		} catch (CSRemoteException e) {
			e.printStackTrace();
		} catch (CSException e) {
			e.printStackTrace();
		} finally {

        	try {
                if (in != null)
                    in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return xml;
	}
}
