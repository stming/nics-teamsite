package com.interwoven.teamsite.ext.builders;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.teamsite.ext.util.FormatUtils;

/**
 * This Class is to be used for creating a Properties Object from various input types
 * @author nbamford
 *
 */
public class PropertiesBuilder
{
	private final Log log = LogFactory.getLog(PropertiesBuilder.class);
	public final static String CSV_PROPS = "CSV_PROPS";
	public final static String WFL_PROPS = "WFL_PROPS";
	public final static String STR_ARR_PROPS = "STR_ARR_PROPS";
	Properties properties = new Properties();

	private String props;
	private String[] propsArr;
	private CSExternalTask csExtTask;
	
	private String propType;
	private List<String> propKeysList;

	/**
	 * Constructor to build Properties from a comma separated key=value pair
	 * parameter e.g. a=1, b=2, c=3 where the propType == CSV_PROPS
	 * @param props    comma separated key=value list
	 * @param propType CSV_PROPS
	 */
	public PropertiesBuilder(String props, String propType)
	{
		log.debug("Creating instance of PropertiesBuilder(String props, String propType)");
		this.props = props;
		this.propType = propType;
	}
	
	public PropertiesBuilder(String[] propsArr)
	{
		log.debug("Creating instance of PropertiesBuilder(String[] propsArr)");
		this.propsArr = propsArr;
		this.propType = STR_ARR_PROPS;
	}

	/**
	 * Constructor to build Properties from CSExternalTask csExtTask
	 * To name the task variables to process after creating an instance
	 * of this class, use the addParamKey passing in variable name
	 * @param csExtTask WF External task
	 */
	public PropertiesBuilder(CSExternalTask csExtTask)
	{
		log.debug("Creating instance of PropertiesBuilder(CSExternalTask csExtTask)");
		this.csExtTask = csExtTask;
		this.propType = WFL_PROPS;
	}

	public void addParamKey(String key)
	{
		if(propKeysList == null)
		{
			propKeysList = new LinkedList<String>();
		}
		propKeysList.add(key);
	}

	/**
	 * Build the properties from the given inputs
	 * @return
	 */
	public Properties buildProperties()
	{
		log.debug("Entering public Properties buildProperties()");
		if(CSV_PROPS.equals(propType))
		{
			if((props != null) && (propType != null))
			{
				//Tokenize the params
				StringTokenizer st = new StringTokenizer(props, ",");

				while(st.hasMoreTokens())
				{
					String keyVal = st.nextToken().trim();
					//Do something with the KeyVal
					StringTokenizer st2 = new StringTokenizer(keyVal, "=");
					if(st2.countTokens() == 2)
					{
						String key = st2.nextToken();
						String val = st2.nextToken();

						properties.put(key.trim(), val.trim());
					}
				}
			}
		}
		else if(STR_ARR_PROPS.equals(propType))
		{
			for(String keyVal: propsArr)
			{
				StringTokenizer st2 = new StringTokenizer(keyVal, "=");
				if(st2.countTokens() == 2)
				{
					String key = st2.nextToken();
					String val = st2.nextToken();

					properties.put(key.trim(), val.trim());
				}
			}
		}
		else if(WFL_PROPS.equals(propType))
		{
			for(String key: propKeysList)
			{
				try 
				{
					properties.put(key, csExtTask.getVariable(key));
					log.debug(FormatUtils.mFormat("Placing key:{0}, val:{1} in properties", key, csExtTask.getVariable(key)));
				} 
				catch (Exception e) 
				{
					log.debug(FormatUtils.mFormat("Unable to create property with key:{0}:{1}", key, e));
				} 
			}
		}
		else
		{
			log.warn(FormatUtils.mFormat("Unknown Properties Type {0}", propType));
		}
		log.debug("Exiting public Properties buildProperties()");
		return properties;
	}
}