package com.interwoven.teamsite.nikon.od;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.od.adapter.delivery.base.IWODDeliveryAdapter;
import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.nikon.managers.JCSCustomCachingManager;

/**
 * This class is responsible for wrapping the {@link JCSCustomCachingManager} class
 * from Open Deploy. In the Open Deploy config you should pass a 
 * <Parameter>PAR_LIVESITE_RUNTIME_URL=?</Parameter>
 * where ? = the runtime url for the JCS JSP
 * @deprecated
 * @author nbamford
 *
 */
public class JCSCustomCachingRegionInvalidationIWOD
extends IWODDeliveryAdapter{

	private final Log log = LogFactory.getLog(JCSCustomCachingRegionInvalidationIWOD.class);
	private Properties props;
	private JCSCustomCachingManager cusCacheMan;

	/* (non-Javadoc)
	 * @see com.interwoven.od.adapter.payload.base.IWODFileRetrieval#getFileList(java.lang.String, java.lang.String, java.lang.String, boolean, java.util.Map)
	 *
	 */


	/**
	 * Default constructor required
	 */
	public JCSCustomCachingRegionInvalidationIWOD(){}

	/* (non-Javadoc)
	 * @see com.interwoven.od.adapter.delivery.base.IWODDeliveryAdapter#deploy()
	 */
	public boolean deploy()
	{
		logMsg("Entering public boolean deploy()");

		//Build the properties
		PropertiesBuilder propBuilder = new PropertiesBuilder(getParam(), PropertiesBuilder.CSV_PROPS);
		Properties properties = propBuilder.buildProperties();
		cusCacheMan = new JCSCustomCachingManager(properties);
		Map<URL, Throwable> failed = cusCacheMan.invalidateCache();
		//Report if failed
		if(failed.size() > 0)
		{
			logMsg("Failed to invalidate some regions");
			for(URL url : failed.keySet())
			{
				logMsg(url.getPath());
			}
		}

		logMsg("Exiting public boolean deploy()");
		
		//Always return true. This may change
		return true;
	}
	
	private void logMsg(String msg)
	{
		logMsg(msg, null);
	}
	
	private void logMsg(String msg, Throwable throwable)
	{
		if(throwable == null)
		{
			log.debug(msg);
		}
		else 
		{
			log.debug(msg, throwable);
		}
	}
}
