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
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.dom4j.Dom4jUtils;

public class DeleteTask implements CSURLExternalTask {

	private static final Log logger = LogFactory.getLog(DeleteTask.class);

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

				boolean solrDeleteStatus = false;

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

								String fallbackLanguage = "";

								if (n.selectSingleNode("root_language/fallback_languages/language_code") != null) {

									fallbackLanguage = n.selectSingleNode("root_language/fallback_languages/language_code").getText();
								}

								locales.put(n.selectSingleNode("root_language").valueOf("@language_code"), fallbackLanguage);

								logger.info("Primary Language:  " + n.selectSingleNode("root_language").valueOf("@language_code"));
								logger.info("Fallback Language: " + fallbackLanguage);
							}
						}
					}
				}

				//------------------------------------------------------------------
				// Create The Output XML
				//------------------------------------------------------------------
				Document solrOutputXML = Dom4jUtils.newDocument("<delete/>");
		                 solrOutputXML.setXMLEncoding("UTF-8");

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
						solrDeleteStatus = true;

						//------------------------------------------------------------------
						// Determine Fallback Needs
						//------------------------------------------------------------------
						String fileVPath = task.getArea().getBranch().toString() + "/STAGING/" + fileRelPath;
						       fileVPath = fileVPath.replaceAll("\\\\", "\\/");

						//------------------------------------------------------------------
						// If en_Asia DCR...
						//------------------------------------------------------------------
						if (fileRelPath.matches(".*/en_Asia/.*")) {

							//------------------------------------------------------------------
							// Deleting en_Asia Solr XML
							//------------------------------------------------------------------
							logger.info("en_Asia DCR... Deleting Locale (en_Asia)");

							CSVPath parentCSVpath = new CSVPath(fileVPath);

               		 		CSSimpleFile parentFile = (CSSimpleFile) client.getFile(parentCSVpath);
               		 		
							//------------------------------------------------------------------
       						// Add XML To Solr Doc
       						//------------------------------------------------------------------
							String parentLocaleId = retrieveId(parentFile);
							String parentSolrId = parentLocaleId + "_en_Asia";
       						
							solrOutputXML.addElement("query").setText("id:" + parentSolrId);

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
                   		 		String countryCode = localeLanguage.substring(3, 5);
                   		 	
                   		 		logger.info("Child DCR... Deleting Locale (" + localeLanguage + ")");
                   		 		
                   		 		//------------------------------------------------------------------
                   		 		// Get Locale Path
                   		 		//------------------------------------------------------------------
                   		 		String productRelPath = fileRelPath;
                   		 			   productRelPath = productRelPath.replaceAll("en_Asia", localeLanguage);

                   		 		String localeProductVPath = "/default/main/Nikon/" + countryCode + "/STAGING/" + productRelPath;
                   		 			   localeProductVPath = localeProductVPath.replaceAll("\\\\", "\\/");

                   		 		CSVPath localeCSVpath = new CSVPath(localeProductVPath);

                   		 		CSSimpleFile localeFile = (CSSimpleFile) client.getFile(localeCSVpath);

                   		 		//------------------------------------------------------------------
                   		 		// If DCR Does Not Exist
                   		 		//------------------------------------------------------------------
                   		 		if (localeFile == null) {
                   		 	
                   		 			//------------------------------------------------------------------
                   		 			// Add Locale XML To Solr Doc
                   		 			//------------------------------------------------------------------
                   		 			logger.info("Locale DCR... Deleting Locale Fallback (" + localeLanguage + ")");
                   		 		
                   		 			String childSolrId = parentLocaleId + "_" + localeLanguage;
                   		 		
                   		 			solrOutputXML.addElement("query").setText("id:" + childSolrId);
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
							logger.info("Locale DCR... Deleting Locale (" + localeLanguage + ")");

							CSVPath parentCSVpath = new CSVPath(fileVPath);
							CSSimpleFile parentFile = (CSSimpleFile) client.getFile(parentCSVpath);
               		 		
							String parentLocaleId = retrieveId(parentFile);
							String parentSolrId = parentLocaleId + "_" + localeLanguage;
       						
							solrOutputXML.addElement("query").setText("id:" + parentSolrId);

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

                       		 		CSSimpleFile childLocaleFile = (CSSimpleFile) client.getFile(childLocaleCSVpath);

                       		 		//------------------------------------------------------------------
                       		 		// If DCR Does Not Exist
                       		 		//------------------------------------------------------------------
                       		 		if (childLocaleFile == null) {

                       		 			//------------------------------------------------------------------
    	                   				// Generate Child Locale Solr XML
                       		 			//------------------------------------------------------------------
                       		 			logger.info("Locale DCR... Deleting Locale Fallback (" + childLocaleLanguage + ")");

                       		 			String childSolrId = parentLocaleId + "_" + childLocaleLanguage;

    	           						solrOutputXML.addElement("query").setText("id:" + childSolrId);
                       				}
                   		 		}
                   		 	}
						}
					}
				}

		        //------------------------------------------------------------------
				// Update Solr
				//------------------------------------------------------------------
		        if (solrDeleteStatus) {

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

					} else if (SOLR_ENVIRONMENT.equals("abfEnv")) {

						String propsSolrServer = props.getProperty("ABF_SOLR_ADDRESS");

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

						logger.debug(solrOutputXML.asXML());

						updateSolrServer.updateSolr(solrServerUrl, solrOutputXML.asXML());

						logger.info("Solr Updated.");
					}
		        }

		        //------------------------------------------------------------------
				// Transition Task
				//------------------------------------------------------------------
		        task.chooseTransition("Solr Delete Success", "SOLR Updated...");
			}

		} catch (Exception e) {

			e.printStackTrace();

			task.chooseTransition("Solr Delete Failed", "SOLR Failure...");
		}
	}

	//====================================================================
	// retrieveId()
	//====================================================================
	public String retrieveId(CSSimpleFile file) {
	
		String productId = "";
		
		try {
			productId = file.getExtendedAttribute("TeamSite/Metadata/prod_id").getValue();
		} catch (CSAuthorizationException e) {
			e.printStackTrace();
		} catch (CSRemoteException e) {
			e.printStackTrace();
		} catch (CSObjectNotFoundException e) {
			e.printStackTrace();
		} catch (CSExpiredSessionException e) {
			e.printStackTrace();
		} catch (CSException e) {
			e.printStackTrace();
		}
		
		return productId;
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
