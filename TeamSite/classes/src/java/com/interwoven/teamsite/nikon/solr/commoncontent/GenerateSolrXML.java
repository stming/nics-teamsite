package com.interwoven.teamsite.nikon.solr.commoncontent;

import com.interwoven.cssdk.common.CSClient;

public interface GenerateSolrXML {

	public String generate(CSClient client, String locale, String filePath);
}
