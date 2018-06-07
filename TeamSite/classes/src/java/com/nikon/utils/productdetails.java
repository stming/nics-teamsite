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
public class productdetails {
    
     public productdetails() {
    }


      public Document getproductdetails(RequestContext context) throws DocumentException {
          
               String langValue = "";

        //for demo get language of cookie and change variable to same in all code

        //get lang from cookie for now
        Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langValue = tcookie.getValue().toString();
        } else {
            langValue = "de_CH";
        }

        // get the TeamSite workarea vpath from the context
        FileDALIfc fileDal = context.getFileDAL();

        //get leaderboard data

        String getData = "";
        String getDataSec = "";
        
        //get first dcr for product detail
        getData = context.getParameterString("getData");
        getData = "/templatedata/" + langValue + getData.substring(18);


        java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + getData);

        // get the DCR and inject it into the returned XML
        Document doc = Dom4jUtils.newDocument("<staticcontent/>");
        Document doc1;


        try {
            doc1 = Dom4jUtils.newDocument(is);
            doc.getRootElement().appendContent(doc1);

        } catch (Exception e) {

        }
        
        
        //get second dcr for product nav
        getDataSec = context.getParameterString("getDataSec");
        getDataSec = "/templatedata/" + langValue + getDataSec.substring(18);


        java.io.InputStream is2 = fileDal.getStream(fileDal.getRoot() + getDataSec);

        // get the DCR and inject it into the returned XML
        Document doc2;


        try {
            doc2 = Dom4jUtils.newDocument(is2);
            doc.getRootElement().appendContent(doc2);

        } catch (Exception e) {

        }

    
        // return the XML to the component
        return doc;
      }
     
}