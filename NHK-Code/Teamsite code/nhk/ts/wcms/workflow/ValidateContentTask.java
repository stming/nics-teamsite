/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow;

import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import nhk.ts.wcms.common.Logger;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.livesite.workflow.web.task.AbstractAjaxWebTask;
import com.interwoven.livesite.workflow.web.task.AjaxWebTaskContext;
import com.interwoven.livesite.workflow.web.task.WebTaskContext;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.LogFactory;
import com.interwoven.cssdk.filesys.CSArea;
import com.interwoven.cssdk.filesys.CSNode;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.File;
import nhk.ts.wcms.common.CommonUtil;
import nhk.ts.wcms.common.FileTypeChecker;
import nhk.ts.wcms.common.IOHelper;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author sbhojnag
 */
public class ValidateContentTask extends AbstractAjaxWebTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.ValidateContentTask"));
    private static final String PRODUCT_RELATED_EA = IOHelper.getString("CreateProductDCR.productRelatedEAKey");
    private static final String PRODUCT_NAME_EA = IOHelper.getString("CreateProductDCR.productNameEAKey");
    private static final String PRODUCT_DEV_CODE_EA = IOHelper.getString("CreateProductDCR.productDevCodeEAKey");
    private static final String CATEGORY_LEVEL1_EA = IOHelper.getString("CreateProductDCR.productCat1EAKey");
    private static final String CATEGORY_LEVEL2_EA = IOHelper.getString("CreateProductDCR.productCat2EAKey");
    private static final String CATEGORY_LEVEL3_EA = IOHelper.getString("CreateProductDCR.productCat3EAKey");
    private static final String CATEGORY_LEVEL4_EA = IOHelper.getString("CreateProductDCR.productCat4EAKey");
    private static final String CATEGORY_LEVEL5_EA = IOHelper.getString("CreateProductDCR.productCat5EAKey");
    private static final String DCR_TYPE_EA = IOHelper.getString("FileTypeChecker.dcrTypeEAKey");

    public void validateContent(WebTaskContext context, HttpServletRequest request, HttpServletResponse response) throws CSException {
        AjaxWebTaskContext ajaxContext = (AjaxWebTaskContext) context;
        CSTask task = context.getTask();
        Document responseDoc = ajaxContext.getResponseDocument();
        Element responseElem = responseDoc.getRootElement();
        // Flag to skip metadata tagging
        mLogger.createLogDebug("No.of Files : " + task.getFiles().length);
        String locale = null;
        try {
            CSAreaRelativePath[] areaRelativePath = task.getFiles();
            CSArea area = task.getArea();
            mLogger.createLogDebug("Area " + area.getVPath());
            String invalidMsg = "<h3>UnTagged Files</h3><p>The following file(s) have not been tagged. Please Tag the Files before retrying.</p>";
            String related_product = null;
            String related_product_devcode = null;
            String related_category_level1 = null;
            String related_category_level2 = null;
            String related_category_level3 = null;
            String related_category_level4 = null;
            String related_category_level5 = null;
            List<String> untagged_list = new ArrayList<String>();
            List<String> file_related_product = new ArrayList<String>();
            List<String> file_nonrelated_products = new ArrayList<String>();
            List<String> nonrelated_file_list = new ArrayList<String>();
            List<String> related_files_list = new ArrayList<String>();
            int i = 0;

            for (CSAreaRelativePath csAreaRelativePath : areaRelativePath) {
                CSFile file = area.getFile(csAreaRelativePath);

                mLogger.createLogDebug("File: " + file.getName());
                if (!(file instanceof CSHole)) {
                    CSSimpleFile cssfile = (CSSimpleFile) file;

                    if (FileTypeChecker.isDcr(cssfile)) {

                        mLogger.createLogDebug("File is a DCR. Get the DCR type");

                        String dcrType = cssfile.getExtendedAttribute(DCR_TYPE_EA).getValue();

                        locale = dcrType.substring(0, dcrType.indexOf("/"));

                        mLogger.createLogDebug("Found locale=" + locale);
                    }

                    try {
                        mLogger.createLogDebug("Check Related Product");
                        String related_products = cssfile.getExtendedAttribute(PRODUCT_RELATED_EA).getValue();
                        mLogger.createLogDebug("EA for related Product: " + related_products);
                        if (related_products != null) {
                            if (related_products.equalsIgnoreCase("Yes")) {
                                mLogger.createLogDebug("Related Products " + related_products + file.getName());
                                file_related_product.add(file.getName());
                                mLogger.createLogDebug("file NAme " + file.getName());
                                if (related_product == null) {

                                    related_product = cssfile.getExtendedAttribute(PRODUCT_NAME_EA).getValue();
                                    related_category_level1 = cssfile.getExtendedAttribute(CATEGORY_LEVEL1_EA).getValue();
                                    //    mLogger.createLogDebug("Level 1: " + related_category_level1);
                                    related_category_level2 = cssfile.getExtendedAttribute(CATEGORY_LEVEL2_EA).getValue();
                                    //  mLogger.createLogDebug("Level 2: " + related_category_level2);
                                    related_category_level3 = cssfile.getExtendedAttribute(CATEGORY_LEVEL3_EA).getValue();
                                    //  mLogger.createLogDebug("Level 3: " + related_category_level3);
                                    related_category_level4 = cssfile.getExtendedAttribute(CATEGORY_LEVEL4_EA).getValue();
                                    //  mLogger.createLogDebug("Level 4: " + related_category_level4);
                                    related_category_level5 = cssfile.getExtendedAttribute(CATEGORY_LEVEL5_EA).getValue();
                                    //  mLogger.createLogDebug("Level 5: " + related_category_level5);
                                    //  mLogger.createLogDebug("Inside i if " + related_product);
                                    related_files_list.add(cssfile.getName());
                                } else if (related_product.equalsIgnoreCase(cssfile.getExtendedAttribute(PRODUCT_NAME_EA).getValue())) {
                                    //  mLogger.createLogDebug("Before else if: ");
                                    related_files_list.add(cssfile.getName());
                                } else {
                                    nonrelated_file_list.add(cssfile.getName());
                                }
                            } else {
                                file_nonrelated_products.add((file.getName()));
                            }
                        } else {
                            untagged_list.add(file.getName());
                        }
                    } catch (Exception e) {
                        this.mLogger.createLogError("One or more files are not Tagged" + e, new Exception(invalidMsg));
                    }
                }
                i++;
            }
            // Check if there are untagged files
            if (untagged_list.size() > 0) {

                String errorMsg = invalidMsg + "<ol>";
                for (String temp_untagged_file : untagged_list) {
                    errorMsg += "<li>" + temp_untagged_file + "</li>";
                }
                errorMsg += "</ol>";
                this.mLogger.createLogError(errorMsg, new Exception(errorMsg));
            }
            // Check if there are files with both related and not related to products
            if (file_related_product.size() > 0 && file_nonrelated_products.size() > 0) {
                String errorMsg = "<h3>Product Related and Non Related Files cannot be Published together</h3><p>The following is the list of product related files that are being published with this workflow.</p><ol>";
                for (String temp_related_product : file_related_product) {
                    errorMsg += "<li>" + temp_related_product + "</li>";
                }
                errorMsg += "</ol>";
                errorMsg += "<p>The following is the list of non product related files that are being published with this workflow.</p><ol>";
                for (String temp_non_related_product : file_nonrelated_products) {
                    errorMsg += "<li>" + temp_non_related_product + "</li>";
                }
                this.mLogger.createLogError(errorMsg, new Exception(errorMsg));
            }
            // Check if files from more than 1 product is attached
            if (nonrelated_file_list.size() > 0) {
                String errorMsg = "<h3>Files related to multiple Products cannot be published together.</h3><p>The workflow shall deploy the following product related files now.</p><ol>";
                for (String temp_related_product : nonrelated_file_list) {
                    errorMsg += "<li>" + temp_related_product + "</li>";
                }
                errorMsg += "</ol>";
                this.mLogger.createLogError(errorMsg, new Exception(errorMsg));
            }
            if (file_related_product.size() > 0 && file_nonrelated_products.size() <= 0) {


                // This method of getting locale will fail for multi-locale sites like en_HK, tc_HK

                //String locale = getLocale(area);


                mLogger.createLogDebug("Locale: " + locale);

                String productDCRDataPath = IOHelper.getString("ValidateContentTask.productDCRPath");

                String categoryIdHierarchyPath = related_category_level1 + "/";
                if (related_category_level2 != null) {
                    categoryIdHierarchyPath = categoryIdHierarchyPath + related_category_level2 + "/";
                }
                if (related_category_level3 != null) {
                    categoryIdHierarchyPath = categoryIdHierarchyPath + related_category_level3 + "/";
                }
                if (related_category_level4 != null) {
                    categoryIdHierarchyPath = categoryIdHierarchyPath + related_category_level4 + "/";
                }
                if (related_category_level5 != null) {
                    categoryIdHierarchyPath = categoryIdHierarchyPath + related_category_level5 + "/";
                }
                mLogger.createLogDebug("Category Hierarchy: " + categoryIdHierarchyPath);
                // Reading the productlabel
                Document propertyFileDocument = readPropertyFile(area.getVPath() + "/resources/properties/" + locale + "/SiteInfo.xml");
                Node node = propertyFileDocument.selectSingleNode("//entry[@key='productNodeLabel']");
                String productlabel = node.getText();

                String categoryNameHierarchyPath = convertToCategoryNamePath(categoryIdHierarchyPath, area, locale, productlabel);

                mLogger.createLogDebug("categoryNameHierarchyPath=" + categoryNameHierarchyPath);

                String WWADateString = null;
                String productDCRPath = "templatedata/" + locale + productDCRDataPath + categoryNameHierarchyPath + related_product.replaceAll("[^a-zA-Z 0-9]+", "") + ".xml";
                mLogger.createLogDebug("productDCRPath=" + productDCRPath);
                CSAreaRelativePath productDCRAreaRelPath = new CSAreaRelativePath(productDCRPath);

                CSFile tmp0 = task.getArea().getFile(productDCRAreaRelPath);
                //added the null condition
                if ((!(tmp0 instanceof CSHole)) && (tmp0 != null)) {

                    CSSimpleFile cssfile = (CSSimpleFile) tmp0;
                    try {
                        mLogger.createLogDebug("File Name: " + cssfile.getName());
                        mLogger.createLogDebug("File Attribute: " + cssfile.getKind());
                        related_product_devcode = cssfile.getExtendedAttribute(PRODUCT_DEV_CODE_EA).getValue();

                        if (related_product_devcode == null) {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        this.mLogger.createLogError("Product Dev Code cannot be Empty", new Exception("Product Dev Code cannot be Empty"));
                    }
                    mLogger.createLogDebug("Product DCR file found. About to load into a DOM object. related_product_devcode=" + related_product_devcode);

                    mLogger.createLogDebug("area.getVPath()=" + area.getVPath());
                    mLogger.createLogDebug("Trying to read Prod DCR filepath=" + area.getVPath() + "/" + productDCRPath);

                    File prodFile = new File(area.getVPath() + "/" + productDCRPath);
                    Document productDCRDoc = Dom4jUtils.newDocument(prodFile);

                    WWADateString = productDCRDoc.selectSingleNode(IOHelper.getString("ValidateContentTask.WWADateXPath")).getText();

                    // This is of "dd-MM-yyyy" format. Need to convert it to the common "yyyy-MM-dd kk:mm" format
                    WWADateString = CommonUtil.convertDateFormat(WWADateString, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm");
                } else {

                    String deployType = task.getWorkflow().getVariable("deployType");
                    mLogger.createLogDebug("Deploy Type: " + deployType);

                    if (!deployType.equalsIgnoreCase("yes")) {
                        // Non-immediate product-related deployment. In this case, the WWA must have been set.        
                        String errorMsg = "<h3>Files related to the current product cannot be published.</h3><p>The product DCR File " + related_product.replaceAll("[^a-zA-Z 0-9]+", "") + ".xml" + " is not found.</p>";
                        this.mLogger.createLogError(errorMsg, new Exception(errorMsg));
                    }
                }
                task.getWorkflow().setVariable("wwa", WWADateString);
                task.getWorkflow().setVariable("isproduct_related", "true");
                task.getWorkflow().setVariable("product_name", related_product);
                task.getWorkflow().setVariable("product_devcode", related_product_devcode);
            }
            responseElem.addElement("Result").addText("SUCCESS");
            responseElem.addElement("Message").addText("File validation successful.");
            mLogger.createLogDebug("Response Document: \n" + responseElem.asXML());
        } catch (Exception e) {
            mLogger.createLogError("Error in validating files: " + e.getMessage(), e);
        }
    }

    private String convertToCategoryNamePath(String categoryIdHierarchyPath, CSArea area, String locale, String productlabel) throws Exception {
        String result = "";
        mLogger.createLogDebug("convertToCategoryNamePath categoryIdHierarchyPath=" + categoryIdHierarchyPath);
        String[] categoryIds = StringUtils.split(categoryIdHierarchyPath, "/");

        String sitemapFileVPath = area.getVPath() + "/sites/" + locale + "/default.sitemap";

        mLogger.createLogDebug("File Path: " + sitemapFileVPath);
        File sitemapFile = new File(sitemapFileVPath);
        Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);

        String categoryName = "";
        Node selectedNode = null;
        for (int i = 0; i < categoryIds.length; i++) {
            String categoryId = categoryIds[i];

            if (i == 0) {
                selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryId
                        + "']/description");
                if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                    categoryName = selectedNode.getText().trim();
                } else {
                    selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                            + categoryId
                            + "']/label");
                    if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                        categoryName = selectedNode.getText().trim();
                    }
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + categoryId + " on sitemap is " + categoryName);
                result += categoryName + "/";
            } else if (i == 1) {

                selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + categoryId
                        + "']/description");
                if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                    categoryName = selectedNode.getText().trim();
                } else {
                    selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                            + categoryIds[0]
                            + "']/node[@id='"
                            + categoryId
                            + "']/label");
                    if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                        categoryName = selectedNode.getText().trim();
                    }
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + categoryId + " on sitemap is " + categoryName);
                result += categoryName + "/";
            } else if (i == 2) {

                selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + categoryIds[1]
                        + "']/node[@id='"
                        + categoryId
                        + "']/description");
                if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                    categoryName = selectedNode.getText().trim();
                    //  if(categoryName != null)
                    //  categoryName.trim();
                } else {
                    selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                            + categoryIds[0]
                            + "']/node[@id='"
                            + categoryIds[1]
                            + "']/node[@id='"
                            + categoryId
                            + "']/label");
                    categoryName = selectedNode.getText().trim();
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + categoryId + " on sitemap is " + categoryName);
                result += categoryName + "/";
            } else if (i == 3) {

                selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + categoryIds[1]
                        + "']/node[@id='"
                        + categoryIds[2]
                        + "']/node[@id='"
                        + categoryId
                        + "']/description");
                if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                    categoryName = selectedNode.getText().trim();

                } else {
                    selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                            + categoryIds[0]
                            + "']/node[@id='"
                            + categoryIds[1]
                            + "']/node[@id='"
                            + categoryIds[2]
                            + "']/node[@id='"
                            + categoryId
                            + "']/label");
                    if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                        categoryName = selectedNode.getText().trim();
                    }
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + categoryId + " on sitemap is " + categoryName);
                result += categoryName + "/";
            } else if (i == 4) {

                selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + categoryIds[1]
                        + "']/node[@id='"
                        + categoryIds[2]
                        + "']/node[@id='"
                        + categoryIds[3]
                        + "']/node[@id='"
                        + categoryId
                        + "']/description");
                if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                    categoryName = selectedNode.getText().trim();

                } else {
                    selectedNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                            + categoryIds[0]
                            + "']/node[@id='"
                            + categoryIds[1]
                            + "']/node[@id='"
                            + categoryIds[2]
                            + "']/node[@id='"
                            + categoryIds[3]
                            + "']/node[@id='"
                            + categoryId
                            + "']/label");
                    if (selectedNode != null && StringUtils.isNotEmpty(selectedNode.getText())) {
                        categoryName = selectedNode.getText().trim();
                    }
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + categoryId + " on sitemap is " + categoryName);
                result += categoryName + "/";
            }
        }
        return result;
    }

    private String getLocale(CSArea area) throws CSObjectNotFoundException, CSExpiredSessionException, CSException {
        String siteName = null;
        CSNode[] nodes = area.getChildren();
        for (CSNode cSNode : nodes) {
            if (cSNode.getVPath().getName().endsWith("sites")) {
                // This is the sites node
                String siteFolderPath = (cSNode.getChildren()[0]).getVPath().getName();
                siteName = siteFolderPath.substring(siteFolderPath.lastIndexOf("/") + 1);
            }
        }
        mLogger.createLogDebug("getLocale locale=" + siteName);
        return siteName;
    }

    private Document readPropertyFile(String propertyFullPath) {
        mLogger.createLogDebug("readPropertyFile propertyFullPath=" + propertyFullPath);
        File propertyFile = new File(propertyFullPath);
        Document propertyFileDocument = Dom4jUtils.newDocument(propertyFile);
        return propertyFileDocument;
    }
}
