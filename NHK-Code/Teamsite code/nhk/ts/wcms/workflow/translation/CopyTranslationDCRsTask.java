package nhk.ts.wcms.workflow.translation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;

import nhk.ts.wcms.common.CommonUtil;
import nhk.ts.wcms.common.Constants;
import nhk.ts.wcms.common.FileTypeChecker;
import nhk.ts.wcms.common.IOHelper;
import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.common.TSHelper;
import nhk.ts.wcms.dct.MasterFactory;
import nhk.ts.wcms.workflow.translation.bean.NHKTranslationObjectHolder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.access.CSGroup;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSBranch;
import com.interwoven.cssdk.filesys.CSConflictException;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSInvalidVPathException;
import com.interwoven.cssdk.filesys.CSReadOnlyFileSystemException;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import nhk.ts.wcms.dao.ConnectionManager;
import nhk.ts.wcms.dao.DataManager;
import org.dom4j.Document;
import org.dom4j.Element;

public class CopyTranslationDCRsTask implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.translation.CopyTranslationDCRsTask"));
    private static String baseLocale = IOHelper.getPropertyValue("TranslationWF.baseLocale");
    private String saleableProductKey = IOHelper.getString("CopyTranslationDCRsTask.ProductDCRKey");
    private String dealerKey = IOHelper.getString("CopyTranslationDCRsTask.DealerDCRKey");
    private String newsKey = IOHelper.getString("CopyTranslationDCRsTask.NewsDCRKey");
    private String productCategoryKey = IOHelper.getString("CopyTranslationDCRsTask.ProductCategoryDCRKey");
    private String promEventKey = IOHelper.getString("CopyTranslationDCRsTask.PromotionsEventsDCRKey");
    private String saNoticesKey = IOHelper.getString("CopyTranslationDCRsTask.ServiceAdvisoryNoticesDCRKey");
    private final String localePrefix = "<LocaleParameter>";
    private final String localeSuffix = "</LocaleParameter>";
    private final String linkParamPrefix = "<Link_Parameter>";
    private final String linkParamSuffix = "</Link_Parameter>";
    private final String dealerDCRLinkParamPrefix = "<LinkParameter>";
    private final String dealerDCRLinkParamSuffix = "</LinkParameter>";
    private final String relProdNamePrefix = "<RelatedProductCategoryName>";
    private final String relProdNameSuffix = "</RelatedProductCategoryName>";

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask currentTask,
            Hashtable params) throws CSException {

        CSWorkflow job = currentTask.getWorkflow();
        CSAreaRelativePath[] files = currentTask.getFiles();
        mLogger.createLogDebug("No of Files attached: " + files.length);
        Vector<CSFile> dcrFiles = new Vector<CSFile>();
        currentTask.getArea();
        try {
            CSClient masterClient = MasterFactory.getMasterClient();
            CSBranch parentBranch = masterClient.getBranch(new CSVPath(IOHelper.getPropertyValue("TranslationWF.parentBranch")), true);

            String translateTo = currentTask.getVariable("translateTo");
            String branches = currentTask.getVariable("branches");

            mLogger.createLogDebug("translateTo::" + translateTo);
            mLogger.createLogDebug("branches::" + branches);

            for (CSAreaRelativePath file : files) {
                mLogger.createLogDebug("File Name: " + file.getName());
                CSFile csFile = currentTask.getArea().getFile(file);
                if (FileTypeChecker.isDcr(csFile)
                        && FileTypeChecker.isCSSimpleFile(csFile, masterClient)) {
                    mLogger.createLogDebug("DCR Files: " + csFile.getName());
                    dcrFiles.add(csFile);
                }
            }

            if (CollectionUtils.isNotEmpty(dcrFiles)
                    || (parentBranch != null && parentBranch.isValid())) {
                processFilesForTranslation(parentBranch, masterClient,
                        dcrFiles, translateTo, branches);
            } else {
                mLogger.createLogDebug("No files to translate. Ending the job!!!");
                currentTask.addComment("No files to translate. Ending the job!!!");
                job.terminate();
            }
            mLogger.createLogDebug("Transitioning to next task:::"
                    + currentTask.getTransitions()[0]);
            currentTask.chooseTransition(currentTask.getTransitions()[0],
                    "Completed copying");
        } catch (CSException cse) {
            mLogger.createLogError(
                    "Error in CopyTranslationDCRs execute () method!!", cse);
        } catch (Exception e) {
            mLogger.createLogError(
                    "Error in CopyTranslationDCRs execute () method!!", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void processFilesForTranslation(CSBranch parentBranch,
            CSClient masterClient, Vector<CSFile> dcrFiles, String translateTo, String branches) throws Exception {
        CSBranch[] targetBranches = parentBranch.getSubBranches();
        // Remove the current branch from list
        if (!ArrayUtils.isEmpty(targetBranches)) {
            mLogger.createLogDebug("Found target branches!!!"
                    + targetBranches.length);
            NHKTranslationObjectHolder.clearDoc();

            boolean proceed = false;

            for (CSBranch csBranch : targetBranches) {

                if (translateTo.equalsIgnoreCase("some")) {
                    if (branches.contains(csBranch.getName())) {
                        proceed = true;
                    }
                } else {
                    proceed = true;
                }
                if (proceed) {

                    CSWorkarea targWA = masterClient.getWorkarea(
                            new CSVPath(
                            csBranch.getVPath().toString()
                            + IOHelper.getPropertyValue("TranslationWF.mainWA")),
                            true);
                    mLogger.createLogDebug("WORKAREA:::"
                            + targWA.getVPath().toString());
                    if (targWA != null && targWA.isValid()) {
                        mLogger.createLogDebug("csBranch name::"
                                + csBranch.getName() + "prop::" + "translation."
                                + csBranch.getName() + ".locale");
                        String locales = IOHelper.getPropertyValue("translation."
                                + csBranch.getName() + ".locale");
                        mLogger.createLogDebug("Locales:::" + locales);

                        if (StringUtils.isNotBlank(locales)
                                && StringUtils.isNotEmpty(locales)) {
                            mLogger.createLogDebug("Locales not null");
                            for (String locale : locales.split(",")) {
                                // Dont process the attached files again!!!
                                if (!StringUtils.equalsIgnoreCase(
                                        baseLocale.trim(), locale.trim())) {
                                    mLogger.createLogDebug("baselocale:::"
                                            + baseLocale + "---->locale:::"
                                            + locale);
                                    Element branchElement = NHKTranslationObjectHolder.translationRootEl.addElement("Branch");
                                    createBranchDom(branchElement, csBranch,
                                            masterClient);
                                    Vector<CSFile> translationFiles = searchFilesInWA(dcrFiles, targWA,
                                            csBranch, locale, branchElement);
                                    // Copy Files
                                    if (CollectionUtils.isNotEmpty(translationFiles)) {
                                        copyFilesToBranch(targWA, translationFiles,
                                                locale);
                                    }
                                }
                            }

                        } else {
                            mLogger.createLogDebug("No locale found in property file. Please contact Administrator and re-submit files");
                        }
                    } else {
                        mLogger.createLogDebug("WORKAREA: "
                                + targWA.getVPath().toString()
                                + " does not exist in branch: "
                                + csBranch.getName());
                    }
                }
            }
        } else {
            mLogger.createLogDebug("Country branches not found");
        }
    }

    private Vector<CSFile> searchFilesInWA(Vector<CSFile> dcrFiles,
            CSWorkarea targWA, CSBranch csBranch, String locale,
            Element branchElement) throws Exception {
        Element filesEl = branchElement.addElement("Files");
        Vector<CSFile> translationFiles = new Vector<CSFile>();
        for (CSFile file : dcrFiles) {
            String oldFilePath = file.getVPath().getAreaRelativePath().toString();
            String areRelPath = CommonUtil.replaceDCRPathWithLocale(
                    oldFilePath, baseLocale, locale);
            CSAreaRelativePath csAreaRelPath = new CSAreaRelativePath(
                    areRelPath);
            CSFile otherFile = targWA.getFile(csAreaRelPath);
            Element fileEl = filesEl.addElement("File").addAttribute("OtherLink", targWA.getVPath().toString() + "/" + areRelPath).addAttribute("Link", file.getVPath().toString());
            if (otherFile != null && FileTypeChecker.isValid(otherFile)
                    && FileTypeChecker.isDcr(otherFile)) {
                fileEl.addAttribute("existing", "true");
            } else {
                // Key is source file CSAreaRelativePath and value is targetWA
                // CSAreaRelativePath
                fileEl.addAttribute("existing", "false");
                translationFiles.add(file);
            }
            fileEl.setText(file.getName());
        }
        return translationFiles;
    }

    private void copyFilesToBranch(CSWorkarea targWA,
            Vector<CSFile> translationFiles, String locale) {

        mLogger.createLogDebug(translationFiles.size() + " files to be copied.");
        try {
            synchronized (translationFiles) {
                String targetPath = "";
                for (CSFile sourceFile : translationFiles) {
                    CSSimpleFile sourceSimpFile = (CSSimpleFile) sourceFile;
                    byte[] content = sourceSimpFile.read(0, -1);

                    targetPath = sourceSimpFile.getVPath().getAreaRelativePath().toString();
                    mLogger.createLogDebug("targetPath=" + targetPath);
                    TSHelper.createDirectoryInTargetWA(targWA, targetPath,
                            locale);

                    mLogger.createLogDebug("About to copy files.");
                    CSSimpleFile targFile = targWA.createSimpleFile(new CSAreaRelativePath(
                            StringUtils.replace(sourceSimpFile.getVPath().getAreaRelativePath().toString(), baseLocale, locale)));
                    targFile.write(content, 0, content.length, false);
                    //targFile.setExtendedAttributes(sourceSimpFile.getExtendedAttributes(null));
                    // Change DCR Type and Locale EA to new locale
                    ArrayList<CSExtendedAttribute> newEAs = new ArrayList<CSExtendedAttribute>();
                    CSExtendedAttribute dcrTypeEA = sourceSimpFile.getExtendedAttribute(IOHelper.getString("FileTypeChecker.dcrTypeEAKey"));
                    CSExtendedAttribute productCode = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productDevCodeEAKey"));
                    CSExtendedAttribute product_name = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productNameEAKey"));
                    CSExtendedAttribute product_related = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productRelatedEAKey"));

                    mLogger.createLogDebug("About to modify product dcr content.");
                    // If it is saleable product from en_Asia branch, then update the relative paths.
                    String dcrType = dcrTypeEA.getValue();
                    mLogger.createLogDebug("Target File Name=" + targFile.getName() + ":::DCRType=" + dcrType);
                    if (dcrType.equalsIgnoreCase(saleableProductKey)) {
                        updateProductDCRReferences(targFile, locale, dcrType);

                        CSExtendedAttribute cat1EA = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productCat1EAKey"));
                        CSExtendedAttribute cat2EA = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productCat2EAKey"));
                        CSExtendedAttribute cat3EA = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productCat3EAKey"));
                        CSExtendedAttribute cat4EA = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productCat4EAKey"));
                        CSExtendedAttribute cat5EA = sourceSimpFile.getExtendedAttribute(IOHelper.getString("CreateProductDCR.productCat5EAKey"));

                        updateProductCategoryEAs(cat1EA, newEAs, locale);
                        updateProductCategoryEAs(cat2EA, newEAs, locale);
                        updateProductCategoryEAs(cat3EA, newEAs, locale);
                        updateProductCategoryEAs(cat4EA, newEAs, locale);
                        updateProductCategoryEAs(cat5EA, newEAs, locale);
                    }

                    if (dcrType.equalsIgnoreCase(dealerKey) || dcrType.equalsIgnoreCase(newsKey) || dcrType.equalsIgnoreCase(productCategoryKey) || dcrType.equalsIgnoreCase(promEventKey) || dcrType.equalsIgnoreCase(saNoticesKey)) {
                        updateOtherDCRReferences(targFile, locale, dcrType);
                    }
                    dcrTypeEA.setValue(StringUtils.replace(
                            dcrTypeEA.getValue(), baseLocale, locale));
                    newEAs.add(dcrTypeEA);
                    newEAs.add(productCode);
                    newEAs.add(product_name);
                    newEAs.add(product_related);
                    targFile.setExtendedAttributes(newEAs.toArray(new CSExtendedAttribute[0]));
                }
            }

        } catch (CSAuthorizationException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        } catch (CSObjectNotFoundException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        } catch (CSRemoteException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        } catch (CSReadOnlyFileSystemException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        } catch (CSInvalidVPathException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        } catch (CSConflictException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        } catch (CSExpiredSessionException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        } catch (CSException e) {
            mLogger.createLogDebug("error while updating WA:"
                    + targWA.getVPath().toString(), e);
        }
    }

    private void updateProductCategoryEAs(CSExtendedAttribute cat1EA, ArrayList<CSExtendedAttribute> newEAs, String locale) {
        if (cat1EA != null && cat1EA.getValue() != null) {
            try {
                String origid = cat1EA.getValue();
                Connection connection = ConnectionManager.getStagingConnection();
                DataManager dm = new DataManager();
                String newid = dm.retrieveCategoryFromMapper(connection, origid, locale);
                cat1EA.setValue(newid);
                newEAs.add(cat1EA);
            } catch (SQLException ex) {
                mLogger.createLogError("Error in updateProductCategoryEAs.", ex);
            }
        }
    }

    private void createBranchDom(Element branchElement, CSBranch csBranch,
            CSClient masterClient) throws CSException {

        branchElement.addAttribute("Name", csBranch.getName());
        branchElement.addAttribute("Vpath", csBranch.getVPath().toString());
        // Webmaster TS for each country branch should follow
        // webmaster_branchname naming convention. e.g.:
        // webmaster_HK,webmaster_SG
        //    CSUser webmaster = null;
        CSUser webmaster = masterClient.getUser(IOHelper.getPropertyValue("TranslationWF.webmasterUserPrefix")
                + Constants.UNDERSCRORE + csBranch.getName(), true);
        CSGroup webmaster_group = masterClient.getGroup(IOHelper.getPropertyValue("TranslationWF.webmasterUserPrefix")
                + Constants.UNDERSCRORE + csBranch.getName(), true);
        if (!(webmaster_group == null)) {
            Element userEl = branchElement.addElement("WebMaster");
            userEl.addAttribute("Name", webmaster_group.getFullName());
            userEl.addAttribute("Email", webmaster_group.getName());
            webmaster = masterClient.getCurrentUser();
        } else if (!(webmaster == null)) {
            Element userEl = branchElement.addElement("WebMaster");
            userEl.addAttribute("Name", webmaster.getFullName());
            userEl.addAttribute("Email", webmaster.getName());
        } else {
            webmaster = masterClient.getCurrentUser();
            Element userEl = branchElement.addElement("WebMaster");
            userEl.addAttribute("Name", webmaster.getFullName());
            userEl.addAttribute("Email", webmaster.getName());
        }
        /*	if (webmaster == null) {
        webmaster = masterClient.getCurrentUser();
        }
        Element userEl = branchElement.addElement("WebMaster");
        userEl.addAttribute("Name", webmaster.getFullName());
        userEl.addAttribute("Email", webmaster.getEmailAddress()); */
    }

    /**
     * The following method will update the following paths within the file.
     *
     * <KeyFeatureDCRPath>
     * <Link_Parameter>
     * <RelatedProductCtr>
     *          <RelatedProductCategory>
     * <RelatedProductCtr>
     *  
     * @param dcrTypeEA
     * @param targFile
     * @param locale
     */
    private void updateProductDCRReferences(CSSimpleFile targFile, String locale, String dcrType) {
        mLogger.createLogDebug("updateProductDCRReferences called.");
        try {
            String filePath = targFile.getVPath().toString();
            File saleableProductFile = new File(filePath);
            Document doc = Dom4jUtils.newDocument(saleableProductFile);
            String inputDCR = doc.asXML();

            HashMap<String, String> mapOfChangedValues = new HashMap<String, String>();

            // Call for key features paths
            updateKeyFeaturesPathInMap(doc, locale, mapOfChangedValues);
            // Call for related product category id paths
            updateRelatedProductCategoryInMap(doc, locale, mapOfChangedValues);
            // Call for categoryId
            updateProductCategoryInMap(doc, locale, mapOfChangedValues, dcrType);
            // Call for LocaleParameter change
            mapOfChangedValues.put(localePrefix + baseLocale + localeSuffix, localePrefix + locale + localeSuffix);
            // Call for Link_Parameter change
            String originalLinkParam = linkParamPrefix + doc.selectSingleNode("//Link_Parameter").getText() + linkParamSuffix;
            String changedLinkParam = StringUtils.replace(originalLinkParam, baseLocale, locale);

            mapOfChangedValues.put(originalLinkParam, changedLinkParam);

            // Replace all paths and write out the DCR
            Iterator keys = mapOfChangedValues.keySet().iterator();
            String orig;
            String repl;
            while (keys.hasNext()) {
                orig = (String) keys.next();
                mLogger.createLogDebug("key: " + orig);
                repl = (String) mapOfChangedValues.get(orig);
                mLogger.createLogDebug("value: " + repl);
                inputDCR = StringUtils.replace(inputDCR, orig, repl);
            }

            FileOutputStream fos = new FileOutputStream(saleableProductFile);
            Writer out = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));

            out.write(inputDCR);
            mLogger.createLogDebug("inputDcr after translation::" + inputDCR);
            out.flush();
            out.close();
        } catch (Exception e) {
            mLogger.createLogDebug("Error in updateProductDCRReferences", e);
        }

    }

    /**
     * The following method will update the following paths within the file.
     *
     * <KeyFeatureDCRPath>
     * <Link_Parameter>
     * <RelatedProductCtr>
     *          <RelatedProductCategory>
     * <RelatedProductCtr>
     *
     * @param dcrTypeEA
     * @param targFile
     * @param locale
     */
    private void updateOtherDCRReferences(CSSimpleFile targFile, String locale, String dcrType) {
        mLogger.createLogDebug("updateProductDCRReferences called.");
        try {
            String filePath = targFile.getVPath().toString();
            File otherDCRFile = new File(filePath);
            Document doc = Dom4jUtils.newDocument(otherDCRFile);
            String inputDCR = doc.asXML();

            HashMap<String, String> mapOfChangedValues = new HashMap<String, String>();

            // Call for LocaleParameter change
            mapOfChangedValues.put(localePrefix + baseLocale + localeSuffix, localePrefix + locale + localeSuffix);
            // Call for Link_Parameter change
            String originalLinkParam = "";
            String changedLinkParam = "";
			
            if (dcrType.equalsIgnoreCase(dealerKey)) {
                originalLinkParam = dealerDCRLinkParamPrefix + doc.selectSingleNode("//LinkParameter").getText() + dealerDCRLinkParamSuffix;
                changedLinkParam = StringUtils.replace(originalLinkParam, baseLocale, locale);
            } else {
                originalLinkParam = linkParamPrefix + doc.selectSingleNode("//Link_Parameter").getText() + linkParamSuffix;
                changedLinkParam = StringUtils.replace(originalLinkParam, baseLocale, locale);
            }
			
            mapOfChangedValues.put(originalLinkParam, changedLinkParam);
            // Call to update L1CategoryName, L2CategoryName ...
            updateProductCategoryInMap(doc, locale, mapOfChangedValues, dcrType);
            // Replace all paths and write out the DCR
            Iterator keys = mapOfChangedValues.keySet().iterator();
            String orig;
            String repl;
            while (keys.hasNext()) {
                orig = (String) keys.next();
                mLogger.createLogDebug("key: " + orig);
                repl = (String) mapOfChangedValues.get(orig);
                mLogger.createLogDebug("value: " + repl);
                inputDCR = StringUtils.replace(inputDCR, orig, repl);
            }

            FileOutputStream fos = new FileOutputStream(otherDCRFile);
            Writer out = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));

            out.write(inputDCR);
            mLogger.createLogDebug("inputDcr after translation::" + inputDCR);
            out.flush();
            out.close();
        } catch (Exception e) {
            mLogger.createLogDebug("Error in updateProductDCRReferences", e);
        }

    }

    /**
     * Update the key features path
     * @param doc
     * @param locale
     * @param mapOfChangedValues
     */
    private void updateKeyFeaturesPathInMap(Document doc, String locale, HashMap<String, String> mapOfChangedValues) {
        mLogger.createLogDebug("updateKeyFeaturesPathInMap called.");
        List<Element> keyFeatureElements = doc.selectNodes("//KeyFeatureDCRPath");
        if (CollectionUtils.isNotEmpty(keyFeatureElements)) {
            String keyFeaturesRef;
            String modofiedKeyFeaturesRef;
            for (Element element : keyFeatureElements) {
                keyFeaturesRef = "<KeyFeatureDCRPath>" + element.getText() + "</KeyFeatureDCRPath>";
                modofiedKeyFeaturesRef = StringUtils.replace(keyFeaturesRef, baseLocale, locale);
                mapOfChangedValues.put(keyFeaturesRef, modofiedKeyFeaturesRef);
            }
        }
    }

    /**
     * Update the related product category ids
     * @param doc
     * @param locale
     * @param mapOfChangedValues
     */
    private void updateRelatedProductCategoryInMap(Document doc, String locale, HashMap<String, String> mapOfChangedValues) {
        mLogger.createLogDebug("updateRelatedProductCategoryInMap called.");
        try {
            List<Element> relatedProductCtrElements = doc.selectNodes("//RelatedProductCtr");
            if (CollectionUtils.isNotEmpty(relatedProductCtrElements)) {
                Connection connection = ConnectionManager.getStagingConnection();
                DataManager dm = new DataManager();
                mapOfChangedValues.putAll(dm.updateRelatedProductsFromMapper(connection, locale, relatedProductCtrElements));
            }
        } catch (Exception e) {
            mLogger.createLogError("Error is reading Product_Category_Mapper table.", e);
        }
    }

    private void updateProductCategoryInMap(Document doc, String locale, HashMap<String, String> mapOfChangedValues, String dcrType) {
        mLogger.createLogDebug("updateProductCategoryInMap called.");

        if (dcrType.equalsIgnoreCase(saleableProductKey) || dcrType.equalsIgnoreCase(productCategoryKey)) {
            try {
                String catElement = "";
                if (dcrType.equalsIgnoreCase(productCategoryKey)) {
                    catElement = (doc.selectSingleNode("//CategoryID") != null ? doc.selectSingleNode("//CategoryID").getText() : "");
                } else if (dcrType.equalsIgnoreCase(saleableProductKey)) {
                    catElement = (doc.selectSingleNode("//Category") != null ? doc.selectSingleNode("//Category").getText() : "");
                }
                String l1Element = (doc.selectSingleNode("//L1CategoryName") != null ? doc.selectSingleNode("//L1CategoryName").getText() : "");
                String l2Element = (doc.selectSingleNode("//L2CategoryName") != null ? doc.selectSingleNode("//L2CategoryName").getText() : "");
                String l3Element = (doc.selectSingleNode("//L3CategoryName") != null ? doc.selectSingleNode("//L3CategoryName").getText() : "");
                String l4Element = (doc.selectSingleNode("//L4CategoryName") != null ? doc.selectSingleNode("//L4CategoryName").getText() : "");
                String l5Element = (doc.selectSingleNode("//L5CategoryName") != null ? doc.selectSingleNode("//L5CategoryName").getText() : "");

                Connection connection = ConnectionManager.getStagingConnection();
                DataManager dm = new DataManager();

                if (dcrType.equalsIgnoreCase(productCategoryKey)) {
                    mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, catElement, "CategoryID"));
                } else if (dcrType.equalsIgnoreCase(saleableProductKey)) {
                    mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, catElement, "Category"));
                }
                mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, l1Element, "L1CategoryName"));
                mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, l2Element, "L2CategoryName"));
                mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, l3Element, "L3CategoryName"));
                mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, l4Element, "L4CategoryName"));
                mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, l5Element, "L5CategoryName"));

                if (dcrType.equalsIgnoreCase(productCategoryKey)) {
                    String parentCatElement = (doc.selectSingleNode("//ParentCategoryID") != null ? doc.selectSingleNode("//ParentCategoryID").getText() : "");
                    String grandParentElement = (doc.selectSingleNode("//GrantParentCategoryID") != null ? doc.selectSingleNode("//GrantParentCategoryID").getText() : "");
                    mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, parentCatElement, "ParentCategoryID"));
                    mapOfChangedValues.putAll(dm.updateProductCategoriesInDCR(connection, locale, grandParentElement, "GrantParentCategoryID"));
                }

            } catch (Exception e) {
                mLogger.createLogError("Error is reading Product_Category_Mapper table.", e);
            }
        }
    }
}
