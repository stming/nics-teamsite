package com.interwoven.teamsite.ext.hibernate.manager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Abstract class to define what we need in a Hibernate DAO Manager
 * @author neal bamford
 *
 */
public abstract class AbstractHibernateDAOManager {

	protected SessionFactory sessionFactory;

	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}
}
