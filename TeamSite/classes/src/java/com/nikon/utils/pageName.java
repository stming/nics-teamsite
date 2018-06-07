package com.nikon.utils;


import org.dom4j.*;
import com.interwoven.livesite.dom4j.*;
import com.interwoven.livesite.runtime.*;

public class pageName
{
    /** default constructor */
    public pageName()
    {
    }

    public Document getPageName(RequestContext context) throws DocumentException
    {
        Document doc = Dom4jUtils.newDocument();

        Element responseElement = doc.addElement("Response");


        responseElement.addElement("PageName").addText(context.getPageName());



		return doc;
    }
}