package nhk.ls.runtime.dao;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.interwoven.livesite.dao.DataAccessObject;
import com.interwoven.livesite.hibernate.dao.impl.BaseHibernateDataAccessObject;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.servlet.RequestUtils;

import nhk.ls.runtime.common.Logger;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class DealerDataManagerImpl implements DealerDataManager {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.dao.DealerDataManagerImpl"));
    private RequestContext context;

    public DealerDataManagerImpl(RequestContext context) {
        super();
        this.context = context;
    }

    public List<Dealer> checkAndRetrieveByCountry(String countryName, String vendorType, String searchText, String regionSel, String citySel, List vendorNameList, String locale, String serviceType) throws DAOException {
        mLogger.createLogInfo("Inside checkAndRetrieveByCountry countyNm is " + countryName);
        mLogger.createLogInfo("Inside checkAndRetrieveByCountry vendorType is " + vendorType);
        DataAccessObject DealerDao = RequestUtils.getDaoFactory().getInstance(Dealer.class);

        BaseHibernateDataAccessObject baseHiberateDataObject = (BaseHibernateDataAccessObject) DealerDao;
        Criteria criteria = baseHiberateDataObject.createCriteria(Dealer.class);

        criteria.add(Restrictions.eq("country", countryName));
        criteria.add(Restrictions.eq("vendorType", vendorType));

        if (StringUtils.isNotEmpty(locale)) {
            if (locale.equalsIgnoreCase("en_Asia")) {
                criteria.add(Restrictions.ne("locale", "en_ME"));
            } else {
                criteria.add(Restrictions.eq("locale", locale));
            }
        }

        if (vendorNameList != null) {
            criteria.add(Restrictions.in("vendorName", vendorNameList));
        }
        
        if (StringUtils.isNotEmpty(regionSel)) {
        	if(StringUtils.isNotEmpty(serviceType)){
        		criteria.add(Restrictions.and(Restrictions.eq("region", regionSel), Restrictions.eq("serviceType", serviceType)));
        	}
            criteria.add(Restrictions.eq("region", regionSel));
        }

        if (StringUtils.isNotEmpty(citySel)) {
            criteria.add(Restrictions.eq("city", citySel));
        }
        
        if (StringUtils.isNotEmpty(serviceType)){
        	if(StringUtils.isNotEmpty(regionSel)){
        		criteria.add(Restrictions.and(Restrictions.eq("region", regionSel), Restrictions.eq("serviceType", serviceType)));
        	}
        	criteria.add(Restrictions.eq("serviceType", serviceType));
        }

        if (StringUtils.isNotEmpty(searchText)) {
            searchText = "%" + searchText + "%";
            criteria.add(Restrictions.disjunction().add(Restrictions.ilike("pincode", searchText)).add(
                    Restrictions.ilike("dealername", searchText)));
        }

        criteria.addOrder(Order.asc("sortOrder"));
        criteria.addOrder(Order.asc("dealername"));

        mLogger.createLogDebug("Included like query now!!" + criteria.toString());
        List<Dealer> resultCountry = criteria.list();
        return resultCountry;
    }

    public List<Dealer> checkAndRetrieveByLocale(String locale, String vendorType, String searchText, String regionSel, String citySel, List vendorNameList, String serviceType) {
        mLogger.createLogInfo("Inside checkAndRetrieveByLocale locale is " + locale);
        mLogger.createLogInfo("Inside checkAndRetrieveByLocale vendorType is " + vendorType);
        DataAccessObject DealerDao = RequestUtils.getDaoFactory().getInstance(Dealer.class);
        BaseHibernateDataAccessObject baseHiberateDataObject = (BaseHibernateDataAccessObject) DealerDao;
        Criteria criteria = baseHiberateDataObject.createCriteria(Dealer.class);

        if (StringUtils.isNotEmpty(locale)) {
            if (locale.equalsIgnoreCase("en_Asia")) {
                criteria.add(Restrictions.ne("locale", "en_ME"));
            } else {
                criteria.add(Restrictions.eq("locale", locale));
            }
        }

        criteria.add(Restrictions.eq("vendorType", vendorType));

        if (vendorNameList != null) {
            criteria.add(Restrictions.in("vendorName", vendorNameList));
        }

        if (StringUtils.isNotEmpty(regionSel)) {
        	if(StringUtils.isNotEmpty(serviceType)){
        		criteria.add(Restrictions.and(Restrictions.eq("region", regionSel), Restrictions.eq("serviceType", serviceType)));
        	}
            criteria.add(Restrictions.eq("region", regionSel));
        }

        if (StringUtils.isNotEmpty(citySel)) {
            criteria.add(Restrictions.eq("city", citySel));
        }
        
        if (StringUtils.isNotEmpty(serviceType)){
        	if(StringUtils.isNotEmpty(regionSel)){
        		criteria.add(Restrictions.and(Restrictions.eq("region", regionSel), Restrictions.eq("serviceType", serviceType)));
        	}
        	criteria.add(Restrictions.eq("serviceType", serviceType));
        }

        if (StringUtils.isNotEmpty(searchText)) {
            searchText = "%" + searchText + "%";
            criteria.add(Restrictions.disjunction().add(Restrictions.ilike("pincode", searchText)).add(
                    Restrictions.ilike("dealername", searchText)));
        }

        criteria.addOrder(Order.asc("sortOrder"));
        criteria.addOrder(Order.asc("dealername"));

        mLogger.createLogDebug("Included like query now!!" + criteria.toString());
        List<Dealer> resultLocCountry = criteria.list();

        return resultLocCountry;
    }

    public List checkForVendorsGroupedByCountries(String vendorType, String locale) {

        List results = null;
        try {

            mLogger.createLogInfo("Inside checkForVendorsGroupedByCountries vendorType is " + vendorType);
            DataAccessObject DealerDao = RequestUtils.getDaoFactory().getInstance(Dealer.class);
            BaseHibernateDataAccessObject baseHiberateDataObject = (BaseHibernateDataAccessObject) DealerDao;
            Criteria criteria = baseHiberateDataObject.createCriteria(Dealer.class);

            if (StringUtils.isNotEmpty(locale)) {
                if (locale.equalsIgnoreCase("en_Asia")) {
                    criteria.add(Restrictions.ne("locale", "en_ME"));
                } else {
                    criteria.add(Restrictions.eq("locale", locale));
                }
            }

            criteria.add(Restrictions.eq("vendorType", vendorType));

            ProjectionList projList = Projections.projectionList();
            projList.add(Projections.groupProperty("country"));
            //projList.add(Projections.rowCount());
            criteria.addOrder(Order.asc("country"));

            criteria.setProjection(projList);
            mLogger.createLogDebug("Included like query now!!" + criteria.toString());
            results = criteria.list();

            /*
            if (CollectionUtils.isNotEmpty(results)) {
            for (Iterator it = results.iterator(); it.hasNext();) {
            Object object = it.next();
            mLogger.createLogDebug(object.toString());
            }
            }
             */
        } catch (Exception e) {
            mLogger.createLogWarn("Error found in checkForVendorsGroupedByCountries:", e);
        }
        return results;
    }

    public List checkForVendorsGroupedByVendorName(String vendorType, String locale, String countrySel, String regionSel, String citySel) {

        List results = null;
        try {

            mLogger.createLogInfo("Inside checkForVendorsGroupedByCountries vendorType is " + vendorType);
            DataAccessObject DealerDao = RequestUtils.getDaoFactory().getInstance(Dealer.class);
            BaseHibernateDataAccessObject baseHiberateDataObject = (BaseHibernateDataAccessObject) DealerDao;
            Criteria criteria = baseHiberateDataObject.createCriteria(Dealer.class);

            criteria.add(Restrictions.eq("vendorType", vendorType));

            if (StringUtils.isNotEmpty(locale)) {
                if (locale.equalsIgnoreCase("en_Asia")) {
                    criteria.add(Restrictions.ne("locale", "en_ME"));
                } else {
                    criteria.add(Restrictions.eq("locale", locale));
                }
            }
            if (StringUtils.isNotEmpty(countrySel)) {
                criteria.add(Restrictions.eq("country", countrySel));
            }

            if (StringUtils.isNotEmpty(regionSel)) {
                criteria.add(Restrictions.eq("region", regionSel));
            }
            if (StringUtils.isNotEmpty(citySel)) {
                criteria.add(Restrictions.eq("city", citySel));
            }

            ProjectionList projList = Projections.projectionList();
            projList.add(Projections.groupProperty("vendorName"));
            //projList.add(Projections.rowCount());

            criteria.addOrder(Order.asc("vendorName"));

            criteria.setProjection(projList);
            mLogger.createLogDebug("Included like query now!!" + criteria.toString());
            results = criteria.list();
        } catch (Exception e) {
            mLogger.createLogWarn("Error found in checkForVendorsGroupedByCountries:", e);
        }
        return results;
    }

    public List checkForVendorsGroupedByRegions(String vendorType, String locale, String countrySel) {
        List results = null;
        try {

            mLogger.createLogInfo("Inside checkForVendorsGroupedByCountries vendorType is " + vendorType);
            DataAccessObject DealerDao = RequestUtils.getDaoFactory().getInstance(Dealer.class);
            BaseHibernateDataAccessObject baseHiberateDataObject = (BaseHibernateDataAccessObject) DealerDao;
            Criteria criteria = baseHiberateDataObject.createCriteria(Dealer.class);

            criteria.add(Restrictions.eq("vendorType", vendorType));

            if (StringUtils.isNotEmpty(locale)) {
                if (locale.equalsIgnoreCase("en_Asia")) {
                    criteria.add(Restrictions.ne("locale", "en_ME"));
                } else {
                    criteria.add(Restrictions.eq("locale", locale));
                }
            }
            if (StringUtils.isNotEmpty(countrySel)) {
                criteria.add(Restrictions.eq("country", countrySel));
            }

            ProjectionList projList = Projections.projectionList();
            projList.add(Projections.groupProperty("region"));
            //projList.add(Projections.rowCount());

            criteria.addOrder(Order.asc("region"));

            criteria.setProjection(projList);
            mLogger.createLogDebug("Included like query now!!" + criteria.toString());
            results = criteria.list();
        } catch (Exception e) {
            mLogger.createLogWarn("Error found in checkForVendorsGroupedByCountries:", e);
        }
        return results;
    }

    public List checkForVendorsGroupedByCity(String vendorType, String locale, String countrySel, String regionSel) {
        List results = null;
        try {

            mLogger.createLogInfo("Inside checkForVendorsGroupedByCountries vendorType is " + vendorType);
            DataAccessObject DealerDao = RequestUtils.getDaoFactory().getInstance(Dealer.class);
            BaseHibernateDataAccessObject baseHiberateDataObject = (BaseHibernateDataAccessObject) DealerDao;
            Criteria criteria = baseHiberateDataObject.createCriteria(Dealer.class);

            criteria.add(Restrictions.eq("vendorType", vendorType));

            if (StringUtils.isNotEmpty(locale)) {
                if (locale.equalsIgnoreCase("en_Asia")) {
                    criteria.add(Restrictions.ne("locale", "en_ME"));
                } else {
                    criteria.add(Restrictions.eq("locale", locale));
                }
            }

            if (StringUtils.isNotEmpty(countrySel)) {
                criteria.add(Restrictions.eq("country", countrySel));
            }

            if (StringUtils.isNotEmpty(regionSel)) {
                criteria.add(Restrictions.eq("region", regionSel));
            }

            ProjectionList projList = Projections.projectionList();
            projList.add(Projections.groupProperty("city"));
            //projList.add(Projections.rowCount());

            criteria.addOrder(Order.asc("city"));

            criteria.setProjection(projList);
            mLogger.createLogDebug("Included like query now!!" + criteria.toString());
            results = criteria.list();

        } catch (Exception e) {
            mLogger.createLogWarn("Error found in checkForVendorsGroupedByCountries:", e);
        }
        return results;
    }

    //Added 20141211
	public List checkForVendorsGroupedByServiceType(String vendorType,
			String locale, String serviceType) {
		// TODO Auto-generated method stub
		List results = null;
		try{
			mLogger.createLogInfo("Inside checkForVendorsGroupedByServiceType vendorType is " + vendorType);
			 DataAccessObject DealerDao = RequestUtils.getDaoFactory().getInstance(Dealer.class);
	         BaseHibernateDataAccessObject baseHiberateDataObject = (BaseHibernateDataAccessObject) DealerDao;
	         Criteria criteria = baseHiberateDataObject.createCriteria(Dealer.class);
	         
	         criteria.add(Restrictions.eq("vendorType", vendorType));

	            if (StringUtils.isNotEmpty(locale)) {
	                if (locale.equalsIgnoreCase("en_Asia")) {
	                    criteria.add(Restrictions.ne("locale", "en_ME"));
	                } else {
	                    criteria.add(Restrictions.eq("locale", locale));
	                }
	            }

	            if (StringUtils.isNotEmpty(serviceType)) {
	                criteria.add(Restrictions.eq("serviceType", serviceType));
	            }

	            ProjectionList projList = Projections.projectionList();
	            projList.add(Projections.groupProperty("serviceType"));
	            //projList.add(Projections.rowCount());

	            criteria.addOrder(Order.asc("serviceType"));

	            criteria.setProjection(projList);
	            mLogger.createLogDebug("Included like query now!!" + criteria.toString());
	            results = criteria.list();

	        } catch (Exception e) {
	            mLogger.createLogWarn("Error found in checkForVendorsGroupedByServiceType:", e);
	        }
		return results;
	}
}
