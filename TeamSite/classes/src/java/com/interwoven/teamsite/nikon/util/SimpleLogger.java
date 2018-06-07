package com.interwoven.teamsite.nikon.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author nbamford
 *
 */
public class SimpleLogger {
	static File logFile;
	static FileWriter fw;
	
	private static SimpleLogger sl;
	
	private SimpleLogger(String fileName) throws IOException
	{
		sl = this;
		logFile = new File(fileName);
		fw = new FileWriter(logFile);
	}
	
	public static SimpleLogger getInstance(String fileName)
	throws IOException
	{
		if(sl == null)
		{
			sl = new SimpleLogger(fileName);
			
		}
		return getInstance();
	}
	
	public static SimpleLogger getInstance()
	{
		return sl;
	}
	
	public void debug(String message)
	{
		debug(message, null);
	}
	public void debug(String message, Throwable t)
	{
		wf("DEBUG", message + (t != null ?t.getMessage():""));
	}
	
SimpleDateFormat sdf = new SimpleDateFormat("HH:mm.ss.S");
	private void wf(String level, String message) 
	{
		Date curTime = Calendar.getInstance().getTime();
		String time = sdf.format(curTime);
		try {
			fw.write(MessageFormat.format("{0}-{1}{2}", new Object[]{time, message, "\n"}));
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
