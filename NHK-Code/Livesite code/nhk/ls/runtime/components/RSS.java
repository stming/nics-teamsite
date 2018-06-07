/*
 * To change this template, choose Tools | Templates
 *
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import nhk.ls.runtime.dao.NewsData;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.util.List;
import java.util.Iterator;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Date;
import nhk.ls.runtime.common.Logger;

/**
 *
 * @author sbhojnag
 */
public class RSS {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.RSS"));

    public Document getRSSList(RequestContext context) {

        Document outDoc = null;
        try {

            outDoc = Dom4jUtils.newDocument();
            DataManager dataManager = new DataManagerImplCommon(context);
            String newsCategory = context.getParameterString("newsCategory", "Corporate");
            mLogger.createLogInfo("Parameter String News Category: " + newsCategory);
            List<NewsData> NewsDataList = null;
            if (newsCategory != null) {
                NewsDataList = dataManager.retrieveNewsByCategoryList(newsCategory, context.getSite().getName());
            } else {
                NewsDataList = dataManager.retrieveAllNewsList(context.getSite().getName());
            }
            mLogger.createLogDebug("Before Size");
            mLogger.createLogInfo("News List Size: " + NewsDataList.size());
            Element responseElement = outDoc.addElement("Response");
            if (NewsDataList.size() > 0 || NewsDataList != null) {
                Element rssElement = responseElement.addElement("rss");
                Element channelElement = rssElement.addElement("channel");
                Date currentdate = new Date();
                channelElement.addElement("Date").addText(currentdate.toString());
                String requestURL = null;
                if (context.isRuntime()) {
                    requestURL = new String(context.getRequest().getRequestURL());
                    requestURL = requestURL.substring(0, requestURL.indexOf("/" + context.getSite().getName()));
                } else {
                    requestURL = "";
                }
                channelElement.addElement("RequestURL").addText(requestURL);
                if (newsCategory != null) {
                    context.setPageTitle("Nikon Latest " + newsCategory + " Press Release");
                    channelElement.addElement("newsCategory").addText(newsCategory);
                } else {
                    context.setPageTitle("Nikon Latest Press Release");
                    channelElement.addElement("newsCategory").addText("");
                }
                mLogger.createLogDebug("Page Title: " + context.getPageTitle());
                for (Iterator i = NewsDataList.iterator(); i.hasNext();) {
                    Element itemElement = channelElement.addElement("item");
                    NewsData currentNewsData = (NewsData) i.next();
                    itemElement.addElement("title").addText(currentNewsData.getHeadline());
                    itemElement.addElement("description").addText(currentNewsData.getSubHeadline());
                    itemElement.addElement("link").addText(currentNewsData.getLinkParameter());
                    itemElement.addElement("pubDate").addText(currentNewsData.getDate().toString());
                }
            } else {
                Element rssElement = responseElement.addElement("rss").addText("Empty");
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating RSS Page", e);
        }
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }
}
