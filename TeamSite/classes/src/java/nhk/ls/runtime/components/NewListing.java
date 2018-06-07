/*
 * To change this template, choose Tools | Templates
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
import nhk.ls.runtime.common.Logger;
import java.io.File;
import org.dom4j.Node;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author sbhojnag
 */
public class NewListing {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.NewListing"));

    public Document getNewsList(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("Result");
            String newsCategory = null;
            // adding news category from the sitemap
            String sitemapPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + "sites" + context.getFileDal().getSeparator() + context.getSite().getDirectory() + context.getFileDal().getSeparator() + "default.sitemap";
            mLogger.createLogInfo("Sitemap Path: " + sitemapPath);
            String PressRoomLabel = context.getParameterString("PressRoomLabel", "Press Room");
            File sitemapFile = new File(sitemapPath);
            Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);
            List<Node> pressroomNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + PressRoomLabel + "']]/node");
            if (pressroomNode.size() != 0) {
                for (Node unitNode : pressroomNode) {
                    Element newsCategoryElement = resultElement.addElement("newsCategory");
                    Node label = unitNode.selectSingleNode("label");
                    Node link = unitNode.selectSingleNode("link/value");
                    newsCategoryElement.addElement("Name").addText(label.getText());
                    newsCategoryElement.addElement("Link").addText(link.getText());
                    if (context.getPageName().equalsIgnoreCase(link.getText())) {
                        newsCategory = label.getText();
                    }

                    mLogger.createLogDebug("Label: " + label.getText());
                    mLogger.createLogDebug("Link: " + link.getText());
                }
            } else {
                Element newsCategoryElement = resultElement.addElement("newsCategory").addText("Empty");
            }
            // getting news listing
            DataManager dataManager = new DataManagerImplCommon(context);
            //  String newsCategory = context.getParameterString("newsCategory","Corporate");
            mLogger.createLogDebug("News Category: " + newsCategory);
            List<NewsData> NewsDataList = dataManager.retrieveNewsByCategoryList(newsCategory, context.getSite().getName());
            mLogger.createLogDebug("Before Size");
            mLogger.createLogDebug("News List Size: " + NewsDataList.size());

            if (NewsDataList.size() > 0) {
                for (Iterator i = NewsDataList.iterator(); i.hasNext();) {
                    NewsData currentNewsData = (NewsData) i.next();
                    Element newsElement = resultElement.addElement("news");
                    newsElement.addElement("Headline").addText(currentNewsData.getHeadline());
                    if(currentNewsData.getSubHeadline()!=null && currentNewsData.getSubHeadline().length()!=0){
                    newsElement.addElement("subHeadline").addText(currentNewsData.getSubHeadline());
                    }
                //    newsElement.addElement("subHeadline").addText(currentNewsData.getsubHeadline());
                    if(currentNewsData.getImagePath()!=null && currentNewsData.getImagePath().length()!=0){
                    newsElement.addElement("imagePath").addText(currentNewsData.getImagePath());
                    }
                    else
                    {
                      newsElement.addElement("imagePath").addText("Empty");
                    }
                  //  newsElement.addElement("date").addText(currentNewsData.getDate().toString());
                    //SimpleDateFormat sdf1 = new SimpleDateFormat("MMMMM dd, yyyy");
                    SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    String date1 = sdf1.format(sdf.parse(currentNewsData.getDate().toString()));
                    newsElement.addElement("date").addText(date1);
                    newsElement.addElement("linkParameter").addText(currentNewsData.getLinkParameter());
                    newsElement.addElement("target").addText(currentNewsData.getTarget());
                    if(currentNewsData.getAlternateText()!=null && currentNewsData.getAlternateText().length()!=0){
                    newsElement.addElement("alternateText").addText(currentNewsData.getAlternateText());
                    }
                    else{
                       newsElement.addElement("alternateText").addText("Empty");
                    }
                    if (currentNewsData.getExternalLink().equalsIgnoreCase("Yes")) {
                        newsElement.addElement("link").addText(currentNewsData.getLink());
                    } else {
                        newsElement.addElement("link").addText("Empty");
                    }
                    if (currentNewsData.getTarget().equalsIgnoreCase("Shadow Box")) {
                        newsElement.addElement("height").addText(currentNewsData.getHeight());
                        newsElement.addElement("width").addText(currentNewsData.getWidth());
                    }
                }
            } else {
                Element newsElement = resultElement.addElement("news").addText("Empty");

            }


        } //     }
        catch (Exception e) {
            mLogger.createLogWarn("Error in generating News Details Page", e);
        }
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }
}
