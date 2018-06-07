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
 */
public class navProducts {
	
	private Log log = LogFactory.getLog(navProducts.class);

    /** default constructor */
    public navProducts() {


    }

    public Document constructNavProducts(RequestContext context) throws DocumentException {



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
            Subnav1Param = "0";

        } else {
            Subnav1Param = context.getRequest().getParameter("Subnav1Param");

        }

        //get sub nav level 2 param
        if (context.getRequest().getParameter("Subnav2Param") == null) {
            Subnav2Param = "0";
        } else {
            Subnav2Param = context.getRequest().getParameter("Subnav2Param");

        }

        //get sub nav level 3 param
        if (context.getRequest().getParameter("Subnav3Param") == null) {
            Subnav3Param = "0";
        } else {
            Subnav3Param = context.getRequest().getParameter("Subnav3Param");

        }

        //getproduct ID
        if (context.getRequest().getParameter("ID") == null) {
            ID = "0";
        } else {
            ID = context.getRequest().getParameter("ID");

        }


        if (context.getRequest().getParameter("RunQuery") == null) {
            //set default data for now, prob best to do in xsl ***
            RunQuery = "0";

        } else {
            RunQuery = context.getRequest().getParameter("RunQuery");

        }

        //which query tidy up below query


        if (RunQuery.equals("l1")) {

            SQLDynQuery = "SELECT Product, UPC FROM productmetadata WHERE " +
                    "ProductCategory = '" + paramValue.replaceAll("'", "''") + "'" +
                    " AND Country = '" + langValue.replaceAll("'", "''") + "'" +
                    " AND Discontinued = 0 " +
                    " AND WWA < GETDATE()" +
                    "AND UPC is not null " +
                    "ORDER BY NavOrder, Product";
        } else if (RunQuery.equals("l2")) {

            SQLDynQuery = "SELECT Product, UPC FROM productmetadata WHERE " +
                    "ProductCategory = '" + paramValue.replaceAll("'", "''") + "' AND " +
                    "Subnav1Param  = '" + Subnav1Param.replaceAll("'", "''") + "'" +
                    " AND Country = '" + langValue.replaceAll("'", "''") + "'" +
                    " AND Discontinued = 0 " +
                    " AND WWA < GETDATE()" +
                    "AND UPC is not null " +
                    "ORDER BY NavOrder, Product";
        } else if (RunQuery.equals("l3")) {

            SQLDynQuery = "SELECT Product, UPC FROM productmetadata WHERE " +
                    "ProductCategory = '" + paramValue.replaceAll("'", "''") + "' AND " +
                    "Subnav1Param  = '" + Subnav1Param.replaceAll("'", "''") + "' AND " +
                    "Subnav2Param  = '" + Subnav2Param.replaceAll("'", "''") + "'" +
                    " AND Country = '" + langValue.replaceAll("'", "''") + "'" +
                    " AND Discontinued = 0 " +
                    " AND WWA < GETDATE()" +
                    "AND UPC is not null " +
                    "ORDER BY NavOrder, Product";
        } else if (RunQuery.equals("l4")) {

            SQLDynQuery = "SELECT Product, UPC FROM productmetadata WHERE " +
                    "ProductCategory = '" + paramValue.replaceAll("'", "''") + "' AND " +
                    "Subnav1Param  = '" + Subnav1Param.replaceAll("'", "''") + "' AND " +
                    "Subnav2Param  = '" + Subnav2Param.replaceAll("'", "''") + "' AND " +
                    "Subnav3Param  = '" + Subnav3Param.replaceAll("'", "''") + "'" +
                    " AND Country = '" + langValue.replaceAll("'", "''") + "'" +
                    " AND Discontinued = 0 " +
                    " AND WWA < GETDATE()" +
                    "AND UPC is not null " +
                    "ORDER BY NavOrder, Product";
        }







        //get dct data
        String navProductsHeadings = context.getParameterString("Product Headings");
        String navProducts = "/templatedata/" + langValue + navProductsHeadings.substring(18);


        // get the TeamSite workarea vpath from the context
        FileDALIfc fileDal = context.getFileDAL();
        java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + navProducts);


        // gcreate main doc
        Document doc = Dom4jUtils.newDocument("<staticcontent/>");

        //declare doc for return of headings
        Document doc1;

        //add is stream to xml
        try {
            doc1 = Dom4jUtils.newDocument(is);
            doc.getRootElement().appendContent(doc1);

        } catch (Exception e) {
			log.error("Exception", e);
        }

        // get sql data if query is required
        //get database connection reqs
        String pool = context.getParameterString("Pool");
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        //debug data
        doc.getRootElement().addElement("query").addText(SQLDynQuery + " - " + RunQuery);

        if (!RunQuery.equals("0")) {

            // Try to connect to the database
            try {



                con = RequestUtils.getConnection(pool);

                stmt = con.createStatement();
                rs = stmt.executeQuery(SQLDynQuery);


                //get data 
                while (rs.next()) {

                    if (rs.getString(1) == null) {

                    } else {

                        doc.getRootElement().addElement("UPC").addText(rs.getString(2));                                           doc.getRootElement().addElement("ProductRow").addText(rs.getString(1));

                    }

                }


                //close dbase
                rs.close();
                stmt.close();
                con.close();


            } catch (Exception e) {
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
        }




        //return selected parameters
        doc.getRootElement().addElement("sParamValue").addText(paramValue);
        doc.getRootElement().addElement("sSubnav1Param").addText(Subnav1Param);
        doc.getRootElement().addElement("sSubnav2Param").addText(Subnav2Param);
        doc.getRootElement().addElement("sSubnav3Param").addText(Subnav3Param);
        doc.getRootElement().addElement("sID").addText(ID);


        // return the XML to the component
        return doc;


    }
    }
