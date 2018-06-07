package com.interwoven.teamsite.ext.hibernate.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.mapping.Collection;

import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironmentAware;

/**
 * Abstract Adapter class to make a Hibernate DAO manager TeamsiteEnvironmentAware
 * 
 * @author nbamford
 *
 */
/**
 * @author nbamford
 *
 */
public class AbstractTSEnvAwareHibernateDAOManager 
extends AbstractHibernateDAOManager
implements TeamsiteEnvironmentAware
{
	TeamsiteEnvironment teamsiteEnvironment;
	protected Log log = LogFactory.getLog(this.getClass());
	
	/* (non-Javadoc)
	 * @see com.interwoven.teamsite.ext.common.TeamsiteEnvironmentAware#getTeamsiteEnvironment()
	 */
	public TeamsiteEnvironment getTeamsiteEnvironment() {
		return this.teamsiteEnvironment;
	}

	/* (non-Javadoc)
	 * @see com.interwoven.teamsite.ext.common.TeamsiteEnvironmentAware#setTeamsiteEnvironment(com.interwoven.teamsite.ext.common.TeamsiteEnvironment)
	 */
	public void setTeamsiteEnvironment(TeamsiteEnvironment teamsiteEnvironment) {
		this.teamsiteEnvironment = teamsiteEnvironment;
	}

	/* (non-Javadoc)
	 * @see com.interwoven.teamsite.ext.common.TeamsiteEnvironment#getEnvironment()
	 */
	public String getEnvironment() {
		return teamsiteEnvironment != null?teamsiteEnvironment.getEnvironment():TeamsiteEnvironment.PRODUCTION;
	}

	/* (non-Javadoc)
	 * @see com.interwoven.teamsite.ext.common.TeamsiteEnvironment#isDevelopment()
	 */
	public boolean isDevelopment() {
		return teamsiteEnvironment != null?teamsiteEnvironment.isDevelopment():false;
	}

	public boolean isProduction() {
		return teamsiteEnvironment != null?teamsiteEnvironment.isProduction():true;
	}

	public boolean isStaging() {
		return teamsiteEnvironment != null?teamsiteEnvironment.isStaging():false;
	}

	public boolean isTesting() {
		return teamsiteEnvironment != null?teamsiteEnvironment.isTesting():false;
	}

	public void setEnvironment(String environment) {
		if (teamsiteEnvironment != null){
			teamsiteEnvironment.setEnvironment(environment);
		}
	}

	protected void enableFilter(boolean enable, String filterName)
	{
		if(enable)
		{
			getSession().enableFilter(filterName);
		}
	}
	protected void enableFilter(boolean enable, String filterName, Object paramName, Object value)
	{
		if((filterName == null || (paramName == null) || (value == null)))
		{
			throw new HibernateException("Something wrong in the data passed to enable the filter");
		}
		
		if(enable)
		{
			Filter filter = getSession().enableFilter(filterName);

			//Single parameter
			if(String.class.isAssignableFrom(paramName.getClass()))
			{
				if(Object[].class.isAssignableFrom(value.getClass()))
				{
					filter.setParameterList((String)paramName, (Object[])value);
				}
				else if (Collection.class.isAssignableFrom(value.getClass()))
				{
					filter.setParameterList((String)paramName, (Collection[])value);
				}
				else
				{
					filter.setParameter((String)paramName, value);
				}
			}
			//Multiple parameters
			else if ((String[].class.isAssignableFrom(paramName.getClass()) && (Object[].class.isAssignableFrom(value.getClass()))))
			{
				String[] paramArray = (String[])paramName;
				
				for(int i=0; i < paramArray.length; i++){
					filter.setParameter(paramArray[i], ((Object[])value)[i]);
				}
			}
			//Don't know
			else
			{
				throw new HibernateException("Can't determine what you're trying to pass to filter");
			}
		}
	}
	
}
