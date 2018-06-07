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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.nio.charset.Charset;


/**
 *
 * @author sbhojnag
 */
public class LearnExploreCategoryDataSource extends AbstractDataSource implements MapDataSource {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.datasources.LearnExploreCategoryDataSource"));

    public Map<String, String> execute(DataSourceContext context) {
        Map<String, String> results = new LinkedHashMap<String, String>();
        String vpath = context.getServerContext();
        if ((null != vpath) && (!("".equals(vpath)))) {
            try {
                Map params = context.getAllParameters();
                String propertyFullPath = context.getParameter("FileVPath");
                propertyFullPath = propertyFullPath.replaceFirst("templatedata","resources/properties");
                propertyFullPath = propertyFullPath.substring(0, propertyFullPath.lastIndexOf("/learn_and_explore")) + "/SiteInfo.xml";
                mLogger.createLogDebug("SiteInfo reading properties from:" + propertyFullPath);
                File sitemapFile = new File(propertyFullPath);
                Document propertyFileDocument = Dom4jUtils.newDocument(sitemapFile);
                Node node = propertyFileDocument.selectSingleNode("//entry[@key='learnNodeLabel']");
                System.out.println(node.getText());
                String learnNodeLabel = node.getText();
                System.out.println("LearnNodeLabel: "+ learnNodeLabel);
                mLogger.createLogDebug("LearnNodeLabel: " + learnNodeLabel);
                String newscategoryFileVPath = (String) params.get("FileVPath");
                mLogger.createLogDebug("File Path: " + newscategoryFileVPath);
                String temp[] = newscategoryFileVPath.split("templatedata/");
                newscategoryFileVPath = temp[1].substring(0, temp[1].indexOf("/"));
                newscategoryFileVPath = vpath + "/sites/" + newscategoryFileVPath + "/default.sitemap";
                mLogger.createLogDebug("File Path: " + newscategoryFileVPath);
                File newscategoryFile = new File(newscategoryFileVPath);
                Document sitemapFileDocument = Dom4jUtils.newDocument(newscategoryFile);
                List<Node> pressroomNode = sitemapFileDocument.selectNodes("//node[label[text() = '" + learnNodeLabel + "']]/node");
//                List<Node> pressroomNode = sitemapFileDocument.selectNodes("//node[label[text() = 'Press Room']]/node");
                if (pressroomNode.size() != 0) {
                    for (Node unitNode : pressroomNode) {
                        Node label = unitNode.selectSingleNode("label");
                        results.put(label.getText().trim(), label.getText().trim());

                        mLogger.createLogDebug("Learn KEY:"+label.getText().trim() + "::VALUE:" + label.getText().trim());
                    }
                }
            } catch (Exception e) {
                mLogger.createLogDebug("Error in execute method::", e);
                this.mLogger.createLogErrorWithoutThrowingException("Error retrieving News Category " + e.getMessage(), e);
            }
        }
        return results;
    }
}
