/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.dct;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.ResultSet;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.http.HttpServlet;
import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.dao.ConnectionManager;
import nhk.ts.wcms.dao.RelatedProductsHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author smukherj
 */
public class RelatedProductPersistence extends HttpServlet {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.dct.RelatedProductPersistence"));

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
        try {
            response.setContentType("text/xml; charset=UTF-8");
            PrintWriter out;
            Element root = DocumentHelper.createElement("RelatedProducts");
            String action = request.getParameter("Action");
            mLogger.createLogDebug("Action=" + action);

            if (action.equalsIgnoreCase("Save")) {

                String relatedProducts = request.getParameter("RelatedProducts");
                HashMap inboundRelatedProducts = mapInboundProducts(relatedProducts);
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection stgConn = ConnectionManager.getStagingConnection();
                String categoryId = request.getParameter("CategoryID");
                String productId = request.getParameter("ProductID");

                String groupCategoryId = RelatedProductsHelper.getGroupCategoryId(categoryId, stgConn);
                ResultSet rs = RelatedProductsHelper.getRelatedProducts(groupCategoryId, productId, stgConn);

                HashMap savedRelatedProducts = new HashMap<String, HashSet<String>>();
                RelatedProductsHelper.putRelatedProductResultInMap(savedRelatedProducts, rs, groupCategoryId, productId);

                RelatedProductsHelper.saveRelatedProducts(productId, groupCategoryId, inboundRelatedProducts, savedRelatedProducts, stgConn, true);

                stgConn.close();

            } else if (action.equalsIgnoreCase("Retrieve")) {

                retrieveRelatedProducts(request, root);

            }
            out = response.getWriter();
            out.println(root.asXML());
        } catch (Exception ex) {
            mLogger.createLogError("Error found in doPost method.", ex);
        }
    }

    private void retrieveRelatedProducts(HttpServletRequest request, Element root) {
        try {
            String categoryId = request.getParameter("CategoryID");
            String productId = request.getParameter("ProductID");
            // load the driver class
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = ConnectionManager.getStagingConnection();
            if (conn != null) {

                // Find the group_category_id
                String groupCategoryId = RelatedProductsHelper.getGroupCategoryId(categoryId, conn);
                // groupCategoryId now holds the desired value with which RELATED_PRODUCTS table should be queried
                if (groupCategoryId != null) {

                    mLogger.createLogDebug("Group category found=" + groupCategoryId);
                    ResultSet rs = RelatedProductsHelper.getRelatedProducts(groupCategoryId, productId, conn);

                    String firstCatId = null;
                    String firstProdId = null;
                    String secondCatId = null;
                    String secondProdId = null;

                    while (rs.next()) {
                        firstCatId = rs.getString("groupCategoryId");
                        firstProdId = rs.getString("productId");
                        secondCatId = rs.getString("relatedGroupCategoryId");
                        secondProdId = rs.getString("relatedProductId");

                        mLogger.createLogDebug("firstCatId=" + firstCatId);
                        mLogger.createLogDebug("firstProdId=" + firstProdId);
                        mLogger.createLogDebug("secondCatId=" + secondCatId);
                        mLogger.createLogDebug("secondProdId=" + secondProdId);

                        if (firstCatId.equalsIgnoreCase(groupCategoryId) && firstProdId.equalsIgnoreCase(productId)) {
                            // Second one is related product.
                            mLogger.createLogDebug("Second pair is related product.");
                            updateRelatedProducts(root, secondCatId, secondProdId);
                        } else {
                            // First one is related product.
                            mLogger.createLogDebug("First pair is related product.");
                            updateRelatedProducts(root, firstCatId, firstProdId);
                        }
                    }
                    mLogger.createLogDebug("Root updated to=" + root.asXML());
                }
            }
            conn.close();
        } catch (Exception ex) {
            mLogger.createLogError("Error found in retrieveRelatedProducts.", ex);
        }
        mLogger.createLogDebug("Returned value=" + root.asXML());
    }

    /**
     * This method updates the related products in the root element. For an existing categoryId element, it will only append the productId at the end.
     * In case of non-existing category elements, it will be a new element entry.
     *
     * @param root Root element which will be updated.
     * @param catId Related group category id
     * @param prodId Related product id
     */
    private void updateRelatedProducts(Element root, String catId, String prodId) {
        if (root.selectSingleNode("RelatedProduct[@CategoryId='" + catId + "']") != null) {
            mLogger.createLogDebug("Category found in root=" + catId);
            String currentProductIds = null;
            Element relProdEle = (Element) root.selectSingleNode("RelatedProduct[@CategoryId='" + catId + "']");
            currentProductIds = relProdEle.getText();
            if (StringUtils.isNotEmpty(currentProductIds)) {
                relProdEle.addText("," + prodId);
                mLogger.createLogDebug("Updated element <RelatedProduct CategoryId='" + catId + "'>" + currentProductIds + "," + prodId + "</RelatedProduct>");
            }
        } else {
            Element relProdEle = root.addElement("RelatedProduct");
            relProdEle.addAttribute("CategoryId", catId);
            relProdEle.addText(prodId);
            mLogger.createLogDebug("Added element <RelatedProduct CategoryId=" + catId + ">" + prodId + "</RelatedProduct>");
        }
    }

    private HashMap<String, HashSet<String>> mapInboundProducts(String relatedProducts) {

        HashMap<String, HashSet<String>> resultMap = new HashMap<String, HashSet<String>>();
        String[] relatedProductsGroup = StringUtils.split(relatedProducts, "::");

        for (String relatedProduct : relatedProductsGroup) {
            String[] categoryAndProducts = StringUtils.split(relatedProduct, "|");

            if (categoryAndProducts.length == 2) {
                String category = categoryAndProducts[0];
                String products = categoryAndProducts[1];

                HashSet<String> productsArray = new HashSet<String>(Arrays.asList(StringUtils.split(products, ",")));

                resultMap.put(category, productsArray);

            }

        }
        return resultMap;
    }
}
