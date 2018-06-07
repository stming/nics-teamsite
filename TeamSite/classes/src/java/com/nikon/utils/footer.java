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



import java.util.*;
import java.text.*;

/**
 *
 * @author epayne
 */
public class footer {

    public footer() {
    }

    public Document getfooter(RequestContext context) throws DocumentException {

        String langValue = "";

        //for demo get language of cookie and change variable to same in all code

        //get lang from cookie for now
        Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langValue = tcookie.getValue().toString();
        } else {
            langValue = "des_CH";
        }

        // get the TeamSite workarea vpath from the context
        FileDALIfc fileDal = context.getFileDAL();

        //get leaderboard data
        String leaderData = "";
        leaderData = context.getParameterString("footer");
        String leaderDataLang = "/templatedata/" + langValue + leaderData.substring(18);


        java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + leaderDataLang);

        // get the DCR and inject it into the returned XML
        Document doc = Dom4jUtils.newDocument("<staticcontent/>");
        Document doc1;


        try {
            doc1 = Dom4jUtils.newDocument(is);
            doc.getRootElement().appendContent(doc1);

        } catch (Exception e) {

        }

        // Get current date object
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        String DATE_FORMAT = "yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        String currentTime = sdf.format(cal.getTime());


        doc.getRootElement().addElement("CurrYear").addText(currentTime);


        // return the XML to the component
        return doc;
    }
}
