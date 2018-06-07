/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow;

import nhk.ts.wcms.common.Logger;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.filesys.CSHole;
import java.util.List;
import org.apache.commons.logging.LogFactory;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.File;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.Element;
import nhk.ts.wcms.common.IOHelper;
import nhk.ts.wcms.common.TSHelper;
import nhk.ts.wcms.common.FileTypeChecker;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author sbhojnag
 */
public class ProductCacheDelete implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.ProductCacheDelete"));
    private static final String PRODUCT_DCR_TYPE = "saleable_product_information";
    private static final String PRODUCT_CATEGORY_DCR_TYPE = "product_category";

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        mLogger.createLogDebug("Start execution ProductCacheDelete");
        try {
            CSAreaRelativePath[] areaRelativePaths = null;
            CSAreaRelativePath[] tempResourceFilesRelPaths = null;
            Vector<CSAreaRelativePath> tempResourceFiles = new Vector<CSAreaRelativePath>();
            areaRelativePaths = task.getFiles();
            for (CSAreaRelativePath areaRelativePath : areaRelativePaths) {
                CSFile file = task.getArea().getFile(areaRelativePath);
                //mLogger.createLogDebug("Checking for file name:" + file.getName());
                //mLogger.createLogDebug("Is Instance of hole?:" + (file instanceof CSHole));
                if (!(file instanceof CSHole)) {
                    CSSimpleFile cssfile = (CSSimpleFile) file;
                    if (FileTypeChecker.isDcr(cssfile)) {
                        String dcrType = cssfile.getExtendedAttribute(IOHelper.getString("FileTypeChecker.dcrTypeEAKey")).getValue();
                        if (dcrType.contains(PRODUCT_DCR_TYPE) || dcrType.contains(PRODUCT_CATEGORY_DCR_TYPE)) {

                            mLogger.createLogDebug("Match occured for file name:" + file.getName());

                            String propertyFullPath = null;
                            String filevpath = file.getVPath().toString();
                            mLogger.createLogDebug("File Path: " + filevpath);
                            filevpath = filevpath.substring(filevpath.indexOf("templatedata/"), filevpath.length());
                            String[] temp = filevpath.split("/");
                            mLogger.createLogDebug("Locale: " + temp[1]);
                            String locale = temp[1];
                            propertyFullPath = "resources/properties/" + locale + "/SiteInfo.xml";
                            propertyFullPath = task.getArea().getVPath().toString() + "/" + propertyFullPath;
                            propertyFullPath = propertyFullPath.replaceAll("/obfuscation_wa", "/main_wa");
                            File propertyFile = new File(propertyFullPath);
                            Document propertyFileDocument = Dom4jUtils.newDocument(propertyFile);
                            Node node = propertyFileDocument.selectSingleNode("//entry[@key='productNodeLabel']");
                            String productNodeLabel = node.getText();
                            mLogger.createLogDebug("Product Node: " + productNodeLabel);
                            mLogger.createLogDebug("Locale: " + locale);
                            String siteFullPath = task.getArea().getVPath().toString() + "/sites/" + locale + "/default.sitemap";
                            siteFullPath = siteFullPath.replaceAll("/obfuscation_wa", "/main_wa");
                            mLogger.createLogDebug("SiteMap File Path: " + siteFullPath);
                            File sitemapFile = new File(siteFullPath);
                            Document sitemapFileDocument = Dom4jUtils.newDocument(sitemapFile);
                            //   Node productNode = sitemapFileDocument.selectSingleNode("//node[label[text() = '" + productNodeLabel + "']]");
                            List<Node> childProductNodes = sitemapFileDocument.selectNodes("//node[label[text() = '" + productNodeLabel + "']]/node");
                            CSAreaRelativePath[] depFilesRelPaths = null;
                            Vector<CSAreaRelativePath> dependentFiles = new Vector<CSAreaRelativePath>();
                            if (childProductNodes.size() != 0) {
                                for (Node unitNode : childProductNodes) {
                                    Node label = unitNode.selectSingleNode("label");
                                    Element unitElement = (Element) unitNode;
                                    mLogger.createLogDebug("Node Label: " + label.getText());
                                    mLogger.createLogDebug("Node ID: " + unitElement.attributeValue("id"));
                                    String categoryFilePath = task.getArea().getVPath().toString() + "/productlisting/" + locale + "/" + unitElement.attributeValue("id") + ".xml";
                                    mLogger.createLogDebug("Path: " + categoryFilePath);
                                    File catFile = new File(categoryFilePath);
                                    File dirFile = new File(categoryFilePath.substring(0, categoryFilePath.lastIndexOf("/")));
                                    mLogger.createLogDebug("File name: " + catFile.getName());
                                    mLogger.createLogDebug("Dir Path: " + dirFile.getPath());
                                    dirFile.mkdirs();

                                    catFile.createNewFile();
                                    CSAreaRelativePath vpath = new CSAreaRelativePath("productlisting/" + locale + "/" + unitElement.attributeValue("id") + ".xml");
                                    CSSimpleFile cssproductfile = (CSSimpleFile) task.getArea().getFile(vpath);
                                    if (cssproductfile.isValid()) {
                                        dependentFiles.add(vpath);
                                    }
                                }
                                depFilesRelPaths = dependentFiles.toArray(new CSAreaRelativePath[dependentFiles.size()]);
                                CSWorkarea targetWA = (CSWorkarea) task.getArea();
                                TSHelper.batchSubmitFiles(targetWA, depFilesRelPaths);
                                mLogger.createLogDebug("Files Submitted Successfully");
                                targetWA.deleteFiles(depFilesRelPaths);
                                task.attachFiles(depFilesRelPaths);

                            }
                        }
                    }
                    mLogger.createLogDebug("Checking for Metadata: " + cssfile.getExtendedAttribute("TeamSite/Resource/Common/Temp") + " of " + cssfile.getVPath());
                    if (cssfile.getExtendedAttribute("TeamSite/Resource/Common/Temp") != null && cssfile.getExtendedAttribute("TeamSite/Resource/Common/Temp").getValue() != null) {
                        if (cssfile.getExtendedAttribute("TeamSite/Resource/Common/Temp").getValue().equalsIgnoreCase("Yes")) {
                            String resourcefilePath = task.getArea().getVPath().toString() + "/" + areaRelativePath.toString();
                            mLogger.createLogDebug("Deleting the dummy resorce file" + resourcefilePath);
                            File resourceFile = new File(resourcefilePath);
                            mLogger.createLogDebug("File name: " + resourceFile.getName());
                            resourceFile.delete();

                            mLogger.createLogDebug("Added " + areaRelativePath + " to the temporary resources file list for deletion.");

                            tempResourceFiles.add(areaRelativePath);
                        }
                    }
                }
            }
            if (tempResourceFiles != null) {
                tempResourceFilesRelPaths = tempResourceFiles.toArray(new CSAreaRelativePath[tempResourceFiles.size()]);
                task.detachFiles(tempResourceFilesRelPaths);
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error Deleting File from Product Cache" + e, e);
            //     task.chooseTransition("Delete file from Obfuscation WA Failure", "Delete file from Obfuscation WA Failure");
        }
        task.chooseTransition("Clear Product Cache Successful", "Clear Product Cache Successful");
    }
}
