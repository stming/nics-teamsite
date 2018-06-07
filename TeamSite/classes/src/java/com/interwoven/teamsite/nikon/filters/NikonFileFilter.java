package com.interwoven.teamsite.nikon.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.teamsite.ext.util.FormatUtils;

/**
 * This class is a file filter class which passes or rejects files based on their name
 * @author nbamford
 *
 */
public class NikonFileFilter 
implements FileFilter
{
	private LinkedList<String> filterList;
    private static Log log = LogFactory.getLog(NikonFileFilter.class);

	private String excludeFile;

	public NikonFileFilter(String excludeFile, List<String> filterListz)
	{

		this.excludeFile = excludeFile;
		this.filterList = new LinkedList<String>();
		for (String filter : filterListz) {
			this.filterList.add(filter.trim().toUpperCase());
		}
	}
	
	public NikonFileFilter(String excludeFile, String[] filterArr)
	{
		this.excludeFile = excludeFile;
		filterList = new LinkedList<String>();
		for (int i = 0; i < filterArr.length; i++) {
			filterList.add(filterArr[i].trim().toUpperCase());
		}
	}

	public boolean accept(File arg0)
	{
		boolean retVal = arg0.isFile()?filterList.contains(fileExt(arg0.getName())):false;
		
		//Once we're happy that it's a file were interested in then get rid of the thumbnails
		if(retVal)
		{
			retVal = arg0.getName().indexOf("_thumb") < 0;
		}
		
		if(false)
		{
		if((excludeFile) != null && (retVal))
		{
			retVal = arg0.getName().indexOf(excludeFile) < 0;
		}
		}
		log.debug(FormatUtils.mFormat("For file {0} returning a value of {1}", arg0.getAbsolutePath(), retVal));
		return retVal;
	}

	private String fileExt(String fileName)
	{
		String ext = fileName.substring(fileName.indexOf(".")).toUpperCase();
		return ext;
	}
}
