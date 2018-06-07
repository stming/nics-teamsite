package com.interwoven.teamsite.nikon.externals;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.javax.VisitorAdapter;
import com.interwoven.teamsite.nikon.components.ComponentHelper;

public class NavGeneric {

	private Log log = LogFactory.getLog(NavGeneric.class);

	private ComponentHelper componentHelper = new ComponentHelper();

	/** default constructor */
	public NavGeneric() {

	}

	@SuppressWarnings("unchecked")
	public Document constructNavGeneric(RequestContext context) throws DocumentException {

		log.debug("Entering Document constructNavGeneric(RequestContext context)");

		Document doc  = componentHelper.getLocalisedDCR(context, "Headings");
		 
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
		doc.getRootElement().addElement("LinkID").addText(Utils.URLEncode(linkID));
		doc.getRootElement().addElement("LinkIDSub1").addText(Utils.URLEncode(linkIDSub1));
		doc.getRootElement().addElement("LinkIDSub2").addText(Utils.URLEncode(linkIDSub2));
		doc.getRootElement().addElement("LinkIDSub3").addText(Utils.URLEncode(linkIDSub3));
		doc.getRootElement().addElement("LinkIDSub4").addText(Utils.URLEncode(linkIDSub4));

		//Add URL Encoding
		doc.accept(new EncodedAttributeVisitor());
		
		//add a title
		ArrayList <String> breadCrumb = new ArrayList<String>();

		// get existing title
		String existingTitle = context.getPageTitle();
		breadCrumb.add(existingTitle);
			
		String pageTitle = "";
		
		for ( String bcPart : breadCrumb )
		{
			if ( ! pageTitle.equals("") )
			{
				pageTitle += " - ";
			}
			pageTitle += bcPart;
		}		

		// output this for debug
		doc.getRootElement().addElement("pageTitle").addText(pageTitle);
		
		// now set the title
		context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, pageTitle);
		
		// return the XML to the component
		log.debug("Exiting Document constructNavGeneric(RequestContext context)");
		
		return doc;

	}
	
	/**
	 * This visitor class is used to added URL Encoded attribute values to
	 * the Label and URLParameters elements
	 * @author nbamford
	 *
	 */
	private class EncodedAttributeVisitor extends VisitorAdapter
	{

		public void visit(Element arg0) {

			
			//Matches on Elements with the following Regex Pattern
			String patternStr = "Label|URLParameters";
		    Pattern pattern = Pattern.compile(patternStr);
		    Matcher matcher = pattern.matcher(arg0.getName());
		    
			if (matcher.matches())
			{
				matcher.reset();
				arg0.addAttribute("Enc", Utils.URLEncode(arg0.getText()));
			}
		}
	}
	
}
