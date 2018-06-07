/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.datasources;

import com.interwoven.datasource.MapDataSource;
import nhk.ts.wcms.common.Logger;
import com.interwoven.datasource.core.DataSourceContext;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import com.interwoven.livesite.common.cssdk.datasource.AbstractDataSource;
import java.util.Properties;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author sbhojnag
 */
public class ProductCategoryDataSource extends AbstractDataSource implements MapDataSource {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.datasources.ProductCategoryDataSource"));

    public Map<String, String> execute(DataSourceContext context) {

        Map<String, String> results = new LinkedHashMap<String, String>();
        try {
            String propertyFullPath = null;
            String locale = context.getParameter("locale");
            mLogger.createLogDebug("Locale: " + locale);
            propertyFullPath = "resources/properties/" + locale + "/SiteInfo.xml";
            propertyFullPath = context.getServerContext() + "/" + propertyFullPath;
            mLogger.createLogDebug("SiteInfo reading properties from:" + propertyFullPath);
            File propertyFile = new File(propertyFullPath);
            Document propertyFileDocument = Dom4jUtils.newDocument(propertyFile);
            Node node = propertyFileDocument.selectSingleNode("//entry[@key='productNodeLabel']");
            String productlabel = node.getText();
            String categoryLevel = context.getParameter("ProductCategory");
            mLogger.createLogDebug("Category Level: " + categoryLevel);

            mLogger.createLogDebug("Product Label: " + productlabel);
            String vpath = context.getServerContext();
            if ((null != vpath) && (!("".equals(vpath)))) {
                String sitemapFileVPath = vpath + "/sites/" + locale + "/default.sitemap";
                mLogger.createLogDebug("Sitemap File VPath: " + sitemapFileVPath);
                File sitemapFile = new File(sitemapFileVPath);
                Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);
                List<Node> productNode = null;
                if (categoryLevel.equalsIgnoreCase("Level1")) {
                    mLogger.createLogDebug("In Level 1");
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level2")) {
                    mLogger.createLogDebug("In Level 2:" + context.getParameter("CategorySelected"));
                    String CategorySelected = context.getParameter("CategorySelected");
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + CategorySelected + "']/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level3")) {
                    String CategorySelected = context.getParameter("CategorySelected");
                    String CategorySelected1 = context.getParameter("CategorySelected1");
                    mLogger.createLogDebug("In Level 3:" + CategorySelected + " " + CategorySelected1);
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + CategorySelected + "']/node[@id = '" + CategorySelected1 + "']/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level4")) {
                    String CategorySelected = context.getParameter("CategorySelected");
                    String CategorySelected1 = context.getParameter("CategorySelected1");
                    String CategorySelected2 = context.getParameter("CategorySelected2");
                    mLogger.createLogDebug("In Level 4:" + CategorySelected + " " + CategorySelected1 + " " + CategorySelected2);
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + CategorySelected + "']/node[@id = '" + CategorySelected1 + "']/node[@id = '" + CategorySelected2 + "']/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level5")) {
                    String CategorySelected = context.getParameter("CategorySelected");
                    String CategorySelected1 = context.getParameter("CategorySelected1");
                    String CategorySelected2 = context.getParameter("CategorySelected2");
                    String CategorySelected3 = context.getParameter("CategorySelected3");
                    mLogger.createLogDebug("In Level 5:" + CategorySelected + " " + CategorySelected1 + " " + CategorySelected2 + " " + CategorySelected3);
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + CategorySelected + "']/node[@id = '" + CategorySelected1 + "']/node[@id = '" + CategorySelected2 + "']/node[@id = '" + CategorySelected3 + "']/node");
                }
                if (productNode.size() != 0) {
                    for (Node unitNode : productNode) {
                        Element el = (Element) unitNode;
                        mLogger.createLogDebug("Attribute Id: " + el.attributeValue("id"));
                        Node label = unitNode.selectSingleNode("label");
                        results.put(el.attributeValue("id"), label.getText());
                    }
                }
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error in execute method::", e);
            this.mLogger.createLogErrorWithoutThrowingException("Error retrieving Product Category " + e.getMessage(), e);
        }
        return results;
    }
}
