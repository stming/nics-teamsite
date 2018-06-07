package com.nikon.utils;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import java.util.*;
import java.text.*;
import org.dom4j.Document;
import org.dom4j.Element;

public class time
{

    public time()
    {
    }

    public Document getTime(RequestContext context)
    {
     
	 DateFormat plain = DateFormat.getInstance();
        String now = plain.format(new Date());
	    Document doc = Dom4jUtils.newDocument();
        Element root = doc.addElement("Response");
        doc.getRootElement().addElement("date").addText(now);
        
        return doc;
	}
	
}