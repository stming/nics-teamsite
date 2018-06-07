package nhk.ts.wcms.workflow.translation;

import java.util.Hashtable;
import java.util.logging.Level;

//import nhk.ts.wcms.common.Constants;
//import nhk.ts.wcms.common.TSHelper;
//import nhk.ts.wcms.workflow.translation.URLTranslationTasksHelper;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
//import com.interwoven.cssdk.filesys.CSExtendedAttribute;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
//import java.util.ArrayList;
//import nhk.ts.wcms.common.IOHelper;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

public class AttachTranslationDependencyTask implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.translation.AttachTranslationDependencyTask"));
    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params)
            throws CSException {
        try {
            mLogger.createLogDebug("AttachTranslationDependency invoked.");
            CSWorkflow workflow = task.getWorkflow();
     /*       String TSFileList = TSHelper.getIWHome()
                    + "\\nikon_custom\\TSFileList" + Constants.UNDERSCRORE
                    + workflow.getId() + Constants.UNDERSCRORE + ".txt";
            String DCRFileList = TSHelper.getIWHome()
                    + "\\nikon_custom\\DCRFileList" + Constants.UNDERSCRORE
                    + workflow.getId() + Constants.UNDERSCRORE + ".txt"; */
            CSAreaRelativePath[] areaRelativePaths = null;
            areaRelativePaths = task.getFiles();
            CSAreaRelativePath[] depFilesRelPaths = null;
            for (CSAreaRelativePath areaRelativePath : areaRelativePaths) {
                CSFile tmp = task.getArea().getFile(areaRelativePath);
                CSSimpleFile file = null;
                if (tmp instanceof CSSimpleFile) {
                    file = (CSSimpleFile) tmp;
                    depFilesRelPaths = URLTranslationTasksHelper.addDependentFiles(file,
                            task, client);

                    // Tag the dependent files, if not already tagged. Then attach.

              //      tagDependentFiles(file, depFilesRelPaths, client,task.getArea());

                    task.attachFiles(depFilesRelPaths);
             /*       mLogger.createLogDebug("Creating File List text file :: ");
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
                    } */
                }
            }
            // choose transition from WF variable
            task.chooseTransition("Copy Translation Files",
                    "successfully attached the files");
        } catch (CSException exception) {
            mLogger.createLogDebug("Error in Attach Dependency task: ", exception);
        }

    }

  /*  private void tagDependentFiles(CSSimpleFile targetSimpleFile, CSAreaRelativePath[] depFilesRelPaths, CSClient client,CSArea csarea) {
        mLogger.createLogDebug("Called tagDependentFiles for " + depFilesRelPaths.length + " files.");
        for (int i = 0; i < depFilesRelPaths.length; i++) {
            try {
                CSAreaRelativePath cSAreaRelativePath = depFilesRelPaths[i];
                CSFile cSFile = csarea.getFile(cSAreaRelativePath);
                mLogger.createLogDebug("Called tagDependentFiles for file" + cSFile.getName());

             //   if (cSFile instanceof CSSimpleFile) {

                    CSSimpleFile sourceSimpleFile = (CSSimpleFile) cSFile;
                    mLogger.createLogDebug("CSSimple Kind: "+ CSSimpleFile.KIND);
                    // Copy over all the extended attributes into the attached files, if empty.

                    populateTagByNameIfBlank(sourceSimpleFile, targetSimpleFile, PRODUCT_RELATED_EA);
                    populateTagByNameIfBlank(sourceSimpleFile, targetSimpleFile, PRODUCT_NAME_EA);
                    populateTagByNameIfBlank(sourceSimpleFile, targetSimpleFile, PRODUCT_DEV_CODE_EA);
                    populateTagByNameIfBlank(sourceSimpleFile, targetSimpleFile, CATEGORY_LEVEL1_EA);
                    populateTagByNameIfBlank(sourceSimpleFile, targetSimpleFile, CATEGORY_LEVEL2_EA);
                    populateTagByNameIfBlank(sourceSimpleFile, targetSimpleFile, CATEGORY_LEVEL3_EA);

                    sourceSimpleFile.setExtendedAttributes(extendedAttrs.toArray(new CSExtendedAttribute[extendedAttrs.size()]));
                    mLogger.createLogDebug(sourceSimpleFile.getName() + " file tagged with " + extendedAttrs.size() + " new tags.");
           //     }


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

    private void populateTagByNameIfBlank(CSSimpleFile sourceSimpleFile, CSSimpleFile targetSimpleFile, String key) {
        try {
        //    mLogger.createLogDebug("Key Value: "+ targetSimpleFile.getExtendedAttribute(key).);
         //   mLogger.createLogDebug("Key: "+key);
        //    mLogger.createLogDebug("Extended Attribute: " + sourceSimpleFile.getExtendedAttribute(key));

           mLogger.createLogDebug("String EA for: " + sourceSimpleFile.getName() + " : " + NodeHelper.getStringEA(sourceSimpleFile, key));
            if (sourceSimpleFile.getExtendedAttribute(key).getValue() == null) {
                mLogger.createLogDebug("populateTagByNameIfBlank:New tag=" + key);
                extendedAttrs.add(new CSExtendedAttribute(key, targetSimpleFile.getExtendedAttribute(key).getValue()));
            }
        } catch (CSAuthorizationException ex) {
            java.util.logging.Logger.getLogger(AttachTranslationDependencyTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CSRemoteException ex) {
            java.util.logging.Logger.getLogger(AttachTranslationDependencyTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CSObjectNotFoundException ex) {
            java.util.logging.Logger.getLogger(AttachTranslationDependencyTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CSExpiredSessionException ex) {
            java.util.logging.Logger.getLogger(AttachTranslationDependencyTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CSException ex) {
            java.util.logging.Logger.getLogger(AttachTranslationDependencyTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    } */
}
