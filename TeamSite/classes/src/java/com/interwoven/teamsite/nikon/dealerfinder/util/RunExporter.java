package com.interwoven.teamsite.nikon.dealerfinder.util;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.interwoven.teamsite.nikon.dealerfinder.HibernateUtil;

public class RunExporter {

	static final Logger oLogger = Logger.getLogger(RunExporter.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Cmd-line options
		Options options = new Options();
		options.addOption("country", true, "Country Code" );
		options.addOption("debug", true, "Debug Logging?");
		
		CommandLineParser parser = new BasicParser();
    	CommandLine cmd = null;
    	ApplicationContext context = new FileSystemXmlApplicationContext("props/app-context.xml");
    	HibernateUtil.init(context);    	
    	try {
    		
    		cmd = parser.parse( options, args );
		
    	} catch (ParseException e) {
		
    		e.printStackTrace();
		}
    	
    	// Logging
		BasicConfigurator.configure();
					
		if (cmd.hasOption("debug") && cmd.getOptionValue("debug").equals("true")) {
			
			Logger.getRootLogger().setLevel(Level.DEBUG);
		
		} else {
		
			Logger.getRootLogger().setLevel(Level.INFO);
		}
		
		
		if ( cmd.hasOption( "country" ) ) {
			
			oLogger.info( "Starting export country ["+cmd.getOptionValue( "country" )+"]");
			
			Exporter i = new Exporter();
			
			try
			{
				i.execute( cmd.getOptionValue( "country" ) );
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		} else {
			
			System.out.println();
    		System.out.println( "Options:" );
    		System.out.println( "          -country   Country to export" );
    		System.out.println( "          -debug     Show Debug (true)" );
    		System.out.println();
		}

	}

}
