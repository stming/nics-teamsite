package nhk.ts.wcms.workflow.servicestatus;

import java.util.*;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.access.CSUser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.*;
import java.util.Locale;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.DateCell;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;

public class DCRFromXSL implements CSURLExternalTask {

    private static Logger mLogger;

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {

        this.mLogger = new Logger(LogFactory.getLog(this.getClass()));

        contentReading("Y:\\default\\main\\Nikon\\SG\\WORKAREA\\main_wa\\templatedata\\en_SG\\service_status\\waiting_for_pickup_EN.xls", "Y:\\default\\main\\Nikon\\SG\\WORKAREA\\main_wa\\templatedata\\en_SG\\service_status\\data\\waiting_for_pickup_EN.xml");
        contentReading("Y:\\default\\main\\Nikon\\SG\\WORKAREA\\main_wa\\templatedata\\en_SG\\service_status\\repair_in_progress_EN.xls", "Y:\\default\\main\\Nikon\\SG\\WORKAREA\\main_wa\\templatedata\\en_SG\\service_status\\data\\repair_in_progress_EN.xml");
        task.chooseTransition("Done", "Generated DCR Successfully");
    }

    public static void contentReading(String ipfile, String opfile) {
        FileInputStream fs = null;
        WorkbookSettings ws = null;
        Workbook workbook = null;
        Sheet s = null;
        Cell rowData[] = null;
        int rowCount = '0';
        int columnCount = '0';
        DateCell dc = null;
        int totalSheet = 0;
        try {
            fs = new FileInputStream(new File(ipfile));
            ws = new WorkbookSettings();
            ws.setLocale(new Locale("en", "EN"));
            workbook = Workbook.getWorkbook(fs, ws);
            s = workbook.getSheet(0);
            Cell[] requiredcol1 = s.getColumn(6);
            Cell[] requiredcol2 = s.getColumn(5);
            Cell[] requiredcol3 = s.getColumn(5);
            Cell[] requiredcol4 = s.getColumn(35);
            FileOutputStream out = new FileOutputStream(opfile);
            PrintStream p = new PrintStream(out);
            p.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            p.print("<Content>");
            for (int i = 1; i < requiredcol1.length; i++) {
                p.print("<ServiceStatus>");
                p.print("<ServiceOrder>");
                p.print(requiredcol1[i].getContents());
                p.print("</ServiceOrder>");
                p.print("<ProductSerial>");
                p.print(requiredcol2[i].getContents());
                p.print("</ProductSerial>");
                p.print("<ServiceSerial>");
                p.print(requiredcol3[i].getContents());
                p.print("</ServiceSerial>");
                p.print("<ServiceStatus>");
                p.print(requiredcol4[i].getContents());
                p.print("</ServiceStatus>");
                p.print("</ServiceStatus>");
            }
            p.print("</Content>");
            p.close();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            mLogger.createLogDebug("Error to creating the xmlDoc from Excel sheet:", e);
        }
    }
}
