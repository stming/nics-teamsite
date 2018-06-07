/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import nhk.ls.runtime.dao.SubscriptionData;
import nhk.ls.runtime.dao.DataManagerForSubscription;
import nhk.ls.runtime.dao.DataManagerForSubscriptionImpl;
import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.common.Email;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.common.web.URLRedirectForwardAction;
import java.util.List;
import java.util.Iterator;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Properties;
import java.io.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;

import java.security.*;
import java.math.*;

/**
 *
 * @author wxiaoxi
 */
public class SubscriptionForm {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.SubscriptionForm"));

//    public static void main (String [] input) throws Exception
//    {
//        String propertyFullPath = null;
//        String propertiesFileName = "en_SG";
//        propertiesFileName = (propertiesFileName!=null && !propertiesFileName.equals(""))?"_"+propertiesFileName:"";
//        propertiesFileName = "EmailServer" + propertiesFileName + ".properties";
//
//        //propertyFullPath = "resources/properties/EmailServer.properties";
//        propertyFullPath = "resources/properties/"+propertiesFileName;
//    }

    public org.dom4j.Document SubscriptionEamilCheck(RequestContext context) {

        mLogger.createLogInfo("********** SubscriptionEamilCheck ********** ");

        String email = context.getParameterString("D002", "");
        String resultSatus = "EmailCheckFailed";

        String redirectLink = "";
        redirectLink = context.getParameterString("RedirectLinkFlag", "");

        if (redirectLink != null && !redirectLink.equals("")) {
            resultSatus = "EmailRedirectStatus" + redirectLink;

        } else if (email != null && !email.equals("")) {
            DataManagerForSubscription dataManagerForSubscription = new DataManagerForSubscriptionImpl(context);
            SubscriptionData sp = dataManagerForSubscription.retrieveSubscriptionByEmail(email);

            if (sp == null) {
                resultSatus = "EmailCheckSuccessful";
            } else if (sp.getStatus().equals("A")) {
                resultSatus = "EmailCheckStatusA";
            } else if (sp.getStatus().equals("I")) {
                resultSatus = "EmailCheckStatusI";
            } else if (sp.getStatus().equals("P")) {
                resultSatus = "EmailCheckStatusP";

                String ResendLinkFlag = context.getParameterString("ResendLinkFlag", "");
                if (ResendLinkFlag != null && ResendLinkFlag.equals("Y")) {
                    sendEmail(email, "A", sp.getConfirmLink(), context);
                    resultSatus = "EmailCheckFailed";
                    email = "";
                }
            }
        }
        String isAsia = "No";
        if (context.getRequest().getRequestURL().toString().toUpperCase().indexOf("ASIA") > 0) {
            isAsia = "Yes";
        }

        Document doc = Dom4jUtils.newDocument();
        mLogger.createLogDebug("email checked");
        Element resultElement = doc.addElement("Email");
        resultElement.addElement("Mode").addText(resultSatus);
        resultElement.addElement("Address").addText(email);
        resultElement.addElement("RedirectLink").addText(redirectLink);
        resultElement.addElement("NikonAsia").addText(isAsia);
        return doc;
    }

    public org.dom4j.Document UnsubscriptionEamilCheck(RequestContext context) {
        mLogger.createLogInfo("********** SubscriptionEamilCheck ********** ");

        String email = context.getParameterString("D002", "");
        String resultSatus = "EmailCheckFailed";

        String redirectLink = "";
        redirectLink = context.getParameterString("RedirectLinkFlag", "");

        if (redirectLink != null && !redirectLink.equals("")) {
            resultSatus = "EmailRedirectStatus" + redirectLink;

        }

        String isAsia = "No";
        if (context.getRequest().getRequestURL().toString().toUpperCase().indexOf("ASIA") > 0) {
            isAsia = "Yes";
        }

        Document doc = Dom4jUtils.newDocument();
        mLogger.createLogDebug("email checked");
        Element resultElement = doc.addElement("Email");
        resultElement.addElement("Mode").addText(resultSatus);
        resultElement.addElement("Address").addText(email);
        resultElement.addElement("RedirectLink").addText(redirectLink);
        resultElement.addElement("NikonAsia").addText(isAsia);
        return doc;
    }

    public ForwardAction SubscriptionSubmitStart(RequestContext context) throws IOException {
        mLogger.createLogInfo("********** Before reading from Subscription Form  *************");
        //read from Subscription Form
        String email = context.getParameterString("D002", "myemail@myserver");
        String lastName = context.getParameterString("D003", "myLastName");
        String firstName = context.getParameterString("D004", "myFirstName");
        String contactNumber = context.getParameterString("D005", "12345678");
        String salutation = context.getParameterString("D017", "Mr");

        String nikonProdoctsOption = context.getParameterString("D0092", "No");
        String nikonProdocts = context.getParameterString("D006", "");

        String agreeOption = context.getParameterString("D0082", "false");

        String countryResidence = context.getParameterString("D010", "mycountryResidence");
        String countrySubscription = context.getParameterString("D011", "mycountrySubscription");

        String status = context.getParameterString("D012", "P");
        String dateSubmitted = context.getParameterString("D013", "");

        String confirmLink = MD5Digest(email);

        Hashtable ht = new Hashtable();
        ht.put("email", email);
        ht.put("lastName", lastName);
        ht.put("firstName", firstName);
        ht.put("contactNumber", contactNumber);
        ht.put("salutation", salutation);
        ht.put("nikonProdocts", nikonProdocts);
        ht.put("countryResidence", countryResidence);
        ht.put("countrySubscription", countrySubscription);
        ht.put("status", "P");
        ht.put("dateSubmitted", dateSubmitted);
        ht.put("confirmLink", confirmLink);


        mLogger.createLogDebug("********** After reading from Subscription Form *************");
        mLogger.createLogDebug("email" + email);
        mLogger.createLogDebug("lastName:" + lastName);
        mLogger.createLogDebug("firstName:" + firstName);
        mLogger.createLogDebug("contactNumber:" + contactNumber);
        mLogger.createLogDebug("nikonProdoctsOption" + nikonProdoctsOption);
        mLogger.createLogDebug("nikonProdocts:" + nikonProdocts);
        mLogger.createLogDebug("agreeOption" + agreeOption);
        mLogger.createLogDebug("countryResidence:" + countryResidence);
        mLogger.createLogDebug("countrySubscription:" + countrySubscription);
        mLogger.createLogDebug("status:" + status);
        mLogger.createLogDebug("dateSubmitted:" + dateSubmitted);
        mLogger.createLogDebug("confirmLink:" + confirmLink);



        mLogger.createLogDebug("********** Before reading from DB ********** ");
        DataManagerForSubscription dataManagerForSubscription = new DataManagerForSubscriptionImpl(context);
        SubscriptionData sp = dataManagerForSubscription.retrieveSubscriptionByEmail(email);
        String resultString1 = "";
        String resultString2 = "";
        if (sp == null) {
            mLogger.createLogDebug("SubscriptionData new email account");
            String sqlQuery1 = insertStatementConstruct(ht);
            resultString1 = dataManagerForSubscription.ExecuteQuery(sqlQuery1);
        } else {
            mLogger.createLogDebug("SubscriptionData existing email account");
            String sqlQuery2 = updateStatementConstruct(ht);
            resultString2 = dataManagerForSubscription.ExecuteQuery(sqlQuery2);

            mLogger.createLogDebug("email" + sp.getEmail());
            mLogger.createLogDebug("lastName:" + sp.getLastName());
            mLogger.createLogDebug("firstName:" + sp.getFirstName());
            mLogger.createLogDebug("contactNumber:" + sp.getContactNumber());
            mLogger.createLogDebug("status:" + sp.getStatus());
            mLogger.createLogDebug("dateSubmitted:" + sp.getDateSubmitted());
            mLogger.createLogDebug("confirmLink:" + sp.getConfirmLink());
        }
        mLogger.createLogDebug("********** After reading from DB ********** ");

        mLogger.createLogDebug("**********  Before sending email ********** ");
        String recipientsEmailAddress = email;
        sendEmail(recipientsEmailAddress, "A", confirmLink, context);
        mLogger.createLogDebug("**********  After sending email ********** ");
        mLogger.createLogDebug("**********  Before generating ouput page ********** ");

        mLogger.createLogDebug("**********  After generating ouput page ********** ");

        String requestUrl = context.getRequest().getRequestURL().toString();
        int endPoint = requestUrl.length();
        if (requestUrl.indexOf("Subscribe.page") > -1) {
            endPoint = requestUrl.indexOf("Subscribe.page");
        } else if (requestUrl.indexOf("Unsubscribe.page") > -1) {
            endPoint = requestUrl.indexOf("Unsubscribe.page");
        }
        requestUrl = requestUrl.substring(0, endPoint);

        String pageName = "Subscribe.page";

        StringBuilder sb = new StringBuilder();
        sb.append(requestUrl);
        sb.append(pageName);
        sb.append("?");
        sb.append("D002=");
        sb.append(email);
        sb.append("&");
        sb.append("RedirectLinkFlag=Sub");

        return new URLRedirectForwardAction(context, sb.toString());

    }

    public ForwardAction UnsubscriptionSubmitStart(RequestContext context) throws IOException {
        mLogger.createLogInfo("********** Before reading from Unsubscription Form  *************");
        //read from Subscription Form
        String email = context.getParameterString("D002", "myemail@myserver");

        String agreeOption = context.getParameterString("D0082", "false");

        String countryResidence = context.getParameterString("D010", "mycountryResidence");
        String countrySubscription = context.getParameterString("D011", "mycountrySubscription");

        String status = context.getParameterString("D012", "A");
        String dateSubmitted = context.getParameterString("D013", "");

        String confirmLink = MD5Digest(email);

        Hashtable ht = new Hashtable();
        ht.put("email", email);
        ht.put("countryResidence", countryResidence);
        ht.put("countrySubscription", countrySubscription);
        ht.put("status", status);
        ht.put("dateSubmitted", dateSubmitted);
        ht.put("confirmLink", confirmLink);


        mLogger.createLogDebug("********** After reading from Unsubscription Form *************");
        mLogger.createLogDebug("email" + email);
        mLogger.createLogDebug("agreeOption" + agreeOption);
        mLogger.createLogDebug("countryResidence:" + countryResidence);
        mLogger.createLogDebug("countrySubscription:" + countrySubscription);
        mLogger.createLogDebug("status:" + status);
        mLogger.createLogDebug("dateSubmitted:" + dateSubmitted);
        mLogger.createLogDebug("confirmLink:" + confirmLink);



        mLogger.createLogDebug("********** Before reading from DB ********** ");
        DataManagerForSubscription dataManagerForSubscription = new DataManagerForSubscriptionImpl(context);
        SubscriptionData sp = dataManagerForSubscription.retrieveSubscriptionByEmail(email);
        String resultString1 = "";
        String resultString2 = "";
        if (sp == null) {
            mLogger.createLogDebug("UnsubscriptionData new email account");
        } else {
            mLogger.createLogDebug("UnsubscriptionData existing email account");

            ht.put("email", email);
            ht.put("lastName", sp.getLastName());
            ht.put("firstName", sp.getLastName());
            ht.put("contactNumber", sp.getContactNumber());
            ht.put("salutation", sp.getSalutation());
            ht.put("nikonProdocts", sp.getNikonProdocts());
            ht.put("countryResidence", sp.getCountryResidence());
            ht.put("countrySubscription", sp.getCountrySubscription());
            ht.put("status", sp.getStatus());
            ht.put("dateSubmitted", "");
            ht.put("confirmLink", confirmLink);

            String sqlQuery2 = updateStatementConstruct(ht);
            resultString2 = dataManagerForSubscription.ExecuteQuery(sqlQuery2);


            mLogger.createLogDebug("email" + sp.getEmail());
            mLogger.createLogDebug("lastName:" + sp.getLastName());
            mLogger.createLogDebug("firstName:" + sp.getFirstName());
            mLogger.createLogDebug("contactNumber:" + sp.getContactNumber());
            mLogger.createLogDebug("status:" + sp.getStatus());
            mLogger.createLogDebug("dateSubmitted:" + sp.getDateSubmitted());
            mLogger.createLogDebug("confirmLink:" + sp.getConfirmLink());
        }
        mLogger.createLogDebug("********** After reading from DB ********** ");

        mLogger.createLogDebug("**********  Before sending email ********** ");
        String recipientsEmailAddress = email;
        sendEmail(recipientsEmailAddress, "I", confirmLink, context);
        mLogger.createLogDebug("**********  Aftersending email ********** ");
        mLogger.createLogDebug("**********  Before generating ouput page ********** ");
        mLogger.createLogDebug("**********  After generating ouput page ********** ");
        // redirect
        String requestUrl = context.getRequest().getRequestURL().toString();
        int endPoint = requestUrl.length();
        if (requestUrl.indexOf("Subscribe.page") > -1) {
            endPoint = requestUrl.indexOf("Subscribe.page");
        } else if (requestUrl.indexOf("Unsubscribe.page") > -1) {
            endPoint = requestUrl.indexOf("Unsubscribe.page");
        }
        requestUrl = requestUrl.substring(0, endPoint);
        String pageName = "Unsubscribe.page";

        StringBuilder sb = new StringBuilder();
        sb.append(requestUrl);
        sb.append(pageName);
        sb.append("?");
        sb.append("D002=");
        sb.append(email);
        sb.append("&");
        sb.append("RedirectLinkFlag=Unsub");

        return new URLRedirectForwardAction(context, sb.toString());

    }

    public String insertStatementConstruct(Hashtable ht) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into Subscription (EMAIL,CONTACT_NUMBER, SALUTATION, LAST_NAME, FIRST_NAME,NIKON_PRODUCTS, COUNTRY_RESIDENCE, COUNTRY_SUBSCRIPTION, STATUS, DATE_SUBMITTED, CONFIRM_LINK) values (");
        sb.append("'" + ht.get("email") + "', ");
        sb.append("'" + ht.get("contactNumber") + "', ");
        sb.append("'" + ht.get("salutation") + "', ");
        sb.append("'" + ht.get("lastName") + "', ");
        sb.append("'" + ht.get("firstName") + "', ");
        sb.append("'" + ht.get("nikonProdocts") + "', ");
        sb.append("'" + ht.get("countryResidence") + "', ");
        sb.append("'" + ht.get("countrySubscription") + "', ");
        sb.append("'" + ht.get("status") + "', ");
        sb.append("getDate(), ");
        sb.append("'" + ht.get("confirmLink") + "'");
        sb.append(")");

        String sqlQuery1 = sb.toString();
        return sqlQuery1;
    }

    public String updateStatementConstruct(Hashtable ht) {
        String statusString = ht.get("status") != null ? (String) ht.get("status") : "";
        StringBuilder sb = new StringBuilder();
        sb.append("update Subscription set ");
        sb.append("EMAIL = '" + ht.get("email") + "', ");
        sb.append("CONTACT_NUMBER = '" + ht.get("contactNumber") + "', ");
        sb.append("SALUTATION = '" + ht.get("salutation") + "', ");
        sb.append("LAST_NAME = '" + ht.get("lastName") + "', ");
        sb.append("FIRST_NAME = '" + ht.get("firstName") + "', ");
        sb.append("NIKON_PRODUCTS = '" + ht.get("nikonProdocts") + "', ");
        sb.append("COUNTRY_RESIDENCE = '" + ht.get("countryResidence") + "', ");
        sb.append("COUNTRY_SUBSCRIPTION = '" + ht.get("countrySubscription") + "', ");
        if (statusString != null && !statusString.equals("")) {
            sb.append("STATUS ='" + statusString + "', ");
        }
        sb.append("DATE_SUBMITTED = getDate(), ");
        sb.append("CONFIRM_LINK = '" + ht.get("confirmLink") + "' ");
        sb.append(" where EMAIL = '" + ht.get("email") + "'");

        String sqlQuery1 = sb.toString();
        return sqlQuery1;
    }

    public String MD5Digest(String s) {
        try {

            Random randomGenerator = new Random();
            int randomInt = randomGenerator.nextInt(100000);
            s = s + randomInt;
            MessageDigest m = MessageDigest.getInstance("MD5");
            s = String.format("%1$032X", new BigInteger(1, m.digest(s.getBytes())));
        } catch (Exception e) {
            mLogger.createLogWarn("Error in MD5Digest::", e);
        } finally {
            return s;
        }
    }

    public void sendEmail(String recipientsEmailAddress, String Status, String confirmLink, RequestContext context) {

        String propertyFullPath = null;
        String propertiesFileName = context.getSite().getName();
        propertiesFileName = (propertiesFileName!=null && !propertiesFileName.equals(""))?"_"+propertiesFileName:"";
        propertiesFileName = "EmailServer" + propertiesFileName + ".properties";

        //propertyFullPath = "resources/properties/EmailServer.properties";
        propertyFullPath = "resources/properties/"+propertiesFileName;
        propertyFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + propertyFullPath;
        mLogger.createLogDebug("sendEmail reading properties from:" + propertyFullPath);


        Properties emailProperties = new Properties();
        try {
            emailProperties.load(new FileInputStream(propertyFullPath));
        } catch (IOException e) {
            e.printStackTrace();
            mLogger.createLogDebug("sendEmail reading properties from:" + propertyFullPath + " error!");
        }

        mLogger.createLogDebug(emailProperties.getProperty("user"));
        mLogger.createLogDebug(emailProperties.getProperty("password"));
        mLogger.createLogDebug(emailProperties.getProperty("host"));
        mLogger.createLogDebug(emailProperties.getProperty("starttlsEnable"));
        mLogger.createLogDebug(emailProperties.getProperty("sender"));

        // hardcode
        Properties props = new Properties();
        props.setProperty("mail.smtp.user", emailProperties.getProperty("user"));
        props.setProperty("mail.smtp.password", emailProperties.getProperty("password"));
        props.setProperty("mail.smtp.host", emailProperties.getProperty("host"));
        props.setProperty("mail.smtp.starttls.enable", emailProperties.getProperty("starttlsEnable"));

        // hardcode
        Email em = new Email();
        em.setRecipients(recipientsEmailAddress);
        em.setSender(emailProperties.getProperty("sender"));

        String linkEamil = "";
        String pageName = "";
        if (Status.equalsIgnoreCase("A")) {
            em.setSubject("Please confirm your Nikon update subscription");
            linkEamil = "To confirm your Nikon update subscription via:";
            pageName = "Subscribe_Confirm.page";
        } else if (Status.equalsIgnoreCase("I")) {
            em.setSubject("Please confirm your Nikon update unsubscription");
            linkEamil = "To confirm your Nikon update unsubscription via:";
            pageName = "Unsubscribe_Confirm.page";
        }

        String requestUrl = context.getRequest().getRequestURL().toString();
        int endPoint = requestUrl.length();
        if (requestUrl.indexOf("Subscribe.page") > -1) {
            endPoint = requestUrl.indexOf("Subscribe.page");
        } else if (requestUrl.indexOf("Unsubscribe.page") > -1) {
            endPoint = requestUrl.indexOf("Unsubscribe.page");
        }
        requestUrl = requestUrl.substring(0, endPoint);

        StringBuilder sb = new StringBuilder();
        sb.append(requestUrl);
        sb.append(pageName);
        sb.append("?");
        sb.append("status=");
        sb.append(Status);
        sb.append("&");
        sb.append("confirmLink=");
        sb.append(confirmLink);

        em.setBody(linkEamil + sb.toString());

        try {
            em.send(props);
        } catch (Exception e) {
            mLogger.createLogDebug("Error in sendEmail::", e);
        }
    }

    public org.dom4j.Document SubscriptionConfirm(RequestContext context) {

        String queryString = context.getRequest().getQueryString();
        String confirmLink = context.getRequest().getParameter("confirmLink");
        String status = context.getRequest().getParameter("status");

        mLogger.createLogInfo("********** SubscriptionConfirm *************");
        mLogger.createLogInfo("confirmlink" + confirmLink + " " + status);

        Document doc = Dom4jUtils.newDocument();

        if (confirmLink != null && status != null && (status.equalsIgnoreCase("A") || status.equalsIgnoreCase("I"))) {
            Hashtable ht = new Hashtable();
            ht.put("status", status);
            ht.put("confirmLink", confirmLink);

            String sqlQuery2 = confirmStatementConstruct(ht);

            DataManagerForSubscription dataManagerForSubscription = new DataManagerForSubscriptionImpl(context);
            String resultString2 = dataManagerForSubscription.ExecuteQuery(sqlQuery2);

            List<SubscriptionData> SubscriptionList = dataManagerForSubscription.retrieveSubscriptioByArg("confirmLink", confirmLink);
            if (SubscriptionList != null && SubscriptionList.size() > 0) {
                Element resultElement = doc.addElement("Email");
                if (SubscriptionList.size() > 0) {
                    if (status.equalsIgnoreCase("A")) {
                        resultElement.addElement("Mode").addText("Subscription");
                    }
                    if (status.equalsIgnoreCase("I")) {
                        resultElement.addElement("Mode").addText("Unsubscription");
                    }
                    resultElement.addElement("Success").addText(" to Nikon Successful");
                    Iterator i = SubscriptionList.iterator();
                    SubscriptionData currentSubscriptionData = (SubscriptionData) i.next();
                    resultElement.addElement("Address").addText(currentSubscriptionData.getEmail());
                    mLogger.createLogDebug("email to update:" + currentSubscriptionData.getEmail());
                }
            } else {
                mLogger.createLogDebug("no email to update ");
                Element resultElement = doc.addElement("Email");
                resultElement.addElement("Mode").addText("Subscription/Unsubscription Link Error");
                resultElement.addElement("Success").addText("");
                resultElement.addElement("Address").addText("");
            }
        } else {
            mLogger.createLogDebug("no email to update ");
            Element resultElement = doc.addElement("Email");
            resultElement.addElement("Mode").addText("Subscription/Unsubscription Link Error");
            resultElement.addElement("Success").addText("");
            resultElement.addElement("Address").addText("");
        }
        return doc;

    }

    public String confirmStatementConstruct(Hashtable ht) {
        StringBuilder sb = new StringBuilder();
        sb.append("update Subscription set ");
        sb.append("STATUS ='" + ht.get("status") + "', ");
        sb.append("DATE_SUBMITTED = getDate() ");
        sb.append(" where CONFIRM_LINK = '" + ht.get("confirmLink") + "' ");
        String sqlQuery1 = sb.toString();
        return sqlQuery1;
    }
}
