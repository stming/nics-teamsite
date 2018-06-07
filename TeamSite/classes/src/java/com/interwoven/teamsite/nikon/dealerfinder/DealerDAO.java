package com.interwoven.teamsite.nikon.dealerfinder;

import com.interwoven.teamsite.nikon.dealerfinder.HibernateUtil;
import com.interwoven.teamsite.nikon.dealerfinder.exceptions.DAOException;
import com.interwoven.teamsite.nikon.dealerfinder.search.AdvancedSearch;
import com.interwoven.teamsite.nikon.dealerfinder.search.BasicSearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


import org.hibernate.HibernateException;
import org.hibernate.Query;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates all dealer-based data actions.
 *
 * @author Mike
 * @version 1.3
 *
 *	1.2 - Merged changes from command line tools (ingest) and webapp
 *  1.3 - Changed status messages to match front end
 */
public class DealerDAO
{

    /**
     * Convert the IDs to a printable list *
     */
    private String printIDs(List<Long> lnIDs)
    {
        String sIDs = null;
        for (Long lnID : lnIDs)
        {
            if (sIDs != null)
            {
                sIDs += ",";
            }
            sIDs += lnID;
        }
        return sIDs;
    }

    private static final String PUBLISH = "publish";
    private static final String DELETE = "delete";
    private static final String DRAFT= "draft";

    static final Logger oLogger = LoggerFactory.getLogger(DealerDAO.class);
    
    public Long createDealer(Dealer oDealer) throws DAOException
    {
        oLogger.info("Saving dealer; name=" + oDealer.getName());
        Session oSession = HibernateUtil.getSessionFactory().openSession();
        Transaction oTransaction = null;
        Long lDealerId = null;
        try
        {
            oTransaction = oSession.beginTransaction();
            //set the state to be DRAFT
            oDealer.setState(DRAFT);
            oDealer.setModifiedDate( new Date().getTime() );
            lDealerId = (Long) oSession.save(oDealer);
            oTransaction.commit();
        }
        catch (HibernateException e)
        {
            oLogger.error("Exception saving dealer: " + e.getMessage() + ": rolling back transaction");
            oTransaction.rollback();
            e.printStackTrace();

            oSession.close();

            throw new DAOException("Exception saving dealer: " + e.getMessage());
        }
        finally
        {
            oSession.close();
        }
        return lDealerId;
    }
    
    @SuppressWarnings("unchecked")
	public Dealer getDealerDetail(long lDealerId) throws DAOException
    {
       oLogger.info("Retrieving dealer (detail view); id=" + lDealerId);
       Dealer oDealer = null;
        Session oSession = HibernateUtil.getSessionFactory().openSession();
       try
        {

            String sHQLQuery = "from Dealer as dealer where dealer.id = \'%s\'";
             
            sHQLQuery = String.format(sHQLQuery, lDealerId);
             
            Query oQuery = oSession.createQuery(sHQLQuery);
            
            List<Dealer> olDealers = oQuery.list();
            
            if (olDealers == null || olDealers.size() == 0)
            {
                oLogger.warn("Could not retrieve Dealer id="+lDealerId);
            }
            else if (olDealers.size() > 1)          
            {
                oLogger.warn("Multiple dealers returned for this ID="+lDealerId);
            }
            else
            {
                oDealer = olDealers.get(0);
                
                // we need to check for additional data fields to make sure these 
                // are retrieved
                Set<AdditionalData> stAddData = oDealer.getAdditionalData();
                
                if (stAddData != null)
                {
                    oLogger.info("Retrieved dealer ID="+lDealerId+"; num. add. data fields = "+stAddData.size());
                }
                else
                {
                    oLogger.info("Retrieved dealer ID="+lDealerId+"; no add. data fields");
                }
            }
            
        }
        catch (HibernateException e)
        {
            oLogger.error("Exception retrieving dealer: " + e.getMessage());
 
            e.printStackTrace();

            oSession.close();

            throw new DAOException("Exception retrieving dealer: " + e.getMessage());
        }
        finally
        {
            oSession.close();
        }
        return oDealer;       
    }
    
    @SuppressWarnings("unchecked")
	public List<Dealer> doBasicSearch(BasicSearch oBasicSearch) throws DAOException
    {
        List<Dealer> olDealers = new ArrayList<Dealer>();
        
        // Construct a SQL query based on the DTO contents,
        // execute and return associated dealer objects
        oLogger.info("Performing basic search; text=" + oBasicSearch.getSearchText() + "; field="+oBasicSearch.getFieldName());
         
        Session oSession = HibernateUtil.getSessionFactory().openSession();
        
        StringBuffer sHQLQueryBuffer = new StringBuffer("from Dealer as dealer where ");
		
	oLogger.info("field name is " + oBasicSearch.getFieldName());
		
        if (oBasicSearch.getFieldName().equals("description"))
        {
            sHQLQueryBuffer.append("dealer.%s like \'%s\' ");
        }
        else
        {
            sHQLQueryBuffer.append("upper(dealer.%s) like upper(\'%s\') ");
        }
        
	oLogger.info("sHQLQueryBuffer is " + sHQLQueryBuffer.toString());
        String sHQLQuery = String.format(sHQLQueryBuffer.toString(), oBasicSearch.getFieldName(), 
                "%" + oBasicSearch.getSearchText() +"%");
        
		
        // Check the groups clause
        String sGroupsClause = oBasicSearch.generateGroupsClause();
        
        if (sGroupsClause != null && sGroupsClause.length() >0)
        {
            sHQLQuery = sHQLQuery + " AND "+ sGroupsClause;
        }
		
		oLogger.info("sHQLQuery is " + sHQLQuery);
        
        try
        {
            Query oQuery = oSession.createQuery(sHQLQuery);
            
            olDealers = oQuery.list();
            
            oLogger.info("Basic search returned "+olDealers.size() + " result(s)");
            
        }
        catch (HibernateException e)
        {
            oLogger.error("Exception performing basic search: " + e.getMessage());
            
            e.printStackTrace();

            oSession.close();

            throw new DAOException("Exception performing basic search: " + e.getMessage());
        }
        finally
        {
            oSession.close();
        }      
        

        return olDealers;
    }

    
    @SuppressWarnings("unchecked")
	public List<Dealer> doAdvancedSearch(AdvancedSearch oAdvSearch) throws DAOException
    {
        List<Dealer> olDealers = new ArrayList<Dealer>();
        // Construct a SQL query based on the DTO contents,
        // execute and return associated dealer objects
        oLogger.info("Performing advanced search; num of constraints=" + oAdvSearch.getConstraints().size());
         
        Session oSession = HibernateUtil.getSessionFactory().openSession();
        
        String sHQLQuery = "from Dealer as dealer where (%s) ";
        
        sHQLQuery = String.format(sHQLQuery, oAdvSearch.generateCombinedConstraint()); 
              
        // Check the groups clause
        String sGroupsClause = oAdvSearch.generateGroupsClause();
        
        if (sGroupsClause != null && sGroupsClause.length() >0)
        {
            sHQLQuery = sHQLQuery + " AND "+ sGroupsClause;
        } 
		
		oLogger.info(sHQLQuery);
		
        try
        {
            Query oQuery = oSession.createQuery(sHQLQuery);
            
            olDealers = oQuery.list();
            
            oLogger.info("Adv search returned "+olDealers.size() + " result(s)");
            
        }
        catch (HibernateException e)
        {
            oLogger.error("Exception performing adv search: " + e.getMessage());
            
            e.printStackTrace();

            oSession.close();

            throw new DAOException("Exception performing adv search: " + e.getMessage());
        }
        finally
        {
            oSession.close();
        }
        
        return olDealers;
    }

    public List<Dealer> listAllDealers() throws DAOException
    {
    	Session oSession = HibernateUtil.getSessionFactory().openSession();
    	
    	List<Dealer> dealers = listAllDealers( oSession );
    	
    	oSession.close();
    	
    	return dealers;
    }
    
    // NOTE may need extension for ordering etc
    @SuppressWarnings("unchecked")
	public List<Dealer> listAllDealers( Session oSession ) throws DAOException
    {
        oLogger.info("listing all dealers");
        
        List<Dealer> olDealers = new ArrayList<Dealer>();
        try
        {
            // this query uses HQL
            // to use standard SQl, use createSQLQuery
            olDealers = oSession.createQuery("from Dealer").list();

        }
        catch (HibernateException e)
        {
            oLogger.error("Exception retrieving all dealers: " + e.getMessage() );
            //oTransaction.rollback();
            e.printStackTrace();

           
            throw new DAOException("Exception when listing dealers: " + e.getMessage());
        }
       
        return olDealers;
    }

    public void deleteDealer(List<Long> olDealerId) throws DAOException
    {
    	Session oSession = HibernateUtil.getSessionFactory().openSession();
    	
    	deleteDealer( oSession, olDealerId );
    	
    	oSession.close();
    }
    
    public void deleteDealer(Session session, List<Long> olDealerId) throws DAOException
    {
        Transaction oTransaction = null;
        try
        {
            oTransaction = session.beginTransaction();
            for (long lDealerId : olDealerId)
            {
                Dealer oDealer = (Dealer) session.get(Dealer.class, lDealerId);
                oDealer.setStatus(DELETE);
                session.update(oDealer);
            }
            oTransaction.commit();
        }
        catch (HibernateException e)
        {
            oTransaction.rollback();
            throw new DAOException("Exception when deleting dealer (id=" + printIDs(olDealerId) + "): " + e.getMessage());
        }
    }

    public void publishDealer(List<Long> olDealerId) throws DAOException
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction oTransaction = null;
        try
        {
            oTransaction = session.beginTransaction();
            for (long lDealerId : olDealerId)
            {
                Dealer oDealer = (Dealer) session.get(Dealer.class, lDealerId);
                oDealer.setStatus(PUBLISH);
                session.update(oDealer);
            }
            oTransaction.commit();
        }
        catch (HibernateException e)
        {
            oTransaction.rollback();
            throw new DAOException("Exception when publishing dealer (id=" + printIDs(olDealerId) + "): " + e.getMessage());
        }
        finally
        {
            session.close();
        }
    }
    
    public void updateDealer(Dealer oDealer) throws DAOException
    {
    	Session oSession = HibernateUtil.getSessionFactory().openSession();
    	
    	// get dealer        
        Dealer currentDealer = getDealerDetail(oDealer.getId() );
        // set the abf, prod and group to original ->
        	// NOTE code only runs from update in UI
        oDealer.setAbfDate( currentDealer.getAbfDate() );
        oDealer.setProdDate( currentDealer.getProdDate() );
        oDealer.setGroup( currentDealer.getGroup() );
        oDealer.setModifiedDate( new Date().getTime() );
    	
    	updateDealer( oSession, oDealer );
    	
    	oSession.close();
    }

    public void updateDealer(Session oSession, Dealer oDealer) throws DAOException
    {
        oLogger.info("Updating dealer: ids=" + oDealer.getId());

        Transaction oTransaction = null;
        try
        {
            oTransaction = oSession.beginTransaction();

            // try merging            
            oSession.merge(oDealer);

            oTransaction.commit();
        }
        catch (HibernateException e)
        {
            oLogger.error("Exception updating dealer: " + e.getMessage() + ": rolling back transaction");
            oTransaction.rollback();
            e.printStackTrace();

            throw new DAOException("Exception when updating dealer (id=" + oDealer.getId() + "): " + e.getMessage());
        }
   
    }

    public void removeDealer(List<Long> olDealerId) throws DAOException
    {
    	Session oSession = HibernateUtil.getSessionFactory().openSession();
    	
    	removeDealer( oSession, olDealerId );
    	
    	oSession.close();
    }
    
    public void removeDealer(Session oSession, List<Long> olDealerId) throws DAOException
    {
        oLogger.info("Deleting dealer: ids=" + printIDs(olDealerId));

        Transaction oTransaction = null;
        try
        {
            oTransaction = oSession.beginTransaction();
            for (long lDealerId : olDealerId)
            {
                Dealer oDealer = (Dealer) oSession.get(Dealer.class, lDealerId);
                oSession.delete(oDealer);
            }
            oTransaction.commit();
        }
        catch (HibernateException e)
        {
            oLogger.error("Exception deleting dealer: " + e.getMessage() + ": rolling back transaction");
            oTransaction.rollback();
            e.printStackTrace();

            throw new DAOException("Exception when deleting dealer (id=" + printIDs(olDealerId) + "): " + e.getMessage());
        }
    }
}
