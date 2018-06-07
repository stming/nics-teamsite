package com.interwoven.teamsite.nikon.workflow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.externals.NikonLiveSiteHBN8ExternalDelegate;
import com.interwoven.teamsite.nikon.repository.NikonRepository;
import com.interwoven.teamsite.nikon.repository.NikonStaticFileRepository;
import com.interwoven.teamsite.nikon.springx.NikonBusinessManager;

public class GenerateRepositoryFiles implements CSURLExternalTask {

	private Logger logger = Logger.getLogger(GenerateRepositoryFiles.class);

	
	public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
		try {
			logger.debug("Attempt to generate Repository file for task: " + task.getId());
			CSAreaRelativePath[] jobFiles = task.getFiles();
			
			String vPath = task.getArea().getVPath().toString();
			
			String fallbackDCR = task.getVariable("fallbackDCR");
			String mountPoint = task.getVariable("iwMountPoint");
			String updateEA = task.getVariable("updateEA");
			if (updateEA == null){
				updateEA = "false";
			}
			
			logger.debug("Retrieving configured beans from Application Context in task: " + task.getId());
			NikonBusinessManager businessManager = (NikonBusinessManager) ApplicationContextUtils.getBean("nikon.hibernate.dao.manager");
			TeamsiteEnvironment env = (TeamsiteEnvironment) ApplicationContextUtils.getBean("nikon.teamsite.Environment");

			
			if (businessManager == null || env == null){
				throw new Exception("Fail to find the beans, please verify beans configuration");
			}
			logger.debug("Creating repository based on the vpath " + vPath);
			NikonStaticFileRepository repository = new NikonStaticFileRepository();
			repository.setCacheEnable(false);
			repository.setCommerceCacheEnable(false);
			repository.setEnable(true);
			repository.setLocaleFallbackDCRPath(fallbackDCR);
			String basePath = vPath;
			if (basePath.startsWith("//")){
				basePath = basePath.replaceAll("//.*/default/(.*)", "/default/$1");
			}
			repository.setBasePath(mountPoint + basePath + "/repository");
			HashMap<String, List<String>> localeIds = new HashMap<String, List<String>>();
			
			if (jobFiles.length > 0) {
				logger.debug("Processing job files, total size: "+jobFiles.length+"...");
				String templateMatch = "^templatedata/.*/product_information_container/data/.*$";
				
				for (int i = 0; i < jobFiles.length; i++) {

					String fileRelPath = jobFiles[i].toString();
						   fileRelPath = fileRelPath.replaceAll("\\\\", "\\/");

					//------------------------------------------------------------------
					// If a Product or Accessory...
					//------------------------------------------------------------------
					if (fileRelPath.matches(templateMatch)) {
						logger.debug("Product DCR found, will try to get the ID: " + fileRelPath);
						String locale = fileRelPath;
						locale = locale.replaceAll("^templatedata/(.*)/product_information_container/data/.*$", "$1");
						
						String fileVPath = task.getArea().getVPath().toString() + "/" + fileRelPath;
					           fileVPath = fileVPath.replaceAll("\\\\", "\\/");
					       
					    CSVPath csVpath = new CSVPath(fileVPath);
						CSFile dcrFile = client.getFile(csVpath);
						
						if ((dcrFile != null) && 
				  		    (dcrFile.isValid()) && 
				  			(dcrFile.getKind() == CSSimpleFile.KIND)) {
				  			
		  				    CSExtendedAttribute[] extAttrs = ((CSSimpleFile)dcrFile).getExtendedAttributes(null);
		  									
		  					for (int j = 0; j < extAttrs.length; j++) {
		  						
		  						String name = extAttrs[j].name;
		  						String value = extAttrs[j].value;
		  						
		  						if (name.equals("TeamSite/Metadata/prod_id")) {
		  							logger.debug("Product ID found: " + value + " in locale: "+ locale +", will add to the locale/id map");
		  							if (localeIds.containsKey(locale)){
		  								localeIds.get(locale).add(value);
		  							}else{
		  								List<String> prodIds = new ArrayList<String>();
		  								prodIds.add(value);
		  								localeIds.put(locale, prodIds);
		  							}
		  							
		  						}
		  					}
						}else{
							logger.warn("File does not exist? may be deleted");
						}
					}
		        }				
			}
			logger.debug("Number of locales it need to geenrate: " + localeIds.size());
			if (localeIds.size() > 0){

				for (Iterator<String> locales = localeIds.keySet().iterator();locales.hasNext();){
					String locale = locales.next();
					List<String> ids = localeIds.get(locale);
					logger.debug("Generating locale " + locale + ", number of id: " + ids.size());
					NikonLiveSiteHBN8ExternalDelegate delegate = new NikonLiveSiteHBN8ExternalDelegate(businessManager, env, repository);
					HBN8QueryParamDTO param = new HBN8QueryParamDTO(null, env);
					param.setTeamsiteEnvironment(env);
					param.setMode(NikonHBN8ParamConstants.MODE_GENERATE);
					param.setRepo(repository);
					param.setEnableWWAFilter(false);
					param.setSourcePath(mountPoint + basePath);
					param.setNikonLocale(locale);
					param.setSiteCountryCode(FormatUtils.countryCode(param.getNikonLocale()));		
					param.setLanguageCode(FormatUtils.languageCode(param.getNikonLocale()));
					param.setCountryCode(FormatUtils.countryCode(param.getNikonLocale()));
					ArrayList<String> fileUpdateList = new ArrayList<String>();
					for (int i=0;i<ids.size();i++){
						logger.debug("Generating product id: " + ids.get(i));
						param.setProductId(ids.get(i));
						Document document = delegate.listProductDetails(param, null);
						if (document != null){
							Node node = document.selectSingleNode("//updatedRelationshipFile");
							if (node != null){
								String idList = node.getText();
								if (idList != null && !"".equals(idList)){
									String[] relationshipIds = idList.split(",");
									for (String id : relationshipIds){
										fileUpdateList.add(id);
									}
								}
							}
							node = document.selectSingleNode("//productDCRPath");
							if (node != null){
								String dcrPath = node.getText();
								fileUpdateList.add(ids.get(i) + "_fragment.xml|"+ dcrPath);
								fileUpdateList.add(ids.get(i) + ".xml|"+ dcrPath);
								fileUpdateList.add(ids.get(i) + "_org.xml|"+ dcrPath);
								fileUpdateList.add(ids.get(i) + "_relationship.properties|"+ dcrPath);
							}else{
								logger.debug("No node for productDCRPath ["+ids.get(i)+"], probably unable to find the product (locale optout?)");
							}
						}else{
							logger.debug("No Document response for Product id: ["+ids.get(i)+"] (locale optout?)");
						}
					}
					logger.debug("Finish generating, will attempt to add file into task, total number of file: " + fileUpdateList.size());
					if (fileUpdateList.size() > 0){
						List<CSAreaRelativePath> filesToAdd = new ArrayList<CSAreaRelativePath>();
						for (int i=0;i<fileUpdateList.size();i++){
							String pathAndDcr = fileUpdateList.get(i);
							
				        	String genFile = pathAndDcr;
							genFile = genFile.replaceAll("(.*)\\|.*", "$1");
							
							String dcrFile = pathAndDcr;
							dcrFile = dcrFile.replaceAll(".*\\|(.*)", "$1");
							dcrFile = dcrFile.replaceAll("\\\\", "\\/");
				        	String genVpath = task.getArea().getVPath().toString() + "/repository/" + locale + "/product/" + genFile;
				            
				        	genVpath = genVpath.replaceAll("\\\\", "\\/");
				        
							CSVPath genCSVpath = new CSVPath(genVpath);
							       
							CSFile dcrGenFile =  client.getFile(genCSVpath);
							
							if ((dcrGenFile != null) && 
							    (dcrGenFile.isValid()) && 
								(dcrGenFile.getKind() == CSSimpleFile.KIND)) {
								
								if ("true".equals(updateEA)){
									CSExtendedAttribute[] extAttrs = new CSExtendedAttribute[1];
									extAttrs[0].setName("TeamSite/LiveSite/DependentURIs");
									extAttrs[0].setValue(dcrFile);
									
									((CSSimpleFile)dcrGenFile).setExtendedAttributes(extAttrs);									
								}
								logger.debug("Adding file into List: " + genVpath);
								filesToAdd.add(genCSVpath.getAreaRelativePath());
							}
							
						}
						if (!filesToAdd.isEmpty()){
							logger.debug("Attaching into the task, total number of files: " + filesToAdd.size());
							task.attachFiles(filesToAdd.toArray(new CSAreaRelativePath[filesToAdd.size()]));
						}
					}

				}
		
			}
			
			
	        task.chooseTransition("Generate Repository Files Success", "Repository Generated...");
			
		} catch (Exception e) {
	
			logger.error("Error while trying to generate Repository file", e);
	
			task.chooseTransition("Generate Repository Files Failed", "Generate Failure...");
		}
	}
	//====================================================================
	// execute()
	//====================================================================
	@SuppressWarnings({ "rawtypes" })
	public void executeOld(CSClient client, CSExternalTask task, Hashtable params) throws CSException {

		try {
	
			CSAreaRelativePath[] jobFiles = task.getFiles();
			
			int jobId = task.getWorkflowId();
			
			String vPath = task.getArea().getVPath().toString();
			
			String INPUT_FILELIST_PATH = task.getVariable("INPUT_FILELIST_PATH");
			String OUTPUT_FILELIST_PATH = task.getVariable("OUTPUT_FILELIST_PATH");
			String COMMAND_LINE = task.getVariable("COMMAND_LINE");
			String LOCALE = "";
			
			if (jobFiles.length > 0) {
	
				String templateMatch = "^templatedata/.*/product_information_container/data/.*$";
				
				//------------------------------------------------------------------
				// Process Workflow DCRs
				//------------------------------------------------------------------
				ArrayList<String> dcrs = new ArrayList<String>();
				
		        for (int i = 0; i < jobFiles.length; i++) {

					String fileRelPath = jobFiles[i].toString();
						   fileRelPath = fileRelPath.replaceAll("\\\\", "\\/");

					//------------------------------------------------------------------
					// If a Product or Accessory...
					//------------------------------------------------------------------
					if (fileRelPath.matches(templateMatch)) {
						
						LOCALE = fileRelPath;
						LOCALE = LOCALE.replaceAll("^templatedata/(.*)/product_information_container/data/.*$", "$1");
						
						String fileVPath = task.getArea().getVPath().toString() + "/" + fileRelPath;
					           fileVPath = fileVPath.replaceAll("\\\\", "\\/");
					       
					    CSVPath csVpath = new CSVPath(fileVPath);
						CSSimpleFile dcrFile = (CSSimpleFile) client.getFile(csVpath);
						
						if ((dcrFile != null) && 
				  		    (dcrFile.isValid()) && 
				  			(dcrFile.getKind() == CSSimpleFile.KIND)) {
				  			
		  				    CSExtendedAttribute[] extAttrs = dcrFile.getExtendedAttributes(null);
		  									
		  					for (int j = 0; j < extAttrs.length; j++) {
		  						
		  						String name = extAttrs[j].name;
		  						String value = extAttrs[j].value;
		  						
		  						if (name.equals("TeamSite/Metadata/prod_id")) {
				
		  							dcrs.add(value);
		  						}
		  					}
						}
					}
		        }
		        
		        //------------------------------------------------------------------
		        // Create Processing Filelist
				//------------------------------------------------------------------
		        INPUT_FILELIST_PATH = INPUT_FILELIST_PATH + "/" + LOCALE + "_" + jobId + "_in.txt";
		        OUTPUT_FILELIST_PATH = OUTPUT_FILELIST_PATH + "/" + LOCALE + "_" + jobId + "_out.txt";
		        
		        createFileList(INPUT_FILELIST_PATH, dcrs);
		        
		        //------------------------------------------------------------------
		        // Run Generation Code
				//------------------------------------------------------------------
		        // Build Command Line Options
		        String options = INPUT_FILELIST_PATH + " " +
		        				 OUTPUT_FILELIST_PATH + " " +
		        				 LOCALE + " " +
		        				 vPath;
		        
		        options.replaceAll("\\\\", "\\/");
		        
		        logger.debug("Options: " + options);
		        
		        // Run Command Line
		        runCommandLine(COMMAND_LINE, options);
		        
		        // Wait For 1 Minute
		        try {
	               Thread.sleep(60000);
	            } catch ( java.lang.InterruptedException ie) {
	                System.out.println(ie);
	            }
		        
		        //------------------------------------------------------------------
		        // Open Processing Output And Attach Files To Workflow
				//------------------------------------------------------------------
		        ArrayList<String> generatedFiles = retrieveOutput(OUTPUT_FILELIST_PATH);
		        
		        CSAreaRelativePath[] filesToAdd = new CSAreaRelativePath[generatedFiles.size()];
		        
		        int count = 0;
		        
		        for (String generatedFile : generatedFiles) {
		        	
		        	String genFile = generatedFile;
		        	       genFile = genFile.replaceAll("(.*)\\|.*", "$1");
		        	       
		        	String dcrFile = generatedFile;
		        	       dcrFile = dcrFile.replaceAll(".*\\|(.*)", "$1");
		        	       dcrFile = dcrFile.replaceAll("\\\\", "\\/");
		        	       
		        	System.out.println(genFile);
		        	System.out.println(dcrFile);
		        	       
		        	// Set EA
		        	String genVpath = task.getArea().getVPath().toString() + "/repository/" + LOCALE + "/product/" + genFile;
			               genVpath = genVpath.replaceAll("\\\\", "\\/");
			        
			        CSVPath genCSVpath = new CSVPath(genVpath);
		        	       
		        	CSSimpleFile dcrGenFile = (CSSimpleFile) client.getFile(genCSVpath);
					
					if ((dcrGenFile != null) && 
			  		    (dcrGenFile.isValid()) && 
			  			(dcrGenFile.getKind() == CSSimpleFile.KIND)) {
						
						//CSExtendedAttribute[] extAttrs = new CSExtendedAttribute[1];
				
				       	//extAttrs[0].setName("TeamSite/LiveSite/DependentURIs");
				    	//extAttrs[0].setValue(dcrFile);
					
				    	//dcrGenFile.setExtendedAttributes(extAttrs);
					}
		        	
		        	// Add Gen File To Job
		        	filesToAdd[count] = genCSVpath.getAreaRelativePath();
			        
			        count++;
		        }
		        
		        // Add Files To Task (If Exists)
		        if (filesToAdd.length > 0) {
		        
		        	task.attachFiles(filesToAdd);
		        }
			}
			
			//------------------------------------------------------------------
			// Transition Task
			//------------------------------------------------------------------
	        task.chooseTransition("Generate Repository Success", "Repository Generated...");
		
		} catch (Exception e) {
	
			e.printStackTrace();
	
			task.chooseTransition("Generate Repository Failed", "Generate Failure...");
		}
	}
	
	//====================================================================
	// createFileList()
	//====================================================================
	public void createFileList(String filePath, ArrayList<String> dcrs) throws IOException {
		
		PrintWriter out = new PrintWriter(new FileWriter(filePath)); 
		
		for (String dcrPath: dcrs) {
			
			out.println(dcrPath);
		}
		
		out.close();
	 	
	 	return;
	}
	
	//====================================================================
	// runCommandLine()
	//====================================================================
	public void runCommandLine(String commandLine, String opts) throws IOException, InterruptedException {
		
		Runtime runtime = Runtime.getRuntime();
		
		try {
		    
			Process p = runtime.exec("cmd /c start " + commandLine + " " + opts);
    		
			final int exitVal = p.waitFor();
			
			if (exitVal == 0) {
				
				return;
			}
		    		
		    		
		} catch (IOException ioe) {
		    
			System.out.println(ioe.getMessage() );
		}
	    
		return;
	}
	
	//====================================================================
	// retrieveOutput()
	//====================================================================
	public ArrayList<String> retrieveOutput(String filePath) throws IOException {
	
		ArrayList<String> output = new ArrayList<String>();
		
		BufferedReader in = new BufferedReader(new FileReader(filePath)); 
	
		String text;
		
		while (in.ready()) { 
			
			text = in.readLine(); 
			
			output.add(text); 
		}
			
		in.close();
		
		return output;
	}
}
