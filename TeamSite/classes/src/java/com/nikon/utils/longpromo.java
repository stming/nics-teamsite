package com.nikon.utils;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.dom4j.*;
import org.dom4j.io.*;
import com.interwoven.livesite.external.*;
import com.interwoven.livesite.file.*;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
//import com.interwoven.teamsite.nikon.common.NikonDomainConstants;



import java.util.*;
import java.text.*;

/**
 *
 * @author epayne
 */
public class longpromo {

	public longpromo() {
	}

	public Document getlongpromo(RequestContext context) throws Exception {

		Document doc = Dom4jUtils.newDocument("<staticcontent/>");

		try {
			String langValue = "";

			//get lang from cookie 
			Cookie tcookie = context.getCookies().getCookie("langCookie");

			if (tcookie != null) {
				langValue = tcookie.getValue().toString();
			} else {
				langValue = "en_Asia";
				//langValue = NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY;
			}

			// Get the path to the product DCR from the context parameter
			 String productDcrPath = context.getParameterString("dcrparam");
			 
			// refactor the path to the DCR according to the lang
			if (tcookie != null) {
				productDcrPath = "/templatedata/" + langValue + productDcrPath.substring(18);
			}

			//			TODO generate the link to the product Querying the DB
			//			
			//
			//

			// Read the DCR and inject it into the returned XML

			Document dcrDocument = ExternalUtils.readXmlFile(context, productDcrPath);
				doc.getRootElement().appendContent(dcrDocument);
		} catch (Exception e) {
			//debug data
			doc.getRootElement().addElement("Error").addText(e.getMessage());
		}



		// return the XML to the component
		return doc;
	}
}
