/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author smukherj
 */
public class RelatedProductsHelper {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.dao.RelatedProductsHelper"));

    public static void updateRelatedProductsMap(HashMap relatedProductsMap, String catId, String prodId) {
        if (relatedProductsMap.containsKey(catId)) {
            HashSet<String> products = (HashSet<String>) relatedProductsMap.get(catId);
            if (!products.contains(prodId)) {
                products.add(prodId);
            }
        } else {
            HashSet<String> products = new HashSet<String>();
            products.add(prodId);
            relatedProductsMap.put(catId, products);
        }
    }

    public static void updateRelatedProductsTable(HashMap relatedProductsMap, Connection conn, String groupCategoryId, String productId, HashMap inboundRelatedProducts, boolean isStagingConn) {
        try {
            // savedRelatedProducts is the list to be DELETED
            // inboundRelatedProducts is the list to be INSERTED
            Iterator deleteItr = relatedProductsMap.entrySet().iterator();
            Statement stmt = conn.createStatement();
            String delSQL1 = "delete from RELATED_PRODUCTS where groupCategoryId='";
            String delSQL2 = "' and productId='";
            String delSQL3 = "' and relatedGroupCategoryId='";
            String delSQL4 = "' and relatedProductId='";
            String delSQL5 = "'";
            while (deleteItr.hasNext()) {
                Map.Entry deleteValues = (Map.Entry) deleteItr.next();
                String deleteCategoryId = (String) deleteValues.getKey();
                HashSet deleteProducts = (HashSet) deleteValues.getValue();
                Iterator deleteProductsItr = deleteProducts.iterator();
                while (deleteProductsItr.hasNext()) {
                    String deleteProductId = (String) deleteProductsItr.next();
                    String firstDeleteSQL = delSQL1 + groupCategoryId + delSQL2 + productId + delSQL3 + deleteCategoryId + delSQL4 + deleteProductId + delSQL5;
                    String secondDeleteSQL = delSQL1 + deleteCategoryId + delSQL2 + deleteProductId + delSQL3 + groupCategoryId + delSQL4 + productId + delSQL5;
                    mLogger.createLogDebug("About to delete from RELATED_PRODUCTS:" + firstDeleteSQL);
                    mLogger.createLogDebug("About to delete from RELATED_PRODUCTS:" + secondDeleteSQL);
                    stmt.addBatch(firstDeleteSQL);
                    stmt.addBatch(secondDeleteSQL);

                    if (isStagingConn) {
                        addLocaleRelatedCategoryDeletes(stmt, groupCategoryId, deleteCategoryId, conn, firstDeleteSQL, secondDeleteSQL);
                    }
                }
            }
            Iterator insertItr = inboundRelatedProducts.entrySet().iterator();
            String insSQL1 = "INSERT into RELATED_PRODUCTS values ('";
            String insSQL2 = "','";
            String insSQL3 = "','";
            String insSQL4 = "','";
            String insSQL5 = "')";
            while (insertItr.hasNext()) {
                Map.Entry insertValues = (Map.Entry) insertItr.next();
                String insertCategoryId = (String) insertValues.getKey();
                HashSet insertProducts = (HashSet) insertValues.getValue();
                Iterator insertProductsItr = insertProducts.iterator();
                while (insertProductsItr.hasNext()) {
                    String insertProductId = (String) insertProductsItr.next();
                    String insertSQL = insSQL1 + groupCategoryId + insSQL2 + productId + insSQL3 + insertCategoryId + insSQL4 + insertProductId + insSQL5;
                    mLogger.createLogDebug("About to insert into RELATED_PRODUCTS:" + insertSQL);
                    stmt.addBatch(insertSQL);
                    if (isStagingConn) {
                        addLocaleRelatedCategoryInserts(stmt, groupCategoryId, insertCategoryId, conn, insertSQL);
                    }
                }
            }
            stmt.executeBatch();
        } catch (Exception ex) {
            mLogger.createLogError("Error found in updateRelatedProductsTable.", ex);
        }
    }

    public static void saveRelatedProducts(String productId, String groupCategoryId, HashMap inboundRelatedProducts, HashMap savedRelatedProducts, Connection conn, boolean isStagingConn) {
        try {
            if (conn != null) {
                // Compare with incoming map and find the new entries which should enter the database.
                // INSERT list : What is there in inboundRelatedProducts and not in savedRelatedProducts
                // DELETE list : What is there in savedRelatedProducts and not in inboundRelatedProducts
                Iterator savedRelatedProductsItr = savedRelatedProducts.entrySet().iterator();
                while (savedRelatedProductsItr.hasNext()) {
                    Map.Entry savedValues = (Map.Entry) savedRelatedProductsItr.next();
                    String savedCatId = (String) savedValues.getKey();
                    HashSet savedProducts = (HashSet) savedValues.getValue();
                    if (inboundRelatedProducts.containsKey(savedCatId)) {
                        //Both the maps have the same categoryId. Now check for products.
                        HashSet inboundProducts = (HashSet) inboundRelatedProducts.get(savedCatId);
                        Iterator savedProductsItr = savedProducts.iterator();

                        ArrayList deletionList = new ArrayList();

                        while (savedProductsItr.hasNext()) {
                            String savedProductId = (String) savedProductsItr.next();
                            if (inboundProducts.contains(savedProductId)) {
                                //Found in both. Hence delete from both.
                                deletionList.add(savedProductId);
                            }
                        }
                        savedProducts.removeAll(deletionList);
                        inboundProducts.removeAll(deletionList);
                    }
                }
                updateRelatedProductsTable(savedRelatedProducts, conn, groupCategoryId, productId, inboundRelatedProducts, isStagingConn);
            }
        } catch (Exception ex) {
            mLogger.createLogError("Error found in saveRelatedProducts.", ex);
        }
    }

    public static String getGroupCategoryId(String categoryId, Connection conn) {
        String returnValue = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean isProductCategoryGroup = false;
        while (!isProductCategoryGroup) {
            try {
                String productCategorySQL = "SELECT group_flag, parent_category_id FROM Product_Category where category_id='" + categoryId + "'";
                mLogger.createLogDebug(productCategorySQL);

                stmt = conn.createStatement();
                rs = stmt.executeQuery(productCategorySQL);
                String groupFlag = null;
                String parentCategoryId = null;
                while (rs.next()) {
                    groupFlag = rs.getString("group_flag");
                    parentCategoryId = rs.getString("parent_category_id");

                    mLogger.createLogDebug("Group_Flag=" + groupFlag + "\nParent_CategoryId=" + parentCategoryId);

                    if (groupFlag.equalsIgnoreCase("Yes")) {
                        isProductCategoryGroup = true;
                        returnValue = categoryId;
                    } else {
                        // Go for the next category call
                        categoryId = parentCategoryId;
                    }
                }
            } catch (Exception ex) {
                mLogger.createLogError("Error in getGroupCategoryId.", ex);
            }
        }
        return returnValue;
    }

    public static void putRelatedProductResultInMap(HashMap savedRelatedProducts, ResultSet prodResults, String groupCategoryId, String productId) {
        try {
            String firstCatId = null;
            String firstProdId = null;
            String secondCatId = null;
            String secondProdId = null;
            while (prodResults.next()) {
                firstCatId = prodResults.getString("groupCategoryId");
                firstProdId = prodResults.getString("productId");
                secondCatId = prodResults.getString("relatedGroupCategoryId");
                secondProdId = prodResults.getString("relatedProductId");
                if (firstCatId.equalsIgnoreCase(groupCategoryId) && firstProdId.equalsIgnoreCase(productId)) {
                    // Second one is related product.
                    updateRelatedProductsMap(savedRelatedProducts, secondCatId, secondProdId);
                } else {
                    // First one is related product.
                    updateRelatedProductsMap(savedRelatedProducts, firstCatId, firstProdId);
                }
            }
        } catch (Exception ex) {
            mLogger.createLogError("Error in putRelatedProductResultInMap.", ex);
        }
    }

    public static ResultSet getRelatedProducts(String groupCategoryId, String productId, Connection conn) {
        ResultSet rs = null;
        try {

            // Making a Union call because the RELATED_PRODUCTS relational table can hold the relation in 2 sets of interchangeable columns.
            String relatedProductsSQL = "select * from related_products where groupCategoryId='" + groupCategoryId + "' and productId='" + productId + "'";
            relatedProductsSQL += " union ";
            relatedProductsSQL += "select * from related_products where relatedGroupCategoryId='" + groupCategoryId + "' and relatedProductId='" + productId + "'";
            mLogger.createLogDebug("productCategorySQL=" + relatedProductsSQL);
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(relatedProductsSQL);
        } catch (Exception ex) {
            mLogger.createLogError("Error in getRelatedProducts.", ex);
        }
        return rs;
    }

    private static void addLocaleRelatedCategoryDeletes(Statement stmt, String groupCategoryId, String deleteCategoryId, Connection stgConn, String firstDeleteSQL, String secondDeleteSQL) {
        try {
            /**
             * The groupCategoryId value is the en_Asia category.
             * Call for ProductCategoryMapper table and find the corresponding locale categories and create batch delete statements.
             */
            PreparedStatement pStatement = null;
            ResultSet rs = null;

            String en_AU_del1 = "";
            String en_AU_del2 = "";
            String sc_CN_del1 = "";
            String sc_CN_del2 = "";
            String en_HK_del1 = "";
            String en_HK_del2 = "";
            String tc_HK_del1 = "";
            String tc_HK_del2 = "";
            String en_IN_del1 = "";
            String en_IN_del2 = "";
            String en_MY_del1 = "";
            String en_MY_del2 = "";
            String en_SG_del1 = "";
            String en_SG_del2 = "";
            String en_ME_del1 = "";
            String en_ME_del2 = "";
            String th_TH_del1 = "";
            String th_TH_del2 = "";
            String en_NZ_del1 = "";
            String en_NZ_del2 = "";
            String vi_VN_del1 = "";
            String vi_VN_del2 = "";
            String en_KW_del1 = "";
            String en_KW_del2 = "";
            String en_QA_del1 = "";
            String en_QA_del2 = "";
            String tr_TR_del1 = "";
            String tr_TR_del2 = "";
            String en_AE_del1 = "";
            String en_AE_del2 = "";
            String en_LB_del1 = "";
            String en_LB_del2 = "";

            String mapperSQL = "SELECT * FROM Product_Category_Mapper mapper where mapper.en_Asia_id=?";
            pStatement = stgConn.prepareStatement(mapperSQL);
            pStatement.setString(1, groupCategoryId);

            mLogger.createLogDebug(mapperSQL + "WITH en_Asia_id=" + groupCategoryId);
            rs = pStatement.executeQuery();

            while (rs.next()) {

                String en_AU_Id = rs.getString("en_AU");
                String sc_CN_Id = rs.getString("sc_CN");
                String en_HK_Id = rs.getString("en_HK");
                String tc_HK_Id = rs.getString("tc_HK");
                String en_IN_Id = rs.getString("en_IN");
                String en_MY_Id = rs.getString("en_MY");
                String en_SG_Id = rs.getString("en_SG");
                String en_ME_Id = rs.getString("en_ME");
                String th_TH_Id = rs.getString("th_TH");
                String en_NZ_Id = rs.getString("en_NZ");
                String vi_VN_Id = rs.getString("vi_VN");
                String en_KW_Id = rs.getString("en_KW");
                String en_QA_Id = rs.getString("en_QA");
                String tr_TR_Id = rs.getString("tr_TR");
                String en_AE_Id = rs.getString("en_AE");
                String en_LB_Id = rs.getString("en_LB");


                en_AU_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_AU_Id);
                en_AU_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_AU_Id);

                sc_CN_del1 = firstDeleteSQL.replaceAll(groupCategoryId, sc_CN_Id);
                sc_CN_del2 = secondDeleteSQL.replaceAll(groupCategoryId, sc_CN_Id);

                en_HK_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_HK_Id);
                en_HK_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_HK_Id);

                tc_HK_del1 = firstDeleteSQL.replaceAll(groupCategoryId, tc_HK_Id);
                tc_HK_del2 = secondDeleteSQL.replaceAll(groupCategoryId, tc_HK_Id);

                en_IN_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_IN_Id);
                en_IN_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_IN_Id);

                en_MY_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_MY_Id);
                en_MY_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_MY_Id);

                en_SG_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_SG_Id);
                en_SG_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_SG_Id);

                en_ME_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_ME_Id);
                en_ME_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_ME_Id);

                th_TH_del1 = firstDeleteSQL.replaceAll(groupCategoryId, th_TH_Id);
                th_TH_del2 = secondDeleteSQL.replaceAll(groupCategoryId, th_TH_Id);

                en_NZ_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_NZ_Id);
                en_NZ_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_NZ_Id);

                vi_VN_del1 = firstDeleteSQL.replaceAll(groupCategoryId, vi_VN_Id);
                vi_VN_del2 = secondDeleteSQL.replaceAll(groupCategoryId, vi_VN_Id);

                en_KW_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_KW_Id);
                en_KW_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_KW_Id);

                en_QA_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_QA_Id);
                en_QA_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_QA_Id);

                tr_TR_del1 = firstDeleteSQL.replaceAll(groupCategoryId, tr_TR_Id);
                tr_TR_del2 = secondDeleteSQL.replaceAll(groupCategoryId, tr_TR_Id);

                en_AE_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_AE_Id);
                en_AE_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_AE_Id);

                en_LB_del1 = firstDeleteSQL.replaceAll(groupCategoryId, en_LB_Id);
                en_LB_del2 = secondDeleteSQL.replaceAll(groupCategoryId, en_LB_Id);

            }

            // Call with deleteCategoryId

            pStatement = stgConn.prepareStatement(mapperSQL);
            pStatement.setString(1, deleteCategoryId);

            mLogger.createLogDebug(mapperSQL + "WITH en_Asia_id=" + deleteCategoryId);

            rs = pStatement.executeQuery();

            while (rs.next()) {

                String en_AU_Id = rs.getString("en_AU");
                String sc_CN_Id = rs.getString("sc_CN");
                String en_HK_Id = rs.getString("en_HK");
                String tc_HK_Id = rs.getString("tc_HK");
                String en_IN_Id = rs.getString("en_IN");
                String en_MY_Id = rs.getString("en_MY");
                String en_SG_Id = rs.getString("en_SG");
                String en_ME_Id = rs.getString("en_ME");
                String th_TH_Id = rs.getString("th_TH");
                String en_NZ_Id = rs.getString("en_NZ");
                String vi_VN_Id = rs.getString("vi_VN");
                String en_KW_Id = rs.getString("en_KW");
                String en_QA_Id = rs.getString("en_QA");
                String tr_TR_Id = rs.getString("tr_TR");
                String en_AE_Id = rs.getString("en_AE");
                String en_LB_Id = rs.getString("en_LB");


                en_AU_del1 = en_AU_del1.replaceAll(deleteCategoryId, en_AU_Id);
                en_AU_del2 = en_AU_del2.replaceAll(deleteCategoryId, en_AU_Id);

                sc_CN_del1 = sc_CN_del1.replaceAll(deleteCategoryId, sc_CN_Id);
                sc_CN_del2 = sc_CN_del2.replaceAll(deleteCategoryId, sc_CN_Id);

                en_HK_del1 = en_HK_del1.replaceAll(deleteCategoryId, en_HK_Id);
                en_HK_del2 = en_HK_del2.replaceAll(deleteCategoryId, en_HK_Id);

                tc_HK_del1 = tc_HK_del1.replaceAll(deleteCategoryId, tc_HK_Id);
                tc_HK_del2 = tc_HK_del2.replaceAll(deleteCategoryId, tc_HK_Id);

                en_IN_del1 = en_IN_del1.replaceAll(deleteCategoryId, en_IN_Id);
                en_IN_del2 = en_IN_del2.replaceAll(deleteCategoryId, en_IN_Id);

                en_MY_del1 = en_MY_del1.replaceAll(deleteCategoryId, en_MY_Id);
                en_MY_del2 = en_MY_del2.replaceAll(deleteCategoryId, en_MY_Id);

                en_SG_del1 = en_SG_del1.replaceAll(deleteCategoryId, en_SG_Id);
                en_SG_del2 = en_SG_del2.replaceAll(deleteCategoryId, en_SG_Id);

                en_ME_del1 = en_ME_del1.replaceAll(deleteCategoryId, en_ME_Id);
                en_ME_del2 = en_ME_del2.replaceAll(deleteCategoryId, en_ME_Id);

                th_TH_del1 = th_TH_del1.replaceAll(deleteCategoryId, th_TH_Id);
                th_TH_del2 = th_TH_del2.replaceAll(deleteCategoryId, th_TH_Id);

                en_NZ_del1 = en_NZ_del1.replaceAll(deleteCategoryId, en_NZ_Id);
                en_NZ_del2 = en_NZ_del2.replaceAll(deleteCategoryId, en_NZ_Id);

                vi_VN_del1 = vi_VN_del1.replaceAll(deleteCategoryId, vi_VN_Id);
                vi_VN_del2 = vi_VN_del2.replaceAll(deleteCategoryId, vi_VN_Id);

                en_KW_del1 = en_KW_del1.replaceAll(deleteCategoryId, en_KW_Id);
                en_KW_del2 = en_KW_del2.replaceAll(deleteCategoryId, en_KW_Id);

                en_QA_del1 = en_QA_del1.replaceAll(deleteCategoryId, en_QA_Id);
                en_QA_del2 = en_QA_del2.replaceAll(deleteCategoryId, en_QA_Id);

                tr_TR_del1 = tr_TR_del1.replaceAll(deleteCategoryId, tr_TR_Id);
                tr_TR_del2 = tr_TR_del2.replaceAll(deleteCategoryId, tr_TR_Id);

                en_AE_del1 = en_AE_del1.replaceAll(deleteCategoryId, en_AE_Id);
                en_AE_del2 = en_AE_del2.replaceAll(deleteCategoryId, en_AE_Id);

                en_LB_del1 = en_LB_del1.replaceAll(deleteCategoryId, en_LB_Id);
                en_LB_del2 = en_LB_del2.replaceAll(deleteCategoryId, en_LB_Id);

            }

            stmt.addBatch(en_AU_del1);
            stmt.addBatch(en_AU_del2);
            stmt.addBatch(sc_CN_del1);
            stmt.addBatch(sc_CN_del2);
            stmt.addBatch(en_HK_del1);
            stmt.addBatch(en_HK_del2);
            stmt.addBatch(tc_HK_del1);
            stmt.addBatch(tc_HK_del2);
            stmt.addBatch(en_IN_del1);
            stmt.addBatch(en_IN_del2);
            stmt.addBatch(en_MY_del1);
            stmt.addBatch(en_MY_del2);
            stmt.addBatch(en_SG_del1);
            stmt.addBatch(en_SG_del2);
            stmt.addBatch(en_ME_del1);
            stmt.addBatch(en_ME_del2);
            stmt.addBatch(th_TH_del1);
            stmt.addBatch(th_TH_del2);
            stmt.addBatch(en_NZ_del1);
            stmt.addBatch(en_NZ_del2);
            stmt.addBatch(vi_VN_del1);
            stmt.addBatch(vi_VN_del2);
            stmt.addBatch(en_KW_del1);
            stmt.addBatch(en_KW_del2);
            stmt.addBatch(en_QA_del1);
            stmt.addBatch(en_QA_del2);
            stmt.addBatch(tr_TR_del1);
            stmt.addBatch(tr_TR_del2);
            stmt.addBatch(en_AE_del1);
            stmt.addBatch(en_AE_del2);
            stmt.addBatch(en_LB_del1);
            stmt.addBatch(en_LB_del2);

        } catch (SQLException ex) {
            mLogger.createLogError("Error in addLocaleRelatedCategoryDeletes.", ex);
        }
    }

    private static void addLocaleRelatedCategoryInserts(Statement stmt, String groupCategoryId, String insertCategoryId, Connection stgConn, String insertSQL) {
        /**
         * The groupCategoryId value is the en_Asia category.
         * Call for ProductCategoryMapper table and find the corresponding locale categories and create batch insert statements.
         */
        try {
            PreparedStatement pStatement = null;
            ResultSet rs = null;

            String en_AU_ins = "";
            String sc_CN_ins = "";
            String en_HK_ins = "";
            String tc_HK_ins = "";
            String en_IN_ins = "";
            String en_MY_ins = "";
            String en_SG_ins = "";
            String en_ME_ins = "";
            String th_TH_ins = "";
            String en_NZ_ins = "";
            String vi_VN_ins = "";
            String en_KW_ins = "";
            String en_QA_ins = "";
            String tr_TR_ins = "";
            String en_AE_ins = "";
            String en_LB_ins = "";

            String mapperSQL = "SELECT * FROM Product_Category_Mapper mapper where mapper.en_Asia_id=?";
            pStatement = stgConn.prepareStatement(mapperSQL);
            pStatement.setString(1, groupCategoryId);

            mLogger.createLogDebug(mapperSQL + "WITH en_Asia_id=" + groupCategoryId);
            rs = pStatement.executeQuery();

            while (rs.next()) {

                String en_AU_Id = rs.getString("en_AU");
                String sc_CN_Id = rs.getString("sc_CN");
                String en_HK_Id = rs.getString("en_HK");
                String tc_HK_Id = rs.getString("tc_HK");
                String en_IN_Id = rs.getString("en_IN");
                String en_MY_Id = rs.getString("en_MY");
                String en_SG_Id = rs.getString("en_SG");
                String en_ME_Id = rs.getString("en_ME");
                String th_TH_Id = rs.getString("th_TH");
                String en_NZ_Id = rs.getString("en_NZ");
                String vi_VN_Id = rs.getString("vi_VN");
                String en_KW_Id = rs.getString("en_KW");
                String en_QA_Id = rs.getString("en_QA");
                String tr_TR_Id = rs.getString("tr_TR");
                String en_AE_Id = rs.getString("en_AE");
                String en_LB_Id = rs.getString("en_LB");


                en_AU_ins = insertSQL.replaceAll(groupCategoryId, en_AU_Id);
                sc_CN_ins = insertSQL.replaceAll(groupCategoryId, sc_CN_Id);
                en_HK_ins = insertSQL.replaceAll(groupCategoryId, en_HK_Id);
                tc_HK_ins = insertSQL.replaceAll(groupCategoryId, tc_HK_Id);
                en_IN_ins = insertSQL.replaceAll(groupCategoryId, en_IN_Id);
                en_MY_ins = insertSQL.replaceAll(groupCategoryId, en_MY_Id);
                en_SG_ins = insertSQL.replaceAll(groupCategoryId, en_SG_Id);
                en_ME_ins = insertSQL.replaceAll(groupCategoryId, en_ME_Id);
                th_TH_ins = insertSQL.replaceAll(groupCategoryId, th_TH_Id);
                en_NZ_ins = insertSQL.replaceAll(groupCategoryId, en_NZ_Id);
                vi_VN_ins = insertSQL.replaceAll(groupCategoryId, vi_VN_Id);
                en_KW_ins = insertSQL.replaceAll(groupCategoryId, en_KW_Id);
                en_QA_ins = insertSQL.replaceAll(groupCategoryId, en_QA_Id);
                tr_TR_ins = insertSQL.replaceAll(groupCategoryId, tr_TR_Id);
                en_AE_ins = insertSQL.replaceAll(groupCategoryId, en_AE_Id);
                en_LB_ins = insertSQL.replaceAll(groupCategoryId, en_LB_Id);
            }

            // Call with deleteCategoryId

            pStatement = stgConn.prepareStatement(mapperSQL);
            pStatement.setString(1, insertCategoryId);

            mLogger.createLogDebug(mapperSQL + "WITH en_Asia_id=" + insertCategoryId);

            rs = pStatement.executeQuery();

            while (rs.next()) {

                String en_AU_Id = rs.getString("en_AU");
                String sc_CN_Id = rs.getString("sc_CN");
                String en_HK_Id = rs.getString("en_HK");
                String tc_HK_Id = rs.getString("tc_HK");
                String en_IN_Id = rs.getString("en_IN");
                String en_MY_Id = rs.getString("en_MY");
                String en_SG_Id = rs.getString("en_SG");
                String en_ME_Id = rs.getString("en_ME");
                String th_TH_Id = rs.getString("th_TH");
                String en_NZ_Id = rs.getString("en_NZ");
                String vi_VN_Id = rs.getString("vi_VN");
                String en_KW_Id = rs.getString("en_KW");
                String en_QA_Id = rs.getString("en_QA");
                String tr_TR_Id = rs.getString("tr_TR");
                String en_AE_Id = rs.getString("en_AE");
                String en_LB_Id = rs.getString("en_LB");


                en_AU_ins = en_AU_ins.replaceAll(insertCategoryId, en_AU_Id);
                sc_CN_ins = sc_CN_ins.replaceAll(insertCategoryId, sc_CN_Id);
                en_HK_ins = en_HK_ins.replaceAll(insertCategoryId, en_HK_Id);
                tc_HK_ins = tc_HK_ins.replaceAll(insertCategoryId, tc_HK_Id);
                en_IN_ins = en_IN_ins.replaceAll(insertCategoryId, en_IN_Id);
                en_MY_ins = en_MY_ins.replaceAll(insertCategoryId, en_MY_Id);
                en_SG_ins = en_SG_ins.replaceAll(insertCategoryId, en_SG_Id);
                en_ME_ins = en_ME_ins.replaceAll(insertCategoryId, en_ME_Id);
                th_TH_ins = th_TH_ins.replaceAll(insertCategoryId, th_TH_Id);
                en_NZ_ins = en_NZ_ins.replaceAll(insertCategoryId, en_NZ_Id);
                vi_VN_ins = vi_VN_ins.replaceAll(insertCategoryId, vi_VN_Id);
                en_KW_ins = en_KW_ins.replaceAll(insertCategoryId, en_KW_Id);
                en_QA_ins = en_QA_ins.replaceAll(insertCategoryId, en_QA_Id);
                tr_TR_ins = tr_TR_ins.replaceAll(insertCategoryId, tr_TR_Id);
                en_AE_ins = en_AE_ins.replaceAll(insertCategoryId, en_AE_Id);
                en_LB_ins = en_LB_ins.replaceAll(insertCategoryId, en_LB_Id);
            }

            stmt.addBatch(en_AU_ins);
            stmt.addBatch(sc_CN_ins);
            stmt.addBatch(en_HK_ins);
            stmt.addBatch(tc_HK_ins);
            stmt.addBatch(en_IN_ins);
            stmt.addBatch(en_MY_ins);
            stmt.addBatch(en_SG_ins);
            stmt.addBatch(en_ME_ins);
            stmt.addBatch(th_TH_ins);
            stmt.addBatch(en_NZ_ins);
            stmt.addBatch(vi_VN_ins);
            stmt.addBatch(en_KW_ins);
            stmt.addBatch(en_QA_ins);
            stmt.addBatch(tr_TR_ins);
            stmt.addBatch(en_AE_ins);
            stmt.addBatch(en_LB_ins);

        } catch (SQLException ex) {
            mLogger.createLogError("Error in addLocaleRelatedCategoryInserts.", ex);
        }

    }
}
