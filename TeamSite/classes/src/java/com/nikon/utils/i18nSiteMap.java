package com.nikon.utils;

import com.interwoven.livesite.common.codec.URLUTF8Codec;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.file.FileDALIfc;
import com.interwoven.livesite.model.EndUserSite;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.external.impl.LivesiteSiteMap;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.*;
import java.io.File;
import javax.servlet.http.Cookie;

public class i18nSiteMap extends LivesiteSiteMap {

    public i18nSiteMap() {
    }

    public Document getSiteMap(RequestContext context) {
        // get the TeamSite workarea vpath from the context
        // FileDALIfc fileDal = context.getUser().getFileDAL();
        FileDALIfc fileDal = context.getFileDAL();

        // get the site name from the context
        EndUserSite site = context.getSite();

        // create a path from TeamSite workarea vpath and site name and append default.sitemap
        String rootPath = site.getPath() + "/" + "default.sitemap";

        // load the sitemap xml into the document to be returned
        java.io.InputStream is = fileDal.getStream(rootPath);

        Document doc = null;
        try {
            doc = Dom4jUtils.newDocument(is);
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred creating the sitemap document.", e);
        }

        // segmentation functionality
        // findSegmentXml(doc, context);

        // translate the SiteMap document
        // only translate if the url parameter is neither en or null
        // needs better logic and a list of allowed language parameters
        translateSiteMap(doc, context);

        // UTF8 encode query string data
        // urlEncodeQueryValues(doc);
        if (mLogger.isDebugEnabled()) {
            String xml = doc.asXML();
            mLogger.debug(xml);
        }
        return doc;
    }

    public void translateSiteMap(Document doc, RequestContext context) {
        // get the "propertiesPath" parameter from the context, this is for ease of development at this stage
        // String propertiesPath = context.getParameterString("propertiesPath");

        // get query string parameter named "lang" from context
        String queryValue = "";

        //if queryValue is null  set default to en
        //if (queryValue == null) {
        //    queryValue = "en";
        //}
        //if queryValue is blank set default to en
        //if (queryValue.equals("")) {
        //   queryValue = "en";
        //}


        //get lang from session
    
        //get lang from session
     Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            queryValue = tcookie.getValue().toString();
        }
        else
        {
         queryValue = "en_Asia";
        }



        // create propertiesPath from lang url parameter
        String propertiesPath = "/common_" + queryValue + ".properties";


        // get the TeamSite workarea vpath from the context
        // FileDALIfc fileDal = context.getUser().getFileDAL();
        FileDALIfc fileDal = context.getFileDAL();

        //check file exists too if not then set to en - should we tell admin **
        File langFile = new File(fileDal.getRoot() + propertiesPath);
        boolean exists = langFile.exists();
        if (!exists) {
        //If no translated properties file found then do not try and use translated values
		return;

    	//  propertiesPath = "/common_en.properties";
     	
        }


        // Create path from TeamSite workarea vpath and "propertiesPath", this is for ease of development at this stage
        java.io.InputStream is = fileDal.getStream(fileDal.getRoot() + propertiesPath);


        // create and load language properties
        Properties langProps = new Properties();

        try {
            langProps.load(is);
            is.close();
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred creating the translated sitemap document.", e);
        }

        // Select all the nodes in the sitemap, this is for ease of development at this stage
        List nodeList = doc.selectNodes("//node");

        // Iterate through the selected nodes
        for (Iterator iter = nodeList.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            List elements = element.elements();
            Iterator iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element childElement = (Element) iterator.next();

                // select node called "label"
                if (childElement.getName().equals("label")) {
                    // select the value of the node
                    String keyString = childElement.getText();

                    System.out.println("keystring is --> ***" + keyString +"***");

                    // check the langProps index contains the key
                    if (langProps.containsKey(keyString)) {
                        // cross reference the attribute key with the langProps index
                        String propertyValue = langProps.getProperty(keyString);

                        // replace the existing key with the key from the langProps index
                        childElement.setText(propertyValue);
                    }
                }
            }
        }

    }

    private void urlEncodeQueryValues(Document doc) {
        List nodeList = doc.selectNodes("//query-string/parameter");
        for (Iterator iter = nodeList.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            List elements = element.elements();
            Iterator iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element childElement = (Element) iterator.next();
                if (childElement.getName().equals("name") || childElement.getName().equals("value")) {
                    String originalValue = childElement.getText();
                    childElement.setText(URLUTF8Codec.encodeString(originalValue));
                }
            }
        }

    }
    protected final Log mLogger = LogFactory.getLog(getClass());
}

