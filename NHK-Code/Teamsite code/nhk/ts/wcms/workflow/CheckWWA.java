package nhk.ts.wcms.workflow;

import java.util.Hashtable;
import nhk.ts.wcms.common.Logger;
import java.net.*;
import java.io.*;
import org.apache.commons.logging.LogFactory;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import nhk.ts.wcms.common.IOHelper;

public class CheckWWA implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.CheckWWA"));
    private final String WWA_CHECK_ENABLED = "CheckWWA.CheckIsEnabled";
    private final String WWA_CHECK_URL = "CheckWWA.URL";
    //   private static final String PRODUCT_DevCode_EA = "TeamSite/Metadata/ProductMetadata/0/ProductDevCode";

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        try {
            mLogger.createLogDebug("Just Entered into checkWWA Class file :: ");
            String wwa = task.getWorkflow().getVariable("wwa");
            String product_name = task.getWorkflow().getVariable("product_name");
            String isproduct_related = task.getWorkflow().getVariable("isproduct_related");
            String deployType = task.getWorkflow().getVariable("deployType");
            mLogger.createLogDebug("WWA Date: " + wwa);
            mLogger.createLogDebug("Product Name: " + product_name);
            mLogger.createLogDebug("Is Product Related: " + isproduct_related);
            mLogger.createLogDebug("Deploy Type: " + deployType);
            if (deployType.equalsIgnoreCase("yes")) {
                // Dont check WWA for both product and non Product contents
                mLogger.createLogDebug("Immediate Deployment");
                task.chooseTransition("WWA Check Successful", "Successfully checked WWA");

            } else {
                if (isproduct_related.equalsIgnoreCase("true")) {

                    if (IOHelper.getPropertyValue(WWA_CHECK_ENABLED).equalsIgnoreCase("Yes")) {
                        //Web Service Call here with the above ProductName along with URL and store it in the below WSWWADatePassed
                        try {
                            String product_devcode = task.getWorkflow().getVariable("product_devcode");
                            mLogger.createLogDebug("Dev Code to check for WWA: " + product_devcode);
                            String wwaCheckURL = IOHelper.getPropertyValue(WWA_CHECK_URL);
                            mLogger.createLogDebug("URL to check for WWA: " + wwaCheckURL);
                            String url = wwaCheckURL + product_devcode;
                            URL WWAurl = new URL(url);
                            URLConnection yc = WWAurl.openConnection();
                            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                            String WWAcheck = in.readLine();
                            mLogger.createLogDebug("Result for WWA: "+WWAcheck );

                            in.close();
                            if (WWAcheck.equalsIgnoreCase("true")) {
                                mLogger.createLogDebug("Deployment when WWA webservice returns true");
                                task.chooseTransition("WWA Check Successful", "Successfully checked WWA");
                            } else {
                                mLogger.createLogDebug("WWA check failed");
                                task.chooseTransition("WWA Check Failed", "WWA Check Failed");
                            }
                        } catch (Exception e) {
                            mLogger.createLogDebug("Error checking WWA Date");
                            task.chooseTransition("WWA Check Failed", "WWA Check Failed");
                        }
                    } else {
                        mLogger.createLogDebug("WWACheck is disbled on the environment. Proceeds for Production deployment.");
                        task.chooseTransition("WWA Check Successful", "Successfully checked WWA");
                    }
                } else {
                    mLogger.createLogDebug("Deployment for non product contents");
                    task.chooseTransition("WWA Check Successful", "Successfully checked WWA");
                }
            }
        } catch (Exception e) {
            mLogger.createLogDebug(e.toString());
        }
    }
}
