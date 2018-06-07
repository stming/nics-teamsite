/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nhk.ls.runtime.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sbhojnag
 */
public interface DataManager {
    public List<NewsData> retrieveNewsByCategoryList(String newsCategory,String locale) throws DAOException;
    public List<NewsData> retrieveAllNewsList(String locale) throws DAOException;
    public NewsData retrievePageByCategory(String newsCategory) throws DAOException;
    public List<ServiceAdvisoryData> retrieveAllServiceAdvisoryList(String locale) throws DAOException;
    public List<Products> retrieveProductsByCategory(String productCategoryID) throws DAOException;
    public List<ProductCategory> retrieveCategoryByParentID(String productCategoryID) throws DAOException;
    public void checkAndRetrieveOverviewText(String productCategoryID,String overViewText,String productCategoryName) throws DAOException;
    public ProductCategory checkAndRetrieveCategoryByID(String categoryID) throws DAOException;
    public List<PromotionEventData> retrievePromotionEventByTypeList(String type,String locale) throws DAOException ;
    public List<NewsData> retrieveAllNewsListDisplayedByHomepage(String locale) throws DAOException;
    public List<Products> retrieveAllProductListDisplayedByHomepage(String locale, int numberOfResultsOnPage) throws DAOException;
    public void saveContactInformation(ContactUs contactUsForm) throws DAOException;
    public List<ProductCategory> retrieveProductCategory() throws DAOException ;
    public String retrieveCategoryNameByID(String CategoryID) throws DAOException;
    public List<Products> retrieveProductsByIds(List productIdList, String locale) throws DAOException;
    public ProductCategory retrieveCategoryByID(String categoryID, boolean checkForFlashBanner) throws DAOException;
    public ProductCategory retrieveL2Category(String categoryId) throws DAOException;
    public HashMap<String, HashSet> retrieveRelatedProducts(String categoryId, String productId);
    public HashMap<String, String> retrieveCategoryDisplayNames(Set<String> keySet);
    public List<LearnExploreData> retrieveLearnByCategoryList(String newsCategory, String locale);

}
