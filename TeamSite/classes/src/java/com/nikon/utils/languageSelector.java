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
import javax.servlet.*;
import javax.servlet.http.Cookie;
import org.dom4j.Document;
import org.dom4j.DocumentException;

/**
 *
 * @author epayne
 */
public class languageSelector {

    public languageSelector() {
    }

    public Document getlanguage(RequestContext context) throws DocumentException {


        //set up new documents
        Document doc = Dom4jUtils.newDocument("<staticcontent/>");
        Document doc1 = Dom4jUtils.newDocument();
        //get language and add dcr to xml
        //String navLanguages = "/templatedata/de_CH/languages/data/languages";

//        FileDALIfc fileDal = context.getFileDAL();
  //      java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + navLanguages);


//        try {
  //          doc1 = Dom4jUtils.newDocument(is);

//        } catch (Exception e) {

//        }

        //------------sequence is as follows--------------
        //1 If user has previosuly created cookie use that
        //2 If user has a session set use that
        //3 If user has a browser setting use that
        //4 Otherwise check branch to find out language
        //5 If user uses form, this overides all above and sets cookie and session
        //NB session will be used in site not cookie





        //append doc for user drop down
        doc.getRootElement().appendContent(doc1);

        String userLang = "";

        if (context.getRequest().getParameter("selectLang") != null) {
            userLang = context.getRequest().getParameter("selectLang");
            //set cookie and session
            context.getSession().setAttribute("langSession", userLang);

            Cookie langCookie = new Cookie("langCookie", userLang);
            langCookie.setMaxAge(20736000);
            context.getResponse().addCookie(langCookie);


            //  try {
            //   context.getResponse().sendRedirect(myURL);}
            //   catch (Exception e) {  }



            //for demo send user on 
            String myURL = context.getParameterString("myURL");
           // try {
           //     context.getResponse().sendRedirect(myURL);
          //  } catch (Exception e) {
          //  }



 doc.getRootElement().addElement("sessionexist").addText("session is - " + context.getSession().getAttribute("langSession").toString());
     
 
 
  Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            doc.getRootElement().addElement("cookieexist").addText("cookie is - " + tcookie.getValue());
        }

 
 //debug
        // doc.getRootElement().addElement("ddselected").addText(context.getRequest().getParameter("selectLang"));
        }
        //   } else





        return doc;
    }
}
