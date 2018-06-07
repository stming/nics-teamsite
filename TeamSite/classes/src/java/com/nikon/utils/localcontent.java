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
 * @author fpriego
 */
public class localcontent{

	public localcontent() {
	}

	public Document getDCR(RequestContext context) throws DocumentException {


		//*******************************************************************


		String langValue = "";

		//for demo get language of cookie and change variable to same in all code

		//get lang from cookie for now
		Cookie tcookie = context.getCookies().getCookie("langCookie");

		if (tcookie != null) {
			langValue = tcookie.getValue().toString();
		} else {
			langValue = "de_CH";
		}





		Document document = null;

		String dcrPath = "";



		document = Dom4jUtils.newDocument();

		document.addElement("DCR");

		Element root = document.getRootElement();



		try{

			/***** dcrPath should be in this format: templatedata/Category/Datatype/data/blah.xml -- relative from the workarea and no leading ''/''. */

			dcrPath = context.getParameterString("dcr");
			if(dcrPath == null)
			{
				dcrPath = context.getParameterString("defaultDCR");
				if(dcrPath == null) dcrPath = "";


			}

			dcrPath = "templatedata/"+langValue+"/local_content/data/"+dcrPath;



			Document dcrDocument = ExternalUtils.readXmlFile(context, dcrPath);

			if( dcrDocument != null ) {

				context.getPageScopeData().put("bill", "This should be the billboard link");

				Element dcrRoot = dcrDocument.getRootElement();



				/***** Create the root node from the DCR document to your document that will be returned to the component XSL*/

				Element dcr = root.addElement(dcrRoot.getName());

				dcr.addAttribute("Status", "Success");

				dcr.addAttribute("DCRPath",dcrPath);

				for ( Iterator i = dcrRoot.attributeIterator(); i.hasNext(); ) {

					Attribute attribute = (Attribute) i.next();

					dcr.add(attribute);

				}



				/***** Copy rest of the nodes from DCR document to your document that will be returned to the component XSL */

				for(Iterator i = dcrRoot.elementIterator(); i.hasNext();){

					Element element = (Element) i.next();

					dcr.add(element.createCopy());


					if(element.getName().equalsIgnoreCase("billboard")) context.getPageScopeData().put("nkbillboard", element.getText());


				}


			}

			else{

				String errorMsg = "Error in getIndividualDCRs Method: Could not load DCR XML: "+dcrPath;

				setError(root, errorMsg);

				System.out.println(errorMsg);

			}



		}

		catch (Exception e){

			String errorMsg = "Error in getIndividualDCRs Method: "+e.toString();

			setError(root, errorMsg);

			System.out.println(errorMsg);

		}

		return document;

	}



	protected void setError(Element root, String errorMsg){

		Element errorElement = root.addElement("Error");

		errorElement.addText(errorMsg);

	}


}
