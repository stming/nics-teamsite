package nhk.ts.wcms.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import javax.activation.DataSource;
import com.interwoven.cssdk.access.CSUser;
import org.dom4j.Element;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;

/*
 * This is a common class used to send an email from the Email task of the workflow.
 * All the email tasks in the workflow uses this class based on the name of the email task
 * it will set task variables like from, to, subject and mail content xml.
 */
public class SendMail implements CSURLExternalTask {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.SendMail"));
    public static final String MAIL_SUBJECT = "Subject";
     protected static final Pattern EMAIL_PATTERN = Pattern.compile(".+@[^\\.]+\\..+$", 2);
    public static final String MAIL_ENCODING = "UTF-8";
    public static final String MAIL_MIME_TYPE = "text/html";
    public static final String MAIL_SERVER = PublishWFGetIWCFGConfigDetails.getMailServer();
    public static final String HOST_NAME = PublishWFGetIWCFGConfigDetails.getHost();
    public static final String KEY_MAIL_TEMPLATE = "BodyStylesheet";
    public static final String KEY_TARGET_TASK_NAME = "target_task_name";
    public static final String SERVER_NAME = "ServerName";
    public static final String STORE = "Store";
    public static final String ORIGINAL_FILES = "originalfiles";
    public static final String WEB_HOST = "localhost";
    public static final boolean DEBUG_MAIL_SESSION = true;
    private String transition = "";
    private String transitionComment = "Completed Email Notification";
    String reviewer = "";

    public void execute(CSClient client, CSExternalTask currentTask, Hashtable params) throws CSException {

        CSWorkflow job = currentTask.getWorkflow();
        String iwuser_emailid = job.getOwner().getEmailAddress();
        int taskID = currentTask.getId();
        String defaultTransition = currentTask.getTransitions()[0];
        this.transition = defaultTransition;
        String targetTaskName = currentTask.getVariable(KEY_TARGET_TASK_NAME);
        CSTask targetTask = PublishWFMailMethods.getTaskByName(job, targetTaskName);
        if (targetTask == null) {
            mLogger.createLogDebug("Error while executing task: Target Task \"" + targetTaskName + "\" not found. Mail is not sent.");
            this.transitionComment = "Error while executing task: Target Task \"" + targetTaskName + "\" not found. Mail is not sent.";
            currentTask.chooseTransition(this.transition, this.transitionComment);
            return;
        }
        String strSubject = currentTask.getVariable(MAIL_SUBJECT).toString();
        Date d = new Date();
        String DATE_FORMAT = "MM-dd-yyyy hh:mm:ss";
        //Create object of SimpleDateFormat and pass the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String currentdate = sdf.format(d);
        strSubject = currentTask.getVariable("Subject");
        if(strSubject == null || strSubject.length()==0)
        strSubject = "Job ID: " + job.getId() + " | " + strSubject + " | " + currentdate;
        int echeckFlag = 0;
        String from = iwuser_emailid;
        mLogger.createLogDebug("From email Address ::" + from);
   /*     String[] recipients = new String[1];
        if (currentTask.getName().equals("NotifyReviewer1")) {
            String reviewer1_mailid = client.getUser(currentTask.getVariable("approver1"), true).getEmailAddress();
            mLogger.createLogDebug("Reviewer one mail id :: " + reviewer1_mailid);
            recipients[0] = reviewer1_mailid;
        } else if (currentTask.getName().equals("NotifyReviewer2")) {
            String reviewer2_mailid = client.getUser(currentTask.getVariable("approver2"), true).getEmailAddress();
            mLogger.createLogDebug("Reviewer two mail id :: " + reviewer2_mailid);
            recipients[0] = reviewer2_mailid;
        } */
    //    else
         Collection temprecipients = buildRecipientList(client,currentTask);
         String recipients [] = (String []) temprecipients.toArray (new String [temprecipients.size()]);
        String hostname = "sgdev-base";
        mLogger.createLogDebug("Host Name is :: " + hostname);
        String strTemplate = currentTask.getVariable(KEY_MAIL_TEMPLATE);
        mLogger.createLogDebug("Debug Statement for testing purpose :: 1");
        CSVPath templateVpath = new CSVPath(strTemplate);
        mLogger.createLogDebug("Debug Statement for testing purpose :: 2");
        CSSimpleFile xslTemplateFile = (CSSimpleFile) (client.getFile(templateVpath));
        mLogger.createLogDebug("Debug Statement for testing purpose :: 3");
        Element mailContent = null;
        mailContent = PublishWFMailMethods.createXmlMailContent(job, currentTask, WEB_HOST, hostname, targetTask.getId(), client);
        mLogger.createLogDebug("Debug Statement for testing purpose :: 4");
        String mailContentXml = PublishWFMailMethods.serializeDomElementToString(mailContent, MAIL_ENCODING, true);
        mLogger.createLogDebug("Debug Statement for testing purpose :: 5");
        // 4. Transform:
        DataSource mailDataSource = null;
        try {
            mLogger.createLogDebug("recipients are :: " + recipients);
            mLogger.createLogDebug("from address is :: " + from);
            mLogger.createLogDebug("subject is :: " + strSubject);
            mLogger.createLogDebug("Mail Server :: " + MAIL_SERVER);
            mLogger.createLogDebug("Mail debug session :: " + DEBUG_MAIL_SESSION);
            mailDataSource = PublishWFMailMethods.transformToMailDataSource(mailContentXml, xslTemplateFile, MAIL_ENCODING, MAIL_MIME_TYPE);
            mLogger.createLogDebug("Debug Statement for testing purpose :: 6");
            PublishWFMailMethods.sendMail(recipients, from, strSubject, mailDataSource, MAIL_SERVER, DEBUG_MAIL_SESSION);
            mLogger.createLogDebug("Debug Statement for testing purpose :: 7");
            mLogger.createLogDebug("Successfully Sent Emails");
            mLogger.createLogDebug("Target Task Name :: " + targetTaskName);
            currentTask.chooseTransition(this.transition, this.transitionComment);
        } catch (Exception ex) {
            mLogger.createLogDebug("Error while executing task:Mail is not sent.", ex);
            this.transitionComment = "Error while executing task: Exception \"" + ex.getMessage() + "\" caught during mail processing. Mail is not sent.";
            currentTask.chooseTransition(this.transition, this.transitionComment);
            return;
        }
        //5. Transition taskSys
     //   mLogger.createLogDebug("Current Task Name before if::" + currentTask.getName().toString());
    }
    protected Collection<String> buildRecipientList(CSClient client,CSExternalTask currentTask)
  {
    Collection recipients = new ArrayList();
    try
    {

      Matcher matcher = EMAIL_PATTERN.matcher("");
      for (Iterator i$ = Arrays.asList(currentTask.getVariable("Recipients").split(",")).iterator(); i$.hasNext(); ) { String userOrEmail = (String)i$.next();

        if (!("".equals(userOrEmail.trim())))
        {
          if (matcher.reset(userOrEmail).matches())
          {
            recipients.add(userOrEmail);
          }
          else
          {

            CSUser user = client.getUser(userOrEmail, true);
            if ((null != user) && (user.isValid()))
            {
              String email = user.getEmailAddress();
              if ((null != email) && (!("".equals(email.trim()))))
              {
                recipients.add(email);
              mLogger.createLogDebug("buildRecipientList: found email address: " + email + ", for user: " + userOrEmail);
             }
              else
              {
                mLogger.createLogDebug("buildRecipientList: no email address found for user: " + userOrEmail + ", user will not receive the email.");
              }

            }
            else
            {
              mLogger.createLogDebug("buildRecipientList: invalid user name: " + userOrEmail + ", user will not receive the email.");
            }
          }
        }

      }

    }
    catch (CSException e)
    {
      throw new RuntimeException("Error while communicating with TeamSite: " + e.getMessage(), e);
    }

    return recipients;
  }
}
