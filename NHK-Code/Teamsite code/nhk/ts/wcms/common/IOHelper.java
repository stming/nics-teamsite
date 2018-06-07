package nhk.ts.wcms.common;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;
import com.interwoven.serverutils100.InstalledLocations;
import org.apache.commons.logging.LogFactory;

public class IOHelper {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.common.IOHelper"));
    private static final String BUNDLE_NAME = "nhk.ts.wcms.common.nikon";
    private static final String PROPERTYFILE = InstalledLocations.getIWHome() + Constants.separator + "Nikon" + Constants.separator + "nikon.properties";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public static String getString(String key) {
        try {
            String tmp = RESOURCE_BUNDLE.getString(key);
            if (!StringUtils.isBlank(tmp)) {
                return tmp;
            } else {
                throw new Exception("Key[" + key + "] does not exist in property file " + BUNDLE_NAME);
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error is getString::", e);
            return Constants.BLANK;
        }
    }

    public static String getCommonPropertyValue(String key) {
        try {
            Properties properties = loadParamsFromDisk(Constants.nikonCommonPropFile);
            String tmp = properties.getProperty(key);
            if (!StringUtils.isBlank(tmp)) {
                return tmp;
            } else {
                throw new Exception("Key[" + key + "] does not exist in property file " + Constants.nikonCommonPropFile);
            }
        } catch (Exception e) {
            return Constants.BLANK;
        }
    }

    public static String getPropertyValue(String key) {
        try {
            Properties properties = loadParamsFromDisk(PROPERTYFILE);
            String tmp = properties.getProperty(key);
            if (!StringUtils.isBlank(tmp)) {
                return tmp;
            } else {
                throw new Exception("Key[" + key + "] does not exist in property file " + PROPERTYFILE);
            }
        } catch (Exception e) {
            mLogger.createLogDebug("Error is getString::", e);
            return "";
        }
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
            mLogger.createLogDebug("Error is getString::", ex);
        }
        return prop;
    }
}
