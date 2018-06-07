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

public class RunImporter
{
	static final Logger oLogger = Logger.getLogger(RunImporter.class);
	
	public static void main(String[] args)
	{
		// Cmd-line options
		Options options = new Options();
		options.addOption( "file", true, "CSV File" );
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
		
		if ( cmd.hasOption( "file" ) ) {
			
			oLogger.info( "Starting import - " + cmd.getOptionValue( "file" ) );
			
			Importer i = new Importer();
			
			try
			{
				i.execute( cmd.getOptionValue( "file" ) );
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		} else {
			
			System.out.println();
    		System.out.println( "java -jar runImporter.jar -file <FILE> -debug true" );
    		System.out.println();
    		System.out.println( "Options:" );
    		System.out.println( "          -file      Path to CSV file" );
    		System.out.println( "          -debug     Show Debug (true)" );
    		System.out.println();
		}

	}

}
