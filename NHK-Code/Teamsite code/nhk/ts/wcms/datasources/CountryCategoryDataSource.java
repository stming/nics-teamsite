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
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.livesite.common.cssdk.datasource.AbstractDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author localadmin
 */
public class CountryCategoryDataSource extends AbstractDataSource implements MapDataSource {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.datasources.CountryCategoryDataSource"));

    public Map<String, String> execute(DataSourceContext context) {
        this.mLogger = new Logger(LogFactory.getLog(this.getClass()));
        Map<String, String> results = new LinkedHashMap<String, String>();
        String vpath = context.getServerContext();
        if ((null != vpath) && (!("".equals(vpath)))) {
            try {
                Map params = context.getAllParameters();
                String countrycategoryFileVPath = (String) params.get("FileVPath");
                mLogger.createLogDebug("File Path: " + countrycategoryFileVPath);
                String temp[] = countrycategoryFileVPath.split("templatedata/");
                countrycategoryFileVPath = temp[1].substring(0, temp[1].indexOf("/"));
                countrycategoryFileVPath = vpath + "/templatedata/" + countrycategoryFileVPath + "/country_region_city/data/country_region_city.xml";
                mLogger.createLogDebug("File Path: " + countrycategoryFileVPath);
                File countrycategoryFile = new File(countrycategoryFileVPath);
                Document CountryFileDocument = Dom4jUtils.newDocument(countrycategoryFile);

                String selectedCountryType = (String) params.get("CountryType");
                String selectedRegionType = (String) params.get("RegionType");

                mLogger.createLogDebug("CountryType="+selectedCountryType);
                mLogger.createLogDebug("RegionType="+selectedRegionType);

                if (selectedCountryType == null) {
                    List<Node> CountryTypeNodes = CountryFileDocument.selectNodes("//country_region_city/country/country_name");
                    mLogger.createLogDebug("Returned " + CountryTypeNodes.size() + " country nodes.");
                    if (CountryTypeNodes.size() != 0) {
                        for (Node unitNode : CountryTypeNodes) {
                            Node label = unitNode.selectSingleNode(".");
                            mLogger.createLogDebug("Node label=" + label);
                            results.put(label.getText(), label.getText());
                        }
                    }
                } else if (selectedCountryType != null && selectedRegionType == null) {

                    String targetField = (String) params.get("TargetField");
                    mLogger.createLogDebug("About to populate targetField=" + targetField);

                    if (targetField.equalsIgnoreCase("Region")) {
                        mLogger.createLogDebug("Inside selected country=" + selectedCountryType);
                        mLogger.createLogDebug("parsing " + CountryFileDocument.asXML() + " for //country_region_city/country[country_name='" + selectedCountryType + "']/region/region_name");
                        List<Node> regionNameNodes = CountryFileDocument.selectNodes("//country_region_city/country[country_name='" + selectedCountryType + "']/region/region_name");
                        addNodes(regionNameNodes, results, targetField);
                    } else if (targetField.equalsIgnoreCase("City")) {
                        mLogger.createLogDebug("Inside selected country=" + selectedCountryType);
                        mLogger.createLogDebug("parsing " + CountryFileDocument.asXML() + " for //country_region_city/country[country_name='" + selectedCountryType + "']/region/city/city_name");
                        List<Node> cityNameNodes = CountryFileDocument.selectNodes("//country_region_city/country[country_name='" + selectedCountryType + "']/region/city/city_name");
                        addNodes(cityNameNodes, results, targetField);
                    }
                } else {
                    mLogger.createLogDebug("Inside selected country=" + selectedCountryType);
                    mLogger.createLogDebug("Inside selected region=" + selectedRegionType);
                    mLogger.createLogDebug("parsing " + CountryFileDocument.asXML() + " for //country_region_city/country[country_name='" + selectedCountryType + "']/region[region_name='" + selectedRegionType + "']/city/city_name");
                    List<Node> cityNameNodes = CountryFileDocument.selectNodes("//country_region_city/country[country_name='" + selectedCountryType + "']/region[region_name='" + selectedRegionType + "']/city/city_name");
                    addNodes(cityNameNodes, results, "City");
                }
            } catch (Exception e) {
                this.mLogger.createLogErrorWithoutThrowingException("Error retrieving Country Category " + e.getMessage(), e);
            }
        }
        return results;
    }

    private void addNodes(List<Node> nameNodes, Map<String, String> results, String targetField) {
        if (nameNodes.size() != 0) {
            for (Node unitNode : nameNodes) {
                String label = unitNode.selectSingleNode(".").getText();
                if (StringUtils.isNotBlank(label)) {
                    mLogger.createLogDebug("Else Node label=" + label);
                    results.put(label, label);
                }
            }
            mLogger.createLogDebug("Returned " + results.size() + " " + targetField + " nodes.");
        }
    }
}
