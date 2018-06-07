package com.interwoven.teamsite.nikon.workflow;

import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSBinaryExtendedAttribute;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.managers.WWADateDBCheckingManager;
import com.interwoven.teamsite.nikon.util.NikonUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Workflow Task is responsible for getting the latest current highest wwa_date for a set of product dev codes
 * in the underlying DCR
 * In the WF Task we need to pass the following variables 
 * 
 * 	PAR_DB_PROPERTIES_FILE    - Location of the database properties file to load 
 *	PAR_DB_DRIVER_KEY         - Key name of the JDBC Driver class to use from the above file, e.g. <b>development.driverClassName</b>=com.microsoft.sqlserver.jdbc.SQLServerDriver   
 *  PAR_DB_URL_KEY            - Key name of the JDBC URL to use from the above file e.g.  <b>development.url</b>=jdbc:jtds:sqlserver://10.100.162.15:1433:database=acc_ls_dev
 *	PAR_DB_USRNM_KEY          - Key name of the JDBC user name to use from the above file e.g. <b>development.username</b>=iw_sql_acc_dev 
 *	PAR_DB_PWD_KEY            - Key name of the JDBC password to use from the above file e.g. <b>development.password</b>=*********
 *
 *  PAR_PDCWWA_WA_REGEX_MATCH - Regex to match the work area name to 
 * 
 * TODO
 * Need to cater for all interesting asset typed, .jpg, .pdf etc this would require creating an extra XML file
 * with the data in though.
 * @author nbamford
 *
 */
public class ProdDevCodeWWADateCheckingWFTask implements CSURLExternalTask {

	private static final Log log = LogFactory.getLog(ProdDevCodeWWADateCheckingWFTask.class);
	private WWADateDBCheckingManager wwaMan;
	
	
	// Here we just check the WWA Date to see if it has changed and if it has we write back to the DCR itself
	public void execute(CSClient client, CSExternalTask task, Hashtable arg2)
	throws CSException {
		log.debug("Entering public void execute(CSClient client, CSExternalTask task, Hashtable arg2)");
		
		//We use a Properties Build Class here to normalise the process. In this
		//instance we want the values from the CSExternalTask passed in with the 
		//Workflow configuration
		PropertiesBuilder propBuild = new PropertiesBuilder(task);
		propBuild.addParamKey(WWADateDBCheckingManager.PAR_DB_PROPERTIES_FILE);
		propBuild.addParamKey(WWADateDBCheckingManager.PAR_DB_DRIVER_KEY);
		propBuild.addParamKey(WWADateDBCheckingManager.PAR_DB_URL_KEY);
		propBuild.addParamKey(WWADateDBCheckingManager.PAR_DB_USRNM_KEY);
		propBuild.addParamKey(WWADateDBCheckingManager.PAR_DB_PWD_KEY);
		propBuild.addParamKey(NikonDomainConstants.PAR_PDCWWA_WA_REGEX_MATCH);
		
		Properties prop = propBuild.buildProperties();
		wwaMan = new WWADateDBCheckingManager(prop);

		//Determine the rexeg to match the VPath on
		String waRegexMatchPattern = FormatUtils.nvl(prop.getProperty(NikonDomainConstants.PAR_PDCWWA_WA_REGEX_MATCH), ".*/main_wa$");
		
		CSAreaRelativePath[] files = task.getFiles();

		for(com.interwoven.cssdk.filesys.CSAreaRelativePath cs:files)
		{
			try
			{
				String vPathString = task.getArea().getBranch().getVPath().toString();
				String parentPath = cs.getParentPath().toString();
				String fileName = cs.getName();
				
				//Choose a VPath we want based on the reqex sent in
				CSVPath vPath = NikonUtils.vPathFromCSWorkAreaArrayFirstRegexMatch(task.getArea().getBranch().getWorkareas(), waRegexMatchPattern);
				vPathString = vPath != null?vPath.toString():vPathString;
				
				String extension = cs.getExtension();

				String fullFileName = FormatUtils.mFormat("{0}/{1}/{2}", vPathString, parentPath, fileName);
				String relativePath = FormatUtils.mFormat("{0}/{1}", parentPath, fileName);

				int workAreaLength = task.getArea().getBranch().getWorkareas().length;
				log.debug("workAreaLength:" + workAreaLength);
				
				for(CSWorkarea wa : task.getArea().getBranch().getWorkareas())
				{
					log.debug("-->" + wa.getVPath().toString());
				}
				
				log.debug("relativePath:" + relativePath);


				CSAreaRelativePath relPathFile = new CSAreaRelativePath(relativePath);
				CSSimpleFile csSimpleFile = (CSSimpleFile)task.getArea().getFile(relPathFile); 
				log.debug("fullFileName: " + fullFileName);

				//Only DCRs
				if(extension == null)
				{

					String prodRelatedDevCodes = csSimpleFile.getExtendedAttribute(NikonDomainConstants.EXT_ATT_REALTES_TO_PRODUCT).getValue();
					String nikonLocale = csSimpleFile.getExtendedAttribute(NikonDomainConstants.EXT_ATT_NIKON_LOCALE).getValue();
					//If not null then go check
					
					//Also need to eliminate ""
					if((!"".equals(prodRelatedDevCodes) && (prodRelatedDevCodes != null)))
					{
						//New Date in correct format
						String formattedWWADate = wwaMan.getWWADateFromProdDevCodeListString(prodRelatedDevCodes, nikonLocale);
						log.debug(FormatUtils.mFormat("formattedWWADate:{0}", formattedWWADate));


						CSBinaryExtendedAttribute extAtt = new CSBinaryExtendedAttribute(NikonDomainConstants.EXT_ATT_PROD_WWA_DATE, formattedWWADate.getBytes(), 0, formattedWWADate.length(), true);
						CSBinaryExtendedAttribute[] extAttArr = new CSBinaryExtendedAttribute[1];
						extAttArr[0] = extAtt;

						csSimpleFile.setBinaryExtendedAttributes(extAttArr);
					}
				}
			}
			catch(Exception exception)
			{
				log.warn("Exception occured ", exception);
			}
		}
		
		//Success
		log.debug("Exiting public void execute(CSClient client, CSExternalTask task, Hashtable arg2)");
		String nextTransition = task.getTransitions()[0];
		task.chooseTransition(nextTransition, "Prod Dev Code WWA Date Checker");
	}
}
