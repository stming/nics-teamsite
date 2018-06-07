/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import nhk.ls.runtime.common.Logger;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author smukherj
 */
public class ContactUsExternal {

    private static final String EXTERNAL_PARAM_COORDINATES_REGEX = "^[0-9]*[-][0-9]*";
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ContactUsExternal"));
    private static final String EXTERNAL_PARAM_COORDINATES = "coordinates";
    private static final String EXTERNAL_PARAM_DCR_PATH = "DCRPath";

    public Document getPageDetails(RequestContext context) throws UnsupportedEncodingException {
        Document contactUsFileDoc = null;
        String dcrRelPath = context.getParameterString(EXTERNAL_PARAM_DCR_PATH, "");

        mLogger.createLogInfo("DCR Path: " + dcrRelPath);
        String dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrRelPath;
        if (StringUtils.isNotEmpty(dcrFullPath)) {
            File dcrFile = new File(dcrFullPath);
            if (dcrFile.exists()) {
                contactUsFileDoc = Dom4jUtils.newDocument(dcrFile);
            }
        }

        String coordinates = context.getParameterString(EXTERNAL_PARAM_COORDINATES, "");
        mLogger.createLogDebug("coordinates<--" + coordinates + "-->");

        Pattern p = Pattern.compile(EXTERNAL_PARAM_COORDINATES_REGEX);
        Matcher m = p.matcher(coordinates);

        Document resultDoc = Dom4jUtils.newDocument();
        if (m.matches()) {
            String values[] = null;
            if (coordinates != null) {
                values = coordinates.split("-");
                if (values.length > 1) {
                    String outer = values[0];
                    String inner = values[1];
                    Element resultEle = resultDoc.addElement("ContactUsExternal");
                    resultEle.addElement("MarketCategory").addText(contactUsFileDoc.selectSingleNode("//contact_us/Links[" + outer + "]/MarketCategory").getText());
                    resultEle.addElement(EXTERNAL_PARAM_DCR_PATH).addText(dcrRelPath);
                    resultEle.addElement(EXTERNAL_PARAM_COORDINATES).addText(coordinates);
                }
            }
            mLogger.createLogDebug("Result doc::" + resultDoc.asXML());
        }
        return resultDoc;
    }
}
