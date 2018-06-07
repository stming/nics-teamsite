/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow;

import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.common.TSHelper;
import org.apache.commons.logging.LogFactory;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import java.util.Hashtable;

/**
 *
 * @author sbhojnag
 */
public class ObfuscationDelete implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.ObfuscationDelete"));
    //   private static final String PRODUCT_DevCode_EA = "TeamSite/Metadata/ProductMetadata/0/ProductDevCode";

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        try {
            mLogger.createLogDebug("Workarea:" + task.getArea().getVPath());
            CSAreaRelativePath[] areaRelativePaths = null;
            areaRelativePaths = task.getFiles();
            for (CSAreaRelativePath areaRelativePath : areaRelativePaths) {
                mLogger.createLogDebug("Trying to delete file:" + areaRelativePath);
                CSFile tmp = task.getArea().getFile(areaRelativePath);
                mLogger.createLogDebug("Area Name: " + task.getArea().getName());

                if (TSHelper.isValidFile(tmp)) {

                    mLogger.createLogDebug("File Name: " + tmp.getName());
                    mLogger.createLogDebug("File Kind " + tmp.getKind());

                    if (tmp instanceof CSSimpleFile) {
                        tmp.delete();
                    }
                }
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error Deleting File from Obfuscation WA", e);
            task.chooseTransition("Delete file from Obfuscation WA Failure", "Delete file from Obfuscation WA Failure");
        }
        mLogger.createLogDebug("Transition to:Delete file from Obfuscation WA Success");
        task.chooseTransition("Delete file from Obfuscation WA Success", "Delete file from Obfuscation WA Success");
    }
}
