/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.datasources;

import com.interwoven.datasource.MapDataSource;
import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.dao.ConnectionManager;
import nhk.ts.wcms.dao.DataManager;
import nhk.ts.wcms.dao.Products;
import java.util.Iterator;
import com.interwoven.datasource.core.DataSourceContext;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import com.interwoven.livesite.common.cssdk.datasource.AbstractDataSource;
import java.util.Properties;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author sbhojnag
 */
public class ProductDataSource extends AbstractDataSource implements MapDataSource {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.datasources.ProductDataSource"));

    public Map<String, String> execute(DataSourceContext context) {
        Map<String, String> results = new LinkedHashMap<String, String>();

        /**
         * NOT USING THE DATASOURCE FILE ANY MORE
         *
         */

        /*
        String propertyFullPath = null;

        propertyFullPath = "resources/properties/SiteInfo.properties";
        propertyFullPath = context.getServerContext() + "/" + propertyFullPath;
        mLogger.createLogDebug("SiteInfo reading properties from:" + propertyFullPath);
        String categoryID = context.getParameter("ProductCategory");
        mLogger.createLogDebug("Category ID: " + categoryID);
        Properties siteProperties = new Properties();
        try {
        siteProperties.load(new FileInputStream(propertyFullPath));
        } catch (IOException e) {
        mLogger.createLogDebug("Error in execute method::", e);
        mLogger.createLogDebug("SiteNode reading properties from:" + propertyFullPath + " error!");
        }
        String locale = siteProperties.getProperty("locale");
        mLogger.createLogDebug("Locale: " + locale);
        if ((null != categoryID) && (!("".equals(categoryID)))) {
        try {
        Connection connection = ConnectionManager.getConnection();
        DataManager dm = new DataManager();
        List<Products> productList = dm.retrieveProductwithCategory(connection, locale, categoryID);
        if (!productList.isEmpty()) {
        Iterator<Products> iterator = productList.iterator();
        while (iterator.hasNext()) {
        Products product = (Products) iterator.next();
        results.put(product.getProductName(), product.getProductName());
        }
        }
        } catch (Exception e) {
        mLogger.createLogDebug("Error in execute method::", e);
        this.mLogger.createLogErrorWithoutThrowingException("Error retrieving Product Category " + e.getMessage(), e);
        }
        }*/
        return results;
    }
}
