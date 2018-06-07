package com.interwoven.teamsite.nikon.data;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;

import com.interwoven.teamsite.ext.hibernate.manager.AbstractTSEnvAwareHibernateDAOManager;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.hibernate.beans.Award;
import com.interwoven.teamsite.nikon.hibernate.beans.AwardTestimonial;
import com.interwoven.teamsite.nikon.hibernate.beans.PressLibrary;
import com.interwoven.teamsite.nikon.hibernate.beans.Price;
import com.interwoven.teamsite.nikon.hibernate.beans.Product;
import com.interwoven.teamsite.nikon.hibernate.manager.NikonHBN8DAOManagerTxn;

public class ProductManagerImpl extends AbstractTSEnvAwareHibernateDAOManager implements ProductManager {

	private Log log = LogFactory.getLog(NikonHBN8DAOManagerTxn.class);
	private static Log staticLog = LogFactory.getLog(NikonHBN8DAOManagerTxn.class);
	
	private static Constructor<ProductDTO> ProductDTOConstructorCache;
	
	static {
		try {
			ProductManagerImpl.ProductDTOConstructorCache = ProductDTO.class.getConstructor(new Class[] {Product.class});
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}		
		
	}
	public List<String> retrieveAllAvailableLocale() throws DataAccessException {
		Session session = null; 
		List<String> localeList = new ArrayList<String>();
		try {
			session = this.getSession();
			session.beginTransaction();
			Criteria criteria = session.createCriteria(Product.class).
				setProjection(Projections.distinct(Projections.property("nikonLocale"))).
				addOrder(Order.asc("nikonLocale"));
			localeList = criteria.list();
		}catch (Exception e){
			log.error(e);
			throw new DataAccessException("Error occur while trying to retrieve all available locale in product tables", e);
		} finally {
			if (session != null){
				session.flush();
				session.close();
			}
		}
		return localeList;
	}

	public List<Product> retrieveProductsByLocale(String locale) throws DataAccessException {
		Session session = null;
		List<Product> result = new ArrayList<Product>();
		try {
			session = this.getSession();
			session.beginTransaction();
			result = session.createCriteria(Product.class).
				add(Restrictions.eq("nikonLocale", locale)).
				addOrder(Order.asc("productCategory")).list();
		} catch (Exception e){
			log.error(e);
			throw new DataAccessException("Error occur while trying to retrieve product by locale", e);			
		} finally {
			if (session != null){
				session.flush();
				session.close();
			}
		}
		return result;
	}

}
