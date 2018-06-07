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
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;
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
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.transform.XSLTransformer;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import nhk.ts.wcms.common.Logger;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.LogFactory;

public class PublishWFMailMethods {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.PublishWFMailMethods"));
    /**
     * Create the mail content XML which will work with the corresponding XSLT
     * mail template.
     */
    public static String userEmail = "";
    public static String user = "";

    protected static Element createXmlMailContent(CSWorkflow job, CSTask currentTask, String webHost, String hostname, int targettaskId, CSClient client) throws CSException {
        Element mailContentRoot = DocumentHelper.createElement("MailContent");
        Element jobElement = mailContentRoot.addElement("Job");
        Element originalFiles = jobElement.addElement("OriginalFileList");
        originalFiles.addAttribute("hostname", hostname);

        String desc = job.getVariable("JobDescription");
        jobElement.addAttribute("description", desc);

        Element currentTaskElement = currentTask.toElement("CurrentTask");
        jobElement.add(currentTaskElement);
        String TargettaskId = Integer.toString(targettaskId);
        currentTaskElement.addAttribute("TargetTaskId", TargettaskId);

        currentTaskElement.addAttribute("branchName", currentTask.getArea().getBranch().getName());
        currentTaskElement.addAttribute("areaName", currentTask.getArea().getName());

        String priority = job.getVariable("priority");
        jobElement.addAttribute("priority", priority);

        CSUser jobOwner = job.getOwner();
        jobElement.add(jobOwner.toElement("Owner"));
        
        String strDueDate = job.getVariable("due_date");
        if((strDueDate != null)&&(!strDueDate.equals(""))){
        	int intDueDate = Integer.parseInt(strDueDate);
        	java.util.Date due_date = new java.util.Date(((long)intDueDate*1000) - (8*60*60*1000));  //minus 8 hours for GMT
        	jobElement.addAttribute("due_date", DateFormatUtils.format(due_date, "yyyy-MM-dd HH:mm:SS"));
        }

        CSTask[] tasks = job.getTasks();
        Element commentsElement = jobElement.addElement("jobComments");

        for (int k = 0; k < tasks.length; k++) {
            Element taskCommentsElement = commentsElement.addElement("TaskComment");
            taskCommentsElement.addAttribute("taskName", tasks[k].getName().toString());
            CSComment[] comment = tasks[k].getComments();
            String completeComment = "";
            for (int m = 0; m < comment.length; m++) {
                completeComment = comment[m].getComment().toString();
            }
            taskCommentsElement.addAttribute("approverComment", completeComment);
        }

        Element filesCommentElement = jobElement.addElement("taskAllFilesComment");
        for (int k = 0; k < tasks.length; k++) {
            if (tasks[k].getName().equals("Attach Dependencies")) {
                CSAreaRelativePath[] files = tasks[k].getFiles();
                for (int m = 0; m < files.length; m++) {
                    Element fileCommentElement = filesCommentElement.addElement("taskFileComment");
                    String path = currentTask.getArea().getVPath().toString() + "/" + files[m].toString();
                    CSSimpleFile csfile = (CSSimpleFile) client.getFile(new CSVPath(path));
                    if((!(csfile instanceof CSHole)) && csfile!=null){
                     mLogger.createLogDebug("FileName: " + csfile.getName());
                    CSComment[] fileComment = currentTask.getFileComments(csfile);
                    String completeComment = "";
                    for (int n = 0; n < fileComment.length; n++) {
                        completeComment = fileComment[n].getComment().toString();
                    }
                    fileCommentElement.addAttribute("fileName", path);
                    fileCommentElement.addAttribute("fileComment", completeComment);
                    }
                }
            }
        }
        CSComment[] jobComments = job.getComments();
        if (jobComments != null) {
            Element jobCommentsElement = jobElement.addElement("Comments");
            for (int i = 0; i < jobComments.length; i++) {
                jobCommentsElement.add(jobComments[i].toElement());
            }
        }
        mailContentRoot.addElement("WebHost").addText(webHost);
        return mailContentRoot;
    }

    protected static Element createXmlMailContent_targetTask(CSWorkflow job, CSTask currentTask, CSTask targetTask,
            String webHost, String hostname, String filelistvar, CSClient client) throws CSException {

        Element mailContentRoot = DocumentHelper.createElement("MailContent");
        Element jobElement = mailContentRoot.addElement("Job");
        Element originalFiles = jobElement.addElement("OriginalFileList");
        originalFiles.addAttribute("hostname", hostname);
        String[] originals = getOriginalFileList(filelistvar);
        for (int i = 0; i < originals.length; i++) {
            Element originalfile = originalFiles.addElement("originalfile");
            originalfile.addCDATA(originals[i]);
        }

        String desc = job.getVariable("JobDescription");
        jobElement.addAttribute("description", desc);

        Element currentTaskElement = currentTask.toElement("CurrentTask");
        jobElement.add(currentTaskElement);

        Element targetTaskElement = targetTask.toElement("TargetTask");
        jobElement.add(targetTaskElement);

        currentTaskElement.addAttribute("branchName", currentTask.getArea().getBranch().getName());
        currentTaskElement.addAttribute("areaName", currentTask.getArea().getName());

        String priority = job.getVariable("priority");
        jobElement.addAttribute("priority", priority);

        CSUser jobOwner = job.getOwner();
        jobElement.add(jobOwner.toElement("Owner"));

        CSTask[] tasks = job.getTasks();
        Element commentsElement = jobElement.addElement("jobComments");

        for (int k = 0; k < tasks.length; k++) {
            Element taskCommentsElement = commentsElement.addElement("TaskComment");
            taskCommentsElement.addAttribute("taskName", tasks[k].getName().toString());
            CSComment[] comment = tasks[k].getComments();
            String completeComment = "";
            for (int m = 0; m < comment.length; m++) {
                completeComment = comment[m].getComment().toString();
            }
            taskCommentsElement.addAttribute("approverComment", completeComment);
        }

        Element filesCommentElement = jobElement.addElement("taskAllFilesComment");
        for (int k = 0; k < tasks.length; k++) {
            if (tasks[k].getName().equals("Attach Dependencies")) {
                CSAreaRelativePath[] files = tasks[k].getFiles();
                for (int m = 0; m < files.length; m++) {
                    Element fileCommentElement = filesCommentElement.addElement("taskFileComment");
                    String path = currentTask.getArea().getVPath().toString() + "/" + files[m].toString();
                    CSSimpleFile csfile = (CSSimpleFile) client.getFile(new CSVPath(path));
                    CSComment[] fileComment = currentTask.getFileComments(csfile);
                    String completeComment = "";
                    for (int n = 0; n < fileComment.length; n++) {
                        completeComment = fileComment[n].getComment().toString();
                    }
                    fileCommentElement.addAttribute("fileName", path);
                    fileCommentElement.addAttribute("fileComment", completeComment);
                }
            }
        }

        CSComment[] jobComments = job.getComments();
        if (jobComments != null) {
            Element jobCommentsElement = jobElement.addElement("Comments");
            for (int i = 0; i < jobComments.length; i++) {
                Element taskCommentsElement = jobCommentsElement.addElement("TaskComment");
                taskCommentsElement.addAttribute("taskName", jobComments[i].toElement().getName());
                taskCommentsElement.add(jobComments[i].toElement());
            }
        }

        mailContentRoot.addElement("WebHost").addText(webHost);
        return mailContentRoot;
    }

    /**
     * Send mail.
     */
    protected static void sendMail(String recipients[], String from, String subject, DataSource mailDataSource, String mailServer, Boolean debugMailSession) throws MessagingException {
        boolean debug = false;

        //Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", mailServer);

        // create some properties and get the default Session
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debugMailSession);

        // create a message
        Message msg = new MimeMessage(session);
        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[recipients.length];
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
     * Perform XSL Transformation on the mail content to construct a mail DataSource.
     */
    protected static DataSource transformToMailDataSource(String xmlMailContent, CSSimpleFile xslTemplateFile, String MailEncoding, String MIMEType) throws CSException, TransformerException, UnsupportedEncodingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlMailContent.getBytes(MailEncoding));
        mLogger.createLogDebug("TemplateFile:  " + xslTemplateFile);
        XSLTransformer.transform(inputStream, xslTemplateFile, outputStream);
        return new javax.mail.util.ByteArrayDataSource(outputStream.toByteArray(), MIMEType);
    }

    /**
     * Serialize an Element to String
     */
    protected static String serializeDomElementToString(Element ele, String encoding, boolean compactXml) {
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
            mLogger.createLogDebug("Error in serializeDomElementToString::", ioex);
            throw new RuntimeException("Error when constructing XML string." + ioex.getMessage(), ioex);
        } finally {
            closeQuietly(writer);
            IOUtils.closeQuietly(out);
        }
        return xml;
    }

    /**
     * Convenient method to get a task by name from a workflow. (linear search)
     * Return null if task by name is not found.
     */
    protected static CSTask getTaskByName(CSWorkflow job, String name) throws CSException {
        if (name == null) {
            return null;
        }
        CSTask[] tasks = job.getTasks();
        for (int i = 0; i < tasks.length; i++) {
            if (name.equals(tasks[i].getName())) {
                return tasks[i];
            }
        }
        return null;
    }

    /**
     * Close a XMLWriter quietly
     */
    public static String[] getOriginalFileList(String filelistvar) {
        String filecontent = "";
        String[] orig_files = {};
        try {
            FileReader file = new FileReader(filelistvar);
            BufferedReader buff = new BufferedReader(file);
            boolean eof = false;
            while (!eof) {
                String line = buff.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    filecontent += line + ",";
                }
            }
            buff.close();
            orig_files = filecontent.split(",");
        } catch (Exception e) {
            mLogger.createLogDebug("Error in getOriginalFileList::", e);
        }
        return orig_files;
    }

    protected static void closeQuietly(XMLWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ioex) {
                // quiet
                mLogger.createLogDebug("Error in closeQuietly::", ioex);
            }
        }
    }

    public static String getReviewerEmailID(String IWHome, String workarea, int JobID, String reviewerinfo, String mount) {

        try {
            mLogger.createLogDebug("Coming in getReviewerEmailID method");
            //Execute command to check the file association with other workflows.
            String filename = IWHome + "\\local\\logs\\" + JobID + ".xml";
            String command = IWHome + "\\bin\\iwgetwfobj.exe" + " " + JobID;
            Process child = Runtime.getRuntime().exec(command);
            mLogger.createLogDebug("Command executed");
            InputStream in = child.getInputStream();
            int c;
            String processOutPut = "";
            while ((c = in.read()) != -1) {
                processOutPut = processOutPut + ((char) c);
            }
            mLogger.createLogDebug("after Command executing");
            Writer output = null;
            File file = new File(filename);
            output = new BufferedWriter(new FileWriter(file));
            output.write(processOutPut);
            output.close();
            // creates and returns new instance of SAX-implementation:
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // create SAX-parser...
            SAXParser parser = factory.newSAXParser();
            // .. define our handler:
            SaxHandler handler = new SaxHandler();

            // and parse:
            parser.parse(filename, handler);
            user = user.toUpperCase();
            mLogger.createLogDebug("User in getReviewerEmailID" + user);
            FileReader Reviewerfile = new FileReader(mount + workarea + "/iw/config/Reviewers.cfg");
            BufferedReader buffer = new BufferedReader(Reviewerfile);
            boolean endOfFile = false;
            while (!endOfFile) {
                String line = buffer.readLine();
                if (line != null) {
                    if (reviewerinfo.equalsIgnoreCase("Contentreviewers")) {
                        if (line.startsWith(reviewerinfo) && (line.contains(user))) {
                            String[] strArr = line.split("=");
                            Reviewerfile.close();
                            buffer.close();
                            userEmail = strArr[1];
                            endOfFile = true;
                            mLogger.createLogDebug("userEmail===" + userEmail);
                            return userEmail;
                        }
                    } else if (reviewerinfo.equalsIgnoreCase("Talreviewers")) {
                        if (line.startsWith(reviewerinfo) && (line.contains(user))) {
                            String[] strArr = line.split("=");
                            Reviewerfile.close();
                            buffer.close();
                            userEmail = strArr[1];
                            endOfFile = true;
                            mLogger.createLogDebug("userEmail===" + userEmail);
                            return userEmail;

                        }
                    }
                }
            }
        } catch (Exception ex) {
            mLogger.createLogDebug("Error in getReviewerEmailID::", ex);
        }
        return userEmail;
    }

    private static final class SaxHandler extends DefaultHandler {

        // we enter to element 'qName':
        public void startElement(String uri, String localName,
                String qName, Attributes attrs) throws SAXException {

            if (qName.equals("event")) {
                String name = attrs.getValue("name");
                String taskname = attrs.getValue("taskname");
                if (name.equalsIgnoreCase("ChooseTransition") && taskname.equalsIgnoreCase("ContentReview")) {
                    user = attrs.getValue("user");
                }
                if (name.equalsIgnoreCase("ChooseTransition") && taskname.equalsIgnoreCase("TALReview")) {
                    user = attrs.getValue("user");
                }
            }
        }
    }
}
