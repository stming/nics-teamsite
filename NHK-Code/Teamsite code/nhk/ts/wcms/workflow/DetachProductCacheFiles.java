/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow;

import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author sbhojnag
 */
public class DetachProductCacheFiles implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.DetachProductCacheFiles"));
    //   private static final String PRODUCT_DevCode_EA = "TeamSite/Metadata/ProductMetadata/0/ProductDevCode";
    //   private static final String PRODUCT_DCR_TYPE = "saleable_product_information";
    //   private static final String PRODUCT_CATEGORY_DCR_TYPE = "product_category";

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        mLogger.createLogDebug("Start execution DetachProductCacheFiles");
        try {
            CSAreaRelativePath[] areaRelativePaths = null;
            areaRelativePaths = task.getFiles();
            CSAreaRelativePath[] depFilesRelPaths = null;
            Vector<CSAreaRelativePath> dependentFiles = new Vector<CSAreaRelativePath>();
            for (CSAreaRelativePath areaRelativePath : areaRelativePaths) {
                //    CSFile file = task.getArea().getFile(areaRelativePath);
                mLogger.createLogDebug("Area Relative path: " + areaRelativePath.toString());
                if (areaRelativePath.toString().contains("productlisting")) {
                    dependentFiles.add(areaRelativePath);
                }
                depFilesRelPaths = dependentFiles.toArray(new CSAreaRelativePath[dependentFiles.size()]);
                task.detachFiles(depFilesRelPaths);
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error Deleting File from Product Cache" + e);
            //     task.chooseTransition("Delete file from Obfuscation WA Failure", "Delete file from Obfuscation WA Failure");
        }
        task.chooseTransition("Detach Product Cache File Success", "Detach Product Cache File Success");
    }
}
