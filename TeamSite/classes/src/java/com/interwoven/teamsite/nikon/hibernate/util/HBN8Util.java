package com.interwoven.teamsite.nikon.hibernate.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;

import com.interwoven.livesite.spring.ApplicationContextUtils;


/**
 * @author nbamford
 * Utility class for HBN8 functionality
 */
public class HBN8Util 
{
	private static Log log = LogFactory.getLog(HBN8Util.class);
	
	/**
	 * Method to return a SessionFactory from the Spring container given a name
	 * @param beanName
	 * @return HBN8 SessionFactory
	 * @throws BeanDefinitionStoreException
	 */
	public static SessionFactory getHibernateSessionFactory(String beanName)
	throws BeanDefinitionStoreException
	{
		ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
		Object o = appCtx.getBean(beanName);
		
		
		if(o == null)
		{
			throw new BeanDefinitionStoreException("Bean of name" + beanName + " not found in ApplicationContext");
		}

		return (SessionFactory)o;
	}

    protected static Map<String, SessionFactory> configuredMap = new LinkedHashMap<String, SessionFactory>();
    
    /**
     * Method to return a SessionFactory given a the configuration file in configFileResourceName
     * @param configFileResourceName
     * @return HBN8 Session Factory
     * @throws ExceptionInInitializerError
     */
    public static SessionFactory getConfiguredSessionFactory(String configFileResourceName)
    throws ExceptionInInitializerError
    {
    	SessionFactory retFact = null;
		synchronized (configuredMap) 
		{
			if(configuredMap.containsKey(configFileResourceName))
			{
				retFact = configuredMap.get(configFileResourceName);
			}
			else	
			{
                try {
                    // Create the SessionFactory from Other.cfg.xml
                	retFact = new Configuration().configure(configFileResourceName).buildSessionFactory();
                	configuredMap.put(configFileResourceName, retFact);
                } catch (Throwable ex) {
                    log.error("Initial SessionFactory creation failed." + ex);
                    throw new ExceptionInInitializerError(ex);
                }
			}
		}
		return retFact;
    }
}
