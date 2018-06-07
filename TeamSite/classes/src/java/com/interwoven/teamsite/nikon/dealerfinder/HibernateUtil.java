package com.interwoven.teamsite.nikon.dealerfinder;

import java.io.File;

import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;

import com.interwoven.teamsite.nikon.dealerfinder.exceptions.DAOException;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author Mike
 */
public class HibernateUtil
{
    
    private volatile static ApplicationContext context;
    
    private static final String BEAN_NAME = "nikon.teamsite.hibernate.SessionFactory";
    
    public static void init(ApplicationContext context){
    	if (HibernateUtil.context != null){
    		return;
    	}else{
    		synchronized (HibernateUtil.class) {
    			// Double check, fields set to volatile so it should be fine
    			if (HibernateUtil.context == null)
    				HibernateUtil.context = context;
			}
    	}
    		
    	
    }

    public static SessionFactory getSessionFactory() throws DAOException
    {
    	if (context == null)
    		throw new DAOException("Need to init Hibernate Util");
        return (SessionFactory) HibernateUtil.context.getBean(BEAN_NAME);
    }
}
