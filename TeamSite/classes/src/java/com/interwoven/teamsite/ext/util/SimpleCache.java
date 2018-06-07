package com.interwoven.teamsite.ext.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author nbamford
 * Simple Cache class
 */
public class SimpleCache 
{
	private Log log = LogFactory.getLog(SimpleCache.class);
	private int maximumCacheSize = 10;
	private Map cache;
	private Map accessLog;
	
	
	public SimpleCache(int maximumCacheSize)
	{
		this.maximumCacheSize = maximumCacheSize;
		cache = new HashMap();
		accessLog = new TreeMap(new ComparableComparator());
	}
	
	public SimpleCache()
	{
		this(10);
	}

	/**
	 * Add an object to the cache
	 * will remove the item which was used least if maximum limit in cache reached
	 * @param key
	 * @param value
	 */
	public void addToCache(Object key, Object value)
	{
		if(!cache.containsKey(key))
		{
			synchronized (accessLog) 
			{
			
				log.info(MessageFormat.format("Cache does not contain key {0}" , new Object[]{key}));
				if(cache.size() == maximumCacheSize)
				{
					//Get hold of the last used object in the cache
					Object accessLogDeleteObj = accessLog.keySet().toArray()[0];
					Object cacheDeleteObj = cache.get(accessLog.get(accessLogDeleteObj));
					
					log.info(MessageFormat.format("Current cache size is at the maximum of {0} objects", new Object[]{"" + maximumCacheSize}));
					//Delete something from the cache
					Object val = accessLog.get(accessLogDeleteObj);
					log.info(MessageFormat.format("Removing object with key {0} and value {1} from cache", new Object[]{accessLogDeleteObj, val}));

					//Remove objects from access log and cache
					accessLog.remove(accessLogDeleteObj);
					cache.remove(cacheDeleteObj);
				}
				log.info(MessageFormat.format("Adding object with key {0} to cache.", new Object[]{key}));
				cache.put(key, value);
				updateAccessLog(key);
			}
		}
	}
	
	public void removeFromCache(Object key)
	{
		synchronized (accessLog) 
		{
			cache.remove(key);
		}		
	}
	
	/**
	 * Get an object from the cache. Returns null if not in there
	 * @param key
	 * @return
	 */
	public Object getFromCache(Object key)
	{
		Object retObj = null;
		synchronized (accessLog) 
		{
			if(cache.containsKey(key))
			{
				retObj = cache.get(key);
				//Update last used time
				updateAccessLog(key);
			}
		}
		return retObj;
	}

	private void updateAccessLog(Object key)
	{ 
		log.info(MessageFormat.format("Updating Access log with key {0}", new Object[]{key}));
		
		//If we don't have this sleep we run the risk of having non unique times for the keys
		try 
		{
			Thread.sleep(50);
		} 
		catch (InterruptedException e) 
		{
		}
		
		Object oldKey = getKeyForValue(key, accessLog);
		
		if(oldKey != null)
		{
			accessLog.remove(oldKey);
		}
		
		accessLog.put(new Long(getCurrentTimeAsLong()), key);
		
	}
	
	private Long getCurrentTimeAsLong()
	{
		return new Long(System.currentTimeMillis());
	}
	
	private Object getKeyForValue(Object value, Map map)
	{
		Object retVal = null;
		Set keySet = map.keySet();
		for (Iterator iter = keySet.iterator(); iter.hasNext();) {
			Object key = iter.next();
			Object storedValue = map.get(key);
			if(storedValue.equals(value))
			{
				retVal = key;
			}
		}
		return retVal;
	}
	
	public Map getAccessLog()
	{
		return accessLog;
	}

}