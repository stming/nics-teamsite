/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.common.web.URLRedirectForwardAction;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.MimeUtility;

import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.dao.ContactUs;
import nhk.ls.runtime.dao.DAOException;

import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 *
 * @author administrator
 */
public class PushContactUsDetails {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.PushContactUsDetails"));
    private DataManager dataManager;
    private static final String EXTERNAL_PARAM_DCR_PATH = "DCRPath";
    private static final String EXTERNAL_PARAM_COORDINATES = "coordinates";
    private static final String EXTERNAL_PARAM_COORDINATES_REGEX = "^[0-9]*[-][0-9]*";
    private static final String EXTERNAL_PARAM_EMAILID_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    public ForwardAction doSaveDetails(RequestContext context) throws DAOException, UnsupportedEncodingException {

        ContactUs contactUsForm = new ContactUs();
        contactUsForm.setFirstName(context.getParameterString("firstname"));
        contactUsForm.setLastName(context.getParameterString("lastname"));
        contactUsForm.setContactNumber(context.getParameterString("contactnumber"));
        contactUsForm.setEmailId(context.getParameterString("email"));
        contactUsForm.setComments(context.getParameterString("comments"));

        String enquiryValue = context.getParameterString("enquiry");

        mLogger.createLogInfo("Enquiry value before truncation::" + enquiryValue);
        enquiryValue = enquiryValue.replaceAll("%0d|%0a", "");
        mLogger.createLogInfo("Enquiry value after  truncation::" + enquiryValue);

        String coordinates = context.getParameterString(EXTERNAL_PARAM_COORDINATES, "");
        mLogger.createLogInfo("coordinates<--" + coordinates + "-->");

        Pattern p = Pattern.compile(EXTERNAL_PARAM_COORDINATES_REGEX);
        Matcher m = p.matcher(coordinates);

        String urlred = context.getParameterString("URLProfileSaveSuccess");
        String toEmail = "";
        String enquiryValueFromFile = "";
        // Read the email and market category values from the DCR file.
        if (m.matches()) {
            String values[] = null;
            if (coordinates != null) {
                values = coordinates.split("-");
                if (values.length > 1) {
                    String outer = values[0];
                    String inner = values[1];
                    Document contactUsFileDoc = null;
                    String dcrRelPath = context.getParameterString(EXTERNAL_PARAM_DCR_PATH, "");
                    mLogger.createLogDebug("DCR Path: " + dcrRelPath);
                    String dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrRelPath;
                    if (StringUtils.isNotEmpty(dcrFullPath)) {
                        File dcrFile = new File(dcrFullPath);
                        if (dcrFile.exists()) {
                            contactUsFileDoc = Dom4jUtils.newDocument(dcrFile);
                            Object emailobj = contactUsFileDoc.selectSingleNode("//contact_us/Links[" + outer + "]/Links[" + inner + "]/EmailId");
                            if (emailobj != null) {
                                toEmail = ((Element) emailobj).getText();
                            }
                            Object enquiryobj = contactUsFileDoc.selectSingleNode("//contact_us/Links[" + outer + "]/MarketCategory");
                            if (enquiryobj != null) {
                                enquiryValueFromFile = ((Element) enquiryobj).getText();
                            }
                        }
                    }
                }
            }
        }

        if (validateInputValue(toEmail, enquiryValue, enquiryValueFromFile)) {

            contactUsForm.setEnquiry(enquiryValue);
            contactUsForm.setCountry("Singapore");
            this.dataManager = new DataManagerImplCommon(context);
            dataManager.saveContactInformation(contactUsForm);

            String fname = context.getParameterString("firstname");
            String lname = context.getParameterString("lastname");
            String cnumber = context.getParameterString("contactnumber");
            String fromEmail = context.getParameterString("email");
            String comments = context.getParameterString("comments");

            mLogger.createLogDebug("Comments:" + comments);
            String country = context.getSite().getName();

            String subjectStr = context.getParameterString("subject", "");
            String subject = subjectStr + enquiryValue;
            mLogger.createLogDebug("Subject:" + subject);
            mLogger.createLogDebug("ContactUs Form Vals :: " + fname + "-" + lname + "-" + cnumber + "-" + fromEmail + "-" + comments + "-" + enquiryValue + ":Email sent to-" + toEmail);

            try {
                String[] recips = {toEmail};

                String msg = "<table border=2><tr><td>First Name : </td><td>" + fname + "</td></tr><tr><td>Last Name : </td><td>" + lname + "</td></tr><tr><td>Contact Number : </td><td>" + cnumber + "</td></tr><tr><td>Email ID : </td><td>" + fromEmail + "</td></tr><tr><td>Comments : </td><td>" + comments + "</td></tr><tr><td>Enquiry : </td><td>" + enquiryValue + "</td></tr><tr><td>Country : </td><td>" + country + "</td></tr></table>";
                mLogger.createLogInfo("FinalMsg:" + msg);

                postMail(recips, subject, comments, fromEmail, fname, lname, cnumber, enquiryValue, context);

            } catch (Exception e) {
                mLogger.createLogWarn("Contact Us Form Sending Email Errors", e);
            }
        }
        return new URLRedirectForwardAction(context, urlred);
    }

    private void postMail(String recipients[], String subject, String comments, String from, String fname, String lname, String cnumber, String enquiry, RequestContext context) throws MessagingException, UnsupportedEncodingException {
        boolean debug = false;
        String propertyFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + "resources/properties/EmailServer.properties";
        Properties emailProperties = new Properties();
        try {
            emailProperties.load(new FileInputStream(propertyFullPath));
        } catch (IOException e) {
            mLogger.createLogWarn("sendEmail reading properties from:" + propertyFullPath + " error!", e);
        }
        Properties props = new Properties();
        String mailserver = emailProperties.getProperty("host");
        props.put("mail.smtp.host", mailserver);
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);
        Message msg = (Message) new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.addHeader("MyHeaderName", "myHeaderValue");
        msg.setSubject(MimeUtility.encodeText(subject, "utf-8", "B"));

        String message = "<table border=2><tr><td>First Name : </td><td>" + fname + "</td></tr><tr><td>Last Name : </td><td>" + lname + "</td></tr><tr><td>Contact Number : </td><td>" + cnumber + "</td></tr><tr><td>Email ID : </td><td>" + from + "</td></tr><tr><td>Comments : </td><td>" + comments + "</td></tr><tr><td>Enquiry : </td><td>" + enquiry + "</td></tr></table>";

        msg.setContent(message, "text/html;charset=UTF-8");
        Transport.send(msg);

    }

    private boolean validateInputValue(String toEmailValue, String enquiryValue, String enquiryValueFromDCRFile) {

        mLogger.createLogInfo("Checking for email");

        boolean isEmailValid = false;
        boolean isEnquiryValid = false;
        Pattern emailPattern = Pattern.compile(EXTERNAL_PARAM_EMAILID_REGEX);
        Matcher m = emailPattern.matcher(toEmailValue);

        if (m.matches()) {
            mLogger.createLogDebug("Email value has matched with the regex.");
            isEmailValid = true;
        }

        if (isEmailValid) {
            mLogger.createLogDebug("Checking for enquiry");
            if (enquiryValue.equalsIgnoreCase(enquiryValueFromDCRFile)) {
                mLogger.createLogDebug("Enquiry value from form has matched with the value on DCR file. Hence, sending email.");
                isEnquiryValid = true;
            }
        }
        return (isEmailValid && isEnquiryValid);
    }
}
