/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow.dcr;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;
import java.util.Hashtable;
import java.io.FileOutputStream;
import nhk.ts.wcms.common.IOHelper;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSNode;
import com.interwoven.cssdk.filesys.CSObjectAlreadyExistsException;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nhk.ts.wcms.common.CommonUtil;
import nhk.ts.wcms.common.TSHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author smukherj
 */
public class CreateProductDCR implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.dcr.CreateProductDCR"));
    private static final String DCR_TYPE_EA = IOHelper.getString("FileTypeChecker.dcrTypeEAKey");
    private static final String PRODUCT_RELATED_EA = IOHelper.getString("CreateProductDCR.productRelatedEAKey");
    private static final String PRODUCT_NAME_EA = IOHelper.getString("CreateProductDCR.productNameEAKey");
    private static final String PRODUCT_DEV_CODE_EA = IOHelper.getString("CreateProductDCR.productDevCodeEAKey");
    private static final String CATEGORY_LEVEL1_EA = IOHelper.getString("CreateProductDCR.productCat1EAKey");
    private static final String CATEGORY_LEVEL2_EA = IOHelper.getString("CreateProductDCR.productCat2EAKey");
    private static final String CATEGORY_LEVEL3_EA = IOHelper.getString("CreateProductDCR.productCat3EAKey");
    private static final String CATEGORY_LEVEL4_EA = IOHelper.getString("CreateProductDCR.productCat4EAKey");
    private static final String CATEGORY_LEVEL5_EA = IOHelper.getString("CreateProductDCR.productCat5EAKey");

    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {

        Document resultDoc = Dom4jUtils.newDocument();
        String productName = null;
        try {
            Document mainContent = null;
            Document specs = null;
            Document meta = null;
            ArrayList<String> listOfKeyFeaturesFiles = null;
            Element resultElement = resultDoc.addElement("saleable_product_information");
            CSAreaRelativePath[] mainContentAreaRelPaths = task.getFiles();
            for (CSAreaRelativePath mainContentAreaRelPath : mainContentAreaRelPaths) {

                String mcRelPathStr = mainContentAreaRelPath.toString();
                String prodDevCode = mcRelPathStr.substring(mcRelPathStr.lastIndexOf("/") + 1, mcRelPathStr.lastIndexOf("."));
                String specRelPathStr = mcRelPathStr.replaceAll("/main_contents/", "/spec/");
                String kfRelPathStr = mcRelPathStr.replaceAll("/main_contents/", "/key_features/");
                String mbmetaRelPathStr = mcRelPathStr.replaceAll("/main_contents/", "/mbmeta/");

                CSAreaRelativePath specAreaRelPath = new CSAreaRelativePath(specRelPathStr);
                CSAreaRelativePath mbmetaAreaRelPath = new CSAreaRelativePath(mbmetaRelPathStr);

                CSFile tmp0 = task.getArea().getFile(mainContentAreaRelPath);
                CSFile tmp1 = task.getArea().getFile(specAreaRelPath);
                CSFile tmp2 = task.getArea().getFile(mbmetaAreaRelPath);

                CSSimpleFile file0 = null;
                CSSimpleFile file1 = null;
                CSSimpleFile file2 = null;

                if (tmp0 instanceof CSSimpleFile) {
                    try {

                        file0 = (CSSimpleFile) tmp0;
                        String mainContentsDCR = TSHelper.readTSFileContents((CSSimpleFile) file0);
                        mainContent = DocumentHelper.parseText(mainContentsDCR);

                        if (tmp1 instanceof CSSimpleFile) {
                            file1 = (CSSimpleFile) tmp1;
                            String specsDCR = TSHelper.readTSFileContents((CSSimpleFile) file1);
                            specs = DocumentHelper.parseText(specsDCR);
                        }
                        if (tmp2 instanceof CSSimpleFile) {
                            file2 = (CSSimpleFile) tmp2;
                            String metaDCR = TSHelper.readTSFileContents((CSSimpleFile) file2);
                            meta = DocumentHelper.parseText(metaDCR);
                        }
                        productName = ProductConversionHelper.getProductName(mainContent);
                        String productFileName = productName.replaceAll("[^a-zA-Z 0-9]+", "");
                        String associatedCatId = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID);
                        String associatedCatName = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_NAME);
                        mLogger.createLogDebug("CreateProductDCR: associatedCatId=" + associatedCatId);
                        mLogger.createLogDebug("CreateProductDCR: associatedCatName=" + associatedCatName);
                        CSWorkarea targWA = getPathToTargetWorkArea(client, task);
                        String locale = IOHelper.getPropertyValue("ConvertProductWF.TargetLocale");

                        String categoryRelativePath = generatePathBasedOnCategoryName(task);
                        mLogger.createLogDebug("categoryRelativePath=" + categoryRelativePath);
                        // Added Folder Name as the product name
                        String keyFeaturesDirectoryPath = "templatedata/" + locale + IOHelper.getString("CreateProductDCR.keyFeaturesDCRPath") + categoryRelativePath.trim() + productFileName.trim() + "/";


                        listOfKeyFeaturesFiles = copyKeyFeaturesIntoSGBranch(client, file0, kfRelPathStr, targWA, prodDevCode, locale, task, keyFeaturesDirectoryPath, productName);
                        String areaRelPath = "templatedata/" + locale + IOHelper.getString("ValidateContentTask.productDCRPath") + categoryRelativePath + productFileName + ".xml";
                        mLogger.createLogDebug("areaRelPath=" + areaRelPath);
                        createProductDCR(task, resultElement, mainContent, specs, meta, listOfKeyFeaturesFiles, associatedCatId, associatedCatName, areaRelPath, productName);
                        String targetDCRFullPath = targWA.getVPath() + "/" + areaRelPath;

                        mLogger.createLogDebug("CreateProductDCR:About to create " + targetDCRFullPath);
                        TSHelper.createDirectoryInTargetWA(targWA, "templatedata/" + locale + IOHelper.getString("ValidateContentTask.productDCRPath") + categoryRelativePath, null);

                        // Delete the file, if existing, from the target location
                        CSAreaRelativePath csAreaRelPath = new CSAreaRelativePath(areaRelPath);
                        CSAreaRelativePath[] targetAreaRelPathsForDeletion = new CSAreaRelativePath[1];
                        targetAreaRelPathsForDeletion[0] = csAreaRelPath;
                        targWA.deleteFiles(targetAreaRelPathsForDeletion);

                        // Now, create the new file
                        FileOutputStream out = new FileOutputStream(targetDCRFullPath);
                        Writer w = new OutputStreamWriter(out, "UTF-8");
                        w.write("<?xml version='1.0' encoding='UTF-8'?>");
                        w.write(resultElement.asXML());
                        w.close();
                        addMetaInformation(client, productFileName, locale, prodDevCode, targetDCRFullPath, task);
                    } catch (Exception e) {
                        mLogger.createLogDebug("Error in execute method::", e);
                    }
                }
            }
            // choose transition from WF variable
            mLogger.createLogDebug("listOfKeyFeaturesFiles=" + listOfKeyFeaturesFiles);
            task.chooseTransition(task.getTransitions()[0], "successfully created the " + productName + " file with the meta. Also copied " + ((listOfKeyFeaturesFiles != null) ? listOfKeyFeaturesFiles.size() : "0") + " files.");
        } catch (CSException exception) {
            mLogger.createLogDebug("Error in creating the DCR file:", exception);
        }
    }

    private void createProductDCR(CSExternalTask task, Element resultElement, Document mainContent, Document specs, Document meta, ArrayList<String> listOfKeyFeaturesFiles, String associatedCatId, String associatedCatName, String areaRelPath, String productName) throws Exception {

        createOverviewCtr(task, resultElement, mainContent, meta, listOfKeyFeaturesFiles, associatedCatId, associatedCatName, areaRelPath, productName);
        createSupplementaryLinkCtr(resultElement);
        createSupportCtr(resultElement);
        createLearnCtr(resultElement);
        createAwardsReviewCtr(resultElement);
        createColourCtr(resultElement);
        createAdditionalInformationCtr(resultElement);
        createRelatedProductCtr(resultElement, mainContent);
        createSpecificationsCtr(resultElement, specs);
        createSampleImagesCtr(resultElement, mainContent);
        createFeaturesPickupCtr(resultElement, mainContent);
    }

    private void createOverviewCtr(CSExternalTask task, Element resultElement, Document mainContent, Document meta, ArrayList<String> listOfKeyFeaturesFiles, String associatedCatId, String associatedCatName, String areaRelPath, String productName) throws Exception {
        Element overviewCtrEle = resultElement.addElement("OverviewCtr");
        overviewCtrEle.addElement("ProductID");
        // Mapping the product_name field of main_content
        mLogger.createLogDebug("createOverviewCtr adding product_name");
        overviewCtrEle.addElement("ProductName").addText(productName);
        overviewCtrEle.addElement("ProductNameImage");
        overviewCtrEle.addElement("ProductImage");
        overviewCtrEle.addElement("ProductCode");
        overviewCtrEle.addElement("Image");
        overviewCtrEle.addElement("Category").addText(associatedCatId);
        overviewCtrEle.addElement("CategoryName").addText(associatedCatName);

        overviewCtrEle.addElement("L1CategoryName").addText(task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L1));
        overviewCtrEle.addElement("L2CategoryName").addText(task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L2));
        overviewCtrEle.addElement("L3CategoryName").addText(task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L3));
        overviewCtrEle.addElement("L4CategoryName").addText(task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L4));
        overviewCtrEle.addElement("L5CategoryName").addText(task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L5));

        overviewCtrEle.addElement("Summary");
        overviewCtrEle.addElement("Price");
        // Mapping the wwa date from the mbmeta field
        mLogger.createLogDebug("createOverviewCtr adding wwa");
        if (meta != null) {
            Node wwaNode = meta.selectSingleNode("//wwa");
            if (wwaNode != null) {
                String reFormattedDate = StringUtils.isNotEmpty(wwaNode.getText()) ? CommonUtil.convertDateFormat(wwaNode.getText(), "dd MMM yy HH:mm:ss z", "yyyy-MM-dd HH:mm:ss") : "";
                mLogger.createLogDebug("createOverviewCtr WWA Date Conversion\nInput =" + wwaNode.getText() + "\nOutput=" + reFormattedDate);
                overviewCtrEle.addElement("AnnouncementDate").addText(reFormattedDate);
            }
        }
        overviewCtrEle.addElement("QuickViewData");
        overviewCtrEle.addElement("Status");
        Element dcrPathEle = overviewCtrEle.addElement("Link_Parameter");
        dcrPathEle.addText(areaRelPath);
        overviewCtrEle.addElement("DoNotShowOnHomePage").addText("Yes");
        mLogger.createLogDebug("createOverviewCtr now adding listOfKeyFeaturesFiles(if not empty)");
        if (CollectionUtils.isNotEmpty(listOfKeyFeaturesFiles)) {
            for (Iterator<String> it = listOfKeyFeaturesFiles.iterator(); it.hasNext();) {
                Element keyFeatureEle = overviewCtrEle.addElement("KeyFeatureDCRPath");
                keyFeatureEle.addText(it.next());
            }
        }
        mLogger.createLogDebug("createOverviewCtr completed overviewCtr");
    }

    private void createSupplementaryLinkCtr(Element resultElement) {
        Element suppLinkCtrEle = resultElement.addElement("SupplementaryLinkCtr");
        suppLinkCtrEle.addElement("SupplementaryIcon");
        suppLinkCtrEle.addElement("AltText");
        suppLinkCtrEle.addElement("Text");
        suppLinkCtrEle.addElement("Link");
        suppLinkCtrEle.addElement("Target");
        suppLinkCtrEle.addElement("Width");
        suppLinkCtrEle.addElement("Height");
    }

    private void createSupportCtr(Element resultElement) {
        Element supportCtrEle = resultElement.addElement("SupportCtr");
        supportCtrEle.addElement("supportCtrEle");
        supportCtrEle.addElement("SupportLink");
        supportCtrEle.addElement("Target");
        supportCtrEle.addElement("Width");
        supportCtrEle.addElement("Height");
    }

    private void createLearnCtr(Element resultElement) {
        Element learnCtrEle = resultElement.addElement("LearnCtr");
        learnCtrEle.addElement("Title");
        learnCtrEle.addElement("Description");
    }

    private void createAwardsReviewCtr(Element resultElement) {
        Element awardsReviewCtrEle = resultElement.addElement("Awards-ReviewCtr");
        awardsReviewCtrEle.addElement("AwardImage");
        awardsReviewCtrEle.addElement("AwardLink");
        awardsReviewCtrEle.addElement("Target");
        awardsReviewCtrEle.addElement("Width");
        awardsReviewCtrEle.addElement("Height");
    }

    private void createColourCtr(Element resultElement) {
        Element colourCtrEle = resultElement.addElement("ColourCtr");
        colourCtrEle.addElement("Name");
        colourCtrEle.addElement("Image");
        Element imageEle = colourCtrEle.addElement("ProductImageCtr");
        imageEle.addElement("ProductImage");
    }

    private void createAdditionalInformationCtr(Element resultElement) {
        Element additionalInformationCtrEle = resultElement.addElement("AdditionalInformationCtr");
        additionalInformationCtrEle.addElement("AvailableInEShop").addText("No");
        additionalInformationCtrEle.addElement("ArchiveFlag").addText("No");
        additionalInformationCtrEle.addElement("ComingSoonFlag").addText("No");
        additionalInformationCtrEle.addElement("SortOrder").addText("0");
    }

    private void createRelatedProductCtr(Element resultElement, Document mainContent) {
        Element relatedProductCtrEle = resultElement.addElement("RelatedProductCtr");
        relatedProductCtrEle.addElement("RelatedProductCategory");
        relatedProductCtrEle.addElement("RelatedProductCategoryName");
        relatedProductCtrEle.addElement("RelatedProductName");
    }

    private void createSpecificationsCtr(Element resultElement, Document specs) {

        Element specificationsCtrEle = resultElement.addElement("SpecificationsCtr");
        specificationsCtrEle.addElement("SpecificationLabel");
        List categoryL1s;
        if (specs != null) {

            categoryL1s = specs.selectNodes("//category_level_1");
            if (CollectionUtils.isNotEmpty(categoryL1s)) {
                for (Object l1obj : categoryL1s) {

                    Element categoryL1 = (Element) l1obj;
                    specificationsCtrEle.add(categoryL1.createCopy());

                }
            }
            if (specs.selectSingleNode("//note") != null) {
                specificationsCtrEle.add(((Element) specs.selectSingleNode("//note")).createCopy());
            }
        }
    }

    private void createSampleImagesCtr(Element resultElement, Document mainContent) {
        resultElement.add(((Element) mainContent.selectSingleNode("//sample_images")).createCopy());
        Node sampleMoviesNode = mainContent.selectSingleNode("//sample_movies");
        if (sampleMoviesNode != null) {
            resultElement.add(((Element) sampleMoviesNode).createCopy());
        }
    }

    private void createFeaturesPickupCtr(Element resultElement, Document mainContent) {
        resultElement.add(((Element) mainContent.selectSingleNode("//main_features")).createCopy());
        resultElement.add(((Element) mainContent.selectSingleNode("//lens")).createCopy());
    }

    private void addMetaInformation(CSClient client, String productName, String locale, String prodDevCode, String targetDCRFullPath, CSExternalTask task) throws Exception {

        mLogger.createLogDebug("Adding meta information");
        CSVPath pathToFile = new CSVPath(targetDCRFullPath);
        CSFile file = client.getFile(pathToFile);
        CSExtendedAttribute extAttr = new CSExtendedAttribute(DCR_TYPE_EA, locale + "/saleable_product_information");
        CSExtendedAttribute[] attrList = new CSExtendedAttribute[9];
        attrList[0] = extAttr;
        extAttr = new CSExtendedAttribute(PRODUCT_RELATED_EA, "Yes");
        attrList[1] = extAttr;
        extAttr = new CSExtendedAttribute(PRODUCT_NAME_EA, productName);
        attrList[2] = extAttr;
        extAttr = new CSExtendedAttribute(PRODUCT_DEV_CODE_EA, prodDevCode);
        attrList[3] = extAttr;

        String catLevelId1 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L1);
        String catLevelId2 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L2);
        String catLevelId3 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L3);
        String catLevelId4 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L4);
        String catLevelId5 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L5);

        extAttr = new CSExtendedAttribute(CATEGORY_LEVEL1_EA, catLevelId1);
        attrList[4] = extAttr;
        extAttr = new CSExtendedAttribute(CATEGORY_LEVEL2_EA, catLevelId2);
        attrList[5] = extAttr;
        extAttr = new CSExtendedAttribute(CATEGORY_LEVEL3_EA, catLevelId3);
        attrList[6] = extAttr;
        extAttr = new CSExtendedAttribute(CATEGORY_LEVEL4_EA, catLevelId4);
        attrList[7] = extAttr;
        extAttr = new CSExtendedAttribute(CATEGORY_LEVEL5_EA, catLevelId5);
        attrList[8] = extAttr;

        ((CSSimpleFile) file).setExtendedAttributes(attrList);
    }

    private ArrayList<String> copyKeyFeaturesIntoSGBranch(CSClient client, CSSimpleFile file, String kfRelPathStr, CSWorkarea targWA, String prodDevCode, String locale, CSExternalTask task, String keyFeaturesDirectoryPath, String productName) throws Exception {
        ArrayList<String> fileList = new ArrayList<String>();
        mLogger.createLogDebug("copyKeyFeaturesIntoSGBranch path to workarea=" + targWA.getVPath().toString());
        if (targWA != null && targWA.isValid()) {
            ArrayList<CSFile> relatedFiles = getRelatedKeyFeaturesFiles(client, file, kfRelPathStr, locale);
            copyFilesToBranch(targWA, relatedFiles, productName, prodDevCode, locale, fileList, task, keyFeaturesDirectoryPath);
        }
        return fileList;
    }

    private CSWorkarea getPathToTargetWorkArea(CSClient client, CSExternalTask task) throws Exception {
        String sourceBranch = IOHelper.getPropertyValue("ConvertProductWF.SourceBranch");
        String mainWA = IOHelper.getPropertyValue("ConvertProductWF.TargetWA");

        String sourceAreaVPath = task.getArea().getVPath().toString();
        String targetAreaVPath = sourceAreaVPath.substring(0, sourceAreaVPath.indexOf(sourceBranch)) + mainWA;
        return client.getWorkarea(new CSVPath(targetAreaVPath), true);
    }

    private void copyFilesToBranch(CSWorkarea targWA, ArrayList<CSFile> relatedFiles, String productName, String prodDevCode, String locale, ArrayList<String> fileList, CSExternalTask task, String keyFeaturesDirectoryPath) {

        synchronized (relatedFiles) {
            String targetPath = "";
            for (CSFile sourceFile : relatedFiles) {
                try {
                    CSSimpleFile sourceSimpFile = (CSSimpleFile) sourceFile;
                    byte[] content = sourceSimpFile.read(0, -1);
                    targetPath = keyFeaturesDirectoryPath.trim() + sourceSimpFile.getName().trim();
                    TSHelper.createDirectoryInTargetWA(targWA, targetPath, null);
                    //   targetPath=targetPath.trim();
                    fileList.add(targetPath);
                    mLogger.createLogDebug("target Path: " + targetPath);
                    // Delete the file, if existing, on the target location
                    CSAreaRelativePath targetRelPath = new CSAreaRelativePath(targetPath);
                    CSAreaRelativePath[] targetRelPathArray = new CSAreaRelativePath[1];
                    targetRelPathArray[0] = targetRelPath;
                    targWA.deleteFiles(targetRelPathArray);
                    CSSimpleFile targFile = targWA.createSimpleFile(new CSAreaRelativePath(targetPath));
                    targFile.write(content, 0, content.length, false);
                    targFile.setExtendedAttributes(sourceSimpFile.getExtendedAttributes(null));
                    //Copy DCR Type and Locale EA to the copied files
                    ArrayList<CSExtendedAttribute> newEAs = new ArrayList<CSExtendedAttribute>();
                    CSExtendedAttribute dcrTypeEA = targFile.getExtendedAttribute(DCR_TYPE_EA);

                    // Modify from product_dcr/key_features to <locale>/key_features for the DCR to be modified using DCT.
                    dcrTypeEA.setValue(locale + "/key_features");

                    CSExtendedAttribute prodRelatedEA = new CSExtendedAttribute(PRODUCT_RELATED_EA, "Yes");
                    CSExtendedAttribute prodNameEA = new CSExtendedAttribute(PRODUCT_NAME_EA, productName);
                    CSExtendedAttribute prodDevCodeEA = new CSExtendedAttribute(PRODUCT_DEV_CODE_EA, prodDevCode);

                    CSExtendedAttribute categoryId1EA = new CSExtendedAttribute(CATEGORY_LEVEL1_EA, task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L1));
                    CSExtendedAttribute categoryId2EA = new CSExtendedAttribute(CATEGORY_LEVEL2_EA, task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L2));
                    CSExtendedAttribute categoryId3EA = new CSExtendedAttribute(CATEGORY_LEVEL3_EA, task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L3));
                    CSExtendedAttribute categoryId4EA = new CSExtendedAttribute(CATEGORY_LEVEL4_EA, task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L4));
                    CSExtendedAttribute categoryId5EA = new CSExtendedAttribute(CATEGORY_LEVEL5_EA, task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_ID_L5));

                    newEAs.add(dcrTypeEA);
                    newEAs.add(prodRelatedEA);
                    newEAs.add(prodNameEA);
                    newEAs.add(prodDevCodeEA);
                    newEAs.add(categoryId1EA);
                    newEAs.add(categoryId2EA);
                    newEAs.add(categoryId3EA);
                    newEAs.add(categoryId4EA);
                    newEAs.add(categoryId5EA);

                    targFile.setExtendedAttributes(newEAs.toArray(new CSExtendedAttribute[7]));

                } catch (CSObjectAlreadyExistsException oaeEx) {
                    mLogger.createLogDebug("Related key_feature file " + targetPath + " already exists on target", oaeEx);
                } catch (CSAuthorizationException ex) {
                    mLogger.createLogDebug("Error in copyFilesToBranch:", ex);
                } catch (CSObjectNotFoundException ex) {
                    mLogger.createLogDebug("Error in copyFilesToBranch:", ex);
                } catch (CSRemoteException ex) {
                    mLogger.createLogDebug("Error in copyFilesToBranch:", ex);
                } catch (CSExpiredSessionException ex) {
                    mLogger.createLogDebug("Error in copyFilesToBranch:", ex);
                } catch (CSException ex) {
                    mLogger.createLogDebug("Error in copyFilesToBranch:", ex);
                }
            }
        }
    }

    private ArrayList<CSFile> getRelatedKeyFeaturesFiles(CSClient client, CSSimpleFile mainContentsFile, String kfRelPathStr, String locale) throws Exception {

        ArrayList<CSFile> relatedFiles = new ArrayList<CSFile>();
        String dataFolderRelativePath = kfRelPathStr.substring(0, kfRelPathStr.lastIndexOf("/"));
        // Relative path upto data folder cause key-features may or may not exist
        String mcWaFilePath = mainContentsFile.getArea().getVPath().toString();
        String xmlFileName = kfRelPathStr.substring(kfRelPathStr.lastIndexOf("/") + 1, kfRelPathStr.lastIndexOf(".xml"));
        String waPath = mcWaFilePath + "/" + dataFolderRelativePath;
        CSVPath waVpath = new CSVPath(waPath);
        CSFile waFile = client.getFile(waVpath);
        mLogger.createLogDebug("getRelatedKeyFeaturesFiles waFile=" + waFile);
        if (waFile != null && waFile.isValid()) {
            mLogger.createLogDebug("getRelatedKeyFeaturesFiles waFile is valid");
            CSNode[] csNodes = waFile.getChildren();
            if (csNodes != null) {
                for (int i = 0; i < csNodes.length; i++) {
                    CSNode node = csNodes[i];
                    if (node.getVPath().toString().contains(xmlFileName)) {
                        String filePath = node.getVPath().toString();
                        CSVPath fileVPath = new CSVPath(filePath);
                        CSFile file = client.getFile(fileVPath);
                        relatedFiles.add(file);
                    }
                }
            }
        }
        return relatedFiles;
    }

    private String generatePathBasedOnCategoryName(CSExternalTask task) throws CSAuthorizationException, CSRemoteException, CSObjectNotFoundException, CSExpiredSessionException, CSException {

        String relPath = "";

        String catLevel1 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_L1);
        String catLevel2 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_L2);
        String catLevel3 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_L3);
        String catLevel4 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_L4);
        String catLevel5 = task.getVariable(AssociateCategoryTask.ASSOCIATED_CATEGORY_L5);
        mLogger.createLogDebug("generatePathBasedOnCategoryName: catLevel1=" + catLevel1);
        mLogger.createLogDebug("generatePathBasedOnCategoryName: catLevel2=" + catLevel2);
        mLogger.createLogDebug("generatePathBasedOnCategoryName: catLevel3=" + catLevel3);
        mLogger.createLogDebug("generatePathBasedOnCategoryName: catLevel4=" + catLevel4);
        mLogger.createLogDebug("generatePathBasedOnCategoryName: catLevel5=" + catLevel5);

        if (StringUtils.isNotEmpty(catLevel1)) {
            relPath += catLevel1 + "/";
            if (StringUtils.isNotEmpty(catLevel2)) {
                relPath += catLevel2 + "/";
                if (StringUtils.isNotEmpty(catLevel3)) {
                    relPath += catLevel3 + "/";
                    if (StringUtils.isNotEmpty(catLevel4)) {
                        relPath += catLevel4 + "/";
                        if (StringUtils.isNotEmpty(catLevel5)) {
                            relPath += catLevel5 + "/";
                        }
                    }
                }
            }
        }
        return relPath;
    }
}
