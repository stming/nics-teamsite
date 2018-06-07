package dbunit.com.interwoven.teamsite.nikon.hibernate.setup;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

public class TestDataDataSetup extends DBTestCase {

	public TestDataDataSetup(String name)
	{
		System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "com.microsoft.sqlserver.jdbc.SQLServerDriver" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:sqlserver://EMEANBAMFORDXPPL03:1360;databaseName=HIBERNATE" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "sa" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "Datascat511" );
	}
	@Override
	protected IDataSet getDataSet() throws Exception {
		// TODO Auto-generated method stub
		IDataSet retSet = null;
		
		try
		{
			URL url = new URL("file:./classes/src/dbunit/Nikon-BV-DBSchema.xml");
			retSet = new FlatXmlDataSet(url.openStream());
		}
		catch(Exception exception)
		{
			System.out.println(exception.getMessage());
		}
		
		return retSet; 
	}
	
	public void testNothing() throws Exception
	{
	}
	
}
