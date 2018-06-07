package com.interwoven.teamsite.nikon.workflow;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

/**
 * This WF Task is for the invalidation of the custom caching regions
 * Currently just region 
 * 
 * @author nbamford
 * @deprecated
 *
 */
public class JCSCustomCachingRegionInvalidationWFTask 
implements CSURLExternalTask {

	private final Log log = LogFactory.getLog(JCSCustomCachingRegionInvalidationWFTask.class);
	private JCSCustomCachingManager customCachingMan;
	
	/* (non-Javadoc)
	 * @see com.interwoven.cssdk.workflow.CSURLExternalTask#execute(com.interwoven.cssdk.common.CSClient, com.interwoven.cssdk.workflow.CSExternalTask, java.util.Hashtable)
	 */
	public void execute(CSClient client, CSExternalTask task, Hashtable arg2)
	throws CSException{
		//Create the properties
		PropertiesBuilder propBuild = new PropertiesBuilder(task);
		propBuild.addParamKey(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL);
		Properties props = propBuild.buildProperties();
		
		
//		try
//		{
//			System.out.println(FormatUtils.mFormat("{0}:{1}", JCSCustomCachingManager.PAR_LIVESITE_RUNTIME_PAGE_LIST, task.getWorkflow().getVariable(JCSCustomCachingManager.PAR_LIVESITE_RUNTIME_PAGE_LIST)));
//			props.put(JCSCustomCachingManager.PAR_LIVESITE_RUNTIME_PAGE_LIST, task.getWorkflow().getVariable(JCSCustomCachingManager.PAR_LIVESITE_RUNTIME_PAGE_LIST));
//		}
//		catch(Throwable t)
//		{
//			t.printStackTrace(System.out);
//		}

		customCachingMan = new JCSCustomCachingManager(props);
		
		//Invalidate the cache
		Map<URL, Throwable> failed = customCachingMan.invalidateCache();
		
		//Report if failed
		if(failed.size() > 0)
		{
			for(URL url : failed.keySet())
			{
				log.debug("Failed");
				log.debug(url.toString());
				log.debug(failed.get(url).getMessage());
			}
			log.fatal("Failed to invalidate some regions");
		}
		
		String nextTransition = task.getTransitions()[0];
		task.chooseTransition(nextTransition, "Cache Invalidation Complete");
		
	}	
}
