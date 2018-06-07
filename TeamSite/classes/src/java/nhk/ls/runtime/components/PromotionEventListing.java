/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import nhk.ls.runtime.dao.PromotionEventData;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.util.List;
import java.util.Iterator;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import nhk.ls.runtime.common.Logger;

/**
 *
 * @author sbhojnag
 */
public class PromotionEventListing {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.PromotionEventListing"));

    public Document getPromotionEventList(RequestContext context) {

        Document outDoc = null;
        try {
            outDoc = Dom4jUtils.newDocument();
            Element resultElement = outDoc.addElement("Result");
            DataManager dataManager = new DataManagerImplCommon(context);
            String type = context.getParameterString("type", "Promotions");
            String type1 = context.getParameterString("type1", "Events");
            mLogger.createLogInfo("Type: " + type);
            List<PromotionEventData> PromotionDataList = dataManager.retrievePromotionEventByTypeList(type, context.getSite().getName());
            List<PromotionEventData> EventDataList = dataManager.retrievePromotionEventByTypeList(type1, context.getSite().getName());
            mLogger.createLogDebug("Before Size");
            mLogger.createLogInfo("Promotion List Size: " + PromotionDataList.size());
            mLogger.createLogInfo("Event List Size: " + EventDataList.size());
            // getting promotion listing
            if (PromotionDataList.size() > 0) {
                for (Iterator i = PromotionDataList.iterator(); i.hasNext();) {
                    PromotionEventData currentPromotionData = (PromotionEventData) i.next();
                    Element PromotionsElement = resultElement.addElement("type");
                    PromotionsElement.addElement("Headline").addText(currentPromotionData.getHeadline());
                    PromotionsElement.addElement("subHeadline").addText(currentPromotionData.getsubHeadline());
                    PromotionsElement.addElement("imagePath").addText(currentPromotionData.getimagePath());
                    if(currentPromotionData.getalternateText()!=null && currentPromotionData.getalternateText().length()!=0){
                      PromotionsElement.addElement("alternateText").addText(currentPromotionData.getalternateText());
                      }else{
                             PromotionsElement.addElement("alternateText").addText("Empty");
                      }
                    PromotionsElement.addElement("date").addText(currentPromotionData.getDate().toString());
                    PromotionsElement.addElement("linkParameter").addText(currentPromotionData.getlinkParameter());
                    if (currentPromotionData.getexternalLink().equalsIgnoreCase("Yes")) {
                        PromotionsElement.addElement("link").addText(currentPromotionData.getLink());
                    } else {
                        PromotionsElement.addElement("link").addText("Empty");
                    }
                    PromotionsElement.addElement("target").addText(currentPromotionData.getTarget());
                    if (currentPromotionData.getTarget().equalsIgnoreCase("Shadow Box")) {
                    PromotionsElement.addElement("height").addText(currentPromotionData.getHeight());
                    PromotionsElement.addElement("width").addText(currentPromotionData.getWidth());
                    }
                }
            } else {
                Element PromotionsElement = resultElement.addElement("type").addText("Empty");

            }
            // getting event listing
            if (EventDataList.size() > 0) {
                for (Iterator i = EventDataList.iterator(); i.hasNext();) {
                    PromotionEventData currentEventData = (PromotionEventData) i.next();
                    Element EventsElement = resultElement.addElement("type1");
                    EventsElement.addElement("Headline").addText(currentEventData.getHeadline());
                    EventsElement.addElement("subHeadline").addText(currentEventData.getsubHeadline());
                    EventsElement.addElement("imagePath").addText(currentEventData.getimagePath());
                    if(currentEventData.getalternateText()!=null && currentEventData.getalternateText().length()!=0){
                    EventsElement.addElement("alternateText").addText(currentEventData.getalternateText());
                    }else{
                                          EventsElement.addElement("alternateText").addText("Empty");
                      }
                    EventsElement.addElement("date").addText(currentEventData.getDate().toString());
                    EventsElement.addElement("linkParameter").addText(currentEventData.getlinkParameter());
                    if (currentEventData.getexternalLink().equalsIgnoreCase("Yes")) {
                        EventsElement.addElement("link").addText(currentEventData.getLink());
                    } else {
                        EventsElement.addElement("link").addText("Empty");
                    }
                    EventsElement.addElement("target").addText(currentEventData.getTarget());
                    if (currentEventData.getTarget().equalsIgnoreCase("Shadow Box")) {
                    EventsElement.addElement("height").addText(currentEventData.getHeight());
                     EventsElement.addElement("width").addText(currentEventData.getWidth());
                    }

               //     EventsElement.addElement("height").addText(currentEventData.getHeight());
                //    EventsElement.addElement("width").addText(currentEventData.getWidth());
                }
            } else {
                Element EventsElement = resultElement.addElement("type1").addText("Empty");
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in generating Promotion Event Listing Page", e);
        }
        mLogger.createLogDebug("XML Document" + outDoc.asXML());
        return outDoc;
    }
}
