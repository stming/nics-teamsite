package com.interwoven.teamsite.nikon.workflow;

import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.managers.JCSCustomCachingManager;
import com.interwoven.teamsite.nikon.to.WorkflowFileTo;

/**
 * This WF Task is for the invalidation of the custom caching regions
 * This is a workflow wrapper class for the management class com.interwoven.teamsite.nikon.managers.JCSCustomCachingManager
 * It takes the following paramers in the workflow task 
 *
 * PAR_DCR_LIST_REGEX_INC    - Regex for DCR names to include
 * PAR_LIVESITE_RUNTIME_URL  - Comma Seperated list of servers ip:port,ip2:port of Cache invalidation
 * PAR_URL_DELAY_MILLIS      - Used to delay the cache invalidation calls, a String 100 = 100ms 1000=1000ms = 1s
 * 
 * @author nbamford
 *
 */
public class JCSCustomCachingInvalidationWFTask 
extends NikonBaseWFT
implements CSURLExternalTask {

	private final Log log = LogFactory.getLog(JCSCustomCachingInvalidationWFTask.class);
	private JCSCustomCachingManager customCachingMan;
	
	/* (non-Javadoc)
	 * @see com.interwoven.cssdk.workflow.CSURLExternalTask#execute(com.interwoven.cssdk.common.CSClient, com.interwoven.cssdk.workflow.CSExternalTask, java.util.Hashtable)
	 */
	public void execute(CSClient client, CSExternalTask task, Hashtable arg2)
	throws CSException{
		log.debug(FormatUtils.mFormat("Entering execute(CSClient client, CSExternalTask task, Hashtable arg2)"));
		//Create the properties
		PropertiesBuilder propBuild = new PropertiesBuilder(task);
		//These are coming from the Workflow themselves and are configurable
		log.debug(FormatUtils.mFormat("Building Properties"));
		propBuild.addParamKey(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL);
		propBuild.addParamKey(NikonDomainConstants.PAR_DCR_LIST_REGEX_INC);
		propBuild.addParamKey(NikonDomainConstants.PAR_URL_DELAY_MILLIS);
		
		Properties props = propBuild.buildProperties();
		log.debug(FormatUtils.mFormat("{0}:{1}", NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL, props.get(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL)));
		log.debug(FormatUtils.mFormat("{0}:{1}", NikonDomainConstants.PAR_DCR_LIST_REGEX_INC, props.get(NikonDomainConstants.PAR_DCR_LIST_REGEX_INC)));
		
		List<String> dcrFiles = new LinkedList<String>();
		List<String> pageFiles = new LinkedList<String>();
		String dcrRegex = props.getProperty(NikonDomainConstants.PAR_DCR_LIST_REGEX_INC);

		//List through all of the files attached to this task
		for(WorkflowFileTo wfFileTo: this.getFiles(task))
		{ 
			log.debug(FormatUtils.mFormat("Extension:{0}", wfFileTo.getExtension()));
			//if we're a dcr and match the regex we're passing in
			if((wfFileTo.getExtension() == null) || ("".equals(wfFileTo.getExtension())))
			{
				//If were one we're interested in
				String fSlashFileName = FormatUtils.allFSlash(wfFileTo.getFile().getAbsolutePath());
				log.debug(FormatUtils.mFormat("Looking at no extension file {0} with regex {1}", fSlashFileName, dcrRegex));
				if(fSlashFileName.matches(dcrRegex))
				{
					log.debug(FormatUtils.mFormat("Adding {0} to list of DCR Files we're interested in", fSlashFileName));
					dcrFiles.add(fSlashFileName);
				}
					
			}
			//Pages
			else if("page".equalsIgnoreCase(wfFileTo.getExtension()))
			{
				pageFiles.add(wfFileTo.getFile().getAbsolutePath());
			}
		}

		//Add the lists to the Properties file to pass into the manager
		props.put(NikonDomainConstants.PAR_DCR_LIST, dcrFiles);
		props.put(NikonDomainConstants.PAR_PAGE_LIST, pageFiles);
		
		customCachingMan = new JCSCustomCachingManager(props);
		
		//Invalidate the cache
		Map<URL, Throwable> failed = customCachingMan.invalidateCache();
		
		//Report if failed
		if((failed != null) && (failed.size() > 0))
		{
			for(URL url : failed.keySet())
			{
				log.warn("Failed");
				log.warn("======");
				log.warn(url.toString());
				log.warn(failed.get(url).getMessage());
				log.warn("------");
			}
			log.fatal("Failed to invalidate some regions");
		}
		
		log.info("Transitioning to next task");
		String nextTransition = task.getTransitions()[0];
		task.chooseTransition(nextTransition, "Cache Invalidation Complete");
		
		log.debug(FormatUtils.mFormat("Exiting execute(CSClient client, CSExternalTask task, Hashtable arg2)"));
	}	
}
