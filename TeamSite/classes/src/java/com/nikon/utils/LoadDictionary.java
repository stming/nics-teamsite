package com.nikon.utils;

import javax.servlet.http.Cookie;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.external.ExternalUtils;
import com.interwoven.livesite.runtime.RequestContext;

/**
 *
 * @author fpriego
 */
public class LoadDictionary {

	/** default constructor */
	public LoadDictionary() {


	}

	public Document getDictionary(RequestContext context) throws DocumentException {

        String langValue = "";
        //get lang from cookie
        Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langValue = tcookie.getValue().toString();
        } else {
            langValue = "en_Asia";
        //langValue = NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY;
        }

        
        //Dictionary
        String langglossary = ""; 
        langglossary = "/templatedata/" + langValue + "/glossary/data/dictionary";

		Document dcrDocument = ExternalUtils.readXmlFile(context, langglossary);
		
		// create Result doc
		Document doc = Dom4jUtils.newDocument("<locale_dictionary/>");
		Element root = doc.getRootElement();

		root.addAttribute("DictionaryDCRPath",langglossary);

		if( dcrDocument != null ) {
			
			root.add(dcrDocument.getRootElement().createCopy());
			root.addAttribute("Status", "Success");

		}
		else{
			root.addAttribute("Status", "Failure");

		}
		
		return doc;

		/*************************************/
		
	}
}
