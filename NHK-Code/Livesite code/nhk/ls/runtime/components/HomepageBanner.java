/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.common.io.FileUtil;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;
import java.util.Iterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;

import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.common.StringTrimUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author wxiaoxi
 */
public class HomepageBanner {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.HomepageBanner"));
    private static final String BUNDLE_NAME = "nhk.ls.runtime.common.DateFormat";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public org.dom4j.Document createHomepageBannerResult(RequestContext context) {

        Document outDoc = null;
        Document inDoc = null;
        outDoc = Dom4jUtils.newDocument();
        Element resultElement = outDoc.addElement("images");

        mLogger.createLogInfo("Homepage Banner video started");

        try {
            String dcrFullPath = null;
            dcrFullPath = (context != null && context.getParameters() != null) ? (String) context.getParameters().get("DCRPath") : "";
            mLogger.createLogDebug("DCR Path: " + dcrFullPath);

            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;
            mLogger.createLogInfo("Full DCR Path: " + dcrFullPath);

            File dcrFile = new File(dcrFullPath);
            if (dcrFile.exists()) {
                inDoc = Dom4jUtils.newDocument(dcrFile);
                String xPath = "//Homepage_header_banner/Banner";
                List<Node> nodes = inDoc.selectNodes(xPath);
                for (Iterator i = nodes.iterator(); i.hasNext();) {
                    Node currentNode = (Node) i.next();
                    Element bannerEle = (Element) currentNode;
                    String StartDate = bannerEle.element("StartDate").getText();
                    String EndDate = bannerEle.element("EndDate").getText();
                    if (dateCheck(StartDate, EndDate)) {
                        Element imgEle = resultElement.addElement("IMAGE");
                        imgEle.addAttribute("URL", bannerEle.element("BannerPath").getText());
                        imgEle.addAttribute("THUMB", bannerEle.element("BannerIconPath").getText());
                        imgEle.addAttribute("LINK", bannerEle.element("BannerLink").getText());
                        if (bannerEle.element("Target").getText().equals("New Window")) {
                            imgEle.addAttribute("target", "_blank");
                        } else if (bannerEle.element("Target").getText().equals("Same Window")) {
                            imgEle.addAttribute("target", "_self");
                        } else if (bannerEle.element("Target").getText().equals("Shadow Box")) {
                            imgEle.addAttribute("target", "CBOX");
                            imgEle.addAttribute("CBHEIGHT", bannerEle.element("Height").getText());
                            imgEle.addAttribute("CBWIDTH", bannerEle.element("Width").getText());
                        }

                        imgEle.addAttribute("TIME", bannerEle.element("TransitionTime").getText());
                    }
                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating from DCT", e);
        }
        mLogger.createLogDebug("HomepageBanner:" + outDoc.asXML());

        String outputFileName = null;
        outputFileName = (context != null && context.getParameters() != null && context.getParameters().get("BannerSoucePath") != null) ? (String) context.getParameters().get("BannerSoucePath") : "";
        mLogger.createLogDebug("outputXML Path:" + outputFileName);
        outputFileName = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + outputFileName;

        mLogger.createLogInfo("outputXML Path:" + outputFileName);

        File outFile = new File(outputFileName);
        try {
            FileUtil.writeText(outFile, outDoc.asXML());
        } catch (Exception e) {
            mLogger.createLogWarn("HomepageBanner error creating file:" + outputFileName, e);
        }
        return outDoc;
    }

    //for Homepage Flash
    public org.dom4j.Document createHomepageFlashResult(RequestContext context) {
        Document outDoc = null;
        Document inDoc = null;
        outDoc = Dom4jUtils.newDocument();
        Element resultElement = outDoc.addElement("Result");

        mLogger.createLogInfo("Homepage flash video started");

        try {
            String dcrFullPath = null;
            dcrFullPath = (context != null && context.getParameters() != null) ? (String) context.getParameters().get("DCRPathForFLV") : "";

            mLogger.createLogInfo("DCRPath For FLV: " + dcrFullPath);
            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;
            mLogger.createLogInfo("Full DCR Path For FLV: " + dcrFullPath);
            File dcrFile = new File(dcrFullPath);
            if (dcrFile.exists() && dcrFile.isFile()) {
                inDoc = Dom4jUtils.newDocument(dcrFile);
                Element homepageBannerElement = resultElement.addElement("Homepage_flash_video");

                //add transition time
                //add flashes
                String xPath = "//Homepage_flash_video/Flash";
                List<Node> nodes = inDoc.selectNodes(xPath);
                for (Iterator i = nodes.iterator(); i.hasNext();) {
                    Node currentNode = (Node) i.next();
                    Element bannerEle = (Element) currentNode;
                    String StartDate = bannerEle.element("StartDate").getText();
                    String EndDate = bannerEle.element("EndDate").getText();
                    if (dateCheck(StartDate, EndDate)) {
                        homepageBannerElement.add(currentNode.detach());
                    }
                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating from DCT", e);
        }
        mLogger.createLogDebug("Homepage flash video:" + outDoc.asXML());
        return outDoc;
    }

    public org.dom4j.Document createHomepageHighlightsResult(RequestContext context) {
        Document outDoc = null;
        Document inDoc = null;
        outDoc = Dom4jUtils.newDocument();
        Element resultElement = outDoc.addElement("Result");

        String homepageHighlightsText = context.getParameterString("HomepageHighlightsText");

        mLogger.createLogInfo("Homepage Highlights started");

        try {
            String dcrFullPath = null;
            dcrFullPath = (context != null && context.getParameters() != null) ? (String) context.getParameters().get("DCRPathForHighlights") : "";

            mLogger.createLogDebug("DCR Path: " + dcrFullPath);
            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;
            mLogger.createLogInfo("Full DCR Path: " + dcrFullPath);
            String HighlightsPerPage = (context != null && context.getParameters() != null && context.getParameters().get("Highlights_per_page") != null) ? (String) context.getParameters().get("Highlights_per_page") : "4";
            Element HighlightsPerPageElement = resultElement.addElement("Highlights_per_page");
            HighlightsPerPageElement.addText(HighlightsPerPage);
            int intHighlightsPerPage = Integer.valueOf(HighlightsPerPage);

            File dcrFile = new File(dcrFullPath);
            if (dcrFile.exists()) {

                inDoc = Dom4jUtils.newDocument(dcrFile);

                Element homepageBannerElement = resultElement.addElement("homepage_hightlights");

                // Reading the sitecatalyst script
                String modified = "";
                if (homepageHighlightsText != null && !homepageHighlightsText.equalsIgnoreCase("")) {
                    modified = StringEscapeUtils.unescapeHtml(homepageHighlightsText).replaceAll("<br />", "\n").replaceAll("<p>", "").replaceAll("</p>", "");
                }

                //add flashes
                String xPath = "//homepage_hightlights/Tab";
                List<Node> nodes = inDoc.selectNodes(xPath);
                int highlightsIndex = -1;
                for (Iterator i = nodes.iterator(); i.hasNext();) {

                    String siteCatalystScriplet = modified;
                    Node currentNode = (Node) i.next();
                    Element bannerEle = (Element) currentNode;
                    String StartDate = bannerEle.element("StartDate").getText();
                    String EndDate = bannerEle.element("EndDate").getText();

                    if (bannerEle.element("Details") != null) {
                        bannerEle.element("Details").setText("");
                    }
                    String outputString = StringTrimUtils.retrieveSubstring(bannerEle.element("Subheadline").getText(), 100, "...");
                    bannerEle.element("Subheadline").setText(outputString);

                    if (dateCheck(StartDate, EndDate)) {

                        // Adding the sitecatalyst onlick data into the node
                        if (StringUtils.isNotEmpty(siteCatalystScriplet)) {
                            String title = bannerEle.element("Headline").getText();
                            siteCatalystScriplet = siteCatalystScriplet.replaceAll("\\$SC_HOMEPAGE_HIGHLIGHT_TITLE\\[\\]", title);
                            ((Element) currentNode).addElement("onClickData").setText(siteCatalystScriplet);
                        }

                        highlightsIndex = highlightsIndex + 1;
                        Element highlightsElement = homepageBannerElement.addElement("highlights_" + (highlightsIndex / intHighlightsPerPage + 1));
                        highlightsElement.add(currentNode.detach());
                    }
                }
                //add HighlightsNumber
                Element countElement = resultElement.addElement("HighlightsNumber");
                countElement.addText(String.valueOf(highlightsIndex + 1));
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating from DCT", e);
        }
        mLogger.createLogDebug("Homepage flash video:" + outDoc.asXML());
        return outDoc;
    }

    public org.dom4j.Document createHomepageFreeFormatWithTabsResult(RequestContext context) {
        Document outDoc = null;
        Document inDoc = null;
        outDoc = Dom4jUtils.newDocument();
        Element resultElement = outDoc.addElement("Result");
        mLogger.createLogInfo("Homepage Free Format with Tabs started");
        try {
            String dcrFullPath = null;
            dcrFullPath = (context != null && context.getParameters() != null && context.getParameters().get("DCRPath") != null) ? (String) context.getParameters().get("DCRPath") : "";

            mLogger.createLogDebug("DCR Path: " + dcrFullPath);
            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;
            mLogger.createLogInfo("Full DCR Path: " + dcrFullPath);

            String targetString = (context != null && context.getParameters() != null && context.getParameters().get("DCRPath") != null) ? (String) context.getParameters().get("Link_Target") : "";
            Element countElement = resultElement.addElement("Target");
            countElement.addText(targetString);

            if (!targetString.equals("") && !targetString.equals("NoChange")) {
                targetString = "target = '" + targetString + "' ";
            } else {
                targetString = "";
            }

            File dcrFile = new File(dcrFullPath);
            if (dcrFile.exists()) {
                inDoc = Dom4jUtils.newDocument(dcrFile);

                Element homepageBannerElement = resultElement.addElement("homepage_free_format");

                //add flashes
                String xPath = "//homepage_free_format/text_tab";
                List<Node> nodes = inDoc.selectNodes(xPath);
                for (Iterator i = nodes.iterator(); i.hasNext();) {
                    Node currentNode = (Node) i.next();
                    Element tabEle = (Element) currentNode;
                    String TitleText = tabEle.element("Title").getText();
                    String ContentText = tabEle.element("Content").getText();
                    ContentText = ContentText.replaceAll("href", targetString + "href");
                    tabEle.element("Content").setText(ContentText);

                    homepageBannerElement.add(currentNode.detach());

                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating from DCT", e);
        }
        mLogger.createLogDebug("Homepage flash video:" + outDoc.asXML());
        return outDoc;
    }

    private boolean dateCheck_bk(String StartDate, String EndDate) {
        mLogger.createLogDebug("Current Date Format:" + getPropertyValue("DATE_FORMAT_DCT3"));

        DateFormat df = new SimpleDateFormat(getPropertyValue("DATE_FORMAT_DCT3"));
        Date today = Calendar.getInstance().getTime();
        String currentDate = df.format(today);

        String[] ss = StartDate.split("-");
        StartDate = ss[2] + ss[1] + ss[0];
        ss = EndDate.split("-");
        EndDate = ss[2] + ss[1] + ss[0];

        int i1 = currentDate.compareTo(StartDate);
        int i2 = currentDate.compareTo(EndDate);
        return ((i1 > 0) && (i2 < 0));
    }

    private boolean dateCheck(String StartDate, String EndDate) {
        mLogger.createLogDebug("Current Date Format:" + getPropertyValue("DATE_FORMAT_DCT3"));

        DateFormat df = new SimpleDateFormat(getPropertyValue("DATE_FORMAT_DCT3"));
        Date today = Calendar.getInstance().getTime();

        Date start = null;
        Date end = null;
        try {
            start = df.parse(StartDate);
            end = df.parse(EndDate);
            // Thu Jan 18 00:00:00 CST 2007
        } catch (Exception e) {
            mLogger.createLogWarn("Exception in dateCheck::", e);
        }
        return (start.before(today) && end.after(today));
    }

    public String getPropertyValue(String key) {
        try {
            String tmp = RESOURCE_BUNDLE.getString(key);
            if (!StringUtils.isBlank(tmp)) {
                return tmp;
            } else {
                throw new Exception("Key[" + key + "] does not exist in property file " + BUNDLE_NAME);
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in getPropertyValue::", e);
            return "";
        }
    }
}
