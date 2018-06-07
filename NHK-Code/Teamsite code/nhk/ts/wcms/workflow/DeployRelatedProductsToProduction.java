/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Hashtable;
import nhk.ts.wcms.common.FileTypeChecker;
import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.dao.ConnectionManager;
import nhk.ts.wcms.dao.DataManager;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

/**
 *
 * @author smukherj
 */
public class DeployRelatedProductsToProduction implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.DeployRelatedProductsToProduction"));

    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        // To decide if it is saleable_product_information DCR-type, if so then needs RelatedProducts deployment.
        Connection stgConnection = null;
        Connection prdConnection = null;
        CSAreaRelativePath[] files = task.getFiles();

        Document doc = null;

        for (CSAreaRelativePath areaRelativePath : files) {
            CSFile tmp = task.getArea().getFile(areaRelativePath);
            CSSimpleFile sourceSimpleFile = null;
            if (tmp instanceof CSSimpleFile) {
                sourceSimpleFile = (CSSimpleFile) tmp;
                boolean isProductDCR = FileTypeChecker.isProductDcr(sourceSimpleFile);
                if (isProductDCR) {
                    InputStream inStream = sourceSimpleFile.getInputStream(true);
                    doc = Dom4jUtils.newDocument(inStream);

                    String categoryId = doc.selectSingleNode("//saleable_product_information/OverviewCtr/Category").getText();
                    String productId = doc.selectSingleNode("//saleable_product_information/OverviewCtr/ProductID").getText();

                    // Find the group category id

                    stgConnection = ConnectionManager.getStagingConnection();
                    prdConnection = ConnectionManager.getProductionConnection();
                    
                    DataManager dm = new DataManager();
                    dm.updateRelatedProductsInProduction(stgConnection, prdConnection, categoryId, productId);
                }
            }
        }
        task.chooseTransition("Deploy Related Products Success", "Deploy Related Products Success");
    }
}
