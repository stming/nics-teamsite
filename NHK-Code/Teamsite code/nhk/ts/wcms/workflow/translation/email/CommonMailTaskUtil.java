package nhk.ts.wcms.workflow.translation.email;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.transform.TransformerException;

import nhk.ts.wcms.common.CommonUtil;
import nhk.ts.wcms.common.Constants;
import nhk.ts.wcms.common.IOHelper;
import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.common.TSHelper;
import nhk.ts.wcms.dct.MasterFactory;
import nhk.ts.wcms.workflow.PublishWFGetIWCFGConfigDetails;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.access.CSGroup;
import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.common.xml.ElementableUtils;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.transform.XSLTransformer;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSGroupTask;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSUserTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.serverutils100.local.InstalledLocationsLocal;
import com.interwoven.sharedutils100.TUserMessage;
import com.interwoven.ui.teamsite.workflow.task.TaskContext;

public class CommonMailTaskUtil {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.translation.email.CommonMailTaskUtil"));
    public static final String ESCAPE_EMAIL_REGEX = "^([0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*@([0-9a-zA-Z][-\\w]*[0-9a-zA-Z]\\.)+[a-zA-Z]{2,9})$";

    // create EMail content
    public static Element createXmlMailContent(CSClient client, CSWorkflow job, CSTask currentTask, CSTask targetTask, CSTask prevTask) throws Exception {

        Locale locale = client.getContext().getLocale();
        Element mailContentRoot = DocumentHelper.createElement("MailContent");
        Element jobElement = mailContentRoot.addElement("Job");
        jobElement.addAttribute("name", job.getName());
        jobElement.addAttribute("description", job.getDescription());

        Element currentTaskElement = currentTask.toElement("CurrentTask");
        currentTaskElement.addAttribute("branchName", currentTask.getArea().getBranch().getName());
        currentTaskElement.addAttribute("areaName", currentTask.getArea().getName());
        jobElement.add(currentTaskElement);
        String priority = job.getVariable("priority");
        if (priority != null) {
            String prioritylbl = TUserMessage.LocalizeMessage(locale, (new StringBuilder()).append("priority_map.").append(priority).append(".label").toString(), "com.interwoven.ui.teamsite.workflow");
            jobElement.addAttribute("priority", prioritylbl);
        }
        String strDueDate = job.getVariable("due_date");

        if (strDueDate != null) {
            try {
                Date dueDate = TaskContext.parseDate(strDueDate);
                if (dueDate != null) {
                    jobElement.add(ElementableUtils.toElement("DueDate", dueDate));
                } else {
                    mLogger.createLogDebug((new StringBuilder()).append("Job variable due_date=[").append(strDueDate).append("] in job id=[").append(job.getId()).append("] could not be parsed.").toString());
                    jobElement.addElement("DueDate");
                }
            } catch (Exception e) {
                mLogger.createLogDebug("Error in CommonMailTakUtil", e);
            }
        }
        CSUser jobOwner = job.getOwner();
        jobElement.add(jobOwner.toElement("Owner"));
        CSComment jobComments[] = job.getComments();
        if (jobComments != null) {
            Element jobCommentsElement = jobElement.addElement("Comments");
            for (CSComment comment : jobComments) {
                jobCommentsElement.add(comment.toElement());
            }
        }
        Element targetTaskElement = null;
        // create custom target task element for group task
        if (targetTask instanceof CSGroupTask) {
            CSGroupTask targGroupTask = (CSGroupTask) targetTask;
            targetTaskElement = groupTaskToElement(targGroupTask);
        } else {// default target task element
            targetTaskElement = targetTask.toElement("TargetTask");
        }
        jobElement.add(targetTaskElement);

        if (prevTask != null) {
            Element previousTaskElement = prevTask.toElement("PreviousTask");
            mailContentRoot.add(previousTaskElement);
        }
        mailContentRoot.addElement("WebHost").setText(TSHelper.getServerName());

        return mailContentRoot;
    }

    public static Element createTranslationMailContent(CSWorkflow job, List branchEls, String senderAddress, CSSimpleFile xslTemplateFile) {
        DataSource mailDataSource = null;
        Element mailContentRoot = null;
        //   Element mailContentRoot = DocumentHelper.createElement("MailContent");
        try {
            //   mailContentRoot.addElement("WebHost").setText(TSHelper.getServerName());
            String mail_subject = IOHelper.getPropertyValue("TranslationWF.mailSubject") + "[" + job.getId() + "]";
            // mLogger.createLogDebug("Create translation: " + branchEls.toArray());
            CSClient masterClient = MasterFactory.getMasterClient();
            for (Object object : branchEls) {
                mailContentRoot = DocumentHelper.createElement("MailContent");
                //mailContentRoot.addElement("WebHost").setText(TSHelper.getServerName());
                mailContentRoot.addElement("WebHost").setText(IOHelper.getCommonPropertyValue("CommonMailTaskUtil.teamsiteServerIP"));
                Element branchEl = (Element) (object);
                mLogger.createLogDebug("Branch EL: " + branchEl.asXML());
                mailContentRoot.add(branchEl.createCopy());
                String mailContentXml = CommonMailTaskUtil.serializeDomElementToString(mailContentRoot, Constants.MAIL_ENCODING, true);
                // Transform:
                mailDataSource = CommonMailTaskUtil.transformToMailDataSource(mailContentXml, xslTemplateFile);
                Element el = (Element) branchEl.selectSingleNode("WebMaster");
                String email = CommonMailTaskUtil.getEmailAddress(el.attributeValue("Email"), masterClient.getCurrentUser().getEmailAddress());
                CommonMailTaskUtil.sendMail(senderAddress, email, "", mail_subject, mailDataSource, "2");
            }
        } catch (UnsupportedEncodingException e) {
            mLogger.createLogError("Error in createTranslationMailContent()!!!", e);
        } catch (CSException e) {
            mLogger.createLogError("Error in createTranslationMailContent()!!!", e);
        } catch (TransformerException e) {
            mLogger.createLogError("Error in createTranslationMailContent()!!!", e);
        } catch (Exception e) {
            mLogger.createLogError("Error in createTranslationMailContent()!!!", e);
        }
        return mailContentRoot;
    }

    public static DataSource transformToMailDataSource(String xmlMailContent, CSSimpleFile xslTemplateFile) throws CSException, TransformerException, UnsupportedEncodingException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlMailContent.getBytes(Constants.MAIL_ENCODING));
        mLogger.createLogDebug("xmlMailContent::\n" + xmlMailContent);
        XSLTransformer.transform(inputStream, xslTemplateFile, outputStream);

        return new javax.mail.util.ByteArrayDataSource(outputStream.toByteArray(), Constants.MAIL_MIME_TYPE);
    }

    /**
     * Send mail.
     */
    public synchronized static void sendMail(String from, String to, String cc, String subject, DataSource mailDataSource, String priority) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", PublishWFGetIWCFGConfigDetails.getMailServer());
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage msg = new MimeMessage(session);
        msg.setHeader("Xlint", "nhk.ts.wcms.translation.email.CommonMailTaskUtil");
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        if (!StringUtils.isBlank(cc)) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
        }
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setDataHandler(new DataHandler(mailDataSource));
        if (!StringUtils.isBlank(priority)) {
            msg.addHeader("X-Priority", priority);
        }
        Transport.send(msg);
    }

    /**
     * Serialize an Element to String
     */
    public static String serializeDomElementToString(Element ele, String encoding, boolean compactXml) {
        OutputFormat format = null;
        if (compactXml) {
            format = OutputFormat.createCompactFormat();
        } else {
            format = OutputFormat.createPrettyPrint();
        }
        format.setEncoding(encoding);
        Writer out = null;
        XMLWriter writer = null;
        String xml = null;
        try {
            out = new StringWriter();
            writer = new XMLWriter(out, format);
            writer.write(ele);
            writer.flush();
            xml = out.toString();
        } catch (IOException ioex) {
            mLogger.createLogDebug("Error in CommonMailTakUtil", ioex);
        } finally {
            closeQuietly(writer);
            IOUtils.closeQuietly(out);
        }
        return xml;
    }

    /**
     * Close a XMLWriter quietly
     */
    public static void closeQuietly(XMLWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ioex) {
                mLogger.createLogDebug("Error in closeQuietly::", ioex);
            }
        }
    }

    public static Element groupTaskToElement(CSGroupTask groupTask) {
        Element targetTaskElement = DocumentHelper.createElement("TargetTask");
        try {
            targetTaskElement.addAttribute("id", ((Integer) groupTask.getId()).toString());
            targetTaskElement.addElement("ActivationDate").setText(groupTask.getActivationDate().toString());
            targetTaskElement.addElement("RelativeTimeout").setText(((Integer) groupTask.getRelativeTimeout()).toString());
            return targetTaskElement;
        } catch (CSException e) {
            mLogger.createLogDebug("Error in CommonMailTakUtil", e);
            return targetTaskElement;
        }
    }

    @SuppressWarnings("unchecked")
    public static String gettaskOwners(CSWorkflow job) {
        HashSet taskowners = new HashSet();
        try {
            CSTask tasks[] = job.getTasks();
            for (CSTask task : tasks) {
                mLogger.createLogDebug("task type??" + task.getClass().getSimpleName());
                if ((!task.getName().equals("Start") && !task.getName().equals("End")) && (task instanceof CSUserTask)) {
                    taskowners.add(task.getOwner().getEmailAddress());
                }
            }
            mLogger.createLogDebug("taskowners: " + CommonUtil.getStringFromSet(taskowners));
        } catch (CSAuthorizationException e) {
            mLogger.createLogError("Error in CommonMailTakUtil", e);
        } catch (CSRemoteException e) {
            mLogger.createLogError("Error in CommonMailTakUtil", e);
        } catch (CSObjectNotFoundException e) {
            mLogger.createLogError("Error in CommonMailTakUtil", e);
        } catch (CSExpiredSessionException e) {
            mLogger.createLogError("Error in CommonMailTakUtil", e);
        } catch (CSException e) {
            mLogger.createLogError("Error in CommonMailTakUtil", e);
        }
        return CommonUtil.getStringFromSet(taskowners);
    }

    public static String getEmailAddress(String name, String defaultValue) throws Exception {
        StringBuilder sb = new StringBuilder();
        CSClient masterClient = MasterFactory.getMasterClient();
        if (!StringUtils.isBlank(name)) {
            String names[] = StringUtils.split(name, Constants.delimiter);
            for (String tsname : names) {
                // Its a EMAIL
                Matcher match = CommonUtil.getPatternMatcher(tsname, ESCAPE_EMAIL_REGEX);
                if (match != null && match.matches()) {
                    sb.append(tsname + Constants.delimiter);
                    continue;
                }
                // Its a Group
                CSGroup group = masterClient.getGroup(tsname, true);
                if (group != null) {
                    mLogger.createLogDebug("recipient is a group:::" + group.getFullName());
                    CSIterator users = group.getUsers(true);
                    sb.append(getMembersEmail(users, defaultValue) + Constants.delimiter);
                    continue;
                }
                // Its a User
                sb.append(getEmailAddressForUser(tsname, defaultValue) + Constants.delimiter);
            }
            if (sb != null && !StringUtils.isBlank(sb.toString())) {
                return StringUtils.removeEnd(sb.toString(), Constants.delimiter);
            }
        }
        return defaultValue;
    }

    public static String getMembersEmail(CSIterator users, String defaultValue) throws Exception {
        StringBuilder emailAddresses = new StringBuilder();
        while (users.hasNext()) {
            CSUser user = (CSUser) users.next();
            if (user != null) {
                String email = user.getEmailAddress();
                if (!StringUtils.isBlank(email)) {
                    Matcher match = CommonUtil.getPatternMatcher(email, ESCAPE_EMAIL_REGEX);
                    if (match != null && match.matches()) {
                        emailAddresses.append(email + Constants.delimiter);
                    } else {
                        mLogger.createLogDebug("Email address[" + email + "]for this user[" + user.getName() + "]is INVALID!");
                    }
                } else {
                    mLogger.createLogDebug("Email address is EMPTY for this user[" + user.getName() + "].");
                }
            }
        }
        if (emailAddresses != null && !StringUtils.isBlank(emailAddresses.toString())) {
            return emailAddresses.toString().substring(0, emailAddresses.length() - 1);
        }
        return defaultValue;
    }

    public static String getEmailAddressForUser(String userName, String defaultValue) throws Exception {
        CSClient masterClient = MasterFactory.getMasterClient();
        if (!StringUtils.isBlank(userName)) {
            CSUser user = masterClient.getUser(userName, true);
            if (user != null) {
                mLogger.createLogDebug("recipient is a user:::" + user.getFullName());
                String email = user.getEmailAddress();
                if (!StringUtils.isBlank(email)) {
                    Matcher match = CommonUtil.getPatternMatcher(email, ESCAPE_EMAIL_REGEX);
                    if (match != null && match.matches()) {
                        return email;
                    }
                    mLogger.createLogDebug("Email address[" + email + "]for this user[" + userName + "]is INVALID!");
                }
                mLogger.createLogDebug("Email address is EMPTY for this user[" + userName + "].");
            }
            mLogger.createLogDebug("User[" + userName + "] does not exist.");
        }
        mLogger.createLogDebug("Username is null.");
        return null;
    }
}
