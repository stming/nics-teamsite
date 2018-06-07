/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow.dcr;

import nhk.ts.wcms.common.Logger;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.workflow.WorkflowUtils;
import com.interwoven.livesite.workflow.web.task.AbstractAjaxWebTask;
import com.interwoven.livesite.workflow.web.task.AjaxWebTaskContext;
import com.interwoven.livesite.workflow.web.task.WebTaskContext;
import java.io.File;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nhk.ts.wcms.common.IOHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author rapatel
 */
public class AssociateCategoryTask extends AbstractAjaxWebTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.dcr.AssociateCategoryTask"));
    public static final String ASSOCIATED_CATEGORY_ID = "AssociatedCategoryId";
    public static final String ASSOCIATED_CATEGORY_NAME = "AssociatedCategoryName";
    public static final String ASSOCIATED_CATEGORY_L1 = "AssocCat1";
    public static final String ASSOCIATED_CATEGORY_L2 = "AssocCat2";
    public static final String ASSOCIATED_CATEGORY_L3 = "AssocCat3";
    public static final String ASSOCIATED_CATEGORY_L4 = "AssocCat4";
    public static final String ASSOCIATED_CATEGORY_L5 = "AssocCat5";
    public static final String ASSOCIATED_CATEGORY_ID_L1 = "AssocCatID1";
    public static final String ASSOCIATED_CATEGORY_ID_L2 = "AssocCatID2";
    public static final String ASSOCIATED_CATEGORY_ID_L3 = "AssocCatID3";
    public static final String ASSOCIATED_CATEGORY_ID_L4 = "AssocCatID4";
    public static final String ASSOCIATED_CATEGORY_ID_L5 = "AssocCatID5";

    public void getProductCategory(WebTaskContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
        mLogger.createLogDebug("Called getProductCategory");
        AjaxWebTaskContext ajaxContext = (AjaxWebTaskContext) context;
        CSClient client = context.getClient();
        CSTask task = context.getTask();
        Document responseDoc = ajaxContext.getResponseDocument();
        Element responseElem = responseDoc.getRootElement();
        this.mLogger = new Logger(LogFactory.getLog(this.getClass()));
        String vpath = task.getArea().getVPath().toString();
        mLogger.createLogDebug("getProductCategory vpath=" + vpath);
        Collection<CSFile> files = WorkflowUtils.getTaskFileMap(task, client, false).values();
        mLogger.createLogDebug("getProductCategory files.size()=" + files.size());
        for (CSFile docFile : files) {
            String docVpath = docFile.getVPath().toString();
            mLogger.createLogDebug("File Path: " + docVpath);
            File dcrFile = new File(docVpath);
            Document dcrFileDocument = Dom4jUtils.newDocument(dcrFile);
            String productName = ProductConversionHelper.getProductName(dcrFileDocument);

            if (productName == null) {
                String errorMsg = "<h3>Product Name field missing in the DCR</h3><p>Fill in the product field to continue.</p><ol>";
                this.mLogger.createLogError(errorMsg, new Exception(errorMsg));
            }
            mLogger.createLogDebug("getProductCategory productName=" + productName);
            responseElem.addElement("ProductName").addText(productName);
        }
        String[] tmp = (String[]) context.getRequestParameters().get("ProductCategory");
        String categoryLevel = tmp[0];
        mLogger.createLogDebug("Category Level: " + categoryLevel);
        String sourceBranch = IOHelper.getPropertyValue("ConvertProductWF.SourceBranch");
        String mainWA = IOHelper.getPropertyValue("ConvertProductWF.TargetWA");
        String locale = IOHelper.getPropertyValue("ConvertProductWF.TargetLocale");
        String productlabel = IOHelper.getPropertyValue("ConvertProductWF.ProductLabel");
        if ((null != vpath) && (!("".equals(vpath)))) {
            try {
                responseElem.addElement("Result").addText("SUCCESS");
                Element categoriesElem = responseElem.addElement("categories");
                String sitemapFileVPath = vpath.substring(0, vpath.indexOf(sourceBranch)) + mainWA + "/sites/" + locale + "/default.sitemap";
                mLogger.createLogDebug("File Path: " + sitemapFileVPath);
                File sitemapFile = new File(sitemapFileVPath);
                Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);

                List<Node> productNode = null;
                if (categoryLevel.equalsIgnoreCase("Level1")) {
                    mLogger.createLogDebug("In Level 1");
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level2")) {
                    mLogger.createLogDebug("In Level 2:" + context.getRequestParameters().get("CategorySelected"));
                    String[] categorySelectedList = (String[]) context.getRequestParameters().get("CategorySelected");
                    String categorySelected = categorySelectedList[0];
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + categorySelected + "']/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level3")) {
                    String[] categorySelectedList = (String[]) context.getRequestParameters().get("CategorySelected");
                    String categorySelected = categorySelectedList[0];
                    String[] categorySelected1List = (String[]) context.getRequestParameters().get("CategorySelected1");
                    String categorySelected1 = categorySelected1List[0];
                    mLogger.createLogDebug("In Level 3:" + categorySelected + " " + categorySelected1);
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + categorySelected + "']/node[@id = '" + categorySelected1 + "']/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level4")) {
                    String[] categorySelectedList = (String[]) context.getRequestParameters().get("CategorySelected");
                    String categorySelected = categorySelectedList[0];
                    String[] categorySelected1List = (String[]) context.getRequestParameters().get("CategorySelected1");
                    String categorySelected1 = categorySelected1List[0];
                    String[] categorySelected2List = (String[]) context.getRequestParameters().get("CategorySelected2");
                    String categorySelected2 = categorySelected2List[0];
                    mLogger.createLogDebug("In Level 4:" + categorySelected + " " + categorySelected1 + " " + categorySelected2);
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + categorySelected + "']/node[@id = '" + categorySelected1 + "']/node[@id = '" + categorySelected2 + "']/node");
                }
                if (categoryLevel.equalsIgnoreCase("Level5")) {
                    String[] categorySelectedList = (String[]) context.getRequestParameters().get("CategorySelected");
                    String categorySelected = categorySelectedList[0];
                    String[] categorySelected1List = (String[]) context.getRequestParameters().get("CategorySelected1");
                    String categorySelected1 = categorySelected1List[0];
                    String[] categorySelected2List = (String[]) context.getRequestParameters().get("CategorySelected2");
                    String categorySelected2 = categorySelected2List[0];
                    String[] categorySelected3List = (String[]) context.getRequestParameters().get("CategorySelected3");
                    String categorySelected3 = categorySelected3List[0];
                    mLogger.createLogDebug("In Level 5:" + categorySelected + " " + categorySelected1 + " " + categorySelected2 + " " + categorySelected3);
                    productNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + productlabel + "']]/node[@id = '" + categorySelected + "']/node[@id = '" + categorySelected1 + "']/node[@id = '" + categorySelected2 + "']/node[@id = '" + categorySelected3 + "']/node");
                }
                if (productNode.size() != 0) {
                    mLogger.createLogDebug("getProductCategory productNode.size=" + productNode.size());
                    Element emptyCategory = categoriesElem.addElement("category").addAttribute("id", "");
                    emptyCategory.addText("Select Category");
                    for (Node unitNode : productNode) {
                        Element el = (Element) unitNode;

                        String displayValue = "";

                        Node description = unitNode.selectSingleNode("description");
                        if (description != null && StringUtils.isNotEmpty(description.getText())) {
                            displayValue = description.getText();
                        } else {
                            displayValue = unitNode.selectSingleNode("label").getText();
                        }
                        Element category = categoriesElem.addElement("category").addAttribute("id", (String) el.attributeValue("id"));
                        category.addText(displayValue);
                        mLogger.createLogDebug("category Id=" + el.attributeValue("id") + "category Name=" + displayValue);
                    }
                }
                mLogger.createLogDebug("AssociateCategoryTask.getProductCategory response=" + responseElem.asXML());
            } catch (Exception e) {
                mLogger.createLogDebug("Error retrieving getProductCategory::", e);
            }
        }
    }

    public void associateCategory(WebTaskContext context, HttpServletRequest request, HttpServletResponse response) throws CSException {
        AjaxWebTaskContext ajaxContext = (AjaxWebTaskContext) context;
        CSClient client = context.getClient();
        CSTask task = context.getTask();
        CSWorkflow job = context.getWorkflow();
        Document responseDoc = ajaxContext.getResponseDocument();
        Element responseElem = responseDoc.getRootElement();
        try {
            String[] tmp = (String[]) context.getRequestParameters().get("associateCategoryId");
            String[] tmp1 = (String[]) context.getRequestParameters().get("associateCategoryName");

            String[] tmp2 = (String[]) context.getRequestParameters().get("categoryLevel1");
            String[] tmp3 = (String[]) context.getRequestParameters().get("categoryLevel2");
            String[] tmp4 = (String[]) context.getRequestParameters().get("categoryLevel3");
            String[] tmp5 = (String[]) context.getRequestParameters().get("categoryLevel4");
            String[] tmp6 = (String[]) context.getRequestParameters().get("categoryLevel5");
            String[] tmp7 = (String[]) context.getRequestParameters().get("categoryLevelId1");
            String[] tmp8 = (String[]) context.getRequestParameters().get("categoryLevelId2");
            String[] tmp9 = (String[]) context.getRequestParameters().get("categoryLevelId3");
            String[] tmp10 = (String[]) context.getRequestParameters().get("categoryLevelId4");
            String[] tmp11 = (String[]) context.getRequestParameters().get("categoryLevelId5");

            String taskToTagCategories = task.getVariable("TaskToTagCategories");
            mLogger.createLogDebug("taskToTagCategories: " + taskToTagCategories);
            String associatedCatId = tmp[0];
            String associatedCatName = tmp1[0];
            String catLevel1 = tmp2[0];
            String catLevel2 = tmp3[0];
            String catLevel3 = tmp4[0];
            String catLevel4 = tmp5[0];
            String catLevel5 = tmp6[0];
            String catLevelId1 = tmp7[0];
            String catLevelId2 = tmp8[0];
            String catLevelId3 = tmp9[0];
            String catLevelId4 = tmp10[0];
            String catLevelId5 = tmp11[0];
            mLogger.createLogDebug("New Cat Id to set: " + associatedCatId);
            mLogger.createLogDebug("New Cat Name to set: " + associatedCatName);
            mLogger.createLogDebug("Cat Level 1: " + catLevel1);
            mLogger.createLogDebug("Cat Level 2: " + catLevel2);
            mLogger.createLogDebug("Cat Level 3: " + catLevel3);
            mLogger.createLogDebug("Cat Level 4: " + catLevel4);
            mLogger.createLogDebug("Cat Level 5: " + catLevel5);
            mLogger.createLogDebug("Cat Level Id 1: " + catLevelId1);
            mLogger.createLogDebug("Cat Level Id 2: " + catLevelId2);
            mLogger.createLogDebug("Cat Level Id 3: " + catLevelId3);
            mLogger.createLogDebug("Cat Level Id 4: " + catLevelId4);
            mLogger.createLogDebug("Cat Level Id 5: " + catLevelId5);

            CSTask createProductDCRTask = WorkflowUtils.getTaskByName(task.getWorkflow(), taskToTagCategories.trim());

            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_ID, associatedCatId);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_NAME, associatedCatName);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_L1, catLevel1);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_L2, catLevel2);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_L3, catLevel3);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_L4, catLevel4);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_L5, catLevel5);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_ID_L1, catLevelId1);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_ID_L2, catLevelId2);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_ID_L3, catLevelId3);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_ID_L4, catLevelId4);
            createProductDCRTask.setVariable(ASSOCIATED_CATEGORY_ID_L5, catLevelId5);

            responseElem.addElement("Result").addText("SUCCESS");
            responseElem.addElement("Message").addText("The variable " + ASSOCIATED_CATEGORY_ID + " of the task " + taskToTagCategories + " was set to " + associatedCatId);
            responseElem.addElement("Message").addText("The variable " + ASSOCIATED_CATEGORY_NAME + " of the task " + taskToTagCategories + " was set to " + associatedCatName);
        } catch (Exception e) {
            mLogger.createLogDebug("Error in associateCategory::", e);
        }
    }
}
