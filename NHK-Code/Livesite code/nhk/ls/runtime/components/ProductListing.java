package nhk.ls.runtime.components;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;

import nhk.ls.runtime.common.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nhk.ls.runtime.common.DateUtils;
import nhk.ls.runtime.common.ProductElementSort;
import nhk.ls.runtime.common.ProductListingHelper;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import nhk.ls.runtime.dao.ProductCategory;

public class ProductListing {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ProductListing"));
    private DataManager dataManager;
    //private static Document doc;
    private String productCategoryID;
    //private String productCategoryName;
    private String overviewTabName;
    private int productDaysParam;
    private int pageSize;
    private int page;
    private int sortBy;
    public static final String SORT_BY_DATE_OPTION_VALUE = "1";
    public static final String SORT_BY_PRICE_DESC_OPTION_VALUE = "2";
    public static final String SORT_BY_PRICE_ASC_OPTION_VALUE = "3";
    public static final String EXTERNAL_PARAM_CURRENT_TAB = "currentTab";
    public static final String EXTERNAL_PARAM_CURRENT_LINK = "currentLink";
    private static final String EXTERNAL_PARAM_NEW_PRODUCT = "NewProductDaysConfig";
    private static final String EXTERNAL_PARAM_OVERVIEW = "OverviewTabName";
    private static final String EXTERNAL_PARAM_PAGE_SIZE = "PageSize";
    private static final String EXTERNAL_PARAM_PAGE = "Page";
    private static final String EXTERNAL_PARAM_SORTBY = "SortBy";
    public static final String EXTERNAL_PARAM_CATEGORYID = "CategoryID";
    private static final String OVERVIEW_TAB_IDENTIFIER = "1";
    private static final String NUMERIC_REGEX = "^[0-9]*";
    private static final String ALPHANUMERIC_REGEX = "^[a-zA-Z0-9]*";

    public Document getProductList(RequestContext context) throws IOException {

        Pattern numPattern = Pattern.compile(NUMERIC_REGEX);
        Pattern alphanumPattern = Pattern.compile(ALPHANUMERIC_REGEX);
        
        Document doc = null;

        Document resultDoc = Dom4jUtils.newDocument();
        try {

            String defaultProductIconImage =
                    (context != null && context.getParameters() != null && context.getParameters().get("DefaultProductIconImage") != null) ? (String) context.getParameters().get("DefaultProductIconImage") : "";
            mLogger.createLogInfo("defaultProductIconImage=" + defaultProductIconImage);
            // TODO Push the defaultProductIconImage to in case of no product image

            overviewTabName = context.getParameterString(EXTERNAL_PARAM_OVERVIEW, "Overview");
            productDaysParam = Integer.valueOf(context.getParameterString(EXTERNAL_PARAM_NEW_PRODUCT, "90")); // Default no. of days for product to be considered new is 90.

            // Used for pagination
            pageSize = Integer.valueOf(context.getParameterString(EXTERNAL_PARAM_PAGE_SIZE, "9")); // Default results per page is 9. Generally chosen by user from an option list of 9,18,27.
            page = Integer.valueOf(context.getParameterString(EXTERNAL_PARAM_PAGE, "1")); // Default value of current page is 1.

            paramRegexChecker(numPattern, pageSize + "");
            paramRegexChecker(numPattern, page + "");

            Document dummyProductDoc = Dom4jUtils.newDocument();
            Element dummyProductRootEle = dummyProductDoc.addElement("ROOT");

            // Used for sorting
            sortBy = Integer.valueOf(context.getParameterString(EXTERNAL_PARAM_SORTBY, SORT_BY_DATE_OPTION_VALUE)); // Default sorting order is by release date.
            paramRegexChecker(numPattern, sortBy + "");


            productCategoryID = context.getParameterString(EXTERNAL_PARAM_CATEGORYID, "");
            mLogger.createLogInfo("ProductListing catId=" + productCategoryID);
            paramRegexChecker(alphanumPattern, productCategoryID);

            Element resultElement = resultDoc.addElement("ProductListing");
            Element tabsEle = resultElement.addElement("Tabs");
            String currentTab = (context != null && context.getParameters() != null && context.getParameters().get(EXTERNAL_PARAM_CURRENT_TAB) != null) ? (String) context.getParameters().get(EXTERNAL_PARAM_CURRENT_TAB) : "";
            String currentLink = (context != null && context.getParameters() != null && context.getParameters().get(EXTERNAL_PARAM_CURRENT_LINK) != null) ? (String) context.getParameters().get(EXTERNAL_PARAM_CURRENT_LINK) : "";

            mLogger.createLogInfo(EXTERNAL_PARAM_CURRENT_TAB + ":" + currentTab);
            mLogger.createLogInfo(EXTERNAL_PARAM_CURRENT_LINK + ":" + currentLink);

            paramRegexChecker(alphanumPattern, currentTab);
            paramRegexChecker(alphanumPattern, currentLink);

            // First tab would be Overview, if it exists.
            this.dataManager = new DataManagerImplCommon(context);
            ProductCategory category = dataManager.retrieveCategoryByID(productCategoryID, false);

            if (category.getOverviewRequired().equalsIgnoreCase("Yes") && StringUtils.isNotEmpty(category.getOverviewText())) {

                mLogger.createLogDebug(currentTab.equalsIgnoreCase(StringUtils.EMPTY) + "");
                if (currentTab.equalsIgnoreCase(StringUtils.EMPTY)) {
                    mLogger.createLogDebug("Setting currentTab to:" + overviewTabName);
                    currentTab = OVERVIEW_TAB_IDENTIFIER; // For the overview tab
                }
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", OVERVIEW_TAB_IDENTIFIER);
                if (currentTab.equalsIgnoreCase(OVERVIEW_TAB_IDENTIFIER)) {
                    mLogger.createLogDebug("Added tab:" + currentTab + " with current=true");
                    tabEle.addAttribute("Current", "True");
                    tabEle.addAttribute("Name", overviewTabName);
                } else {
                    mLogger.createLogDebug("Added tab:" + OVERVIEW_TAB_IDENTIFIER);
                    tabEle.addAttribute("Name", overviewTabName);
                }
            }


            //Create File if it doesnt exist
            String FilePath = context.getFileDAL().getRoot() + context.getFileDAL().getSeparator() + "productlisting" + context.getFileDAL().getSeparator() + context.getSite().getName();
            //   File catFile = new File(Constants.getCatFileLocation(context) + File.separator + productCategoryID + ".xml");
            File catFile = new File(FilePath + File.separator + productCategoryID + ".xml");
            mLogger.createLogDebug("CatFile: " + catFile.getName());
            if (!catFile.exists()) {
                mLogger.createLogDebug("Creating file:::");
                mLogger.createLogDebug("Creating new file:: ");
                doc = Dom4jUtils.newDocument();
                ProductListingHelper.createCatFile(context, catFile, doc, productCategoryID, defaultProductIconImage);
            } else {
                doc = Dom4jUtils.newDocument(catFile);
            }
            //Process the file and create current tab result dom object
            Element categoryIDElement = resultElement.addElement("CatID");
            categoryIDElement.addText(productCategoryID);

            String currentTabName = doc.selectSingleNode("//L2Category/@Name") != null ? doc.selectSingleNode("//L2Category/@Name").getText() : "";
            String catId = doc.selectSingleNode("//L2Category/@Id") != null ? doc.selectSingleNode("//L2Category/@Id").getText() : "";
            resultElement.addAttribute("CatName", currentTabName);

            boolean l3Exists = (doc.selectSingleNode("//L3") != null);

            if (StringUtils.isEmpty(currentTab)) {
                if (l3Exists) {
                    mLogger.createLogDebug("currentTab value set to:" + doc.selectSingleNode("//L3/Category/@Id").getText());
                    currentTab = doc.selectSingleNode("//L3/Category/@Id").getText();
                    currentTabName = doc.selectSingleNode("//L3/Category/@Name").getText();
                } else {
                    mLogger.createLogDebug("currentTab value set to:" + catId);
                    currentTab = catId;
                }
            }
            if (l3Exists) {
                List tabEls = doc.selectNodes("//L3/Category");

                for (Object object : tabEls) {

                    mLogger.createLogDebug("Within tabEls loop");
                    Element tabEL = (Element) object;
                    if (Dom4jUtils.newDocument(tabEL.asXML()).selectNodes("//Product[@Archived='No']").size() > 0) {
                        mLogger.createLogDebug(Dom4jUtils.newDocument(tabEL.asXML()).selectNodes("//Product[@Archived='No']").size() + "non-archived products found within this tab. Hence adding this tab.");
                        String tabId = tabEL.selectSingleNode("@Id").getText();
                        Element tabEle = tabsEle.addElement("Tab").addAttribute("id", tabId);
                        if (currentTab.equalsIgnoreCase(tabId)) {
                            mLogger.createLogDebug("Added tab:" + currentTab + " with current=true");
                            tabEle.addAttribute("Current", "True");
                            tabEle.addAttribute("Name", tabEL.selectSingleNode("@Name").getText());
                        } else {
                            mLogger.createLogDebug("Added tab:" + tabEL.selectSingleNode("@Name").getText());
                            tabEle.addAttribute("Name", tabEL.selectSingleNode("@Name").getText());
                        }
                    }
                }
            } else {
                String tabId = catId;
                Element tabEle = tabsEle.addElement("Tab").addAttribute("id", tabId);
                tabEle.addAttribute("Name", currentTabName);
                if (currentTab.equalsIgnoreCase(tabId)) {
                    tabEle.addAttribute("Current", "True");
                }
            }
            Element currentTabResultEle = resultElement.addElement("CurrentTabContent");
            if (!OVERVIEW_TAB_IDENTIFIER.equalsIgnoreCase(currentTab)) {
                List products;
                mLogger.createLogDebug("parsing //L3/Category[@Id='" + currentTab + "']/*");
                Element currentTabEl = l3Exists ? (Element) doc.selectSingleNode("//L3/Category[@Id='" + currentTab + "']/*") : (Element) doc.selectSingleNode("//L2Category/*");
                currentTabResultEle.addAttribute("Name", currentTabName);

                // Create Top Nav Links and Products list for categories with L3/L4/L5
                // sub-categories

                if (currentTabEl == null) {
                    currentTabResultEle.addElement("ErrorText").setText("No products to be displayed");
                } else if (l3Exists) {
                    List subCats = currentTabEl.selectNodes("../L4Category");
                    if (CollectionUtils.isNotEmpty(subCats)) {
                        Element topNavLinks = currentTabResultEle.addElement("TopNavLinks");

                        for (Object object : subCats) {
                            mLogger.createLogDebug("Within sub-category links");
                            Element subCat = (Element) object;

                            mLogger.createLogDebug("LinkName=" + subCat.selectSingleNode("@Id").getText());

                            if (Dom4jUtils.newDocument(subCat.asXML()).selectNodes("//Product[@Archived='No']").size() > 0) {
                                mLogger.createLogDebug(Dom4jUtils.newDocument(subCat.asXML()).selectNodes("//Product[@Archived='No']").size() + " non-archived products found within this link. Hence, this link is added.");
                                String linkId = subCat.selectSingleNode("@Id").getText();
                                Element topNavLink = topNavLinks.addElement("TopNavLink").addAttribute("id", linkId);

                                if (StringUtils.isEmpty(currentLink)) {
                                    currentLink = subCat.selectSingleNode("@Id").getText();
                                }

                                if (currentLink.equalsIgnoreCase(linkId)) {
                                    topNavLink.addAttribute("Name", subCat.selectSingleNode("@Name").getText());
                                    topNavLink.addAttribute("Current", "True");
                                } else {
                                    topNavLink.addAttribute("Name", subCat.selectSingleNode("@Name").getText());
                                }
                            }
                        }

                        products = currentTabEl.selectNodes("../L4Category[@Id='"
                                + currentLink + "']//Product[@Archived='No']");
                        if (CollectionUtils.isNotEmpty(products)) {
                            List subCats2 = currentTabEl.selectNodes("../L4Category[@Id='" + currentLink
                                    + "']/L5Category/Products");
                            if (CollectionUtils.isNotEmpty(subCats2)) {
                                mLogger.createLogDebug("Scenario with L3, L4 and L5 sub-categories");
                                addL5CategoryAndProducts(currentTabEl, dummyProductRootEle, currentLink);

                                /*for (Object object : subCats2) {
                                // Scenario with L3, L4 and L5 sub-categories
                                addProductContent((Element) object, dummyProductRootEle);
                                }*/
                            } else {
                                // Scenario with L3, L4 but no L5 sub-categories
                                mLogger.createLogDebug("Scenario with L3, L4 but no L5 sub-categories");
                                addProductContent((Element) currentTabEl.selectSingleNode("../L4Category[@Id='"
                                        + currentLink + "']/Products"), dummyProductRootEle);
                            }


                        }
                    } else {// Scenario with L3 without L4/L5 sub-categories
                        mLogger.createLogDebug("Scenario with L3 without L4/L5 sub-categories");
                        addProductContent(currentTabEl, dummyProductRootEle);
                    }
                } else {// Scenario without L3/L4/L5 sub-categories e.g. Digital SLR cameras
                    mLogger.createLogDebug("Scenario without L3/L4/L5 sub-categories e.g. Digital SLR cameras");
                    addProductContent(currentTabEl, dummyProductRootEle);
                }
                Element productContentResultEle = currentTabResultEle.addElement("ProductContent");
                Element leftNavProductContentResultEle = currentTabResultEle.addElement("LeftNavProductContent");
                int numberOfProducts = 0;
                int numberOfProductsForPagination = 0;

                List listOf5Categories = dummyProductRootEle.selectNodes("//L5Category");

                if (CollectionUtils.isNotEmpty(listOf5Categories)) {

                    for (Object object : listOf5Categories) {
                        Element l5Cat = ((Element) object);

                        mLogger.createLogDebug("CHECK L5CAT //L5Category/Product:::" + l5Cat.asXML());

                        List listOfL5Products = l5Cat.selectNodes("Product");
                        // Setting the Product as per pagination.
                        if (listOfL5Products != null) {
                            int numberOfL5Products = listOfL5Products.size();
                            numberOfProductsForPagination = Math.max(numberOfL5Products, numberOfProductsForPagination);
                            mLogger.createLogDebug("A total of " + numberOfL5Products + " products found in " + l5Cat.selectSingleNode("@Name").getText() + " l5 category.About to sort and then paginate.");
                            mLogger.createLogDebug("numberOfProductsForPagination set to:" + numberOfProductsForPagination);
                            switch (sortBy) {
                                case 1:
                                    Collections.sort(listOfL5Products, ProductElementSort.RELEASE_DATE);
                                    break;
                                case 2:
                                    Collections.sort(listOfL5Products, ProductElementSort.PRICE_DESCENDING);
                                    break;
                                case 3:
                                    Collections.sort(listOfL5Products, ProductElementSort.PRICE_ASCENDING);
                                    break;
                            }
                            List l5productsForPage = paginateResults(listOfL5Products);
                            if (l5productsForPage.size() > 0) {
                                Element l5ResultElement = productContentResultEle.addElement("L5Category").addAttribute("Name", l5Cat.selectSingleNode("@Name").getText());
                                l5ResultElement.setContent(l5productsForPage);
                            }
                        }
                    }
                    leftNavProductContentResultEle.setContent(listOf5Categories);
                    numberOfProducts = leftNavProductContentResultEle.selectNodes("//LeftNavProductContent/L5Category/Product").size();
                } else {

                    List listOfProducts = dummyProductRootEle.selectNodes("//Product");
                    // Setting the Product as per pagination.
                    if (listOfProducts != null) {
                        numberOfProducts = listOfProducts.size();
                        numberOfProductsForPagination = numberOfProducts;
                        mLogger.createLogDebug("A total of " + numberOfProducts + " products found.About to sort and then paginate.");
                        switch (sortBy) {
                            case 1:
                                Collections.sort(listOfProducts, ProductElementSort.RELEASE_DATE);
                                break;
                            case 2:
                                Collections.sort(listOfProducts, ProductElementSort.PRICE_DESCENDING);
                                break;
                            case 3:
                                Collections.sort(listOfProducts, ProductElementSort.PRICE_ASCENDING);
                                break;
                        }
                        leftNavProductContentResultEle.setContent(listOfProducts);
                        List productsForPage = paginateResults(listOfProducts);
                        productContentResultEle.setContent(productsForPage);
                    }
                }
                // Setting the total number of results.
                productContentResultEle.addElement("ProductCount").addText(Integer.toString(numberOfProducts));
                productContentResultEle.addElement("ProductCountForPagination").addText(Integer.toString(numberOfProductsForPagination));
                resultElement.addElement("PageSize").addText(String.valueOf(pageSize));
                resultElement.addElement("Page").addText(String.valueOf(page));
                resultElement.addElement("SortBy").addText(String.valueOf(sortBy));
            } else {
                currentTabResultEle.addText(category.getOverviewText());
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating Product List", e);
        }
        mLogger.createLogDebug("Product Listing Content XML Value: " + resultDoc.asXML());
        return resultDoc;
    }

    private void paramRegexChecker(Pattern p, String str) throws Exception {
        Matcher m = p.matcher(str);
        if (!m.matches()) {
            throw new Exception("Invalid parameter value set.");
        }
    }

    private void addProductContent(Element currentTabEl, Element dummyProductRootEle) {
        if (currentTabEl != null) {
            List products = currentTabEl.selectNodes("Product");
            if (CollectionUtils.isNotEmpty(products)) {
                for (Object object : products) {
                    Element prodEle = ((Element) (object)).createCopy();

                    if (prodEle.selectSingleNode("@Archived") != null && prodEle.selectSingleNode("@Archived").getText().equalsIgnoreCase("No")) {
                        prodEle.addAttribute("New", Boolean.toString(setNewFlag(prodEle.selectSingleNode("@ReleaseDate").getText())));
                        mLogger.createLogDebug("Product element added:" + prodEle.asXML());
                        dummyProductRootEle.add(prodEle);
                    }
                }
            }
        }
    }

    private boolean setNewFlag(String dateStr) {
        mLogger.createLogInfo("To setNewFlag, passed date value:" + dateStr);
        Date wwaDate = DateUtils.getDate(dateStr, DateUtils.DATE_FORMAT_WITH_SECONDS);
        Date now = DateUtils.currentdatetime();
        int diff = DateUtils.getDaysDifference(wwaDate, now);
        mLogger.createLogDebug("DATE DIFF::" + diff);
        mLogger.createLogDebug("ASSIGN VALUE OF:" + ((diff < productDaysParam) && (diff > 0)));
        return ((diff < productDaysParam) && (diff >= 0));
    }

    private List paginateResults(List listOfProducts) {

        List pagedData = new ArrayList();

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(listOfProducts.size(), page * pageSize);
        mLogger.createLogInfo("Reading from the " + startIndex + "th product to the " + endIndex + "th. A total of " + (endIndex - startIndex) + " products read.");

        if (startIndex <= endIndex) {
            pagedData = listOfProducts.subList(startIndex, endIndex);
        }
        return pagedData;
    }

    private void addL5CategoryAndProducts(Element currentTabEl, Element dummyProductRootEle, String currentLink) {

        List l5Categories = currentTabEl.selectNodes("../L4Category[@Id='" + currentLink
                + "']/L5Category");

        if (CollectionUtils.isNotEmpty(l5Categories)) {
            for (Object object : l5Categories) {

                Element l5Cat = ((Element) object);

                Element l5CategoryResultElement = dummyProductRootEle.addElement("L5Category").addAttribute("Name", l5Cat.selectSingleNode("@Name").getText());

                List subCats2 = ((Element) object).selectNodes("Products");
                if (CollectionUtils.isNotEmpty(subCats2)) {
                    for (Object object1 : subCats2) {
                        addProductContent((Element) object1, l5CategoryResultElement);
                    }
                }
            }
        }
    }
}
