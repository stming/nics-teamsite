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


public class nav_news extends SQLCSNikon {

    /** default constructor */
    public nav_news() {


    }

    public Document please(RequestContext context) throws DocumentException {



        // get query string parameter named "lang" from context
        String langValue = null;
        
            //get lang from session
     Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langValue = tcookie.getValue().toString();
        }
        else
        {
         langValue = "de_CH";
        }

     	ParameterHash params = context.getParameters();
    	params.put( "Locale",  langValue );
     
        
        {


            //context.getRequest().getParameter("lang").equals("") ||


          //  if (context.getRequest().getParameter("lang") == null) {
            //    langValue = "de_CH";
           // } else {
             //   langValue = context.getRequest().getParameter("lang");

            //}


            // get the TeamSite workarea vpath from the context
            FileDALIfc fileDal = context.getFileDAL();


//what if dcr does not exist? ***

            //get news quarters datum for lang
            String navQuartersDatum = "";
            navQuartersDatum = context.getParameterString("News Quarters");
            String navQuartersDatumLang = "/templatedata/" + langValue + navQuartersDatum.substring(18);

            //get news articles
            String navNewsdatum = "";
            navNewsdatum = context.getParameterString("News Articles");
            String navNewsDatumLang = "/templatedata/" + langValue + navNewsdatum.substring(18);


            //get dcrs
            java.io.InputStream is2 = fileDal.getStream(fileDal.getRoot() + navQuartersDatumLang);
            java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + navNewsDatumLang);



            // get the DCR and inject it into the returned XML
            Document doc = Dom4jUtils.newDocument("<staticcontent/>");
            Document doc1;
            Document doc2;


            try {
                doc1 = Dom4jUtils.newDocument(is);
                doc.getRootElement().appendContent(doc1);

            } catch (Exception e) {

            }

            try {
                //doc = Dom4jUtils.newDocument(is2);

                doc2 = Dom4jUtils.newDocument(is2);
                doc.getRootElement().appendContent(doc2);

            } catch (Exception e) {

            }


            //get sqlcsnikon output
            doc.getRootElement().appendContent(get(context));


            //debug
            //doc.getRootElement().addElement("outputhere").addText(navQuartersDatumLang); 


            // return the XML to the component
            return doc;
        }

    }
}
