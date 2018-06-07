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

/**
 *
 * @author epayne
 */
/* a test comment to see if build date changes
 */
public class navGeneric {

	/** default constructor */
	public navGeneric() {

	}

	public Document constructNavGeneric(RequestContext context)
			throws DocumentException {

		Document doc = Dom4jUtils.newDocument("<staticcontent/>");

		// get query string parameter named "lang" from context
		String langValue = null;

		Cookie tcookie = context.getCookies().getCookie("langCookie");

		if (tcookie != null) {
			langValue = tcookie.getValue().toString();
		} else {
			langValue = "de_CH";
		}

		//see if link need to be highlighted
		String linkID = "";
		String linkIDSub1 = "";
		String linkIDSub2 = "";
		String linkIDSub3 = "";
		String linkIDSub4 = "";

		//get linkID

		if (context.getRequest().getParameter("lid") == null) {
			linkID = "0";

			//if session use that
			if (context.getSession().getAttribute("linkID") != null) {
				linkID = context.getSession().getAttribute("linkID").toString();
			}

		} else {
			linkID = context.getRequest().getParameter("lid");
			context.getSession().setAttribute("linkID", linkID);

		}

		//get linkIDSub1

		if (context.getRequest().getParameter("lidsub1") == null) {
			linkIDSub1 = "0";

			//if session use that
			if (context.getSession().getAttribute("lidsub1") != null) {
				linkIDSub1 = context.getSession().getAttribute("lidsub1")
						.toString();
			}

		} else {
			linkIDSub1 = context.getRequest().getParameter("lidsub1");
			context.getSession().setAttribute("linkIDSub1", linkIDSub1);

		}

		//          get linkIDSub2

		if (context.getRequest().getParameter("lidsub2") == null) {
			linkIDSub2 = "0";

			//if session use that
			if (context.getSession().getAttribute("lidsub2") != null) {
				linkIDSub2 = context.getSession().getAttribute("lidsub2")
						.toString();
			}

		} else {
			linkIDSub2 = context.getRequest().getParameter("lidsub2");
			context.getSession().setAttribute("linkIDSub2", linkIDSub2);

		}

		//          get linkIDSub3

		if (context.getRequest().getParameter("lidsub3") == null) {
			linkIDSub3 = "0";

			//if session use that
			if (context.getSession().getAttribute("lidsub3") != null) {
				linkIDSub3 = context.getSession().getAttribute("lidsub3")
						.toString();
			}

		} else {
			linkIDSub3 = context.getRequest().getParameter("lidsub3");
			context.getSession().setAttribute("linkIDSub3", linkIDSub3);

		}

		//          get linkIDSub4

		if (context.getRequest().getParameter("lidsub4") == null) {
			linkIDSub4 = "0";

			//if session use that
			if (context.getSession().getAttribute("lidsub4") != null) {
				linkIDSub4 = context.getSession().getAttribute("lidsub4")
						.toString();
			}

		} else {
			linkIDSub4 = context.getRequest().getParameter("lidsub4");
			context.getSession().setAttribute("linkIDSub4", linkIDSub4);

		}

		//append link to doc
		doc.getRootElement().addElement("LinkID").addText(linkID);
		doc.getRootElement().addElement("LinkIDSub1").addText(linkIDSub1);
		doc.getRootElement().addElement("LinkIDSub2").addText(linkIDSub2);
		doc.getRootElement().addElement("LinkIDSub3").addText(linkIDSub3);
		doc.getRootElement().addElement("LinkIDSub4").addText(linkIDSub4);

		//get dcr data
		String navHeadings = context.getParameterString("Headings");

		if (!navHeadings.equals(null) || !navHeadings.equals("")) {
			navHeadings = "/templatedata/" + langValue
					+ navHeadings.substring(18);
		}

		// get the TeamSite workarea vpath from the context
		FileDALIfc fileDal = context.getFileDAL();
		java.io.InputStream is = null;

		if (!navHeadings.equals(null) || !navHeadings.equals("")) {
			is = fileDal.getStream(fileDal.getRoot() + navHeadings);
		}

		//declare doc for return of headings
		Document doc1 = null;

		//add is stream to xml
		try {

			if (is != null || !is.equals(null)) {
				doc1 = Dom4jUtils.newDocument(is);
			}

			if (doc1 != null || !doc1.equals(null)) {
				doc.getRootElement().appendContent(doc1);
			}

		} catch (Exception e) {

			doc = ExternalUtils.newErrorDocument("Exception Occured constructNavGeneric method in navGeneric class");
		}

		// return the XML to the component
		return doc;

	}
}
