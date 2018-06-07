package com.interwoven.teamsite.ext.common;

/**
 * Simple Interface to give notion of an environment, the options being
 * Development, Testing, Staging or Production. Will be used to shape behaviour
 * accordingly and should be set via Spring configuration and injected into interested
 * classes
 * 
 * @author nbamford
 *
 */
public interface TeamsiteEnvironment 
{
	public final static String DEVELOPMENT = "DEVELOPMENT";
	public final static String TESTING     = "TESTING";
	public final static String STAGING     = "STAGING";
	public final static String PRODUCTION  = "PRODUCTION";

	/**
	 * Set environment name DEVELOPMENT, TESTING, STAGING or PRODUCTION
	 * @param environment
	 */
	public void setEnvironment(String environment);
	
	/**
	 * Return environment name DEVELOPMENT, TESTING, STAGING or PRODUCTION
	 * @return
	 */
	public String getEnvironment();

	/**
	 * Returns true if environment set to equalsIgnoreCase("DEVELOPMENT");
	 * @return
	 */
	public boolean isDevelopment();
	
	/**
	 * Returns true if environment set to equalsIgnoreCase("TESTING");
	 * @return
	 */
	public boolean isTesting();
	
	/**
	 * Returns true if environment set to equalsIgnoreCase("STAGING");
	 * @return
	 */
	public boolean isStaging();
	
	/**
	 * Returns true if environment set to equalsIgnoreCase("PRODUCTION");
	 * @return
	 */
	public boolean isProduction();
}
