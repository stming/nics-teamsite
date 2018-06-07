/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import org.dom4j.Document;
import org.apache.commons.logging.LogFactory;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import nhk.ls.runtime.common.Logger;
import org.dom4j.Element;
import org.dom4j.Node;
import java.io.*;
import java.net.URLDecoder;
//import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import nhk.ls.runtime.common.DateUtils;

/**
 *
 * @author sbhojnag
 */
public class newsDetails {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.newsDetails"));

    public Document getNewsDetails(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            String dcrPath = null;
            String queryString = context.getRequest().getQueryString();
            queryString=URLDecoder.decode(queryString, "UTF-8");
            mLogger.createLogInfo("After decoding: " +queryString );
            if (queryString != null) {
                mLogger.createLogDebug("Query String: " + queryString);
                String[] qsElement = queryString.split("=");

                dcrPath = qsElement[1];
            }
            Element responseElement = outDoc.addElement("Response");
            if (dcrPath != null) {
                mLogger.createLogInfo("DCR Path: " + dcrPath);
                String dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrPath;
                mLogger.createLogDebug("Full DCR Path: " + dcrFullPath);
                mLogger.createLogDebug("Request URL: " + context.getRequest().getRequestURL().toString());
                mLogger.createLogDebug("Query String: " + context.getRequest().getQueryString());
                File dcrFile = new File(dcrFullPath);
                mLogger.createLogDebug("File Exists: "+ dcrFile.exists() + dcrFile.getName());
                if (dcrFile.exists()) {
                    mLogger.createLogDebug("Inside DCR Response");
                    Document document = Dom4jUtils.newDocument(dcrFile);
                    responseElement.add(document.getRootElement());
                    Node node = document.selectSingleNode("//Date");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
                    Date date1 = sdf.parse(node.getText());
                    //node.setText(DateUtils.getDateString(date1, "MMMMM dd, yyyy"));
                    node.setText(DateUtils.getDateString(date1, "dd/MM/yyyy"));
                }
            }
            responseElement.addElement("SiteName").addText(context.getSite().getName());
            responseElement.addElement("PageUrl").addText(context.getRequest().getRequestURL().toString() + "?" + queryString);
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating News Details Page", e);
        }
        
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }
}
