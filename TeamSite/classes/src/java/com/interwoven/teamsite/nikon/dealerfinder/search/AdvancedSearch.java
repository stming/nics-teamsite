package com.interwoven.teamsite.nikon.dealerfinder.search;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mike
 */
public class AdvancedSearch extends AbstractSearch
{

	static final Logger oLogger = LoggerFactory.getLogger(AdvancedSearch.class);

    // These will need to correspond to the Objet properties
    // that are mapped to columns in the dealer table
    public enum DealerCol
    {
		// note these map to OBJECT properties, not column names for HQL 
        name, description, street, town, state, country, postCode, author, from, to, status 
    }
    
    Map<DealerCol, String> mConstraints = new HashMap<DealerCol, String>();
    
    
    public void addConstraint(DealerCol eDealerCol, String sValue)
    {
        mConstraints.put(eDealerCol, sValue);
    }
    
    public Map<DealerCol, String> getConstraints()
    {
        return mConstraints;
    }

    public String generateCombinedConstraint()
    {
        StringBuilder oWhereBuffer = new StringBuilder(); 
        
        for (DealerCol eDealerCol : mConstraints.keySet())
        {
            String sValue = mConstraints.get(eDealerCol);
            
			oLogger.info("Constraint: " + eDealerCol.toString() + ": " + sValue);
			
            if (sValue != null)
            {
                String sColName = eDealerCol.toString();
				String sConstraint = "";
				
				if (sColName.equals("description"))
				{
					sValue = "%" +sValue+ "%";
					sConstraint = String.format("dealer.%s like \'%s\' ", sColName, sValue);
				}
				else if (sColName.equals("from"))
				{
					sConstraint = String.format("dealer.modifiedDate > %d ", Long.parseLong(sValue));
				}
				else if (sColName.equals("to"))
				{
					sConstraint = String.format("dealer.modifiedDate < %d ", Long.parseLong(sValue));
				}
				else
				{
					sValue = "%" +sValue+ "%";
					sConstraint = String.format("(upper(dealer.%s) LIKE upper(\'%s\'))", sColName, sValue);
				}
				
				oLogger.info("Constraint: " + sConstraint);
	
                if (oWhereBuffer.toString().length() > 0)
                {
                    oWhereBuffer.append(" AND ").append(sConstraint);             
                }
                else 
                { 
                    oWhereBuffer.append(sConstraint);
                }
            
            }
        }
        
        return oWhereBuffer.toString();
    }
}
