package com.interwoven.teamsite.nikon.dealerfinder.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

public class Importer
{
	Logger oLogger = LoggerFactory.getLogger(this.getClass());
	private int uniqueID = 1;
	
	public static String DB_SERVER;
	public static String DB_NAME;
	public static String DB_USER;
	public static String DB_PASS;
	
	public void execute( String filename ) throws Exception
	{
		// Props file configuration
		Configuration propsFile = null;
		
		try {
			
			propsFile = new PropertiesConfiguration("props/db.properties");
			
			DB_SERVER = propsFile.getString("DB_SERVER");
			DB_NAME = propsFile.getString("DB_NAME");
			DB_USER = propsFile.getString("DB_USER");
			DB_PASS = propsFile.getString("DB_PASS");
			
		} catch (ConfigurationException e) {
			
			e.printStackTrace();
		}
		
		FileInputStream fstream = new FileInputStream( filename );
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader ( new InputStreamReader (in, "UTF8") );
		CsvListReader reader = new CsvListReader(br, CsvPreference.STANDARD_PREFERENCE);
		String strline ;
		
		oLogger.info("Open connection");
		Connection con = DriverManager.getConnection("jdbc:sqlserver://"+DB_SERVER+";database="+DB_NAME+";", DB_USER, DB_PASS); 
		con.setAutoCommit(false); 
		
//		con.createStatement().execute("SET IDENTITY_INSERT dealer ON");
		
		oLogger.info("Prepare statement");
		PreparedStatement mainPS = con.prepareStatement("INSERT INTO dealer " +
				   "(" +
				   "innerid, " +
		           "name, " +
		           "description, " +
		           "street, " +
		           "post_code, " +
		           "town, " +
		           "state, " +
		           "country, " +
		           "country_code, " +		           
		           "tel, " +
		           "fax, " +
		           "email, " +
		           "url, " +
		           "opening_hours, " +
		           "lon, " +
		           "lat, " +
		           "author, " +
		           "modified_date, " +
		           "status, " +
		           "abf, " +
		           "prod, " +
		           "user_group) " + 
		           "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		
		PreparedStatement cmpPS = con.prepareStatement("INSERT INTO cmp " +
				   "(id, " +
				   "cmpadr, " +
		           "cmpadr2, " +
		           "cmpadr3, " +
		           "cmpadr4, " +
		           "cmpadr5) " + 
		           "VALUES (?,?,?,?,?,?)");
		
		PreparedStatement additionalPS = con.prepareStatement("INSERT INTO additional " +
				   "(pk, " +
				   "id, " +
				   "fieldID, " +
		           "fieldValue) " + 
		           "VALUES (?,?,?,?)");
		
		//strline = br.readLine();
		List<String> strLine = reader.read();
		int line=0;
		while (strLine != null)
		{
			line++;
			String[] cells = strLine.toArray(new String[strLine.size()]);
			oLogger.info("Line " + line + ", array length: " + cells.length + ", dealer: "+ cells[3]);
			if (cells.length >= 71){
				uniqueID = insertMain( mainPS, cells );
				if (uniqueID != -1){
					insertCMP(cmpPS, cells);
					insertAdditional(additionalPS, cells);
				}
			}else{
				oLogger.error("Skipping because cell does not match " + cells[3]);
			}
			
			//uniqueID++;
			strLine = reader.read();
			//strline = br.readLine();
		}
		
		//in.close();
		
		oLogger.info("Commit");
		
		reader.close();
		mainPS.close(); 
    	cmpPS.close();
		con.commit();    	
    	con.close();
    	
	}
	
	// cells
    /* 0 = client_id (X)	16 = fax		32 = c13	48 = c29	64 = c45
     * 1 = innercode		17 = email		33 = c14	49 = c30	65 = c46
     * 2 = name				18 = url		34 = c15	50 = c31	66 = c47
     * 3 = description		19 = c00		35 = c16	51 = c32	67 = c48
     * 4 = street			20 = c01		36 = c17	52 = c33	68 = c49
     * 5 = pc				21 = c02		37 = c18	53 = c34	69 = opening_hours
     * 6 = town				22 = c03		38 = c19	54 = c35	70 = lon
     * 7 = state			23 = c04		39 = c20	55 = c36	71 = lat
     * 8 = country			24 = c05		40 = c21	56 = c37	72 = storelocation
     * 9 = countrycode		25 = c06		41 = c22	57 = c38	73 = src_email
     * 10 = cmpadr			26 = c07		42 = c23	58 = c39	74 = icon
     * 11 = cmpadr2			27 = c08		43 = c24	59 = c40	75 = logo
     * 12 = cmpadr3			28 = c09		44 = c25	60 = c41	76 = id
     * 13 = cmpadr4			29 = c10		45 = c26	61 = c42	77 = GeoQuality
     * 14 = cmpadr5			30 = c11		46 = c27	62 = c43	78 = GeocodeSource
     * 15 = tel				31 = c12		47 = c28	63 = c44	79 = GeocodeDate
     */
	private int insertMain( PreparedStatement ps, String[] cells ) throws Exception
    {
		ps.clearParameters();
		
		if ( cells[1] != null && cells[1].equals( "client_id") )
        {
        	oLogger.info("Header row");
        	return -1;
        }
		oLogger.info("Insert dealer - " + cells[3]);
        
        // fill innercode to countrycode
        int sqlColNumber = 1;
        
//        oLogger.debug( "Setting " + uniqueID + " to " + sqlColNumber );
//        ps.setInt(sqlColNumber++, uniqueID );
        
        int cellNumber = 2;
        while ( cellNumber <= 10 )
        {
        	oLogger.debug( "Setting " + cells[cellNumber] + " to " + sqlColNumber );
        	ps.setString(sqlColNumber++, removeQuotes(cells[cellNumber++]) );
        }
        
        // tel to email
        cellNumber = 16;
        while ( cellNumber <= 19 )
        {
        	oLogger.debug( "Setting " + cells[cellNumber] + " to " + sqlColNumber );
        	ps.setString(sqlColNumber++, removeQuotes(cells[cellNumber++]) );
        }
        
        // opening hours to lat
        cellNumber = 69;
        while ( cellNumber <= 71 )
        {
        	oLogger.debug( "Setting " + cells[cellNumber] + " to " + sqlColNumber );
        	ps.setString(sqlColNumber++, removeQuotes(cells[cellNumber++]) );
        }

        oLogger.debug( "Setting SYSTEM to " + sqlColNumber );
        ps.setString(sqlColNumber++, "SYSTEM" );
        oLogger.debug( "Setting TIME to " + sqlColNumber );
        ps.setLong(sqlColNumber++, (long) (new Date()).getTime() );
        oLogger.debug( "Setting Publish to " + sqlColNumber );
        ps.setString(sqlColNumber++, "Publish" );
        oLogger.debug( "Setting 0 to " + sqlColNumber );
        ps.setInt(sqlColNumber++, 0 );
        oLogger.debug( "Setting 0 to " + sqlColNumber );
        ps.setInt(sqlColNumber++, 0 );
        oLogger.debug( "Setting " + "dealers_" + cells[10] + " to " + sqlColNumber );
        ps.setString(sqlColNumber++ , "dealers_" + cells[10]);
     
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }
	
	private void insertCMP( PreparedStatement ps, String[] cells ) throws Exception
    {
		ps.clearParameters();
		
		if ( cells[1] != null && cells[1].equals( "client_id") )
        {
        	oLogger.info("Header row");
        	return;
        }
		oLogger.info("Insert cmp for dealer - " + cells[3]);
        
		int sqlColNumber = 1;
        
        oLogger.debug( "Setting " + uniqueID + " to " + sqlColNumber );
        ps.setInt(sqlColNumber++, uniqueID );
        
        int cellNumber = 11;
        boolean dataFound = false;
        while ( cellNumber <= 15 )
        {
        	oLogger.debug( "Setting " + cells[cellNumber] + " to " + sqlColNumber );
        	if ( ! ( cells[cellNumber] == null || cells[cellNumber].equals("") || cells[cellNumber].equals(" ")) )
        	{
        		dataFound = true;
        	}
        	ps.setString(sqlColNumber++, removeQuotes(cells[cellNumber++]) );
        }
        
        if ( dataFound )
        {
        	ps.execute();
        }
    }
	
	private void insertAdditional( PreparedStatement ps, String[] cells ) throws Exception
    {
		ps.clearParameters();
		
		if ( cells[1] != null && cells[1].equals( "client_id") )
        {
        	oLogger.info("Header row");
        	return;
        }
		oLogger.info("Insert additional for dealer - " + cells[3]);
        
		int cellNumber = 20;

        while ( cellNumber <= 69 )
        {
        	if ( ! ( cells[cellNumber] == null || cells[cellNumber].equals("") || cells[cellNumber].equals(" ") || cells[cellNumber].equals("0.0") || cells[cellNumber].equals("0") ) ) 
        	{
        		oLogger.info( "Setting " + uniqueID + " to 1" );
	        	ps.setString(1, uniqueID + "__" + (cellNumber - 19) );
	        	oLogger.info( "Setting " + uniqueID + " to 2" );
	        	ps.setInt(2, uniqueID );
	        	oLogger.info( "Setting " + (cellNumber - 19) + " to 3" );
	            ps.setInt(3, (cellNumber - 19) );
	            oLogger.info( "Setting " + cells[cellNumber] + " to 4" );
				if (removeQuotes(cells[cellNumber]).equals("1.0") || removeQuotes(cells[cellNumber]).equals("1") )
				{
					ps.setString(4, "true");
				}
				else
				{
					ps.setString(4, "false");
				}

	            
	            ps.addBatch();
        	}
        	cellNumber++;
        }
        
        ps.executeBatch();
    }
	
	private String removeQuotes( String input )
	{
		if (input == null) {
			return "";
		}else{
			return input.replaceFirst("^\"(.*)\"$", "$1");
		}
	}

}
