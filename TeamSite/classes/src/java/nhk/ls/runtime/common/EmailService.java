/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.common;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.interwoven.livesite.component.foundation.util.DhDom4jUtils;
import com.interwoven.livesite.component.foundation.util.DocumentUtils;
import com.interwoven.livesite.component.foundation.util.Email;
import com.interwoven.livesite.component.foundation.util.FileSystemUtils;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * Provides an external method that generates and sends an email when
 * a page containing the component is posted to.
 *
 * @author Dave Raring
 */
public class EmailService {

    /** The logger for this instance. */
    private final Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.common.EmailService"));
    // URL Redirect Parameters set in component Datums
    /** URL Redirect Parameter: Success URL */
    private static final String PARAM_URL_FORM_SUBMIT_SUCCESS = "Success URL";
    /** URL Redirect Parameter: Error URL */
    private static final String PARAM_URL_FORM_SUBMIT_ERROR = "Error URL";
    // Component Datum Parameters
    /** Component Datum Parameter: Subject Field Name */
    private static final String PARAM_SUBJECT_FIELD = "Subject Field Name";
    /** Component Datum Parameter: Sender Field Name */
    private static final String PARAM_SENDER_FIELD = "Sender Field Name";
    /** Component Datum Parameter: Default Mail Recipient */
    private static final String PARAM_DEFAULT_RECIPIENT = "Default Mail Recipient";
    /** Component Datum Parameter: Debug Logging */
    private static final String PARAM_SMTP_DEBUG_LOGGING = "Debug Logging";
    /** Component Datum Parameter: SMTP Server Address */
    private static final String PARAM_SMTP_HOST_URL = "SMTP Server Address";
    /** Component Datum Parameter: SMTP Username */
    private static final String PARAM_SMTP_USER = "SMTP Username";
    /** Component Datum Parameter: SMTP Password */
    private static final String PARAM_SMTP_PASSWORD = "SMTP Password";
    /** Component Datum Parameter: SMTP Port */
    private static final String PARAM_SMTP_PORT = "SMTP Port";
    /** Component Datum Parameter: Mime Type */
    private static final String PARAM_MIME_TYPE = "Mime Type";
    /** Component Datum Parameter: Email Body Transform XSL (Optional) */
    private static final String PARAM_OPTIONAL_EMAIL_TRANSFORM_XSL = "Email Body Transform XSL (Optional)";
    /** Component Datum Parameter: EmailFormFieldPrefix */
    private static final String PARAM_EMAIL_FORM_FIELD_PREFIX = "EmailFormFieldPrefix";
    /** Component Datum Parameter: form-builder-field-names-list */
    private static final String PARAM_FORM_BUILDER_FIELD_NAMES_LIST = "form-builder-field-names-list";
    // Messages for logging. These could be moved into Datums at some point later
    // in case they were to be exposed in the Appearance XSL
    /** Logging message: Error processing email form */
    private static final String MESSAGE_PROCESSING_ERROR = "Error processing email form";
    /** Logging message: Error sending email */
    private static final String MESSAGE_SEND_ERROR = "Error sending email";
    /** Logging message: Email sent successfully */
    private static final String MESSAGE_SEND_SUCCESS = "Email sent successfully";
    // Email DOM Element and Attribute names
    /** Email DOM document element. */
    private static final String DOCUMENT_ROOT_EMAIL_FORM = "<EmailForm />";
    /** Email DOM Element: Field */
    private static final String ELEMENT_FIELD = "Field";
    /** Email DOM Attribute: Name */
    private static final String ATTRIBUTE_NAME = "Name";
    /** Email DOM Attribute: Value */
    private static final String ATTRIBUTE_VALUE = "Value";
    // Service Response DOM Element names
    /** An XML Element name. */
    private static final String DOCUMENT_ROOT_SERVICE_RESPONSE = "<ServiceResponse />";
    /** An XML Element name. */
    private static final String ELEMENT_ERROR = "Error";
    /** An XML Element name. */
    private static final String ELEMENT_ERRORS = "Errors";
    /** An XML Element name. */
    private static final String ELEMENT_SUCCESS = "Success";
    /** An XML Element name. */
    private static final String ELEMENT_MESSAGE = "Message";
    /** An XML Element name. */
    private static final String ELEMENT_TRANSPORT = "Transport";
    /** An XML Element name. */
    private static final String ELEMENT_EMAIL = "Email";
    /** An XML Element name. */
    private static final String ELEMENT_SENDER = "Sender";
    /** An XML Element name. */
    private static final String ELEMENT_RECIPIENTS = "Recipients";
    /** An XML Element name. */
    private static final String ELEMENT_BODY = "Body";
    /** An XML Element name. */
    private static final String ELEMENT_SUBJECT = "Subject";
    /** An XML Element name. */
    private static final String ELEMENT_USERNAME = "Username";
    /** An XML Element name. */
    private static final String ELEMENT_DEBUG = "Debug";
    /** An XML Element name. */
    private static final String ELEMENT_HOST = "Host";
    /** An XML Element name. */
    private static final String ELEMENT_PASSWORD = "Password";
    /** An XML Element name. */
    private static final String ELEMENT_PORT = "Port";
    /** An XML Element name. */
    private static final String ELEMENT_MIME_TYPE = "MimeType";
    /**
     * Indicates whether the process was successful or not
     */
    private boolean mSuccess = false;

    /**
     * External Method for processing an email form when the form is posted to the
     * page containing the Email Service component. Upon generation of the email,
     * a URL Redirect is set on the response to let the user know whether the
     * email send was a success or not. If the page containing the Email Service
     * component is rendered directly with a GET, nothing happens.
     *
     * @param context
     *          the current request context.
     * @return a Document, never <code>null</code>.
     */
    public Document showEmailServiceResponse(RequestContext context) {
        Document result = Dom4jUtils.newDocument(DOCUMENT_ROOT_SERVICE_RESPONSE);

        if (StringUtils.equalsIgnoreCase("post", context.getRequest().getMethod())) {
            result = processEmailForm(context);
            String redirect = null;

            if (getSuccess()) {
                redirect = context.getParameterString(PARAM_URL_FORM_SUBMIT_SUCCESS);
            } else {
                redirect = context.getParameterString(PARAM_URL_FORM_SUBMIT_ERROR);
            }
            try {
                // strip out the redirect substitution artifacts from the parameter.
                // avoid using JDK 1.5 specific replace APIs, so 1.4.x JVMs can handle.
                redirect = redirect.substring(11, redirect.length() - 1);
            } catch (Exception e) {
                // either no redirect or unexpected redirect string
                // continue with original redirect string (i.e. do nothing)
            }

            try {
                String site = "";

                /*
                 * Get the Site for the current context.
                 */
                if (null != context.getSite().getName()) {
                    site = context.getSite().getName();
                }

                /*
                 * Build the page URL based on the Site name and Page Key
                 */
                redirect = "/" + site + "/" + redirect + ".page";

                context.getResponse().sendRedirect(redirect);
            } catch (Exception e) {
                mLogger.createLogWarn("Error in showEmailServiceResponse::", e);
            }
        }

        return result;
    }

    /**
     * Generalized Method processing HTML form containing Email Form Fields. This
     * method could be used either from an External call or a Controller call.
     *
     * @param context
     *          the current request context.
     * @return a Document, never <code>null</code>.
     */
    private Document processEmailForm(RequestContext context) {
        /*
         * Create a Service Response Doc to return back to the component. Contents
         * include Success Status, Error Messages, Transport Props, and the Email
         * Fields from the submitted email form, and the Email pay load.
         */
        Document serviceResponse = Dom4jUtils.newDocument(DOCUMENT_ROOT_SERVICE_RESPONSE);
        Element successElem = DocumentHelper.createElement(ELEMENT_SUCCESS);
        serviceResponse.getRootElement().add(successElem);
        Element messageElem = DocumentHelper.createElement(ELEMENT_MESSAGE);
        serviceResponse.getRootElement().add(messageElem);
        Element errorsElem = DocumentHelper.createElement(ELEMENT_ERRORS);
        serviceResponse.getRootElement().add(errorsElem);
        Element transportElem = DocumentHelper.createElement(ELEMENT_TRANSPORT);
        serviceResponse.getRootElement().add(transportElem);
        Element emailElem = DocumentHelper.createElement(ELEMENT_EMAIL);
        serviceResponse.getRootElement().add(emailElem);

        /*
         * Datum Values From Email Service Component required for building the email
         * and response document
         */
        String subjectField = context.getParameterString(PARAM_SUBJECT_FIELD);
        String senderField = context.getParameterString(PARAM_SENDER_FIELD);
        String processingError = MESSAGE_PROCESSING_ERROR;
        String sendError = MESSAGE_SEND_ERROR;
        String successMessage = MESSAGE_SEND_SUCCESS;

        /*
         * Specific Fields Required for Email Object
         */
        String subject = context.getParameterString(subjectField, "");
        emailElem.addElement(ELEMENT_SUBJECT).setText(subject);
        String sender = context.getParameterString(senderField, "");
        emailElem.addElement(ELEMENT_SENDER).setText(sender);
        String recipients = context.getParameterString(PARAM_DEFAULT_RECIPIENT, "");
        emailElem.addElement(ELEMENT_RECIPIENTS).setText(recipients);

        /*
         * Construct the Body Text
         */
        Document bodyDoc = parseEmailForm(context);
        emailElem.add(bodyDoc.getRootElement());
        String body = transformEmailBody(context, bodyDoc, errorsElem);
        emailElem.addElement(ELEMENT_BODY).setText(body);

        if (StringUtils.isBlank(body)) {
            messageElem.setText(processingError);
            setSuccess(false);
        }

        /*
         * Build the Mail Transport Properties based on the Email Service Component
         * Datum values
         */
        Properties props = new Properties();
        props.put("mail.debug", context.getParameterString(PARAM_SMTP_DEBUG_LOGGING));
        transportElem.addElement(ELEMENT_DEBUG).setText(context.getParameterString(PARAM_SMTP_DEBUG_LOGGING, ""));
        props.put("mail.smtp.host", context.getParameterString(PARAM_SMTP_HOST_URL));
        transportElem.addElement(ELEMENT_HOST).setText(context.getParameterString(PARAM_SMTP_HOST_URL, ""));
        props.put("mail.smtp.user", context.getParameterString(PARAM_SMTP_USER));
        transportElem.addElement(ELEMENT_USERNAME).setText(context.getParameterString(PARAM_SMTP_USER, ""));
        props.put("mail.smtp.password", context.getParameterString(PARAM_SMTP_PASSWORD));
        transportElem.addElement(ELEMENT_PASSWORD).setText(Boolean.toString(StringUtils.isNotBlank(context.getParameterString(PARAM_SMTP_PASSWORD, ""))));
        props.put("mail.smtp.port", context.getParameterString(PARAM_SMTP_PORT));
        transportElem.addElement(ELEMENT_PORT).setText(context.getParameterString(PARAM_SMTP_PORT, ""));

        /*
         * Send the email if the form was processed successfully
         */
        if (StringUtils.isNotBlank(body)) {
            try {
                Email email = new Email();
                email.setSubject(subject);
                email.setRecipients(recipients);
                email.setSender(sender);
                email.setBody(body);
                email.setMimeType(context.getParameterString(PARAM_MIME_TYPE, ""));
                transportElem.addElement(ELEMENT_MIME_TYPE).setText(email.getMimeType());

                mLogger.createLogInfo("Sending email:");
                mLogger.createLogDebug(email.getRecipients() + "\n" + email.getSubject() + "\n" + email.getBody());

                email.send(props);
                messageElem.setText(successMessage);
                setSuccess(true);
            } catch (Exception e) {
                mLogger.createLogDebug("Error sending email::", e);
                messageElem.setText(sendError);
                setSuccess(false);
            }
        }
        successElem.setText(Boolean.toString(getSuccess()));
        mLogger.createLogDebug(DhDom4jUtils.prettyPrintXml(serviceResponse));
        return serviceResponse;
    }

    /**
     * Returns the success status of the last email form process
     *
     * @return true if successful.
     */
    private boolean getSuccess() {
        return mSuccess;
    }

    /**
     * Sets the success status of the last email form process
     *
     * @param success
     *          the success flag.
     */
    private void setSuccess(boolean success) {
        mSuccess = success;
    }

    /**
     * Parses the Submitted Email Form by Looping through the Parameters in
     * Context and building an XML document of Name / Value pairs. This XML
     * document is later transformed into the Email Body String.
     *
     * @param context
     *          the current request context.
     * @return a Document, never <code>null</code>.
     */
    private Document parseEmailForm(RequestContext context) {
        Document body = Dom4jUtils.newDocument(DOCUMENT_ROOT_EMAIL_FORM);
        Hashtable params = context.getParameters();
        /*
         * In order to distinguish which fields to consider as Email Form Fields,
         * try to grab a prefix specification from the Email Service Component Datum
         */
        String fieldPrefix = context.getParameterString(PARAM_EMAIL_FORM_FIELD_PREFIX);

        /*
         * Another possible way to distinguish email form fields is to look for a
         * submitted list variable containing the field names
         */
        Map fieldNames = new HashMap();
        try {
            String names = context.getParameterString(PARAM_FORM_BUILDER_FIELD_NAMES_LIST);
            String[] nArray = names.split(",");
            for (int i = 0; i <= nArray.length - 1; i++) {
                if (StringUtils.isNotBlank(nArray[i])) {
                    fieldNames.put(nArray[i], nArray[i]);
                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Field names list not provided for email form processing::", e);
        }

        /*
         * Process the Submitted HTML Form by creating an XML Document containing
         * the Fields as Name / Value pairs
         */
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            Element field = DocumentHelper.createElement(ELEMENT_FIELD);
            String fieldName = (String) i.next();
            /*
             * Only process the field if the Prefix matches or the field is in the
             * list of submitted fields
             */
            if ((null != fieldPrefix && fieldName.startsWith(fieldPrefix)) || (null != fieldNames && fieldNames.containsKey(fieldName))) {
                field.addAttribute(ATTRIBUTE_NAME, fieldName);
                try {
                    String al = (String) params.get(fieldName);
                    field.addAttribute(ATTRIBUTE_VALUE, al);
                    body.getRootElement().add(field);
                } catch (Exception e) {
                    mLogger.createLogWarn("Error processing email form field::", e);
                }
            }
        }

        return body;
    }

    /**
     * Transforms the Email XML content into an Email Body String. If an XSL
     * transform file has been specified, the Email XML doc is transformed
     * accordingly. Otherwise, an email body containing name / value pairs is
     * generated.
     *
     * @param context
     *          the current request context.
     * @param bodyDoc
     *          the body document to be transformed.
     * @param errors
     *          the error elements.
     * @return the email body, never <code>null</code>.
     */
    private String transformEmailBody(RequestContext context, Document bodyDoc, Element errors) {
        String transformXsl = null;
        String ft = context.getParameterString(PARAM_OPTIONAL_EMAIL_TRANSFORM_XSL);
        String body = "";
        /*
         * If an XSL transform was specified, transform the body with it
         */
        if (StringUtils.isNotBlank(ft)) {
            try {
                transformXsl = FileSystemUtils.getFileString(context, ft);
                body = DocumentUtils.transformDocument(context, bodyDoc, transformXsl);
            } catch (RuntimeException e) {
                mLogger.createLogDebug("Error in transformEmailBody", e);
                errors.addElement(ELEMENT_ERROR).setText(e.toString());
            } catch (TransformerConfigurationException e1) {
                mLogger.createLogDebug("Error in transformEmailBody", e1);
                errors.addElement(ELEMENT_ERROR).setText(e1.toString());
            } catch (TransformerException e) {
                mLogger.createLogDebug("Error in transformEmailBody", e);
                errors.addElement(ELEMENT_ERROR).setText(e.toString());
            }
        } /*
         * Otherwise, create a body that consists of name / value pairs from the
         * submitted form
         */ else {
            Iterator i = bodyDoc.getRootElement().elementIterator();
            while (i.hasNext()) {
                Element e = (Element) i.next();
                body = body + e.attributeValue(ATTRIBUTE_NAME) + ": " + e.attributeValue(ATTRIBUTE_VALUE) + "\n";
            }
        }

        return body;
    }
}
