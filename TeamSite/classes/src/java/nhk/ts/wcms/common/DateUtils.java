package nhk.ts.wcms.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.LogFactory;

public class DateUtils {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm";
    public static final String DATE_FORMAT_DATEONLY = "yyyy-MM-dd";
    public static final String DATE_FORMAT = "yyyy-MM-dd kk:mm";
    public static final String DATE_FORMAT_WITH_SECONDS = "yyyy-MM-dd kk:mm:ss";
    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.common.DateUtils"));

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public static Date currentdatetime() {
        Date currentdate = null;
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            String currentdatetime = sdf.format(cal.getTime());
            currentdate = (Date) sdf.parse(currentdatetime);
        } catch (Exception e) {
            mLogger.createLogWarn("Error is currentDateTime", e);
        }
        return currentdate;
    }

    public static String getDateString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static Date getDate(String dateStr, String format) {
        Date dateObj = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            dateObj = (Date) sdf.parse(dateStr);
        } catch (Exception e) {
            mLogger.createLogWarn("Error is currentDateTime", e);
        }
        return dateObj;
    }

    public static int getDaysDifference(Date ealierDate, Date laterDate) {
        int val = (int) Math.floor((laterDate.getTime() - ealierDate.getTime()) / (1000 * 24 * 60 * 60));
        return val;
    }
}
