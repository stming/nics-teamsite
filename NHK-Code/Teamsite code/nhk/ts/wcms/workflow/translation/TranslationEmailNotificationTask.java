package nhk.ts.wcms.workflow.translation;

import java.util.Hashtable;
import java.util.List;

import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.common.TSHelper;
import nhk.ts.wcms.dct.MasterFactory;
import nhk.ts.wcms.workflow.translation.bean.NHKTranslationObjectHolder;
import nhk.ts.wcms.workflow.translation.email.CommonMailTaskUtil;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;

public class TranslationEmailNotificationTask implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.translation.TranslationEmailNotificationTask"));

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params)
            throws CSException {
        try {
            mLogger.createLogDebug("inside execute of TranslationEmailNotificationTask!!!");
            CSWorkflow job = task.getWorkflow();
            String strTemplate = TSHelper.getTaskVariable(task, "mail_template");
            String senderAddress = job.getCreator().getEmailAddress();
            // Locate the email XSLT template file and set mail params
            CSVPath templateVpath = new CSVPath(strTemplate);
            //Get the email template xsl using master client, as not all users will have access to the config branch.
            CSSimpleFile xslTemplateFile = (CSSimpleFile) (MasterFactory.getMasterClient().getFile(templateVpath));
            //Add existing file content to mailcontent
            Element branchesContent = NHKTranslationObjectHolder.getTranslationRootEl();
            if (branchesContent != null) {
                List branchEls = NHKTranslationObjectHolder.getTranslationRootEl().content();
                CommonMailTaskUtil.createTranslationMailContent(job, branchEls, senderAddress, xslTemplateFile);
            }
            // Transition task
            task.chooseTransition(task.getTransitions()[0], "Completed copying");
        } catch (Exception e) {
            mLogger.createLogError("Error in TranslationEmailNotificationTask!!", e);
        }
    }
}
