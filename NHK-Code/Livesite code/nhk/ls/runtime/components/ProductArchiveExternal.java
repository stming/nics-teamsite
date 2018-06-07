/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nhk.ls.runtime.common.Constants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.common.ProductListingHelper;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author smukherj
 */
public class ProductArchiveExternal {

    private static Document doc;
    //private static String productCategoryID;
    private static String productCategoryName;
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ProductArchiveExternal"));

    public Document getProductList(RequestContext context) throws IOException {

        Document resultDoc = Dom4jUtils.newDocument();
        try {
            String productCategoryID = (context != null && context.getParameters() != null && context.getParameters().get("CategoryID") != null) ? (String) context.getParameters().get("CategoryID") : "";

            String defaultProductIconImage =
                    (context != null && context.getParameters() != null && context.getParameters().get("DefaultProductIconImage") != null) ? (String) context.getParameters().get("DefaultProductIconImage") : "";
            mLogger.createLogInfo("defaultProductIconImage=" + defaultProductIconImage);
            // TODO Push the defaultProductIconImage to in case of no product image

            mLogger.createLogInfo("ProductArchiveExternal catId=" + productCategoryID);
            String FilePath = context.getFileDAL().getRoot() + context.getFileDAL().getSeparator() + "productlisting" + context.getFileDAL().getSeparator() + context.getSite().getName();

            File catFile = new File(FilePath + File.separator + productCategoryID + ".xml");

            if (!catFile.exists()) {
                mLogger.createLogDebug("Creating file:::");
                doc = Dom4jUtils.newDocument();
                ProductListingHelper.createCatFile(context, catFile, doc, productCategoryID, defaultProductIconImage);
            } else {
                doc = Dom4jUtils.newDocument(catFile);
            }
            //Process the file and create current tab result dom object
            // Input from the first field on the form

            String categoryInput = context.getParameterString("categoryInput");
            String subCategoryInput = context.getParameterString("subCategoryInput");

            mLogger.createLogDebug("categoryInput=" + categoryInput);
            mLogger.createLogDebug("subCategoryInput=" + subCategoryInput);

            Element resultElement = resultDoc.addElement("Dropdown");

            Element categoryIDElement = resultElement.addElement("CatID");
            categoryIDElement.addText(productCategoryID);

            if (CollectionUtils.isNotEmpty(doc.selectNodes("//L3/Category"))) {

                List categoryElements = doc.selectNodes("//L3/Category");
                Element categoryDropdownResult = resultElement.addElement("CategoryOptions");

                for (Object object : categoryElements) {
                    Element categoryOptionResult = categoryDropdownResult.addElement("Option");
                    Element categoryElement = (Element) object;

                    String optionName = categoryElement.selectSingleNode("@Name").getText();
                    String optionValue = categoryElement.selectSingleNode("@Id").getText();

                    categoryOptionResult.addAttribute("value", optionValue);
                    categoryOptionResult.addText(optionName);

                    if (StringUtils.equalsIgnoreCase(optionValue, categoryInput)) {
                        categoryOptionResult.addAttribute("selected", "true");
                    }

                }
                if (!StringUtils.isEmpty(categoryInput)) {

                    Element productContent;
                    List products;


                    Element currentCategoryElement = (Element) doc.selectSingleNode("//L3/Category[@Id='" + categoryInput + "']/*");

                    // Generating L4 dropdown options within the sub-category dropdown

                    if (CollectionUtils.isNotEmpty(currentCategoryElement.selectNodes("../L4Category"))) {

                        mLogger.createLogDebug("Found L4Category within L3/Category[@Id='" + categoryInput + "']");

                        List subCats = currentCategoryElement.selectNodes("../L4Category");

                        boolean is_l5_selected = !StringUtils.isEmpty(subCategoryInput) && subCategoryInput.indexOf("|") >= 0;

                        Element subCategoryDropdownResult = resultElement.addElement("SubCategoryOptions");

                        for (Object object : subCats) {
                            Element subCategoryOptionResult = subCategoryDropdownResult.addElement("Option");

                            Element subCategoryOptionEle = (Element) object;

                            String l4optionName = subCategoryOptionEle.selectSingleNode("@Name").getText();
                            String l4optionValue = subCategoryOptionEle.selectSingleNode("@Id").getText();

                            mLogger.createLogDebug("Parsing l4optionName=" + l4optionName);

                            subCategoryOptionResult.addAttribute("value", l4optionValue);
                            subCategoryOptionResult.addText(l4optionName);

                            if (StringUtils.equalsIgnoreCase(l4optionValue, subCategoryInput)) {
                                subCategoryOptionResult.addAttribute("selected", "true");
                            }

                            // Generating L5 dropdown options within the sub-category dropdown

                            mLogger.createLogDebug("Searching within l4optionName=" + l4optionName + " for L5Category");

                            if (CollectionUtils.isNotEmpty(subCategoryOptionEle.selectNodes("L5Category"))) {

                                mLogger.createLogDebug("Found L5Category within L3/Category[@Id=" + categoryInput + "']/L5Category");

                                List subCats2 = subCategoryOptionEle.selectNodes("L5Category");

                                for (Object object1 : subCats2) {
                                    Element subCategory2OptionEle = (Element) object1;
                                    subCategoryOptionResult = subCategoryDropdownResult.addElement("Option");

                                    String l5optionName = subCategory2OptionEle.selectSingleNode("@Name").getText();
                                    String l5optionValue = subCategory2OptionEle.selectSingleNode("@Id").getText();

                                    subCategoryOptionResult.addAttribute("value", l4optionValue + "|" + l5optionValue);
                                    subCategoryOptionResult.addText("     -" + l5optionName);

                                    mLogger.createLogDebug("Parsing l5optionName=" + l5optionName);

                                    if (is_l5_selected) {

                                        String[] categories = StringUtils.split(subCategoryInput, "|");

                                        if (StringUtils.equalsIgnoreCase(l4optionValue, categories[0]) && StringUtils.equalsIgnoreCase(l5optionValue, categories[1])) {
                                            subCategoryOptionResult.addAttribute("selected", "true");
                                        }
                                    }
                                }
                            }
                        }
                        if (!StringUtils.isEmpty(subCategoryInput)) {
                            // Output the products for the selected subCategory dropdown
                            List subCats2;
                            if (is_l5_selected) {
                                // L5 exists and is selected
                                String[] categories = StringUtils.split(subCategoryInput, "|");
                                subCats2 = currentCategoryElement.selectNodes("../L4Category[@Id='" + categories[0] + "']/L5Category[@Id='" + categories[1] + "']/*");
                            } else {
                                subCats2 = currentCategoryElement.selectNodes("../L4Category[@Id='" + subCategoryInput + "']/L5Category/*");
                            }
                            if (CollectionUtils.isNotEmpty(subCats2)) {
                                // Checking that products do exist at the L5 level for current selection
                                for (Object object2 : subCats2) {
                                    Element productElement = (Element) object2;
                                    addProductOption(productElement, resultElement);

                                }
                            } else {

                                // Only upto L4 exists
                                subCats2 = currentCategoryElement.selectNodes("../L4Category[@Id='" + subCategoryInput + "']/*");
                                if (CollectionUtils.isNotEmpty(subCats2)) {
                                    // Products exist within L4
                                    for (Object object2 : subCats2) {
                                        Element productElement = (Element) object2;
                                        addProductOption(productElement, resultElement);
                                    }
                                }
                            }

                        }

                    } else {
                        //For categories without L4/L5 sub-categories, Populate the Products directly under L3
                        if (currentCategoryElement != null) {
                            addProductOption(currentCategoryElement, resultElement);
                        }
                    }
                }
            } else {

                // Add products from L2 category
                List products = doc.selectNodes("//L2Category/*");
                if (CollectionUtils.isNotEmpty(products)) {
                    for (Object object : products) {
                        Element productElement = (Element) object;
                        addProductOption(productElement, resultElement);
                    }
                }

            }
            Element countElement = resultElement.addElement("ProductCount");
            if (CollectionUtils.isNotEmpty(resultDoc.selectNodes("//ProductContent/Product"))) {
                countElement.addText("" + ((List) resultDoc.selectNodes("//ProductContent/Product")).size());
            }

        } catch (Exception e) {
            mLogger.createLogWarn("Error in retrieving Product Archive Result", e);
        }
        mLogger.createLogDebug("Product Archieve Content XML Value: " + resultDoc.asXML());

        return resultDoc;
    }

    private void addProductOption(Element productElement, Element resultElement) {
        mLogger.createLogInfo("Add ProductOption: " + productElement.asXML() + resultElement.asXML());
        List products = productElement.selectNodes("../Products/Product[@Archived='Yes']");
        if (CollectionUtils.isNotEmpty(products)) {
            Element productDropdownResult = createElement(resultElement, "ProductOptions");
            Element productContentResult = createElement(resultElement, "ProductContent");
            String productOptionName;
            String productDCRPath;

            for (Object object : products) {
                Element product = (Element) object;

                productOptionName = product.selectSingleNode("Name").getText();
                productDCRPath = product.selectSingleNode("DCRPath").getText();

                Element productOptionResult = productDropdownResult.addElement("Option");
                productOptionResult.addAttribute("value", productDCRPath);
                productOptionResult.addText(productOptionName);
                mLogger.createLogDebug("Product Name: " + productOptionName);
                productContentResult.add(product.createCopy());
            }
        }
    }

    private Element createElement(Element resultElement, String nodeName) {

        Element targetNode = null;
        if (resultElement.selectSingleNode(nodeName) != null) {
            targetNode = (Element) resultElement.selectSingleNode(nodeName);
        } else {
            targetNode = resultElement.addElement(nodeName);
        }
        return targetNode;
    }
}
