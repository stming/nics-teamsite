package nhk.ts.wcms.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

public class CommonUtil {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.common.CommonUtil"));

    public static String replaceDCRPathWithLocale(String oldFilePath, String baseLocale, String locale) {
        return StringUtils.replace(oldFilePath, baseLocale, locale);
    }

    /**
     * Custom toStirng() function to extract items in a set as comma separated values in a String object
     * @param set
     * @return
     */
    public static String getStringFromSet(Set set) {
        String setString = set.toString();
        setString = setString.substring(setString.indexOf("[") + 1, setString.indexOf("]"));
        return setString;
    }

    /**
     * Creates a {@link Matcher} object for the specified source string using the specified regex
     * @param srcString - Source string
     * @param regex - Regular expression to be applied on the source string
     * @return {@link Matcher}
     */
    public static Matcher getPatternMatcher(String srcString, String regex) {
        Matcher matcher = null;
        Pattern pattern = Pattern.compile(regex);
        if (pattern != null) {
            matcher = pattern.matcher(srcString);
        }
        return matcher;
    }

    public static String convertDateFormat(String text, String inputFormat, String outputFormat) {
        String result = text;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
            Date inputDate = sdf.parse(text);
            SimpleDateFormat changedFormat = new SimpleDateFormat(outputFormat);
            result = changedFormat.format(inputDate);

        } catch (ParseException pex) {
            mLogger.createLogDebug("Error faced in converting date format::", pex);
        }
        return result;
    }


}
