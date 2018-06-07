package nhk.ts.wcms.workflow.translation.bean;

import nhk.ts.wcms.common.Logger;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class NHKTranslationObjectHolder {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.translation.bean.NHKTranslationObjectHolder"));
    public static Element translationRootEl = null;

    static {
        translationRootEl = DocumentHelper.createElement("Branches");
        mLogger.createLogDebug("Initialized existingFilesDoc from NHKTranslationObjectHolder!!");
    }

    public static Element getTranslationRootEl() {
        return translationRootEl;
    }

    public static void clearDoc(Document existingFilesDoc) {
        if (existingFilesDoc != null) {
            existingFilesDoc = null;
        }
    }
    public static void clearDoc() {
        translationRootEl=null;
        translationRootEl = DocumentHelper.createElement("Branches");
        mLogger.createLogDebug("Reinitialized translationRootEl");
    }
}
