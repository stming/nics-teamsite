/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.common.io.FileUtil;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.dao.Products;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import nhk.ls.runtime.dao.DAOException;
import java.io.File;
import com.interwoven.livesite.runtime.model.SiteMap;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;

/**
 *
 * @author wxiaoxi
 */
public class HomepageProduct {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.HomepageProduct"));

    public Document getLatestProductsList(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("items");

            // getting products listing
            String defaultProductIconImage = "/"
                    + ((context != null && context.getParameters() != null && context.getParameters().get("DefaultProductIconImage") != null) ? (String) context.getParameters().get("DefaultProductIconImage") : "");
            int numberOfResultsOnPage = Integer.parseInt(context.getParameterString("NumberOfProductsToDisplay", "12"));

            mLogger.createLogInfo("defaultProductIconImage=" + defaultProductIconImage);
            mLogger.createLogInfo("numberOfResults=" + numberOfResultsOnPage);

            String ProductDetailsPage = context.getParameterString("ProductDetailsPage");
            String ProductLabel = context.getParameterString("ProductLabel", "Products");
            ProductDetailsPage = ProductDetailsPage.substring(ProductDetailsPage.indexOf("[") + 1, ProductDetailsPage.lastIndexOf("]"));
            List<Products> ProductsList = retrieveSortedProductListDisplayedByHompage(context, numberOfResultsOnPage);
            resultElement.addElement("records").addText(ProductsList.size() + "");

            if (ProductsList.size() > 0) {
                SiteMap sitemap = context.getLiveSiteDal().getSiteMap(context.getSite().getName());
                Document siteMapDoc = sitemap.getDocument();
                for (int i = 0; i < ProductsList.size(); i++) {

                    String categoryIDTraverser = "";

                    Products currentProductsData = ProductsList.get(i);
                    String categoryID = currentProductsData.getCategory().getCategoryID();

                    if (categoryID != null) {
                        Node node = siteMapDoc.selectSingleNode("//node[@id ='" + categoryID + "']");
                        while (node.selectSingleNode("label").getText() != null && !node.selectSingleNode("label").getText().equalsIgnoreCase(ProductLabel)) {
                            categoryIDTraverser += ((Element) node).attributeValue("id") + "|";
                            mLogger.createLogDebug("Checking for label in node id::" + node.getParent().attributeValue("id"));
                            if (node.getParent().selectSingleNode("label").getText().equalsIgnoreCase(ProductLabel)) {
                                Element element = (Element) node;
                                categoryID = element.attributeValue("id");
                            }
                            node = node.getParent();
                        }
                    }
                    Element productsElement = resultElement.addElement("item");
                    productsElement.addText("empty");
                    productsElement.addAttribute("url", StringUtils.isNotEmpty(currentProductsData.getImage()) ? currentProductsData.getImage() : defaultProductIconImage);
                    productsElement.addAttribute("title", currentProductsData.getProductName());
                    productsElement.addAttribute("text", StringUtils.isNotEmpty(currentProductsData.getQuickViewData()) ? "\u2022 " + currentProductsData.getQuickViewData().replaceAll("\r\n", "\n\u2022 ") : "");

                    String prodDetailsPageLink = context.getPageLink(ProductDetailsPage) + "?DCRPath=" + currentProductsData.getDcrPath();

                    String[] arr = StringUtils.split(categoryIDTraverser, "|");

                    mLogger.createLogDebug("categoryIDTraverser::" + categoryIDTraverser);

                    String variableLink = "";
                    int len = (arr.length >= 3) ? 3 : arr.length;

                    mLogger.createLogDebug("len" + len);
                    switch (len) {
                        case 3:
                            variableLink = "&CategoryID=" + arr[arr.length - 1] + "&currentTab=" + arr[arr.length - 2] + "&currentLink=" + arr[arr.length - 3];
                            break;
                        case 2:
                            variableLink = "&CategoryID=" + arr[arr.length - 1] + "&currentTab=" + arr[arr.length - 2];
                            break;
                        default:
                            variableLink = "&CategoryID=" + arr[arr.length - 1];
                    }
                    mLogger.createLogDebug("Url for " + categoryID + "::" + prodDetailsPageLink + variableLink);
                    productsElement.addAttribute("link", prodDetailsPageLink + variableLink);
                    productsElement.addAttribute("target", "_self");
                }
                //output to an xml file
                String outputFileName = null;
                outputFileName = (context != null && context.getParameters() != null && context.getParameters().get("ProductThumbsSoucePath") != null) ? (String) context.getParameters().get("ProductThumbsSoucePath") : "";
                mLogger.createLogDebug("outputXML Path:" + outputFileName);
                if (outputFileName != null && !outputFileName.equals("")) {
                    outputFileName = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + outputFileName;
                    mLogger.createLogDebug("outputXML Path:" + outputFileName);

                    File outFile = new File(outputFileName);
                    try {
                        FileUtil.writeText(outFile, outDoc.asXML());
                    } catch (Exception e) {
                        mLogger.createLogDebug("HomepageProduct error creating file:" + outputFileName + " " + e);
                    }
                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating get Latest Products List Page", e);
        }
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }

    public List<Products> retrieveSortedProductListDisplayedByHompage(RequestContext context, int numberOfResultsOnPage) throws DAOException {
        DataManager dataManager = new DataManagerImplCommon(context);
        List<Products> resultProducts = dataManager.retrieveAllProductListDisplayedByHomepage(context.getSite().getName(), numberOfResultsOnPage);
        return resultProducts;
    }
}
