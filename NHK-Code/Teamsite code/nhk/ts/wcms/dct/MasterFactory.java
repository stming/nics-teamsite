package nhk.ts.wcms.dct;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.factory.CSFactory;
import com.interwoven.cssdk.factory.CSJavaFactory;
import com.interwoven.serverutils100.InstalledLocations;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;

/**
 * MasterFactory allows the user to create a new CSClinet Object.
 *
 */
public class MasterFactory {

    private static CSClient trustedClient = null;
    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.dct.MasterFactory"));
    private static final String PROPERTYFILE = InstalledLocations.getIWHome() + File.separator + "Nikon" + File.separator + "master.properties";

    /**
     * This method creates a new client with the credentials provided in
     * $iw_home\custom\cssdk\master.properties file. If the requesting host
     * is a desktop PC it creates the client with password otherwise it
     * creates the client without password using  getClientForTrustedUser()
     * @param iw_home Location of TeamSite home directory
     * @return CSClient object
     */
    public static CSClient getNewClient() {
        CSFactory factory = null;

        Properties masterProp = null;
        String username = "", role = "";

        //read master user credentials
        try {
            masterProp = loadParamsFromDisk(PROPERTYFILE);
            factory = CSJavaFactory.getFactory(masterProp);
        } catch (Exception iEx) {
            mLogger.createLogDebug("Error in getNewClient::", iEx);
        }
        try {
            username = masterProp.getProperty("masterUsername");
            role = masterProp.getProperty("masterRole");
            trustedClient = factory.getClientForTrustedUser(username, role, Locale.getDefault(), "custMasterClient", null);
        } catch (CSAuthorizationException csAEx) {
            mLogger.createLogDebug("Error in getNewClient::", csAEx);
        } catch (CSRemoteException csREx) {
            mLogger.createLogDebug("Error in getNewClient::", csREx);
        } catch (CSException csEx) {
            mLogger.createLogDebug("Error in getNewClient::", csEx);
        }
        return trustedClient;
    }

    /**
     * Returns the master client object from the application context memory.
     * If the client is null or invalid or expiration time is less than 15 minutes
     * a  new CSClient Object created and set to application context.
     */
    public static CSClient getMasterClient() throws Exception {
        try {
            if (trustedClient == null || !trustedClient.isValid()) {
                trustedClient = getNewClient();
            }
            Date currTime = new Date();
            //get current client expiration time
            long expTime = trustedClient.getExpirationDate().getTime() - currTime.getTime();

            //Reset client if its about to expire in 15 minutes
            if (expTime < 15) {
                //invalidate the current client
                trustedClient.endSession();
                //create a new client
                trustedClient = getNewClient();
            }
        } catch (Exception ex) {
            mLogger.createLogDebug("Error in getMasterClient::", ex);
            throw ex;
        }
        return trustedClient;

    }

    public static Properties loadParamsFromDisk(String file) throws Exception {
        Properties prop = new Properties();
        try {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            prop.load(fis);
            fis.close();
            if (prop == null) {
                mLogger.createLogDebug("Property file[" + file + "] could not be loaded or does not exist!");
            }
        } catch (Exception ex) {
            mLogger.createLogDebug("Error in loadParamsFromDisk", ex);
        }
        return prop;
    }
}
