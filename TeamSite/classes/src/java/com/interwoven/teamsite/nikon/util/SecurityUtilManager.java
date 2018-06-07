package com.interwoven.teamsite.nikon.util;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import com.interwoven.cssdk.access.CSAuthenticationException;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.teamsite.ext.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SecurityUtilManager {
	
	private Log log = LogFactory.getLog(SecurityUtilManager.class);

	private String useCase;
	private String propertyFileLoc;
	private String vPath;
	private String localeLanguage;
	private String appName;
	private String hostName;
	private String newStartingPoint;
	private String fileList;
	private String listOfDCRs;
	private String listToken;
	
	SecurityUtil su = new SecurityUtil();
	
	
	static String[] args;
	/**
	 * @param args
	 */
	public static void main(String[] argz) {
		args = argz;
		
		SecurityUtilManager sum = new SecurityUtilManager();
	}
	
	public SecurityUtilManager()
	{
		try
		{
			Utils.writeFile("c:/temp/someFile.txt", "created SecurityUtilManager");
		}
		catch(Exception exception)
		{
			Utils.writeFile("c:/temp/exception1.txt", exception.getMessage());
		}

		if(args.length != 10)
		{
//			File f = new File();
//			f.getAbsolutePath();
			Utils.writeFile("c:/temp/not enough parameters.txt", "Not enough parameters, make sure non used are set to empty");
			throw new RuntimeException("Not enough parameters, make sure non used are set to empty");
		}

		useCase = args[0];
		propertyFileLoc = args[1];
		vPath = args[2];
		localeLanguage = args[3];
		appName = args[4];
		hostName = args[5];
		newStartingPoint = args[6];
		fileList = args[7];
		listOfDCRs = args[8];
		listToken = args[9];
		
		if("1".equals(useCase))
		{
			useCaseOne();
		}
		else if ("2".equals(useCase)){
			useCaseTwo();
		}
		else
		{
			String msg = "Use Case " + useCase + " not known.";

			Utils.writeFile("c:/temp/error.txt", msg);

			log.error(msg);
			throw new RuntimeException(msg);
		}

	}

	//Just the images
	private void useCaseOne()
	{
		Properties properties = null;
		if(!"".equals(propertyFileLoc))
		{
			properties = new Properties();
			properties.put("cssdk.cfg.path", propertyFileLoc);
		}
		
		SecurityTo to = null;
		try {
			to = su.obfusticateAndCopyFiles(properties, vPath, new Locale(localeLanguage), appName, hostName, newStartingPoint, tokenListToList(fileList, listToken));
		} catch (CSAuthenticationException e) {
			// TODO Auto-generated catch block
			log.error("CSAuthenticationException", e);
		} catch (CSRemoteException e) {
			// TODO Auto-generated catch block
			log.error("CSRemoteException", e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			log.error("NoSuchAlgorithmException", e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error("UnsupportedEncodingException", e);
		} catch (CSException e) {
			// TODO Auto-generated catch block
			log.error("CSException", e);
		}
		//TODO Report
		
	}
	
	//The images and the dcrs
	private void useCaseTwo()
	{

	}
	
	private List<String> tokenListToList(String tokenList, String token)
	{
		List<String> retList = new LinkedList<String>();
		for(String s: tokenList.split(token))
		{
			retList.add(s);
		}
		return retList;
	}
	

}
