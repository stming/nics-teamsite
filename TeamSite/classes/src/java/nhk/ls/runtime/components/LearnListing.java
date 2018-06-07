/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import nhk.ls.runtime.dao.LearnExploreData;
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

/**
 *
 * @author sbhojnag
 */
public class LearnListing {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.LearnListing"));

    public Document getLearnList(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("Result");
            String learnCategory = null;
            // adding learn category from the sitemap
            String sitemapPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + "sites" + context.getFileDal().getSeparator() + context.getSite().getDirectory() + context.getFileDal().getSeparator() + "default.sitemap";
            mLogger.createLogInfo("Sitemap Path: " + sitemapPath);
            String learnLabel = context.getParameterString("LearnExploreLabel", "Learn & Explore");
            File sitemapFile = new File(sitemapPath);
            Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);
            List<Node> learnExploreNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + learnLabel + "']]/node");
            if (!learnExploreNode.isEmpty()) {
                for (Node unitNode : learnExploreNode) {
                    Element learnCategoryElement = resultElement.addElement("learnCategory");
                    Node label = unitNode.selectSingleNode("label");
                    Node link = unitNode.selectSingleNode("link/value");
                    learnCategoryElement.addElement("Name").addText(label.getText());
                    learnCategoryElement.addElement("Link").addText(link.getText());
                    if (context.getPageName().equalsIgnoreCase(link.getText())) {
                        learnCategory = label.getText();
                    }

                    mLogger.createLogDebug("Label: " + label.getText());
                    mLogger.createLogDebug("Link: " + link.getText());
                }
            } else {
                Element learnCategoryElement = resultElement.addElement("learnCategory").addText("Empty");
            }
            // getting learn listing
            DataManager dataManager = new DataManagerImplCommon(context);
            //  String learnCategory = context.getParameterString("learnCategory","Corporate");
            mLogger.createLogDebug("learn Category: " + learnCategory);
            List<LearnExploreData> learnExploreDataList = dataManager.retrieveLearnByCategoryList(learnCategory, context.getSite().getName());
            mLogger.createLogDebug("Before Size");
            mLogger.createLogDebug("Learn List Size: " + learnExploreDataList.size());

            if (learnExploreDataList.size() > 0) {
                for (Iterator i = learnExploreDataList.iterator(); i.hasNext();) {
                    LearnExploreData currentLearnData = (LearnExploreData) i.next();
                    Element learnElement = resultElement.addElement("learn");
                    learnElement.addElement("Headline").addText(currentLearnData.getHeadline());
                    if(currentLearnData.getSubHeadline()!=null && currentLearnData.getSubHeadline().length()!=0){
                    learnElement.addElement("subHeadline").addText(currentLearnData.getSubHeadline());
                    }
                //    learnElement.addElement("subHeadline").addText(currentLearnData.getsubHeadline());
                    if(currentLearnData.getImagePath()!=null && currentLearnData.getImagePath().length()!=0){
                    learnElement.addElement("imagePath").addText(currentLearnData.getImagePath());
                    }
                    else
                    {
                      learnElement.addElement("imagePath").addText("Empty");
                    }
                  //  learnElement.addElement("date").addText(currentLearnData.getDate().toString());
                    //SimpleDateFormat sdf1 = new SimpleDateFormat("MMMMM dd, yyyy");
                    SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    String date1 = sdf1.format(sdf.parse(currentLearnData.getDate().toString()));
                    learnElement.addElement("date").addText(date1);
                    learnElement.addElement("linkParameter").addText(currentLearnData.getLinkParameter());
                    learnElement.addElement("target").addText(currentLearnData.getTarget());
                    if(currentLearnData.getAlternateText()!=null && currentLearnData.getAlternateText().length()!=0){
                    learnElement.addElement("alternateText").addText(currentLearnData.getAlternateText());
                    }
                    else{
                       learnElement.addElement("alternateText").addText("Empty");
                    }
                    if (currentLearnData.getExternalLink().equalsIgnoreCase("Yes")) {
                        learnElement.addElement("link").addText(currentLearnData.getLink());
                    } else {
                        learnElement.addElement("link").addText("Empty");
                    }
                    if (currentLearnData.getTarget().equalsIgnoreCase("Shadow Box")) {
                        learnElement.addElement("height").addText(currentLearnData.getHeight());
                        learnElement.addElement("width").addText(currentLearnData.getWidth());
                    }
                }
            } else {
                Element learnElement = resultElement.addElement("learn").addText("Empty");

            }


        } //     }
        catch (Exception e) {
            mLogger.createLogWarn("Error in generating learn Details Page", e);
        }
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }
}
