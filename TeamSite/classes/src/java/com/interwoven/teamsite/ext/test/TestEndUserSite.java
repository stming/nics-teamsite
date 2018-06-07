package com.interwoven.teamsite.ext.test;

import com.interwoven.livesite.model.EndUserSite;

public class TestEndUserSite 
extends EndUserSite {

	String branch;

	public String getBranch() {
		return this.branch;
	}
	
	public void setBranch(String branch)
	{
		this.branch = branch;
	}
}
