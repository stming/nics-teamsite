package com.nikon.utils;


import org.dom4j.*;
import com.interwoven.livesite.dom4j.*;
import com.interwoven.livesite.runtime.*;

import java.io.*;
import javax.servlet.http.Cookie;

public class langParam
{
    /** default constructor */
    public langParam()
    {
    }

    public Document getLangParam(RequestContext context) throws DocumentException, IOException
    {
	
	
	
	
        Document doc = Dom4jUtils.newDocument();
        
        Element responseElement = doc.addElement("Response");
        
       // String langParam = context.getRequest().getParameter("lang");
        
	//if (langParam != null)
	//{
   // responseElement.addElement("LangParam").addText(langParam);
	//}
	//else
//	{
	//responseElement.addElement("LangParam").addText("en");
//	langParam = "en";
//	}
	
        String langParam = "";
        
            //get lang from session
     Cookie tcookie = context.getCookies().getCookie("langCookie");

        if (tcookie != null) {
            langParam = tcookie.getValue().toString();
        }
        else
        {
         langParam = "en_CH";
        }
        
        
        
	Cookie langCookie = new Cookie("lang", langParam);
	
	langCookie.setMaxAge(60);
	
	langCookie.setPath("/");
	
	context.getResponse().addCookie(langCookie);
		
	Cookie[] cookies = context.getRequest().getCookies();
	
	// context.getResponse().sendRedirect("/");
	
	String cookieValue = getCookieValue(cookies, "langCookie", "defaultValue");
	
	responseElement.addElement("langCookie").addText(cookieValue);
	
        return doc;
    }
    
    public static String getCookieValue(Cookie[] cookies,
            String cookieName,
            String defaultValue) {
    	for(int i=0; i<cookies.length; i++) {
    		Cookie cookie = cookies[i];
    		if (cookieName.equals(cookie.getName()))
    			return(cookie.getValue());
    	}
    	return(defaultValue);
    }
}
