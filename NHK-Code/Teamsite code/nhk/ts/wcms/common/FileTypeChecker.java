package nhk.ts.wcms.common;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;

/**
 * @author Interwoven CSO
 * Base class to check TS and custom file type
 *
 *
 */
public class FileTypeChecker {

    private static Logger log = new Logger(LogFactory.getLog("nhk.ts.wcms.common.FileTypeChecker"));
    private static final String DCR_TYPE_EA = IOHelper.getString("FileTypeChecker.dcrTypeEAKey");
    private static final String LIVESITE_PAGE_EA = IOHelper.getString("FileTypeChecker.livesitePageEAKey");
    private static final String LIVESITE_PAGE_EA_VALUE = IOHelper.getString("FileTypeChecker.livesitePageEAValue");
    private static final String HTML_EXTENSION = ".html";

    /**
     * Checks if the specified file is a DCR
     *
     * @param csfile
     * @return true if the file is a DCR
     */
    public static boolean isDcr(CSFile file) {
        boolean rval = false;
        if (file instanceof CSSimpleFile) {
            CSSimpleFile csfile = (CSSimpleFile) file;
            try {
                if (csfile.getExtendedAttribute(DCR_TYPE_EA).getValue() != null
                        && !csfile.getExtendedAttribute(DCR_TYPE_EA).getValue().equals("")) {
                    rval = true;
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", npe);
            } catch (CSRemoteException csre) {
                csre.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", csre);
            } catch (CSObjectNotFoundException csonf) {
                csonf.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", csonf);
            } catch (CSExpiredSessionException csex) {
                csex.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", csex);
            } catch (CSException cse) {
                cse.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", cse);
            }
        }
        return rval;
    }

    /**
     * Checks if the specified fiie is a Page
     * @param currentFile
     * @param client
     * @return
     * @throws CSAuthorizationException
     * @throws CSExpiredSessionException
     * @throws CSRemoteException
     * @throws CSException
     */
    public static boolean isPageFile(CSFile currentFile, CSClient client)
            throws CSAuthorizationException, CSExpiredSessionException,
            CSRemoteException, CSException {
        CSFile file = client.getFile(new CSVPath(currentFile.getName()));
        try {
            if (file instanceof CSSimpleFile) {
                CSSimpleFile pageFile = (CSSimpleFile) file;
                String pageEA = pageFile.getExtendedAttribute(LIVESITE_PAGE_EA).getValue();
                if (StringUtils.equalsIgnoreCase(LIVESITE_PAGE_EA_VALUE, pageEA)) {
                    return true;
                }
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            //log.createLogError("Error in FiletypeChecker isDcr() method", npe);
        } catch (CSRemoteException csre) {
            csre.printStackTrace();
            //log.createLogError("Error in FiletypeChecker isDcr() method", csre);
        } catch (CSObjectNotFoundException csonf) {
            csonf.printStackTrace();
            //log.createLogError("Error in FiletypeChecker isDcr() method", csonf);
        } catch (CSExpiredSessionException csex) {
            csex.printStackTrace();
            //log.createLogError("Error in FiletypeChecker isDcr() method", csex);
        } catch (CSException cse) {
            cse.printStackTrace();
            //log.createLogError("Error in FiletypeChecker isDcr() method", cse);
        }
        return false;
    }

    public static boolean isCSSimpleFile(CSFile file, CSClient client) throws CSException {
        CSSimpleFile simpleFile = (CSSimpleFile) client.getFile(file.getVPath());
        if (simpleFile != null && simpleFile.isValid()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValid(CSFile file) {
        if (file.isValid()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isHtmlFile(CSFile currentFile) throws CSAuthorizationException, CSObjectNotFoundException, CSExpiredSessionException, CSRemoteException, CSException {

        boolean result = false;
        String fileName = currentFile.getName();
        int lastDot = fileName.lastIndexOf('.');
        String extension = (lastDot > 0 ? fileName.substring(lastDot) : "");
        if (extension.equalsIgnoreCase(HTML_EXTENSION)) {
            result = true;
        }
        return result;
    }

    /**
     * Checks if the specified file is a product DCR or in other words DCR-type contains "saleable_product_information".
     *
     * @param csfile
     * @return true if the file is a product DCR
     */
    public static boolean isProductDcr(CSFile file) {
        boolean rval = false;
        if (file instanceof CSSimpleFile) {
            CSSimpleFile csfile = (CSSimpleFile) file;
            try {
                if (csfile.getExtendedAttribute(DCR_TYPE_EA).getValue() != null
                        && !csfile.getExtendedAttribute(DCR_TYPE_EA).getValue().equals("") && csfile.getExtendedAttribute(DCR_TYPE_EA).getValue().contains("saleable_product_information")) {
                    rval = true;
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", npe);
            } catch (CSRemoteException csre) {
                csre.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", csre);
            } catch (CSObjectNotFoundException csonf) {
                csonf.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", csonf);
            } catch (CSExpiredSessionException csex) {
                csex.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", csex);
            } catch (CSException cse) {
                cse.printStackTrace();
                log.createLogError("Error in FiletypeChecker isDcr() method", cse);
            }
        }
        return rval;
    }
}
