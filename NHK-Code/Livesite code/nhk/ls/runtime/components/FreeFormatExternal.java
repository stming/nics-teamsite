/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.File;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;

import nhk.ls.runtime.common.Logger;
import org.dom4j.Document;

/**
 *
 * @author smukherj
 */
public class FreeFormatExternal {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.FreeFormatExternal"));
    private static final String EXTERNAL_PARAM_DCR_PATH = "DCRPath";

    public Document getContentDetails(RequestContext context) {

        String dcrFullPath = null;
        Document inDoc = null;
        try {
            dcrFullPath = context.getParameterString(EXTERNAL_PARAM_DCR_PATH, "");
            mLogger.createLogInfo("DCR Path: " + dcrFullPath);
            dcrFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + dcrFullPath;
            mLogger.createLogDebug("Full DCR Path: " + dcrFullPath);

            if (StringUtils.isNotEmpty(dcrFullPath)) {
                File dcrFile = new File(dcrFullPath);
                if (dcrFile.exists()) {
                    inDoc = Dom4jUtils.newDocument(dcrFile);
                }
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in retrieving Free Format DCR Details", e);
        }
        return inDoc;
    }
}
