/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.dao.NewsData;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import nhk.ls.runtime.dao.DAOException;
import nhk.ls.runtime.common.StringTrimUtils;

/**
 *
 * @author wxiaoxi
 */
public class HomepageNews {

    private static final String NEWS_PER_TAB = "NewsPerTab";
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.HomepageNews"));

    public Document getLatestNewsList(RequestContext context) {
        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("Result");
            // getting news listing
            List<NewsData> NewsDataList = retrieveSortedNewsListDisplayedByHompage(context);

            String newsPerTab = (context != null && context.getParameters() != null && context.getParameters().get(NEWS_PER_TAB) != null) ? (String) context.getParameters().get(NEWS_PER_TAB) : "4";
            Element newsPerTabElement = resultElement.addElement(NEWS_PER_TAB);
            newsPerTabElement.addText(newsPerTab);
            int intValueNewsPerTab = Integer.valueOf(newsPerTab);

            if (NewsDataList.size() > 0) {
                Element countElement = resultElement.addElement("NewsNumber");
                countElement.addText(String.valueOf(NewsDataList.size()));

                int newsIndex = -1;
                for (Iterator i = NewsDataList.iterator(); i.hasNext();) {
                    newsIndex = newsIndex + 1;
                    NewsData currentNewsData = (NewsData) i.next();
                    Element newsElement = resultElement.addElement("news_" + (newsIndex / intValueNewsPerTab + 1));
                    newsElement.addElement("Headline").addText(currentNewsData.getHeadline());
                    if (currentNewsData.getSubHeadline() != null && currentNewsData.getSubHeadline().length() != 0) {
                        String outputString = StringTrimUtils.retrieveSubstring(currentNewsData.getSubHeadline(), 50, "...");
                        newsElement.addElement("subHeadline").addText(outputString);
                    }
                    //newsElement.addElement("subHeadline").addText(currentNewsData.getsubHeadline());
                    if (currentNewsData.getImagePath() != null && currentNewsData.getImagePath().length() != 0) {
                        newsElement.addElement("imagePath").addText(currentNewsData.getImagePath());
                    }
                    SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    String date1 = sdf1.format(sdf.parse(currentNewsData.getDate().toString()));
                    newsElement.addElement("date").addText(date1);

                    //  newsElement.addElement("date").addText(currentNewsData.getDate().toString());
                    newsElement.addElement("linkParameter").addText(currentNewsData.getLinkParameter());
                    newsElement.addElement("target").addText(currentNewsData.getTarget());
                    newsElement.addElement("alternateText").addText(currentNewsData.getAlternateText() != null ? currentNewsData.getAlternateText() : "");
                    if (currentNewsData.getTarget().equalsIgnoreCase("Shadow Box")) {
                        newsElement.addElement("height").addText(currentNewsData.getHeight());
                        newsElement.addElement("width").addText(currentNewsData.getWidth());
                    }

                    if (currentNewsData.getExternalLink() != null && currentNewsData.getExternalLink().equals("Yes")) {
                        newsElement.addElement("externalLinkFlag").addText("Yes");
                        newsElement.addElement("link").addText(currentNewsData.getLink() != null ? currentNewsData.getLink() : "");
                    } else {
                        newsElement.addElement("externalLinkFlag").addText("No");
                        newsElement.addElement("link").addText("Empty");
                    }

                }
            } else {
                Element newsElement = resultElement.addElement("news").addText("Empty");
            }

        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating News Details Page", e);
        }
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }

    public List<NewsData> retrieveSortedNewsListDisplayedByHompage(RequestContext context) throws DAOException {

        DataManager dataManager = new DataManagerImplCommon(context);
        List<NewsData> resultNews = dataManager.retrieveAllNewsListDisplayedByHomepage(context.getSite().getName());
        return resultNews;
    }
}
