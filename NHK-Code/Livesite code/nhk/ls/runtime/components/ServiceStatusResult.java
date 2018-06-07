package nhk.ls.runtime.components;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import org.dom4j.Document;
import com.interwoven.livesite.runtime.RequestContext;
import java.io.File;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import nhk.ls.runtime.common.Logger;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import java.util.List;

public class ServiceStatusResult {

    //final static String REPAIR_CONTENT_XML = "REPAIR_CONTENT_XML";
    //final static String WAITING_CONTENT_XML = "WAITING_CONTENT_XML";
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ServiceStatusResult"));
    static String xPath = "//Content/ServiceStatus";
    private String strOrdStatus = "";
    private String readFrom = "";

    public Document readServiceNos(RequestContext context) {
        Document document = null;
        try {
            String waiting_for_pickup = context.getFileDAL().getRoot() + context.getFileDAL().getSeparator() + context.getParameterString("Waiting For Pickup");
            String repair_in_progress = context.getFileDAL().getRoot() + context.getFileDAL().getSeparator() + context.getParameterString("Repair In Progress");
            String listPattern = context.getParameterString("so_id");
            mLogger.createLogInfo("Here is list pattern value :: " + listPattern);

            String s_order = "";
            String p_serial = "";
            String arrayList[];
            arrayList = listPattern.split(";");
            mLogger.createLogDebug("Values:" + arrayList);

            s_order = arrayList[0];
            p_serial = arrayList[1];
            boolean flagxml = getServiceOrderDetails(s_order, p_serial, waiting_for_pickup, repair_in_progress);
            mLogger.createLogInfo("Flag value: " + flagxml);

            if (flagxml) {
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
            root.addElement("ErrorFlag").addText("" + flagxml);
            root.addElement("ReadFrom").addText(readFrom);
            mLogger.createLogDebug("Service Status Document: " + document.asXML());
        } catch (Exception e) {
            mLogger.createLogWarn("Error in readServiceNos", e);
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
        }
        return document;
    }


    /*
     * Getting ServiceOrderDetails from repairXML
     */
    private boolean getServiceOrderDetails(String strSrvOrder, String strPrdSerial, String waiting_for_pickup, String repair_in_progress) {
        boolean xmlFlag = false;
        Document document = null;

        //   document = getXMLDocument( getXMLPath(REPAIR_CONTENT_XML));
        readFrom = "repair_in_progress";
        document = getXMLDocument(repair_in_progress);
        xmlFlag = getServiceNodeList(document, strSrvOrder, strPrdSerial, xmlFlag);

        if (xmlFlag) {
            //mLogger.createLogDebug("Inside IF for waitingXML............");
            readFrom = "waiting_for_pickup";
            document = getXMLDocument(waiting_for_pickup);
            xmlFlag = getServiceNodeList(document, strSrvOrder, strPrdSerial, xmlFlag);
        }
        return xmlFlag;
    }


    /*
     * Getting ServiceOrderDetails from repairXML
     */
    private boolean getServiceNodeList(Document document, String strSrvOrder, String strPrdSerial, boolean xmlFlag) {

        List<Node> nodes = document.selectNodes(xPath);
        try {
            for (Node node : nodes) {
                String srvsOrder = node.valueOf("ServiceOrder");
                if (srvsOrder == null) {
                    mLogger.createLogDebug("srvsOrderisEmpty 3333");
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
                mLogger.createLogDebug("Service Order " + strSrvOrder + " is not available");
            }

        } catch (Exception e) {
            mLogger.createLogDebug("Error in method getServiceNodeList", e);
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
            mLogger.createLogDebug("Service Order : " + strSrvOrder);
            mLogger.createLogDebug("Product Serial: " + strPrdSerial);
            mLogger.createLogDebug("Service Serial: " + serviceSerial);
            mLogger.createLogDebug("Service Status : " + serviceStatus);
            strOrdStatus = serviceStatus;

        } else {
            mLogger.createLogDebug("Product Serial " + strPrdSerial + " is not available for Service Order " + strSrvOrder);
        }
    }
}
