package com.interwoven.teamsite.nikon.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.teamsite.ext.util.FormatUtils;

/**
 * @author Arnout Cator, Rashid Siddiqui
 * URL External Task checking the WWA date on DCRs
 * a.	If TeamSite/Metadata/prod_wwa_date is null
 * b.   then build a List of files that do not have this set
 * c.   send the workflow to an email notification task
 */
public class WWADateChecker implements CSURLExternalTask {
	// Define a LOGGER for debugging purpose
	private static final Log log = LogFactory.getLog(WWADateChecker.class);
	private String url;
	private String usr;
	private String pwd;
	//private File dbPropsFile = new File("D:/Interwoven/TeamSite/httpd/webapps/content_center/WEB-INF/conf/livesite_customer/database.properties");
	private File dbPropsFile = new File("D:/Interwoven/ApplicationContainer/server/default/deploy/iw-cc.war/WEB-INF/conf/livesite_customer/database.properties");
	

	/**
	 * List of DCRs.
	 */
	private List<CSSimpleFile> assetList;

	/**
	 * Extended Attribute World Wide Announcement Date.
	 * TeamSite/Metadata/prod_wwa_date
	 */
	private CSExtendedAttribute prodWwaDate;

	/**
	 * Array of DCRs without prodWwaDate set.
	 */
	private List<CSSimpleFile> noWwaList;

	/**
	 * Workflow ID.
	 */
	private CSWorkflow workflow;

	/**
	 * ArrayList of Miscelaneous Files (like Site Publisher)
	 */
	private ArrayList<CSSimpleFile> miscList;

	/**
	 * URL External Task execute method.
	 */
	public void execute(CSClient client, CSExternalTask task, Hashtable params)
			throws CSException {
		
			
		//Load the dbProperties
		Properties dbProperties = new Properties();
		
		try {
			dbProperties.load(new FileInputStream(dbPropsFile));
		} 
		catch (FileNotFoundException e1) 
		{
			log.debug("FileNotFoundException", e1);
			throw new CSException(e1);
		} 
		catch (IOException e1) 
		{
			log.debug("IOException", e1);
			throw new CSException(e1);
		}
		url = (String)dbProperties.get("development.url");
		usr = (String)dbProperties.get("development.username");
		pwd = (String)dbProperties.get("development.password");

		log.debug(checkWWADate("Q320--","en_Asia"));
		
		CSAreaRelativePath[] files = task.getFiles();
		assetList = new ArrayList<CSSimpleFile>();
		miscList = new ArrayList<CSSimpleFile>();
		noWwaList = new ArrayList<CSSimpleFile>();
		String nextTransition = "";
		String wwadate = "";
		String format = "";

		//TODO make the digital assets populated through a DCT
		ArrayList<String> extList = new ArrayList<String>();
		extList.add("jpg");
		extList.add("jpeg");
		extList.add("gif");
		extList.add("swf");
		extList.add("png");
		
		//TODO make the SitePublisher assets populated through a DCT
		ArrayList<String> miscExtList = new ArrayList<String>();
		miscExtList.add("page");
		miscExtList.add("site");
		miscExtList.add("default");	

		String ext = null;

		//populate list of assets
		for (int i = 0; i < files.length; i++) {

			CSSimpleFile simpleFile = null;
			try {
				simpleFile = (CSSimpleFile) task.getArea().getFile(files[i]);
				log.debug("simpleFile: " + simpleFile.getName());
			} catch (Exception e) {
				// TODO: handle exception
				log.debug("Exception", e);
			}

			ext = files[i].getExtension();

			boolean isDCR = simpleFile.getKind() == CSSimpleFile.KIND
					&& simpleFile.getContentKind() == CSSimpleFile.kDCR;
			

			if (isDCR || extList.contains(ext)) {

				// adding DCRS to List
				assetList.add(simpleFile);
			}

		}

		//iterate through list of assets and check valid wwa date
		
		for (Iterator iterator = assetList.iterator(); iterator.hasNext();) {
			CSSimpleFile asset = (CSSimpleFile) iterator.next();

			boolean isDCR = asset.getKind() == CSSimpleFile.KIND
					&& asset.getContentKind() == CSSimpleFile.kDCR;

			if (isDCR) {
				// Get Extended Attribute from dcr
				prodWwaDate = asset
						.getExtendedAttribute("TeamSite/Metadata/prod_wwa_date");
				wwadate = prodWwaDate.getValue();
				format = "yyyy-MM-dd HH:mm:ss";
			}
			// Get Extended Attribute digital asset
			else {

				String ea = asset.getExtendedAttribute("MediaBin/Metadata")
						.getValue();
				// String ea2 =
				// asset.getExtendedAttribute("MediaBin/Server:").getValue();
				// String ea3 =
				// asset.getExtendedAttribute("TeamSite/Metadata/CreationDate:").getValue();

				// parse ea to get the WWA date value as string
				if (ea == null) {
					ea = "";
				}

				wwadate = getAssetWwaDate(ea);
				format = "yyyy-MM-dd'T'HH:mm:ss";

			}

			// add regex to make sure the wwa date is in the right
			// format
			if (!isValidDate(wwadate, format)) {

				// if prodWwaDate is null then create a list of
				// files that have this not set
				noWwaList.add(asset);
				
				//At this point if 
				if(isDCR)
				{
					
				}

			}

			wwadate = null;

		}
		
		//all W dates are all set and I have miscellaneous files in my workflow
		//or we have just DCRs or images that have no WWADate set
		if (noWwaList.isEmpty() ) {

			// transition to next stage in workflow
			
			nextTransition = task.getTransitions()[0];
			log.debug("transition of pages: " + nextTransition);
			task.chooseTransition(nextTransition, "All WWA Dates Set");
		
		}
		
		//not all wwa dates area set 
		else {
			// transform the list of null wwa dcrs into an array
			StringBuffer noWwaCSV = new StringBuffer();
			CSSimpleFile[] dcrArray = (CSSimpleFile[]) noWwaList
					.toArray(new CSSimpleFile[0]);
			for (int j = 0; j < dcrArray.length; j++) {

				// assign the array to a workflow variable, get rid
				// of the trailing ,
				if (noWwaCSV.length() == 0) {
					noWwaCSV.append(dcrArray[j].getVPath().toString());
				} else {
					noWwaCSV.append("," + dcrArray[j].getVPath().toString());
				}
			}
			workflow = task.getWorkflow();
			log.debug("Asset Array Variable" + noWwaCSV.toString());
			workflow.setVariable("Asset Array", noWwaCSV.toString());

			// add the array of null wwa dcrs to the task and
			// transition
			nextTransition = task.getTransitions()[1];
			log.debug("transition of dcrs/assets: " + nextTransition);
			task.chooseTransition(nextTransition, "Not All WWA Dates Set");

		}

	}

	/**
	 * Checks the World Wide Announcement date.
	 * Set default to false
	 * @param dateField
	 * @param dateFormat
	 * @return
	 */
	private boolean isValidDate(String dateField, String dateFormat) {
		boolean valid = false;
		String outFormat = "yyyy-MM-dd HH:mm:ss";
		String inFormat = "dd/MM/yyyy HH:mm:ss";
		Date tempDate = null;
		log.debug("$$$dateField:" + dateField + " value:" + dateField);
		if (dateField != null && !"".equals(dateField.trim())) {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			try {
				tempDate = sdf.parse(dateField);
				// formatting image date format on deployment to Production
				// sdf = new SimpleDateFormat(outFormat);
				// dateField = sdf.format(tempDate);

				valid = true;
			} catch (ParseException e) {
				dateField = "";
				log.error("ParseException", e);
			}

		}

		return valid;
	}

	/**
	 * Get Asset WWA Date for digital assets (non-DCRs).
	 * 
	 * @param xml
	 * @return
	 */
	private String getAssetWwaDate(String xml) {

		Document document = convertString2Document(xml);

		String wwaValue = null;

		// log.debug("after convertString2Document" +
		// document.getName());
		// if no xml metadata then wwa value will be returned as null
		// then isValidWwaDate method will add to the list of assets that
		// cannot be deployed
		if (document != null) {

			// get the list of all the 'link' elements in the doc of type
			// 'image' in the page section.
			List list = document.selectNodes("//metadata/attribute");
			Iterator iter = list.iterator();
			int i = 0;
			// process image links in page section
			while (iter.hasNext()) {
				Element imageElement = (Element) iter.next();
				List elementList = imageElement.elements();

				for (Iterator iterator = elementList.iterator(); iterator
						.hasNext();) {
					Element elem = (Element) iterator.next();
					if (elem.getData().toString().equalsIgnoreCase("wwa")) {
						Element value = (Element) iterator.next();
						// holds the WWA value
						wwaValue = value.getData().toString();

						outer: break;
					}

				}
			}
		}

		return wwaValue;
	}

	/**
	 * This method is used to convert a XML String to a Document object.
	 * 
	 * @param xml
	 *            - XML string which has to be converted to a Document object
	 * @return
	 */
	public Document convertString2Document(String xml) {
		Document document = null;
		try {
			document = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			log.debug(e.getMessage());
		}
		return document;
	}

	//Method to get the maximum wwa_date for a comma seperated list of prod_dev_codes and nikon_locale
	private Date checkWWADate(String prodDevCodes, String locale)
	throws CSException
	{
			Date retVal = null;
			Connection con;
			Timestamp ts = null;
			try {
				con = DriverManager.getConnection(url,usr,pwd);
				CallableStatement stmt = con.prepareCall("SELECT [dbo].WWA_DATE_CHECK(?, ?) as PROD_WWA_DATE");
				stmt.setString(1, prodDevCodes);
				stmt.setString(2, locale);
				
				ResultSet rs = stmt.executeQuery();
				
				//Get the value from the resultset, should be just the one
				while (rs.next()) {
					ts = rs.getTimestamp("PROD_WWA_DATE");
				}
				
				//Turn into a java.util.Date from Calendar
				Calendar c = new GregorianCalendar();
				c.setTimeInMillis(ts.getTime());
				con.close();
				retVal = c.getTime();
			} 
			catch (SQLException e) 
			{
				log.error("SQLException", e);
				throw new CSException(e);
			}

			return retVal;
	}
}