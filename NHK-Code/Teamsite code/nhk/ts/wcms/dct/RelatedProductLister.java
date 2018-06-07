/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.dct;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interwoven.cssdk.filesys.CSVPath;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.dao.ConnectionManager;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author smukherj
 */
public class RelatedProductLister extends HttpServlet {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.dct.RelatedProductLister"));

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    /**
     * This method will fetch the products from the Products table based on the categoryId passed as a parameter.
     *
     * @param request
     * @param response
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        List<String> results = new ArrayList<String>();
        CSVPath siteVPath = null;
        String sitePath = null;

        try {
            response.setContentType("text/xml; charset=UTF-8");
            PrintWriter out;
            Element root = DocumentHelper.createElement("Products");

            String category_id = request.getParameter("ProductCategory");
            if (category_id != null) {

                // load the driver class
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection conn = ConnectionManager.getStagingConnection();

                Statement stmt = null;
                ResultSet rs = null;

                if (conn != null) {

                    Set categories = new HashSet();
                    categories.add(category_id);

                    retrieveChildCategories(conn, category_id, categories);

                    String productSQL = buildStatement(categories);
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery(productSQL);

                    // Iterate through the data in the result set and display it.
                    String name = null;
                    String product_id = null;

                    while (rs.next()) {
                        name = rs.getString("product_name");
                        product_id = (rs.getString("product_id"));

                        Element productElement = root.addElement("Product").addText(name);
                        productElement.addAttribute("id", product_id);
                    }
                }
                conn.close();
                out = response.getWriter();
                out.println(root.asXML());
            }

        } catch (ClassNotFoundException cnfe) {
            mLogger.createLogDebug("Error in doPost method:;", cnfe);
        } catch (SQLException sqle) {
            mLogger.createLogDebug("Error in doPost method:;", sqle);
        } catch (Exception e) {
            mLogger.createLogDebug("Error in doPost method:;", e);
        }
    }

    private void retrieveChildCategories(Connection conn, String category_id, Set categories) throws SQLException {

        String categorySQL = "SELECT category_id FROM Product_Category where parent_category_id='" + category_id + "' or grand_parent_category_id='" + category_id + "'";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(categorySQL);

        while (rs.next()) {
            String nextCategoryId = rs.getString("category_id");
            categories.add(nextCategoryId);
            retrieveChildCategories(conn, nextCategoryId, categories);
        }
    }

    private String buildStatement(Set categories) {

        String sql = "SELECT product_name, product_id FROM Products where category_id IN(";
        Iterator itr = categories.iterator();
        int count = 0;
        while (itr.hasNext()) {
            String category_id  = (String) itr.next();
            if (count > 0) {
                sql += ",";
            }
            sql += "'" + category_id + "'";
            count++;
        }

        sql += ")";

        return sql;
    }
}
