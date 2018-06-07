/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow;

import java.util.*;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.access.CSUser;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author administrator
 */
public class SetValues implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.SetValues"));

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
        try {
            mLogger.createLogDebug("Just Entered into Set Values Class file :: ");
            CSWorkflow workflow = task.getWorkflow();
            CSTask[] tasks = workflow.getTasks();
            String reviews = task.getVariable("reviewvalue");
            mLogger.createLogDebug("Just Entered into Set Values Class file :: " + reviews);
            String ChkRvw = task.getVariable("ChkRvw");
            mLogger.createLogDebug("ChkRvw :: " + ChkRvw);
            if (reviews.equals("0") && ChkRvw.equals("ReviewsNotStarted")) {
                mLogger.createLogDebug("Only One Review ::");
                for (int i = 0; i < tasks.length; i++) {
                    if ("Review1".equals(tasks[i].getName())) {
                        CSUser reviewer1 = client.getUser(task.getVariable("approver1"), true);
                        mLogger.createLogDebug("First Review :: Getting CSUser object from the client :: " + reviewer1);
                        tasks[i].setOwner(reviewer1);
                    }
                }
                ChkRvw = "DoneFirstReview";
                task.setVariable("ChkRvw", "DoneFirstReview");
                mLogger.createLogDebug("Transition assigned from first if="+"Review1");
                task.chooseTransition("Review1", "Review with 1 Reviewer and going for first review");

            } else if (reviews.equals("0") && ChkRvw.equals("DoneFirstReview")) {

                mLogger.createLogDebug("Transition assigned from second if="+"Approved or NoApprovals");
                task.chooseTransition("Approved or NoApprovals", "Only One Review and Its Approved");

            } else if (reviews.equals("1") && ChkRvw.equals("ReviewsNotStarted")) {
                
                mLogger.createLogDebug("Two Reviews selected ::");
                for (int i = 0; i < tasks.length; i++) {
                    if ("Review1".equals(tasks[i].getName())) {
                        CSUser reviewer1 = client.getUser(task.getVariable("approver1"), true);
                        mLogger.createLogDebug("First Review :: Getting CSUser object from the client :: " + reviewer1);
                        tasks[i].setOwner(reviewer1);
                    }
                    if ("Review2".equals(tasks[i].getName())) {
                        CSUser reviewer2 = client.getUser(task.getVariable("approver2"), true);
                        mLogger.createLogDebug("Second Review :: Getting CSUser object from the client :: " + reviewer2.getName());
                        tasks[i].setOwner(reviewer2);
                    }
                }
                ChkRvw = "DoneFirstReview";
                task.setVariable("ChkRvw", "DoneFirstReview");

                mLogger.createLogDebug("Transition assigned from third if="+"Review1");

                task.chooseTransition("Review1", "Review with 2 Reviewers and going for first review");
            } else if (reviews.equals("1") && ChkRvw.equals("DoneFirstReview")) {
                mLogger.createLogDebug("Two Reviews selected ::");
                for (int i = 0; i < tasks.length; i++) {
                    if ("Review1".equals(tasks[i].getName())) {
                        CSUser reviewer1 = client.getUser(task.getVariable("approver1"), true);
                        mLogger.createLogDebug("First Review :: Getting CSUser object from the client :: " + reviewer1);
                        tasks[i].setOwner(reviewer1);
                    }
                    if ("Review2".equals(tasks[i].getName())) {
                        CSUser reviewer2 = client.getUser(task.getVariable("approver2"), true);
                        mLogger.createLogDebug("Second Review :: Getting CSUser object from the client :: " + reviewer2.getName());
                        tasks[i].setOwner(reviewer2);
                    }
                }
                mLogger.createLogDebug("Transition assigned from fourth if="+"Review2");
                
                task.chooseTransition("Review2", "Review with 2 Reviewers and going for 2nd review");
            } else if (reviews.equals("2")) {
                mLogger.createLogDebug("No Reviews selected ::");

                mLogger.createLogDebug("Transition assigned from last if="+"Approved or NoApprovals");
                task.chooseTransition("Approved or NoApprovals", "No Reviews");
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error in execute method::", e);
        }
    }
}
