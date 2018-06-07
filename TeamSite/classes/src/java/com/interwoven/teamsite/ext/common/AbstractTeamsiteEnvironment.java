package com.interwoven.teamsite.ext.common;

/**
 * @author nbamford
 * Abstract class to use in Adapter of {@link AbstractTeamsiteEnvironment}
 */
public abstract class AbstractTeamsiteEnvironment 
implements TeamsiteEnvironment 
{
	/*
	 * For fail safe operation the default environment should be PRODUCTION. This
	 * under normal circumstances will be the most restrictive and safe
	 */
	String environment = PRODUCTION;
	
	public String getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public boolean isDevelopment() {
		return DEVELOPMENT.equalsIgnoreCase(environment);
	}

	public boolean isProduction() {
		return PRODUCTION.equalsIgnoreCase(environment);
	}

	public boolean isStaging() {
		return STAGING.equalsIgnoreCase(environment);
	}

	public boolean isTesting() {
		return TESTING.equalsIgnoreCase(environment);
	}

}
