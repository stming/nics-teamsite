package nhk.ls.runtime.common;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

public class IOHelper {

    private static final Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.common.IOHelper"));
    private static final String BUNDLE_NAME = "nhk.ls.runtime.common.nikon";
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
            mLogger.createLogWarn("Error in getString", e);
            return Constants.BLANK;
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
            mLogger.createLogWarn("Error in getString", ex);
        }
        return prop;
    }
}
