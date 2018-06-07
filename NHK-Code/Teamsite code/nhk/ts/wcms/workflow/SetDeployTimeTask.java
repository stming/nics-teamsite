/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow;

import nhk.ts.wcms.common.Logger;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.livesite.workflow.WorkflowUtils;
import com.interwoven.livesite.workflow.web.task.AbstractAjaxWebTask;
import com.interwoven.livesite.workflow.web.task.AjaxWebTaskContext;
import com.interwoven.livesite.workflow.web.task.WebTaskContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 *
 * @author sbhojnag
 */
public class SetDeployTimeTask extends AbstractAjaxWebTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.SetDeployTimeTask"));
    public static final String JOB_VAR_NAME = "timeout_job_variable";

    public void getCurrentDeployTime(WebTaskContext context, HttpServletRequest request, HttpServletResponse response) throws CSException {
        AjaxWebTaskContext ajaxContext = (AjaxWebTaskContext) context;
        CSClient client = context.getClient();
        CSTask task = context.getTask();
        Document responseDoc = ajaxContext.getResponseDocument();
        Element responseElem = responseDoc.getRootElement();

        Date timeoutDate = new Date();
        try {
            String taskToSetTimeout = task.getVariable("TaskToSetTimeout");
            mLogger.createLogDebug("taskToSetTimeout: " + taskToSetTimeout);
            String deployType = task.getWorkflow().getVariable("deployType");
            mLogger.createLogDebug("Deploy Type: " + deployType);
            String isproduct_related = task.getWorkflow().getVariable("isproduct_related");
            mLogger.createLogDebug("Is Product Related: " + isproduct_related);
            CSTask targetTask = WorkflowUtils.getTaskByName(task.getWorkflow(), taskToSetTimeout);
            mLogger.createLogDebug("before getting timeoutDate");
            String wwa = task.getWorkflow().getVariable("wwa");
            mLogger.createLogDebug("WWA Date: " + wwa);
            if (deployType.equalsIgnoreCase("yes")) {
                mLogger.createLogDebug("Immediate Deployment");
                responseElem.addElement("WWA").addText("empty");
                mLogger.createLogDebug("Timeout Value: " + timeoutDate);
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                String currentDateString = formatter.format(timeoutDate);
                mLogger.createLogDebug("Current deploy time is set to: " + timeoutDate.getTime());
                responseElem.addElement("Result").addText("SUCCESS");
                responseElem.addElement("CurrentTime").addText(timeoutDate.getTime() + "");
            } else {
                if (isproduct_related.equalsIgnoreCase("true")) {
                    mLogger.createLogDebug("Product Deployment");
                    mLogger.createLogDebug("Inside WWA Date Set");
                    responseElem.addElement("WWA").addText("isSet");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    timeoutDate = formatter.parse(wwa);
                    mLogger.createLogDebug("Current WWA deploy time is set to: " + timeoutDate.getTime());
                    responseElem.addElement("CurrentTime").addText(timeoutDate.getTime() + "");
                    responseElem.addElement("Result").addText("SUCCESS");
                } else {
                    mLogger.createLogDebug("Non Product Deployment");
                    responseElem.addElement("WWA").addText("empty");
                    mLogger.createLogDebug("Timeout Value: " + timeoutDate);
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    String currentDateString = formatter.format(timeoutDate);
                    mLogger.createLogDebug("Current deploy time is set to: " + timeoutDate.getTime());
                    responseElem.addElement("Result").addText("SUCCESS");
                    responseElem.addElement("CurrentTime").addText(timeoutDate.getTime() + "");
                }
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error setting the deploy time: " + e.getMessage(), e);
        }
    }

    public void setDeployTime(WebTaskContext context, HttpServletRequest request, HttpServletResponse response) throws CSException {
        AjaxWebTaskContext ajaxContext = (AjaxWebTaskContext) context;
        CSClient client = context.getClient();
        CSTask task = context.getTask();
        Document responseDoc = ajaxContext.getResponseDocument();
        Element responseElem = responseDoc.getRootElement();
        Date timeoutDate = new Date();


        try {
            String taskToSetTimeout = task.getVariable("TaskToSetTimeout");
            mLogger.createLogDebug("taskToSetTimeout: " + taskToSetTimeout);
            String[] tmp = (String[]) context.getRequestParameters().get("deployTime");
            String[] tmp1 = (String[]) context.getRequestParameters().get("localOffset");
            String newDeployTime = tmp[0];
            String localOffset = tmp1[0];

            mLogger.createLogDebug("New Deploy Time to set: " + newDeployTime);
            CSTask targetTask = WorkflowUtils.getTaskByName(task.getWorkflow(), taskToSetTimeout);
            String isproduct_related = task.getWorkflow().getVariable("isproduct_related");
            String deployType = task.getWorkflow().getVariable("deployType");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
            timeoutDate = sdf.parse(newDeployTime);
            Long utctime = timeoutDate.getTime() + Integer.parseInt(localOffset);
            Long servertime = utctime + (8*3600000);
            Date servertimeoutDate = new Date(servertime);
            String newServerDeployTime = sdf.format(servertimeoutDate);
           // servertimeoutDate=sdf.format(timeoutDate);
       //     DateFormat
            targetTask.setTimeout(servertimeoutDate);
            mLogger.createLogDebug("New Server Deploy Time to set: " + newServerDeployTime);
            task.getWorkflow().setVariable(JOB_VAR_NAME, newServerDeployTime);
            task.getWorkflow().setVariable("publish_on", newServerDeployTime);
            responseElem.addElement("Result").addText("SUCCESS");
            responseElem.addElement("Message").addText("The timeout of the task " + taskToSetTimeout + " was set to " + newServerDeployTime);
        } catch (Exception e) {
            mLogger.createLogDebug("Error changing the approver: " + e.getMessage(), e);
        }
    }
}
