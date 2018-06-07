/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.IOException;

import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import nhk.ls.runtime.dao.ProductCategory;

import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.dao.DAOException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author smukherj
 */
public class ProductCategoryHeader {

    //private static Document doc;
    private DataManager dataManager;
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.ProductCategoryHeader"));
    private static final String EXTERNAL_PARAM_CATEGORYID = "CategoryID";

    public Document getProductCategory(RequestContext context) throws IOException {
        Document doc = null;
        try {
            String productCategoryID = context.getParameterString(EXTERNAL_PARAM_CATEGORYID, "");
            doc = Dom4jUtils.newDocument();
            if (StringUtils.isNotEmpty(productCategoryID)) {
                ProductCategory category = retrieveProductCategoryForDisplay(context, productCategoryID);
                doc.addElement("CategoryInfo");
                Element base = doc.getRootElement();
                base.addElement("CategoryName").addText(category.getCategoryName());
                base.addElement("Banner").addText(category.getFlashBannerPath());
                Element bannerTextEle = base.addElement("BannerText");
                String bannerText = category.getFlashBannerText();
                if (StringUtils.isNotEmpty(bannerText)) {
                    bannerTextEle.addText(bannerText);
                }
                base.addElement("SiteName").addText(context.getSite().getName());
            }
        } catch (Exception e) {
            mLogger.createLogWarn("Error in retrieving Product Category", e);
        }
        return doc;
    }

    /**
     * The method returns Product Category for levels 1,2,3.
     * For level 4 and 5, it returns the Product Category at level 3
     *
     * @param flashBannerPath
     * @return
     */
    private ProductCategory retrieveProductCategoryForDisplay(RequestContext context, String productCategoryID) throws DAOException {
        this.dataManager = new DataManagerImplCommon(context);
        return dataManager.checkAndRetrieveCategoryByID(productCategoryID);
    }
}
