package com.nikon.utils;

import java.net.URLEncoder;

import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class NavTop {

	public Document getCartStoreId(RequestContext context) {
		Document doc = Dom4jUtils.newDocument();
		Element response = doc.addElement("Response");
		response.addElement("CartStoreId").addText(CartStoreId.getCartStoreId(context));
		return doc;
	}

	public Document getFromUrl(RequestContext context) {
		Document doc = Dom4jUtils.newDocument();
		Element response = doc.addElement("Response");
		response.addElement("FromUrl").addText(getCurrentUrl(context));
		return doc;
	}

	public static String getCurrentUrl(RequestContext context) {
		StringBuffer currentUrlBuffer = context.getRequest().getRequestURL();
		if (context.getRequest().getQueryString() != null) {
			currentUrlBuffer.append("?");
			currentUrlBuffer.append(context.getRequest().getQueryString());
		}
		String currentUrl = currentUrl = URLEncoder.encode(currentUrlBuffer.toString());
		return currentUrl;
	}

}
