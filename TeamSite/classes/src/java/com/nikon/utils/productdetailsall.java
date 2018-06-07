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
public class productdetailsall {

	private Log log = LogFactory.getLog(productdetailsall.class);
	
    /** default constructor */
    public productdetailsall() {


    }

    public Document getproductdetailsall(RequestContext context) throws DocumentException {


        // gcreate main doc
        Document doc = Dom4jUtils.newDocument("<staticcontent/>");

        // get query string parameter named "lang" from context
        String ID = "";
        String langValue = "";
        String SQLDynQuery = "";
        String vPath = "";




        Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langValue = tcookie.getValue().toString();
        } else {
            langValue = "de_CH";
        }

        //get parameters
        if (context.getRequest().getParameter("ID") == null) {
            //set default data for now, prob best to do in xsl ***

            ID = "NA";

        } else {
            ID = context.getRequest().getParameter("ID");

        }


        SQLDynQuery = "SELECT Path FROM productmetadata WHERE " +
                "UPC = '" + ID.replaceAll("'", "''") + "'" +
                "AND Country = '" + langValue + "'";


        // get sql data if query is required
        //get database connection reqs
        String pool = context.getParameterString("Pool");
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;




        // Try to connect to the database
        try {



            con = RequestUtils.getConnection(pool);

            stmt = con.createStatement();
            rs = stmt.executeQuery(SQLDynQuery);


            //get data 
            while (rs.next()) {

                if (rs.getString(1) == null) {

                } else {

                    vPath = rs.getString(1);
                }

            }


            //close dbase
        //attach doc



        } catch (Exception e) {
            //debug output
            doc.getRootElement().addElement("error").addText("CONNECTION ERROR");
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

        // get the TeamSite workarea vpath from the context


        if (!vPath.equals("")) {

            FileDALIfc fileDal = context.getFileDAL();
            java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + "\\" +  vPath);




            //declare doc for return of headings
            Document doc1;

            //add is stream to xml
            try {
                doc1 = Dom4jUtils.newDocument(is);
                doc.getRootElement().appendContent(doc1);

            } catch (Exception e) {
            	log.error("Exception", e);

            }

        } else {
            doc.getRootElement().addElement("error").addText("Data cannot be found");
        }


        return doc;
    }
}
