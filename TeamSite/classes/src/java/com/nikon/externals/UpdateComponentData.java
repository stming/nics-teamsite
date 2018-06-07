package com.nikon.externals;


import com.interwoven.livesite.common.util.Hashtable;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.ExternalUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.impl.BaseRequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;


/**
 * Created by Paul W Russell on 28/07/2014.
 */
public class UpdateComponentData {

    private Log log = LogFactory.getLog(UpdateComponentData.class);

    public Document setFallbackDCRs(RequestContext context)
    {
        log.debug("setFallbackDCRs");
        log.debug("context.getFileDAL().getRoot() : " + context.getFileDAL().getRoot());
        log.debug("context.getFileDal().getRoot() : " + context.getFileDal().getRoot());
        log.debug("context.getLiveSiteDal().toString() : " + context.getLiveSiteDal().toString());
        Document doc = Dom4jUtils.newDocument("<fallback-result/>");

        try {
            Document componentData = ((BaseRequestContext) context).getThisComponentRenderingData().getModel();
            if (componentData == null) {
                log.warn("componentData is null");
            } else {
                log.debug("componentData XML : " + FormatUtils.prettyPrint(componentData));
            }

            //Figure out locale we are browsing

            String url = Utils.getCurrentUrl(context);
            log.debug("url: " + url);
            String locale = context.getRequest().getParameter("locale");
            log.debug("locale: " + locale);

            /*if (context.isPreview()){
                locale = "en_SA";
                log.debug("preview - setting locale to : " + locale);
            }*/

            //If we are a locale other than asia, try to replace the DCR
            if (locale != null && locale.length() > 0 && !locale.toLowerCase().equals("en_asia")) {
                //build document to return
                doc.getRootElement().addAttribute("locale", locale);

                //Get fallback locale DCR
                String fallbackNames = ((BaseRequestContext) context).getThisComponentModel().valueOf("//Datum[@ID='FallbackNames']");
                log.debug("fallbackNames: " + fallbackNames);
                String[] fallbackNameArray = fallbackNames.split(",");
                for (String fallbackName : fallbackNameArray) {
                    String XPath = "//Datum[@Name='" + fallbackName + "']/DCR";
                    Node fallbackNode = componentData.selectSingleNode(XPath);
                    if (fallbackNode == null){
                        log.warn("No value found at XPath: " + XPath);
                    } else {
                        String dcrPath = fallbackNode.getText();
                        log.debug("original dcrPath:" + dcrPath);

                        //If this DCR is en_Asia and it exists,
                        String fallbackDCRPath = dcrPath.replaceFirst("(.*)/([a-z][a-z]_[A-Z][A-Z]|en_Asia)/(.*)", "$1/" + locale + "/$3");
                        log.debug("fallbackDCRPath:" + fallbackDCRPath);

                        //Check DCR exists
                        Document fallbackDCRDoc;
                        try {
                            fallbackDCRDoc = ExternalUtils.readXmlFile(context, fallbackDCRPath);
                            log.debug("fallbackDCRDoc XML : " + FormatUtils.prettyPrint(fallbackDCRDoc));
                            if (fallbackDCRDoc != null) {
                                //Now swap into component data
                                log.debug("swapping in fallback DCR path - " + fallbackDCRPath);
                                componentData.selectSingleNode(XPath).setText(fallbackDCRPath);
                            } else {
                                log.warn("fallback DCR path does not exist, not swapping");
                            }
                        } catch (Throwable e) {
                            log.warn("No fallback DCR found for: " + fallbackDCRPath);
                        }
                    }
                }

                //Update data within component
                ((BaseRequestContext) context).getThisComponentRenderingData().setModel(componentData);
                //log.debug("AFTER UPDATE - componentData XML : " + FormatUtils.prettyPrint(((BaseRequestContext) context).getThisComponentRenderingData().getModel()));
            } else {
                log.debug("locale is " + locale + ", not falling back");
            }
        } catch (Exception e){
            log.error(e);
        }

        return doc;
    }

}
