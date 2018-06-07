/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.common;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import nhk.ls.runtime.dao.ProductCategory;
import nhk.ls.runtime.dao.Products;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.common.io.FileUtil;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;

/**
 *
 * @author smukherj
 */
public class ProductListingHelper {

    public static DataManager dataManager;
    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.common.ProductListingHelper"));

    public static void createCatFile(RequestContext context, File catFile, Document doc, String productCategoryID, String defaultProductIconImage) throws IOException {
        dataManager = new DataManagerImplCommon(context);


        //Element resultElement = doc.addElement("L2Category");


        // Dummy is created to counter the scenario when there are no products, no tabs should be created.
        Document dummyDoc = Dom4jUtils.newDocument();
        Element resultElement = dummyDoc.addElement("L2Category");

        ProductCategory l2Category = dataManager.retrieveL2Category(productCategoryID);

        String l2CatId = l2Category.getCategoryID();
        boolean l2CategoryPassed = l2CatId.equalsIgnoreCase(productCategoryID);
        resultElement.addAttribute("Id", l2CatId);
        resultElement.addAttribute("Name", l2Category.getCategoryName());

        // End
        // 2. Get Sub-categories for Tabs and create product hierarchy
        List<ProductCategory> prodCats = dataManager.retrieveCategoryByParentID(l2CatId);
        mLogger.createLogDebug("Prod Cat Size: " + prodCats.size());
        //If sub-categories exist
        if (CollectionUtils.isNotEmpty(prodCats)) {
            Element L3CatsEl = resultElement.addElement("L3");

            int level = 4;
            for (ProductCategory productCategory : prodCats) {
                mLogger.createLogDebug("Inside For Product Category::" + productCategory.getCategoryID());
                if (l2CategoryPassed || productCategory.getCategoryID().equalsIgnoreCase(productCategoryID)) {

                    Document dummyCategoryDoc = Dom4jUtils.newDocument();
                    Element L3El = dummyCategoryDoc.addElement("Category");

                    L3El.addAttribute("Name", productCategory.getCategoryName());
                    L3El.addAttribute("Id", productCategory.getCategoryID());
                    createSubCategoryTree(productCategory.getCategoryID(),
                            L3El, level, defaultProductIconImage);

                    if (L3El.selectNodes("//Product").size() > 0) {
                        L3CatsEl.add(L3El.createCopy());
                    }
                }
            }
        } //Else generate product hierarchy
        else {
            createProductHierarchy(l2CatId, resultElement, defaultProductIconImage);
        }
        // Add the element only in case of existence of products
        if (resultElement.selectNodes("//Product").size() > 0) {
            doc.add(resultElement.createCopy());
        }
        mLogger.createLogInfo("Created File " + catFile.getName() + " successfully.");
        FileUtil.writeText(catFile, doc.asXML());
    }

    public static void createSubCategoryTree(String categoryID, Element L3El, int level, String defaultProductIconImage) {
        List<ProductCategory> prodCatList = dataManager.retrieveCategoryByParentID(categoryID);

        if (CollectionUtils.isNotEmpty(prodCatList)) {

            for (ProductCategory productCategory : prodCatList) {
                //Element subCat = L3El.addElement("L" + level + "Category");

                Document dummySubCategoryDoc = Dom4jUtils.newDocument();
                Element subCat = dummySubCategoryDoc.addElement("L" + level + "Category");

                String subCatID = productCategory.getCategoryID();
                subCat.addAttribute("Name", productCategory.getCategoryName());
                subCat.addAttribute("Id", productCategory.getCategoryID());
                createSubCategoryTree(subCatID, subCat, level + 1, defaultProductIconImage);

                // Add the element only in case of existence of products
                if (subCat.selectNodes("//Product").size() > 0) {
                    L3El.add(subCat.createCopy());
                }
            }
        } else {
            createProductHierarchy(categoryID, L3El, defaultProductIconImage);
        }
    }

    public static void createProductHierarchy(String categoryID, Element L3El, String defaultProductIconImage) {
        List<Products> productList = dataManager.retrieveProductsByCategory(categoryID);

        if (CollectionUtils.isNotEmpty(productList)) {
            mLogger.createLogInfo("Number of products found from database query:" + productList.size());
            Element products = L3El.addElement("Products");
            for (Products pro : productList) {
                Element prod = products.addElement("Product");
                prod.addAttribute("Archived", pro.getArchiveFlag());
                prod.addAttribute("ComingSoon", pro.getComingSoonFlag());
                prod.addAttribute("ID", pro.getProductID());
                //prod.addAttribute("Status", pro.getStatus()); No longer need this flag as only Status="Yes" products are retrieved.

                mLogger.createLogDebug("PRODUCT=" + pro.getProductName() + ":RELEASE_DATE=" + pro.getReleaseDate().toString());

                prod.addAttribute("ReleaseDate", pro.getReleaseDate().toString());
                prod.addAttribute("Price", pro.getPrice());
                prod.addElement("Name").setText(pro.getProductName());
                prod.addElement("Image").setText(StringUtils.isNotEmpty(pro.getImage()) ? pro.getImage() : defaultProductIconImage);
                Element featureEle = prod.addElement("Features");
                if (pro.getQuickViewData() != null) {
                    featureEle.setText(StringUtils.isNotEmpty(pro.getQuickViewData()) ? "\u2022 " + pro.getQuickViewData().replaceAll("\n", "<br/>\u2022 ") : "");
                }
                prod.addElement("DCRPath").setText(StringUtils.isBlank(pro.getDcrPath()) ? "na" : pro.getDcrPath());
            }
        }
    }

    public static String getVariableLinkForCategoryTraversal(Node tempNode, String productLabel) {

        String variableLink = "";
        String categoryIDTraverser = "";
        int i = 0;
        while (tempNode.selectSingleNode("label") != null && tempNode.selectSingleNode("label").getText() != null && !tempNode.selectSingleNode("label").getText().equalsIgnoreCase(productLabel)) {

            if (((Element) tempNode).selectSingleNode("link[@type!='']") != null) {
                categoryIDTraverser += ((Element) tempNode).attributeValue("id") + "|";
            }

            tempNode = tempNode.getParent();
            i++;
        }
        if (tempNode.selectSingleNode("label") != null && tempNode.selectSingleNode("label").getText() != null && tempNode.selectSingleNode("label").getText().equalsIgnoreCase(productLabel)) {
            String[] arr = StringUtils.split(categoryIDTraverser, "|");
            mLogger.createLogInfo("categoryIDTraverser::" + categoryIDTraverser);
            int len = (arr.length >= 3) ? 3 : arr.length;
            mLogger.createLogDebug("len::" + len);
            switch (len) {
                case 3:
                    variableLink = "?CategoryID=" + arr[arr.length - 1] + "&currentTab=" + arr[arr.length - 2] + "&currentLink=" + arr[arr.length - 3];
                    break;
                case 2:
                    variableLink = "?CategoryID=" + arr[arr.length - 1] + "&currentTab=" + arr[arr.length - 2];
                    break;
                case 1:
                    variableLink = "?CategoryID=" + arr[arr.length - 1];
                    break;
                default:
                    break;
            }
        }
        return variableLink;
    }

    public static String getVariableLinkForSiteCatalyst(Node tempNode, String productLabel) {

        String variableLink = "";
        String categoryEngNameTraverser = "";

        while (tempNode.selectSingleNode("label") != null && tempNode.selectSingleNode("label").getText() != null && !tempNode.selectSingleNode("label").getText().equalsIgnoreCase(productLabel)) {

            if (((Element) tempNode).selectSingleNode("description") != null && StringUtils.isNotEmpty(((Element) tempNode).selectSingleNode("description").getText())) {
                categoryEngNameTraverser += ((Element) tempNode).selectSingleNode("description").getText() + "|";
            } else {
                categoryEngNameTraverser += ((Element) tempNode).selectSingleNode("label").getText() + "|";
            }

            tempNode = tempNode.getParent();
        }

        mLogger.createLogDebug("categoryEngNameTraverser=" + categoryEngNameTraverser);

        String[] arr = StringUtils.split(categoryEngNameTraverser, "|");
        List<String> list = Arrays.asList(arr);
        Collections.reverse(list);
        arr = (String[]) list.toArray();

        for (int i = 0; i < arr.length; i++) {

            if (i == 0) {
                variableLink += arr[i];
            } else {
                variableLink += ":" + arr[i];
            }
        }
        mLogger.createLogDebug("variableLink=" + variableLink);
        return variableLink;
    }
}
