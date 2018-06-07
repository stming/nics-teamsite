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
 /* a test comment to see if build date changes
 */
public class navCommunity {

    /** default constructor */
    public navCommunity() {


    }

    public Document constructNavGeneric(RequestContext context) throws DocumentException {



        Document doc = Dom4jUtils.newDocument("<staticcontent/>");


        // get query string parameter named "lang" from context
        String langValue = null;


        Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langValue = tcookie.getValue().toString();
        } else {
            langValue = "en_CH";
        }


        //see if link need to be highlighted
        String linkID = "";
        String linkIDSub1 = "";

        //get linkID
        {
            if (context.getRequest().getParameter("lid") == null) {
                linkID = "0";

                //if session use that
                if (context.getSession().getAttribute("linkID") != null) {
                    linkID = context.getSession().getAttribute("linkID").toString();
                }


            } else {
                linkID = context.getRequest().getParameter("lid");
                context.getSession().setAttribute("linkID", linkID);

            }
        }
        //get linkIDSub1
        {
            if (context.getRequest().getParameter("lidsub1") == null) {
                linkIDSub1 = "0";


                //if session use that
                if (context.getSession().getAttribute("lidsub1") != null) {
                    linkIDSub1 = context.getSession().getAttribute("lidsub1").toString();
                }



            } else {
                linkIDSub1 = context.getRequest().getParameter("lidsub1");
                context.getSession().setAttribute("linkIDSub1", linkIDSub1);

            }
        }




        //append link to doc
        doc.getRootElement().addElement("LinkID").addText(linkID);
        doc.getRootElement().addElement("LinkIDSub1").addText(linkIDSub1);


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
