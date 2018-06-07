package com.interwoven.teamsite.nikon.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.sci.filesys.CSFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.managers.WWADateDBCheckingManager;
import com.interwoven.teamsite.nikon.util.NikonUtils;

/**
 * This Workflow Task is responsible for the injection of Extended Attributes as XML Attributes
 * in the underlying DCR
 * TODO
 * Need to cater for all interesting asset typed, .jpg, .pdf etc this would require creating an extra XML file
 * with the data in though.
 * @author nbamford
 *
 */
public class ExtendedAttributeInjectionWFTask implements CSURLExternalTask {

	private final Log log = LogFactory.getLog(this.getClass());
	
	public void execute(CSClient client, CSExternalTask task, Hashtable arg2)
	throws CSException {
		
		PropertiesBuilder propBuild = new PropertiesBuilder(task);
		propBuild.addParamKey(NikonDomainConstants.PAR_EXATTI_WA_REGEX_MATCH);
		Properties prop = propBuild.buildProperties();
		
		//Determine the rexeg to match the VPath on
		String waRegexMatchPattern = FormatUtils.nvl(prop.getProperty(NikonDomainConstants.PAR_EXATTI_WA_REGEX_MATCH), ".*/main_wa$");

		CSAreaRelativePath[] files = task.getFiles();
		if (log.isDebugEnabled())
			log.debug(">>>>>>>>>" + files.length);

		for(com.interwoven.cssdk.filesys.CSAreaRelativePath cs:files)
		{
			String vPathString = task.getArea().getBranch().getVPath().toString();
			String parentPath = cs.getParentPath().toString();
			String fileName = cs.getName();
			
			CSVPath vPath = NikonUtils.vPathFromCSWorkAreaArrayFirstRegexMatch(task.getArea().getBranch().getWorkareas(), waRegexMatchPattern);
			vPathString = vPath != null?vPath.toString():vPathString;
			String extension = cs.getExtension();
			
			String fullFileName = FormatUtils.mFormat("{0}/{1}/{2}", vPathString, parentPath, fileName);
			String relativePath = FormatUtils.mFormat("{0}/{1}", parentPath, fileName);
			if (log.isDebugEnabled())
				log.debug("---> fullFileName: " + fullFileName);
			
			try
			{
				File file = new File(fullFileName);
				
				CSAreaRelativePath relPathFile = new CSAreaRelativePath(relativePath);
				CSSimpleFile csSimple = (CSSimpleFile)task.getArea().getFile(relPathFile); 
				if(extension == null)
				{
					if (log.isDebugEnabled())
						log.debug(FormatUtils.mFormat("File {0} exists:{1}", fullFileName, file.exists()));
					
					LinkedList<String> extAttsList = new LinkedList<String>();
					extAttsList.add(NikonDomainConstants.EXT_ATT_REALTES_TO_PRODUCT);
					extAttsList.add(NikonDomainConstants.EXT_ATT_PROD_WWA_DATE);
					extAttsList.add(NikonDomainConstants.EXT_ATT_NIKON_LOCALE);
					extAttsList.add(NikonDomainConstants.EXT_ATT_PRODUCT_ID);
					extAttsList.add(NikonDomainConstants.EXT_ATT_SHORT_NAME);
					extAttsList.add(NikonDomainConstants.EXT_ATT_LOCAL_NAME);
					extAttsList.add(NikonDomainConstants.EXT_ATT_PROD_CATEGORY);
					extAttsList.add(NikonDomainConstants.EXT_ATT_PROD_NAVCAT1);
					extAttsList.add(NikonDomainConstants.EXT_ATT_PROD_NAVCAT2);
					extAttsList.add(NikonDomainConstants.EXT_ATT_PROD_NAVCAT3);
					
					//If we're a product_information_container or accessory_information_container then add the PROD_RELATED
					extAttsList.add(NikonDomainConstants.EXT_ATT_PROD_RELATED );
					
					writeExtAttToXML(fullFileName, csSimple, extAttsList);
				}
				//Actually we don't care about non DCR artefacts going from TS --> ABF Runtime as we don't ensure WWA
				//It's only in the OD DNR from ABF Runtime --> Production RT where we should be concerned
				else if((file.exists()) && ((".page".equalsIgnoreCase(extension)) || ("page".equalsIgnoreCase(extension))))
				{
					Document doc = Utils.fileToXML(file);

					String componentIdList = "";
					List<Node> l = doc.selectNodes("/Page/Page_Content/Component");
					
					//
					for(Node componentNode : l)
					{
						String componentId = ((Element)componentNode).attributeValue("ID");
						Node componentCacheNode = componentNode.selectSingleNode("ContainerProperties/CacheTime");
						if(componentCacheNode != null)
						{
							String componentCacheString = componentCacheNode.getText();
							if(!"0".equals(componentCacheString))
							{
								componentIdList += componentId;
								componentIdList +=  ",";
							}
							if (log.isDebugEnabled())
								log.debug(FormatUtils.mFormat("Component:{0}, Cache:{1}", componentId, componentCacheString));
						}
					}
					
					if(!"".equals(componentIdList))
					{
						componentIdList = componentIdList.substring(0,componentIdList.lastIndexOf(","));
						Map<String, String> map = new LinkedHashMap<String, String>();
						map.put(NikonDomainConstants.ATT_COMPONENT_LIST, componentIdList);
						this.writeAttToXML(fullFileName, csSimple, map);
					}
				}
				else
				{
					
				}
			}
			catch(Exception exception)
			{
				log.error("Exception", exception);
			}
		}
		//Success
		String nextTransition = task.getTransitions()[0];
		task.chooseTransition(nextTransition, "XML Attribute Injection");
	}
	
	//Simple method to write Extended Attributes to the XML File. Usually a DCR
	private void writeExtAttToXML(String fullFileName, CSSimpleFile csSimpleFile, List<String> extAttsList)
	{
		//TODO Get the full filename from CSSimpleFile
		File file = new File(fullFileName);
		if (log.isDebugEnabled())
			log.debug(FormatUtils.mFormat("File Exists:{0}", file.exists()));
		
		Document xml = Utils.fileToXML(file);
		
		//Write the extAtts to the root of the DCR XML
		for(String extAttName: extAttsList)
		{
			try {
				String extAttVal = csSimpleFile.getExtendedAttribute(extAttName).getValue();
				String extAttNamePart = Utils.simpleAttNameFromTSExtendedAttName(extAttName);
				log.debug(FormatUtils.mFormat("Setting extAtt{0}:{1}", extAttNamePart, extAttVal));
				xml.getRootElement().addAttribute("extAtt" + extAttNamePart, extAttVal != null?extAttVal.trim():extAttVal);
			} catch (CSAuthorizationException e) {
				log.error("CSAuthorizationException", e);
			} catch (CSRemoteException e) {
				log.error("CSRemoteException", e);
			} catch (CSObjectNotFoundException e) {
				log.error("CSObjectNotFoundException", e);
			} catch (CSExpiredSessionException e) {
				log.error("CSExpiredSessionException", e);
			} catch (CSException e) {
				log.error("CSException", e);
			}
			catch(Throwable e)
			{
				log.error("Throwable", e);
			}
		}
		
		if (log.isDebugEnabled())
			log.debug(FormatUtils.prettyPrint(xml));
		
		FileOutputStream fos; 
		try {
			fos = new FileOutputStream(file);
			XMLWriter xmlWriter = new XMLWriter(fos);
			xmlWriter.write(xml);
			xmlWriter.flush();
			xmlWriter.close();
			fos.close();			
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException", e);
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException", e);
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if (log.isDebugEnabled()) 
			log.debug("Finished writing " + file.getAbsolutePath());
	}

	private void writeAttToXML(String fullFileName, CSSimpleFile csSimpleFile, Map<String, String> attsMap)
	{
		File file = new File(fullFileName);
		if (log.isDebugEnabled()) 
			log.debug(FormatUtils.mFormat("File Exists:{0}", file.exists()));
		
		Document xml = Utils.fileToXML(file);
		
		//Write the extAtts to the root of the DCR XML
		for(String attName: attsMap.keySet())
		{
			try {
				String attVal = attsMap.get(attName);
				xml.getRootElement().addAttribute(attName, attVal.trim());
			}
			catch(Throwable e)
			{
				log.error("Throwable", e);
			}
		}
		
 
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setNewlines(true);
			XMLWriter xmlWriter = new XMLWriter(fos, outformat);
			xmlWriter.write(xml);
			xmlWriter.flush();
			xmlWriter.close();
			fos.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException", e);
		} catch (IOException e) {
			log.error("IOException", e);
		}
	}
}
