/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.workflow.dcr;

import org.dom4j.Document;
import org.dom4j.Node;
import org.apache.commons.logging.LogFactory;

import nhk.ts.wcms.common.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author smukherj
 */
public class ProductConversionHelper {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.dcr.ProductConversionHelper"));
    private static final String PRODUCT_DISPLAY_NAME = "//product_display_name";
    private static final String PRODUCT_NAME = "//product_name";

    public static String getProductName(Document dcrFileDocument) {

        Node product = dcrFileDocument.selectSingleNode(PRODUCT_DISPLAY_NAME);
        mLogger.createLogDebug("PRODUCT_DISPLAY_NAME node is " + product);
        if (product == null || product.getText().length() == 0) {
            product = dcrFileDocument.selectSingleNode(PRODUCT_NAME);
            mLogger.createLogDebug("PRODUCT_NAME node is " + product);
        }
        String productName = (product.getText() != null && StringUtils.isNotEmpty(product.getText().trim())) ? product.getText().trim() : null;
        mLogger.createLogDebug("productName=" + productName);
        return productName;
    }
}
