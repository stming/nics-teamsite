package com.interwoven.teamsite.nikon.workflow;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSAssociation;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.sci.filesys.CSFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;


/**
 * @author Arnout Cator, Rashid Siddiqui
 * URL External Task checking the WWA date on DCRs
 * a.	If TeamSite/Metadata/prod_wwa_date is null
 * b.   then build a List of files that do not have this set
 * c.   send the workflow to an email notification task
 */
public class AttachDependencies implements CSURLExternalTask {
	// Define a LOGGER for debugging purpose
	private static final Log log = LogFactory.getLog(AttachDependencies.class);
	
	/**
	 * URL External Task execute method.
	 */
	public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
		//get all files attached
		CSAreaRelativePath[] files = task.getFiles();

		for (int i = 0; i < files.length; i++) {
			CSSimpleFile simpleFile = null;
			try {
				simpleFile = (CSSimpleFile) task.getArea().getFile(files[i]);
				log.debug("Found simpleFile: " + simpleFile.getName());
				
				if(simpleFile.getKind() == CSSimpleFile.KIND && simpleFile.getContentKind() == CSSimpleFile.kDCR){
					//if it's a DCR process it.
					log.debug(simpleFile.getName()+ " is a DCR, process it.");
					processDCR(simpleFile, task);
				}else{
					String strAsset = simpleFile.getName();
					Pattern pattern = Pattern.compile("(.+(\\.(?i)(page|site))$)");
					Matcher matcher = pattern.matcher(strAsset);
					if(matcher.matches()){
						//if it's a .page or .site file, get file association
						log.debug(strAsset+ " is a sitepublisher file, process it.");
						processPage(simpleFile, task);						
					}
				}
			} catch (Exception e) {
				log.error("Exception in execute() : " + e.toString());
			}
		}
		
		task.chooseTransition(task.getTransitions()[0], "All related files attached");
		
	}
	
	public void processPage(CSSimpleFile simpleFile, CSExternalTask task){
		log.debug("enter processPage");
		try{
			//CSAssociation[] arrAssociations = simpleFile.getParentAssociations(null);
			//get deployable association
			CSAssociation[] arrAssociations = simpleFile.getParentAssociations("Sitepub.Deployable");
			
			List<CSAreaRelativePath> csPathList = new ArrayList<CSAreaRelativePath>();
			
			for(int i=0; i<arrAssociations.length; i++){
				log.debug(arrAssociations[i].getType() + "    " + arrAssociations[i].getSecondary().getUAI());
				CSVPath csVPath = new CSVPath(arrAssociations[i].getSecondary().getUAI());
				CSAreaRelativePath csARPath = csVPath.getAreaRelativePath();
				//check if file exists
				if(csARPath.isEmpty()){
					log.debug("File does not exist");
				}else{
					log.debug("File exists. Add this file to ArrayList");
					csPathList.add(csARPath);
					
					//if it's also a DCR, process it.
					CSSimpleFile associatedFile = (CSSimpleFile) arrAssociations[i].getSecondary();
					log.debug(associatedFile.getName()+ " is a DCR, process it.");
					processDCR(associatedFile, task);
				}
			}
			
			if(csPathList.size()>0){
				log.info("csPathList size > 0");
				try{
					log.info("Attach files to this task");
					CSAreaRelativePath[] csPathArr = csPathList.toArray(new CSAreaRelativePath[csPathList.size()]);
					task.attachFiles(csPathArr);
				}catch(Exception e){
					log.error("Exception in processPage() : " + e.toString());
				}
			}
			
		}catch(Exception e){
			log.error("Exception in processPage() : " + e.toString());
		}
	}
	
	

	public void processDCR(CSSimpleFile simpleFile, CSExternalTask task){
		List<String> assetList = new ArrayList<String>();
		log.debug("enter processDCR");
		String strDCRContent = retrieveFileAsString(simpleFile);
		log.debug("strDCRContent = "+strDCRContent);
		if(! "".equals(strDCRContent)){
			// Get all image files from the DCR using these regex.
			//my @withQuotes    = $dcrContent =~ m|"\/(.*?)\"|gsm  ;
		    //my @withoutQuotes = $dcrContent =~ m|>\/(.*?)<|gsm  ;
		    //my @justQuotes    = $dcrContent =~ m|file=\"(.*?)\"|gsm  ;
		    //my @allImgs = $dcrContent =~ m|imported(.*?)["<]|gsm ;
			//my @tmpImgs = $dcrContent =~ m|tmp(.*?)["<]|gsm ;
			//my @resourceImgs = $dcrContent =~ m|resources(.*?)["<]|gsm ;  #added 20130816
			Pattern pattern = Pattern.compile("\"\\/(.*?)\\\"");
			Matcher matcher = pattern.matcher(strDCRContent);
			while (matcher.find()) {
				assetList.add(matcher.group(1));
	        }
	        pattern = Pattern.compile(">\\/(.*?)<");
	        matcher = pattern.matcher(strDCRContent);
	        while (matcher.find()) {
	        	assetList.add(matcher.group(1));
	        }
	        pattern = Pattern.compile("file=\\\"(.*?)\\\"");
	        matcher = pattern.matcher(strDCRContent);
	        while (matcher.find()) {
	        	assetList.add(matcher.group(1));
	        }
	        pattern = Pattern.compile("(imported.*?)[\"<]");
	        matcher = pattern.matcher(strDCRContent);
	        while (matcher.find()) {
	        	assetList.add(matcher.group(1));
	        }
	        pattern = Pattern.compile("(tmp.*?)[\"<]");
	        matcher = pattern.matcher(strDCRContent);
	        while (matcher.find()) {
	        	assetList.add(matcher.group(1));
	        }
	        pattern = Pattern.compile("(resources.*?)[\"<]");
	        matcher = pattern.matcher(strDCRContent);
	        while (matcher.find()) {
	        	assetList.add(matcher.group(1));
	        }
		}
		
		List<CSAreaRelativePath> csPathList = new ArrayList<CSAreaRelativePath>();
		
		for (Iterator<String> iterator = assetList.iterator(); iterator.hasNext();) {
			String strAsset = (String) iterator.next();
			log.debug("strAsset = "+ strAsset);
			String strFileExtns = "(.+(\\.(?i)(gif|png|jpg|jpeg|tiff|tif|svg|doc|xls|ppt|swf|css|js|xml|pdf))$)";
			//Check whether the file is an image file.  If yes, attach to workflow or else skip
			Pattern pattern = Pattern.compile(strFileExtns);
			Matcher matcher = pattern.matcher(strAsset);
			if(matcher.matches()){
				// check whether the file exists
				log.debug("File extension matches");
				CSAreaRelativePath csPath = new CSAreaRelativePath(strAsset);
				if(csPath.isEmpty()){
					log.debug("File does not exist");
				}else{
					log.debug("File exists");
					csPathList.add(csPath);
				}
			}
		}
		if(csPathList.size()>0){
			log.debug("csPathList size > 0");
			try{
				log.debug("Attach files to this task");
				CSAreaRelativePath[] csPathArr = csPathList.toArray(new CSAreaRelativePath[csPathList.size()]);
				task.attachFiles(csPathArr);
			}catch(Exception e){
				log.error("Exception in processDCR() : " + e.toString());
			}
		}
	}
	
	//====================================================================
	// retrieveXML()
	//====================================================================
	public String retrieveFileAsString(CSSimpleFile file) {

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

		} catch (Exception e) {
			log.error("Exception in retrieveFileAsString() : " + e.toString());
		} finally {
        	try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
                log.error("Exception in retrieveFileAsString() : " + e.toString());
            }
        }

        return xml;
	}
}