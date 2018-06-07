package com.interwoven.teamsite.nikon.solr.products;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;

public class UpdateSolrServerImpl implements UpdateSolrServer {

	private static final Log logger = LogFactory.getLog(UpdateSolrServerImpl.class);
	
	public void updateSolr(String solrServerUrl, String solrUpdateXML) {
		
		logger.info("Solr Server - Update Process Start");
		
		try {
				logger.info("Solr Server - Address: " + solrServerUrl + "/");
			
				logger.info("Solr Server - XML: " + solrUpdateXML);
				
				HttpSolrServer solrServer = new HttpSolrServer(solrServerUrl);			
				
				DirectXmlRequest up = new DirectXmlRequest( "/update", solrUpdateXML); 
			
				solrServer.request(up);
				
				logger.info("Solr Server - Update Sent");
				
				solrServer.commit();
				
				logger.info("Solr Server - Commit Executed");
				
				logger.info("Solr Server - Update Process End");
				
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (SolrServerException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		}
		
		return;
	}
	
}
