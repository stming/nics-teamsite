package com.interwoven.teamsite.nikon.managers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TagFindingVisitor;

import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.businessrules.CacheKeyManager;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;

/**
 * Manager class for the JCS custom caching invalidation
 * Takes care of pages, components and custom caching.
 * 
 * The class requires the following properties in the Properties object 
 * passed in the constructor
 *
 * NikonDomainConstants.NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL - Comma Seperated list of servers ip:port,ip2:port of Cache invalidation
 * NikonDomainConstants.PAR_URL_DELAY_MILLIS - Used to delay the cache invalidation calls, a String 100 = 100ms 1000=1000ms = 1s
 * Other values are passed in the Properties object via the calling wrapper Class. Either the OD DNR or the WF Task
 * 
 * @author nbamford
 *
 */
public class JCSCustomCachingManager {
	//Constanst


	//Used to clear a region with a given name
	private final String CMD_REMOVE_FROM_CACHE_REGION = "{0}/iw/admin/JCS.jsp?action=clearRegion&cacheName={1}";
	//Used to clear a keyed value from a cache region, like a page with a given name
	private final String CMD_REMOVE_FROM_CACHE_KEY    = "{0}/iw/admin/JCS.jsp?action=remove&cacheName={1}&key={2}";
	//Used to get the details of components within the cache
	private final String CMD_DETAILS_COMPONENTS      = "{0}/iw/admin/JCS.jsp?action=detail&cacheName=component";

	//Variables
	private Properties properties;
	private static Log log = LogFactory.getLog(JCSCustomCachingManager.class);

	/**
	 * Constructor for manager class
	 * @param properties - Properties for the manager
	 * Should be PAR_LIVESITE_RUNTIME_URL which should point to the URL of the Runtime JCS JSP Admin page
	 * also PAR_PAGE_LIST which is fed in from the client using this manager and is a List<String> of 
	 * absolute file locations.
	 * 
	 */
	public JCSCustomCachingManager(Properties properties)
	{
		log.info("Creating Instance of com.interwoven.teamsite.nikon.managers.JCSCustomCachingManager(Properties properties)");
		this.properties = properties;
	}

	/**
	 * Method to invalidate the custom caching in the runtime
	 * @return Map of <URL, Throwable> key values for URLs not succesfully processed
	 */
	public Map<URL, Throwable> invalidateCache()
	{
		List<URL> invalidationURLs = new LinkedList<URL>();

		//Add the urls before invalidating them
		invalidationURLs.addAll(createComponentInvalidationURLs());
		invalidationURLs.addAll(createPageInvalidationURLs());
		invalidationURLs.addAll(createCustomRegionInvalidationURLs());

		return invalidateCustomCache(invalidationURLs);
	}


	//Creates the URLs to invalidate Pages
	//TODO Refactor
	@SuppressWarnings("unchecked")
	private List<URL> createPageInvalidationURLs()
	{
		log.debug(FormatUtils.mFormat("Entering List<URL> createPageInvalidationURLs()"));
		List<URL> retList = new LinkedList<URL>();
		String commaSeperatedURL = properties.getProperty(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL);
		log.debug(FormatUtils.mFormat("Comma seperated URLs:{0}", commaSeperatedURL));

		for(String livesiteRuntime:commaSeperatedURL.split(",")){
			livesiteRuntime = livesiteRuntime.trim();
			//Get the List<String> of pages to process
			List<String> listOfPages = (List<String>) properties.get(NikonDomainConstants.PAR_PAGE_LIST);

			//Loop through the pages and crate URLS
			for(String fileName: listOfPages)
			{
				//Create the key from the page file name
				String pageKey = CacheKeyManager.pageFileKey(fileName);
				retList.add(buildURL(livesiteRuntime, CMD_REMOVE_FROM_CACHE_KEY, NikonDomainConstants.JCS_REGION_PAGE, pageKey));
			}
		}
		log.debug(FormatUtils.mFormat("Exiting List<URL> createPageInvalidationURLs()"));
		return retList;
	}

	//Creates the URLS to invalidate Components
	//TODO Refactor
	@SuppressWarnings("unchecked")
	private List<URL> createComponentInvalidationURLs()
	{

		log.debug(FormatUtils.mFormat("Entering List<URL> createPageComponentsRegionInvalidationURLs()"));
		List<URL> retList = new LinkedList<URL>();

		//TODO
		String commaSeperatedURL = properties.getProperty(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL);
		log.debug(FormatUtils.mFormat("Comma seperated URLs:{0}", commaSeperatedURL));

		for(String livesiteRuntime:commaSeperatedURL.split(",")){
			livesiteRuntime = livesiteRuntime.trim();
			log.debug(FormatUtils.mFormat("livesiteRuntime:{0}", livesiteRuntime));

			//String livesiteRuntime = properties.getProperty(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL);
			List<String> listOfPages = (List<String>)properties.get(NikonDomainConstants.PAR_PAGE_LIST);

			//Holds the pages and component list we want to invalidate
			Map<String,String> mapOfPagesAndComponents = new LinkedHashMap<String, String>();

			List<String> regexList = null;
			if(listOfPages != null)
			{
				for(String pageLocation: listOfPages)
				{
					File pageFile = new File(pageLocation);
					log.debug(FormatUtils.mFormat("Looking for File {0}", pageFile.getAbsolutePath()));
					if(pageFile.exists() && pageFile.isFile())
					{
						log.debug(FormatUtils.mFormat("Looking at file {0} for components to invalidate", pageFile.getAbsolutePath()));
						//Read as XML
						Document pageDoc = Utils.fileToXML(pageFile);
						//For each page we've already injected a csv list of components which are cached until published
						String componentList = pageDoc.getRootElement().attributeValue(NikonDomainConstants.ATT_COMPONENT_LIST);
						log.debug(FormatUtils.mFormat("componentList:{0}", componentList));
						mapOfPagesAndComponents.put(pageFile.getAbsolutePath(), componentList);
					}
				}
				regexList = toRegex(mapOfPagesAndComponents);
			}
			else
			{
				log.debug(FormatUtils.mFormat("List of Pages NULL"));
			}


			try {
				if(regexList != null)
				{
					TagFindingVisitor tfVisitor = new TagFindingVisitor(new String[]{"a"});
					Parser parser;
					String componentCache = FormatUtils.mFormat(CMD_DETAILS_COMPONENTS, livesiteRuntime);
					log.debug(FormatUtils.mFormat("Looking at URL:{0}", componentCache));
					parser = new Parser(componentCache);
					parser.visitAllNodesWith(tfVisitor);

					org.htmlparser.Node[] nodeArr = tfVisitor.getTags(0);

					//We only visit Anchor/Link tags
					for(org.htmlparser.Node node : nodeArr)
					{
						org.htmlparser.tags.LinkTag aNode = (org.htmlparser.tags.LinkTag)node; 
						String linkText = aNode.getLinkText();
						if(linkText != null && "Remove".equals(linkText.trim()))
						{
							String link = aNode.extractLink();
							log.debug(FormatUtils.mFormat("link:{0}", link));
							for(String regEx: regexList)
							{
								if(link.matches(regEx))
								{
									try 
									{
										//Remove everything past the :
										int lastIndex = link.lastIndexOf(":");
										link = link.substring(0, lastIndex + 1);

										log.debug(FormatUtils.mFormat("Adding URL:{0} to list", link));
										retList.add(new URL(link));
										//TODO May may not put this back in
										//break;
									} 
									catch (MalformedURLException e) 
									{
										log.warn(e);
									}
								}
							}
						}
					}
				}
			} 
			catch (ParserException e) {
				FormatUtils.peFormat(e.getMessage());
			}
			log.debug(FormatUtils.mFormat("Exiting List<URL> createPageComponentsRegionInvalidationURLs()"));
		}
		return retList;
	}

	//Turns a Map of Pages and comma seperated component id's into a list of
	//Regexes
	private List<String> toRegex(Map<String, String> mapOfPagesAndComponents) {
		List<String> retList = new LinkedList<String>();

		//Loop through the pages
		for(String page: mapOfPagesAndComponents.keySet())
		{
			String componentList = mapOfPagesAndComponents.get(page);
			log.debug(FormatUtils.mFormat("Looking for component list in page:{0}", page));
			log.debug(FormatUtils.mFormat("Found componentList:{0}", componentList));
			//Build regex for each page/component combination

			if(componentList != null)
			{
				for(String component: componentList.split(","))
				{
					String pageKey = CacheKeyManager.pageFileKey(page);
					component = component.trim();
					String regEx = FormatUtils.mFormat(".*{0}.*:{1}:.*", pageKey, component);
					log.debug(FormatUtils.mFormat("Adding regex:{0}", regEx));
					retList.add(regEx);
				}
			}
		}
		return retList;
	}

	//Method to create the URL invalidation tasks for the custom caching
	private List<URL> createCustomRegionInvalidationURLs()
	{
		log.debug(FormatUtils.mFormat("Entering List<URL> createCustomRegionInvalidationURLs()"));
		List<URL> retList = new LinkedList<URL>();
		//Get the livesite runtime URL from the properties
		String commaSeperatedURL = properties.getProperty(NikonDomainConstants.PAR_LIVESITE_RUNTIME_URL);
		log.debug(FormatUtils.mFormat("Comma seperated URLs:{0}", commaSeperatedURL));

		for(String livesiteRuntime:commaSeperatedURL.split(",")){
			livesiteRuntime = livesiteRuntime.trim();


			List<String> dcrList = (List<String>) properties.get(NikonDomainConstants.PAR_DCR_LIST);
			//We may want to just flush the cache if we're deploying from ABF RT to PRD RT
			if((NikonDomainConstants.VAL_TRUE.equalsIgnoreCase(NikonDomainConstants.PAR_CLEAR_CUSTOM_CACHE_REGIONS)) || ((dcrList != null) && (dcrList.size() > 0)))
			{
				retList.add(buildURL(livesiteRuntime, CMD_REMOVE_FROM_CACHE_REGION, NikonDomainConstants.JCS_REGION_PROD_DTO));
				retList.add(buildURL(livesiteRuntime, CMD_REMOVE_FROM_CACHE_REGION, NikonDomainConstants.JCS_REGION_PROD_DTO_CAT));
				retList.add(buildURL(livesiteRuntime, CMD_REMOVE_FROM_CACHE_REGION, NikonDomainConstants.JCS_REGION_PROD_DTO_NAV));
				retList.add(buildURL(livesiteRuntime, CMD_REMOVE_FROM_CACHE_REGION, NikonDomainConstants.JCS_REGION_DCR_DOC));
				retList.add(buildURL(livesiteRuntime, CMD_REMOVE_FROM_CACHE_REGION, NikonDomainConstants.JCS_REGION_AWARD_DTO));
				retList.add(buildURL(livesiteRuntime, CMD_REMOVE_FROM_CACHE_REGION, NikonDomainConstants.JCS_REGION_PRESS_LIBRARY_DTO));
			}
		}
		log.debug(FormatUtils.mFormat("Exiting List<URL> createCustomRegionInvalidationURLs()"));
		return retList;
	}

	//Method to open the URL
	private Map<URL, Throwable> invalidateCustomCache(Set<URL> setOfURLs)
	{
		return invalidateCustomCache(Utils.set2List(setOfURLs));
	}

	private Map<URL, Throwable> invalidateCustomCache(List<URL> listOfURLs)
	{
		//Return list of in error URLS
		Map<URL, Throwable> retMap = new LinkedHashMap<URL, Throwable>();
		//Loop through to run
		for(URL url: listOfURLs)
		{
			//If not null then open
			if(url != null)
			{
				log.debug(FormatUtils.mFormat("Opening:{0}", url.toString()));
				try {
					url.openConnection();
					url.openStream();
					url = null;
					
					log.debug(FormatUtils.mFormat("Sleeping for {0} ms", urlDelayInMillis()));
					try 
					{
						Thread.sleep(urlDelayInMillis());
					} 
					catch (InterruptedException e)
					{
						log.debug(FormatUtils.mFormat("Woken up"));
					}
					
				} 
				catch (IOException e) 
				{
					retMap.put(url, e);
				}
			}
		}
		return retMap;
	}
	private URL buildURL(String serverPort, String command, String region)
	{
		return buildURL(serverPort, command, region, null);
	}

	//Overidden to include key as well as region
	private URL buildURL(String serverPort, String command, String region, String key)
	{
		log.debug(FormatUtils.mFormat("serverPort:{0}, command:{1}, region:{2}, key:{3}", serverPort, command, region, key));
		URL retVal = null;
		try {
			//Remove from cache by region. key not needed
			String urlString = null;
			if(CMD_REMOVE_FROM_CACHE_REGION.equals(command))
			{
				urlString = FormatUtils.mFormat(command
						,serverPort
						,region); 

				log.debug(FormatUtils.mFormat("urlString:{0}", urlString));
			}
			//Remove from cache by region and key. The generation of the key
			else if(CMD_REMOVE_FROM_CACHE_KEY.equals(command))
			{
				urlString = FormatUtils.mFormat(command
						,serverPort
						,region
						,URLEncoder.encode(key)); 

				log.debug(FormatUtils.mFormat("urlString:{0}", urlString));
			}

			retVal = new URL(urlString);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.error("MalformedURLException", e);
		}	

		return retVal;
	}
	
	long timeInMillis = -1;
	
	//Method to retrieve the delay time in ms from the parameters
	private long urlDelayInMillis()
	{
		if(timeInMillis == -1)
		{
			try
			{
				timeInMillis = Long.parseLong(properties.getProperty(NikonDomainConstants.PAR_URL_DELAY_MILLIS));
				if(timeInMillis <0)
				{
					throw new NumberFormatException("Value can't be less than 0");
				}
			}
			catch (NumberFormatException e) {
				timeInMillis = 0;
			}
		}
		
		return timeInMillis;
	}
}
