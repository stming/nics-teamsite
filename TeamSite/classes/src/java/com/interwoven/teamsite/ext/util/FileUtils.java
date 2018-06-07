package com.interwoven.teamsite.ext.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author nbamford
 *
 * Class containing some useful File utilities
 */
/**
 * @author nbamford
 *
 */
/**
 * @author nbamford
 *
 */
public class FileUtils {


	private static Log log = LogFactory.getLog(FileUtils.class);

	public final static void IOCopy(File iFile, File oFile) 
	throws IOException
	{
		IOCopy(new FileInputStream(iFile), new FileOutputStream(oFile), 1024);
	}

	/**
	 * Copy an input stream to an outputs3tream with a fixed buffer size
	 * of 1024k
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public final static void IOCopy(InputStream is, OutputStream os) 
	throws IOException
	{
		IOCopy(is, os, 1024);
	}

	/**
	 * Copy an input stream to an outputstream with a variable buffer
	 * size
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public final static void IOCopy(InputStream is, OutputStream os, int bufferSize) 
	throws IOException
	{
		log.debug("Entering public final static void IOCopy(InputStream is, OutputStream os)");
		BufferedInputStream bis = new BufferedInputStream(is);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		int count = 0;
		byte[] b = new byte[bufferSize];

		while((count = bis.read(b)) != -1)
		{
			bos.write(b,0,count);
		}
		bos.flush();
		log.debug("Exiting public final static void IOCopy(InputStream is, OutputStream os)");
	}

	public final static void IOCopy(File iFile, File oFile, int bufferSize) 
	throws IOException
	{
		log.debug("Entering public final static void IOCopy(File iFile, File oFile, int bufferSize)");

		IOCopy(new FileInputStream(iFile), new FileOutputStream(oFile), bufferSize);

		log.debug("Exiting public final static void IOCopy(File iFile, File oFile, int bufferSize)");
	}

	public final static void zipFile(File zipFile, File[] arrOfFiles)
	throws Exception
	{
		zipFile(zipFile, arrOfFiles, 1024);
	}

	public final static void zipFile(File zipFile, File[] arrOfFiles, int bufferSize)
	throws Exception
	{
		List<File> listOfFiles = new LinkedList<File>();
		for(File f : arrOfFiles)
		{
			listOfFiles.add(f);
		}

		zipFile(zipFile, listOfFiles, bufferSize);
	}

	public final static void zipFile(File zipFile, List<File> listOfFiles)
	throws Exception
	{
		zipFile(zipFile, listOfFiles, 1024);
	}

	public final static void zipFile(File zipFile, Map<File, String> mapOfFilesAndNames)
	throws Exception
	{
		zipFile(zipFile, mapOfFilesAndNames, 1024);
	}

	public final static void zipFile(File zipFile, List<File> listOfFiles, int bufferSize)
	throws Exception
	{
		//If we only pass a List<File> then the name of the file in the zip should be the original name of the file
		//So create the Map<File, String> with the values of File, File.getName()
		Map<File, String> fileMap = new LinkedHashMap<File, String>();
		for(File f: listOfFiles)
		{
			fileMap.put(f, f.getName());
		}
		zipFile(zipFile, fileMap, bufferSize);
	}

	/**
	 * Util method to create a Zip file containing 1..n files as dictated by values in mapOfFilesAndName
	 * Currently no folder structure is maintained in the Zip file
	 * @param zipFile - Path to Zip file to contain content
	 * @param mapOfFilesAndNames - Map of File and fileName objects
	 * @param bufferSize - Buffer Size
	 * @throws Exception
	 */
	public final static void zipFile(File zipFile, Map<File, String> mapOfFilesAndNames, int bufferSize)
	throws Exception
	{
		// Create a buffer for reading the files
		byte[] buf = new byte[bufferSize];

		try {
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

			// Compress the files
			for(File f: mapOfFilesAndNames.keySet())
			{
				FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(mapOfFilesAndNames.get(f)));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = fis.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Complete the entry
				out.closeEntry();
				bis.close();
			}

			// Complete the ZIP file
			out.close();
		} 
		catch (IOException e){
			throw e;
		}
	}

	public final static void write2File(String fileName, String message)
	throws IOException
	{
		write2File(new File(fileName), message);
	}

	public final static void write2File(File file, String message)
	throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bos.write(message.getBytes());
		bos.flush();
		bos.close();
		fos.flush();
		fos.close();
	}

	/**
	 * This method will return the path to a file or the path itself
	 * 
	 * @param file
	 * @return
	 */
	public final static File fileToFolder(File file) {
		log.debug("Entering public final static File fileToFolder(File file)");
		File retFile = file;

		log.info(FormatUtils.mFormat("file.absaloutPath:{0}", file.getAbsolutePath()));

		//If it's a file then get its parent path
		if(file.isFile())
		{
			retFile = file.getParentFile();
		}

		log.debug("Exiting public final static File fileToFolder(File file)");

		return retFile;
	}

	/**
	 * Static helper method to return the extension of a file, minus the '.'
	 * @param file
	 * @return extension of the file minus '.'
	 */
	public final static String fileExtension(File file)
	{
		String retVal = "";

		if(file != null)
		{
			int lstIdx = -1;
			lstIdx = file.getAbsolutePath().lastIndexOf(".");
			if(lstIdx >= 0)
			{
				retVal = file.getAbsolutePath().substring(lstIdx + 1, file.getAbsolutePath().length());
			}
		}
		return retVal;
	}

	/**
	 * Static helper method to return the vPath of a file, given the path and the relative filename
	 * @param path Path usually retrieved from the RequestContext
	 * @param fileName relative filename
	 * @return String VPath equivalent
	 */
	public final static String fileVpath(String path, String fileName)
	{
		return FormatUtils.mFormat("{0}/{1}", path, fileName);
	}

	/**
	 * Method to return the contents of a File into a StringBuffer
	 * @param fileName
	 * @return
	 */
	public final static StringBuffer file2StringBuffer(String fileName)
	{
		BufferedReader source = null;
		StringBuffer retSb = new StringBuffer();
		try {
			source = new BufferedReader(new FileReader( fileName ));

			String line = source.readLine();

			while (line != null )
			{
				//this is where I am trying to load the file to the StringBuffer 
				retSb.append(line); 
				line = source.readLine();
			}		
		} catch (FileNotFoundException e) {
			retSb.append(e.getMessage());
			log.error("FileNotFoundException", e);
		} catch (IOException e) {
			retSb.append(e.getMessage());
			log.error("IOException", e);
		}
		finally
		{
			return retSb;
		}
	}
}