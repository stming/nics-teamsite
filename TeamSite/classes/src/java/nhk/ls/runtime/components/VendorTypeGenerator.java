/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.PropertyContext;

import nhk.ls.runtime.common.Logger;

/**
 *
 * @author smukherj
 */
public class VendorTypeGenerator {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.VendorTypeGenerator"));

    public Document getVendorTypeOptions(PropertyContext context) {
        Document doc = Dom4jUtils.newDocument();
        Element optionsBase = doc.addElement("Options");
        String filePathSeparator = File.separator;
        String sitePath = context.getSite().getPath();
        sitePath = sitePath.substring(0, sitePath.lastIndexOf("sites/"));
        String filePath = sitePath +  filePathSeparator + "templatedata" + filePathSeparator + context.getSite().getName() + filePathSeparator + "master_list/data/master_vendors.xml";
        mLogger.createLogInfo("File Path: " + filePath);
        File vendorcategoryFile = new File(filePath);
        Document vendorFileDocument = Dom4jUtils.newDocument(vendorcategoryFile);
        List<Node> vendorTypeNodes = vendorFileDocument.selectNodes("//master_list/main_type/type_name");
        mLogger.createLogDebug("Node vendor=" + vendorTypeNodes);
        if (vendorTypeNodes != null && !vendorTypeNodes.isEmpty()) {
            int count = 0;
            Element option = optionsBase.addElement("Option");
            option.addAttribute("Selected", "true");
            option.addElement("Display").addText("Select a Type");
            option.addElement("Value").addText("");
            for (Node unitNode : vendorTypeNodes) {
                String label = unitNode.selectSingleNode(".").getText();
                mLogger.createLogDebug("Node label=" + label);
                option = optionsBase.addElement("Option");
                option.addElement("Display").addText(label);
                option.addElement("Value").addText(label);
                count++;
            }
        }
        mLogger.createLogDebug(doc.asXML());
        return doc;
    }
}
