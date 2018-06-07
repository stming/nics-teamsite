/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.dao;

import java.util.Date;
import java.util.Set;
import nhk.ls.runtime.common.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.LogFactory;
import com.interwoven.livesite.dao.SearchArguments;
import com.interwoven.livesite.dao.DataAccessObject;
import com.interwoven.livesite.dao.SearchArgument;
import com.interwoven.livesite.external.ExternalUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.servlet.RequestUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author rmantrav
 */
public class DataManagerImplCommon implements DataManager {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.dao.DataManagerImplCommon"));
    private RequestContext context;

    public DataManagerImplCommon(RequestContext context) {
        super();
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public void checkAndRetrieveOverviewText(String productCategoryID, String overViewText, String productCategoryName) throws DAOException {
        boolean overViewRequired = false;
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);
        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }
        args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, productCategoryID));
        ProductCategory parentCat = (ProductCategory) prodCatDAO.findBySearchArguments(args).get(0);
        if (parentCat != null) {
            overViewRequired = Boolean.parseBoolean(parentCat.getOverviewRequired());
            if (overViewRequired) {
                overViewText = parentCat.getOverviewText();
            }
            productCategoryName = parentCat.getCategoryName();
        }

    }

    @SuppressWarnings("unchecked")
    public List<ProductCategory> retrieveCategoryByParentID(String productCategoryID) throws DAOException {
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);

        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }
        mLogger.createLogInfo("Parent Cat ID:::" + productCategoryID);
        args.add(SearchArgument.create("parentCategoryID", SearchArgument.EQUAL, productCategoryID));
        return prodCatDAO.findBySearchArguments(args);
    }

    @SuppressWarnings("unchecked")
    public ProductCategory checkAndRetrieveCategoryByID(String productCategoryID) throws DAOException {
        ProductCategory result = null;

        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);

        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }

        args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, productCategoryID));

        ProductCategory category = (ProductCategory) prodCatDAO.findBySearchArguments(args).get(0);
        String flashBanner = category.getFlashBannerPath();

        if (flashBanner != null && flashBanner.length() != 0) {
            // Level 1, 2 or 3 Category. Hence, flashbanner found and returned
            result = category;
        } else {
            // Retrieve by parentID to get the appropriate category for display
            result = retrieveCategoryByID(category.getParentCategoryID(), true);
            if (result == null) {
                // Have to move one level further up to return level 3 category
                result = retrieveCategoryByID(category.getGrandParentCategoryID(), true);
            }
        }
        return result;
    }

    public ProductCategory retrieveCategoryByID(String categoryID, boolean checkForFlashBanner) {
        ProductCategory result = null;
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }
        args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, categoryID));
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);
        List<ProductCategory> prodCats = prodCatDAO.findBySearchArguments(args);


        if (CollectionUtils.isNotEmpty(prodCats)) {
            if (checkForFlashBanner) {
                String flashBanner = prodCats.get(0).getFlashBannerPath();
                if (flashBanner != null && flashBanner.length() != 0) {
                    // This is Level 3 Category. Hence, result found with search by flashbanner
                    result = prodCats.get(0);
                }
            } else {
                result = prodCats.get(0);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Products> retrieveProductsByCategory(String productCategoryID) throws DAOException {
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodDAO = RequestUtils.getDaoFactory().getInstance(Products.class);
        args.clear();
        Date currentdate = new Date();
        mLogger.createLogInfo("Product cat ID:::" + productCategoryID);
        mLogger.createLogDebug("Product status:::Yes" + productCategoryID);
        args.add(SearchArgument.create("status", SearchArgument.EQUAL, "Yes"));
        args.add(SearchArgument.create("releaseDate", 5, currentdate));
        args.add(SearchArgument.create("category.categoryID", SearchArgument.EQUAL, productCategoryID));

        mLogger.createLogInfo("retrieveProductsByCategory::" + args.toString());
        return prodDAO.findBySearchArguments(args);
    }

    public List<NewsData> retrieveAllNewsList(String locale) throws DAOException {
        DataAccessObject newsDao = RequestUtils.getDaoFactory().getInstance(NewsData.class);
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        Date currentdate = new Date();
        args.add(SearchArguments.eq("locale", locale));
        args.add(SearchArguments.orderByDesc("date"));
        args.add(SearchArgument.create("startDate", 5, currentdate));
        args.add(SearchArgument.create("endDate", 6, currentdate));
        mLogger.createLogDebug(args.toString());
        return newsDao.findBySearchArguments(args);
    }

    public List<NewsData> retrieveNewsByCategoryList(String newsCategory, String locale)
            throws DAOException {
        DataAccessObject newsDao = RequestUtils.getDaoFactory().getInstance(NewsData.class);
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        Date currentdate = new Date();
        mLogger.createLogInfo("Current Date: " + currentdate);
        args.add(SearchArguments.eq("newsCategory", newsCategory));
        args.add(SearchArguments.eq("locale", locale));
        args.add(SearchArguments.orderByDesc("date"));
        args.add(SearchArgument.create("startDate", 5, currentdate));
        args.add(SearchArgument.create("endDate", 6, currentdate));
        mLogger.createLogDebug(args.toString());
        return newsDao.findBySearchArguments(args);
    }

    public NewsData retrievePageByCategory(String newsCategory)
            throws DAOException {
        DataAccessObject newsDao = RequestUtils.getDaoFactory().getInstance(NewsData.class);
        return (NewsData) newsDao.findById(newsCategory);
    }

    public List<ServiceAdvisoryData> retrieveAllServiceAdvisoryList(String locale) throws DAOException {
        DataAccessObject serviceadvisoryDao = RequestUtils.getDaoFactory().getInstance(ServiceAdvisoryData.class);
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        Date currentdate = new Date();
        args.add(SearchArguments.eq("locale", locale));
        args.add(SearchArguments.orderByDesc("date"));
        args.add(SearchArgument.create("startDate", 5, currentdate));
        args.add(SearchArgument.create("endDate", 6, currentdate));
        mLogger.createLogDebug(args.toString());
        return serviceadvisoryDao.findBySearchArguments(args);
    }

    public List<PromotionEventData> retrievePromotionEventByTypeList(String type, String locale)
            throws DAOException {
        DataAccessObject promotioneventDao = RequestUtils.getDaoFactory().getInstance(PromotionEventData.class);
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        Date currentdate = new Date();
        args.add(SearchArguments.eq("Type", type));
        args.add(SearchArguments.eq("locale", locale));
        args.add(SearchArguments.orderByAsc("displayOrder"));
        args.add(SearchArguments.orderByDesc("date"));
        args.add(SearchArgument.create("startDate", 5, currentdate));
        args.add(SearchArgument.create("endDate", 6, currentdate));
        mLogger.createLogDebug(args.toString());
        return promotioneventDao.findBySearchArguments(args);
    }

    public List<NewsData> retrieveAllNewsListDisplayedByHomepage(String locale) throws DAOException {
        DataAccessObject newsDao = RequestUtils.getDaoFactory().getInstance(NewsData.class);
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        Date currentdate = new Date();
        args.add(SearchArguments.eq("locale", locale));
        args.add(SearchArgument.create("startDate", 5, currentdate));
        args.add(SearchArgument.create("endDate", 6, currentdate));
        args.add(SearchArgument.create("homepageDisplay", SearchArgument.EQUAL, "Yes"));
        args.add(SearchArguments.orderByAsc("displayOrder"));
        args.add(SearchArguments.orderByDesc("date"));

        mLogger.createLogDebug("retrieveAllNewsListDisplayedByHomepage criteria:" + args.toString());
        List<NewsData> resultNews = newsDao.findBySearchArguments(args);

        return resultNews;
    }

    public List<Products> retrieveAllProductListDisplayedByHomepage(String locale, int numberOfResultsOnPage) throws DAOException {
        DataAccessObject productsDao = RequestUtils.getDaoFactory().getInstance(Products.class);
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        //     Date currentdate = new Date();
        args.add(SearchArguments.eq("locale", locale));
        args.add(SearchArguments.eq("doNotShowOnHomepage", "Yes"));
        args.add(SearchArgument.create("status", SearchArgument.EQUAL, "Yes"));
        args.add(SearchArgument.create("archiveFlag", SearchArgument.EQUAL, "No"));
        // Maximum of "12" results
        args.add(SearchArguments.maxResults(numberOfResultsOnPage));
        // Release date should be before current date
        Date currentdate = new Date();
        args.add(SearchArgument.create("releaseDate", 5, currentdate));
        // Order by release date
        args.add(SearchArguments.orderByDesc("releaseDate"));

        mLogger.createLogInfo(args.toString());
        List<Products> resultProducts = productsDao.findBySearchArguments(args);

        return resultProducts;
    }

    /**
     * This method is responsible to persist the ContactUs form information into the Contact_Us table.
     *
     */
    public void saveContactInformation(ContactUs contactUsForm) throws DAOException {
        DataAccessObject contactDao = RequestUtils.getDaoFactory().getInstance(ContactUs.class);
        contactDao.makePersistent(contactUsForm);
    }

    @SuppressWarnings("unchecked")
    public List<ProductCategory> retrieveProductCategory() throws DAOException {
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);
        return prodCatDAO.findAll();
    }

    @SuppressWarnings("unchecked")
    public String retrieveCategoryNameByID(String CategoryID) throws DAOException {
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);

        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }
        mLogger.createLogInfo("Cat ID:::" + CategoryID);
        args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, CategoryID));
        ProductCategory prodCat = (ProductCategory) prodCatDAO.findBySearchArguments(args).get(0);
        String categoryname = prodCat.getCategoryName();
        return categoryname;
    }

    @SuppressWarnings("unchecked")
    public List<Products> retrieveProductsByIds(List productIdList, String locale) throws DAOException {
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodDAO = RequestUtils.getDaoFactory().getInstance(Products.class);
        args.clear();
        //mLogger.createLogDebug("Product cat ID:::" + productCategoryID);
        args.add(SearchArguments.orderByDesc("releaseDate"));
        mLogger.createLogInfo("Searching Products by the clause IN for product Ids:");
        for (Object prodId : productIdList) {
            mLogger.createLogDebug((String) prodId);
        }
        args.add(SearchArgument.create("productID", SearchArgument.IN, productIdList));
        args.add(SearchArgument.create("locale", SearchArgument.EQUAL, locale));

        return prodDAO.findBySearchArguments(args);
    }

    public ProductCategory retrieveL2Category(String categoryId) throws DAOException {
        String product_root_category = "0";
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);
        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }
        args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, categoryId));
        mLogger.createLogInfo("retrieveL2Category search by category id::" + categoryId);
        ProductCategory category = (ProductCategory) prodCatDAO.findBySearchArguments(args).get(0);
        mLogger.createLogInfo("retrieveL2Category Result Category ID::" + category.getCategoryID());
        mLogger.createLogInfo("retrieveL2Category Result Parent Category ID::" + category.getParentCategoryID());
        mLogger.createLogInfo("retrieveL2Category Result Grand Parent Category ID::" + category.getGrandParentCategoryID());
        if (!category.getParentCategoryID().trim().equalsIgnoreCase(product_root_category)) {
            args.clear();

            mLogger.createLogDebug("retrieveL2Category parent category is products root. Hence, need to make the parent call.");
            mLogger.createLogDebug("retrieveL2Category search by category id::" + category.getParentCategoryID());
            args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, category.getParentCategoryID()));
            category = (ProductCategory) prodCatDAO.findBySearchArguments(args).get(0);
            mLogger.createLogDebug("retrieveL2Category Result Category ID::" + category.getParentCategoryID());
            mLogger.createLogDebug("retrieveL2Category Result Parent Category ID::" + category.getParentCategoryID());
            mLogger.createLogDebug("retrieveL2Category Result Grand Parent Category ID::" + category.getGrandParentCategoryID());
        }
        return category;
    }

    /**
     * This method retrieves the related products in the form of a HashMap.
     *
     * The first String contains the related Group Category Id
     * The second HashSet contains the list of product ids.
     *
     * @param categoryId
     * @param productId
     * @return
     */
    public HashMap<String, HashSet> retrieveRelatedProducts(String categoryId, String productId) {
        // First make a call to rerieve the group category id

        boolean isProductCategoryGroup = false;
        String groupCategoryId = categoryId;

        List args = ExternalUtils.createBaseSearchArguments(this.context);
        while (!isProductCategoryGroup) {

            DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);
            if (CollectionUtils.isNotEmpty(args)) {
                args.clear();
            }
            args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, groupCategoryId));
            ProductCategory cat = (ProductCategory) prodCatDAO.findBySearchArguments(args).get(0);
            String parentCategoryId = cat.getParentCategoryID();
            if (cat.getGroupFlag().equalsIgnoreCase("Yes")) {
                isProductCategoryGroup = true;
            } else {
                // Go for the next category call
                groupCategoryId = parentCategoryId;
            }
        }
        mLogger.createLogDebug("retrieveRelatedProducts: groupCategoryId=" + groupCategoryId);
        // Now the groupCategoryId holds the actual group category with which the RELATED_PRODUCTS has to be queried.
        // Based on the group category id and product id, retrieve all related products data
        DataAccessObject relatedProductDAO = RequestUtils.getDaoFactory().getInstance(RelatedProduct.class);
        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }
        // First call
        args.add(SearchArgument.create("groupCategoryId", SearchArgument.EQUAL, groupCategoryId));
        args.add(SearchArgument.create("productId", SearchArgument.EQUAL, productId));
        List<RelatedProduct> relatedProducts = relatedProductDAO.findBySearchArguments(args);

        boolean readFirstTwoColumns = false;

        HashMap<String, HashSet> relatedProductsMap = new HashMap<String, HashSet>();
        updateRelatedProductsMap(relatedProducts, relatedProductsMap, readFirstTwoColumns);


        // Combining with Second call
        args.clear();
        args.add(SearchArgument.create("relatedGroupCategoryId", SearchArgument.EQUAL, groupCategoryId));
        args.add(SearchArgument.create("relatedProductId", SearchArgument.EQUAL, productId));
        relatedProducts = relatedProductDAO.findBySearchArguments(args);

        readFirstTwoColumns = true;
        updateRelatedProductsMap(relatedProducts, relatedProductsMap, readFirstTwoColumns);

        return relatedProductsMap;
    }

    private void updateRelatedProductsMap(List<RelatedProduct> relatedProducts, HashMap<String, HashSet> relatedProductsMap, boolean readFirstTwoColumns) {

        String rpCatId = null;
        String rpId = null;

        for (RelatedProduct relatedProduct : relatedProducts) {

            if (readFirstTwoColumns) {
                rpCatId = relatedProduct.getGroupCategoryId();
                rpId = relatedProduct.getProductId();
            } else {
                rpCatId = relatedProduct.getRelatedGroupCategoryId();
                rpId = relatedProduct.getRelatedProductId();
            }
            if (relatedProductsMap.containsKey(rpCatId)) {
                ((HashSet) relatedProductsMap.get(rpCatId)).add(rpId);
            } else {
                String[] strArr = {rpId};
                relatedProductsMap.put(rpCatId, new HashSet(Arrays.asList(strArr)));
            }
        }
    }

    /**
     * 
     *
     * @param keySet
     * @return
     */
    public HashMap<String, String> retrieveCategoryDisplayNames(Set<String> categoryIdSet) {
        HashMap<String, String> returnMap = new HashMap<String, String>();

        List args = ExternalUtils.createBaseSearchArguments(this.context);
        DataAccessObject prodCatDAO = RequestUtils.getDaoFactory().getInstance(ProductCategory.class);
        if (CollectionUtils.isNotEmpty(args)) {
            args.clear();
        }
        List<String> categoryIdList = new ArrayList<String>(categoryIdSet);
        args.add(SearchArgument.create("categoryID", SearchArgument.IN, categoryIdList));
        List<ProductCategory> categories = prodCatDAO.findBySearchArguments(args);

        for (Iterator<ProductCategory> it = categories.iterator(); it.hasNext();) {
            ProductCategory productCategory = it.next();

            if (!productCategory.getParentCategoryID().equalsIgnoreCase("0")) {
                // Need to call for the parent category id and set <ParentCategoryName> - <GroupCategoryName>
                args.clear();
                args.add(SearchArgument.create("categoryID", SearchArgument.EQUAL, productCategory.getParentCategoryID()));
                ProductCategory parentCategory = (ProductCategory) prodCatDAO.findBySearchArguments(args).get(0);
                returnMap.put(productCategory.getCategoryID(), parentCategory.getCategoryName() + " - " + productCategory.getCategoryName());

            } else {
                returnMap.put(productCategory.getCategoryID(), productCategory.getCategoryName());
            }
        }

        return returnMap;
    }

    @Override
    public List<LearnExploreData> retrieveLearnByCategoryList(String newsCategory, String locale)
            throws DAOException {
        DataAccessObject newsDao = RequestUtils.getDaoFactory().getInstance(NewsData.class);
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        Date currentdate = new Date();
        mLogger.createLogInfo("Current Date: " + currentdate);
        args.add(SearchArguments.eq("newsCategory", newsCategory));
        args.add(SearchArguments.eq("locale", locale));
        args.add(SearchArguments.orderByDesc("date"));
        args.add(SearchArgument.create("startDate", 5, currentdate));
        args.add(SearchArgument.create("endDate", 6, currentdate));
        mLogger.createLogDebug(args.toString());
        return newsDao.findBySearchArguments(args);
    }
}
