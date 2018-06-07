package com.nikon.utils;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import org.dom4j.*;
import org.dom4j.io.*;
import com.interwoven.livesite.external.*;
import com.interwoven.livesite.file.*;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.sql.*;

import com.interwoven.livesite.runtime.servlet.RequestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author epayne
 * @author fpriego
 */
public class ProductsCatalogueList {
	
	private Log log = LogFactory.getLog(ProductsCatalogueList.class);

	/** default constructor */
	public ProductsCatalogueList() {


	}

	public Document getProductListData(RequestContext context) throws DocumentException {



		// get query string parameter named "lang" from context
		String langValue = null;
		String paramValue = null;
		String Subnav1Param = null;
		String Subnav2Param = null;
		String Subnav3Param = null;
		String ID = null;
		String RunQuery = "0";
		String SQLDynQuery = "";

		{
			Cookie tcookie = context.getCookies().getCookie("langCookie");

			if (tcookie != null) {
				langValue = tcookie.getValue().toString();
			} else {
				langValue = "de_CH";
			}
		}

		       //get parameters

        //get main one
        if (context.getRequest().getParameter("ParamValue") == null) {
            //set default data for now, prob best to do in xsl ***
            paramValue = "Digital Cameras";

        } else {
            paramValue = context.getRequest().getParameter("ParamValue");

        }
        //get sub nav level 1 param
        if (context.getRequest().getParameter("Subnav1Param") == null) {
            Subnav1Param = "SLR";

        } else {
            Subnav1Param = context.getRequest().getParameter("Subnav1Param");

        }

        //get sub nav level 2 param
        if (context.getRequest().getParameter("Subnav2Param") == null) {
            Subnav2Param = "Consumer";
        } else {
            Subnav2Param = context.getRequest().getParameter("Subnav2Param");

        }

        //get sub nav level 3 param
        if (context.getRequest().getParameter("Subnav3Param") == null) {
            Subnav3Param = "0";
        } else {
            Subnav3Param = context.getRequest().getParameter("Subnav3Param");

        }

        if (context.getRequest().getParameter("RunQuery") == null) {
            //set default data for now, prob best to do in xsl ***
            RunQuery = "0";

        } else {
            RunQuery = context.getRequest().getParameter("RunQuery");

        }

        
		//Generate the Query
		SQLDynQuery = "SELECT Path,Product,isnew, UPC FROM productmetadata WHERE ";
		SQLDynQuery = SQLDynQuery + " Country = '" + langValue.replaceAll("'", "''") + "' " ;
		SQLDynQuery = SQLDynQuery + " AND Discontinued = 0 " ;
		SQLDynQuery = SQLDynQuery + " AND WWA < GETDATE() " ;
		   // Add the uri parameter to the query
		SQLDynQuery = SQLDynQuery + " AND ProductCategory = '" + paramValue.replaceAll("'", "''") + "'" ;
		if(!Subnav1Param.equals("0")) 
			SQLDynQuery = SQLDynQuery + " AND Subnav1Param  = '" + Subnav1Param.replaceAll("'", "''") + "'" ;		
		if(!Subnav2Param.equals("0")) 
			SQLDynQuery = SQLDynQuery + " AND Subnav2Param  = '" + Subnav2Param.replaceAll("'", "''") + "'" ;		
		if(!Subnav3Param.equals("0")) 
			SQLDynQuery = SQLDynQuery + " AND Subnav3Param  = '" + Subnav3Param.replaceAll("'", "''") + "'" ;
            // Order By (change?)
		SQLDynQuery = SQLDynQuery + " ORDER BY NavOrder, Product" ;
	
		// gcreate main doc
		Document doc = Dom4jUtils.newDocument("<staticcontent/>");
		Element root = doc.getRootElement();

		// get sql data if query is required
		//get database connection reqs
		String pool = context.getParameterString("Pool");
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		//debug data
		doc.getRootElement().addElement("query").addText(SQLDynQuery + " - " + RunQuery);
		//add the locale
		doc.getRootElement().addElement("nklocale").addText(langValue);

			// Try to connect to the database
			try {
				con = RequestUtils.getConnection(pool);
				stmt = con.createStatement();
				rs = stmt.executeQuery(SQLDynQuery);

				//get data 
				while (rs.next()) {
					if (rs.getString(1) == null) {
					} else {
						Document dcrDocument = ExternalUtils.readXmlFile(context, rs.getString(1));
						if( dcrDocument != null ) {
							
							
							Element dcrRoot = dcrDocument.getRootElement();
							/***** Create the root node from the DCR document to your document that will be returned to the component XSL*/
							Element dcr = root.addElement(dcrRoot.getName());
							dcr.addAttribute("Status", "Success");
							dcr.addAttribute("DCRPath",rs.getString(1));
							for ( Iterator i = dcrRoot.attributeIterator(); i.hasNext(); ) {
								Attribute attribute = (Attribute) i.next();
								log.debug("attribute = "+attribute);
								dcr.addAttribute(attribute.getName(), attribute.getValue());
							}
							
							// Add the Product short name to the doc and the isnew property
							dcr.addElement("product_title").addText(rs.getString(2));
							dcr.addElement("is_new").addText((rs.getString(3) == null) ? "0" : rs.getString(3));
							dcr.addElement("upc").addText((rs.getString(4) == null) ? "0" : rs.getString(4));
							/***** Copy rest of the nodes from DCR document to your document that will be returned to the component XSL */
							for(Iterator i = dcrRoot.elementIterator(); i.hasNext();){
								Element element = (Element) i.next();
								log.debug("attribute = "+element);
								dcr.add(element.createCopy());            					
							}
						}

						//                        doc.getRootElement().addElement("ProductRow").addText(rs.getString(1));
					}

				}


				//close dbase
				rs.close();
				stmt.close();
				con.close();


			} catch (Exception e) {
				e.printStackTrace();
				//debug output
				doc.getRootElement().addElement("connected").addText("CONNECTION ERROR");
				log.error("Exception", e);
			} finally {
	        	if (rs != null) {
	        		try {
	        			rs.close();
	        		} catch (SQLException e){
	        			log.error("Error while trying to close result set", e);
	        		}
	        	}
	        	if (stmt != null) {
	        		try {
	        			stmt.close();
	        		} catch (SQLException e){
	        			log.error("Error while trying to close Statement", e);
	        		}
	        	}
	        	if (con != null) {
	        		try {
	        			con.close();
	        		} catch (SQLException e){
	        			log.error("Error while trying to close connection", e);
	        		}
	        	}
	        }
		


	        if (context.getRequest().getParameter("RunQuery") == null) {
	            //set default data for now, prob best to do in xsl ***
	            RunQuery = "0";
	        } else {
	            RunQuery = context.getRequest().getParameter("RunQuery");

	        }

		//return selected parameters
		doc.getRootElement().addElement("sParamValue").addText(paramValue);
		doc.getRootElement().addElement("sSubnav1Param").addText(Subnav1Param);
		doc.getRootElement().addElement("sSubnav2Param").addText(Subnav2Param);
		doc.getRootElement().addElement("sSubnav3Param").addText(Subnav3Param);
	//	doc.getRootElement().addElement("sID").addText(ID);


		// return the XML to the component
		return doc;


	}
}
