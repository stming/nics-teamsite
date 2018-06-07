package com.interwoven.teamsite.nikon.od;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.od.adapter.delivery.base.IWODDeliveryAdapter;
import com.interwoven.od.adapter.payload.base.IWODFileRetrieval;
import com.interwoven.teamsite.ext.builders.PropertiesBuilder;
import com.interwoven.teamsite.ext.util.FileUtils;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.managers.WWADateDBCheckingManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WWADateCheckingIWODFileRetrieval
extends IWODDeliveryAdapter
implements IWODFileRetrieval{
	
	private Log log = LogFactory.getLog(WWADateCheckingIWODFileRetrieval.class);

	private WWADateDBCheckingManager wwaDateMan;

	private Properties props;
	public static final String PAR_MEDIABIN_URL      = "PAR_MEDIABIN_URL";
	public static final String PAR_URL_CONN_TO       = "PAR_URL_CONN_TO";
	public static final String PAR_URL_READ_TO       = "PAR_URL_READ_TO";
	public static final String PAR_BYPASS_WS         = "PAR_BYPASS_WS";
	//Added so that we can bypass processing for files with a regex, e.g. product_information_container
	public static final String PAR_EXC_WWA_CHK_RGX = "PAR_EXC_WWA_CHK_RGX";
	
	//Added so that we can do an Imediate IF IIF the format should be regex;matches:doesn't match
	//for when we need to check against two differen't work areas for a media bin asset. We only get 
	//the image and not where it came from so we have to derive that in the rt environment
	public static final String PAR_OBF_IIF = "PAR_OBF_IIF";

	private String mediaBinUrl;
	private String urlConnectionTimeout = "15000";
	private String urlReadTimeout = "15000";
	private boolean bypassWebservice;
	private String excludeFromCheckRegex;
	private String parObfIID;
	private IIF iif;
	
	public WWADateCheckingIWODFileRetrieval(){}

	public Iterator getFileList(String area, String fileListFileName, String parameter, boolean isQuery, Map adapterMap) {
		log.debug("Entering public Iterator getFileList(String area, String fileListFileName, String parameter, boolean isQuery, Map adapterMap)");
		//System.out.println("Entering public Iterator getFileList(String area, String fileListFileName, String parameter, boolean isQuery, Map adapterMap)");
		//Build the properties
		PropertiesBuilder propBuilder = new PropertiesBuilder(parameter, PropertiesBuilder.CSV_PROPS);
		props = propBuilder.buildProperties();
		wwaDateMan = new WWADateDBCheckingManager(props);
		mediaBinUrl = props.getProperty(PAR_MEDIABIN_URL);
		urlConnectionTimeout = FormatUtils.nvl(props.getProperty(PAR_URL_CONN_TO), urlConnectionTimeout);
		urlReadTimeout = FormatUtils.nvl(props.getProperty(PAR_URL_READ_TO), urlReadTimeout);
		bypassWebservice = Utils.string2boolean(props.getProperty(PAR_BYPASS_WS));
		excludeFromCheckRegex = props.getProperty(PAR_EXC_WWA_CHK_RGX);
		parObfIID = props.getProperty(PAR_OBF_IIF);
		iif = new IIF(parObfIID, ":");
		
		List<String> fileList = new LinkedList<String>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {
			log.debug("area             :" + area);
			log.debug("fileListFileName :" + fileListFileName);
			log.debug("parameter        :" + parameter);
			log.debug("isQuery          :" + isQuery);
			//System.out.println("area             :" + area);
			//System.out.println("fileListFileName :" + fileListFileName);
			//System.out.println("parameter        :" + parameter);
			//System.out.println("isQuery          :" + isQuery);

			File fileListFile = new File(fileListFileName);
			fileReader = new FileReader(fileListFile);
			bufferedReader = new BufferedReader(fileReader);

			String deployedFileName = null;
			while((deployedFileName = bufferedReader.readLine()) != null) {
				//We may want to bypass the checking for certain files based on the passed in property PAR_EXC_WWA_CHK_RGX
				if((excludeFromCheckRegex != null) && (!"".equals(excludeFromCheckRegex)) && (deployedFileName.matches(excludeFromCheckRegex))) {
					log.debug(FormatUtils.mFormat("File {0} matched regex {1} so bypassing WWA Date Check", fullPath(area, deployedFileName), excludeFromCheckRegex));
					//System.out.println("File {0} matched regex {1} so bypassing WWA Date Check");
					//At this point the regex is not null and not an empty String and
					//the filename matches our regex pattern
					fileList.add(this.fullPath(area, deployedFileName));
				} else if(checkFileForWWADate(area, deployedFileName)) {
					//Otherwise send through the WWA Date checking service
					log.debug(FormatUtils.mFormat("Checking if file {0} has valid WWA Date", fullPath(area, deployedFileName)));
					//System.out.println("Checking if file {0} has valid WWA Date");
					fileList.add(this.fullPath(area, deployedFileName));
				}
			}
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			//System.out.println("Error: " + e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
			//System.out.println("Error: " + e.getMessage());
		}

		log.debug("List of files being returned in the Iterator");
		log.debug("============================================");
		//System.out.println("List of files being returned in the Iterator");
		//System.out.println("============================================");

		for(String s : fileList) {
			log.debug(s);
			//System.out.println(s);
		}

		log.debug("Exiting public Iterator getFileList(String area, String fileListFileName, String parameter, boolean isQuery, Map adapterMap)");
		//System.out.println("Exiting public Iterator getFileList(String area, String fileListFileName, String parameter, boolean isQuery, Map adapterMap)");
		return fileList.iterator();
	}

	//Checks if a given wwa date on a file/asset is valid
	private boolean checkFileForWWADate(String area, String relFileName) {
		log.debug("-->Entering private boolean checkFileForWWADate(String area, String relFileName)");
		//System.out.println("-->Entering private boolean checkFileForWWADate(String area, String relFileName)");
		boolean retVal = false;

		String fullFileNamePath = FormatUtils.mFormat("{0}/{1}", area, relFileName);
		File f = new File(fullFileNamePath.replaceAll("\\\\", "/"));

		//If the file exists
		if(f.exists()) {
			log.debug(FormatUtils.mFormat("File {0} exists", f.getAbsolutePath()));
			//System.out.println("File {0} exists " + f.getAbsolutePath());

			//If empty then assume it's a DCR
			String ext = FileUtils.fileExtension(f);
			if("".equals(ext)) {
				try {
					log.debug(FormatUtils.mFormat("---Loading file {0} as XML", fullPath(area, relFileName)));
					//System.out.println("---Loading file {0} as XML");

					Document doc = Utils.getDom4JDocFromW3CDoc(Utils.getXmlDoc(new File(fullFileNamePath)));
					log.debug("---Looking for attribute " + Utils.deployedDCRAttNameBuilder((Utils.simpleAttNameFromTSExtendedAttName(NikonDomainConstants.EXT_ATT_NIKON_LOCALE))));
					//System.out.println("---Looking for attribute " + Utils.deployedDCRAttNameBuilder((Utils.simpleAttNameFromTSExtendedAttName(NikonDomainConstants.EXT_ATT_NIKON_LOCALE))));
					Element root = doc.getRootElement();

					String nikonLocale;
					String prodDevCodes = null;
					String wwaDate;
					String attName = Utils.deployedDCRAttNameBuilder(Utils.simpleAttNameFromTSExtendedAttName(NikonDomainConstants.EXT_ATT_NIKON_LOCALE));
					log.debug(FormatUtils.mFormat("attName:{0}", attName));
					//System.out.println("attName:{0}");
					Attribute nikonLocaleAtt = root.attribute(attName);

					if(nikonLocaleAtt != null) {
						nikonLocale = nikonLocaleAtt.getValue();
						log.debug(FormatUtils.mFormat("nikonLocale:{0}", nikonLocale));
						//System.out.println("nikonLocale:{0} " + nikonLocale);
					
						/*
						 * Fix for getting the related products attribute value form two different attributes.
						 * 
						 * Historically DCRs used to store their related products in an EA called 'TeamSite/Metadata/relates_to_product', 
						 * this was superseded with another EA called 'TeamSite/Metadata/prod_related', however some DCRs (eg migrated content) still used the old
						 * EA but also have the new EA. New Products etc only have the new one, originally this code did not check for the newer EA.
						 * 
						 * The following changes check for the existence of the new EA first, if that fails it will attempt to find the old EA
						 * 
						 * see NikonDomainConstants.EXT_ATT_PROD_RELATED
						 * 
						 * The above is wrong.
						 * 
						 ***** PLEASE READ *****
						 * 
						 * There are two different attributes used. For Products/Accessories there's the prod_related. For everything else relates_to_product
						 * here we're interested in the everything else i.e we only check for relates_to_product. If it doesn't then revert to using WWA Date
						 *  
						 */
					
						// Start --
						// Get the NikonDomainConstants.EXT_ATT_PROD_RELATED attribute from the DCR (may be null) - This attr is the new style one
						//Please read explanation above at ***** PLEASE READ ***** before re-instating this codes
						boolean crap = true;
					
						if(!crap) {
							Attribute extAtt_prod_relatedAttr = root.attribute(Utils.deployedDCRAttNameBuilder((Utils.simpleAttNameFromTSExtendedAttName(NikonDomainConstants.EXT_ATT_PROD_RELATED))));
						
							// The DCR may not have this attribute so the attr might be null
							if(extAtt_prod_relatedAttr != null) {
								// get the prodDevCodes from the attribute
								prodDevCodes = extAtt_prod_relatedAttr.getValue();	
							}
						
							// If the prodDevCodes are still empty try and get the codes using the older EA name
							if(prodDevCodes == null) {
								Attribute extAtt_realtes_to_productAttr = root.attribute(Utils.deployedDCRAttNameBuilder((Utils.simpleAttNameFromTSExtendedAttName(NikonDomainConstants.EXT_ATT_REALTES_TO_PRODUCT))));
							
								// This also might be null
								if(extAtt_realtes_to_productAttr != null) {
									// get the prodDevCodes from the attribute
									prodDevCodes = extAtt_realtes_to_productAttr.getValue();
								} else {
									log.debug("EA/ATTR NOT SET. This DCR does not contain either the 'prod_related' or 'relates_to_product' attribute." + f.getAbsolutePath());
								}
							}
						}
					
						// Original code for reference
						//Have changed the method being called to attributeValue instead of attribute(...).getValue() so null pointers are wrapped
						prodDevCodes = root.attributeValue(Utils.deployedDCRAttNameBuilder((Utils.simpleAttNameFromTSExtendedAttName(NikonDomainConstants.EXT_ATT_REALTES_TO_PRODUCT))));
						log.debug(FormatUtils.mFormat("prodDevCodes:{0}", prodDevCodes));
						//System.out.println("prodDevCodes:{0} " + prodDevCodes);

						wwaDate = root.attribute(Utils.deployedDCRAttNameBuilder((Utils.simpleAttNameFromTSExtendedAttName(NikonDomainConstants.EXT_ATT_PROD_WWA_DATE )))).getValue();
						log.debug(FormatUtils.mFormat("---wwaDate     :{0}:", wwaDate));
						//System.out.println("---wwaDate     :{0}: " + wwaDate);

						//If it's the prod dev code is not null and empty then look up the value from the database
						//Don't really like this logic, could simplify
						if((prodDevCodes != null) && (!"".equals(prodDevCodes))) {
							log.debug("---prodDevCode not null and looking up value from database");
							//System.out.println("---prodDevCode not null and looking up value from database");
							//						wwaDate = FormatUtils.formatWWADate(checkWWADate(prodDevCodes, nikonLocale));
							if(bypassWebservice) {
								log.debug("Bypassing WebService");
								//System.out.println("Bypassing WebService");
								retVal = wwaDateMan.prodDevCodesWithinWWADB(prodDevCodes, nikonLocale);
							} else {
								log.debug("Calling WebService");
								//System.out.println("Calling WebService");
								retVal = wwaDateMan.prodDevCodesWithinWWADBAndWS(prodDevCodes, nikonLocale);
							}
						
							log.debug("---retVal=" + retVal);
							//System.out.println("---retVal=" + retVal);
						} else {
							log.debug(FormatUtils.mFormat("---No valid prodDevCodes to check against so using original wwaDate:{0}", wwaDate));
							//System.out.println("---No valid prodDevCodes to check against so using original wwaDate:{0} " + wwaDate);
							//At this point we have the correct wwaDate. i.e. if prod_dev_code then dbLookup if not then one passed
							retVal = wwaDateMan.dateWithinWWA(wwaDate);
						}
					} else {
						log.debug(FormatUtils.mFormat("File {0} has not been injected with the correct metadata nikonLocale...", f.getAbsolutePath()));
						//System.out.println("File {0} has not been injected with the correct metadata nikonLocale...");
					}
				} catch(Throwable throwable) {
					throwable.printStackTrace(System.err);
					throwable.printStackTrace(System.out);

					//If the DCR has no attributes for the wwa_date, prod_dev_code and etc.
					//For now let it go
					retVal = false;
				}
			} else {
				//Go and check via the JSP
				//WE NOW check excludeFromCheckRegex for a match. If we get one then we don't 
				//do any WWA Date checking so this is not needed
				//TODO Need to soften this
				boolean crap = true;
				
				if((!crap) && ("page".equalsIgnoreCase(ext)) || 
						("js".equalsIgnoreCase(ext)) || 
						("css".equalsIgnoreCase(ext)) ||
						("site".equalsIgnoreCase(ext)) ||
						("sitemap".equalsIgnoreCase(ext)))
				{
					retVal = true;
				} else {
					try {
						//This needs parameterising
						log.debug(FormatUtils.mFormat("mediaBinUrl:{0}", mediaBinUrl));
						//System.out.println("mediaBinUrl:{0} " + mediaBinUrl);
						
						FormatUtils.peFormat("relFileName:{0}", relFileName);
						relFileName = FormatUtils.allFSlash(relFileName);
						String regex = "tmp/([A-Z][A-Z]|Asia)/.*";
						String countryCodeFromObfVpath = NikonDomainConstants.DEFAULT_COUNTRY;
						if(relFileName.matches(regex)) {
							countryCodeFromObfVpath = relFileName.replaceAll(regex, "$1");
						}

						FormatUtils.pFormat("Country Code Resolved to:{0}", countryCodeFromObfVpath);
						FormatUtils.peFormat("relFileName:{0}", relFileName);

						//This checks to use main_wa or obfuscation_wa via the iif passed in through the parameter PAR_OBF_IIF
//						String urlString = FormatUtils.mFormat(mediaBinUrl, "=", countryCodeFromObfVpath, (relFileName.matches(iif.getRegex())?iif.getTrueCond():iif.getFalseCond()), Utils.URLEncode(relFileName));
						String urlString = FormatUtils.mFormat(mediaBinUrl, "=", countryCodeFromObfVpath, (relFileName.matches(iif.getRegex())?iif.getTrueCond():iif.getFalseCond()), false?relFileName:Utils.URLEncode(relFileName));
						urlString = FormatUtils.allFSlash(urlString);
						log.debug(FormatUtils.mFormat("urlString:{0}", urlString));
						//System.out.println("urlString:{0} " + urlString);
						String xml = doGet(urlString);
						Document doc = Utils.string2XML(xml);
						
//						Document doc = Utils.string2XML(doGetx(urlString));
						List<Node> nl = doc.selectNodes("/metadata/attribute");
						for(Node n: nl) {
							Node name  = n.selectSingleNode("name");
							if("WWA".equals(name.getStringValue())) {
								Node wwaDate = n.selectSingleNode("value");
								String candedateWWADate = wwaDate.getStringValue();
								log.debug(FormatUtils.mFormat("candedateWWADate:{0}", candedateWWADate));
								retVal = wwaDateMan.dateWithinMediabinWWA(candedateWWADate); 
							}
						}
					} catch (Throwable e) {
						log.debug("Throwable", e);
						//System.out.println("Error: " + e);
					}
				}
			}
			log.debug(FormatUtils.mFormat("---File {0} is {1} in WWA", fullFileNamePath, retVal?"valid":"invalid"));
			//System.out.println("---File {0} is {1} in WWA");
		} else {
			/* Code amended 12-05-2010 - MJS
			 * Previous code in this else condition just logged and did nothing.
			 * This is where we end up with a deleted DCR though, so a deleted record in the
			 * source filelist will be ripped out and NOT returned in the iterator to OpenDeploy
			 * As a result deletions in TeamSite/ABF are not replicated to Public.
			 * 
			 * New code assumes all non-existant files are deletions and adds them to the iterator.
			 * As file is deleted in both WORKAREA and STAGING at this point it's the only option available.
			 */
			log.debug(FormatUtils.mFormat("File {0} does not exist", f.getAbsolutePath()));
			log.debug("Assume deleted file, return true so file is added to fileList");
			//System.out.println("File {0} does not exist " + f.getAbsolutePath());
			//System.out.println("Assume deleted file, return true so file is added to fileList");
			retVal = true;
		}
		// TODO Auto-generated method stub
		log.debug("-->Exiting private boolean checkFileForWWADate(String area, String relFileName)");
		//System.out.println("-->Exiting private boolean checkFileForWWADate(String area, String relFileName)");
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.interwoven.od.adapter.delivery.base.IWODDeliveryAdapter#deploy()
	 */
	public boolean deploy() {
		log.debug("Entering public boolean deploy()");
		log.debug("Exiting public boolean deploy()");
		return true;
	}

	public void createManifest(String arg0, String arg1) {
		log.debug("Entering public void createManifest(String arg0, String arg1)");
		log.debug(arg0);
		log.debug(arg1);
		super.createManifest(arg0, arg1);
		log.debug("Exiting public void createManifest(String arg0, String arg1)");
	}

	public boolean isAvailable() {
		return true;
	}

	//This could move to the Utils class
	private String fullPath(String area, String relFileName) {
		return FormatUtils.mFormat("{0}/{1}", area, relFileName);
	}

	private static final int BUFF_SIZE = 1024;

	public String doGet(String urlStr) throws IOException {
		StringBuffer content = new StringBuffer();
		System.setProperty("sun.net.client.defaultConnectTimeout", urlConnectionTimeout);
		log.debug(FormatUtils.mFormat("Setting {0} to {1}", "sun.net.client.defaultConnectTimeout", urlConnectionTimeout));
		System.setProperty("sun.net.client.defaultReadTimeout", urlReadTimeout);
		log.debug(FormatUtils.mFormat("Setting {0} to {1}", "sun.net.client.defaultReadTimeout", urlReadTimeout));

		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
		byte[] buffer = new byte [BUFF_SIZE];
		int bytes_read;
		while ((bytes_read = bis.read(buffer)) != -1) {
			content.append(new String(buffer, 0, bytes_read));
		}
		bis.close();
		String retVal = content.toString();
//			retVal = retVal.substring("java.lang.RuntimeException: Error while parsing XML: java.lang.StringIndexOutOfBoundsException: String index out of range: -1".length());

		retVal = retVal.trim();		
		//Quick Fix
		log.debug("Start of Content");
		log.debug(retVal);
		log.debug("End of Content");

		return retVal;
	}
	
	public InputStream doGetx(String urlStr) throws IOException {
		System.setProperty("sun.net.client.defaultConnectTimeout", urlConnectionTimeout);
		System.setProperty("sun.net.client.defaultReadTimeout", urlReadTimeout);

		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		
		return connection.getInputStream();
	}

	/**
	 * Inner bean class for asset mediabin lookup
	 * @author nbamford
	 *
	 */
	private class IIF {
		private String regex;
		private String trueCond;
		private String falseCond;
		
		public IIF(String arg0, String token) {
			StringTokenizer st = new StringTokenizer(arg0, token);
			setRegex(st.nextToken());
			setTrueCond(st.nextToken());
			setFalseCond(st.nextToken());
		}

		public void setRegex(String regex) {
			this.regex = regex;
		}

		public String getRegex() {
			return regex;
		}

		public void setTrueCond(String trueCond) {
			this.trueCond = trueCond;
		}

		public String getTrueCond() {
			return trueCond;
		}

		public void setFalseCond(String falseCond) {
			this.falseCond = falseCond;
		}

		public String getFalseCond() {
			return falseCond;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			
			sb.append("[regex=").append(regex).append(",");
			sb.append("trueCond=").append(trueCond).append(",");
			sb.append("falseCond=").append(falseCond).append("]");
			
			return sb.toString();
		}
	}
}