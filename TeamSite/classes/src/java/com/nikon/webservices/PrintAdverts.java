package com.nikon.webservices;

import java.io.*;
import org.xml.sax.*;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;


public class PrintAdverts {

	private static Log LOG = LogFactory.getLog(PrintAdverts.class);

	public Document getPrintAdverts(RequestContext context) {
		Document doc = Dom4jUtils.newDocument("<DCRs/>");
		String path = context.getParameterString("dcr_path");
		//get the path to the dcr
		String dcrpath = new String();

		dcrpath = context.getFileDal().getRoot()+"/"+path;

		//open the dir with all the dcrs in and build the xml
		File dir = new File(dcrpath);
		Document dcrdoc = null;
		java.io.InputStream is = null;

		File[] children = dir.listFiles();
		if (children == null) {
		    // Either dir does not exist or is not a directory
			LOG.info("dir not found");
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        String filename = dcrpath + "\\" + children[i].getName();
		        LOG.debug("found file "+ filename);
		        //open the file
		        File dcrfile = null;
		        try {
		            dcrfile = new File(filename);
		        } catch (Exception e) {
		            LOG.error("Error getting the dcr "+ dcrfile.toString());
		        }
		        LOG.debug("found the dcr "+ dcrfile.toString());

				//parse as XML
		        SAXReader reader = new SAXReader();

		        //disable dtd parsing...
		        reader.setEntityResolver(new EntityResolver() {
		            public InputSource resolveEntity(String publicId, String systemId)
		                    throws SAXException, IOException {
		                if (systemId.contains(".dtd")) {
		                    return new InputSource(new StringReader(""));
		                } else {
		                    return null;
		                }
		            }
		        });


		        try{
		        	dcrdoc = reader.read(dcrfile);
		        }catch (DocumentException de){
		        	LOG.error("Error reading the dcr in as XML "+ de.toString());
		        }
		        Element eDCR = doc.getRootElement().addElement("dcr").addAttribute("dcrname", children[i].getName());
		        //add each doc to the final return doc.
		        org.dom4j.Node node = null;
		        try{
		        	node = dcrdoc.getRootElement();
		        }catch (Exception e){
		        	LOG.error("Error selecting the node "+ e.toString());
		        }

		        eDCR.add(node);

		    }
		}



		return doc;

	}

}
