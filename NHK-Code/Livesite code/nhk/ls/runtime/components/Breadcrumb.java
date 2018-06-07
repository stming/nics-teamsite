/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

/**
 *
 * @author sbhojnag
 */
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.impl.BaseRequestContext;
import com.interwoven.livesite.runtime.model.SiteMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import nhk.ls.runtime.common.Logger;
import nhk.ls.runtime.common.ProductListingHelper;
import org.dom4j.Node;
//import com.interwoven.livesite.external.impl.Breadcrumb;

//public class Breadcrumb extends Breadcrumb {
//}
public class Breadcrumb {
    //  protected final Log mLogger;

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.Breadcrumb"));
    public static final String ELEMENT_BREADCRUMB = "Breadcrumb";
    public static final String ELEMENT_NODES = "Nodes";
    private static final String ELEMENT_LINK = "Link";
    private static final String ELEMENT_LINK_URL = "Url";
    private static final String ELEMENT_REQUEST = "Request";
    public static final String CONST_EMIT_REQUEST = "emitRequest";
    public static final String CONST_NODE_ID_PARAMETER = "nodeIdParameter";
    public static final String PARAM_NODE_ID = "nodeId";
    private static final String OVERVIEW_TAB_ID = "1";
    private ExternalSiteMapXmlAdapter mXmlAdapter;

    public Breadcrumb() {
//        this.mLogger = LogFactory.getLog(super.getClass());
        this.mXmlAdapter = null;
        this.mXmlAdapter = new ExternalSiteMapXmlAdapter();
    }

    public Document displayBreadcrumb(RequestContext context) {
        SiteMap siteMap = context.getLiveSiteDal().getSiteMap(context.getSite().getName());
        String nodeId = getSelectedNodeId(siteMap, (BaseRequestContext) context);

        mLogger.createLogInfo("displayBreadcrumb:Initial from sitemap Node ID: " + nodeId);

        String currentLink = context.getParameterString(ProductListing.EXTERNAL_PARAM_CURRENT_LINK, "");
        String currentTab = context.getParameterString(ProductListing.EXTERNAL_PARAM_CURRENT_TAB, "");
        mLogger.createLogDebug("displayBreadcrumb currentLink:" + currentLink);
        mLogger.createLogDebug("displayBreadcrumb currentTab:" + currentTab);

        if (StringUtils.isNotEmpty(currentLink) && !currentLink.equalsIgnoreCase(nodeId)) {
            mLogger.createLogDebug("Assigning currentLink=" + currentLink + " to Node ID.");
            nodeId = currentLink;
        } else if (StringUtils.isNotEmpty(currentTab) && !currentLink.equalsIgnoreCase(currentTab) && !currentTab.equalsIgnoreCase(OVERVIEW_TAB_ID)) {
            mLogger.createLogDebug("Assigning currentTab=" + currentTab + " to Node ID.");
            nodeId = currentTab;
        }
        mLogger.createLogInfo("displayBreadcrumb:Final from tab and link Node ID: " + nodeId);

        if (StringUtils.isEmpty(nodeId)) {
            nodeId = null;
        }
        return buildBreadcrumbDocument(context, siteMap, nodeId);
    }

    protected String getSelectedNodeId(SiteMap siteMap, BaseRequestContext context) {

        String nodeId = context.getParameterString("CategoryID");
        mLogger.createLogDebug("NodeID from Query String: " + nodeId);
        if (StringUtils.isEmpty(nodeId) || nodeId == null) {
            nodeId = siteMap.getSelectedNodeId();
            mLogger.createLogDebug("NodeID from the sitemap get selected: " + nodeId);
            if (StringUtils.isEmpty(nodeId)) {
                nodeId = siteMap.findNodeId(context);
                mLogger.createLogDebug("NodeID from the sitemap findNodeId: " + nodeId);
                /*    if (StringUtils.isNotEmpty(nodeId)){
                siteMap.setSelectedNodeId(nodeId);
                } */
            }
        }
        if (StringUtils.isNotEmpty(nodeId)) {
            siteMap.setSelectedNodeId(nodeId);
        }
        return nodeId;
    }

    protected Document buildBreadcrumbDocument(RequestContext context, SiteMap map, String nodeId) {
        Document doc = Dom4jUtils.newDocument();
        Element root = doc.addElement("Breadcrumb");

        String refAttrId = null;
        Attribute visibleBreadCrumbAttr = null;
        String visibleBreadCrumbAttrValue = null;

        String startPage = context.getSite().getStartPage();
        if (StringUtils.isNotEmpty(startPage)) {
            emitPage(root, startPage).setName("StartPage");
        } else {
            root.addElement("StartPage");
        }

        if (StringUtils.isNotEmpty(nodeId)) {
            List nodes = new ArrayList();
            Element current = (Element) map.getDocument().getRootElement().selectSingleNode("segment" + "//" + "node" + "[@" + "id" + "='" + nodeId + "']");

            if (current != null) {
                refAttrId = current.attributeValue("refid");

                if (null != refAttrId) {
                    current = (Element) map.getDocument().getRootElement().selectSingleNode("segment" + "//" + "node" + "[@" + "id" + "='" + refAttrId + "']");
                }

                visibleBreadCrumbAttr = current.attribute("visible-in-breadcrumbs");
                if (null != visibleBreadCrumbAttr) {
                    visibleBreadCrumbAttrValue = visibleBreadCrumbAttr.getValue();
                }

                if ("false".equals(visibleBreadCrumbAttrValue)) {
                    return null;
                }
            }

            while ((current != null) && (!(current.getName().equals("segment")))) {
                Element node = current.createCopy();
                node.content().removeAll(node.selectNodes("node"));
                nodes.add(0, node);
                current = current.getParent();
            }

            Element last = root.addElement("Nodes");
            int nodeCount = nodes.size();

            for (int i = 0; i < nodeCount; ++i) {
                Element newNode = (Element) nodes.get(i);
                String NODEID = newNode.attributeValue("id");

                mLogger.createLogDebug("calling getNodeLinkUrl with:" + NODEID);

                String url = map.getNodeLinkUrl(NODEID, context);
                mLogger.createLogDebug("url:" + url);

                this.mXmlAdapter.toExternalFormat(newNode);

                String productLabel = context.getParameterString("ProductLabel", "Products");



                Node sitemapNode = map.getDocument().selectSingleNode("//node[@id ='" + NODEID + "']");


                mLogger.createLogDebug("Reading node=" + NODEID + " from sitemap.");

                Node tempNode = sitemapNode;

                String variableLink = "";

                if (tempNode != null) {

                    variableLink = ProductListingHelper.getVariableLinkForCategoryTraversal(tempNode, productLabel);

                    mLogger.createLogDebug("variableLink:" + variableLink);



                    String val = (StringUtils.isNotEmpty(url)) ? url + variableLink : "";
                    mLogger.createLogDebug("Setting URL value to:" + val);

                }

                ((Element) newNode.elements("Link").get(0)).addElement("Url").setText((StringUtils.isNotEmpty(url)) ? url + variableLink : "");

                mLogger.createLogDebug("newNode=" + newNode.asXML());

                last.add(newNode);
                last = newNode;
            }
        }
        mLogger.createLogDebug("Custom Breadcrumb: " + doc.asXML());
        return doc;
    }

    protected Element emitPage(Element parent, String pageName) {
        Element root = (parent == null) ? DocumentHelper.createElement("Page") : parent.addElement("Page");

        root.setText(pageName);
        return root;
    }

    static class ExternalSiteMapXmlAdapter {

        public void toExternalFormat(Element node) {
            formatNames(node);
        }

        protected void formatNames(Element e) {
            e.setName(formatXmlName(e.getName()));
            List attributes = new LinkedList(e.attributes());
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                Attribute a = (Attribute) i.next();
                a.detach();
                e.addAttribute(formatXmlName(a.getName()), a.getValue());
            }
            for (Iterator i = e.elements().iterator(); i.hasNext();) {
                Element el = (Element) i.next();
                formatNames(el);
            }
        }

        protected String formatXmlName(String name) {
            StringBuffer newName = new StringBuffer();
            int i = name.indexOf("-");
            while (i > -1) {
                newName.append(name.substring(0, 1).toUpperCase()).append((i == 1) ? "" : name.substring(1, i).toLowerCase());

                if (i >= name.length()) {
                    break;
                }
                name = name.substring(i + 1);
                i = name.indexOf("-");
            }

            if (name.length() > 0) {
                name = name.toLowerCase();
                newName.append(name.substring(0, 1).toUpperCase());
                if (name.length() > 1) {
                    newName.append(name.substring(1));
                }
            }
            return newName.toString();
        }
    }
}
