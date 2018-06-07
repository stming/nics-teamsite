package com.interwoven.teamsite.nikon.solr.products;

import com.interwoven.cssdk.common.CSClient;

public interface GenerateSolrXML {

	public String generate(CSClient client, String locale, String filePath);
}
