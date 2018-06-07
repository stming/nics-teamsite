package nhk.ts.wcms.workflow;

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
import org.apache.log4j.Logger;
import java.sql.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class DeployDealerFromXLS implements CSURLExternalTask {

	private static final Logger logger = Logger.getLogger(DeployDealerFromXLS.class);
	private static Connection con = null;
	private static String db_uri = "";
	private static String db_account = "";
	private static String db_password = "";
	private static String transition_comment = "";

    public void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
    	
    	transition_comment = "";
    	//1. read DB definition
    	String target_db = task.getVariable("target");
    	logger.debug("Target DB is "+target_db);
    	read_db_parameter(target_db);
    	
    	//2. Get DB connection
    	con = getConnection();
    	
    	if(con!=null){   		
    		
    		//3. read XLS files and insert to DB
    		CSAreaRelativePath[] xlsFilePaths = task.getFiles();
    		for (CSAreaRelativePath xlsFilePath : xlsFilePaths) {
    			String xlsRelFilePathStr = xlsFilePath.toString();
    			String xlsFullFilePathStr = task.getArea().getVPath() + "/" + xlsRelFilePathStr;
    			insertDBFromXLS(xlsFullFilePathStr);
    		}
        
    		//4. close DB connection
    		closeConnection();
    	}
        
    	if("".equals(transition_comment)) task.chooseTransition("Done", "Generated DCR Successfully");
    	else task.chooseTransition("Fail", transition_comment);

    }
    
    private void closeConnection(){
		try{
			if(con!=null) con.close();
			con = null;
		}catch(Exception e){
			logger.error("Error occurs in closeConnection() : "+e.getMessage());
			transition_comment = "Error in closeConnection()";
		}
	}
    
    private Connection getConnection(){
		Connection con = null;
		try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection(db_uri+";user="+db_account+";password="+db_password+";");   			
			if(con==null){
				logger.error("Error occurs in getConnection()");
				transition_comment = "Error in getConnection()";
			}
		}catch(Exception e){
			logger.error("Error occurs in getConnection() : "+e.getMessage());
			transition_comment = "Error in getConnection()";
		}
		return con;
	}
    
    private void read_db_parameter(String target_db){
		try {
			File file = new File("D:\\Interwoven\\OpenDeployNG\\etc\\livesite-database.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("database");
	
			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					if(target_db.equals(fstElmnt.getAttribute("name"))){
						db_uri = "jdbc:sqlserver://" + fstElmnt.getAttribute("db");
						db_account = fstElmnt.getAttribute("user");
						db_password = fstElmnt.getAttribute("password");
						logger.debug("DB informaiton => "+db_uri+" "+db_account+" "+db_password);
					}
				}
			}
		}catch (Exception e){
			logger.error("Error occurs in read_db_parameter() : "+e.getMessage());
			transition_comment = "Error in read_db_parameter()";
		}
	}

    public void insertDBFromXLS(String ipfile) {
    	logger.debug("Input XLS file: "+ipfile);
        FileInputStream fs = null;
        WorkbookSettings ws = null;
        Workbook workbook = null;
        Sheet s = null;
        Statement stmt = null;
        String str_sql = "";
        try {
        	//clear Dealer table
    		stmt = con.createStatement();
			str_sql = "delete from Dealer";
			stmt.execute(str_sql);
        }catch (Exception e) {
        	logger.error("Error occurs in cleaning Dealer table: "+e.getMessage());
			transition_comment = "Error occurs in cleaning Dealer table";
        }
        
        try {
            fs = new FileInputStream(ipfile);
            ws = new WorkbookSettings();
            workbook = Workbook.getWorkbook(fs, ws);
            s = workbook.getSheet(0);

            Cell[] requiredcol1 = s.getColumn(0); //dealername
            Cell[] requiredcol2 = s.getColumn(2); //vendor_type
            Cell[] requiredcol3 = s.getColumn(3); //Country
            Cell[] requiredcol4 = s.getColumn(4); //Region
            Cell[] requiredcol5 = s.getColumn(6); //Pincode
            Cell[] requiredcol6 = s.getColumn(7); //Contact
            Cell[] requiredcol7 = s.getColumn(9); //Locale
            Cell[] requiredcol8 = s.getColumn(11); //sort_order
            Cell[] requiredcol9 = s.getColumn(12); //URL   // added on 20140401
            Cell[] requiredcol10 = s.getColumn(13); // service_type added on 20141208
            Cell[] requiredcol11 = s.getColumn(14); // opening_hours added on 20141208

            
            PreparedStatement pst = null;
            str_sql = "insert into Dealer values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";   //modified on 20141208
            pst = con.prepareStatement(str_sql);
            logger.debug("Total rows = "+requiredcol1.length);
            for (int i = 1; i < requiredcol1.length; i++) {
            	logger.debug("name = "+requiredcol1[i].getContents());
            	pst.setString(1, requiredcol1[i].getContents());
            	pst.setNull(2, java.sql.Types.VARCHAR);
            	pst.setString(3, requiredcol2[i].getContents());
            	pst.setString(4, requiredcol3[i].getContents());
            	pst.setString(5, requiredcol4[i].getContents());
            	pst.setNull(6, java.sql.Types.VARCHAR);
            	pst.setString(7, requiredcol5[i].getContents());
            	pst.setString(8, requiredcol6[i].getContents());
            	pst.setString(9, "dummy_dcr_link_"+i);
            	pst.setString(10, requiredcol7[i].getContents());
            	pst.setNull(11, java.sql.Types.VARCHAR);
            	pst.setInt(12, Integer.parseInt(requiredcol8[i].getContents()));
            	
            	/*Cell[] rowCells = s.getRow(i);   //added on 20141028 for empty cell exception
            	if(rowCells.length<13){
            		pst.setString(13, "");
            	}else{
            		pst.setString(13, requiredcol9[i].getContents());     // added on 20140401
            	}*/
            	
            	//added on 20141208 for empty cell exception
            	Cell[] rowCells = s.getRow(i);
            	if(rowCells.length < 13){
            		pst.setString(13, "");
            		pst.setString(14, "");
            		pst.setString(15, "");
            	}else if(rowCells.length < 14){
            		pst.setString(13, requiredcol9[i].getContents());
            		pst.setString(14, "");
            		pst.setString(15, "");
            	}else if(rowCells.length < 15){
            		pst.setString(13, requiredcol9[i].getContents());
            		pst.setString(14, requiredcol10[i].getContents());
            		pst.setString(15, "");
            	}else{
            		pst.setString(13, requiredcol9[i].getContents());
            		pst.setString(14, requiredcol10[i].getContents());
            		pst.setString(15, requiredcol11[i].getContents());
            	}
            	            	
                pst.executeUpdate();                
            }
            workbook.close();
        } catch (Exception e) {
        	logger.error("Error occurs in inserting rows to Dealer table: "+e.getMessage());
			transition_comment = "Error occurs in inserting rows to Dealer table: "+e.getMessage();
        }
    }
}
