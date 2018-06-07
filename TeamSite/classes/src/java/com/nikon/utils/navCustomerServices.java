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

/**
 *
 * @author epayne
 */
public class navCustomerServices {

    /** default constructor */
    public navCustomerServices() {


    }

    public Document constructCustomerServices(RequestContext context) throws DocumentException {



        Document doc = Dom4jUtils.newDocument("<staticcontent/>");


        // get query string parameter named "lang" from context
        String langValue = null;


        Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langValue = tcookie.getValue().toString();
        } else {
            langValue = "de_CH";
        }


        //see if link need to be highlighted
        String linkIDCS = "";
        String linkIDCSSub1 = "";

        //get linkIDCS
        {
            if (context.getRequest().getParameter("lidCS") == null) {
                linkIDCS = "0";

                //if session use that
                if (context.getSession().getAttribute("linkIDCS") != null) {
                    linkIDCS = context.getSession().getAttribute("linkIDCS").toString();
                }


            } else {
                linkIDCS = context.getRequest().getParameter("lidCS");
                context.getSession().setAttribute("linkIDCS", linkIDCS);

            }
        }
        //get linkIDCSSub1
        {
            if (context.getRequest().getParameter("lidCSsub1") == null) {
                linkIDCSSub1 = "0";


                //if session use that
                if (context.getSession().getAttribute("lidCSsub1") != null) {
                    linkIDCSSub1 = context.getSession().getAttribute("lidCSsub1").toString();
                }



            } else {
                linkIDCSSub1 = context.getRequest().getParameter("lidCSsub1");
                context.getSession().setAttribute("linkIDCSSub1", linkIDCSSub1);

            }
        }




        //append link to doc
        doc.getRootElement().addElement("LinkIDCS").addText(linkIDCS);
        doc.getRootElement().addElement("LinkIDCSSub1").addText(linkIDCSSub1);


        //get dcr data
        String navHeadings = context.getParameterString("Headings");
        navHeadings = "/templatedata/" + langValue + navHeadings.substring(18);


        // get the TeamSite workarea vpath from the context
        FileDALIfc fileDal = context.getFileDAL();
        java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + navHeadings);



        //declare doc for return of headings
        Document doc1;

        //add is stream to xml
        try {
            doc1 = Dom4jUtils.newDocument(is);
            doc.getRootElement().appendContent(doc1);

        } catch (Exception e) {

        }




        // return the XML to the component
        return doc;


    }
    }
