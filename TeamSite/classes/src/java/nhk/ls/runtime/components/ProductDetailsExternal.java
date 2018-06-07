/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.common.ProductElementSort;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import nhk.ls.runtime.dao.Products;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;

/**
 *
 * @author smukherj
 */
public class ProductDetailsExternal {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ProductDetailsExternal"));
    private DataManager dataManager;
    private static final String EXSPEC_SPEC_FILENAME = "exspec_spec";
    private static final String EXSPEC_COMPATIBILITY_FILENAME = "exspec_compatibility";
    private static final String KEY_FEATURE_HEADLINE_LINKNAME_PATH = "//key_feature/info_level1/headline";
    private static final String EXTERNAL_PARAM_DCR_PATH = "DCRPath";
    private static final String EXTERNAL_PARAM_PRODUCT_LABEL = "ProductLabel";
    private static final String EXTERNAL_PARAM_CATEGORY_ID = "CategoryID";
    private static final String EXTERNAL_PARAM_CURRENT_TAB = "currentProductTab";
    private static final String EXTERNAL_PARAM_CURRENT_LINK = "currentProductLink";
    private static final String EXTERNAL_PARAM_SAMPLE_IMAGES = "LabelForSampleImages";
    private String sampleImagesTabName = "";
    private static final String EXTERNAL_PARAM_KEY_FEATURES = "LabelForKeyFeatures";
    private String keyFeaturesTabName = "";
    private static final String EXTERNAL_PARAM_SPECIFICATIONS = "LabelForSpecifications";
    private String specificationsTabName = "";
    private static final String EXTERNAL_PARAM_FEATURES_EXPLAINED = "LabelForFeaturesExplained";
    private String featuresExplainedTabName = "";
    private static final String EXTERNAL_PARAM_SYSTEM = "LabelForSystem";
    private String systemTabName = "";
    private static final String EXTERNAL_PARAM_RELATED_PRODUCTS = "LabelForRelatedProducts";
    private String relatedProductsTabName = "";
    private static final String ALPHANUMERIC_REGEX = "^[a-zA-Z0-9]*";
    private static final String NUMERIC_REGEX = "^[0-9]*";
    private static final String SALEABLE_PRODUCT_DCR_REGEX = "(^templatedata/[a-zA-Z_]*[/saleable_product_information/data/].*)[.]xml$";

    public Document getProductDetails(RequestContext context) {

        String dcrFullPath = null;
        Document inDoc = null;
        Pattern dcrPattern = Pattern.compile(SALEABLE_PRODUCT_DCR_REGEX);
        try {
            dcrFullPath = context.getParameterString(EXTERNAL_PARAM_DCR_PATH, "");

            String defaultProductImage = "/"
                    + ((context != null && context.getParameters() != null && context.getParameters().get("DefaultProductImage") != null) ? (String) context.getParameters().get("DefaultProductImage") : "");
            mLogger.createLogInfo("defaultProductImage=" + defaultProductImage);

            paramRegexChecker(dcrPattern, dcrFullPath);

            mLogger.createLogInfo("getProductDetails invoked with DCR Path: " + dcrFullPath);
            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;
            mLogger.createLogInfo("Full DCR Path: " + dcrFullPath);

            if (StringUtils.isNotEmpty(dcrFullPath)) {
                File dcrFile = new File(dcrFullPath);
                if (dcrFile.exists()) {
                    inDoc = Dom4jUtils.newDocument(dcrFile);
                    setDefaultGalleryImageIfNotPresent(inDoc, defaultProductImage);

                    // Call for other siteCatalyst script
                    // Product Support
                    // Product Highlights

                    String productSupportText = context.getParameterString("ProductSupportText");
                    String productHighlightsText = context.getParameterString("ProductHighlightsText");

                    if (productSupportText != null && !productSupportText.equalsIgnoreCase("")) {
                        modifyProductSupportScript(productSupportText, inDoc);
                    }
                    if (productHighlightsText != null && !productHighlightsText.equalsIgnoreCase("")) {
                        modifyProductHighlightsScript(productHighlightsText, inDoc);
                    }

                    inDoc.getRootElement().addElement("SiteName").addText(context.getSite().getName());
                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in retrieving Product Details", e);
        }
        //mLogger.createLogDebug("getProductDetails FINALXML:" + inDoc.asXML());
        return inDoc;
    }

    private void modifyProductSupportScript(String productSupportText, Document inDoc) {

        String modified = StringEscapeUtils.unescapeHtml(productSupportText).replaceAll("<br />", "\n").replaceAll("<p>", "").replaceAll("</p>", "");
        // Run over all the SupportCtr elements and add a <onClickData> element to <SupportCtr>
        String productName = inDoc.selectSingleNode("//ProductName").getText();
        List<Node> supportNodes = inDoc.selectNodes("//SupportCtr");

        for (Iterator<Node> it = supportNodes.iterator(); it.hasNext();) {
            String scriplet = modified;
            Node node = it.next();

            if (!isElementCompleteBlank((Element) node, true)) { // Is not empty kind of check
                String title = ((Element) node).selectSingleNode("SupportLinkTitle").getText();

                mLogger.createLogDebug("modifyProductSupportScript Title value=" + title);

                scriplet = scriplet.replaceAll("\\$SC_PRODUCT_SUPPORT_TITLE\\[\\]", title);
                scriplet = scriplet.replaceAll("\\$SC_PRODUCT_NAME\\[\\]", productName);

                ((Element) node).addElement("onClickData").setText(scriplet);
            }
        }
    }

    private void modifyProductHighlightsScript(String productHighlightsText, Document inDoc) {
        String modified = StringEscapeUtils.unescapeHtml(productHighlightsText).replaceAll("<br />", "\n").replaceAll("<p>", "").replaceAll("</p>", "");
        // Run over all the SupportCtr elements and add a <onClickData> element to <SupportCtr>
        String productName = inDoc.selectSingleNode("//ProductName").getText();
        List<Node> highlightsNodes = inDoc.selectNodes("//Awards-ReviewCtr");

        for (Iterator<Node> it = highlightsNodes.iterator(); it.hasNext();) {
            String scriplet = modified;
            Node node = it.next();

            if (!isElementCompleteBlank((Element) node, true)) { // Is not empty kind of check
                String title = "highlights";
                scriplet = scriplet.replaceAll("\\$SC_PRODUCT_HIGHLIGHTS_TITLE\\[\\]", title);
                scriplet = scriplet.replaceAll("\\$SC_PRODUCT_NAME\\[\\]", productName);

                ((Element) node).addElement("onClickData").setText(scriplet);
            }
        }
    }

    private void setDefaultGalleryImageIfNotPresent(Document inDoc, String defaultProductImage) {
        List<Element> colorCtrs = inDoc.selectNodes("//ColourCtr");
        boolean noImagesPresentFlag = true;
        if (CollectionUtils.isNotEmpty(colorCtrs)) {
            if (colorCtrs.size() == 1) {
                mLogger.createLogDebug("setDefaultGalleryImageIfNotPresent only 1 element present");
                Element element = colorCtrs.get(0);
                if (!isElementCompleteBlank(element, true)) {
                    mLogger.createLogDebug("setDefaultGalleryImageIfNotPresent setting noImagesPresentFlag to FALSE");
                    noImagesPresentFlag = false;
                } else {
                    // No Images present, removing the existing ColorCtr elements
                    mLogger.createLogDebug("setDefaultGalleryImageIfNotPresent No Images present, removing the existing 1 ColourCtr element");
                    inDoc.remove(element);
                }
            } else {
                // More than one colorCtr present. No need to add default image.
                noImagesPresentFlag = false;
            }
        }
        if (noImagesPresentFlag) {
            // Adding a new default one
            mLogger.createLogDebug("setDefaultGalleryImageIfNotPresent noImagesPresentFlag is TRUE: Adding a default image");

            Element colourCtr = inDoc.getRootElement().addElement("ColourCtr");
            colourCtr.addElement("Name");
            colourCtr.addElement("Image");
            Element productImageCtr = colourCtr.addElement("ProductImageCtr");
            productImageCtr.addElement("ProductImage").setText(defaultProductImage);
        }
    }

    public Document getProductTabDetails(RequestContext context) {
        String dcrFullPath = null;
        Document inDoc = null;
        Document resultDoc = Dom4jUtils.newDocument();
        Pattern alphanumPattern = Pattern.compile(ALPHANUMERIC_REGEX);
        Pattern numPattern = Pattern.compile(NUMERIC_REGEX);
        Pattern dcrPattern = Pattern.compile(SALEABLE_PRODUCT_DCR_REGEX);
        try {
            int tabCounter = 0;
            int linkCounter = 0;

            String dcrRelativePath = context.getParameterString(EXTERNAL_PARAM_DCR_PATH, "");
            String categoryID = context.getParameterString(EXTERNAL_PARAM_CATEGORY_ID, "");

            String defaultProductIconImage = "/"
                    + ((context != null && context.getParameters() != null && context.getParameters().get("DefaultProductIconImage") != null) ? (String) context.getParameters().get("DefaultProductIconImage") : "");
            mLogger.createLogInfo("defaultProductIconImage=" + defaultProductIconImage);
            // TODO Push the defaultProductIconImage to in case of no product image

            paramRegexChecker(dcrPattern, dcrRelativePath);
            paramRegexChecker(alphanumPattern, categoryID);

            String waPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator();

            dcrFullPath = waPath + dcrRelativePath;
            mLogger.createLogInfo("getProductTabDetails invoked with Full DCR Path: " + dcrFullPath);

            File dcrFile = new File(dcrFullPath);

            if (dcrFile.exists()) {
                inDoc = Dom4jUtils.newDocument(dcrFile);
            }

            Element resultElement = resultDoc.addElement("ProductContent");

            Element dcrPathEl = resultElement.addElement(EXTERNAL_PARAM_DCR_PATH);
            dcrPathEl.addText(dcrRelativePath);
            Element catIdEl = resultElement.addElement("CatID");
            catIdEl.addText(categoryID);

            sampleImagesTabName = context.getParameterString(EXTERNAL_PARAM_SAMPLE_IMAGES, "Sample Images");
            keyFeaturesTabName = context.getParameterString(EXTERNAL_PARAM_KEY_FEATURES, "Key Features");
            specificationsTabName = context.getParameterString(EXTERNAL_PARAM_SPECIFICATIONS, "Specifications");
            featuresExplainedTabName = context.getParameterString(EXTERNAL_PARAM_FEATURES_EXPLAINED, "Features Explained");
            systemTabName = context.getParameterString(EXTERNAL_PARAM_SYSTEM, "System");
            relatedProductsTabName = context.getParameterString(EXTERNAL_PARAM_RELATED_PRODUCTS, "Related Products");

            String currentTab = context.getParameterString(EXTERNAL_PARAM_CURRENT_TAB, "");
            String currentLink = context.getParameterString(EXTERNAL_PARAM_CURRENT_LINK, "");
            mLogger.createLogInfo(EXTERNAL_PARAM_CURRENT_TAB + ":" + currentTab);
            mLogger.createLogInfo(EXTERNAL_PARAM_CURRENT_LINK + ":" + currentLink);

            paramRegexChecker(numPattern, currentTab);
            paramRegexChecker(numPattern, currentLink);

            Element tabsEle = resultElement.addElement("Tabs");

            HashMap<String, String> mapOfTabNames = new HashMap<String, String>();
            HashMap<String, String> mapOfLinkNames = new HashMap<String, String>();
            /**
             * KEY FEATURES tab
             */
            boolean mainFeaturesExistsInMainDCR = !isElementCompleteBlank((Element) inDoc.selectSingleNode("//saleable_product_information/main_features"), true);

            mLogger.createLogDebug("mainFeaturesExistsInMainDCR=" + mainFeaturesExistsInMainDCR);

            if (mainFeaturesExistsInMainDCR) {
                tabCounter++;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", String.valueOf(tabCounter));
                mapOfTabNames.put(String.valueOf(tabCounter), keyFeaturesTabName);
                tabEle.addAttribute("Name", keyFeaturesTabName);

                mLogger.createLogDebug("Adding tab:" + keyFeaturesTabName);

                if (StringUtils.isEmpty(currentTab)) {
                    // Choose the first tab entry as the default ta to be shown on screen
                    currentTab = String.valueOf(tabCounter);
                }

                if (tabCounter == Integer.parseInt(currentTab)) {
                    tabEle.addAttribute("Current", "True");
                }
            }


            List relatedKeyFeaturePaths = inDoc.selectNodes("//saleable_product_information/OverviewCtr/KeyFeatureDCRPath[text()!='']");
            List kfSystemRelatedPaths = new ArrayList();
            List kfSpecRelatedPaths = new ArrayList();
            filterPathsByName(relatedKeyFeaturePaths, kfSystemRelatedPaths, kfSpecRelatedPaths);

            /**
             * FEATURES EXPLAINED tab
             */
            mLogger.createLogDebug("Checking for Features Explained.");
            Document selectKFDoc = null;
            if (CollectionUtils.isNotEmpty(relatedKeyFeaturePaths)) {
                tabCounter++;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", String.valueOf(tabCounter));
                String currentTabToCheckWith;

                if (mainFeaturesExistsInMainDCR) {
                    // Key Features tab already added. Hence, now Features Explained tab is to be added.
                    mapOfTabNames.put(String.valueOf(tabCounter), featuresExplainedTabName);
                    tabEle.addAttribute("Name", featuresExplainedTabName);
                    mLogger.createLogDebug("Adding tab:" + featuresExplainedTabName);
                    if (StringUtils.isEmpty(currentTab)) {
                        // Choose the first tab entry as the default ta to be shown on screen
                        currentTab = String.valueOf(tabCounter);
                    }

                    currentTabToCheckWith = String.valueOf(tabCounter);
                } else {
                    // Key Features tab not present. Hence, now Key Features tab is to be added.
                    mapOfTabNames.put(String.valueOf(tabCounter), keyFeaturesTabName);
                    tabEle.addAttribute("Name", keyFeaturesTabName);
                    mLogger.createLogDebug("Adding tab:" + keyFeaturesTabName);
                    if (StringUtils.isEmpty(currentTab)) {
                        // Choose the first tab entry as the default ta to be shown on screen
                        currentTab = String.valueOf(tabCounter);
                    }
                    currentTabToCheckWith = String.valueOf(tabCounter);
                }

                if (StringUtils.equals(currentTab, currentTabToCheckWith)) {

                    mLogger.createLogDebug("currentTab matched with currentTabToCheckWith");

                    tabEle.addAttribute("Current", "True");
                    String linkNamePath = KEY_FEATURE_HEADLINE_LINKNAME_PATH;
                    selectKFDoc = addLinksToTab(relatedKeyFeaturePaths, waPath, tabEle, currentLink, linkNamePath, linkCounter, mapOfLinkNames);

                    mLogger.createLogDebug(tabEle.selectNodes("Link").size() + " links added to " + currentTabToCheckWith + " tab.");

                }
            }

            /**
             * SYSTEM tab
             */
            mLogger.createLogDebug("Checking for System.");

            if (CollectionUtils.isNotEmpty(kfSystemRelatedPaths)) {
                tabCounter++;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", String.valueOf(tabCounter));
                mapOfTabNames.put(String.valueOf(tabCounter), systemTabName);
                tabEle.addAttribute("Name", systemTabName);
                mLogger.createLogDebug("Adding tab:" + systemTabName);
                if (StringUtils.isEmpty(currentTab)) {
                    // Choose the first tab entry as the default ta to be shown on screen
                    currentTab = String.valueOf(tabCounter);
                }

                if (tabCounter == Integer.parseInt(currentTab)) {
                    tabEle.addAttribute("Current", "True");
                    String linkNamePath = KEY_FEATURE_HEADLINE_LINKNAME_PATH;
                    selectKFDoc = addLinksToTab(kfSystemRelatedPaths, waPath, tabEle, currentLink, linkNamePath, linkCounter, mapOfLinkNames);

                    mLogger.createLogDebug(tabEle.selectNodes("Link").size() + " links added to System tab.");
                }
            }

            /**
             * SPECIFICATIONS tab
             */
            mLogger.createLogDebug("Checking for Specifications.");

            Document kfForSpecDoc = loadKFDocumentForSpecification(kfSpecRelatedPaths, waPath);
            if (!isElementCompleteBlank((Element) inDoc.selectSingleNode("//saleable_product_information/SpecificationsCtr/category_level_1"), true) || (kfForSpecDoc != null)) {
                tabCounter++;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", String.valueOf(tabCounter));
                mapOfTabNames.put(String.valueOf(tabCounter), specificationsTabName);
                tabEle.addAttribute("Name", specificationsTabName);
                mLogger.createLogDebug("Adding tab:" + specificationsTabName);

                if (StringUtils.isEmpty(currentTab)) {
                    // Choose the first tab entry as the default ta to be shown on screen
                    currentTab = String.valueOf(tabCounter);
                }

                if (tabCounter == Integer.parseInt(currentTab)) {
                    tabEle.addAttribute("Current", "True");
                }
            }
            /**
             * SAMPLE IMAGES tab
             */
            mLogger.createLogDebug("Checking for Sample Images.");

            if (!isElementCompleteBlank((Element) inDoc.selectSingleNode("//saleable_product_information/sample_images"), true) || !isElementCompleteBlank((Element) inDoc.selectSingleNode("//saleable_product_information/sample_movies"), true)) {
                tabCounter++;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", String.valueOf(tabCounter));
                mapOfTabNames.put(String.valueOf(tabCounter), sampleImagesTabName);
                tabEle.addAttribute("Name", sampleImagesTabName);
                mLogger.createLogDebug("Adding tab:" + sampleImagesTabName);
                if (StringUtils.isEmpty(currentTab)) {
                    // Choose the first tab entry as the default ta to be shown on screen
                    currentTab = String.valueOf(tabCounter);
                }

                if (tabCounter == Integer.parseInt(currentTab)) {
                    tabEle.addAttribute("Current", "True");
                }
            }

            /**
             * RELATED PRODUCTS tab
             */
            mLogger.createLogDebug("Checking for Related Products.");
/*
 * LINES OF CODE REQUIRED TO USE THE 2-WAY RELATED PRODUCTS FEATURE
 *
            if (this.dataManager == null) {
                this.dataManager = new DataManagerImplCommon(context);
            }
            String categoryId = inDoc.selectSingleNode("//saleable_product_information/OverviewCtr/Category").getText();
            String productId = inDoc.selectSingleNode("//saleable_product_information/OverviewCtr/ProductID").getText();

            HashMap<String, HashSet> relatedProductsMap = dataManager.retrieveRelatedProducts(categoryId, productId);

            if (!relatedProductsMap.isEmpty()) {
                tabCounter++;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", String.valueOf(tabCounter));
                mapOfTabNames.put(String.valueOf(tabCounter), relatedProductsTabName);
                tabEle.addAttribute("Name", relatedProductsTabName);
                mLogger.createLogDebug("Adding tab:" + relatedProductsTabName);
                if (StringUtils.isEmpty(currentTab)) {
                    // Choose the first tab entry as the default ta to be shown on screen
                    currentTab = String.valueOf(tabCounter);
                }

                if (tabCounter == Integer.parseInt(currentTab)) {
                    tabEle.addAttribute("Current", "True");
                }
            }
*/
            if (!isElementCompleteBlank((Element) inDoc.selectSingleNode("//saleable_product_information/RelatedProductCtr"), true)) {
                tabCounter++;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", String.valueOf(tabCounter));
                mapOfTabNames.put(String.valueOf(tabCounter), relatedProductsTabName);
                tabEle.addAttribute("Name", relatedProductsTabName);
                mLogger.createLogDebug("Adding tab:" + relatedProductsTabName);
                if (StringUtils.isEmpty(currentTab)) {
                    // Choose the first tab entry as the default ta to be shown on screen
                    currentTab = String.valueOf(tabCounter);
                }

                if (tabCounter == Integer.parseInt(currentTab)) {
                    tabEle.addAttribute("Current", "True");
                }
            }

            populateCurrentTabContent(currentTab, selectKFDoc, inDoc, resultElement, kfSpecRelatedPaths, waPath, mainFeaturesExistsInMainDCR, context, mapOfTabNames, mapOfLinkNames, defaultProductIconImage);
        } catch (Exception e) {
            mLogger.createLogWarn("Error in retrieving Product Details", e);
        }
        mLogger.createLogDebug(resultDoc.asXML());
        return resultDoc;
    }

    private Document addLinksToTab(List kfSystemRelatedPaths, String waPath, Element tabEle, String currentLink, String linkNamePath, int linkCounter, HashMap<String, String> mapOfLinkNames) throws DocumentException {

        mLogger.createLogInfo("Called addLinksToTab");

        mLogger.createLogDebug("Before links added to tab:\n" + tabEle.asXML());
        Document selectKFDoc = null;
        String linkFullPath;
        File kfFile;
        Document kfDoc = null;
        int numberOfLinks = 0;
        int index = 0;
        for (Object object : kfSystemRelatedPaths) {
            Element linkPath = (Element) object;
            linkFullPath = waPath + linkPath.getText();

            mLogger.createLogDebug("linkFullPath:" + linkFullPath);

            kfFile = new File(linkFullPath);
            if (kfFile.exists()) {
                kfDoc = Dom4jUtils.newDocument(kfFile);
            }
            // Default the selected Key Features file to the first available. If more links are found, then it will be updated.
            if (index == 0) {
                selectKFDoc = kfDoc;
            }

            Element linkNameEle = (Element) kfDoc.selectSingleNode(linkNamePath);

            // Iff the <headline> element contains a non-empty String, then Link is added to the tab.
            if (linkNameEle != null && StringUtils.isNotEmpty(linkNameEle.getText())) {
                linkCounter++;
                Element linkEle = tabEle.addElement("Link").addAttribute("id", String.valueOf(linkCounter));
                String linkName = linkNameEle.getText();
                mapOfLinkNames.put(String.valueOf(linkCounter), linkName);
                linkEle.addAttribute("Name", linkName);
                if (numberOfLinks == 0 && StringUtils.isEmpty(currentLink)) {
                    // Either first link
                    currentLink = String.valueOf(linkCounter);
                    linkEle.addAttribute("Current", "True");
                    mLogger.createLogDebug("selectKFDoc=" + linkFullPath);
                    selectKFDoc = kfDoc;
                } else if (linkCounter == Integer.parseInt(currentLink)) {
                    // Or selected link
                    linkEle.addAttribute("Current", "True");
                    mLogger.createLogDebug("selectKFDoc=" + linkFullPath);
                    selectKFDoc = kfDoc;
                }
                numberOfLinks++;
            }
            index++;
        }
        mLogger.createLogDebug("After links added to tab:\n" + tabEle.asXML());
        return selectKFDoc;
    }

    private void populateCurrentTabContent(String currentTab, Document selectKFDoc, Document inDoc, Element resultElement, List kfSpecRelatedPaths, String waPath, boolean mainFeaturesExistsInMainDCR, RequestContext context, HashMap<String, String> mapOfTabs, HashMap<String, String> mapOfLinks, String defaultProductIconImage) {

        mLogger.createLogInfo("populateCurrentTabContent:currentTab=" + currentTab);

        Element currentTabEle = resultElement.addElement("CurrentTabContent");
        Element productContent = currentTabEle.addElement("TabContent");

        String currentTabName = (String) mapOfTabs.get(currentTab);

        currentTabEle.addAttribute("Name", currentTabName);

        if (StringUtils.equalsIgnoreCase(currentTabName, relatedProductsTabName)) {
            if (this.dataManager == null) {
                this.dataManager = new DataManagerImplCommon(context);
            }
            List<Element> relatedElements = inDoc.selectNodes("//saleable_product_information/RelatedProductCtr");

            HashMap mapOfRelatedProducts = new HashMap();
            HashMap mapOfProductIds = new HashMap();
            for (Element element : relatedElements) {
                Element relatedProductResultElement = new DOMElement("RelatedProduct");

                String relCategoryName = element.selectSingleNode("RelatedProductCategoryName").getText();
                String relCategoryId = element.selectSingleNode("RelatedProductCategory").getText();
                // TODO: The categories should be sorted as per sitemap locations. L3 first, then L1.

                String relProductIds = element.selectSingleNode("RelatedProductName").getText();
                String[] relProductArray = relProductIds.split(", ");
                List productIdList = Arrays.asList(relProductArray);

                String locale = (context != null && context.getSite().getName() != null && context.getSite().getName() != null) ? (String) context.getSite().getName()
                        : " ";
                List<Products> products = dataManager.retrieveProductsByIds(productIdList, locale);
                // This is a sorted product list based on WWA Date.

                Element categoryResultElement = relatedProductResultElement.addElement("CategoryName");
                categoryResultElement.addText(relCategoryName);

                for (Products product : products) {
                    String productKey = product.getProductID();
                    if (!mapOfProductIds.containsKey(productKey)) {
                        mapOfProductIds.put(productKey, productKey);
                        Element productResultElement = relatedProductResultElement.addElement("Product");
                        productResultElement.addElement("Name").addText(product.getProductName());
                        productResultElement.addElement("Image").addText(StringUtils.isNotEmpty(product.getImage()) ? product.getImage() : defaultProductIconImage);

                        mLogger.createLogDebug("getReleaseDate=" + product.getReleaseDate());

                        productResultElement.addAttribute("ReleaseDate", product.getReleaseDate().toString());
                        productResultElement.addElement(EXTERNAL_PARAM_DCR_PATH).addText(product.getDcrPath());
                        productResultElement.addElement(EXTERNAL_PARAM_CATEGORY_ID).addText(product.getCategory().getCategoryID());
                    }
                }
                addToRelatedProductsMap(mapOfRelatedProducts, relCategoryId, relatedProductResultElement);
            }
            sortAddBySitemap(mapOfRelatedProducts, productContent, context); // To return a sorted Element list based on Category Ids located on the sitemap.
        } else if (StringUtils.equalsIgnoreCase(currentTabName, sampleImagesTabName)) {
            productContent.add(((Element) (inDoc.selectSingleNode("//saleable_product_information/sample_images"))).createCopy());
            productContent.add(((Element) (inDoc.selectSingleNode("//saleable_product_information/sample_movies"))).createCopy());
        } else if (StringUtils.equalsIgnoreCase(currentTabName, specificationsTabName)) {
            productContent.add(((Element) (inDoc.selectSingleNode("//saleable_product_information/SpecificationsCtr"))).createCopy());
            Document kfForSpecDoc = loadKFDocumentForSpecification(kfSpecRelatedPaths, waPath);
            if (kfForSpecDoc != null) {
                productContent.add(((Element) (kfForSpecDoc.selectSingleNode("//key_feature"))).createCopy());
            }

        } else if (StringUtils.equalsIgnoreCase(currentTabName, keyFeaturesTabName)) {

            if (mainFeaturesExistsInMainDCR) {
                // Key Features tab has resulted because of the presence of "main_features" content.
                mLogger.createLogDebug("Key Features tab has resulted because of the presence of main_features content.");
                productContent.add(((Element) (inDoc.selectSingleNode("//saleable_product_information/main_features"))).createCopy());
                productContent.add(((Element) (inDoc.selectSingleNode("//saleable_product_information/lens"))).createCopy());
            } else {
                // The other condition of Key Features tab is when the related files are present. Hence, setting the relevant file.
                mLogger.createLogDebug("The other condition of Key Features tab is when the related files are present. Hence, setting the relevant file.");
                productContent.add(((Element) (selectKFDoc.selectSingleNode("//key_feature"))).createCopy());
            }

        } else if (StringUtils.equalsIgnoreCase(currentTabName, featuresExplainedTabName)) {
            productContent.add(((Element) (selectKFDoc.selectSingleNode("//key_feature"))).createCopy());

        } else if (StringUtils.equalsIgnoreCase(currentTabName, systemTabName)) {
            productContent.add(((Element) (selectKFDoc.selectSingleNode("//key_feature"))).createCopy());

        }
    }

    /**
     * This method will filter out all the key-features file names which do not have numerical ends to it
     * For example Q0740_01.xml will stay in the list. Whereas Q0740_exspec_compatibility01.xml
     *
     * @param relatedKeyFeaturePaths
     */
    private void filterPathsByName(List relatedKeyFeaturePaths, List kfSystemRelatedPaths, List kfSpecRelatedPaths) {

        List dropList = new ArrayList();

        for (Object object : relatedKeyFeaturePaths) {
            Element linkPath = (Element) object;

            String pathStr = linkPath.getText().trim();

            mLogger.createLogInfo("filterPathsByName pathStr=" + pathStr);

            String relatedFileNameWithExtension = pathStr.substring(pathStr.lastIndexOf("/") + 1, pathStr.length());
            mLogger.createLogDebug("patproductDevCodehStr=" + relatedFileNameWithExtension);

            if (relatedFileNameWithExtension.indexOf("_") >= 0) {
                // This indexOf check would ensure that files named as <ProductDevCode>.xml is not dropped.
                String checkForNumericalPart = relatedFileNameWithExtension.substring(relatedFileNameWithExtension.indexOf("_") + 1, relatedFileNameWithExtension.indexOf(".xml"));
                mLogger.createLogDebug("checkForNumericalPart=" + checkForNumericalPart);

                if (checkForNumericalPart == null || checkForNumericalPart.length() == 0) {
                    //If we find a blank String drop it from the list
                    dropList.add(linkPath);
                } else if (checkForNumericalPart.equalsIgnoreCase(EXSPEC_SPEC_FILENAME)) {

                    // Check for Related Key Features file used in Specifications Tab.
                    dropList.add(linkPath);
                    kfSpecRelatedPaths.add(linkPath);
                } else if (checkForNumericalPart.contains(EXSPEC_COMPATIBILITY_FILENAME)) {

                    // Check for Related Key Features file used in System Tab.
                    dropList.add(linkPath);
                    kfSystemRelatedPaths.add(linkPath);
                } else if (StringUtils.isEmpty(pathStr)) {
                    // Blank entries need to be removed
                    dropList.add(linkPath);
                } else {
                    int i = 0;
                    boolean isValid = true;
                    while (isValid && i < checkForNumericalPart.length()) {

                        //If we find a non-digit character drop it from the list
                        if (!Character.isDigit(checkForNumericalPart.charAt(i))) {
                            dropList.add(linkPath);
                            isValid = false;
                        }
                        i++;
                    }
                }
            }
        }
        if (!dropList.isEmpty()) {
            relatedKeyFeaturePaths.removeAll(dropList);
        }

        mLogger.createLogInfo("filterPathsByName method completed successfully.");
    }

    private Document loadKFDocumentForSpecification(List kfSpecRelatedPaths, String waPath) {
        Document kfDoc = null;
        if (!kfSpecRelatedPaths.isEmpty()) {
            Element kfForSpecPathEle = (Element) kfSpecRelatedPaths.get(0);
            String linkFullPath = waPath + kfForSpecPathEle.getText();
            File kfFile = new File(linkFullPath);
            if (kfFile.exists()) {
                kfDoc = Dom4jUtils.newDocument(kfFile);
            }
        }
        return kfDoc;
    }

    private static boolean isElementCompleteBlank(Element element, boolean isBlank) {

        if (element != null) {
            for (int i = 0, size = element.nodeCount(); i < size; i++) {
                Node node = element.node(i);
                if (node instanceof Element) {
                    isBlank = isElementCompleteBlank((Element) node, isBlank);
                } else {
                    String value = element.getText();

                    if (isBlank) {
                        isBlank = StringUtils.isEmpty(value.trim());
                    }
                }
            }
        }
        return isBlank;
    }

    private void reattachSortedProducts(Element relatedProductElement, List<Element> products) {
        for (Iterator<Element> it = relatedProductElement.elementIterator("Product"); it.hasNext();) {
            Element element = it.next();
            relatedProductElement.remove(element);
        }
        for (Iterator<Element> it = products.iterator(); it.hasNext();) {
            Element element = it.next();
            relatedProductElement.add(element);
        }
    }

    private void sortAddBySitemap(HashMap mapOfRelatedProducts, Element productContent, RequestContext context) {
        Set<String> keys = mapOfRelatedProducts.keySet();

        String sitemapPath = context.getSite().getPath() + "/default.sitemap";
        mLogger.createLogInfo("Sitemap Path: " + sitemapPath);
        File sitemapFile = new File(sitemapPath);
        Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);
        String productsLabel = context.getParameterString(EXTERNAL_PARAM_PRODUCT_LABEL, "Products");

        List<Node> l5Nodes = sitemapFileDocument.selectNodes("//site-map/segment/node[label='" + productsLabel + "']/node/node/node/node/node");
        List<Node> l4Nodes = sitemapFileDocument.selectNodes("//site-map/segment/node[label='" + productsLabel + "']/node/node/node/node");
        List<Node> l3Nodes = sitemapFileDocument.selectNodes("//site-map/segment/node[label='" + productsLabel + "']/node/node/node");
        List<Node> l2Nodes = sitemapFileDocument.selectNodes("//site-map/segment/node[label='" + productsLabel + "']/node/node");
        List<Node> l1Nodes = sitemapFileDocument.selectNodes("//site-map/segment/node[label='" + productsLabel + "']/node");

        checkAndAddToContent(l5Nodes, mapOfRelatedProducts, productContent);
        checkAndAddToContent(l4Nodes, mapOfRelatedProducts, productContent);
        checkAndAddToContent(l3Nodes, mapOfRelatedProducts, productContent);
        checkAndAddToContent(l2Nodes, mapOfRelatedProducts, productContent);
        checkAndAddToContent(l1Nodes, mapOfRelatedProducts, productContent);
    }

    private void checkAndAddToContent(List<Node> nodes, HashMap mapOfRelatedProducts, Element productContent) {
        if (nodes != null) {
            for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
                Node node = (Node) iterator.next();
                String id = node.valueOf("@id");
                if (mapOfRelatedProducts.containsKey(id)) {
                    Element relatedProductElement = (Element) mapOfRelatedProducts.get(id);
                    List<Element> products = relatedProductElement.selectNodes("Product");
                    Collections.sort(products, ProductElementSort.RELEASE_DATE);
                    reattachSortedProducts(relatedProductElement, products);
                    productContent.add((Element) mapOfRelatedProducts.get(id));
                }
            }
        }
    }

    private void addToRelatedProductsMap(HashMap mapOfRelatedProducts, String relCategoryId, Element relatedProductResultElement) {
        if (mapOfRelatedProducts.containsKey(relCategoryId)) {
            Element currentResultElement = (Element) mapOfRelatedProducts.get(relCategoryId);

            List products = relatedProductResultElement.selectNodes("Product");
            if (CollectionUtils.isNotEmpty(products)) {
                for (Object object : products) {
                    Element prodEle = ((Element) (object)).createCopy();
                    currentResultElement.add(prodEle);
                }
            }
        } else {
            // Add the relatedProductResultElement only when there is atleast a product within
            List products = relatedProductResultElement.selectNodes("Product");
            if (CollectionUtils.isNotEmpty(products)) {
                mapOfRelatedProducts.put(relCategoryId, relatedProductResultElement);
            }
        }
    }

    private void paramRegexChecker(Pattern p, String str) throws Exception {
        Matcher m = p.matcher(str);
        if (!m.matches()) {
            throw new Exception("Invalid parameter value set.");
        }
    }
}
