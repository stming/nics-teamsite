/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.Element;
import org.apache.commons.logging.LogFactory;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import nhk.ls.runtime.common.Logger;

/**
 *
 * @author chitresh
 */
public class DealerDetailsExternal {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.DealerDetailsExternal"));

    public Document getDealerDetails(RequestContext context) {
        String dcrFullPath = null;
        Document inDoc = null;
        try {
            dcrFullPath = (context != null && context.getParameters() != null && context.getParameters().get("DCRPath") != null) ? (String) context.getParameters().get("DCRPath") : "";
            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;

            mLogger.createLogInfo("dcrFullPath=" + dcrFullPath);
            File dcrFile = new File(dcrFullPath);

            Document doc = Dom4jUtils.newDocument();
            doc.addElement("DealerInfo");

            Element base = doc.getRootElement();

            if (dcrFile.exists()) {
                mLogger.createLogDebug("inDoc");
                inDoc = Dom4jUtils.newDocument(dcrFile);
                mLogger.createLogDebug("inDoc" + inDoc.asXML());
            }
            base.addElement("Details").addText("Details data");
        } catch (Exception e) {
            mLogger.createLogWarn("Error in getDealerDetails", e);
        }
        /*   if(context.isPreview()){
        Element root = inDoc.getRootElement();
        root.addElement("isPreview").addText("true");
        } */
        return inDoc;
    }
}
