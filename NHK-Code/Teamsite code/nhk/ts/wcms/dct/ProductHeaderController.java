package nhk.ts.wcms.dct;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSNode;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.servlet.ServletOutputStream;
import nhk.ts.wcms.common.Logger;
import nhk.ts.wcms.common.TSHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

public class ProductHeaderController extends HttpServlet {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.dct.ProductHeaderController"));

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        mLogger.createLogDebug("doPost invoked from TS");

        CSVPath siteVPath = null;

        try {

            if (request.getParameter("DCRPath") != null) {
                // Create the directory if it does not exist
                String dcrPath = request.getParameter("DCRPath");
                mLogger.createLogDebug("Input DCRPath=" + dcrPath);

                Document propertyFileDocument = readPropertyFile(dcrPath);

                Node node = propertyFileDocument.selectSingleNode("//entry[@key='productNodeLabel']");
                String productlabel = node.getText();

                String catIdRelativePath = dcrPath.substring(dcrPath.indexOf("/data/") + 6, dcrPath.lastIndexOf("/"));
                String catEnglishNameRelativePath = convertToCategoryEnglishNamePath(catIdRelativePath, request, productlabel);
                String dcrDirPath = dcrPath.substring(0, dcrPath.indexOf("/data/") + 6) + catEnglishNameRelativePath;

                File dcrFolderStr = new File(dcrDirPath);
                dcrFolderStr.mkdirs();

                response.setContentType("text/xml; charset=UTF-8");
                PrintWriter out;
                Element root = DocumentHelper.createElement("Result");
                String dcrFilePath = dcrDirPath.substring(dcrPath.indexOf("/data/") + 6) + dcrPath.substring(dcrPath.lastIndexOf("/") + 1);
                root.addText(dcrFilePath);
                out = response.getWriter();
                out.println(root.asXML());

                mLogger.createLogDebug("Output DCRPath=" + root.getText());
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error in doPost method::", e);
        }
    }

    private String convertToCategoryEnglishNamePath(String categoryIdHierarchyPath, HttpServletRequest request, String productlabel) throws Exception {
        String result = "";
        mLogger.createLogDebug("convertToCategoryEnglishNamePath categoryIdHierarchyPath=" + categoryIdHierarchyPath);
        String[] categoryIds = categoryIdHierarchyPath.split("/");

        Document sitemapFileDocument = readSitemapFile(request);
        String categoryEnglishDesc = "";
        Node selectedCategoryDescriptionNode = null;
        for (int i = 0; i < categoryIds.length; i++) {
            String currentCategoryId = categoryIds[i];

            if (i == 0) {
                selectedCategoryDescriptionNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + currentCategoryId
                        + "']/description");
                if (selectedCategoryDescriptionNode != null && StringUtils.isNotEmpty(selectedCategoryDescriptionNode.getText())) {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getText();
                } else {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getParent().selectSingleNode("label").getText();
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + currentCategoryId + " on sitemap is " + categoryEnglishDesc);
                result += categoryEnglishDesc + "/";
            } else if (i == 1) {

                selectedCategoryDescriptionNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + currentCategoryId
                        + "']/description");
                if (selectedCategoryDescriptionNode != null && StringUtils.isNotEmpty(selectedCategoryDescriptionNode.getText())) {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getText();
                } else {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getParent().selectSingleNode("label").getText();
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + currentCategoryId + " on sitemap is " + categoryEnglishDesc);
                result += categoryEnglishDesc + "/";
            } else if (i == 2) {

                selectedCategoryDescriptionNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + categoryIds[1]
                        + "']/node[@id='"
                        + currentCategoryId
                        + "']/description");
                if (selectedCategoryDescriptionNode != null && StringUtils.isNotEmpty(selectedCategoryDescriptionNode.getText())) {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getText();
                } else {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getParent().selectSingleNode("label").getText();
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + currentCategoryId + " on sitemap is " + categoryEnglishDesc);
                result += categoryEnglishDesc + "/";
            } else if (i == 3) {

                selectedCategoryDescriptionNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + categoryIds[1]
                        + "']/node[@id='"
                        + categoryIds[2]
                        + "']/node[@id='"
                        + currentCategoryId
                        + "']/description");
                if (selectedCategoryDescriptionNode != null && StringUtils.isNotEmpty(selectedCategoryDescriptionNode.getText())) {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getText();
                } else {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getParent().selectSingleNode("label").getText();
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + currentCategoryId + " on sitemap is " + categoryEnglishDesc);
                result += categoryEnglishDesc + "/";
            } else if (i == 4) {

                selectedCategoryDescriptionNode = sitemapFileDocument.selectSingleNode("//site-map/segment/node[label='" + productlabel + "']/node[@id='"
                        + categoryIds[0]
                        + "']/node[@id='"
                        + categoryIds[1]
                        + "']/node[@id='"
                        + categoryIds[2]
                        + "']/node[@id='"
                        + categoryIds[3]
                        + "']/node[@id='"
                        + currentCategoryId
                        + "']/description");
                if (selectedCategoryDescriptionNode != null && StringUtils.isNotEmpty(selectedCategoryDescriptionNode.getText())) {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getText();
                } else {
                    categoryEnglishDesc = selectedCategoryDescriptionNode.getParent().selectSingleNode("label").getText();
                }
                mLogger.createLogDebug("convertToCategoryNamePath " + currentCategoryId + " on sitemap is " + categoryEnglishDesc);
                result += categoryEnglishDesc + "/";
            }
        }
        mLogger.createLogDebug("convertToCategoryNamePath categoryEnglishNameHierarchyPath=" + result);
        return result;
    }

    private Document readSitemapFile(HttpServletRequest request)
            throws Exception, CSAuthorizationException,
            CSExpiredSessionException, CSRemoteException, CSException,
            CSObjectNotFoundException, DocumentException {
        String sitePath = null;
        CSVPath siteVPath;
        CSClient masterClient = MasterFactory.getMasterClient();

        String dctPath = request.getParameter("dctpath");

        String waPath = dctPath.substring(0, dctPath.indexOf("/templatedata"));
        CSVPath waVpath = new CSVPath(waPath);

        CSFile waFile = masterClient.getFile(waVpath);

        CSNode[] csNodes = waFile.getChildren();
        for (int i = 0; i < csNodes.length; i++) {
            CSNode node = csNodes[i];
            if (node.getVPath().toString().endsWith("sites")) {
                sitePath = ((CSNode) node.getChildren()[0]).getVPath().toString();
            }
        }
        File sitemapFile = new File(sitePath + "/default.sitemap");

        return Dom4jUtils.newDocument(sitemapFile);
    }

    private Document readPropertyFile(String dcrPath) {
        String locale = dcrPath.substring(dcrPath.indexOf("/templatedata/") + 14, dcrPath.indexOf("/", dcrPath.indexOf("/templatedata/") + 14));
        String propertyFullPath = dcrPath.substring(0, dcrPath.indexOf("/templatedata/")) + "/resources/properties/" + locale + "/SiteInfo.xml";

        File propertyFile = new File(propertyFullPath);
        Document propertyFileDocument = Dom4jUtils.newDocument(propertyFile);

        return propertyFileDocument;
    }
}
