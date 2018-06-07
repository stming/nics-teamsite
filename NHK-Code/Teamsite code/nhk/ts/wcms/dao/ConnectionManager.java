package nhk.ts.wcms.dao;

/**
 *
 * @author sbhojnag
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import nhk.ts.wcms.common.IOHelper;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible to parse the database.xml from the OpenDeploy and translate and retrieve the JDBC Connection String.
 * Currently only work with microsoft-db in database.xml setting
 *
 */
public class ConnectionManager {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.dao.ConnectionManager"));
    private static final String STAGING_CONNECTION_URL = "ConnectionManager.StagingDBConnectionURL";
    private static final String STAGING_CONNECTION_USER = "ConnectionManager.StagingDBConnectionUser";
    private static final String STAGING_CONNECTION_PASSWORD = "ConnectionManager.StagingDBConnectionPassword";
    private static final String PRODUCTION_CONNECTION_URL = "ConnectionManager.ProductionDBConnectionURL";
    private static final String PRODUCTION_CONNECTION_USER = "ConnectionManager.ProductionDBConnectionUser";
    private static final String PRODUCTION_CONNECTION_PASSWORD = "ConnectionManager.ProductionDBConnectionPassword";

    public static Connection getStagingConnection() throws DAOException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(IOHelper.getPropertyValue(STAGING_CONNECTION_URL), IOHelper.getPropertyValue(STAGING_CONNECTION_USER), IOHelper.getPropertyValue(STAGING_CONNECTION_PASSWORD));
        } catch (ClassNotFoundException e) {
            mLogger.createLogDebug("Error in getConnection::", e);
            throw new DAOException("Fail to load the microsoft JDBC driver, please check classpath", e);
        } catch (SQLException e) {
            mLogger.createLogDebug("Error in getConnection::", e);
            throw new DAOException("Fail to create connection", e);
        }
    }

    public static Connection getProductionConnection() throws DAOException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(IOHelper.getPropertyValue(PRODUCTION_CONNECTION_URL), IOHelper.getPropertyValue(PRODUCTION_CONNECTION_USER), IOHelper.getPropertyValue(PRODUCTION_CONNECTION_PASSWORD));
        } catch (ClassNotFoundException e) {
            mLogger.createLogDebug("Error in getConnection::", e);
            throw new DAOException("Fail to load the microsoft JDBC driver, please check classpath", e);
        } catch (SQLException e) {
            mLogger.createLogDebug("Error in getConnection::", e);
            throw new DAOException("Fail to create connection", e);
        }
    }
}
