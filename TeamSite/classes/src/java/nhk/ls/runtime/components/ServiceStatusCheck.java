package nhk.ls.runtime.components;

//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.List;
//import java.util.Properties;
//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.Node;
import nhk.ls.runtime.common.Logger;
import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.common.web.URLRedirectForwardAction;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.commons.logging.LogFactory;
//import java.net.URLEncoder;

public class ServiceStatusCheck {

    //final static String REPAIR_CONTENT_XML = "REPAIR_CONTENT_XML";
    //final static String WAITING_CONTENT_XML = "WAITING_CONTENT_XML";
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ServiceStatusCheck"));
    //static String xPath = "//Content/ServiceStatus";
    //String strOrdStatus="";
//	Properties prop = new Properties();

    public ForwardAction doSaveDetails(RequestContext context) throws Exception {

        String so_number = context.getParameterString("service_order");
        mLogger.createLogInfo("Service Number: " + so_number);
        String ps_number = context.getParameterString("product_serial");
        mLogger.createLogInfo("Product Serial Number: " + ps_number);
        String service_status_result = context.getParameterString("resultpage");
        //End

        mLogger.createLogDebug("got result page ::" + service_status_result);
        //		strOrdStatus=URLEncoder.encode(strOrdStatus, "UTF8");
        String ordDetailPattern = so_number + ";" + ps_number;
        mLogger.createLogDebug("Encoded URL: " + ordDetailPattern);
        service_status_result = service_status_result + "so_id=" + ordDetailPattern;
        mLogger.createLogDebug("The formed url is :: " + service_status_result);

        return new URLRedirectForwardAction(context, service_status_result);
    }
    /**
     * This method is used to load the xml file to a document and return it
     *
     * @param xmlFileName is the xml file name to be loaded
     * @return Document
     */
    /*   public Document getXMLDocument( final String xmlFileName 

    }
    catch (DocumentException e)
    {
    e.printStackTrace();
    }
    return document;
    }

    /*   private static String getXMLPath(String key) {

    Properties prop = new Properties();
    String filePath=null;
    try {
    FileInputStream fis = new FileInputStream("C:/iw-home/TeamSite/local/config/lib/content_center/livesite_customer_src/src/nhk/ls/runtime/contentfielocation.properties");
    prop.load(fis);
    filePath= prop.getProperty(key);
    }catch(IOException e){
    e.printStackTrace();
    }


    return filePath;
    } */
    /*
     * Getting ServiceOrderDetails from repairXML
     */
    /*	private boolean getServiceOrderDetails(String strSrvOrder, String strPrdSerial, String waiting_for_pickup, String repair_in_progress) {
    boolean xmlFlag=false;
    Document document=null;

    //   document = getXMLDocument( getXMLPath(REPAIR_CONTENT_XML));
    document = getXMLDocument(repair_in_progress);
    xmlFlag = getServiceNodeList(document,strSrvOrder,strPrdSerial,xmlFlag);

    if(xmlFlag){
    document = getXMLDocument(waiting_for_pickup);
    xmlFlag = getServiceNodeList(document,strSrvOrder,strPrdSerial,xmlFlag);
    }
    return true;
    }


    /*
     * Getting ServiceOrderDetails from repairXML
     */
    /*	private boolean getServiceNodeList(Document document, String strSrvOrder, String strPrdSerial, boolean xmlFlag) {

    List<Node> nodes = document.selectNodes(xPath);
    try{
    for (Node node : nodes)
    {
    String srvsOrder = node.valueOf("ServiceOrder");
    if(srvsOrder == null)
    {
    }
    else{
    if(srvsOrder.equalsIgnoreCase(strSrvOrder))
    {
    xmlFlag=false;
    getProductSerial(node,strSrvOrder,strPrdSerial);
    break;
    } else{
    xmlFlag=true;
    }
    }
    }
    if(xmlFlag)

    }catch(Exception e){
    e.printStackTrace();
    }
    return xmlFlag;
    }

    /*
     * Getting ProductSerialDetails  from waitingXML
     */
    /*	private void getProductSerial(Node node, String strSrvOrder, String strPrdSerial) {

    String productSerial = node.valueOf("ProductSerial");

    if(productSerial.equalsIgnoreCase(strPrdSerial)){
    String serviceSerial = node.valueOf("ServiceSerial");
    String serviceStatus = node.valueOf("ServiceStatus");
    strOrdStatus=serviceStatus;

    }else{
    //strOrdStatus = "Service order and Service serial numbers are not valid";
    }
    } */
}
