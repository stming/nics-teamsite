/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.model.rule.Segment;
import com.interwoven.livesite.runtime.model.SiteMap;
import java.io.File;
import java.util.ResourceBundle;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import nhk.ls.runtime.common.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.Set;
import nhk.ls.runtime.common.ProductListingHelper;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author sbhojnag
 */
public class SiteAnalytics {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.SiteAnalytics"));
    private static final String EXT_PARAM_PRODUCT_DETAILS_DCR_PATH = "DCRPath";
    private static final String BUNDLE_NAME = "nhk.ls.runtime.common.DateFormat";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    public static final String EXTERNAL_PARAM_CURRENT_TAB = "currentTab";
    public static final String EXTERNAL_PARAM_CURRENT_LINK = "currentLink";
    public static final String EXTERNAL_PARAM_CATEGORYID = "CategoryID";
    private static final String OVERVIEW_TAB_ID = "1";

    public Document modifySiteCatalystScript(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            outDoc.addElement("LiveSiteAnalytics");
            if (context.isPreview()) {
                mLogger.createLogDebug("AnalyticsInfo.setCoreFunctionality: RUNTIME not detected, doing nothing.");
                return outDoc;
            }
            String segmentName = "";
            Set segments = context.getUserSegments();
            if (segments.size() > 0) {
                Segment segment = (Segment) segments.iterator().next();
                segmentName = segment.getName();
            }

            if (context.getRequest() == null) {

                this.mLogger.createLogDebug("AnalyticsInfo.setCoreFunctionality: No request object detected, doing nothing.");
                return outDoc;
            }
            context.getPageScopeData().put("site", context.getSite());
            String GoogleAnalyticsFile = context.getParameterString("GoogleAnalyticsFile");
            String GoogleAnalyticsText = context.getParameterString("GoogleAnalyticsText");
            String SiteCatalystFile = context.getParameterString("SiteCatalystFile");
            String siteCatalystText = context.getParameterString("SiteCatalystText");

            String jsContent = "";
            if (GoogleAnalyticsText != null || !GoogleAnalyticsText.equalsIgnoreCase("") || GoogleAnalyticsFile != null || !GoogleAnalyticsFile.equalsIgnoreCase("")) {
                jsContent = jsContent + "\n<!--[Google Analytics]-->";
                if (GoogleAnalyticsFile != null && !GoogleAnalyticsFile.equalsIgnoreCase("")) {
                    jsContent = jsContent + "\n\n<script src=\"" + GoogleAnalyticsFile + "\" type=\"text/javascript\" charset=\"UTF-8\"/>";
                }
                if (GoogleAnalyticsText != null && !GoogleAnalyticsText.equalsIgnoreCase("")) {
                    jsContent = jsContent + "\n\n" + StringEscapeUtils.unescapeHtml(GoogleAnalyticsText).replaceAll("<br />", "\n").replaceAll("<p>", "").replaceAll("</p>", "");
                    //  jsContent = jsContent + "\n\n" + Jsoup.parse(GoogleAnalyticsText).text();
                }

                jsContent = jsContent + "\n<!--[Google Analytics]-->";
            }
            if (siteCatalystText != null || !siteCatalystText.equalsIgnoreCase("") || SiteCatalystFile != null || !SiteCatalystFile.equalsIgnoreCase("")) {
                jsContent = jsContent + "\n\n\n<!--[Site Catalyst]-->";
                if (SiteCatalystFile != null && !SiteCatalystFile.equalsIgnoreCase("")) {
                    jsContent = jsContent + "\n\n<script src=\"" + SiteCatalystFile + "\" type=\"text/javascript\" charset=\"UTF-8\"/>";
                }
                if (siteCatalystText != null && !siteCatalystText.equalsIgnoreCase("")) {
                    jsContent = jsContent + "\n\n" + modifyScript(siteCatalystText, context);
                }

                jsContent = jsContent + "\n<!--[Site Catalyst]-->";
            }
            jsContent = replaceAllOOTBStrings(jsContent, context);
            mLogger.createLogDebug("FINAL jsContent=" + jsContent);

            context.getRequest().setAttribute("AnalyticsBaseJS", jsContent);
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating Analytics Information", e);
        }
        return outDoc;
    }

    private String modifyScript(String siteCatalystText, RequestContext context) {

        String modified = StringEscapeUtils.unescapeHtml(siteCatalystText).replaceAll("<br />", "\n").replaceAll("<p>", "").replaceAll("</p>", "");

        String pageName = context.getPageName();
        mLogger.createLogDebug("modifyScript pageName=" + pageName);

        if (pageName.equalsIgnoreCase("product_details")) {

            String dcrFullPath = context.getParameterString(EXT_PARAM_PRODUCT_DETAILS_DCR_PATH, "");
            Document inDoc = readDCRFileIntoXML(context, dcrFullPath);
            if (inDoc != null) {
                String productName = inDoc.selectSingleNode("//ProductName").getText();
                modified = modified.replaceAll("\\$SC_PRODUCT_NAME\\[\\]", productName);
            }
        }

        // Based on the URL string on the page
        if (pageName.equalsIgnoreCase("product_details") || pageName.equalsIgnoreCase("products") || pageName.equalsIgnoreCase("product_archive")) {

            String productLabel = context.getParameterString("ProductLabel", "Products");

            SiteMap siteMap = context.getLiveSiteDal().getSiteMap(context.getSite().getName());
            Document siteMapDoc = siteMap.getDocument();

            String currentLink = context.getParameterString(ProductListing.EXTERNAL_PARAM_CURRENT_LINK, "");
            String currentTab = context.getParameterString(ProductListing.EXTERNAL_PARAM_CURRENT_TAB, "");
            String categoryId = context.getParameterString(ProductListing.EXTERNAL_PARAM_CATEGORYID, "");
            String innermostCatId = categoryId;

            if (StringUtils.isNotEmpty(currentTab) && !currentTab.equalsIgnoreCase(OVERVIEW_TAB_ID)) {
                innermostCatId = currentTab;
            }
            if (StringUtils.isNotEmpty(currentLink)) {
                innermostCatId = currentLink;
            }

            Node tempNode = siteMapDoc.selectSingleNode("//node[@id ='" + innermostCatId + "']");
            mLogger.createLogDebug("innermostCatId=" + innermostCatId);
            String categoryNameTraverser = ProductListingHelper.getVariableLinkForSiteCatalyst(tempNode, productLabel);

            modified = modified.replaceAll("\\$SC_CATEGORY_NAME\\[\\]", categoryNameTraverser);
            modified = modified.replaceAll("\\$SC_HIER_CATEGORY_NAME\\[\\]", categoryNameTraverser.replaceAll(":", "|"));
        }

        if (pageName.equalsIgnoreCase(
                "dealer_details") || pageName.equalsIgnoreCase("distributor_details") || pageName.equalsIgnoreCase("servicecentre_details") || pageName.equalsIgnoreCase("reseller_details")) {
            String dcrFullPath = context.getParameterString("DCRPath", "");
            // Read the title value and replace
            Document inDoc = readDCRFileIntoXML(context, dcrFullPath);
            if (inDoc != null) {
                String title = inDoc.selectSingleNode("//Name").getText(); // From the "dealer" DCR
                modified = modified.replaceAll("\\$SC_DETAILS_PAGE_TITLE\\[\\]", title);
            }
        }
        if (pageName.equalsIgnoreCase(
                "pagearticle") || pageName.equalsIgnoreCase("pagearticle_full") || pageName.equalsIgnoreCase("pagearticle_panel")) {
            String dcrFullPath = context.getParameterString("DCRPath", "");
            // Read the title value and replace
            Document inDoc = readDCRFileIntoXML(context, dcrFullPath);
            if (inDoc != null) {
                String title = inDoc.selectSingleNode("//Title").getText(); // From the "free_format_content" DCR
                modified = modified.replaceAll("\\$SC_DETAILS_PAGE_TITLE\\[\\]", title);
            }
        }
        if (pageName.equalsIgnoreCase(
                "serviceadvisory_details") || pageName.equalsIgnoreCase(
                "highlight_details") || pageName.equalsIgnoreCase("announcement_details")) {
            String dcrFullPath = context.getParameterString("id", "");
            // Read the title value and replace
            Document inDoc = readDCRFileIntoXML(context, dcrFullPath);
            String title = "";
            if (inDoc != null) {
                if (pageName.equalsIgnoreCase("highlight_details")) {
                    // For highlight_details
                    String tabPath = (context != null && context.getParameters() != null) ? (String) context.getParameters().get("WithTabNum") : "";
                    title = inDoc.selectSingleNode("//Tab[Link_Parameter='" + dcrFullPath + "&WithTabNum=" + tabPath + "']/Headline").getText();
                } else {
                    title = inDoc.selectSingleNode("//Headline").getText(); // From the "service_advisory_notices" DCR
                }
                modified = modified.replaceAll("\\$SC_DETAILS_PAGE_TITLE\\[\\]", title);
            }
        }
        if (pageName.equalsIgnoreCase(
                "promotion_details")) {
            String dcrFullPath = context.getParameterString("id", "");
            // Read the title value and replace
            Document inDoc = readDCRFileIntoXML(context, dcrFullPath);
            if (inDoc != null) {
                String title = inDoc.selectSingleNode("//Headline").getText(); // From the "promotions_events" DCR, there is only one <Headline> to be read.
                modified = modified.replaceAll("\\$SC_DETAILS_PAGE_TITLE\\[\\]", title);
            }
        }

        mLogger.createLogDebug("modified script value=" + modified);
        return modified;
    }

    private Document readDCRFileIntoXML(RequestContext context, String dcrFullPath) {
        mLogger.createLogInfo("modifyProductSupportScript invoked with DCR Path: " + dcrFullPath);
        dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;
        mLogger.createLogInfo("Full DCR Path: " + dcrFullPath);
        Document inDoc = null;
        if (StringUtils.isNotEmpty(dcrFullPath)) {
            File dcrFile = new File(dcrFullPath);
            if (dcrFile.exists()) {
                inDoc = Dom4jUtils.newDocument(dcrFile);
            }
        }
        return inDoc;
    }

    private static boolean isElementCompleteBlank(Element element, boolean isBlank) {

        if (element != null) {
            for (int i = 0, size = element.nodeCount(); i < size; i++) {
                Node node = element.node(i);
                if (node instanceof Element) {
                    isBlank = isElementCompleteBlank((Element) node, isBlank);
                } else {
                    String value = element.getText();

                    if (isBlank) {
                        isBlank = StringUtils.isEmpty(value.trim());
                    }
                }
            }
        }
        return isBlank;
    }

    private String replaceAllOOTBStrings(String jsContent, RequestContext context) {

        String completeBranchName = context.getSite().getBranch();
        String branchName = completeBranchName.substring(completeBranchName.lastIndexOf("/") + 1);

        return jsContent.replaceAll("\\$CONTEXT\\{site.branch\\}", branchName).replaceAll("\\$CONTEXT\\{pageName\\}", context.getPageName()).replaceAll("\\$CONTEXT\\{request.serverName\\}", context.getRequest().getServerName()).replaceAll("\\$CONTEXT\\{site.name\\}", context.getSite().getName());
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
