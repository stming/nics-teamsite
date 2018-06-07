package nhk.ts.wcms.workflow.servicestatus;

import java.util.*;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;
import java.io.*;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.DateCell;
import nhk.ts.wcms.common.IOHelper;
import nhk.ts.wcms.common.Logger;
import org.apache.commons.logging.LogFactory;

public class DCRFromXLS implements CSURLExternalTask {

    private static Logger mLogger = new Logger(LogFactory.getLog("nhk.ts.wcms.workflow.servicestatus.DCRFromXLS"));
    private static int productIdx = Integer.parseInt(IOHelper.getPropertyValue("ServiceStatusWF.ProductSN.ColumnIndex")); //8
    private static int servOrderIdx = Integer.parseInt(IOHelper.getPropertyValue("ServiceStatusWF.ServiceOrder.ColumnIndex")); //10
    private static int servStatusIdx = Integer.parseInt(IOHelper.getPropertyValue("ServiceStatusWF.ServiceStatus.ColumnIndex")); //39

    @SuppressWarnings("unchecked")
    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {

        CSAreaRelativePath[] xlsFilePaths = task.getFiles();
        for (CSAreaRelativePath xlsFilePath : xlsFilePaths) {

            String xlsRelFilePathStr = xlsFilePath.toString();
            String xlsFullFilePathStr = task.getArea().getVPath() + "/" + xlsRelFilePathStr;

            generateXML(xlsFullFilePathStr);

        }
        task.chooseTransition("Done", "Generated DCR Successfully");
    }

    public static void generateXML(String ipfile) {
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
            fs = new FileInputStream(ipfile);
            ws = new WorkbookSettings();
            //    ws.setLocale(new Locale("en", "EN"));
            workbook = Workbook.getWorkbook(fs, ws);
            s = workbook.getSheet(0);
            /*    Cell[] requiredcol1 = s.getColumn(1);
            Cell[] requiredcol2 = s.getColumn(3);
            Cell[] requiredcol3 = s.getColumn(7);
            Cell[] requiredcol4 = s.getColumn(5); */

            Cell[] requiredcol1 = s.getColumn(servOrderIdx);
            Cell[] requiredcol2 = s.getColumn(productIdx);
            Cell[] requiredcol3 = s.getColumn(productIdx);
            Cell[] requiredcol4 = s.getColumn(servStatusIdx);

            String targetFileName = ipfile.substring(ipfile.lastIndexOf("/") + 1, ipfile.lastIndexOf("."));
            String opfile = ipfile.substring(0, ipfile.lastIndexOf("/")) + "/data/" + targetFileName + ".xml";

            mLogger.createLogDebug("Target generateXML filename:" + opfile);

            FileOutputStream out = new FileOutputStream(opfile);
            PrintStream p = new PrintStream(out, true, "UTF-8");
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
            mLogger.createLogDebug("Error contentReading::", e);
        }
    }
}
