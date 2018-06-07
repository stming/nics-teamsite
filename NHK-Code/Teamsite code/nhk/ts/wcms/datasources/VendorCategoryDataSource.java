/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.datasources;

import com.interwoven.datasource.MapDataSource;
import nhk.ts.wcms.common.Logger;
import com.interwoven.datasource.core.DataSourceContext;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Node;
import com.interwoven.livesite.common.cssdk.datasource.AbstractDataSource;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author localadmin
 */
public class VendorCategoryDataSource extends AbstractDataSource implements MapDataSource {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.datasources.VendorCategoryDataSource"));

    public Map<String, String> execute(DataSourceContext context) {
        Map<String, String> results = new LinkedHashMap<String, String>();
        String vpath = context.getServerContext();
        if ((null != vpath) && (!("".equals(vpath)))) {
            try {
                Map params = context.getAllParameters();
                String vendorcategoryFileVPath = (String) params.get("FileVPath");
                mLogger.createLogDebug("File Path: " + vendorcategoryFileVPath);
                String temp[] = vendorcategoryFileVPath.split("templatedata/");
                vendorcategoryFileVPath = temp[1].substring(0, temp[1].indexOf("/"));
                vendorcategoryFileVPath = vpath + "/templatedata/" + vendorcategoryFileVPath + "/master_list/data/master_vendors.xml";
                mLogger.createLogDebug("File Path: " + vendorcategoryFileVPath);
                File vendorcategoryFile = new File(vendorcategoryFileVPath);
                Document vendorFileDocument = Dom4jUtils.newDocument(vendorcategoryFile);

                String selectedVendorType = (String) params.get("VendorType");

                if (selectedVendorType == null) {

                    List<Node> vendorTypeNodes = vendorFileDocument.selectNodes("//master_list/main_type/type_name");
                    mLogger.createLogDebug("Node vendor=" + vendorTypeNodes);
                    if (vendorTypeNodes.size() != 0) {
                       results.put("","Select Vendor");
                        for (Node unitNode : vendorTypeNodes) {
                            Node label = unitNode.selectSingleNode(".");
                            mLogger.createLogDebug("Node label=" + label);
                            results.put(label.getText(), label.getText());
                        }
                    }
                } else {

                    mLogger.createLogDebug("Inside selected vendor=" + selectedVendorType);
                    mLogger.createLogDebug("parsing " + vendorFileDocument.asXML() + " for //master_list/main_type[type_name='" + selectedVendorType + "']/vendor/product_name");

                    List<Node> productNameNodes = vendorFileDocument.selectNodes("//master_list/main_type[type_name='" + selectedVendorType + "']/vendor/product_name");
                    mLogger.createLogDebug("Returned " + productNameNodes.size() + " nodes");
                    if (productNameNodes.size() != 0) {
                        for (Node unitNode : productNameNodes) {
                            Node label = unitNode.selectSingleNode(".");
                            mLogger.createLogDebug("Else Node label=" + label);
                            results.put(label.getText(), label.getText());
                        }
                    }
                }
            } catch (Exception e) {
                mLogger.createLogDebug("Error in execute method::", e);
                this.mLogger.createLogErrorWithoutThrowingException("Error retrieving Vendor Category " + e.getMessage(), e);
            }
        }
        return results;
    }
}
