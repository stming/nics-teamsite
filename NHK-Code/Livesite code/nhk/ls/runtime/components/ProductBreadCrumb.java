/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.util.ArrayList;
import com.interwoven.livesite.runtime.RequestContext;
import org.dom4j.Element;
import org.dom4j.Document;
import nhk.ls.runtime.common.Logger;
import java.io.File;
import org.dom4j.Node;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import org.apache.commons.logging.LogFactory;
import com.interwoven.livesite.runtime.model.SiteMap;
import nhk.ls.runtime.common.ProductListingHelper;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author sbhojnag
 */
public class ProductBreadCrumb {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ProductBreadCrumb"));

    public Document getbreadCrumb(RequestContext context) {
        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("Result");
            String dcrPath = context.getParameterString("DCRPath");
            String productLabel = context.getParameterString("ProductLabel", "Products");
            String productListingPageLink = context.getParameterString("ProductListingPage");
            productListingPageLink = productListingPageLink.substring(productListingPageLink.indexOf("[") + 1, productListingPageLink.lastIndexOf("]"));
            ArrayList<String> nodelabel = new ArrayList();
            ArrayList<String> nodelink = new ArrayList();
            mLogger.createLogInfo("DCRPATH: " + context.getParameterString("DCRPath"));

            String dcrfullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrPath;
            File dcrFile = new File(dcrfullPath);
            Document dcrDoc = Dom4jUtils.newDocument(dcrFile);
            String CategoryID = dcrDoc.selectSingleNode("//saleable_product_information/OverviewCtr/Category").getText();
            String productName = dcrDoc.selectSingleNode("//saleable_product_information/OverviewCtr/ProductName").getText();

            mLogger.createLogInfo("CategoryID: " + CategoryID);
            mLogger.createLogInfo("ProductName: " + productName);
            mLogger.createLogInfo("ProductLabel: " + productLabel);
            if (CategoryID != null) {

                SiteMap sitemap = context.getLiveSiteDal().getSiteMap(context.getSite().getName());
                Document siteMapDoc = sitemap.getDocument();
                Node node = siteMapDoc.selectSingleNode("//node[@id ='" + CategoryID + "']");
                while (node.selectSingleNode("label").getText() != null && !node.selectSingleNode("label").getText().equalsIgnoreCase(productLabel)) {
                    nodelabel.add(node.selectSingleNode("label").getText());
                    Element element = (Element) node;
                    Node tempNode = node;

                    String prodListingPageLink = context.getPageLink(productListingPageLink);
                    String variableLink = ProductListingHelper.getVariableLinkForCategoryTraversal(tempNode, productLabel);

                    mLogger.createLogDebug("Url for " + element.attributeValue("id") + "::" + prodListingPageLink + variableLink);
                    nodelink.add(prodListingPageLink + variableLink);
                    node = node.getParent();
                }
            }
            Element nodeelement = resultElement.addElement("Node");
            nodeelement.addElement("Label").addText(productLabel);

            for (int i = nodelabel.size() - 1; i >= 0; i--) {
                nodeelement = resultElement.addElement("Node");
                nodeelement.addElement("Label").addText(nodelabel.get(i).toString());
                Element linkelement = nodeelement.addElement("Link");
                linkelement.addAttribute("Type", "page");
                linkelement.addAttribute("Target", "");

                linkelement.addElement("Url").addText(nodelink.get(i).toString());

            }
            nodeelement = resultElement.addElement("Node");
            nodeelement.addElement("Label").addText(productName);
            mLogger.createLogDebug("Node Label: " + outDoc.asXML());
        } catch (Exception e) {
            mLogger.createLogWarn("Exception in ProductBreadCrumb getbreadCrumb method: " + e.getMessage(), e);
        }
        return outDoc;
    }
}
