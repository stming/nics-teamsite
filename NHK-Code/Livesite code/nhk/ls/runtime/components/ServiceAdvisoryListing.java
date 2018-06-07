/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import nhk.ls.runtime.dao.ServiceAdvisoryData;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import nhk.ls.runtime.common.DateUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import nhk.ls.runtime.common.Logger;

/**
 *
 * @author sbhojnag
 */
public class ServiceAdvisoryListing {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ServiceAdvisoryListing"));

    public Document getServiceAdvisoryList(RequestContext context) {
        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("Result");
            // getting service advisory listing
            DataManager dataManager = new DataManagerImplCommon(context);
            List<ServiceAdvisoryData> ServiceAdvisoryDataList = dataManager.retrieveAllServiceAdvisoryList(context.getSite().getName());
            mLogger.createLogInfo("Service Advisory List Size: " + ServiceAdvisoryDataList.size());

            if (ServiceAdvisoryDataList.size() > 0) {
                for (Iterator i = ServiceAdvisoryDataList.iterator(); i.hasNext();) {
                    ServiceAdvisoryData currentServiceAdvisoryData = (ServiceAdvisoryData) i.next();
                    Element ServiceAdvisoryElement = resultElement.addElement("ServiceAdvisory");

                    mLogger.createLogDebug("getServiceAdvisoryList:Headline=" + currentServiceAdvisoryData.getHeadline());

                    ServiceAdvisoryElement.addElement("Headline").addText((currentServiceAdvisoryData.getHeadline() != null ? currentServiceAdvisoryData.getHeadline() : ""));
                    ServiceAdvisoryElement.addElement("subHeadline").addText((currentServiceAdvisoryData.getsubHeadline() != null ? currentServiceAdvisoryData.getsubHeadline() : ""));
                    ServiceAdvisoryElement.addElement("imagePath").addText(currentServiceAdvisoryData.getimagePath());
                    if (currentServiceAdvisoryData.getalternateText() != null && currentServiceAdvisoryData.getalternateText().length() != 0) {
                        ServiceAdvisoryElement.addElement("alternateText").addText(currentServiceAdvisoryData.getalternateText());
                    } else {
                        ServiceAdvisoryElement.addElement("alternateText").addText("Empty");
                    }
                    Date date = currentServiceAdvisoryData.getDate();
                    //ServiceAdvisoryElement.addElement("date").addText(DateUtils.getDateString(date, "MMMMM dd, yyyy"));
                    ServiceAdvisoryElement.addElement("date").addText(DateUtils.getDateString(date, "dd/MM/yyyy"));
                    ServiceAdvisoryElement.addElement("linkParameter").addText(currentServiceAdvisoryData.getlinkParameter());
                    if (currentServiceAdvisoryData.getexternalLink().equalsIgnoreCase("Yes")) {
                        ServiceAdvisoryElement.addElement("link").addText(currentServiceAdvisoryData.getLink());
                    } else {
                        ServiceAdvisoryElement.addElement("link").addText("Empty");
                    }
                    ServiceAdvisoryElement.addElement("target").addText(currentServiceAdvisoryData.getTarget());
                    if (currentServiceAdvisoryData.getTarget().equalsIgnoreCase("Shadow Box")) {
                        ServiceAdvisoryElement.addElement("height").addText(currentServiceAdvisoryData.getHeight());
                        ServiceAdvisoryElement.addElement("width").addText(currentServiceAdvisoryData.getWidth());
                    }
                }
            } else {
                Element ServiceAdvisoryElement = resultElement.addElement("ServiceAdvisory").addText("Empty");

            }


        } //     }
        catch (Exception e) {
            mLogger.createLogWarn("Error in generating Service Advisory Listing Page", e);
        }
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }
}
