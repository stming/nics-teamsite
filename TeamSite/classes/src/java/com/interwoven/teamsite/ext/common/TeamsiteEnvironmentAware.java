package com.interwoven.teamsite.ext.common;

/**
 * Interface which denotes that a class or component should be aware of the 
 * environment it is running in
 * 
 * @author nbamford
 *
 */
public interface TeamsiteEnvironmentAware
extends TeamsiteEnvironment
{
	/**
	 * Setter for environment
	 * @param teamsiteEnvironment
	 */
	public void setTeamsiteEnvironment(TeamsiteEnvironment teamsiteEnvironment);
	
	/**
	 * Getter for environment
	 * @return
	 */
	public TeamsiteEnvironment getTeamsiteEnvironment();
	
}
