/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nhk.ls.runtime.dao;

import java.util.List;

/**
 *
 * @author wxiaoxi
 */
public interface DataManagerForSubscription {

    public SubscriptionData retrieveSubscriptionByEmail(String email) throws DAOException;

    public String ExecuteQuery(String sqlQuery);

    public List <SubscriptionData> retrieveSubscriptioByArg(String argName, String argValue) throws DAOException;


}
