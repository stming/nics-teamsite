package com.interwoven.teamsite.nikon.od;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.interwoven.od.adapter.delivery.base.IWODDeliveryAdapter;
import com.interwoven.od.adapter.payload.base.IWODFileRetrieval;
import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.ext.util.FileUtils;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.managers.JCSCustomCachingManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @deprecated
 * @author nbamford
 *
 */
public class JCSCacheInvalidationIWOD 
extends IWODDeliveryAdapter{

	private Log log = LogFactory.getLog(JCSCacheInvalidationIWOD.class);

	JCSCustomCachingManager customCachingMan;

	public JCSCacheInvalidationIWOD(){}
	/* (non-Javadoc)
	 * @see com.interwoven.od.adapter.delivery.base.IWODDeliveryAdapter#deploy()
	 */
	public boolean deploy()
	{
		FormatUtils.pFormat("Entering deploy()");
		PropertiesBuilder propBuilder = new PropertiesBuilder(getParam(), PropertiesBuilder.CSV_PROPS);
		Properties props = propBuilder.buildProperties();


		String flatFileString = props.getProperty(NikonDomainConstants.PAR_FLAT_FILE_NAME);

		List<String> dcrFiles = new LinkedList<String>();
		List<String> pageFiles = new LinkedList<String>();
		String dcrRegex = props.getProperty(NikonDomainConstants.PAR_DCR_LIST_REGEX_INC);
		String livesiteHome = props.getProperty(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL);

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
					
					if((FileUtils.fileExtension(file) == null) || ("".equals(FileUtils.fileExtension(file))))
					{
						//If were one we're interested in
						String fSlashFileName = FormatUtils.allFSlash(file.getAbsolutePath());
						FormatUtils.pFormat("Looking at no extension file {0} with regex {1}", fSlashFileName, dcrRegex);
						if(fSlashFileName.matches(dcrRegex))
						{
							FormatUtils.pFormat("Adding {0} to list of DCR Files we're interested in", fSlashFileName);
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
		
		Map<URL, Throwable> failed = customCachingMan.invalidateCache();
		
		//Report if failed
		if((failed != null) && (failed.size() > 0))
		{
			for(URL url : failed.keySet())
			{
				log.debug(FormatUtils.mFormat("Failed"));
				log.debug(FormatUtils.mFormat("======"));
				log.debug(FormatUtils.mFormat(url.toString()));
				log.debug(FormatUtils.mFormat(failed.get(url).getMessage()));
				log.debug(FormatUtils.mFormat("------"));
			}
			log.debug(FormatUtils.mFormat("Failed to invalidate some regions"));
		}		

		log.debug(FormatUtils.mFormat("Exiting deploy()"));
		return true;
	}



	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

}
