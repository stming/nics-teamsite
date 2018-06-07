package nhk.ts.wcms.common;

import java.io.File;

import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.serverutils100.InstalledLocations;

public class Constants {

    public static final String separator = File.separator;
    public static final String catFileLocation = InstalledLocations.getIWHome() + separator + "tmp" + separator + "NHKLS" + separator;
    public static final String BLANK = "";
    public static String UNDERSCRORE = "_";
    public static String delimiter = ",";
    public static String hyphen = "-";
    public static String EQUAL = "=";
    public static String DOUBLE_BACKWARD_SLASH = "\\";
    public static String DOUBLE_FORWARD_SLASH = "//";
    public static String BREAK = "<br/>";
    public static String PARAMETER = "-k";
    public static String iwodcmdStart = "start";
    public static String filePathSep = "/";
    public static String urlPathSep = "$URL";
    public static String newLine = "\n";
    public static String defaultSelectValue = "----Select----";
    public static String iwTempFolderPath = TSHelper.getIWHome() + filePathSep + "tmp";
    public static String customFolderPath = TSHelper.getIWHome() + filePathSep + "Nikon";
    public static String nikonCommonPropFile = customFolderPath + filePathSep + "nikon_common.properties";
    public static String iwfCopy_Publish_HTTPDFolder = TSHelper.getIWHome() + "/httpd/iw/nikon_custom/workflow";
    public static String iwfPropFilePath = TSHelper.getIWHome() + "/httpd/iw/nikon_custom/workflow/aa_default.properties";
    public static int ODFailureValue = 1;
    public static String jugLib = customFolderPath + "/lib/jug-native";
    //mail constants
    public static String MAIL_ENCODING = "UTF-8";
    public static String MAIL_MIME_TYPE = "text/html";
    public static String XML_MIME_TYPE = "text/xml";
    public static String subjJobIdOpen = "<";
    public static String subjJobId = "JobID:";
    public static String subjJobIdClose = ">";
    public static String htmlExtention = "html";
    public static String workarea = "WORKAREA";
    public static String htmlBaseDir = "html";
    public static String startTag = "<Page_Display_Properties Class=\"com.interwoven.livesite.model.page.PageProperties\">";
    public static String endTag = "</Page_Display_Properties>";
    //OD
    public static final String DEPLOYMENT_TEMP_DIRECTORY = customFolderPath + filePathSep + "WorkFlowDeployment";
    public static final String DEPLOYMENT_PROPERTY_FILE = customFolderPath + filePathSep + "opendeploy.properties";
    public static final String DEPLOYMENT_SUCCESS_STATUS = "Completed";
    public static final String DEPLOYMENT_RUNNING_STATUS = "Running";
    public static final String DEPLOYMENT_TIMEOUT_STATUS = "Read timed out";
    public static final String DEPLOYMENT_Failed_STATUS = "Failed";
    public static final String DEPLOYMENT_ERROR_STATUS1 = "ERROR";
    public static final String DEPLOYMENT_ERROR_STATUS2 = "Error";
    // Deployment Stuff
    public static final String REPLICATION_FARM = "REPLICATIONFARMNAME";
    public static final String SOURCE_AREA = "sourceArea";
    public static final String FILELIST = "fileList";
    public static final String TARGET_AREA = "targetArea";
    public static final String submitCommand = "bin/iwsubmit";
    public static final String submitOverwriteFlag = "w";
    public static final String submitUnlockFlag = "u";
    public static final String submitComment = "File Submitted";

    public static String inValidFileComment(CSHole invalidFile) {
        String comment = "";
        comment = "Dependent file: " + invalidFile.getVPath().toString() + " has not been attached to the workflow because it has been deleted from the WORKAREA";
        return comment;
    }
}
