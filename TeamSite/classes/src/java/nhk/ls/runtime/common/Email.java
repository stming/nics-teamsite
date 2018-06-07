/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 */
package nhk.ls.runtime.common;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
//wxx
import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

/**
 * Email class for email feedback component.
 *
 * @author Dave Raring
 * @author Wang Xiaoxi
 */
public class Email {

    /** Internal logger. */
    private final Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.common.Email"));
    /** The email address of the sender. */
    private String mSender;
    /** Email addresses of the recipients. */
    private String[] mRecipients;
    /** The subject of the email. */
    private String mSubject;
    /** The message body. */
    private String mBody;
    /** The MIME type of the message, or <code>null</code> for the default. */
    private String mMimeType;

    /**
     * Sends an email, using the properties file to create a javax.mail.session
     *
     * @param props email configuration properties.
     * @throws MessagingException if the sender address is invalid or the message could not be sent.
     * @throws AddressException if a recipient address is invalid.
     */
    public void send(Properties props) throws AddressException, MessagingException {
        Session session = null;
        Authenticator auth = null;
        if (StringUtils.isNotBlank(props.getProperty("mail.smtp.password"))) {
            props.put("mail.smtp.auth", "true");
            auth = new MyMailAuthenticator(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
            session = Session.getInstance(props, auth);
        }
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(getSender()));
        String[] mailToArray = getRecipients();
        for (int i = 0; i < mailToArray.length; ++i) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailToArray[i]));
        }
        message.setSubject(getSubject());
        if (null != getMimeType()) {
            message.setContent(getBody(), getMimeType());
        } else {
            message.setText(getBody());
        }
        mLogger.createLogInfo("Initiating email transport");
        mLogger.createLogDebug("message==null?" + (message == null));
        Transport transport = session.getTransport("smtp");
        transport.connect(props.getProperty("mail.smtp.host"), props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
        message.saveChanges();
        Transport.send(message);
        transport.close();
        mLogger.createLogInfo("Email successfully sent.");

    }

    /**
     * Returns the body.
     *
     * @return the email body, or <code>null</code> if none has been set.
     */
    public String getBody() {
        return mBody;
    }

    /**
     * Sets the body.
     *
     * @param body
     *          the body.
     */
    public void setBody(String body) {
        mBody = body;
    }

    /**
     * Returns the set of recipient email addresses.
     *
     * @return the email addresses, or <code>null</code>.
     */
    public String[] getRecipients() {
        return mRecipients;
    }

    /**
     * Sets the email addresses of recipients.
     *
     * @param recipients
     *          an array of email addresses.
     */
    public void setRecipients(String[] recipients) {
        mRecipients = recipients;
    }

    /**
     * Given a semi-colon or comma-separated list of recipients, sets the email
     *
     * @param recipients
     *          a single demilited String, or <code>null</code> for no
     *          recipients.
     */
    public void setRecipients(String recipients) {
        String[] recipientsArray;
        if (null != recipients) {
            String splitChar = recipients.indexOf(';') > 0 ? ";" : ",";
            recipientsArray = recipients.split(splitChar);
        } else {
            recipientsArray = new String[0];
        }
        setRecipients(recipientsArray);
    }

    /**
     * Returns the email address of the sender of this email.
     *
     * @return the sender, or <code>null</code> if none has been set.
     */
    public String getSender() {
        return mSender;
    }

    /**
     * Sets the email address of the sender.
     *
     * @param sender
     *          the sender email address.
     */
    public void setSender(String sender) {
        mSender = sender;
    }

    /**
     * Returns the subject of the email.
     *
     * @return the subject, or <code>null</code> if none has been set.
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * Sets the subject of this email.
     *
     * @param subject
     *          the email subject.
     */
    public void setSubject(String subject) {
        mSubject = subject;
    }

    /**
     * Returns the MIME type of the email body.
     *
     * @return the mime type, or <code>null</code> if none has been set.
     */
    public String getMimeType() {
        return mMimeType;
    }

    /**
     * Sets the MIME type of the email body.
     *
     * @param mimeType
     *          the new MIME type, or <code>null</code> for the default type.
     */
    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }
}

class MyMailAuthenticator extends Authenticator {

    String user;
    String pw;

    public MyMailAuthenticator(String username, String password) {
        super();
        this.user = username;
        this.pw = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pw);
    }
}
