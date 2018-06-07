package nhk.ts.wcms.workflow;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.logging.Level;

import nhk.ts.wcms.common.Constants;
import nhk.ts.wcms.common.TSHelper;
import nhk.ts.wcms.common.URLTasksHelper;
import com.interwoven.livesite.util.NodeHelper;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSArea;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import java.util.ArrayList;
import nhk.ts.wcms.common.IOHelper;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

public class AttachDependencyTask implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.AttachDependencyTask"));
    private static final String PRODUCT_RELATED_EA = IOHelper.getString("CreateProductDCR.productRelatedEAKey");
    private static final String PRODUCT_NAME_EA = IOHelper.getString("CreateProductDCR.productNameEAKey");
    private static final String PRODUCT_DEV_CODE_EA = IOHelper.getString("CreateProductDCR.productDevCodeEAKey");
    private static final String CATEGORY_LEVEL1_EA = IOHelper.getString("CreateProductDCR.productCat1EAKey");
    private static final String CATEGORY_LEVEL2_EA = IOHelper.getString("CreateProductDCR.productCat2EAKey");
    private static final String CATEGORY_LEVEL3_EA = IOHelper.getString("CreateProductDCR.productCat3EAKey");
    private static final String CATEGORY_LEVEL4_EA = IOHelper.getString("CreateProductDCR.productCat4EAKey");
    private static final String CATEGORY_LEVEL5_EA = IOHelper.getString("CreateProductDCR.productCat5EAKey");

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params)
            throws CSException {
        try {
            CSWorkflow workflow = task.getWorkflow();
            String TSFileList = TSHelper.getIWHome()
                    + "\\nikon_custom\\TSFileList" + Constants.UNDERSCRORE
                    + workflow.getId() + Constants.UNDERSCRORE + ".txt";
            String DCRFileList = TSHelper.getIWHome()
                    + "\\nikon_custom\\DCRFileList" + Constants.UNDERSCRORE
                    + workflow.getId() + Constants.UNDERSCRORE + ".txt";
            CSAreaRelativePath[] areaRelativePaths = null;
            areaRelativePaths = task.getFiles();
            CSAreaRelativePath[] depFilesRelPaths = null;
            for (CSAreaRelativePath areaRelativePath : areaRelativePaths) {
                CSFile tmp = task.getArea().getFile(areaRelativePath);
                CSSimpleFile sourceSimpleFile = null;
                if (tmp instanceof CSSimpleFile) {
                    sourceSimpleFile = (CSSimpleFile) tmp;
                    depFilesRelPaths = URLTasksHelper.addDependentFiles(sourceSimpleFile,
                            task, client);

                    // Tag the dependent files, if not already tagged. Then attach.

                    tagDependentFiles(sourceSimpleFile, depFilesRelPaths, task.getArea());

                    task.attachFiles(depFilesRelPaths);
                    mLogger.createLogDebug("Creating File List text file :: ");
                    try {
                        FileOutputStream out = new FileOutputStream(TSFileList);
                        PrintStream p = new PrintStream(out);
                        FileOutputStream dout = new FileOutputStream(
                                DCRFileList);
                        PrintStream dp = new PrintStream(dout);
                        CSAreaRelativePath[] myfiles = task.getFiles();
                        for (int j = 0; j < myfiles.length; j++) {
                            if (myfiles[j].toString().contains("templatedata")) {
                                dp.println(myfiles[j].toString());
                            }
                            p.println(myfiles[j].toString());
                        }
                        p.close();
                    } catch (Exception e) {
                        mLogger.createLogDebug("Error in creating file list:", e);
                    }
                }
            }
            // choose transition from WF variable
            task.chooseTransition(task.getTransitions()[0],
                    "successfully attached the files");
        } catch (CSException exception) {
            mLogger.createLogDebug("Error in Attach Dependency task: ", exception);
        }

    }

    private void tagDependentFiles(CSSimpleFile sourceSimpleFile, CSAreaRelativePath[] depFilesRelPaths, CSArea csarea) {
        mLogger.createLogDebug("Called tagDependentFiles for " + depFilesRelPaths.length + " files.");

        try {
            ArrayList<CSExtendedAttribute> extendedAttrs = new ArrayList<CSExtendedAttribute>();

            extendedAttrs.add(new CSExtendedAttribute(PRODUCT_RELATED_EA, sourceSimpleFile.getExtendedAttribute(PRODUCT_RELATED_EA).getValue()));
            extendedAttrs.add(new CSExtendedAttribute(PRODUCT_NAME_EA, sourceSimpleFile.getExtendedAttribute(PRODUCT_NAME_EA).getValue()));
            extendedAttrs.add(new CSExtendedAttribute(PRODUCT_DEV_CODE_EA, sourceSimpleFile.getExtendedAttribute(PRODUCT_DEV_CODE_EA).getValue()));
            extendedAttrs.add(new CSExtendedAttribute(CATEGORY_LEVEL1_EA, sourceSimpleFile.getExtendedAttribute(CATEGORY_LEVEL1_EA).getValue()));
            extendedAttrs.add(new CSExtendedAttribute(CATEGORY_LEVEL2_EA, sourceSimpleFile.getExtendedAttribute(CATEGORY_LEVEL2_EA).getValue()));
            extendedAttrs.add(new CSExtendedAttribute(CATEGORY_LEVEL3_EA, sourceSimpleFile.getExtendedAttribute(CATEGORY_LEVEL3_EA).getValue()));
            extendedAttrs.add(new CSExtendedAttribute(CATEGORY_LEVEL4_EA, sourceSimpleFile.getExtendedAttribute(CATEGORY_LEVEL4_EA).getValue()));
            extendedAttrs.add(new CSExtendedAttribute(CATEGORY_LEVEL5_EA, sourceSimpleFile.getExtendedAttribute(CATEGORY_LEVEL5_EA).getValue()));

            for (int i = 0; i < depFilesRelPaths.length; i++) {
                CSAreaRelativePath cSAreaRelativePath = depFilesRelPaths[i];
                CSFile cSFile = csarea.getFile(cSAreaRelativePath);
                mLogger.createLogDebug("Called tagDependentFiles for file" + cSFile.getName());

                if (cSFile instanceof CSSimpleFile) {
                    CSSimpleFile targetSimpleFile = (CSSimpleFile) cSFile;
                    mLogger.createLogDebug("CSSimple Kind: " + CSSimpleFile.KIND);
                    // Copy over all the extended attributes into the attached files.
                    // We have to overwrite the tags to counter a scenario like HK branch, where there will be the same image files once tagged category values with en_HK site and then for tc_HK site.
                    targetSimpleFile.setExtendedAttributes(extendedAttrs.toArray(new CSExtendedAttribute[extendedAttrs.size()]));
                    mLogger.createLogDebug(targetSimpleFile.getName() + " file tagged with " + extendedAttrs.size() + " new tags.");
                }
            }
        } catch (CSAuthorizationException ex) {
            mLogger.createLogDebug("Error in tagging dependent files:", ex);
        } catch (CSExpiredSessionException ex) {
            mLogger.createLogDebug("Error in tagging dependent files:", ex);
        } catch (CSRemoteException ex) {
            mLogger.createLogDebug("Error in tagging dependent files:", ex);
        } catch (CSException ex) {
            mLogger.createLogDebug("Error in tagging dependent files:", ex);
        }
    }
}
