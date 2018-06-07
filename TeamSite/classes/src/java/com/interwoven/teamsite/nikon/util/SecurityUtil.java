package com.interwoven.teamsite.nikon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.io.DOMReader;
import org.xml.sax.InputSource;

import com.interwoven.cssdk.access.CSAuthenticationException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.factory.CSFactory;
import com.interwoven.cssdk.factory.CSJavaFactory;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class SecurityUtil {

	private MessageDigest md;
	public static final int B64_SIMPLE_STRING = 0;
	public static final int B64_NOT_PATH_AND_FILE_NAME = 1;
	public static final int B64_PATH_AND_FILE_NAME = 2;
	public static final int B64_FILE_NAME_ONLY = 3;
    private static Log log = LogFactory.getLog(SecurityUtil.class);

	public SecurityUtil()
	{
		try 
		{
			md = MessageDigest.getInstance("SHA");
		} 
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			log.debug("NoSuchAlgorithmException", e);
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Method to create an obfusticated copy of a file within Teamsite.
	 * Given a vPath  

	 * @param properties Properties to attach to the Application
	 * @param locale 	 The Locale
	 * @param appName    The name of the Application
	 * @param hostName   The hostname
	 * @param vPath      The Starting vPath of the List of files to copy
	 * @param fileList   A List of file names relative to the vPath
	 * @return Instance of SecuirtyTo which contains a Map of files copied successfully and a List of files which didn't copy
	 * @throws CSAuthenticationException
	 * @throws CSRemoteException
	 * @throws CSException
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException 
	 */
	public SecurityTo obfusticateAndCopyFiles(Properties properties, String vPath, Locale locale, String appName, String hostName, String newStartingPoint, List<String> fileList)
	throws CSAuthenticationException, CSRemoteException, CSException, NoSuchAlgorithmException, UnsupportedEncodingException
	{
		//Return information about what went right/wrong
		SecurityTo secTo = new SecurityTo();
		

		//TODO this needs to work from a workflow context
		CSFactory lc = CSJavaFactory.getFactory(properties);
		CSClient client = lc.getClientForCurrentUser(locale, appName, hostName);

		int testCnt = -1;
		for(String fileNameStr : fileList)
		{
	    	int firstSlash = fileNameStr.indexOf('/');
	    	//Should give us sites/ for instance
	    	String rootInVPath = firstSlash >0?fileNameStr.substring(0,firstSlash + 1 ):"";
	    	//rest of the file name
	    	String remainder = fileNameStr.substring(firstSlash > 0?firstSlash + 1:0, fileNameStr.length());

			//Take the remained and obfusticate only the name of the file.
			String obfusticatedFileName = b64DigestEncoder(remainder, B64_FILE_NAME_ONLY);

			//Reference the file including the vPath
			CSVPath vPathFileLocation = new CSVPath(vPath + fileNameStr);

			//Fudge a refernce to the root
			File fooFile = new File(remainder);
			String newRelativeDir = rootInVPath + "/" + newStartingPoint + "/" + fooFile.getParent().replaceAll("\\\\", "/") + "/";

			//Create the new file name including the new path omitting the vPath.
			String newFileStr = newRelativeDir + obfusticatedFileName;

			
			//Create the directory if it doesn't exist
			String newDirToCreate = "//" + hostName + "/" + vPath + "/" + newRelativeDir;
			log.debug("newDirToCreate:" + newDirToCreate);
			File newDirToCreateFile = new File(newDirToCreate);
			newDirToCreateFile.mkdirs();

			CSAreaRelativePath obfuscatedRelativePath = new CSAreaRelativePath(newFileStr);

			//Get a handle to the file to copy and copy to the new location with the new name
			CSFile fileToCopy = client.getFile(vPathFileLocation);
			log.debug(mFormat("Copying {0} to {1}", vPath + fileNameStr, newFileStr));
			fileToCopy.copy(obfuscatedRelativePath, true);
			
			//Check if it created OK, if so put it in the return map
			vPathFileLocation = new CSVPath(vPath + obfuscatedRelativePath);
			
			//Force an error. Testing purposes
			if((testCnt --) == 0 )
			{
				vPathFileLocation = new CSVPath(vPath + "errorInPath" + obfuscatedRelativePath);
			}
			
			fileToCopy = client.getFile(vPathFileLocation);
			
			if((fileToCopy != null) && (fileToCopy.isValid()))
			{
				//We need the proceeding slash in order to do the Map matching in the visitor below
				secTo.getMapOfFilesSuccessfullyCopied().put("/" + fileNameStr, "/" + newFileStr);
			}
			else
			{
				secTo.getListOfFilesNotCopied().add("/" + fileNameStr);
			}
			
		}
		return secTo;
	}

	public void obfusticateDCRs(String host, String vPathRoot, List<String> dcrFileList, Map<String, String> map) 
	throws Exception
	{
		for(String fileNameStr: dcrFileList)
		{
			String fileName = "//" + host + vPathRoot + "/" + fileNameStr;
			log.debug(mFormat("Processing dcr:{0}", fileName));
			
			//Create the file and parse as a Doc
			File f = new File(fileName);
			org.dom4j.Document doc = getDom4JDocFromW3CDoc(getXmlDoc(f));

			//Create our visitor and search and replace
			SearchAndReplaceVisitor mv = new SearchAndReplaceVisitor(map);
			doc.accept(mv);
			
			//If we've changed anything then re-write the DCR
			if(mv.isDirty())
			{
				log.debug("DIRTY");
				FileWriter fw = new FileWriter(f);
				doc.write(fw);
				fw.flush();
				fw.close();
			}
			else
			{
				log.debug("NOT DIRTY");
			}
		}		

	}
	public String b64DigestEncoder(String message)
	throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		return b64DigestEncoder(message, B64_SIMPLE_STRING);
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

	private String _b64Digest(String part) throws UnsupportedEncodingException{
//		return URLEncoder.encode(new String(md.digest(part.getBytes())), "UTF-8");
		return new String(Base64.encode(md.digest(part.getBytes()))).replaceAll("\n", "").replaceAll("/", "").replaceAll("=", "").replaceAll("\\+", "");
	}
	
	
	public static String mFormat(String formatMask, Object o1)
	{
		return MessageFormat.format(formatMask, new Object[]{o1});
	}
	public static String mFormat(String formatMask, Object o1, Object o2)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2});
	}
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3});
	}
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4});
	}
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5});
	}
	public static String mFormat(String formatMask, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6)
	{
		return MessageFormat.format(formatMask, new Object[]{o1, o2, o3, o4, o5, o6});
	}

	public class SearchAndReplaceVisitor 
	implements Visitor
	{
		private boolean dirty;
		
		Map<String, String> map;
		public SearchAndReplaceVisitor(Map<String, String> map)
		{
			this.map = map;
		}
		
		public void visit(Document arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(DocumentType arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(Element arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(Attribute arg0) {
			if(map.containsKey(arg0.getValue()))
			{
				log.debug(mFormat("Replacing {0} with {1} in attribute {2}", arg0.getValue(), map.get(arg0.getValue()), arg0.getName()));
				arg0.setValue(map.get(arg0.getValue()));
				dirty = true;
			}
		}

		public void visit(CDATA arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(Comment arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(Entity arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(Namespace arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(ProcessingInstruction arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visit(Text arg0) {
			if(map.containsKey(arg0.getText()))
			{
				log.debug(mFormat("Replacing {0} with {1} in text {2}", arg0.getText(), map.get(arg0.getText()), arg0.getName()));
				arg0.setText(map.get(arg0.getText()));
				dirty = true;
			}
		}

		public boolean isDirty() {
			return dirty;
		}

		public void setDirty(boolean dirty) {
			this.dirty = dirty;
		}
		
	}
	
    public static org.w3c.dom.Document getXmlDoc(File f) 
    throws Exception
    {

        FileInputStream in = new FileInputStream(f);

        // Build the document with DOM and Xerces, no validation
        // Create a buffered reader for the parser
        DOMParser parser = new DOMParser();
        org.w3c.dom.Document doc = null;

        try {

            // Parse the whole file
            parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

            // Don't validate the DTD.
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            

            // Parse It
            parser.parse(new InputSource(in));

            // Got a DOM tree?
            doc = parser.getDocument();
            
        }
        catch (Exception e) {
            log.debug("Error creating doc: " + e);
            throw e;
        }
        finally {
            in.close();
        }

        return doc;
    }
    
	public static org.dom4j.Document getDom4JDocFromW3CDoc(org.w3c.dom.Document document)
	throws Exception
	{
		DOMReader a = new DOMReader();
		return a.read(document);
	}
	    
}
