package com.nikon.externals;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.components.ComponentHelper;

/**
 * Created by Paul W Russell on 4/08/2014.
 */
public class UpdatePageData {

    private Log log = LogFactory.getLog(UpdatePageData.class);

    ComponentHelper ch = new ComponentHelper();

    @SuppressWarnings("unchecked")
    public Document setPageTitle(RequestContext context) {

        log.debug("entering setPageTitle");

        // get the DCR and inject it into the returned XML
        Document doc = Dom4jUtils.newDocument("<staticcontent/>");

        try {
            // Current Page URL
            String currentPageURL = context.getRequest().getRequestURI();
            log.debug("currentPageURL : " + currentPageURL);
            currentPageURL = currentPageURL.replaceAll("^/iw-preview(.*)$", "$1");
            log.debug("new currentPageURL : " + currentPageURL);

            // If A Home Page
            if ((currentPageURL.contains("home.page")) && (!currentPageURL.matches(".*(_N1|_N1/).*"))) {

                String langValue = ComponentHelper.getCookieValue(context, NikonDomainConstants.CKIE_LANG_CODE);
                String countryValue = langValue;
                countryValue = countryValue.replaceAll(".*_(.*)", "$1");
                log.debug("langValue: " + langValue);
                log.debug("countryValue: " + countryValue);

                FileDal fileDal = context.getFileDal();
                boolean titleFallback = false;

                // Initialize The Breadcrumb Array
                ArrayList<String> breadCrumb = new ArrayList<String>();

                String dcrPath = fileDal.getRoot() + "/templatedata/" + langValue + "/page_titles/data/page_titles";
                String fallbackDCRPath = fileDal.getRoot() + "/templatedata/en_Asia/page_titles/data/page_titles";

                if (context.isPreview()) {
                    fallbackDCRPath = fallbackDCRPath.replaceAll("^(.*)/" + countryValue + "/(.*)$", "$1/en_Asia/$2");
                    log.debug("Fallback DCR: " + fallbackDCRPath);
                }

                java.io.InputStream is = null;
                Document localisedTitles = null;
                try {

                    if (fileDal.exists(dcrPath)) {
                        // Get Localised Page Title DCR
                        log.debug("getting localised page title DCR : " + dcrPath);
                        is = fileDal.getStream(dcrPath);
                    } else {
                        is = fileDal.getStream(fallbackDCRPath);
                        titleFallback = true;
                    }

                    localisedTitles = Dom4jUtils.newDocument(is);
                } catch (Exception e) {
                    log.error("Error: " + e.toString());
                }

                // Get NSO Site Title
                String nsoSiteTitle = localisedTitles.selectSingleNode("/page_titles/nso_title").getText();
                log.debug("NSO Site Title: " + nsoSiteTitle);
                breadCrumb.add(nsoSiteTitle);

                // Get All Titles
                List<Node> pageTitleList = localisedTitles.selectNodes("/page_titles/titles");


                // Loop Through Titles
                for (Node pageNode : pageTitleList) {
                    Node pageURLNode = pageNode.selectSingleNode("page");
                    String pageURL = pageURLNode.getText();

                    if (titleFallback) {
                        pageURL = pageURL.replaceAll("en_Asia", langValue);
                    }

                    // If We Match On Current URL
                    log.debug("Loop Page URL: " + pageURL);
                    log.debug("Current Page URL: " + currentPageURL);
                    if (pageURL.equals(currentPageURL)) {

                        Node pageTitleNode = pageNode.selectSingleNode("title");
                        String localisedPageTitle = pageTitleNode.getText();

                        log.debug("Match Page Title: " + localisedPageTitle);

                        breadCrumb.add(localisedPageTitle);
                        break;
                    }
                }
                log.debug("finished looping titles");

                String pageTitle = "";

                // Output Breadcrumb
                for (String bcPart : breadCrumb) {
                    if (!pageTitle.equals("")) {
                        pageTitle += " - ";
                    }
                    pageTitle += bcPart;
                }

                // output this for debug
                log.debug("output this for debug : " + pageTitle);
                doc.getRootElement().addElement("pageTitle").addText(pageTitle);

                // now set the title
                log.debug("setting pagescope");
                context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, pageTitle);
            }
        } catch (Exception e) {
            log.error(e);
        }

        // return the XML to the component
        return doc;
    }
}
