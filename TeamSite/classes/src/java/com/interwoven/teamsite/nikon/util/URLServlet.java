package com.interwoven.teamsite.nikon.util ;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.factory.CSFactory;
import com.interwoven.cssdk.factory.CSFactoryInitializationException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Servlet for testing Obfuscation. No longer used.
 * @author nbamford
 *
 */
public class URLServlet extends HttpServlet {

			private Log log = LogFactory.getLog(URLServlet.class);
			
			MessageDigest md;
			int B64_SIMPLE_STRING = 0;
			int B64_NOT_PATH_AND_FILE_NAME = 1;
			int B64_PATH_AND_FILE_NAME = 2;
			int B64_FILE_NAME_ONLY = 3;


			String fileNameStr ;
			String vPath ;
			String newStartingPoint ;
			String itemName ;
			String newFileStr ;
			String errorThrown = null;
			String obfusticatedFileName ;
			String newRelativeDir ;

		/**
	 	* Constructor of the object.
	 	*/
		public URLServlet() {
			super();
		}

		public void destroy() {
			super.destroy(); // Just puts "destroy" string in log
			// Put your code here
		}

		public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

        	BufferedWriter fileOut = new BufferedWriter(new FileWriter("D:/Interwoven/TeamSite/tmp/URLServlet.log" ));
			fileOut.write("Started ----------\n");


			try {
				md = MessageDigest.getInstance("SHA");
			} 	catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				log.error("NoSuchAlgorithmException", e);
				errorThrown = "ERROR in MessageDigest" ;
				fileOut.write("NoSuchAlgorithmException " + e + "\n") ;
				throw new RuntimeException(e);
			}

			fileOut.write("md created" + "\n" );

   			fileNameStr = request.getParameter("imageURL");
    		vPath = request.getParameter("workarea");
    		newStartingPoint = request.getParameter("newStartingPoint");
    		itemName = request.getParameter("itemName") ;


			fileOut.write("fileNameStr : " + fileNameStr + "\n" ) ;
			fileOut.write("vPath : " + vPath + "\n" ) ;
			fileOut.write("newStartingPoint : " + newStartingPoint + "\n" ) ;
			fileOut.write("itemName : " + itemName + "\n" ) ;

    		CSFactory factory = null;
    		CSClient client = null;
    		// String username, password, role;
    		String name = null;


    		// read the factory type from a properties file
    		Properties props=new Properties();
    		try {
      			props.load(new FileInputStream("D:/Interwoven/TeamSite/cssdk/cssdk.cfg"));
    		} catch ( FileNotFoundException e ) {
				log.error("FileNotFoundException", e);
				errorThrown = "ERROR - file not found";
				fileOut.write("cssdk.cfg file not found " + e + "\n" ) ;
				// throw new RuntimeException("error in copying file") ;
    		} catch ( IOException e ) {
				log.error("IOException", e);
 				errorThrown = "ERROR - IO Exception";
 				fileOut.write("IO error " + e + "\n" ) ;
    		}

			fileOut.write("After Properties " + "\n" ) ;

    		// create the factory

    		try {
      			factory=CSFactory.getFactory(props);
      			name = factory.getClass().getName();
    		} catch (CSFactoryInitializationException e) {
				log.error("NoSuchAlgorithmException", e);
      			errorThrown = "ERROR - CSFactory " ;
      			fileOut.write("The factory cannot be initialized " + e + "\n" );
    		}

      		fileOut.write("Factory of type " + name + " created" + "\n");
      		fileOut.write("Factory object created!" + "\n");
      		fileOut.write("Type read from properties file" + "\n");

    		try {
      			client = factory.getClientForCurrentUser(Locale.getDefault(),"TeamSite", null );
      			fileOut.write( "Client object obtained \n" );
      			fileOut.write("factory version : " + factory.getClientVersion());
    		} catch ( CSException e ) {
				log.error("NoSuchAlgorithmException", e);
				errorThrown = "ERROR - CSException" ;
      			fileOut.write( "An exception occurred " + e + "\n"  );
    		} finally {
				fileOut.write("In the finally block" + "\n" ) ;
			}


	    	// int firstSlash = fileNameStr.indexOf('/');
	    	int firstSlash = fileNameStr.indexOf("imported/");
	    	fileOut.write("firstSlash : " + firstSlash) ;

	    	//Should give us sites/ for instance
	    	String rootInVPath = "";
	    	// rootInVPath = firstSlash >0?fileNameStr.substring(0,firstSlash + 1 ):"";
	    	fileOut.write("rootInVPath : " + rootInVPath + "\n" ) ;

	    	//rest of the file name
	    	String remainder = fileNameStr.substring(firstSlash > 0?firstSlash :0, fileNameStr.length());
	    	fileOut.write("remainder : " + remainder + "\n" ) ;

	    	String firstPart = fileNameStr.substring(0,firstSlash) ;
			fileOut.write("firstPart : " + firstPart + "\n" ) ;


			try {
			    //Take the remained and obfusticate only the name of the file.
				obfusticatedFileName = b64DigestEncoder(remainder, B64_PATH_AND_FILE_NAME);
				fileOut.write("obfusticatedFileName : " + obfusticatedFileName + "\n" ) ;
			} catch (NoSuchAlgorithmException e) {
				log.error("NoSuchAlgorithmException", e);
				errorThrown = "ERROR - NoSuchAlgorithm";
				fileOut.write("about obfuscatedfilename " + e + "\n" ) ;
				// throw new RuntimeException("error in copying file") ;
			} finally {
				fileOut.write("about obfuscatedfilename in finally " + "\n" ) ;
				// fileOut.close() ;
			}

			fileOut.write("after obfusticatedFile " + "\n" ) ;

			//Reference the file including the vPath
			CSVPath vPathFileLocation = new CSVPath(vPath + fileNameStr);
			fileOut.write("vPathFileLocation : " + vPathFileLocation + "\n" ) ;

			// get the directory name here :
			newRelativeDir = newStartingPoint + firstPart + obfusticatedFileName ;

			int dirStart = newRelativeDir.lastIndexOf("/") ;
			newRelativeDir = newRelativeDir.substring(0,dirStart);
			fileOut.write("newRelativeDir with obfus : " + newRelativeDir + "\n" ) ;

			//  this is created as CSAreaRelativePath should not have a leading slash
			String newRelativeDirWithoutSlash = rootInVPath + newRelativeDir ;
			fileOut.write("newRelativeDirWithoutSlash : " + newRelativeDirWithoutSlash + "\n" ) ;

			newFileStr = newStartingPoint + firstPart + obfusticatedFileName ;
			fileOut.write("newFileStr  again : " + newFileStr + "\n" ) ;

			//Create the directory if it doesn't exist
			// vPath already has the hostname.  so removing the same from the var assignment
			String newDirToCreate = vPath + "/" + newRelativeDir;
			fileOut.write("newDirToCreate:" + newDirToCreate + "\n" );
			File newDirToCreateFile = new File(newDirToCreate);
			newDirToCreateFile.mkdirs();

			CSAreaRelativePath obfuscatedRelativePath = new CSAreaRelativePath(newFileStr);
			fileOut.write("obfuscatedRelativePath : " + obfuscatedRelativePath + "\n" ) ;

			try {
				//Get a handle to the file to copy and copy to the new location with the new name
				CSFile fileToCopy = client.getFile(vPathFileLocation);
				// fileOut.write(mFormat("Copying {0} to {1}", vPath + fileNameStr, newFileStr));
				fileToCopy.copy(obfuscatedRelativePath, true);
			} catch (CSAuthorizationException e) {
				log.error("CSAuthorizationException", e);
				errorThrown = " ERROR - CSAuthorizationException";
				fileOut.write("CSAuthorizationException error " + e + "\n" ) ;
				// throw new RuntimeException("error in copying file") ;
			} catch (CSExpiredSessionException e) {
				log.error("CSExpiredSessionException", e);
				errorThrown = "ERROR - CSExpiredSessionException";
				fileOut.write("CSExpiredSessionException error " + e + "\n" ) ;
				// throw new RuntimeException("error in copying file") ;
			} catch (CSRemoteException e) {
				log.error("CSRemoteException", e);
				errorThrown = "ERROR - CSRemoteException";
				fileOut.write("CSRemoteException " + e + "\n" ) ;
				// throw new RuntimeException("error in copying file") ;
			} catch (CSException e) {
				log.error("CSException", e);
				errorThrown = "ERROR - CSException";
				fileOut.write("CSException " + e+ "\n" ) ;
				// throw new RuntimeException("error in copying file") ;
			} finally {
				fileOut.write("errorThrown in finally  : " + errorThrown + "\n" ) ;

			}


			// check whether any exception had occurred and set the value accordingly.  This is the value that will be passed
			// to the FormAPI
			newFileStr = ( errorThrown == null ? newFileStr : errorThrown ) ;

			fileOut.write("newFileStr : " + newFileStr + " errorThrown : " + errorThrown + "\n") ;

			PrintWriter out = response.getWriter();
			out.println("Content-type: text/html\n\n");
			out.println("<html>");
			out.println("<head>");
			out.println("<script language=\"javascript\">");
			out.println("var newURL = " + "'/" + newFileStr + "'" + ";" ) ;
			out.println("var iName = " + "'" + itemName  + "'" + ";" ) ;
			out.println("</script>");
			out.println("</head>");
			fileOut.write("newFileStr before JS : " + newFileStr + "\n" ) ;
			out.println("<body onload=\"parent.getScriptFrame().populateContent(iName,newURL)\">");
			out.println("  </body>");
			out.println("</html>");
			fileOut.close() ;
			out.close();
    	}





	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 *
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

      		doGet(request,response);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occure
	 */
	public void init() throws ServletException {
		// Put your code here
	}

	public String _b64Digest(String part) throws UnsupportedEncodingException{
		return new String(Base64.encode(md.digest(part.getBytes()))).replaceAll("\n", "").replaceAll("/", "").replaceAll("=", "").replaceAll("\\+", "");
	}


	public  String b64DigestEncoder(String message, int type)
		throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		String retVal = null;
		if(B64_SIMPLE_STRING == type)
		{
			retVal = _b64Digest(message);
		}
		// If we send in /a/b/c/d/someFile.txt or /a/b/c/d/someFile
		// then obfusticate someFile.txt or someFile only and return it prepended with /a/b/c/d/
		// Similary if just the filename no path and extension then obfusticate that and return
		else if(B64_NOT_PATH_AND_FILE_NAME == type)
		{
			int lastSlash = message.lastIndexOf('/') > 0 ? message.lastIndexOf('/'):0;
			int lastDot   = message.lastIndexOf('.');

			String path      = (lastSlash > 0?message.substring(0, lastSlash + 1):"");
			String fileName  = (lastDot   > 0?message.substring(lastSlash == 0?0:lastSlash + 1,lastDot):message.substring(lastSlash == 0?0:lastSlash + 1));
			String extension = (lastDot   > 0?message.substring(lastDot):"");

			retVal = path + b64DigestEncoder(fileName) + extension;

		}
		// If we send in /a/b/c/d/someFile.txt or /a/b/c/d/someFile
		// then obfusticate someFile.txt or someFile only and return it
		else if(B64_FILE_NAME_ONLY == type)
		{
			int lastSlash = message.lastIndexOf('/') > 0 ? message.lastIndexOf('/'):0;
			int lastDot   = message.lastIndexOf('.');
			String fileName  = (lastDot   > 0?message.substring(lastSlash == 0?0:lastSlash + 1,lastDot):message.substring(lastSlash == 0?0:lastSlash + 1));
			String extension = (lastDot   > 0?message.substring(lastDot):"");
			retVal = b64DigestEncoder(fileName) + extension;
		}
		// If we send in /a/b/c/d/someFile.txt or /a/b/c/d/someFile
		// then obfusticate /a/b/c/d/someFile.txt or /a/b/c/d/someFile only and return it
		else if(B64_PATH_AND_FILE_NAME == type)
		{
			String extension = null;
			String theRest = message;
			boolean startingSlash = message.charAt(0) == '/';
			int dotPos = message.lastIndexOf(".");
			if(dotPos >0)
			{
				extension = message.substring(dotPos);
				theRest = message.substring(0, dotPos);

				StringBuffer x = new StringBuffer();
				for(String enc : theRest.split("/"))
				{
					x.append(_b64Digest(enc));
					x.append("/");
				}
				x.deleteCharAt(x.length()-1);

				retVal = (startingSlash?"/":"") + (x.toString()) + (extension != null ?extension:"");
			}
		}

		return retVal;
	}

public String b64DigestEncoder(String message)
	throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		return b64DigestEncoder(message, B64_SIMPLE_STRING);
	}

}