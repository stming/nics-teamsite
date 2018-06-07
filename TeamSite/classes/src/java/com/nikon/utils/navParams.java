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
public class navParams {

	private static String LEVEL_1_PARAM_NAME="sParamValueLbl";
	private static String LEVEL_2_PARAM_NAME="sParam1ValueLbl";
	private static String LEVEL_3_PARAM_NAME="sSubnav2ParamLbl";
	private static String LEVEL_4_PARAM_NAME="sSubnav3ParamLbl";
	private static String LEVEL_5_PARAM_NAME="sSubnav4ParamLbl";
	
	
	/** default constructor */
	public navParams() {

	}

	public Document getBreadCrumbParams(RequestContext context)
			{

		Document doc = Dom4jUtils.newDocument("<staticcontent/>");
		
		if (context.getParameterString(LEVEL_1_PARAM_NAME)!=null && !context.getParameterString(LEVEL_1_PARAM_NAME).equals("") && context.getParameterString(LEVEL_1_PARAM_NAME).length() > 0)
		{
		doc.getRootElement().addElement("sParamValueLbl").addText(context.getParameterString(LEVEL_1_PARAM_NAME));
		}
		if (context.getParameterString(LEVEL_2_PARAM_NAME)!=null && !context.getParameterString(LEVEL_2_PARAM_NAME).equals("") && context.getParameterString(LEVEL_2_PARAM_NAME).length() > 0)
		{
		doc.getRootElement().addElement("sParam1ValueLbl").addText(context.getParameterString(LEVEL_2_PARAM_NAME));
		}
		if (context.getParameterString(LEVEL_3_PARAM_NAME)!=null && !context.getParameterString(LEVEL_3_PARAM_NAME).equals("") && context.getParameterString(LEVEL_3_PARAM_NAME).length() > 0)
		{
		doc.getRootElement().addElement("sSubnav2ParamLbl").addText(context.getParameterString(LEVEL_3_PARAM_NAME));
		}
		if (context.getParameterString(LEVEL_4_PARAM_NAME)!=null && !context.getParameterString(LEVEL_4_PARAM_NAME).equals("") && context.getParameterString(LEVEL_4_PARAM_NAME).length() > 0)
		{
		doc.getRootElement().addElement("sSubnav3ParamLbl").addText(context.getParameterString(LEVEL_4_PARAM_NAME));
		}
		if (context.getParameterString(LEVEL_5_PARAM_NAME)!=null && !context.getParameterString(LEVEL_5_PARAM_NAME).equals("") && context.getParameterString(LEVEL_5_PARAM_NAME).length() > 0)
		{
		doc.getRootElement().addElement("sSubnav4ParamLbl").addText(context.getParameterString(LEVEL_5_PARAM_NAME));
		}
		
		return doc;

	}
}
