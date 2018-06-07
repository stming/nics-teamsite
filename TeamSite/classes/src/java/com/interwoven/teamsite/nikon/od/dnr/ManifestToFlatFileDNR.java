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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class ManifestToFlatFileDNR {
	
	private Log log = LogFactory.getLog(ManifestToFlatFileDNR.class);

	private String[] args;
	private Properties props;

	ManifestToFlatFileDNR(String[] args)
	{
		log.debug("Creating instance of " + this.getClass().getName());
		for(String arg: args)
		{
			log.debug(arg);
		}
		this.args = args;
	}


	public static void main(String[] args)
	{

		ManifestToFlatFileDNR m = new ManifestToFlatFileDNR(args);
		m.buildProperties();
		m.process();
	} 

	void process()
	{

		log.debug("Entering process()");

		String manifestFileString = props.getProperty(NikonDomainConstants.PAR_MANIFEST_FILE_NAME);
		String flatFileString = props.getProperty(NikonDomainConstants.PAR_FLAT_FILE_NAME);

		//Check the manifestFileString is not null and not an empty String
		if((manifestFileString != null) && (!"".equals(manifestFileString)))
		{
			File manifestFile = new File(manifestFileString);

			if((manifestFile.exists()) && (manifestFile.isFile()))
			{
				//Create the output file
				File flatFile = new File(flatFileString);
				try {
					FileWriter frFileWriter = new FileWriter(flatFile);
					BufferedWriter bufWriter = new BufferedWriter(frFileWriter);
					Document doc = null;
					doc = Utils.fileToXML(manifestFile);

					List nodes = doc.selectNodes("/iwodManifest/item");

					int cnt = 1;
					for(Object o: nodes)
					{
						Element e = (Element)o;
						String typeVal = e.attributeValue("type");
						if("FILE".equalsIgnoreCase(typeVal))
						{
							String pathVal = e.attributeValue("path");

							bufWriter.write(pathVal);
							//Don't write the final new line
							if(cnt++ < nodes.size())
							{
								bufWriter.newLine();
							}
						}					
						else
						{
							cnt++;
						}
					}

					//Add the flat file to the list of files to deploy too as we need on the target
					bufWriter.flush();
					frFileWriter.close();
				} catch (Throwable e1) {
					log.error("Throwable", e1);
				}
			}
			else
			{
				log.debug("Manifest file not found");
				log.error("Manifest file not found");
			}
		}
		else
		{

		}

		log.debug("Exiting process()");
	}

	void buildProperties()
	{
		PropertiesBuilder propBuild = new PropertiesBuilder(args);
		props = propBuild.buildProperties();
	}
}
