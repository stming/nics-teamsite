/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.datasources;

import com.interwoven.datasource.MapDataSource;
import java.sql.SQLException;
import nhk.ts.wcms.common.Logger;
import com.interwoven.datasource.core.DataSourceContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.interwoven.livesite.common.cssdk.datasource.AbstractDataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import nhk.ts.wcms.dao.ConnectionManager;
import nhk.ts.wcms.dao.DAOException;
import nhk.ts.wcms.dao.DataManager;
import nhk.ts.wcms.dao.ProductCategory;
import nhk.ts.wcms.dao.Products;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

/**
 *
 * @author smukherj
 */
public class RelatedProductCategoryDataSource extends AbstractDataSource implements MapDataSource {

    private static final String LEVEL0_CATEGORY_ID = "0";
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.datasources.RelatedProductCategoryDataSource"));

    public Map<String, String> execute(DataSourceContext context) {

        mLogger.createLogDebug("RelatedProductCategoryDataSource called.");
        Map<String, String> results = new LinkedHashMap<String, String>();
        String category_id = context.getParameter("ProductCategory");
        String locale = context.getParameter("locale");

        try {
            Connection connection = ConnectionManager.getStagingConnection();
            DataManager dm = new DataManager();

            if (category_id != null) {
                mLogger.createLogDebug("Retrieving products for category_id=" + category_id);
                mLogger.createLogDebug("Retrieving products for locale=" + locale);
                retrieveProductsByCategoryGroupID(dm, connection, category_id, results, locale);
            } else {
                retrieveCategoryGroups(dm, connection, results, locale);
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error in execute method::", e);
            this.mLogger.createLogErrorWithoutThrowingException("Error retrieving Product Category based on " + e.getMessage(), e);
        }

        return results;
    }

    private void retrieveCategoryGroups(DataManager dm, Connection connection, Map<String, String> results, String locale) throws DAOException {
        List<ProductCategory> categoryList = dm.retrieveCategoriesByGroupFlag(connection, locale);
        if (!categoryList.isEmpty()) {
            Iterator<ProductCategory> iterator = categoryList.iterator();
            while (iterator.hasNext()) {
                ProductCategory productCategory = (ProductCategory) iterator.next();
                mLogger.createLogDebug("Following group category found::" + productCategory.getCategoryID() + ":" + productCategory.getCategoryName());
                results.put(productCategory.getCategoryID(), productCategory.getCategoryName());
            }
        }
    }

    private void retrieveProductsByCategoryGroupID(DataManager dm, Connection connection, String category_id, Map<String, String> results, String locale) throws SQLException {

        List<Products> productList = dm.retrieveProductsByCategoryGroupID(connection, category_id, locale);

        Collections.sort(productList, PRODUCT_NAME);

        if (!productList.isEmpty()) {
            Iterator<Products> iterator = productList.iterator();
            while (iterator.hasNext()) {
                Products product = (Products) iterator.next();
                results.put(product.getProductID(), product.getProductName());
            }
        }
    }
    private final Comparator<Products> PRODUCT_NAME = new Comparator<Products>() {

        public int compare(Products o1, Products o2) {
            String o1Name = o1.getProductName();
            String o2Name = o2.getProductName();

            int result = o1Name.compareTo(o2Name);

            mLogger.createLogDebug("PRODUCT_NAME::Comparing o1Name:" + o1Name + " with o2Name:" + o2Name
                    + "::Returning " + result);
            return result;
        }
    };
}
