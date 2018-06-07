package com.interwoven.teamsite.nikon.od.dnr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.ext.util.FileUtils;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.managers.JCSCustomCachingManager;


/**
 * This class is a DNR Wrapper for the manager class com.interwoven.teamsite.nikon.managers.JCSCustomCachingManager
 * It looks at the manifest flat file and builds a list of DCRs which fit a regex pattern and Pages
 * this list is then sent to the manager class which is then called to invalidate
 * 
 * It takes the following JVM Parameters
 * 
 * PAR_DCR_LIST_REGEX_INC    - Regex for DCR names to include
 * PAR_FLAT_FILE_NAME        - Mannifest file location
 * PAR_LIVESITE_RUNTIME_HOME - Runtime home of Livesite to find the manifest file
 * PAR_LIVESITE_RUNTIME_URL  - Comma Seperated list of servers ip:port,ip2:port of Cache invalidation
 * PAR_URL_DELAY_MILLIS      - Used to delay the cache invalidation calls, a String 100 = 100ms 1000=1000ms = 1s
 *    
 * @author nbamford
 *
 */
public class JCSCacheInvalidationDNR { 
	
	private final Log log = LogFactory.getLog(JCSCacheInvalidationDNR.class);
	private String[] args;
	private Properties props;
	JCSCustomCachingManager customCachingMan;

	JCSCacheInvalidationDNR(String[] args)
	{
		log.debug(FormatUtils.mFormat("Creating instance of {0}", this.getClass().getName()));
		int cnt = 0;
		for(String arg: args)
		{
			log.debug(FormatUtils.mFormat("arg[{0}]:{1}",cnt++, arg));
		}
		this.args = args;
	}


	public static void main(String[] args)
	{

		JCSCacheInvalidationDNR m = new JCSCacheInvalidationDNR(args);
		m.buildProperties();
		m.process();
	} 

	void process()
	{
		log.debug(FormatUtils.mFormat("Entering process()"));
		
		String flatFileString = props.getProperty(NikonDomainConstants.PAR_FLAT_FILE_NAME);

		List<String> dcrFiles = new LinkedList<String>();
		List<String> pageFiles = new LinkedList<String>();
		String dcrRegex = props.getProperty(NikonDomainConstants.PAR_DCR_LIST_REGEX_INC);
		String livesiteHome = props.getProperty(NikonDomainConstants.PAR_LIVESITE_RUNTIME_HOME);

		//Check the manifestFileString is not null and not an empty String
		if((flatFileString != null) && (!"".equals(flatFileString)))
		{
			File fileListFile = new File(flatFileString);

			FileReader fileReader;
			try {
				fileReader = new FileReader(fileListFile);
				BufferedReader bufferedReader = new BufferedReader(fileReader);

				String deployedFileName = null;
				while((deployedFileName = bufferedReader.readLine()) != null)
				{
					String fullFileName = FormatUtils.mFormat("{0}/{1}", livesiteHome, deployedFileName);
					fullFileName = FormatUtils.allBSlash(fullFileName);
					File file = new File(fullFileName);
					
					//DCRS and matches dcrRegex
					if((FileUtils.fileExtension(file) == null) || ("".equals(FileUtils.fileExtension(file))))
					{
						//If were one we're interested in
						String fSlashFileName = FormatUtils.allFSlash(file.getAbsolutePath());
						log.debug(FormatUtils.mFormat("Looking at no extension file {0} with regex {1}", fSlashFileName, dcrRegex));
						if(fSlashFileName.matches(dcrRegex))
						{
							log.debug(FormatUtils.mFormat("Adding {0} to list of DCR Files we're interested in", fSlashFileName));
							dcrFiles.add(fSlashFileName);
						}
							
					}
					//Pages
					else if("page".equalsIgnoreCase(FileUtils.fileExtension(file)))
					{
						pageFiles.add(file.getAbsolutePath());
					}
				}

			} catch (FileNotFoundException e) {
				log.error("FileNotFoundException", e);
			} catch (IOException e) {
				log.error("IOException", e);
			}
		}
		else
		{
			log.debug(FormatUtils.mFormat("Manifest file not found"));
		}

		props.put(NikonDomainConstants.PAR_DCR_LIST, dcrFiles);
		props.put(NikonDomainConstants.PAR_PAGE_LIST, pageFiles);
		customCachingMan = new JCSCustomCachingManager(props);
		
		//Invalidate the cache here
		Map<URL, Throwable> failed = customCachingMan.invalidateCache();
		
		//Report if failed
		if((failed != null) && (failed.size() > 0))
		{
			for(URL url : failed.keySet())
			{
				log.error("Failed");
				log.error(FormatUtils.mFormat("======"));
				log.error(FormatUtils.mFormat(url.toString()));
				log.error(FormatUtils.mFormat(failed.get(url).getMessage()));
				log.debug(FormatUtils.mFormat("------"));
			}
			log.error(FormatUtils.mFormat("Failed to invalidate some regions"));
		}		

		log.debug(FormatUtils.mFormat("Exiting process()"));
	}

	void buildProperties()
	{
		PropertiesBuilder propBuild = new PropertiesBuilder(args);
		props = propBuild.buildProperties();
	}
}
