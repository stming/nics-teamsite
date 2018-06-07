package com.nikon.utils;


import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.*;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.servlet.RequestUtils;
import java.sql.*;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

public class SQLCSNikon
{
	
    private static Log LOG = LogFactory.getLog(SQLCSNikon.class);

    public Document get(RequestContext context)
    {
        // create new document
        Document doc = Dom4jUtils.newDocument("<external/>");

        String pool = context.getParameterString("Pool");
        if(null == pool)
        {
                return ExternalUtils.newErrorDocument("executeSqlQuery: Parameter 'pool' not found.");
        }

        // check we've got a stored procedure
        String sqlStatement = context.getParameterString( "cs" );
        if ( null == sqlStatement )
        {
                return ExternalUtils.newErrorDocument("executeSqlQuery: Parameter 'cs' not found.");
        }

        Element siteElem = doc.getRootElement().addElement("site");
        siteElem.addAttribute("name", context.getSite().getName() );
        siteElem.addAttribute( "path", context.getSite().getPath() );
        siteElem.addAttribute( "branch", context.getSite().getBranch() );

        sqlStatement = getSQLStatement( context.getParameters(), sqlStatement );

        Document resultSets = runCS( pool, sqlStatement );

        doc.getRootElement().add( resultSets.getRootElement().detach() );

        return doc;
    }

    private String getSQLStatement( ParameterHash params, String sqlStatement )
    {
        // get iterator of params
        Iterator it = params.keySet().iterator();
        while ( it.hasNext() )
        {
            String key = it.next().toString();

            Object value = params.get( key );

            // prepare the parameters
            if ( value == null )
            {
                    // no param for this found so forget it
            }
            else if(value instanceof ArrayList)
            {
                 ArrayList vals = (ArrayList) value;
                 String newString = "";
                 for ( int i = 0; i < vals.size(); i++ )
                 {
                         if ( i > 0 )
                         {
                                 newString += ",";
                         }
                         newString += (String) vals.get( i );
                 }
                 sqlStatement = sqlStatement.replaceFirst( ( "\\{" + key + "\\}" ), "'" + newString + "'" );
            }
            else if (! ( value.toString().equalsIgnoreCase("Pool") || value.toString().equalsIgnoreCase("cs") ) )
            {
                sqlStatement = sqlStatement.replaceFirst( ( "\\{" + key + "\\}" ), "'" + value.toString() + "'" );
            }
        }

        // replace any unmatched params with nulls
        sqlStatement = sqlStatement.replaceAll( "\\{(.*?)\\}", "null" );
        sqlStatement = "{call " + sqlStatement + "}";

        return sqlStatement;
    }

    protected Document runCS (String pool, String sqlStatement)
    {
        // declare document for return
        Document doc = Dom4jUtils.newDocument( "<ResultSets/>" );
        doc.getRootElement().addAttribute( "version", "1.1" );
        doc.getRootElement().addAttribute( "sqlStatement", sqlStatement );

        Connection conn = null;
        CallableStatement cs = null;

        // Try to connect to the database
        conn = RequestUtils.getConnection(pool);

	try
        {
            // Execute the sql query and put the results in a resultset
            cs = conn.prepareCall(sqlStatement);

	    LOG.debug("SQL Statement: " + sqlStatement);

            // execute stored procedure
            if ( cs.execute() )
            {
		 LOG.debug("SQL Statement: Executed");

                do
                {
			LOG.debug("SQL Statement: Found Results");

                    ResultSet rs = cs.getResultSet();

                    Element resultSet = doc.getRootElement().addElement("ResultSet");

                    ResultSetMetaData rsmd = rs.getMetaData();

                    ArrayList columnNames = new ArrayList();

                    // Get the column names; column indices start from 1
                    for (int i=1; i<rsmd.getColumnCount()+1; i++)
                    {
                        String columnName = rsmd.getColumnName(i);
			LOG.debug("SQL Statement: Column Name: " + columnName);
                        columnNames.add( columnName );
                    }

                    // Loop through the result set
                    while ( rs.next() )
                    {
                        Element row = resultSet.addElement("Row");

                        for ( int j = 0; j < columnNames.size(); j++ )
                        {
                            Element columnNode = row.addElement( columnNames.get( j ).toString() );
                            String val = rs.getString( columnNames.get(j).toString() );
				LOG.debug("SQL Statement: Column Name: " + columnNames.get( j ).toString() + "- Value: " + val);

                            if ( val != null && ! val.equals("") )
                            {
                                columnNode.addCDATA( val );
                            }

                        }
                    }
                } while (cs.getMoreResults() != false);
            }
        }
        catch(SQLException e)
        {
            // TODO - add error note
            ExternalUtils.newErrorDocument("SQLException error: " + e.toString() );
        }
        catch( Exception e )
        {
            ExternalUtils.newErrorDocument("Error: " + e.toString() );
        }
        finally
        {
            // Close all open connections etc.
            if(cs != null)
            {
                try
                {
                    cs.close();
                }
                catch(SQLException e)
                {
                    ExternalUtils.newErrorDocument("SQLException error while closing: " + e.toString() );
                }
            }
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(SQLException e)
                {
                    ExternalUtils.newErrorDocument("SQLException error while closing: " + e.toString() );
                }
            }
        }

        // Return the results
        return doc;
    }
}