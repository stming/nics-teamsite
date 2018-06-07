/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.components;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Iterator;

import nhk.ls.runtime.dao.Dealer;
import nhk.ls.runtime.dao.DealerDataManager;
import nhk.ls.runtime.dao.DealerDataManagerImpl;

import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import nhk.ls.runtime.dao.DAOException;
import nhk.ls.runtime.common.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;

/**
 *
 * @author smukherj
 */
public class DealerSearch {

    private static final String FAILURE_TO_SHOW_PAGE_DUE_TO_INVALID_INPUTS = "Failure to show page.";
    private DealerDataManager dataManager;
    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.components.DealerSearch"));
    private static final String BUNDLE_NAME = "nhk.ls.runtime.common.Country_Dealer";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    private static final String descriptionDelimiter = "|";
    private static final String ALPHA_REGEX = "^[a-zA-Z ]*";
    private static final String NUMERIC_REGEX = "^[0-9]*";

    public Document getDynamicCountryXML(RequestContext context) throws IOException {

        Pattern alphaPattern = Pattern.compile(ALPHA_REGEX);
        Pattern numPattern = Pattern.compile(NUMERIC_REGEX);

        dataManager = new DealerDataManagerImpl(context);

        mLogger.createLogInfo("Inside getDynamicCountryXML method of DealerSearch-->");
        Document doc = null;
        try {
            doc = Dom4jUtils.newDocument();
            // Get the Selected County selection from Country Drop down in AJAX
            String locale = (context != null && context.getSite().getName() != null && context.getSite().getName() != null) ? (String) context.getSite().getName()
                    : " ";
            String countrySel = context.getParameterString("CountrySel", "");
            String countryReq = context.getParameterString("CountryDropDown", " ");
            String vendorType = context.getParameterString("VendorType", "");
            String searchText = context.getParameterString("SearchText", "");
            String vendorNames = context.getParameterString("VendorNamesSel", "");
            String userClickedSearch = context.getParameterString("UserClickedSearch", "");
            String serviceType = context.getParameterString("ServiceType", ""); // added on 20141211


            // Just to validate that nothing can be injected.
            String countryDropdownDisplay = context.getParameterString("CountryDropDownDisplay", "");
            String vendorTypeDisplay = context.getParameterString("VendorTypeDisplay", "");
            String regionSel = context.getParameterString("RegionSel", "");
            String citySel = context.getParameterString("CitySel", "");

            mLogger.createLogInfo("Locale-->" + locale + "-->");
            mLogger.createLogInfo("CountrySel-->" + countrySel + "-->");
            mLogger.createLogInfo("countryReq->" + countryReq + "-->");
            mLogger.createLogInfo("vendorType->" + vendorType + "-->");
            mLogger.createLogInfo("searchText->" + searchText + "-->");
            mLogger.createLogInfo("regionSel->" + regionSel + "-->");
            mLogger.createLogInfo("citySel->" + citySel + "-->");
            mLogger.createLogInfo("vendorNames->" + vendorNames + "-->");
            mLogger.createLogInfo("userClickedSearch->" + userClickedSearch + "-->");
            mLogger.createLogInfo("serviceType->" + serviceType + "-->"); // added on 20141211

            Element root = doc.addElement("CountryInfo");

            String masterListLocale = locale;

            if (masterListLocale.equalsIgnoreCase("en_Asia") && !countrySel.equalsIgnoreCase("")) {
                masterListLocale = getLocaleBasedOnCountrySelected(context, countrySel);
            }

            HashMap<String, String> vendorNameDescMap = setVendorDescriptionMap(context, masterListLocale, vendorType);

            if (countryReq.equalsIgnoreCase("1")) {
                root = checkAddCountries(root, vendorType, locale);
                if (userClickedSearch.equalsIgnoreCase("Yes")) {
                    root = checkAddVendors(root, vendorType, locale, countrySel, regionSel, citySel, vendorNameDescMap);
                }
            } else {
                root = checkAddVendors(root, vendorType, locale, countrySel, regionSel, citySel, vendorNameDescMap);
            }
            List vendorNameList = null;
            if (StringUtils.isNotEmpty(vendorNames)) {
                String[] vendorNameStringArray = vendorNames.split(",");
                vendorNameList = Arrays.asList(vendorNameStringArray);
            }

            if (userClickedSearch.equalsIgnoreCase("Yes") || !countryReq.equalsIgnoreCase("1")) {
                // User has taken some action on the page, which suggests he wants dealers to be fetched.
                List<Dealer> dealerList = new ArrayList<Dealer>();
                if (countrySel.equalsIgnoreCase("")) {
                    mLogger.createLogDebug("Fetch the code on the basis of locale=" + locale);
                    // Method to retrive all the dealers for respective locale
                    if (locale.equalsIgnoreCase("en_Asia")) {
                        // No point in searching for en_Asia locale as there would be no records under that locale.
                        locale = "";
                    } else {
                        root = checkAddRegions(root, vendorType, locale, countrySel);
                        root = checkAddCity(root, vendorType, locale, countrySel, regionSel);
                        root = this.checkAddServiceType(root, vendorType, locale, serviceType); //Added 20141211
                    }
                    dealerList = retrieveAllDealersforLocale(locale, vendorType, searchText, regionSel, citySel, vendorNameList, serviceType);
                } else {
                    mLogger.createLogDebug("Fetch the code on the basis of country=" + countrySel);

                    // Method to retrive all the dealers for selected country
                    root = checkAddRegions(root, vendorType, "", countrySel);
                    root = checkAddCity(root, vendorType, "", countrySel, regionSel);
                    root = this.checkAddServiceType(root, vendorType, "", serviceType); //Added 20141211
                    dealerList = retrieveAllDealersforCountry(countrySel, vendorType, searchText, regionSel, citySel, vendorNameList, masterListLocale, serviceType);
                }
                populateDealersIntoResult(root, dealerList);
            }

        } catch (Exception e) {
            mLogger.createLogWarn("Error in getDynamicCountryXML", e);
        }
        mLogger.createLogDebug("Dealer Doc: " + doc.asXML());
        return doc;
    }

    private String getLocaleBasedOnCountrySelected(RequestContext context, String countrySel) {
        // Have to find the correct locale based on country selected.
        String propertyFullPath = context.getFileDal().getRoot() + context.getFileDal().getSeparator() + "resources/properties/MasterVendorList.properties";
        Properties masterVendorList = new Properties();
        try {
            masterVendorList.load(new FileInputStream(propertyFullPath));
        } catch (IOException e) {
            mLogger.createLogWarn("sendEmail reading properties from:" + propertyFullPath + " error!", e);
        }
        Properties props = new Properties();
        return (masterVendorList.getProperty(countrySel) != null ? masterVendorList.getProperty(countrySel) : "en_Asia");
    }

    private Element checkAddCountries(Element root, String vendorType, String locale) {

        List<String> filteredlistOfCountries = new ArrayList<String>();

        mLogger.createLogInfo("About to check if countries exist for vendor type");
        List objects = dataManager.checkForVendorsGroupedByCountries(vendorType, locale);
        if (CollectionUtils.isNotEmpty(objects)) {
            mLogger.createLogDebug(objects.size() + " such countries found.");
            for (Iterator it = objects.iterator(); it.hasNext();) {
                String row = (String) it.next();
                if (row != null) {
                    mLogger.createLogDebug(row + " has " + vendorType + "s.");
                    filteredlistOfCountries.add(row);
                }
            }
        }
        return addNode(filteredlistOfCountries, root, "CountryList");
    }

    private Element checkAddVendors(Element root, String vendorType, String locale, String countrySel, String regionSel, String citySel, HashMap<String, String> vendorNameDescMap) {

        List<String> filteredlistOfVendorNames = new ArrayList<String>();

        mLogger.createLogInfo("About to check if vendors exist for vendor type");
        List objects = dataManager.checkForVendorsGroupedByVendorName(vendorType, locale, countrySel, regionSel, citySel);
        if (CollectionUtils.isNotEmpty(objects)) {
            mLogger.createLogDebug(objects.size() + " such vendors found.");
            for (Iterator it = objects.iterator(); it.hasNext();) {
                String row = (String) it.next();
                if (row != null) {
                    mLogger.createLogDebug(row + " has " + vendorType + "s.");
                    filteredlistOfVendorNames.add(row + descriptionDelimiter + vendorNameDescMap.get(row));
                }
            }
        }
        return addNode(filteredlistOfVendorNames, root, "VendorName");
    }

    /**
     * Method to read the Country and Dealer list and Add to Node
     *
     * @param PropList
     *            ,document root object,NodeName
     * @return Country and Dealer List Element
     */
    public Element addNode(List<String> optionList, Element root, String NodeNm) {
        String[] dealer;
        if (NodeNm != null && NodeNm.equalsIgnoreCase("VendorName")) {

            for (Iterator<String> it = optionList.iterator(); it.hasNext();) {
                String nameDesc = it.next();
                mLogger.createLogDebug("To split by " + descriptionDelimiter + " nameDesc=" + nameDesc);
                dealer = StringUtils.split(nameDesc, descriptionDelimiter);
                Element dealerList = root.addElement(NodeNm);
                // Read the Name and Description into the 2 attributes.
                for (int i = 0; i < dealer.length; i++) {
                    if (i == 0) {
                        dealerList.addAttribute("Name", dealer[i]);
                    } else {
                        dealerList.addAttribute("Desc", dealer[i]);
                    }
                }
                // Add a default blank description to counter blank node issue
                if (dealer.length == 1) {
                    dealerList.addAttribute("Desc", "");
                }
            }

        } else {
            for (Iterator<String> it = optionList.iterator(); it.hasNext();) {
                Element countryList = root.addElement(NodeNm);
                countryList.addAttribute("Name", it.next());
            }
        }
        return root;
    }

    private HashMap<String, String> setVendorDescriptionMap(RequestContext context, String masterListLocale, String selectedVendorType) {
        HashMap<String, String> vendorNameDescMap = new HashMap<String, String>();

        mLogger.createLogInfo("Inside selected vendor=" + selectedVendorType);
        String vendorcategoryFileVPath = context.getFileDal().getRoot() + "/templatedata/" + masterListLocale
                + "/master_list/data/master_vendors.xml";
        mLogger.createLogDebug("File Path: " + vendorcategoryFileVPath);
        File vendorcategoryFile = new File(vendorcategoryFileVPath);
        Document vendorFileDocument = Dom4jUtils.newDocument(vendorcategoryFile);
        mLogger.createLogDebug("parsing " + vendorFileDocument.asXML() + " for //master_list/main_type[type_name='"
                + selectedVendorType + "']/vendor");

        List<Node> vendorNameNodes = vendorFileDocument.selectNodes("//master_list/main_type[type_name='"
                + selectedVendorType + "']/vendor");
        mLogger.createLogDebug("Returned " + vendorNameNodes.size() + " nodes");
        if (vendorNameNodes.size() != 0) {

            for (Iterator<Node> it = vendorNameNodes.iterator(); it.hasNext();) {
                Node node = it.next();
                String vendorDesc = "";
                if (node.selectSingleNode("product_name") == null) {
                    continue;
                }
                String vendorName = node.selectSingleNode("product_name").getText();
                if (node.selectSingleNode("product_description") != null) {
                    vendorDesc = node.selectSingleNode("product_description").getText();
                }
                mLogger.createLogDebug("Node vendorName=" + vendorName);
                mLogger.createLogDebug("Node vendorDesc=" + vendorDesc);
                vendorNameDescMap.put(vendorName, vendorDesc);
            }
        }
        return vendorNameDescMap;
    }

    /**
     * The method returns the dealers information for locale.
     * @param serviceType 
     *
     * @param RequestContext
     *            , locale
     * @return List of Dealers info with respect to country region city
     */
    public List<Dealer> retrieveAllDealersforLocale(String locale, String vendorType,
            String searchText, String regionSel, String citySel, List vendorNameList, String serviceType) throws DAOException {
        List<Dealer> resultLocCountry = dataManager.checkAndRetrieveByLocale(locale, vendorType, searchText, regionSel, citySel, vendorNameList, serviceType);
        mLogger.createLogInfo(resultLocCountry.size() + " dealers found.");
        return resultLocCountry;
    }

    /**
     * The method returns the dealers information for selected country.
     * @param serviceType 
     *
     * @param RequestContext
     *            , Country selected
     * @return List of Dealers info with respect to country region city
     */
    public List<Dealer> retrieveAllDealersforCountry(String country, String vendorType,
            String searchText, String regionSel, String citySel, List vendorNameList, String locale, String serviceType) throws DAOException {
        List<Dealer> resultCountry = dataManager.checkAndRetrieveByCountry(country, vendorType, searchText, regionSel, citySel, vendorNameList, locale, serviceType);
        return resultCountry;
    }

    private void populateDealersIntoResult(Element root, List<Dealer> dealerList) {
        for (Iterator i = dealerList.iterator(); i.hasNext();) {
            Dealer dealerInfo = (Dealer) i.next();

            String dealerName = dealerInfo.getDealername();
            String dealerLink = dealerInfo.getDcrlink();
            String pinCode = (dealerInfo.getPincode() != null) ? (dealerInfo.getPincode().replaceAll("\\\\r\\\\n", "<br/>")) : "";
            String contactPhone = (dealerInfo.getContact() != null) ? dealerInfo.getContact() : "";
            String contactFax = (dealerInfo.getFax() != null) ? dealerInfo.getFax() : "";
            String vendorName = dealerInfo.getVendorName();
            String URL = dealerInfo.getURL();  //added on 20140327
            String serviceType = dealerInfo.getServiceType(); //added on 20141208
            String openingHours = dealerInfo.getOpeningHours(); //added on 20141208

	        Element dealerEle = root.addElement("Dealer");
	
	        dealerEle.addAttribute("Name", getValidStringValue(dealerName));
	        dealerEle.addAttribute("PinCode", getValidStringValue(pinCode));
	        dealerEle.addAttribute("Contact", getValidStringValue(contactPhone));
	        dealerEle.addAttribute("Fax", getValidStringValue(contactFax));
	        dealerEle.addAttribute("Type", getValidStringValue(vendorName));
	        dealerEle.addAttribute("URL", getValidStringValue(URL));  //added on 20140327
	        dealerEle.addAttribute("ServiceType", getValidStringValue(serviceType)); //added on 20141208
	        dealerEle.addAttribute("OpeningHours", getValidStringValue(openingHours)); //added on 20141208
	
	        dealerEle.addAttribute("dcrpath", dealerLink);
        }
    }

    private String getValidStringValue(String input) {
        if (input == null) {
            return "";
        } else {
            return input;
        }
    }

    private Element checkAddRegions(Element root, String vendorType, String locale, String countrySel) {

        List<String> filteredlistOfRegions = new ArrayList<String>();

        mLogger.createLogInfo("About to check if regions exist for vendor type");
        List objects = dataManager.checkForVendorsGroupedByRegions(vendorType, locale, countrySel);
        if (CollectionUtils.isNotEmpty(objects)) {
            mLogger.createLogDebug(objects.size() + " such regions found.");
            for (Iterator it = objects.iterator(); it.hasNext();) {
                String row = (String) it.next();
                if (row != null) {
                    mLogger.createLogDebug(row + " has " + vendorType + "s.");
                    filteredlistOfRegions.add(row);
                }
            }
        }
        return addNode(filteredlistOfRegions, root, "Region");
    }

    private Element checkAddCity(Element root, String vendorType, String locale, String countrySel, String regionSel) {

        List<String> filteredlistOfCities = new ArrayList<String>();

        mLogger.createLogInfo("About to check if city exist for vendor type");
        List objects = dataManager.checkForVendorsGroupedByCity(vendorType, locale, countrySel, regionSel);
        if (CollectionUtils.isNotEmpty(objects)) {
            mLogger.createLogDebug(objects.size() + " such city found.");
            for (Iterator it = objects.iterator(); it.hasNext();) {
                String row = (String) it.next();
                if (row != null) {
                    mLogger.createLogDebug(row + " has " + vendorType + "s.");
                    filteredlistOfCities.add(row);
                }
            }
        }
        return addNode(filteredlistOfCities, root, "City");
    }
    
    //Added 20141211
    private Element checkAddServiceType(Element root, String vendorType, String locale, String serviceType){
    	List<String > filteredlistOfServiceTypes = new ArrayList<String>();
    	
    	mLogger.createLogInfo("About to check if service types exist for vendor type");
    	List objects = dataManager.checkForVendorsGroupedByServiceType(vendorType, locale, serviceType);
    	if (CollectionUtils.isNotEmpty(objects)) {
            mLogger.createLogDebug(objects.size() + " such service types found.");
            for (Iterator it = objects.iterator(); it.hasNext();) {
                String row = (String) it.next();
                if (row != null) {
                    mLogger.createLogDebug(row + " has " + serviceType + "s.");
                    filteredlistOfServiceTypes.add(row);
                }
            }
        }
    	return addNode(filteredlistOfServiceTypes, root, "ServiceType");
    }
}
