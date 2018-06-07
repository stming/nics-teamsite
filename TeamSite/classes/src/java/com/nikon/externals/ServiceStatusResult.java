package com.nikon.externals;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import com.interwoven.livesite.runtime.RequestContext;
import java.io.File;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.dom4j.Node;
import java.util.List;

public class ServiceStatusResult {

    private Log log = LogFactory.getLog(ServiceStatusResult.class);
    static String SERVICE_NODE_XPATH = "//Content/ServiceStatus";

    private String strOrdStatus = "";
    private String readFrom = "";

    public Document getServiceStatusResult(RequestContext context) {
        log.debug("getServiceStatusResult");

        Document document = null;
        try {
            String waitingForPickup = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + context.getThisComponentModel().valueOf("//Datum[@ID='Repair']");
            String repairInProgress = context.getFileDal().getRoot() + context.getFileDal().getSeparator() +  context.getThisComponentModel().valueOf("//Datum[@ID='Pickup']");
            log.debug("waitingForPickup: " + waitingForPickup);
            log.debug("repairInProgress: " + repairInProgress);

            String s_order = context.getParameterString("service_order");
            String p_serial = context.getParameterString("product_serial");
            log.debug("s_order: " + s_order);
            log.debug("p_serial: " + p_serial);

            boolean flagXml = getServiceOrderDetails(s_order, p_serial, waitingForPickup, repairInProgress);
            log.debug("Flag value: " + flagXml);

            if (flagXml) {
                strOrdStatus = context.getParameterString("Error Message");
            }

            document = DocumentHelper.createDocument();
            document.setXMLEncoding("UTF-8");
            Element root = document.addElement("root");
            Element so = root.addElement("ServiceOrder");
            so.addCDATA(s_order);
            Element ps = root.addElement("ProductSerial");
            ps.addCDATA(p_serial);
            Element ordsts = root.addElement("OrderStatus");
            ordsts.addCDATA(strOrdStatus);
            root.addElement("ErrorFlag").addText("" + flagXml);
            root.addElement("ReadFrom").addText(readFrom);
            log.debug("Service Status Document: " + document.asXML());

        } catch (Exception e) {
            log.error("Error in readServiceNos", e);
        }
        return document;
    }

    /**
     * This method is used to load the xml file to a document and return it
     *
     * @param xmlFileName is the xml file name to be loaded
     * @return Document
     */
    public Document getXMLDocument(final String xmlFileName) {
        Document document = null;
        File dcrFile = new File(xmlFileName);
        if (dcrFile.exists()) {
            document = Dom4jUtils.newDocument(dcrFile);
        } else {
            log.warn("Cannot find document : " + xmlFileName);
        }
        return document;
    }


    /*
     * Getting ServiceOrderDetails from repairXML
     */
    private boolean getServiceOrderDetails(String strSrvOrder, String strPrdSerial, String waiting_for_pickup, String repair_in_progress) {
        boolean xmlFlag = false;
        Document document = null;

        readFrom = "repair_in_progress";
        document = getXMLDocument(repair_in_progress);
        xmlFlag = getServiceNodeList(document, strSrvOrder, strPrdSerial, xmlFlag);

        if (xmlFlag) {
            readFrom = "waiting_for_pickup";
            document = getXMLDocument(waiting_for_pickup);
            xmlFlag = getServiceNodeList(document, strSrvOrder, strPrdSerial, xmlFlag);
        }
        return xmlFlag;
    }


    /*
     * Getting getServiceNodeList from repairXML
     */
    private boolean getServiceNodeList(Document document, String strSrvOrder, String strPrdSerial, boolean xmlFlag) {

        try {
            List<Node> nodes = document.selectNodes(SERVICE_NODE_XPATH);

            for (Node node : nodes) {
                String srvsOrder = node.valueOf("ServiceOrder");
                if (srvsOrder == null) {
                    log.debug("srvsOrderisEmpty 3333");
                } else {
                    if (srvsOrder.equalsIgnoreCase(strSrvOrder)) {
                        xmlFlag = false;
                        getProductSerial(node, strSrvOrder, strPrdSerial);
                        break;
                    } else {
                        xmlFlag = true;
                    }
                }
            }
            if (xmlFlag) {
                log.debug("Service Order " + strSrvOrder + " is not available");
            }

        } catch (Exception e) {
            log.debug("Error in method getServiceNodeList", e);
        }
        return xmlFlag;
    }

    /*
     * Getting ProductSerialDetails  from waitingXML
     */
    private void getProductSerial(Node node, String strSrvOrder, String strPrdSerial) {

        String productSerial = node.valueOf("ProductSerial");

        if (productSerial.equalsIgnoreCase(strPrdSerial)) {
            String serviceSerial = node.valueOf("ServiceSerial");
            String serviceStatus = node.valueOf("ServiceStatus");
            log.debug("Service Order : " + strSrvOrder);
            log.debug("Product Serial: " + strPrdSerial);
            log.debug("Service Serial: " + serviceSerial);
            log.debug("Service Status : " + serviceStatus);
            strOrdStatus = serviceStatus;

        } else {
            log.debug("Product Serial " + strPrdSerial + " is not available for Service Order " + strSrvOrder);
        }
    }
}
