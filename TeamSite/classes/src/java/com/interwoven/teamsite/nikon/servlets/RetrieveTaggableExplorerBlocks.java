package com.interwoven.teamsite.nikon.servlets;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.json.JSONException;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSDir;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSFileKindMask;
import com.interwoven.cssdk.filesys.CSNode;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSSortKey;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.livesite.dom4j.Dom4jUtils;

public class RetrieveTaggableExplorerBlocks extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(RetrieveTaggableExplorerBlocks.class);
	
	public void init() throws ServletException {
		
	}
	
	/** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
    	try {
			processRequest(request, response);
		} catch (CSObjectNotFoundException e) {
			e.printStackTrace();
		} catch (CSAuthorizationException e) {
			e.printStackTrace();
		} catch (CSExpiredSessionException e) {
			e.printStackTrace();
		} catch (CSRemoteException e) {
			e.printStackTrace();
		} catch (CSException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, CSObjectNotFoundException, CSAuthorizationException, CSExpiredSessionException, CSRemoteException, CSException, JSONException {
		
    	logger.debug("Start processRequest");
    	
		CSClient client = (CSClient) request.getAttribute("iw.csclient");
		
		String currentWorkarea = request.getParameter("workarea");
		
		Document taggingXML = Dom4jUtils.newDocument("<tagging/>");
		         taggingXML.setXMLEncoding("UTF-8");
		 
		if (!currentWorkarea.equals("")) {

			String currentLocale = request.getParameter("locale");
						
			// Parse All DCRs in taggable_content_explorer_block
			CSVPath csVpath = new CSVPath(currentWorkarea + "/templatedata/" + currentLocale + "/taggable_content_explorer_block/data");
			
			CSDir rootDir = (CSDir) client.getFile(csVpath);
			
			CSSortKey[] sortArray = new CSSortKey[1];
	                    sortArray[0] = new CSSortKey(CSSortKey.NAME, true);
	                
	        System.out.println("Retrieving DRCs...");
	        
			CSIterator files = rootDir.getFiles(CSFileKindMask.SIMPLEFILE | CSFileKindMask.DIR, sortArray, CSFileKindMask.SIMPLEFILE | CSFileKindMask.DIR, "(.*)", 0, -1);
			
			while(files.hasNext()) {
				
				CSNode node = (CSNode)files.next();
				
				if (node.getKind() == CSDir.KIND) {
				
					recursiveWalk(client, node, taggingXML);
				
				} else if (node.getKind() == CSSimpleFile.KIND) {
					
					CSFile file = (CSFile) node;
					
					parseDCRFile(client, file, taggingXML);
				}
			}
			
			// Convert to JSON
			XMLSerializer xmlSerializer = new XMLSerializer();
	        JSON json = xmlSerializer.read( taggingXML.asXML() );

			// Write JSON Response
			response.setContentType("application/json;charset=UTF-8");
	        response.setHeader("Cache-Control", "no-cache");
	        response.getWriter().write(json.toString(4));
		}
		
        return;
		
	}
    
    public static void recursiveWalk (CSClient client, CSNode node, Document taggingXML) throws CSAuthorizationException, CSExpiredSessionException, CSRemoteException, CSException {
		
		CSSortKey[] sortArray=new CSSortKey[1];
        sortArray[0]=new CSSortKey(CSSortKey.NAME, true);

        if (node.getKind() == CSDir.KIND) {
			
        	CSDir dir = (CSDir) node;
        	
			CSIterator files = dir.getFiles(CSFileKindMask.SIMPLEFILE | CSFileKindMask.DIR, sortArray, CSFileKindMask.SIMPLEFILE | CSFileKindMask.DIR, "(.*)", 0, -1);
			
			while(files.hasNext()) {
				
				CSNode node2 = (CSNode)files.next();
				
				if (node2.getKind() == CSDir.KIND) {
					
					recursiveWalk(client, node2, taggingXML);
				
				} else if (node2.getKind() == CSSimpleFile.KIND) {
					
					CSFile file = (CSFile) node2;
					
					parseDCRFile(client, file, taggingXML);
				}
			}
			
		} else if (node.getKind() == CSSimpleFile.KIND) {
			
			parseDCRFile(client, (CSFile)node, taggingXML);
		}
	}
    
    public static void parseDCRFile (CSClient client, CSFile file, Document taggingXML) throws CSAuthorizationException, CSObjectNotFoundException, CSExpiredSessionException, CSRemoteException, CSException {
		
		String fileXML = "";
			
		if ((file != null) && 
			(file.isValid()) && 
			(file.getKind() == CSSimpleFile.KIND)) {
				
			BufferedInputStream in = null;
			Reader reader = null;
				
			try {
					
				in = ((CSSimpleFile) file).getBufferedInputStream(false);
				reader = new InputStreamReader(in, "UTF-8");
					
				int bytesRead;
					
				while ((bytesRead = reader.read()) != -1) {
		                
					fileXML += (char) bytesRead;
		        }
					
			} catch (IOException ex) {
				ex.printStackTrace();
		    } finally {
		            
		        	try {
		                if (in != null)
		                    in.close();
		            } catch (IOException ex) {
		                ex.printStackTrace();
		            }
		    }
		    
		    Document pageXML = Dom4jUtils.newDocument(fileXML);

		    taggingXML.getRootElement().add(pageXML.getRootElement().detach());

		}
		
		return;
	}

	/** Returns a short description of the servlet.
     */
    public String getServletInfo()
    {
        return "Retrieves the tagging explorer blocks in JSON format.";
    }
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }

    /** Destroys the servlet.
     */
    public void destroy()
    {

    }
}
