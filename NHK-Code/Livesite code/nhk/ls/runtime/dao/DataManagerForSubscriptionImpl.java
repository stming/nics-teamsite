/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ls.runtime.dao;

import nhk.ls.runtime.common.Logger;

import com.interwoven.livesite.dao.DataAccessObject;
import com.interwoven.livesite.dao.SearchArguments;
import com.interwoven.livesite.external.ExternalUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.servlet.RequestUtils;

import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;


import org.apache.commons.logging.LogFactory;

/**
 *
 * @author wxiaoxi
 */
public class DataManagerForSubscriptionImpl implements DataManagerForSubscription {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.dao.DataManagerForSubscriptionImpl"));
    private RequestContext context;
    private DataAccessObject dao;
    private static final String DB_POOL_LIVESITE = "livesite";

    public DataManagerForSubscriptionImpl(RequestContext context) {
        super();
        this.context = context;
        this.dao = RequestUtils.getDaoFactory().getInstance(SubscriptionData.class);
    }

    public SubscriptionData retrieveSubscriptionByEmail(String email) throws DAOException {
        return (SubscriptionData) this.dao.findById(email);
    }

    public List<SubscriptionData> retrieveSubscriptioByArg(String argName, String argValue) throws DAOException {
        List args = ExternalUtils.createBaseSearchArguments(this.context);
        args.clear();
        args.add(SearchArguments.eq(argName, argValue));


        mLogger.createLogDebug(args.toString());
        return this.dao.findBySearchArguments(args);
    }

    public String ExecuteQuery(String sqlQuery) {
        String resultString = null;

        Connection conn = RequestUtils.getConnection(DB_POOL_LIVESITE);
        ResultSet result = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            result = stmt.executeQuery();
            conn.commit();

            while (result.next()) {
                resultString = result.getString(1);
            }

            stmt.close();
            conn.close();

        } catch (SQLException e) {
            mLogger.createLogWarn("Error in ExecuteQuery::", e);
        } finally {
            return resultString;
        }


    }
}
