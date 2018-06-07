package com.interwoven.teamsite.nikon.common;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;

import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.common.AbstractTeamsiteEnvironment;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;

/**
 * Class which will contain information about the Environment we are working in.
 * @author nbamford
 *
 */
public class NikonTeamsiteEnvironmentX 
extends AbstractTeamsiteEnvironment 
{
	public static final String beanName = "nikon.teamsite.Environment";

	public NikonTeamsiteEnvironmentX()
	{
		
	}
	
	public NikonTeamsiteEnvironmentX(String environment)
	{
		setEnvironment(environment);
	}

	public static final TeamsiteEnvironment getSpringApplicationContextInstance()
	throws BeanDefinitionStoreException
	{
		ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
		Object o = null;
		
		if(appCtx != null)
		{
			o = appCtx.getBean(beanName);
		
		
			if(o == null)
			{
				
				throw new BeanDefinitionStoreException("Bean of name" + beanName + " not found in ApplicationContext");
			}
		}

		return (TeamsiteEnvironment)o;
	}

}
