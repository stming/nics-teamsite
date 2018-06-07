package com.interwoven.teamsite.nikon.externals;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.components.ComponentHelper;

/**
 *
 * @author epayne
 */
public class FooterBuilder {

	private Log log = LogFactory.getLog(FooterBuilder.class);
	
	ComponentHelper ch = new ComponentHelper();
    
	public FooterBuilder() {
    }

    public Document buildFooter(RequestContext context) throws DocumentException {

        // get the DCR and inject it into the returned XML
        Document doc = DocumentFactory.getInstance().createDocument("<staticcontent/>");

        Document doc1;
		
        try {
		
        	doc1 = ch.getLocalisedDCR(context, NikonDomainConstants.FTR_DCR);
			doc = doc1;
		
        } catch (Exception e) {
			
        	log.error(e.getMessage());
			Element errorEl = DocumentFactory.getInstance().createElement("error");
			
			errorEl.setText(e.getMessage());
			doc = DocumentFactory.getInstance().createDocument();
			doc.setRootElement(DocumentFactory.getInstance().createElement("staticcontent"));
			log.debug(FormatUtils.prettyPrint(doc));
			doc.getRootElement().add(errorEl);
		}

        // Get current date object
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        String DATE_FORMAT = "yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        String currentTime = sdf.format(cal.getTime());


        doc.getRootElement().addElement("CurrYear").addText(currentTime);

        // return the XML to the component
        return doc;
    }
}
