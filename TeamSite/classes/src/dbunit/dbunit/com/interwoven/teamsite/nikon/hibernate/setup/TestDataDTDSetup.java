package dbunit.com.interwoven.teamsite.nikon.hibernate.setup;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatDtdDataSet;

import junit.framework.TestCase;

public class TestDataDTDSetup extends TestCase {

	public TestDataDTDSetup(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//TODO Parameterise these values
	public void testCreateDTD()
	throws Exception
	{
        // database connection
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Connection jdbcConnection = DriverManager.getConnection("jdbc:sqlserver://EMEANBAMFORDXPPL03:1360;databaseName=HIBERNATE", "sa", "Datascat511");
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        // write DTD file
        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("./classes/src/dbunit/Nikon-BV-DBSchema.dtd"));
		
	}
}
