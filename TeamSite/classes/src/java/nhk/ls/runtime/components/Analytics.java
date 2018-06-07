/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.model.rule.Segment;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import nhk.ls.runtime.common.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.Set;
import org.jsoup.Jsoup;

/**
 *
 * @author sbhojnag
 */
public class Analytics {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.Analytics"));

    public Document setsiteCatalyst(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            outDoc.addElement("LiveSiteAnalytics");
            if (context.isPreview()) {
                mLogger.createLogDebug("AnalyticsInfo.setCoreFunctionality: RUNTIME not detected, doing nothing.");
                return outDoc;
            }
            String segmentName = "";
            /*Set segments = context.getUserSegments();
            if (segments.size() > 0) {
                Segment segment = (Segment) segments.iterator().next();
                segmentName = segment.getName();
            }*/

            String userName = "";
            if (context.getRequest() == null) {

                this.mLogger.createLogDebug("AnalyticsInfo.setCoreFunctionality: No request object detected, doing nothing.");
                return outDoc;
            }
            context.getPageScopeData().put("site", context.getSite());
            String GoogleAnalyticsFile = context.getParameterString("GoogleAnalyticsFile");
            String GoogleAnalyticsText = context.getParameterString("GoogleAnalyticsText");
            String SiteCatalystFile = context.getParameterString("SiteCatalystFile");
            String SiteCatalystText = context.getParameterString("SiteCatalystText");
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
            if (SiteCatalystText != null || !SiteCatalystText.equalsIgnoreCase("") || SiteCatalystFile != null || !SiteCatalystFile.equalsIgnoreCase("")) {
                jsContent = jsContent + "\n\n\n<!--[Site Catalyst]-->";
                if (SiteCatalystFile != null && !SiteCatalystFile.equalsIgnoreCase("")) {
                    jsContent = jsContent + "\n\n<script src=\"" + SiteCatalystFile + "\" type=\"text/javascript\" charset=\"UTF-8\"/>";
                }
                if (SiteCatalystText != null && !SiteCatalystText.equalsIgnoreCase("")) {
                    jsContent = jsContent + "\n\n" + StringEscapeUtils.unescapeHtml(SiteCatalystText).replaceAll("<br />", "\n").replaceAll("<p>", "").replaceAll("</p>", "");
                  //    jsContent = jsContent + "\n\n" + Jsoup.parse(SiteCatalystText).text();
                }
                jsContent = jsContent + "\n<!--[Site Catalyst]-->";
            }
            jsContent = jsContent.replaceAll("\\{\\$sc\\.site\\}", context.getSite().getName());
            jsContent = jsContent.replaceAll("\\{\\$sc\\.server\\}", context.getRequest().getServerName());
            jsContent = jsContent.replaceAll("\\{\\$sc\\.pagename\\}", context.getPageLink(context.getPageName()));

            // COMMENTED THIS LINE TO TEST OUT SITECATALYST

            context.getRequest().setAttribute("AnalyticsBaseJS", jsContent);
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating Analytics Information", e);
        }
        return outDoc;
    }
}
