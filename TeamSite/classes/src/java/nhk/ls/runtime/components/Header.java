/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.model.rule.Segment;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import nhk.ls.runtime.common.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.Set;
import org.jsoup.Jsoup;

/**
 *
 * @author sbhojnag
 */
public class Header {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.Header"));

    public Document setGSACode(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("Result");
            String GSACode = context.getParameterString("GSACode");
            GSACode = Jsoup.parse(GSACode).text();
           //String jsContent = "";
            if (GSACode != null) {
             resultElement.addElement("GSACode").addText(GSACode);
           }
           } catch (Exception e) {
            mLogger.createLogWarn("Error in generating Analytics Information", e);
        }
        mLogger.createLogDebug("Header: " + outDoc.asXML());
        return outDoc;
    }
}
