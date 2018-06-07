package nhk.ls.runtime.common;

import java.io.File;

import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.livesite.runtime.RequestContext;

public class Constants {

    public static final String separator = File.separator;
    public static final String BLANK = "";
    public static String delimiter = ",";
    public static String underscore = "_";
    public static String urlPathSep = "$URL";
    public static String filePathSep = "/";

    public static String inValidFileComment(CSHole invalidFile) {
        String comment = "";
        comment = "Dependent file: " + invalidFile.getVPath().toString() + " has not been attached to the workflow because it has been deleted from the WORKAREA";
        return comment;

    }

    public static String getCatFileLocation(RequestContext context) {
        String catFilepath = null;
        if (context.isPreview()) {
            catFilepath = IOHelper.getString("ProductListing.previewCatFilePath");
        }
        if (context.isRuntime()) {
            catFilepath = IOHelper.getString("ProductListing.runtimeCatFilePath");
        }
        return catFilepath;
    }
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
}
