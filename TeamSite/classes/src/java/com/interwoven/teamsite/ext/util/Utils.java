package com.interwoven.teamsite.ext.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassLoaderUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.livesite.common.xml.XmlEmittable;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.CookieHash;
import com.interwoven.livesite.file.FileDALIfc;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.to.TeamsiteTo;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;


/**
 * General Livesite Utils Class
 * @author nbamford
 *
 */
public class Utils 
{
	protected static File retFile = null;
	protected static Character cacheLock = new Character('A');

	//This Class is hit a lot, so if you want to try and filter out 
	//various method logging, I suggest creating a specific method logger
	//and turning the base logging up high
	private static Log log = LogFactory.getLog(Utils.class);
	private static Log urlEncodeLog = LogFactory.getLog(FormatUtils.mFormat("{0}.URLEncode", Utils.class.getName()));
	private static Log urlDecodeLog = LogFactory.getLog(FormatUtils.mFormat("{0}.URLDecode", Utils.class.getName()));


	/**
	 * Helper method to create a Standard org.dom4j.Document, error document given a String message
	 * @param messageString - Text to go into Message text
	 * @return org.dom4j.Document
	 */
	public static Properties loadProperties(String propsName)
	{
		log.debug("Entering public static Properties loadProperties(String propsName)");
		Properties retObj = new Properties();

		try 
		{

			java.io.InputStream is = Utils.class.getResourceAsStream("/" + propsName);
			log.debug(FormatUtils.mFormat("InputStream {0}", (is == null)?"is null":"is not null"));

			retObj.load(is);
		} 
		catch (Exception e) {
			log.warn(FormatUtils.mFormat("Can not find properties file {0} on ClassLoader classpath:{1}", propsName, ClassLoaderUtils.showClassLoaderHierarchy(ClassLoader.getSystemClassLoader())));
			log.error("Exception", e);
		}

		log.debug("Exiting public static Properties loadProperties(String propsName)");
		return retObj;
	}

	/**
	 * Helper method to create a Standard org.dom4j.Document, error document given a throwable
	 * @param throwable - Throwable to retrieve the message from
	 * @return org.dom4j.Document XML in form <Error><Message>ERROR MESSAGE</Message></Error>
	 */
	public static org.dom4j.Document dom4JErrorDocument(Throwable throwable)
	{
		return dom4JErrorDocument(throwable.getMessage());
	}

	public static org.dom4j.Document dom4JDocumentFromMap(Map<String, Object> mapOfData)
	{
		DocumentFactory fact = DocumentFactory.getInstance();
		org.dom4j.Document retDoc = fact.createDocument();
		Element root = fact.createElement("Dom4JDocumentFromMap");
		retDoc.setRootElement(root);

		for(String paramName : mapOfData.keySet())
		{
			Element dataElement = fact.createElement("Data");
			Element keyElement = fact.createElement("Key");
			Element valElement = fact.createElement("Value");

			keyElement.setText(paramName);

			Object mapVal = mapOfData.get(paramName);

			//Implement the class types catered for here
			if(String.class.isAssignableFrom(mapVal.getClass()))
			{
				valElement.setText((String)mapVal);
			}
			else if(XmlEmittable.class.isAssignableFrom(mapVal.getClass()))
			{
				valElement.add(((XmlEmittable)mapVal).toElement());
			}
			else
			{
				valElement.addAttribute("Warning", FormatUtils.mFormat("Class type {0} not implemented yet", mapVal.getClass().getName()));
				valElement.setText(mapVal.toString());
			}

			dataElement.add(keyElement);
			dataElement.add(valElement);
			root.add(dataElement);
		}

		return retDoc;
	}

	/**
	 * Helper method to create a Standard org.dom4j.Document, error document given a String message
	 * @param messageString - Text to go into Message text
	 * @return org.dom4j.Document XML in form <Error><Message>ERROR MESSAGE</Message></Error>
	 */

	public static org.dom4j.Document dom4JErrorDocument(String messageString)
	{
		DocumentFactory fact = DocumentFactory.getInstance();
		org.dom4j.Document retDoc = fact.createDocument();
		Element root = fact.createElement("Error");
		retDoc.setRootElement(root);
		Element message = fact.createElement("Message");
		message.setText(messageString);
		root.add(message);
		return retDoc;
	}

	/**
	 * Converts an XML File f into a org.w3c.dom.Document
	 * @param f File of XML document
	 * @return instance of org.w3c.dom.Document
	 * @throws Exception
	 */
	public static Document getXmlDoc(File f) 
	throws Exception
	{

		FileInputStream in = new FileInputStream(f);

		// Build the document with DOM and Xerces, no validation
		// Create a buffered reader for the parser
		DOMParser parser = new DOMParser();
		Document doc = null;

		try {

			// Parse the whole file
			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

			// Don't validate the DTD.
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			//Setting this feature allows non standard XML encodings, like Windows-1252. Go figure
			parser.setFeature("http://apache.org/xml/features/allow-java-encodings", true);

			// Parse It
			parser.parse(new InputSource(in));

			// Got a DOM tree?
			doc = parser.getDocument();

		}
		catch (Exception e) {
			log.error("Error creating doc: ", e);
			throw e;
		}
		finally {
			in.close();
		}

		return doc;
	}

	private static SimpleCache cache;

	/**
	 * Searches for  file
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	public static File searchForFile(String fileName)
	throws FileNotFoundException
	{
		return searchForFile(fileName, false);
	}    

	/**
	 * Method to search for a file along all of the classpath locations first then the
	 * user paths. Searches recursivley.
	 * @param fileName Name of file to look for.
	 * @return File if found
	 * @throws FileNotFoundException
	 */
	public static File searchForFile(String fileName, boolean useCache)
	throws FileNotFoundException
	{
		File retVal = null;

		if(useCache)
		{
			synchronized (cacheLock) {
				if(cache == null)
				{
					cache = new SimpleCache(100);
				}
			}
			retVal = (File)cache.getFromCache(fileName);

		}

		//Not in cache or not using cache
		if(retVal == null)
		{
			String classPath = System.getProperty("java.class.path");

			String[] pathElements = classPath.split(System.getProperty("path.separator"));

			try
			{
				//Loop through the files, if file then check otherwise look at parent
				for (int i = 0; i < pathElements.length && retVal == null; i++) {
					File file = new File(pathElements[i]);

					retVal = searchForFile(file, fileName); 
				}
			}
			catch(FileNotFoundException fileNotFoundException)
			{
				File userDir = new File(System.getProperty("user.dir"));
				retVal = searchForFile(userDir, fileName);
			}

			//If we don't have it by here then throw an exception
			if(retVal == null)
			{
				throw new FileNotFoundException(FormatUtils.mFormat("Could not find file {0} under any resources.", fileName));
			}

			//If using the cache and we found the file, then stick it in the cache
			if(useCache)
			{
				cache.addToCache(fileName, retVal);
			}
		}
		return retVal;
	}

	/**
	 * Method to search recursivley from a path root for a given filename 
	 * @param rootDir path to start searching from
	 * @param fileName filename to search for
	 * @return File if found
	 * @throws FileNotFoundException
	 */
	public static File searchForFile(File rootDir, String fileName)
	throws FileNotFoundException
	{
		retFile = null;
		retFile = _searchForFile(rootDir, fileName);
		if(retFile == null)
		{
			throw new FileNotFoundException(FormatUtils.mFormat("Could not find file {0} along classpath or user path.", fileName));
		}

		return retFile;
	}

	//Recursive method
	private static File _searchForFile(File rootDir, String fileName)
	{
		if(rootDir.isFile())
		{
			rootDir = rootDir.getParentFile();
		}

		File[] fileListArr = rootDir.listFiles();
		//Loop through all the files in the root one passed in
		for (int i = 0; i < fileListArr.length && retFile == null; i++) {
			if(fileListArr[i].isDirectory())
			{
				//Recurse
				_searchForFile(fileListArr[i], fileName);
			}
			else
			{
				if(fileListArr[i].getAbsolutePath().endsWith(fileName))
				{
					retFile = fileListArr[i];
				}
			}
		}
		return retFile;
	}


	/**
	 * This method will return a org.dom4j.Document from a org.w3c.dom.Document
	 * @param org.w3c.dom.Document
	 * @return org.dom4j.Document
	 * @throws Exception
	 */
	public static org.dom4j.Document getDom4JDocFromW3CDoc(Document document)
	throws Exception
	{
		DOMReader a = new DOMReader();
		return a.read(document);
	}

	/**
	 * This method will copy a file to a given path fileName location
	 * @param original	 - Existing file
	 * @param path       - Path to copy to. Null if same path
	 * @param fileName   - Name of new file
	 * @return           - File of new file
	 * @throws Exception 
	 */
	public static File copyFile(File original, String path, String fileName)
	throws Exception
	{
		if(!original.exists())
		{
			throw new FileNotFoundException(FormatUtils.mFormat("Could not find file {0} to copy.", original.getAbsolutePath()));
		}

		if(!original.isFile())
		{
			throw new Exception(FormatUtils.mFormat("Utility can only copy individual files, not directories", ""));
		}

		//If the path is null then set to file to be copied's path
		path = path != null?path:original.getParent();
		File retFile = new File(FormatUtils.mFormat("{0}{1}{2}", path, File.separator, fileName));

		FileInputStream fis= new FileInputStream(original);
		BufferedInputStream bis = new BufferedInputStream(fis);
		FileOutputStream fos = new FileOutputStream(retFile);

		(new FileUtils()).IOCopy(fis, fos);

		fos.close();
		bis.close();
		fis.close();


		fos = null;
		bis = null;
		fis = null;

		return retFile;

	}


	/**
	 * Collection to object Array
	 * @param col
	 * @param clazz
	 * @return Object representing the array return type, i.e. String[].class
	 */
	public static Object collectionToArray(Collection col, Class clazz)
	{
		Object[] unTypedArr = null;

		if(col != null && col.size() > 0)
		{
			if(clazz.isAssignableFrom(String[].class))
			{
				unTypedArr = new String[col.size()];
			}
			else{
				throw new RuntimeException(FormatUtils.mFormat("Class {0} currently not supported. Please implement or use other.", clazz.getName()));
			}
			int count = 0;

			for (Iterator<?> iter = col.iterator(); iter.hasNext();) {
				unTypedArr[count++] = iter.next();
			}
		}
		return unTypedArr;
	}

	/**
	 * Method to return a Collection<String> from a String Arr
	 * @param stringArr
	 * @return
	 */
	public static Collection<String> stringArr2StringCollection(String[] stringArr)
	{
		return Arrays.asList(stringArr);
	}

	public static StringBuffer mapToStringBuffer(Map map)
	{
		StringBuffer sb = new StringBuffer();
		Set keySet = map.keySet();
		for (Iterator i = keySet.iterator(); i.hasNext();) {
			Long key = (Long) i.next();
			sb.append(MessageFormat.format("{0}-{1}", new Object[]{key, map.get(key)}));
			sb.append("\n");
		}
		return sb;
	}

	/**
	 * Method to write a message to a file. Use in quick debug wheren Log4j not available easily
	 * like in Workflows
	 * @param fileName
	 * @param message
	 */
	public static void writeFile(String fileName, String message)
	{
		writeFile(new File(fileName), message);
	}

	/**
	 * Method to write a message to a file. Use in quick debug wheren Log4j not available easily
	 * like in Workflows
	 * @param f
	 * @param message
	 */
	public static void writeFile(File f, String message)
	{
		try
		{
			FileWriter fw = new FileWriter(f);
			fw.write(message);
			fw.flush();
			fw.close();
		}
		catch(Exception exception)
		{

		}
	}

	/**
	 * Convert a Set to a List
	 * @param dcrs
	 * @return
	 */
	public static List<?> setToList(Set<?> dcrs) {
		return new LinkedList(dcrs);
	}

	/**
	 * Convert a String[] to a List<String>
	 * @param values
	 * @return
	 */
	public static List<String> stringArrayToList(String[] values) {
		LinkedList<String> retList = new LinkedList<String>();
		for(String s: values)
		{
			retList.add(s);
		}
		return retList;
	}

	/**
	 * Convert a String[] to a collection. This should superseded the above
	 * @param stringArr
	 * @return
	 */
	public static Collection<String> stringArrayToCollection(String[] stringArr)
	{
		List<String> retList = new LinkedList<String>();
		for(String s: stringArr)
		{
			retList.add(s);
		}
		return retList;
	}

	/**
	 * Return a Collection<String> of field values from a List<?> of beans
	 * 
	 * @param objects
	 * @param fieldName
	 * @return 
	 */
	public static Collection<String> listOfBeansFieldToListOfStrings(List<?> objects, String fieldName)
	{
		Collection<String> retList = new LinkedList<String>();
		for(Object o: objects)
		{
			try {
				retList.add(BeanUtils.getProperty(o, fieldName));
			} catch (IllegalAccessException e) {
				log.error(FormatUtils.mFormat("Unable to access field {0} in bean", fieldName), e);
			} catch (InvocationTargetException e) {
				log.error(FormatUtils.mFormat("Unable to access field {0} in bean", fieldName), e);
			} catch (NoSuchMethodException e) {
				log.error(FormatUtils.mFormat("Unable to access field {0} in bean", fieldName), e);
			}
		}
		//TODO this is a quick dirty fix
		if(retList.size() == 0)
		{
			retList.add("-1");
		}
		return retList;
	}

	/**
	 * Method to return the JVM Class Path as a String
	 */
	public static String classPathToString()
	{
		StringBuffer sb = new StringBuffer();
		ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

		//Get the URLs
		URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();

		for(int i=0; i< urls.length; i++)
		{
			sb.append(urls[i].getFile());
		}
		return sb.toString();
	}

	/**
	 * Helper method to return the contents of a DCR as {@link Document}
	 * @param requestContext
	 * @param path
	 * @return
	 */
	public static org.dom4j.Document dcrToXML(RequestContext requestContext, String path)
	{
		return dcrToXML(requestContext, path, null);
	}

	/**
	 * Helper method to turn a DCR into a DOM4J Document
	 * @param requestContext
	 * @param path
	 * @param branchCountryCode
	 * @return
	 */
	public static org.dom4j.Document fileToXML(File file)
	{
		org.dom4j.Document retDoc = null;
		try
		{
			if((file != null) && (file.exists()) && (file.isFile()))
			{
				FileInputStream fis = new FileInputStream(file);
				//add is stream to xml
				retDoc = Dom4jUtils.newDocument(fis);
			}
			else
			{
				retDoc = org.dom4j.DocumentFactory.getInstance().createDocument();
				retDoc.setRootElement(org.dom4j.DocumentFactory.getInstance().createElement("error"));
				retDoc.getRootElement().setText(FormatUtils.mFormat("Resource {0} not found.", file.getAbsoluteFile()));
			}
		} catch (Exception e) {
			log.error("Exception", e);

			retDoc = org.dom4j.DocumentFactory.getInstance().createDocument();
			retDoc.setRootElement(org.dom4j.DocumentFactory.getInstance().createElement("error"));
			retDoc.getRootElement().setText(e.getMessage());
		}        
		finally
		{
			return retDoc;
		}

	}

	/**
	 * Method to return an XML Document from a String representation
	 * @param xmlAsString
	 * @return
	 */
	public static org.dom4j.Document string2XML(String xmlAsString)
	{
		return Dom4jUtils.newDocument(xmlAsString);
	}

	public static org.dom4j.Document string2XML(InputStream xmlAsInputStream)
	{
		return Dom4jUtils.newDocument(xmlAsInputStream);
	}

	public static boolean string2boolean(String string)
	{
		return Boolean.parseBoolean(string);
	}


	public static org.dom4j.Document dcrToXML(RequestContext requestContext, String path, String branchCountryCode)
	{
		log.debug("Entering public static org.dom4j.Document dcrToXML(RequestContext requestContext, String path, String branchCountryCode)");
		org.dom4j.Document retDoc = null;
		String fileNameAndLoc = null;
		try {
			FileDALIfc fileDal = requestContext.getFileDAL();
			fileNameAndLoc = changeCountryCodeBranch(fileDal.getRoot(), branchCountryCode) + "/" + path;

			log.debug(FormatUtils.mFormat("fileNameAndLoc:{0}", fileNameAndLoc));

			File checkFile = new File(fileNameAndLoc);


			//Only load it if it exists
			if((checkFile.exists()) && (checkFile.isFile()))
			{
				log.debug(FormatUtils.mFormat("file exists:{0}", fileNameAndLoc));

				//				log.debug(FormatUtils.mFormat("Looking for DCR {0}", (fileDal.getRoot() + path)));
				java.io.InputStream is = fileDal.getStream(fileNameAndLoc);

				//declare doc for return of headings

				//add is stream to xml
				retDoc = Dom4jUtils.newDocument(is);

				log.debug(FormatUtils.prettyPrint(retDoc));
			}
			else
			{
				retDoc = org.dom4j.DocumentFactory.getInstance().createDocument();
				retDoc.setRootElement(org.dom4j.DocumentFactory.getInstance().createElement("error"));
				retDoc.getRootElement().setText(FormatUtils.mFormat("Resource {0} not found.", fileNameAndLoc));
			}
		} catch (Exception e) {
			log.error("Exception", e);

			retDoc = org.dom4j.DocumentFactory.getInstance().createDocument();
			retDoc.setRootElement(org.dom4j.DocumentFactory.getInstance().createElement("error"));
			retDoc.getRootElement().setText(e.getMessage());
		}        
		finally
		{
			log.debug("Exiting public static org.dom4j.Document dcrToXML(RequestContext requestContext, String path, String branchCountryCode)");
			return retDoc;
		}
	}

	/**
	 * Helper method to encode the url on a Request context
	 * @param context
	 * @return
	 */
	public static String getCurrentUrl(RequestContext context)
	{
		StringBuffer sb = new StringBuffer();
		if(context != null)
		{
			sb = context.getRequest().getRequestURL();
			if (context.getRequest().getQueryString() != null) {
				sb.append("?");
				sb.append(context.getRequest().getQueryString());
			}
			//Commented out because not needed
			//			String currentUrl = currentUrl = URLEncoder.encode(sb.toString());
		}
		else
		{
			sb.append("* NOT KNOWN *");
		}

		return sb.toString();
	}


	private static String changeCountryCodeBranch(String root, String branchCountryCode) 
	{
		log.debug("Entering private static String changeCountryCodeBranch(String root, String branchCountryCode)");
		String retString = root;
		if(branchCountryCode != null)
		{	
			//Had to do this to cater for the UAT as well as Live environmnet Branching Structure
			log.debug("branchCountryCode:" + branchCountryCode);
			log.debug("root B4 :" + root);

			//retString = root.replaceFirst("(.*)/([A-Z][A-Z])/(.*)", FormatUtils.mFormat("$1/{0}/$3",branchCountryCode));
			retString = root.replaceAll("UAT/(..)/WORKAREA", "UAT/" + branchCountryCode + "/WORKAREA");
			retString = retString.replaceAll("Nikon/(..)/WORKAREA", "Nikon/" + branchCountryCode + "/WORKAREA");
			retString = retString.replaceAll("Training/(..)/WORKAREA", "Training/" + branchCountryCode + "/WORKAREA");

			log.debug("root After :" + retString);

			retString = root.replaceFirst("(.*)/([A-Z][A-Z]|Asia)/(.*)", FormatUtils.mFormat("$1/{0}/$3",branchCountryCode));
			log.debug(FormatUtils.mFormat("retString:{0}", retString));
		}
		log.debug("Exiting private static String changeCountryCodeBranch(String root, String branchCountryCode)");
		return retString;
	}	

	public static String getFormattedDate(String dateFormat)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		Date d = Calendar.getInstance().getTime();

		return(sdf.format(d));

	}

	private static org.dom4j.Document singletonDoc;
	public static synchronized org.dom4j.Document getSingletonDoc()
	{
		if(singletonDoc == null)
		{
			DocumentFactory docFac = DocumentFactory.getInstance();
			singletonDoc = docFac.createDocument();
			singletonDoc.setRootElement(docFac.createElement("staticdata"));
		}
		return singletonDoc;
	}
	public static String simpleAttNameFromTSExtendedAttName(String tsExtAttName)
	{
		return tsExtAttName.substring(tsExtAttName.lastIndexOf("/") + 1);
	}

	public static String deployedDCRAttNameBuilder(String attName)
	{
		return FormatUtils.mFormat("extAtt{0}", attName);
	}

	public static List set2List(Set set)
	{
		return new LinkedList(set);
	}

	public static Set list2Set(List list)
	{
		return new HashSet(list);
	}

	public static List<String> tokenedString2List(String tokenedString, String token)
	{
		List<String> retList = new LinkedList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(tokenedString, token);
		while(stringTokenizer.hasMoreTokens())
		{
			String to = stringTokenizer.nextToken();
			retList.add(to.trim());
		}
		return retList;
	}

	public static void listSpringApplicationContext(UtilsParamTo paramTo)
	{
		Log _log = paramTo.getLog()==null?log:paramTo.getLog();

		_log.debug("Entering public static void listSpringApplicationContext(ListSpringAppCtxParamTo paramTo)");

		_log.debug("Retrieving ApplicationContext");

		ApplicationContext ctx = ApplicationContextUtils.getApplicationContext();

		for(String beanName:ctx.getBeanDefinitionNames())
		{
			Object beanObject = ctx.getBean(beanName);
			_log.debug(FormatUtils.mFormat("{0}:{1}", beanName, beanObject.getClass().getName()));
		}
		_log.debug("Exiting public static void listSpringApplicationContext(ListSpringAppCtxParamTo paramTo)");
	}


	public class UtilsParamTo
	implements TeamsiteTo
	{
		public UtilsParamTo(){}

		//		public static UtilsParamTo getInstance()
		//		{
		//			return new UtilsParamTo();
		//		}

		private Log log;

		public void setLog(Log log) {
			this.log = log;
		}

		public Log getLog() {
			return log;
		}
	}

	/**
	 * Class method to return the value of a cookie with a given name if it exists. As we're getting this from the
	 * RequestContext CookieHash we don't need the path parameter, it's redundant
	 * 
	 * @param requestContext
	 * @param cookieName
	 * @param path
	 * @return
	 */
	public static String getCookieValue(RequestContext requestContext, String cookieName, String path)
	{
		log.debug("Entering public static String getCookieValue(RequestContext requestContext, String cookieName, String path)");
		String retVal = null;
		if(requestContext != null)
		{
			if(requestContext.getCookies() != null)
			{
				//This is a poor implementation to have to catch a Null Pointer if
				//we ask for a value which doesnt exist in the Cookie Hash but 
				//needed so we'll put it in here
				try
				{
					retVal = requestContext.getCookies().getValue(cookieName);
				}
				catch(NullPointerException e)
				{
					log.debug("Null returned trying to get value of cookie " + cookieName, e);
				}
			}
		}
		//		String retVal = requestContext != null?requestContext.getCookies() != null?requestContext.getCookies().getValue(NikonDomainConstants.CKIE_LANG_CODE):null:null;
		log.debug(FormatUtils.mFormat("retVal:{0}", retVal));
		log.debug("Exiting public static String getCookieValue(RequestContext requestContext, String cookieName, String path)");
		return retVal;
	}

	/**
	 * Helper method to return a cookie with a given name and path. If not there
	 * 
	 * PROVEN TO BE UN RELIABLE SO I've DEPRECATED IT
	 *  
	 * method returns null
	 * @param requestContext TS/LS RequestContext
	 * @param cookieName     Name of the Cookie to look for
	 * @return Either the cookie or null if the cookie doesn't exist
	 * @deprecated
	 */
	public static Cookie getCookie(RequestContext requestContext, String cookieName, String path) {
		log.debug("Entering public static Cookie getCookie(RequestContext requestContext, String cookieName, String path)");
		Cookie retVal = null;
		boolean crap = true;
		if(!crap) {
			return requestContext.getCookies() != null?requestContext.getCookies().getCookie(NikonDomainConstants.CKIE_LANG_CODE):null;
		} else {
			if((requestContext != null) && (requestContext.getRequest() != null) && (requestContext.getCookies() != null)) {
				CookieHash cookieHash = requestContext.getCookies();
				log.debug(FormatUtils.mFormat("Start for(Object cookieObj: cookieHash.getElements())"));
				for(Object cookieObj: cookieHash.getElements()) {
					try {
						Cookie cookie = (Cookie)cookieObj;
						log.debug(FormatUtils.mFormat("cookie.getPath()   :{0}", cookie.getPath()));
						log.debug(FormatUtils.mFormat("cookie.getName()   :{0}", cookie.getName()));
						log.debug(FormatUtils.mFormat("cookie.getValue()  :{0}", cookie.getValue()));
						log.debug(FormatUtils.mFormat("cookie.getDomain() :{0}", cookie.getDomain()));
						log.debug(FormatUtils.mFormat("cookie.getMaxAge() :{0}", cookie.getMaxAge()));
						log.debug(FormatUtils.mFormat("cookie.getVersion():{0}", cookie.getVersion()));				
						log.debug(FormatUtils.mFormat("cookie.getSecure() :{0}", cookie.getSecure()));
						log.debug(FormatUtils.mFormat("cookie.getComment():{0}", cookie.getComment()));
					} catch(Exception e) {
						log.warn(FormatUtils.mFormat("NOT A COOKIE {0}", cookieObj.getClass().getName()));
					}
				}
				log.debug(FormatUtils.mFormat("End for(Object cookieObj: cookieHash.getElements())"));
				log.debug(FormatUtils.mFormat("Start for(Cookie cookie: requestContext.getRequest().getCookies())"));
				if((requestContext != null) && (requestContext.getRequest() != null) && (requestContext.getRequest().getCookies() != null)) {
					for(Cookie cookie: requestContext.getRequest().getCookies()) {
						try {
							log.debug(FormatUtils.mFormat("cookie.getPath()   :{0}", cookie.getPath()));
							log.debug(FormatUtils.mFormat("cookie.getName()   :{0}", cookie.getName()));
							log.debug(FormatUtils.mFormat("cookie.getValue()  :{0}", cookie.getValue()));
							log.debug(FormatUtils.mFormat("cookie.getDomain() :{0}", cookie.getDomain()));
							log.debug(FormatUtils.mFormat("cookie.getMaxAge() :{0}", cookie.getMaxAge()));
							log.debug(FormatUtils.mFormat("cookie.getVersion():{0}", cookie.getVersion()));				
							log.debug(FormatUtils.mFormat("cookie.getSecure() :{0}", cookie.getSecure()));
							log.debug(FormatUtils.mFormat("cookie.getComment():{0}", cookie.getComment()));
						} catch(Exception e) {
							log.warn(FormatUtils.mFormat("Unable to log cookie attributes {0}", cookie.getClass().getName()));
						}
						//If we match on name and path, then that's our COOKIE so return it
						if(cookieName.equals(cookie.getName())) {
							retVal = cookie;
							log.debug(FormatUtils.mFormat("Found Cookie {0}{1}", path, cookieName));
							break;
						}
					}
				}
				log.debug(FormatUtils.mFormat("End for(Cookie cookie: requestContext.getRequest().getCookies())"));
				log.debug(FormatUtils.mFormat("retVal:{0}", retVal));
				return retVal;
			}
		}
		log.debug("Exiting public static Cookie getCookie(RequestContext requestContext, String cookieName, String path)");
		return retVal;
	}

	/**
	 * Class method to set a cookie in the RequestContext cookie hash.
	 * @param context
	 * @param path
	 * @param cookieName
	 * @param cookieValueNew
	 * @param expiry
	 */
	public static void setCookieValue(RequestContext context, String path, String cookieName, String cookieValueNew, int expiry)
	{
		log.debug("Entering public static void setCookieValue(RequestContext context, String cookieName, String cookieValue)");

		log.debug(FormatUtils.mFormat("path       :{0}", path));
		log.debug(FormatUtils.mFormat("cookieName :{0}", cookieName));
		log.debug(FormatUtils.mFormat("cookieValue:{0}", cookieValueNew));
		log.debug(FormatUtils.mFormat("expiry     :{0}", expiry));

		Cookie cookie;
		String cookieValueOld = Utils.getCookieValue(context, cookieName, path);
		//See if the cookie already exists
		//		cookie = Utils.getCookie(context, cookieName, path);
		if(cookieValueOld != null)
		{
			log.info(FormatUtils.mFormat("Cookie {0}/{1} exists oldValue={2} newValue={3}", path, cookieName, cookieValueOld, cookieValueNew));
		}

		//Set it to what we want
		cookie = null;
		cookie = new Cookie(cookieName, cookieValueNew);
		cookie.setMaxAge(expiry);
		cookie.setPath(path);

		context.getResponse().addCookie(cookie);
		log.debug("Added cookie to response");
		//		context.getRgetResponse().Cookie(cookie);
		//		context.getResponse().addCookie(cookie);
		//				context.getCookies().append(cookieName, cookie);
		context.getCookies().put(cookieName, cookie);
		log.debug("Exiting public static void setCookieValue(RequestContext context, String cookieName, String cookieValue)");
	}


	/**
	 * URL Encodes Strings to UTF-8
	 * If there's an encoding problem, passes back the original value
	 * @param val
	 * @return
	 */
	public static String URLEncode(String val)
	{
		debug(urlEncodeLog, "Entering public static String URLEncode(String val)");
		if(false) return val;
		String retVal = val;
		if(retVal != null)
		{
			try 
			{
				retVal = URLEncoder.encode(val, NikonDomainConstants.DEFAULT_ENCODING);
			} 
			catch (UnsupportedEncodingException e) 
			{
				error(urlEncodeLog, e); 
			}
		}
		debug(urlEncodeLog, FormatUtils.mFormat("   val:{0}", val));
		debug(urlEncodeLog, FormatUtils.mFormat("retVal:{0}", retVal));
		debug(urlEncodeLog, "Exiting public static String URLEncode(String val)");
		return retVal;

	}

	/**
	 * URL Decodes Strings to UTF-8
	 * If there's an encoding problem, passes back the original value
	 * @param val
	 * @return
	 */
	public static String URLDecode(String val)
	{
		debug(urlDecodeLog, "Entering public static String URLDecode(String val)");

		if(false) return val;

		String retVal = val;
		if(retVal != null)
		{
			try 
			{
				retVal = URLDecoder.decode(val, NikonDomainConstants.DEFAULT_ENCODING);
			} 
			catch (UnsupportedEncodingException e) 
			{
				error(urlDecodeLog, e); 
			}
		}
		debug(urlDecodeLog, FormatUtils.mFormat("   val:{0}", val));
		debug(urlDecodeLog, FormatUtils.mFormat("retVal:{0}", retVal));
		debug(urlDecodeLog, "Exiting public static String URLDecode(String val)");
		return retVal;

	}

	//Logging wrappers

	public static void debug(Log log, String msg)
	{
		if(log.isDebugEnabled())
		{
			log.debug(msg);
		}
	}

	public static void debug(Log log, Throwable thrw)
	{
		if(log.isDebugEnabled())
		{
			log.debug(thrw);
		}
	}

	public static void debug(Log log, String msg, Throwable thrw)
	{
		if(log.isDebugEnabled())
		{
			log.debug(msg, thrw);
		}
	}

	public static void info(Log log, String msg)
	{
		if(log.isInfoEnabled())
		{
			log.info(msg);
		}
	}

	public static void info(Log log, Throwable thrw)
	{
		if(log.isInfoEnabled())
		{
			log.info(thrw);
		}
	}

	public static void info(Log log, String msg, Throwable thrw)
	{
		if(log.isInfoEnabled())
		{
			log.info(msg, thrw);
		}
	}

	public static void error(Log log, String msg)
	{
		if(log.isErrorEnabled())
		{
			log.error(msg);
		}
	}

	public static void error(Log log, Throwable thrw)
	{
		if(log.isErrorEnabled())
		{
			log.error(thrw);
		}
	}

	public static void error(Log log, String msg, Throwable thrw)
	{
		if(log.isErrorEnabled())
		{
			log.error(msg, thrw);
		}
	}

	public static void warn(Log log, String msg)
	{
		if(log.isWarnEnabled())
		{
			log.warn(msg);
		}
	}

	public static void warn(Log log, Throwable thrw)
	{
		if(log.isWarnEnabled())
		{
			log.warn(thrw);
		}
	}

	public static void warn(Log log, String msg, Throwable thrw)
	{
		if(log.isWarnEnabled())
		{
			log.warn(msg, thrw);
		}
	}

	public static void fatal(Log log, String msg)
	{
		if(log.isFatalEnabled())
		{
			log.fatal(msg);
		}
	}

	public static void fatal(Log log, Throwable thrw)
	{
		if(log.isFatalEnabled())
		{
			log.fatal(thrw);
		}
	}

	public static void fatal(Log log, String msg, Throwable thrw)
	{
		if(log.isFatalEnabled())
		{
			log.fatal(msg, thrw);
		}
	}

}
