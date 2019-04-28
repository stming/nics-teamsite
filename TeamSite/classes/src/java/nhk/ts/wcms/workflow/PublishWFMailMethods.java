package nhk.ts.wcms.workflow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

import nhk.ts.wcms.common.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.transform.XSLTransformer;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSWorkflow;

public class PublishWFMailMethods {

    private static final class SaxHandler extends DefaultHandler {

        // we enter to element 'qName':
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attrs) throws SAXException {

            if (qName.equals("event")) {
                final String name = attrs.getValue("name");
                final String taskname = attrs.getValue("taskname");
                if (name.equalsIgnoreCase("ChooseTransition") && taskname.equalsIgnoreCase("ContentReview")) {
                    PublishWFMailMethods.user = attrs.getValue("user");
                }
                if (name.equalsIgnoreCase("ChooseTransition") && taskname.equalsIgnoreCase("TALReview")) {
                    PublishWFMailMethods.user = attrs.getValue("user");
                }
            }
        }
    }

    protected static void closeQuietly(final XMLWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (final IOException ioex) {
                // quiet
                PublishWFMailMethods.mLogger.createLogDebug("Error in closeQuietly::", ioex);
            }
        }
    }

    protected static Element createXmlMailContent(final CSWorkflow job, final CSTask currentTask, final String webHost, final String hostname,
            final int targettaskId, final CSClient client) throws CSException {
        final Element mailContentRoot = DocumentHelper.createElement("MailContent");
        final Element jobElement = mailContentRoot.addElement("Job");
        final Element originalFiles = jobElement.addElement("OriginalFileList");
        originalFiles.addAttribute("hostname", hostname);

        final String desc = job.getVariable("JobDescription");
        jobElement.addAttribute("description", desc);

        final Element currentTaskElement = currentTask.toElement("CurrentTask");
        jobElement.add(currentTaskElement);
        final String TargettaskId = Integer.toString(targettaskId);
        currentTaskElement.addAttribute("TargetTaskId", TargettaskId);

        currentTaskElement.addAttribute("branchName", currentTask.getArea().getBranch().getName());
        currentTaskElement.addAttribute("areaName", currentTask.getArea().getName());

        final String priority = job.getVariable("priority");
        jobElement.addAttribute("priority", priority);

        final CSUser jobOwner = job.getOwner();
        jobElement.add(jobOwner.toElement("Owner"));

        final String strDueDate = job.getVariable("due_date");
        if (strDueDate != null && !strDueDate.equals("")) {
            final int intDueDate = Integer.parseInt(strDueDate);
            final java.util.Date due_date = new java.util.Date((long) intDueDate * 1000 - 8 * 60 * 60 * 1000); //minus 8 hours for GMT
            jobElement.addAttribute("due_date", DateFormatUtils.format(due_date, "yyyy-MM-dd HH:mm:SS"));
        }

        final CSTask[] tasks = job.getTasks();
        final Element commentsElement = jobElement.addElement("jobComments");

        for (final CSTask task : tasks) {
            final Element taskCommentsElement = commentsElement.addElement("TaskComment");
            taskCommentsElement.addAttribute("taskName", task.getName().toString());
            final CSComment[] comment = task.getComments();
            String completeComment = "";
            for (final CSComment element : comment) {
                completeComment = element.getComment().toString();
            }
            taskCommentsElement.addAttribute("approverComment", completeComment);
        }

        final Element filesCommentElement = jobElement.addElement("taskAllFilesComment");
        for (final CSTask task : tasks) {
            if (task.getName().equals("Attach Dependencies")) {
                final CSAreaRelativePath[] files = task.getFiles();
                for (final CSAreaRelativePath file : files) {
                    final Element fileCommentElement = filesCommentElement.addElement("taskFileComment");
                    final String path = currentTask.getArea().getVPath().toString() + "/" + file.toString();
                    final CSSimpleFile csfile = (CSSimpleFile) client.getFile(new CSVPath(path));
                    if (!(csfile instanceof CSHole) && csfile != null) {
                        PublishWFMailMethods.mLogger.createLogDebug("FileName: " + csfile.getName());
                        final CSComment[] fileComment = currentTask.getFileComments(csfile);
                        String completeComment = "";
                        for (final CSComment element : fileComment) {
                            completeComment = element.getComment().toString();
                        }
                        fileCommentElement.addAttribute("fileName", path);
                        fileCommentElement.addAttribute("fileComment", completeComment);
                    }
                }
            }
        }
        final CSComment[] jobComments = job.getComments();
        if (jobComments != null) {
            final Element jobCommentsElement = jobElement.addElement("Comments");
            for (final CSComment jobComment : jobComments) {
                jobCommentsElement.add(jobComment.toElement());
            }
        }
        mailContentRoot.addElement("WebHost").addText(webHost);
        return mailContentRoot;
    }

    protected static Element createXmlMailContent_targetTask(final CSWorkflow job, final CSTask currentTask, final CSTask targetTask, final String webHost,
            final String hostname, final String filelistvar, final CSClient client) throws CSException {

        final Element mailContentRoot = DocumentHelper.createElement("MailContent");
        final Element jobElement = mailContentRoot.addElement("Job");
        final Element originalFiles = jobElement.addElement("OriginalFileList");
        originalFiles.addAttribute("hostname", hostname);
        final String[] originals = PublishWFMailMethods.getOriginalFileList(filelistvar);
        for (final String original : originals) {
            final Element originalfile = originalFiles.addElement("originalfile");
            originalfile.addCDATA(original);
        }

        final String desc = job.getVariable("JobDescription");
        jobElement.addAttribute("description", desc);

        final Element currentTaskElement = currentTask.toElement("CurrentTask");
        jobElement.add(currentTaskElement);

        final Element targetTaskElement = targetTask.toElement("TargetTask");
        jobElement.add(targetTaskElement);

        currentTaskElement.addAttribute("branchName", currentTask.getArea().getBranch().getName());
        currentTaskElement.addAttribute("areaName", currentTask.getArea().getName());

        final String priority = job.getVariable("priority");
        jobElement.addAttribute("priority", priority);

        final CSUser jobOwner = job.getOwner();
        jobElement.add(jobOwner.toElement("Owner"));

        final CSTask[] tasks = job.getTasks();
        final Element commentsElement = jobElement.addElement("jobComments");

        for (final CSTask task : tasks) {
            final Element taskCommentsElement = commentsElement.addElement("TaskComment");
            taskCommentsElement.addAttribute("taskName", task.getName().toString());
            final CSComment[] comment = task.getComments();
            String completeComment = "";
            for (final CSComment element : comment) {
                completeComment = element.getComment().toString();
            }
            taskCommentsElement.addAttribute("approverComment", completeComment);
        }

        final Element filesCommentElement = jobElement.addElement("taskAllFilesComment");
        for (final CSTask task : tasks) {
            if (task.getName().equals("Attach Dependencies")) {
                final CSAreaRelativePath[] files = task.getFiles();
                for (final CSAreaRelativePath file : files) {
                    final Element fileCommentElement = filesCommentElement.addElement("taskFileComment");
                    final String path = currentTask.getArea().getVPath().toString() + "/" + file.toString();
                    final CSSimpleFile csfile = (CSSimpleFile) client.getFile(new CSVPath(path));
                    final CSComment[] fileComment = currentTask.getFileComments(csfile);
                    String completeComment = "";
                    for (final CSComment element : fileComment) {
                        completeComment = element.getComment().toString();
                    }
                    fileCommentElement.addAttribute("fileName", path);
                    fileCommentElement.addAttribute("fileComment", completeComment);
                }
            }
        }

        final CSComment[] jobComments = job.getComments();
        if (jobComments != null) {
            final Element jobCommentsElement = jobElement.addElement("Comments");
            for (final CSComment jobComment : jobComments) {
                final Element taskCommentsElement = jobCommentsElement.addElement("TaskComment");
                taskCommentsElement.addAttribute("taskName", jobComment.toElement().getName());
                taskCommentsElement.add(jobComment.toElement());
            }
        }

        mailContentRoot.addElement("WebHost").addText(webHost);
        return mailContentRoot;
    }

    /**
     * Close a XMLWriter quietly
     */
    public static String[] getOriginalFileList(final String filelistvar) {
        String filecontent = "";
        String[] orig_files = {};
        try {
            final FileReader file = new FileReader(filelistvar);
            final BufferedReader buff = new BufferedReader(file);
            boolean eof = false;
            while (!eof) {
                final String line = buff.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    filecontent += line + ",";
                }
            }
            buff.close();
            orig_files = filecontent.split(",");
        } catch (final Exception e) {
            PublishWFMailMethods.mLogger.createLogDebug("Error in getOriginalFileList::", e);
        }
        return orig_files;
    }

    public static String getReviewerEmailID(final String IWHome, final String workarea, final int JobID, final String reviewerinfo, final String mount) {

        try {
            PublishWFMailMethods.mLogger.createLogDebug("Coming in getReviewerEmailID method");
            //Execute command to check the file association with other workflows.
            final String filename = IWHome + "\\local\\logs\\" + JobID + ".xml";
            final String command = IWHome + "\\bin\\iwgetwfobj.exe" + " " + JobID;
            final Process child = Runtime.getRuntime().exec(command);
            PublishWFMailMethods.mLogger.createLogDebug("Command executed");
            final InputStream in = child.getInputStream();
            int c;
            String processOutPut = "";
            while ((c = in.read()) != -1) {
                processOutPut = processOutPut + (char) c;
            }
            PublishWFMailMethods.mLogger.createLogDebug("after Command executing");
            Writer output = null;
            final File file = new File(filename);
            output = new BufferedWriter(new FileWriter(file));
            output.write(processOutPut);
            output.close();
            // creates and returns new instance of SAX-implementation:
            final SAXParserFactory factory = SAXParserFactory.newInstance();

            // create SAX-parser...
            final SAXParser parser = factory.newSAXParser();
            // .. define our handler:
            final SaxHandler handler = new SaxHandler();

            // and parse:
            parser.parse(filename, handler);
            PublishWFMailMethods.user = PublishWFMailMethods.user.toUpperCase();
            PublishWFMailMethods.mLogger.createLogDebug("User in getReviewerEmailID" + PublishWFMailMethods.user);
            final FileReader Reviewerfile = new FileReader(mount + workarea + "/iw/config/Reviewers.cfg");
            final BufferedReader buffer = new BufferedReader(Reviewerfile);
            boolean endOfFile = false;
            while (!endOfFile) {
                final String line = buffer.readLine();
                if (line != null) {
                    if (reviewerinfo.equalsIgnoreCase("Contentreviewers")) {
                        if (line.startsWith(reviewerinfo) && line.contains(PublishWFMailMethods.user)) {
                            final String[] strArr = line.split("=");
                            Reviewerfile.close();
                            buffer.close();
                            PublishWFMailMethods.userEmail = strArr[1];
                            endOfFile = true;
                            PublishWFMailMethods.mLogger.createLogDebug("userEmail===" + PublishWFMailMethods.userEmail);
                            return PublishWFMailMethods.userEmail;
                        }
                    } else if (reviewerinfo.equalsIgnoreCase("Talreviewers")) {
                        if (line.startsWith(reviewerinfo) && line.contains(PublishWFMailMethods.user)) {
                            final String[] strArr = line.split("=");
                            Reviewerfile.close();
                            buffer.close();
                            PublishWFMailMethods.userEmail = strArr[1];
                            endOfFile = true;
                            PublishWFMailMethods.mLogger.createLogDebug("userEmail===" + PublishWFMailMethods.userEmail);
                            return PublishWFMailMethods.userEmail;

                        }
                    }
                }
            }
        } catch (final Exception ex) {
            PublishWFMailMethods.mLogger.createLogDebug("Error in getReviewerEmailID::", ex);
        }
        return PublishWFMailMethods.userEmail;
    }

    /**
     * Convenient method to get a task by name from a workflow. (linear search) Return null if task by name is not found.
     */
    protected static CSTask getTaskByName(final CSWorkflow job, final String name) throws CSException {
        if (name == null) {
            return null;
        }
        final CSTask[] tasks = job.getTasks();
        for (final CSTask task : tasks) {
            if (name.equals(task.getName())) {
                return task;
            }
        }
        return null;
    }

    /**
     * Send mail.
     */
    protected static void sendMail(final String recipients[], final String from, final String subject, final DataSource mailDataSource,
            final String mailServer, final Boolean debugMailSession) throws MessagingException {
        //boolean debug = false;

        //Set the host smtp address
        final Properties props = new Properties();
        props.put("mail.smtp.host", mailServer);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "80");

        // create some properties and get the default Session
        //Session session = Session.getDefaultInstance(props, null);
        final String mailUser = PublishWFGetIWCFGConfigDetails.getMailUser();
        final String mailPassword = PublishWFGetIWCFGConfigDetails.getMailPassword();
        final Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUser, mailPassword);
            }
        });

        session.setDebug(debugMailSession);

        // create a message
        final Message msg = new MimeMessage(session);
        // set the from and to address
        final InternetAddress addressFrom = new InternetAddress(mailUser);
        msg.setFrom(addressFrom);

        final InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        // Optional : You can also set your custom headers in the Email if you Want
        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setDataHandler(new DataHandler(mailDataSource));
        Transport.send(msg);
    }

    /**
     * Serialize an Element to String
     */
    protected static String serializeDomElementToString(final Element ele, final String encoding, final boolean compactXml) {
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
        } catch (final IOException ioex) {
            PublishWFMailMethods.mLogger.createLogDebug("Error in serializeDomElementToString::", ioex);
            throw new RuntimeException("Error when constructing XML string." + ioex.getMessage(), ioex);
        } finally {
            PublishWFMailMethods.closeQuietly(writer);
            IOUtils.closeQuietly(out);
        }
        return xml;
    }

    /**
     * Perform XSL Transformation on the mail content to construct a mail DataSource.
     */
    protected static DataSource transformToMailDataSource(final String xmlMailContent, final CSSimpleFile xslTemplateFile, final String MailEncoding,
            final String MIMEType) throws CSException, TransformerException, UnsupportedEncodingException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlMailContent.getBytes(MailEncoding));
        PublishWFMailMethods.mLogger.createLogDebug("TemplateFile:  " + xslTemplateFile);
        XSLTransformer.transform(inputStream, xslTemplateFile, outputStream);
        return new javax.mail.util.ByteArrayDataSource(outputStream.toByteArray(), MIMEType);
    }

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.PublishWFMailMethods"));

    /**
     * Create the mail content XML which will work with the corresponding XSLT mail template.
     */
    public static String userEmail = "";

    public static String user = "";
}
