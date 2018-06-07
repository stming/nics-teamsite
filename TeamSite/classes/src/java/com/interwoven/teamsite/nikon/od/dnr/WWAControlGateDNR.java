package com.interwoven.teamsite.nikon.od.dnr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.managers.WWADateDBCheckingManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WWAControlGateDNR {
	
	private Log log = LogFactory.getLog(WWAControlGateDNR.class);

	private String[] args;
	private Properties props;
	private WWADateDBCheckingManager wwaDateMan;

	WWAControlGateDNR(String[] args)
	{
		log.debug("Creating instance of " + this.getClass().getName());
		for(String arg: args)
		{
			log.debug(arg);
		}
		this.args = args;
		buildProperties();
		wwaDateMan = new WWADateDBCheckingManager(props);
	}


	public static void main(String[] args)
	{

		WWAControlGateDNR m = new WWAControlGateDNR(args);
		m.process();
	} 

	void process()
	{
		log.debug("Entering process()");
		wwaDateMan.createWWAGateRecords();
		log.debug("Exiting process()");		
	}

	void buildProperties()
	{
		PropertiesBuilder propBuild = new PropertiesBuilder(args);
		props = propBuild.buildProperties();
	}
}
