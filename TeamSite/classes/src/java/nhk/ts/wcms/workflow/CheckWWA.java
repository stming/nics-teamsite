package nhk.ts.wcms.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Pattern;

import nhk.ts.wcms.common.Logger;
import java.net.*;
import java.io.*;
import org.apache.commons.logging.LogFactory;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import nhk.ts.wcms.common.IOHelper;

public class CheckWWA implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.CheckWWA"));
    private final String WWA_CHECK_ENABLED = "CheckWWA.CheckIsEnabled";
    private final String WWA_CHECK_URL = "CheckWWA.URL";

    @SuppressWarnings("rawtypes")
	public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        
    	 String deployType = task.getWorkflow().getVariable("deployType");
    	 Pattern prodDevCodePattern = Pattern.compile("(^Q\\d{4,5}$|^R\\d{3}$|^T\\d{3}$|^U\\d{4}$|^X\\d{4}$|^V\\d{4}$)");
		
		boolean wwaBooleanCheck = true;
		
    	try {
        	
        	CSAreaRelativePath[] jobFiles = task.getFiles();
        	Date today = new Date();
        	if (jobFiles.length > 0) {
        	
        		for (int i = 0; i < jobFiles.length; i++) {
        			String fileRelPath = jobFiles[i].toString();
        			
					CSSimpleFile localeFile = (CSSimpleFile) task.getArea().getFile(jobFiles[i]);
							
					int kind = (localeFile == null) ? -1 : localeFile.getKind();
					mLogger.createLogDebug("File: "  + fileRelPath + ", kind flag: " + kind);
					if ((localeFile != null) && 
						(localeFile.isValid()) && 
						(localeFile.getKind() == CSSimpleFile.KIND)) {
						
						String wwa = localeFile.getExtendedAttribute("TeamSite/Metadata/prod_wwa_date").getValue(); //2013-09-02 12:00:00
						String product_name = localeFile.getExtendedAttribute("TeamSite/Metadata/prod_short_name").getValue();
						String isproduct_related = localeFile.getExtendedAttribute("TeamSite/Metadata/isRelated").getValue();
						String skipNCWWA = localeFile.getExtendedAttribute("TeamSite/Metadata/skip_nc_wwa").getValue();
						if (isproduct_related == null){
							isproduct_related = "";
						}

						
						mLogger.createLogDebug("WWA Date: " + wwa);
			            mLogger.createLogDebug("Product Name: " + product_name);
			            mLogger.createLogDebug("Is Product Related: " + isproduct_related);
			            mLogger.createLogDebug("Skip NC WWA: " + skipNCWWA);
			            
			            if (wwa != null && !"".equals(wwa)){
			            	SimpleDateFormat parserSDF =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            	Date wwaDate = parserSDF.parse(wwa);
			            	if (wwaDate == null){
			            		wwaBooleanCheck = false;
			            		mLogger.createLogDebug("Unable to parse wwaDate: " + wwa);
			            	}else if (wwaDate.after(today)){
			            		wwaBooleanCheck = false;
			            		mLogger.createLogDebug("WWA date is after today, will return false: " + "[" + fileRelPath + "], Date: ["+wwa+"]");
			            	}
			            }
			            
			            if (!wwaBooleanCheck){
			            	mLogger.createLogDebug("WWA Date is in the future, will callback");
                        	task.chooseTransition("WWA Check Failed", "WWA Check Failed for file: ["+fileRelPath+"], ");
                        	return;	
			            }
			            
			            mLogger.createLogDebug("Past the WWA Date check for file ["+fileRelPath+"], will continue...");
			            
			            	
		            	String product_devcode = localeFile.getExtendedAttribute("TeamSite/Metadata/prod_dev_code").getValue();
						
		            	if (product_devcode != null && !"".equals(product_devcode)){
		            		mLogger.createLogDebug("product_devcode not null, will check for dev code pattern: ["+product_devcode+"]");
		            		if (prodDevCodePattern.matcher(product_devcode).matches()){
		            			mLogger.createLogDebug("Product dev code pattern matches, will attempt to see whether it require to contact NC");
		            			if (IOHelper.getPropertyValue(WWA_CHECK_ENABLED).equalsIgnoreCase("Yes")) {
			                        if ("Yes".equals(skipNCWWA)){
			                        	mLogger.createLogDebug("skipNCWWA Check flag enable, will skip NC WWA Check this file: ["+fileRelPath+"]");
			                        }else{
				                        try {
				                            
				                        	mLogger.createLogDebug("Dev Code to check for WWA: " + product_devcode);
				                            
				                        	String wwaCheckURL = IOHelper.getPropertyValue(WWA_CHECK_URL);
				                            
				                        	mLogger.createLogDebug("URL to check for WWA: " + wwaCheckURL);
				                            
				                        	String url = wwaCheckURL + product_devcode;
				                            
				                        	URL WWAurl = new URL(url);
				                            URLConnection yc = WWAurl.openConnection();
				                            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
				                            String WWAcheck = in.readLine();
				                            
				                            mLogger.createLogDebug("Result for WWA: " + WWAcheck );
				                            
				                            if (WWAcheck.equalsIgnoreCase("false")) {
				                            	
				                            	wwaBooleanCheck = false;
				                            
				                            }

				                            in.close();
				                            
				        	        		if (!wwaBooleanCheck) {	
				        						mLogger.createLogDebug("WWA check failed");
				        		        		task.chooseTransition("WWA Check Failed", "WWA Check Failed for dev code: ["+product_devcode+"], file: ["+fileRelPath+"]");
				        		        		return;
				        					}			                            
				                            
				                        } catch (Exception e) {
				                            
				                        	mLogger.createLogError("Error checking WWA Date", e);
				                            
				                        	task.chooseTransition("WWA Check Failed", "Exception while WWA Check: [" + e.toString() + "]");
				                        	return;
				                        }			                        	
			                        }

			                        
			                    } else {
			                    	mLogger.createLogDebug("WWA Check disabled, will process to the next file");
			                    }
		            		
		            		}else{
		            			mLogger.createLogDebug("Dev code not matches, will process to the next file");
		            			
		            		}
		            		
		            	}
			            if (isproduct_related.equalsIgnoreCase("y") || isproduct_related.equalsIgnoreCase("1")) {

			            	String product_relates_to = localeFile.getExtendedAttribute("TeamSite/Metadata/relates_to_product").getValue();

		                    if (IOHelper.getPropertyValue(WWA_CHECK_ENABLED).equalsIgnoreCase("Yes")) {
		                    	if ("Yes".equals(skipNCWWA)){
		                        	mLogger.createLogDebug("skipNCWWA Check flag enable, will skip NC WWA Check this file: ["+fileRelPath+"]");
		                        }else{
		                        	String[] relatedProducts = product_relates_to.split(","); 
			                    	
			                    	for (int j = 0; j < relatedProducts.length; j++) {
	 			                    	String relatedDevCode = relatedProducts[j].trim();
			                    		mLogger.createLogDebug("relatedDevCode not null, will check for dev code pattern: ["+relatedDevCode+"]");
					            		if (prodDevCodePattern.matcher(relatedDevCode).matches()){
					            			try {
					                            
					                        	mLogger.createLogDebug("Dev Code to check for WWA: " + relatedDevCode);
					                            
					                        	String wwaCheckURL = IOHelper.getPropertyValue(WWA_CHECK_URL);
					                            
					                        	mLogger.createLogDebug("URL to check for WWA: " + wwaCheckURL);
					                            
					                        	String url = wwaCheckURL + relatedDevCode;
					                            
					                        	URL WWAurl = new URL(url);
					                            URLConnection yc = WWAurl.openConnection();
					                            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
					                            String WWAcheck = in.readLine();
					                            mLogger.createLogDebug("Result for WWA: " + WWAcheck );
					                            
					                            if (WWAcheck.equalsIgnoreCase("false")) {
					                            	wwaBooleanCheck = false;
					                            }
					                            in.close();
					                            
					        	        		if (!wwaBooleanCheck) {	
					        						mLogger.createLogDebug("WWA check failed");
					        		        		task.chooseTransition("WWA Check Failed", "WWA Check Failed for related dev code: ["+product_devcode+"], file: ["+fileRelPath+"]");
					        		        		return;
					        					}				                            
					                            
					                            
					                        } catch (Exception e) {
					                        	mLogger.createLogError("Error checking WWA Date", e);
					                        	task.chooseTransition("WWA Check Failed", "Exception while WWA Check: [" + e.toString() + "]");
					                        	return;
					                        }
					            			
					            		}else{
					            			mLogger.createLogDebug("Dev code not matches, will process to the next file");
					            		}
			                    	}		                        	
		                        }
		                    	
		                        
		                    }
		                    
		                }
					}	
        		}
        		
        		if (!wwaBooleanCheck) {
					
					mLogger.createLogDebug("WWA check failed");
                    
	        		task.chooseTransition("WWA Check Failed", "WWA Check Failed");
	        		return;
				}
        	}
        	
        	if (wwaBooleanCheck) {
                
        		mLogger.createLogDebug("Deployment when WWA webservice returns true");
                
        		task.chooseTransition("WWA Check Successful", "Successfully checked WWA");
        		return;
            
        	} 
    	
    	} catch (Exception e) {
    		mLogger.createLogError(e.toString(), e);
    		task.chooseTransition("WWA Check Failed", "Exception while WWA Check: [" + e.toString() + "]");
    		return;
        
    	}
    }
}
