/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.PropertyContext;
import java.util.List;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import nhk.ls.runtime.common.Logger;
import java.io.File;
import org.dom4j.Node;

/**
 *
 * @author sbhojnag
 */
public class newsCategoryGenerator {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.newsCategoryGenerator"));

    public Document getNewsCategory(PropertyContext context) {
        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            outDoc.addElement("Options");
            Element base = outDoc.getRootElement();
            String sitemapPath = context.getSite().getPath() + "/default.sitemap";
            mLogger.createLogInfo("Sitemap Path: " + sitemapPath);
            File sitemapFile = new File(sitemapPath);
            Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);
            String PressLabel = (String) context.getParameters().get("newsLabel");
            List<Node> pressroomNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + PressLabel + "']]/node");
            //      List<Node> pressroomNode = sitemapFileDocument.selectNodes("//node[label[text() = 'Press Room']]/node");
            if (pressroomNode.size() != 0) {
                for (Node unitNode : pressroomNode) {
                    Element option = base.addElement("Option");
                    Node label = unitNode.selectSingleNode("label");
                    option.addElement("Display").addText(label.getText());
                    option.addElement("Value").addText(label.getText());
                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating RSS Page", e);
        }
        return outDoc;
    }
}
