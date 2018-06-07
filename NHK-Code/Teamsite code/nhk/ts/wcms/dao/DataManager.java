/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.dao;

/**
 *
 * @author sbhojnag
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.LogFactory;
import nhk.ts.wcms.common.Logger;
import org.dom4j.Element;

/**
 * This is the DAO (Data Access Object) class, response for any operation need to perform on the Page_Metadata table
 * @author sbhojnag
 *
 */
public class DataManager {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.dao.DataManager"));
    private final String idprefix = "<RelatedProductCategory>";
    private final String idsuffix = "</RelatedProductCategory>";
    private final String relProdNamePrefix = "<RelatedProductCategoryName>";
    private final String relProdNameSuffix = "</RelatedProductCategoryName>";

    public List<Products> retrieveProductwithCategory(Connection connection, String locale, String categoryID) throws DAOException {
        mLogger.createLogDebug("Retrieving Product based on Category ID");
        String sql = "select product_name,release_date from Products where category_id = ? AND locale = ? order by release_date";
        List<Products> list = new ArrayList<Products>();
        PreparedStatement pStatement = null;
        ResultSet rs = null;
        try {
            pStatement = connection.prepareStatement(sql);
            pStatement.setString(1, categoryID);
            pStatement.setString(2, locale);
            mLogger.createLogDebug("SQL Statement: " + pStatement.toString());
            rs = pStatement.executeQuery();
            while (rs.next()) {
                list.add(this.constructProducts(rs));
            }
        } catch (SQLException e) {
            mLogger.createLogDebug("Error in retrieveProductwithCategory::", e);
            throw new DAOException("error while trying to retrieve ProductwithCategory", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pStatement != null) {
                    pStatement.close();
                }
            } catch (SQLException e) {
                // Ignore
                mLogger.createLogDebug("Error in retrieveProductwithCategory::", e);
            }
        }
        return list;
    }

    public List<ProductCategory> retrieveCategoriesByGroupFlag(Connection connection, String locale) throws DAOException {
        mLogger.createLogDebug("Retrieving Product Category based on group_flag");
        String sql = "select category_id,category_name,parent_category_id from Product_Category where group_flag = ? and locale= ?";
        List<ProductCategory> list = new ArrayList<ProductCategory>();
        PreparedStatement pStatement = null;
        ResultSet rs = null;
        try {
            pStatement = connection.prepareStatement(sql);
            pStatement.setString(1, "Yes");
            pStatement.setString(2, locale);
            mLogger.createLogDebug("SQL Statement: " + sql);
            rs = pStatement.executeQuery();
            while (rs.next()) {
                ProductCategory productCategoryData = new ProductCategory();
                productCategoryData.setCategoryID(rs.getString("category_id"));
                String categoryDisplayName = buildCategoryDisplayName(rs, connection);
                productCategoryData.setCategoryName(categoryDisplayName);
                list.add(productCategoryData);
            }
        } catch (SQLException e) {
            mLogger.createLogDebug("Error in retrieveProductwithCategory::", e);
            throw new DAOException("error while trying to retrieve ProductwithCategory", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pStatement != null) {
                    pStatement.close();
                }
            } catch (SQLException e) {
                // Ignore
                mLogger.createLogDebug("Error in retrieveProductwithCategory::", e);
            }
        }
        return list;
    }

    private String buildCategoryDisplayName(ResultSet rs, Connection connection) throws SQLException {
        String categoryDisplayName = rs.getString("category_name");
        String parentCatId = rs.getString("parent_category_id");
        if (!parentCatId.equalsIgnoreCase("0")) {
            // It is not a L1 category. Hence, retrieve the parent category name and prepend it.
            PreparedStatement newPStatement = connection.prepareStatement("select category_name from Product_Category where category_id =?");
            newPStatement.setString(1, parentCatId);
            ResultSet newrs = newPStatement.executeQuery();
            while (newrs.next()) {
                categoryDisplayName = newrs.getString("category_name") + " - " + categoryDisplayName;
            }
        }

        return categoryDisplayName;


    }

    public Timestamp retrieveWWA(Connection connection, String locale, String product_name) throws DAOException {
        mLogger.createLogDebug("Retrieving Product WWA based on Name");
        String sql = "select release_date from Products where product_name = ? AND Locale = ?";
        PreparedStatement pStatement = null;
        ResultSet rs = null;
        Timestamp WWA = null;
        try {
            pStatement = connection.prepareStatement(sql);
            pStatement.setString(1, product_name);
            pStatement.setString(2, locale);
            mLogger.createLogDebug("SQL Statement: " + pStatement.toString());
            rs = pStatement.executeQuery();
            rs.next();
            WWA = rs.getTimestamp("release_date");
        } catch (SQLException e) {
            mLogger.createLogDebug("Error in retrieveWWA::", e);
            throw new DAOException("error while trying to retrieve WWA", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pStatement != null) {
                    pStatement.close();
                }
            } catch (SQLException e) {
                // Ignore
                mLogger.createLogDebug("Error in retrieveWWA::", e);
            }
        }
        return WWA;
    }

    public String retrieveProductpath(Connection connection, String locale, String product_name) throws DAOException {
        mLogger.createLogDebug("Retrieving Product path based on Name");
        String sql = "select dcr_path from Products where product_name = ? AND Locale = ?";
        PreparedStatement pStatement = null;
        ResultSet rs = null;
        String dcrpath = null;
        try {
            pStatement = connection.prepareStatement(sql);
            pStatement.setString(1, product_name);
            pStatement.setString(2, locale);
            mLogger.createLogDebug("SQL Statement: " + pStatement.toString());
            rs = pStatement.executeQuery();
            rs.next();
            dcrpath = rs.getString("dcr_path");
        } catch (SQLException e) {
            mLogger.createLogDebug("Error in dcrlink::", e);
            throw new DAOException("error while trying to retrieve dcrpath", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pStatement != null) {
                    pStatement.close();
                }
            } catch (SQLException e) {
                // Ignore
                mLogger.createLogDebug("Error in dcrpath::", e);
            }
        }
        return dcrpath;
    }

    private Products constructProducts(ResultSet rs) throws SQLException {
        Products productData = new Products();
        productData.setProductName(rs.getString("product_name"));
        return productData;
    }

    public List<Products> retrieveProductsByCategoryGroupID(Connection conn, String category_id, String locale) throws SQLException {
        List<Products> list = new ArrayList<Products>();
        Statement stmt = null;
        ResultSet rs = null;
        if (conn != null) {

            Set categories = new HashSet();
            categories.add(category_id);
            retrieveChildCategories(conn, category_id, categories);
            String productSQL = buildStatement(categories, locale);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(productSQL);

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                Products productData = new Products();
                productData.setProductID(rs.getString("product_id"));
                productData.setProductName(rs.getString("product_name"));
                list.add(productData);
            }
        }
        mLogger.createLogDebug("A total of " + list.size() + " products returned.");
        return list;
    }

    private void retrieveChildCategories(Connection conn, String category_id, Set categories) throws SQLException {
        mLogger.createLogDebug("Retrieving Products based on Category Group Id:" + category_id);
        String categorySQL = "SELECT category_id FROM Product_Category where parent_category_id='" + category_id + "' or grand_parent_category_id='" + category_id + "'";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(categorySQL);

        while (rs.next()) {
            String nextCategoryId = rs.getString("category_id");
            categories.add(nextCategoryId);
            retrieveChildCategories(conn, nextCategoryId, categories);
        }
    }

    private String buildStatement(Set categories, String locale) {

        String sql = "SELECT product_id, product_name FROM Products where locale='" + locale + "' and category_id IN(";
        Iterator itr = categories.iterator();
        int count = 0;
        while (itr.hasNext()) {
            String category_id = (String) itr.next();
            if (count > 0) {
                sql += ",";
            }
            sql += "'" + category_id + "'";
            count++;
        }

        sql += ")";
        mLogger.createLogDebug("Query fired for all products:" + sql);
        return sql;
    }

    public HashMap<String, String> updateRelatedProductsFromMapper(Connection conn, String locale, List<Element> relatedProductCtrs) throws SQLException {

        HashMap<String, String> result = new HashMap<String, String>();
        String mapperSQL = "SELECT mapper.en_Asia_id,mapper." + locale + ",cat.parent_category_id,cat.category_name FROM Product_Category_Mapper mapper, Product_Category cat where mapper.en_Asia_id=? and cat.category_id=mapper." + locale;

        PreparedStatement pStatement = null;
        ResultSet rs = null;
        for (Element element : relatedProductCtrs) {
            String origId = element.selectSingleNode("RelatedProductCategory").getText();
            String origName = element.selectSingleNode("RelatedProductCategoryName").getText();
            pStatement = conn.prepareStatement(mapperSQL);
            pStatement.setString(1, origId);
            mLogger.createLogDebug(mapperSQL + "WITH en_Asia_id=" + origId);
            rs = pStatement.executeQuery();
            while (rs.next()) {
                String newid = rs.getString(locale);
                String newName = buildCategoryDisplayName(rs, conn);

                result.put(idprefix + origId + idsuffix, idprefix + newid + idsuffix);
                result.put(relProdNamePrefix + origName + relProdNameSuffix, relProdNamePrefix + newName + relProdNameSuffix);
            }
        }
        return result;
    }

    public HashMap<String, String> updateProductCategoriesInDCR(Connection conn, String locale, String origId, String elementName) throws SQLException {

        mLogger.createLogDebug("retrieveCategoryFromMapper called with locale=" + locale + " origId=" + origId + " elementName=" + elementName);
        HashMap<String, String> result = new HashMap<String, String>();

        String newid = retrieveCategoryFromMapper(conn, origId, locale);
        String prefix = "<" + elementName + ">";
        String suffix = "</" + elementName + ">";
        result.put(prefix + origId + suffix, prefix + newid + suffix);

        return result;
    }

    /**
     * This method is responsible to read the data from RELATED_PRODUCTS (STAGING) table for a particular category-product combination
     * and populate the RELATED_PRODUCTS (PRODUCTION) table.
     *
     * @param stgConnection
     * @param prdConnection
     * @param categoryId
     * @param productId
     */
    public void updateRelatedProductsInProduction(Connection stgConnection, Connection prdConnection, String categoryId, String productId) {
        try {
            // Find needs to find the group_category_id
            String groupCategoryId = RelatedProductsHelper.getGroupCategoryId(categoryId, stgConnection);

            // groupCategoryId now holds the desired value with which RELATED_PRODUCTS table should be queried
            if (groupCategoryId != null) {

                ResultSet devResults = RelatedProductsHelper.getRelatedProducts(groupCategoryId, productId, stgConnection);
                ResultSet prodResults = RelatedProductsHelper.getRelatedProducts(groupCategoryId, productId, prdConnection);

                HashMap savedRelatedProducts = new HashMap<String, HashSet<String>>();
                HashMap inboundRelatedProducts = new HashMap<String, HashSet<String>>();

                RelatedProductsHelper.putRelatedProductResultInMap(inboundRelatedProducts, devResults, groupCategoryId, productId);
                RelatedProductsHelper.putRelatedProductResultInMap(savedRelatedProducts, prodResults, groupCategoryId, productId);


                RelatedProductsHelper.saveRelatedProducts(productId, groupCategoryId, inboundRelatedProducts, savedRelatedProducts, prdConnection, false);


            }
            stgConnection.close();
            prdConnection.close();
        } catch (SQLException ex) {
            mLogger.createLogError("Error found in updateRelatedProductsInProduction.", ex);
        }
    }

    public String retrieveCategoryFromMapper(Connection conn, String origId, String locale) throws SQLException {
        String newid = "";

        String mapperSQL = "SELECT mapper.en_Asia_id,mapper." + locale + " FROM Product_Category_Mapper mapper where mapper.en_Asia_id=?";
        PreparedStatement pStatement = null;
        ResultSet rs = null;

        if (!origId.equalsIgnoreCase("0")) {

            pStatement = conn.prepareStatement(mapperSQL);
            pStatement.setString(1, origId);

            mLogger.createLogDebug(mapperSQL + " WITH en_Asia_id=" + origId);
            rs = pStatement.executeQuery();
            while (rs.next()) {
                newid = rs.getString(locale);
            }
        } else {
            newid = "0";
        }
        return newid;
    }
}
