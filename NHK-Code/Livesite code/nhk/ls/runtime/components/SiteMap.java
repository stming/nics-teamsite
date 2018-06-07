/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.dao.ProductCategory;
import nhk.ls.runtime.dao.DataManager;
import nhk.ls.runtime.dao.DataManagerImplCommon;
import java.util.List;
import java.util.Iterator;
import com.interwoven.livesite.external.impl.LivesiteSiteMap;
import com.interwoven.livesite.runtime.RequestContext;
import org.dom4j.Document;
import org.dom4j.Element;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author sbhojnag
 */
public class SiteMap extends LivesiteSiteMap {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.SiteMap"));

    @Override
    public Document getSiteMap(RequestContext context) {
        Document siteMapDoc = super.getSiteMap(context);
        String ProductLabel = context.getParameterString("ProductLabel","Products");
        DataManager dataManager = new DataManagerImplCommon(context);
        List<ProductCategory> ProductCategoryList = dataManager.retrieveProductCategory();
        if (ProductCategoryList.size() > 0) {
            for (Iterator i = ProductCategoryList.iterator(); i.hasNext();) {
                ProductCategory currentProductCategory = (ProductCategory) i.next();
                Element element = (Element) siteMapDoc.selectSingleNode("//node[label[text() ='" + ProductLabel + "']]/node[@id ='" + currentProductCategory.getCategoryID() + "']");
                if (element != null) {
                    element.addAttribute("navigation-icon", currentProductCategory.getIconImageMainNavigation());
                }
            }
        }
        return siteMapDoc;
    }
}
