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
import org.dom4j.Node;

import java.util.List;
import java.util.Iterator;
import java.io.*;

import nhk.ls.runtime.common.Logger;

/**
 *
 * @author wxiaoxi
 */
public class HighlightDetails {

    private Logger mLogger;

    public org.dom4j.Document createHomepageHighlightsResult(RequestContext context) {
        this.mLogger = new Logger(LogFactory.getLog(this.getClass()));
        Document outDoc = null;
        Document inDoc = null;
        outDoc = Dom4jUtils.newDocument();
        Element resultElement = outDoc.addElement("Result");

        mLogger.createLogInfo("Homepage Highlights started");
        try {
            String dcrFullPath = null;
            dcrFullPath = (context != null && context.getParameters() != null) ? (String) context.getParameters().get("id") : "";

            mLogger.createLogInfo("DCR Path: " + dcrFullPath);
            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;

            mLogger.createLogDebug("Full DCR Path: " + dcrFullPath);
            mLogger.createLogDebug("Full DCR Path: " + dcrFullPath);

            String TabPath = null;
            TabPath = (context != null && context.getParameters() != null) ? (String) context.getParameters().get("WithTabNum") : "";

            mLogger.createLogDebug("TabPath: " + TabPath);

            File dcrFile = new File(dcrFullPath);
            if (dcrFile.exists()) {
                inDoc = Dom4jUtils.newDocument(dcrFile);
                Element homepageBannerElement = resultElement.addElement("homepage_hightlights_details");

                //add flashes
                String xPath = "//homepage_hightlights/Tab";
                List<Node> nodes = inDoc.selectNodes(xPath);
                for (Iterator i = nodes.iterator(); i.hasNext();) {
                    Node currentNode = (Node) i.next();
                    Element tabEle = (Element) currentNode;
                    String LinkParameter = tabEle.element("Link_Parameter").getText();

                    if (LinkParameter.endsWith("WithTabNum=" + TabPath)) {
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
}
