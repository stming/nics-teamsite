package nhk.ls.runtime.dao;

import java.util.List;

/**
 *
 * @author Anu
 */
public interface DealerDataManager {

    public List<Dealer> checkAndRetrieveByCountry(String countryName, String vendorType, String searchText, String regionSel, String citySel, List vendorNameList, String locale, String serviceType) throws DAOException;

    public List<Dealer> checkAndRetrieveByLocale(String locale, String vendorType, String searchText, String regionSel, String citySel, List vendorNameList, String serviceType);

    public List checkForVendorsGroupedByCountries(String vendorType, String locale);

    public List checkForVendorsGroupedByVendorName(String vendorType, String locale, String countrySel, String regionSel, String citySel);

    public List checkForVendorsGroupedByRegions(String vendorType, String locale, String countrySel);

    public List checkForVendorsGroupedByCity(String vendorType, String locale, String countrySel, String regionSel);
    
    public List checkForVendorsGroupedByServiceType(String vendorType, String locale, String serviceType);
}
