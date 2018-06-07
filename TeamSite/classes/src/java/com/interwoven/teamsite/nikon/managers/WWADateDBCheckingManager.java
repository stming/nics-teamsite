package com.interwoven.teamsite.nikon.managers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.dto.WWAControlGateDTO;
import com.interwoven.teamsite.nikon.springx.dao.MetaDataSpringDAO;
import com.interwoven.teamsite.nikon.springx.dao.WWAControlGateDTOSpringDAO;
import com.interwoven.teamsite.nikon.util.NikonUtils;
import com.interwoven.teamsite.nikon.util.WWADateUtils;

/**
 * Management class for the WWA Date security checking
 * Can hang different methods here
 * @author nbamford
 *
 */
public class WWADateDBCheckingManager {

	private static final String NIKON_OD_WWA_DB_METADATA_DAO = "nikon.od.wwa.db.metadataDAO";
	private static final String NIKON_OD_WWA_DB_WWA_CNT_GTE_DTO_DAO = "nikon.od.wwa.db.wwaControlGateDtoDAO";
	private static final String NIKON_OD_WWA_DB_DATASOURCE = "nikon.od.wwa.db.datasource";

	public static String PAR_DB_PROPERTIES_FILE = "PAR_DB_PROPERTIES_FILE";
	public static String PAR_DB_DRIVER_KEY      = "PAR_DB_DRIVER_KEY";  
	public static String PAR_DB_URL_KEY         = "PAR_DB_URL_KEY"; 
	public static String PAR_DB_USRNM_KEY       = "PAR_DB_USRNM_KEY"; 
	public static String PAR_DB_PWD_KEY         = "PAR_DB_PWD_KEY";
	public static String PAR_WS_URL_PAR         = "PAR_WS_URL_PAR"; 

	Properties properties;
	Properties dbProperties = new Properties();

	String driverClass;
	String url;
	String usr;
	String pwd;
	String wsUrl;


	static Log log = LogFactory.getLog(WWADateDBCheckingManager.class);

	public WWADateDBCheckingManager(Properties properties)
	{
		log.info("Creating Instance of com.interwoven.teamsite.nikon.managers.WWADateDBCheckingManager(Properties properties)");
		this.properties = properties;
		log.info(FormatUtils.mFormat("properties==null:{0}",properties==null));

		//Move the creation of the JDBC DataSource to here from below and Use Spring
		//Read in the property file
		try {
			dbProperties.load(new FileInputStream(properties.getProperty(PAR_DB_PROPERTIES_FILE)));
		} 
		catch (FileNotFoundException e1) 
		{
			log.fatal(e1);
			throw new RuntimeException(e1);
		} 
		catch (IOException e1) 
		{
			log.fatal(e1);
			throw new RuntimeException(e1);
		}


		//Here we get the keys to use for database.properties file eg. development.className or development.url

		driverClass = dbProperties.getProperty(properties.getProperty(PAR_DB_DRIVER_KEY));
		url = dbProperties.getProperty(properties.getProperty(PAR_DB_URL_KEY));
		usr = dbProperties.getProperty(properties.getProperty(PAR_DB_USRNM_KEY));
		pwd = dbProperties.getProperty(properties.getProperty(PAR_DB_PWD_KEY));
		log.debug(FormatUtils.mFormat("url:{0}", url));
		log.debug(FormatUtils.mFormat("usr:{0}", usr));
		log.debug(FormatUtils.mFormat("pwd:{0}", FormatUtils.repeatPattern(pwd.length() * 2, "*")));

		ctx = new GenericApplicationContext();

		//Constructor Arguments for our DataSource
		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addIndexedArgumentValue(0, driverClass, "java.lang.String");
		constructorArgumentValues.addIndexedArgumentValue(1, url, "java.lang.String");
		constructorArgumentValues.addIndexedArgumentValue(2, usr, "java.lang.String");
		constructorArgumentValues.addIndexedArgumentValue(3, pwd, "java.lang.String");

		//Define the Generic Bean with type DriverManagerDataSource.class
		GenericBeanDefinition dsBeanDefinition = new GenericBeanDefinition();
		dsBeanDefinition.setBeanClass(DriverManagerDataSource.class);
		dsBeanDefinition.setLazyInit(false);
		dsBeanDefinition.setAbstract(false);
		dsBeanDefinition.setAutowireCandidate(true);
		dsBeanDefinition.setDestroyMethodName("close");
		dsBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

		//Put it into the context
		ctx.registerBeanDefinition(NIKON_OD_WWA_DB_DATASOURCE, dsBeanDefinition);

		GenericBeanDefinition productDTOBeanDefinition = new GenericBeanDefinition();
		productDTOBeanDefinition.setBeanClass(MetaDataSpringDAO.class);
		productDTOBeanDefinition.setLazyInit(false);
		productDTOBeanDefinition.setAbstract(false);
		productDTOBeanDefinition.setAutowireCandidate(true);


		GenericBeanDefinition wwaGateControlDTOBeanDefinition = new GenericBeanDefinition();
		wwaGateControlDTOBeanDefinition.setBeanClass(WWAControlGateDTOSpringDAO.class);
		wwaGateControlDTOBeanDefinition.setLazyInit(false);
		wwaGateControlDTOBeanDefinition.setAbstract(false);
		wwaGateControlDTOBeanDefinition.setAutowireCandidate(true);

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue("dataSource", ctx.getBean(NIKON_OD_WWA_DB_DATASOURCE));

		productDTOBeanDefinition.setPropertyValues(mpv);
		wwaGateControlDTOBeanDefinition.setPropertyValues(mpv);

		ctx.registerBeanDefinition(NIKON_OD_WWA_DB_METADATA_DAO, productDTOBeanDefinition);
		ctx.registerBeanDefinition(NIKON_OD_WWA_DB_WWA_CNT_GTE_DTO_DAO, wwaGateControlDTOBeanDefinition);



	}

	/**
	 * Method to return the WWADate in the format yyyy-MM-dd HH:mm:ss as a String
	 * @param prodDevCodes - Comma separeated prod_dev_codes
	 * @param nikonLocale  - Nikon Locale we're interested in for any local products
	 * @return             - java.util.Date of the furthest WWA Date
	 * @throws Exception
	 */
	public String getWWADateFromProdDevCodeListString(String prodDevCodes, String nikonLocale)
	{
		return FormatUtils.formatWWADate(getWWADateFromProdDevCodeList(prodDevCodes, nikonLocale));
	}

	/**
	 * Method to return the WWADate for a given list of comma separated prod_dev_codes and a NikonLocale
	 * @param csvProdDevCodes - Comma separeated prod_dev_codes
	 * @param nikonLocale     - Nikon Locale we're interested in for any local products
	 * @return                - java.util.Date of the furthest WWA Date
	 * @throws Exception
	 */
	public Date getWWADateFromProdDevCodeList(String csvProdDevCodes, String nikonLocale)
	{
		log.debug("Entering public Date getWWADateFromProdDevCodeList(String prodDevCodes, String nikonLocale)");
		Date retVal = getMetadataSpringDAO().findMaxWWADateForCSVProdDevCodes(csvProdDevCodes, nikonLocale);
		log.debug(FormatUtils.mFormat("Returning date:{0}", retVal));
		log.debug("Exiting public Date getWWADateFromProdDevCodeList(String prodDevCodes, String nikonLocale)");
		return retVal;
	}

	/**
	 * Method to return if a list of ProdDevCodes are within WWA Date from Database ONLY.
	 * @param csvProdDevCodes
	 * @param nikonLocale
	 * @return
	 */
	public boolean prodDevCodesWithinWWADB(String csvProdDevCodes, String nikonLocale)
	{
		return dateWithinWWA(getWWADateFromProdDevCodeList(csvProdDevCodes, nikonLocale));
	}

	
	/**
	 * Little helper TO class for storing return value and status back from web service
	 * @author nbamford
	 *
	 */
	private class ProdDevCodesWithinWWAWSTo
	{
		public ProdDevCodesWithinWWAWSTo(){}
		
		private boolean deploy;
		private String status = "OK";

		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public void setDeploy(boolean deploy) {
			this.deploy = deploy;
		}
		public boolean isDeploy() {
			return deploy;
		}
		
	}
	/**
	 * Method to return if a list of ProdDevCodes are within WWA Date from Web Service ONLY.
	 * @param csvProdDevCodes
	 * @param nikonLocale
	 * @return
	 */
	public ProdDevCodesWithinWWAWSTo prodDevCodesWithinWWAWS(String csvProdDevCodes)
	{
		
		ProdDevCodesWithinWWAWSTo retTo = new ProdDevCodesWithinWWAWSTo();


		InputStream is = null;
		URL url = null;
		try 
		{
			csvProdDevCodes = NikonUtils.cleanCSVProdDevCode(csvProdDevCodes);
			log.debug(FormatUtils.mFormat("csvProdDevCodes:{0}", csvProdDevCodes));

			//Parameters
			String content = "folderName=" + URLEncoder.encode(csvProdDevCodes);
			log.debug(FormatUtils.mFormat("content:{0}", content));

			//			url = new URL(properties.getProperty(PAR_WS_URL_PAR));
			url = new URL(FormatUtils.mFormat("{0}?{1}", properties.getProperty(PAR_WS_URL_PAR), content));
			log.debug(FormatUtils.mFormat("url:{0}", url));

			//Get the connection and set it to POST
			URLConnection urlConnection = url.openConnection();
			urlConnection.setDoInput (true);
			urlConnection.setDoOutput (true);
			urlConnection.setUseCaches (false);

			DataOutputStream printout = new DataOutputStream (urlConnection.getOutputStream ());

			//POST
			printout.writeBytes (content);
			printout.flush ();
			printout.close ();

			//Get the result
			log.debug(FormatUtils.mFormat("url(host.path.query):{0}{1}?{2}", url.getHost(), url.getPath(), url.getQuery()));
			is = urlConnection.getInputStream();
			DataInputStream dis = new DataInputStream(new BufferedInputStream(is));

			//Read into String
			String s = "";
			while ((s = dis.readLine()) != null) {
				if(!"".equals(s))
				{
					//Once we have a value then set the return value and bomb out
					retTo.setDeploy((new Boolean(s.trim())).booleanValue());
					break;
				}
			}
			log.debug(FormatUtils.mFormat("URL Response [{0}]", s));
			log.debug(FormatUtils.mFormat("as boolean retVal[{0}]", retTo));
		} 
		catch (MalformedURLException e) 
		{
			log.debug("MalformedURLException", e);
			retTo.setStatus(e.getMessage());
		} catch (IOException e) {
			log.debug("IOException", e);
			retTo.setStatus(e.getMessage());
		}
		catch(Exception e)
		{
			log.debug("Exception", e);
			retTo.setStatus(e.getMessage());
		}
		finally 
		{
			try {
				//Close the InputStream if we can and not null
				if(is != null)
				{
					is.close();
				}
			} catch (Exception ioe) {
				log.debug("Exception", ioe);
				// just going to ignore this one
			}
			return retTo;
		}				
	}

	/**
	 * Method to return if a list of ProdDevCodes are withing WWA Date, from the Web Service AND Database.
	 * @param csvProdDevCodes
	 * @param nikonLocale
	 * @return
	 */
	public boolean prodDevCodesWithinWWADBAndWS(String csvProdDevCodes, String nikonLocale)
	{
		boolean retVal = false;

		Map<String, ProductDTO> mapOfTos = getMetadataSpringDAO().mapOfProductDevCodesAsProductDTO(csvProdDevCodes);

		List<String> dbProdDevCodes = new LinkedList<String>();
		List<String> wsProdDevCodes = new LinkedList<String>();

		//Loop through the prod dev codes. The ones we find which are migrated or local then push through the db check
		//Otherwise send throught the web service
		for(String prodDevCode: NikonUtils.csvProdDevCode2StringCollection(csvProdDevCodes))
		{
			List<String> list2use = wsProdDevCodes;

			//We found it in our database
			if(mapOfTos.containsKey(prodDevCode))
			{
				ProductDTO to = mapOfTos.get(prodDevCode);
				//But it needs to be a migratd or local to be checked solely in the DB
				if((to.isMigrated()) || (to.isLocalProduct()))
				{
					list2use = dbProdDevCodes;
				}
			}
			//Put in the List<String> to use, either db or ws
			list2use.add(prodDevCode);
		}
		//At this point we should have a List<String> of product dev codes to check in the db and ws
		//We set these true because it might be that there's nothing to check in the List<String>
		boolean db = true;
		if(dbProdDevCodes.size() > 0)
		{
			db = prodDevCodesWithinWWADB(FormatUtils.collection2String(dbProdDevCodes), nikonLocale);
		}

		boolean ws = true;
		if(wsProdDevCodes.size() > 0)
		{
			ws = prodDevCodesWithinWWAWS(FormatUtils.collection2String(wsProdDevCodes)).isDeploy();
		}

		log.debug(FormatUtils.mFormat("Results from Database(db) and Web Service(ws) check are - db:{0} ws:{1}", db, ws));
		//The return value should be an AND operation of the two
		retVal = (db && ws);


		return retVal;
	}

	public boolean dateWithinWWA(Date candedateWWADate)
	{
		log.debug("Entering public boolean dateWithinWWA(Date candedateWWADate)");
		boolean retVal = false;
		if(candedateWWADate == null)
		{
			throw new RuntimeException("WWA Date being checked was null");
		}
		long cendedateWWADateLong = candedateWWADate.getTime();

		retVal = cendedateWWADateLong <= getCurrentWWATime();	

		log.debug("Exiting public boolean dateWithinWWA(Date candedateWWADate)");
		return retVal;
	}

	public boolean dateWithinMediabinWWA(String candedateMediabinWWADateString)
	{
		boolean retVal = false;
		if(!"".equals(candedateMediabinWWADateString))
		{
			String yearPart = candedateMediabinWWADateString.substring(0, 10);
			String timePart = candedateMediabinWWADateString.substring(11, 18);

			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			try 
			{
				retVal = dateWithinWWA(sf.parse(FormatUtils.mFormat("{0} {1}", yearPart, timePart)));
			} 
			catch (ParseException e) {
				log.warn(e);
			}
		}
		return retVal;
	}	

	/**
	 * Method for creating the WWA_GATE_CONTROL Table records to test against
	 * when doing a deployment
	 * 
	 */
	public void createWWAGateRecords()
	{
		log.debug("Entering public void createWWAGateRecords()");

		
		//First flag all old records as old
		getWWAControlGateDTOSpringDAO().flagWWAControlGateRecordsSpent();
		
		Date wwaDateTime = getCurrentWWATimeDate();

		List<ProductDTO> wwaProductList = getMetadataSpringDAO().listAllProductAccessoriesInViewMetadataBeforeWWA();

		for(ProductDTO prodDto : wwaProductList)
		{		
			//We will loop through in this part
			//Create the values we can via the constructor
			WWAControlGateDTO wwaCtlGtDto = new WWAControlGateDTO(prodDto);
			
			//here are the ones we can't
			wwaCtlGtDto.setDeploymentDate(wwaDateTime);
			
			//If we are local or migrated then always deploy
			if((wwaCtlGtDto.isLocalProduct()) || (wwaCtlGtDto.isMigrated()))
			{
				wwaCtlGtDto.setDeploy(true);
			}
			//Otherwise we need to check the WS to determine
			else
			{
				//Here let's go to the web service and check for the value against the prod_dev_code
				ProdDevCodesWithinWWAWSTo to = prodDevCodesWithinWWAWS(wwaCtlGtDto.getProdDevCode());
				wwaCtlGtDto.setDeploy(to.isDeploy());
				wwaCtlGtDto.setStatus(to.getStatus());
			}
			getWWAControlGateDTOSpringDAO().insertWWAControlGateDTO(wwaCtlGtDto);
		}

		log.debug("Exiting public void createWWAGateRecords()");
	}

	public boolean dateWithinWWA(String candedateWWADateString)
	{
		log.debug("Entering public boolean dateWithingWWA(String candedateWWADateString)");
		Date retWWADate;
		try 
		{
			retWWADate = FormatUtils.parseWWADate(candedateWWADateString);
		} 
		catch (ParseException e) 
		{
			log.fatal("ParseException", e);
			throw new RuntimeException(e);
		}
		log.debug("Exiting public boolean dateWithingWWA(String candedateWWADateString)");
		return dateWithinWWA(retWWADate);
	}

	private long currWWATime = -1;
	private GenericApplicationContext ctx;

	//Way of getting the current time. Eventually to be replaced with a web service

	/**
	 * For a given instance of this class will store the current WWA Date
	 * and use. Each instance will create its own.
	 * @return
	 */
	public long getCurrentWWATime()
	{
		if(currWWATime == -1)
		{
			currWWATime = WWADateUtils.getCurrentWWATime();
		}
		return currWWATime;
	}

	/** 
	 * Convenience method to return the formatted WWADate. Format is yyyy-MM-dd HH:mm:ss
	 * @return Current WWA Date/Time in format yyyy-MM-dd HH:mm:ss
	 */
	public String getCurrentWWATimeString()
	{
		Date currentWWADateTimeDate = new Date(getCurrentWWATime());
		return FormatUtils.formatWWADate(currentWWADateTimeDate);
	}

	/** 
	 * Convenience method to return the the Current WWA TimeDate as a Date
	 * @return Current WWA Date/Time in Date object
	 */
	public Date getCurrentWWATimeDate()
	{
		return new Date(getCurrentWWATime());
	}

	//Accessor methods to get the DAOs out of our local programatically created 
	//Spring context
	private MetaDataSpringDAO getMetadataSpringDAO()
	{
		return (MetaDataSpringDAO)ctx.getBean(NIKON_OD_WWA_DB_METADATA_DAO, MetaDataSpringDAO.class);
	}

	private WWAControlGateDTOSpringDAO getWWAControlGateDTOSpringDAO()
	{
		return (WWAControlGateDTOSpringDAO)ctx.getBean(NIKON_OD_WWA_DB_WWA_CNT_GTE_DTO_DAO, WWAControlGateDTOSpringDAO.class);
	}
}
