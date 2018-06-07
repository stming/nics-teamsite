package com.interwoven.teamsite.nikon.components;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.axis.encoding.Base64;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.interwoven.livesite.common.io.StreamUtil;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;

public class RetrieveRSSFeed {

	/** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** The name of the parameter containing our RSS URL. */
	private static final String PARAMETER_RSS_URL = "RSSUrl";
	
	/** The name of the parameter containing the username, if any. */
	private static final String PARAMETER_RSS_USERNAME = "RSSUsername";

	/** The name of the parameter containing the password, if any. */
	private static final String PARAMETER_RSS_PASSWORD = "RSSPassword";

	/** The name of the parameter containing the security mode. */
	private static final String PARAMETER_RSS_USER_AGENT = "RSSUserAgent";

	/** The name of the parameter containing the security mode. */
	private static final String PARAMETER_RSS_SECURITY_MODE = "RSSSecurityMode";

	/** String constant for no HTTP security. */
	private static final String SECURITY_MODE_NONE = "none";

	/** String constant for the BASIC security mode. */
	private static final String SECURITY_MODE_BASIC = "basic";

	public Document handleRequest(RequestContext context)
	{
		Document doc;
		try
		{
			doc = executeRSSCall(context);
		}
		catch (MalformedURLException mue)
		{
			// occurs when the URL is mis-configured in our component
			doc = Dom4jUtils.newDocument();
			Element errors = doc.addElement("Errors");
			addErrorElement(errors, mue);
		}
		catch (IOException ioe)
		{
			// occurs when the REST service is unavailable or some other error occurs
			doc = Dom4jUtils.newDocument();
			Element errors = doc.addElement("Errors");
			addErrorElement(errors, ioe);
			ioe.printStackTrace();
		}
		return doc;
	}
	
	protected void addErrorElement(Element root, Throwable t)
	{
		Element errorElement = root.addElement("Error");
		errorElement.setText(t.getMessage());
		errorElement.addAttribute("class", t.getClass().getName());
		if (t.getMessage() != null)
		{
			errorElement.addAttribute("message", t.getMessage());
		}

		Throwable nextCause = t.getCause();
		Element nextCauseParentElement = errorElement;
		while (nextCause != null)
		{
			nextCauseParentElement = nextCauseParentElement.addElement("Cause");
			nextCauseParentElement.addAttribute("class", nextCause.getClass().getName());
			nextCauseParentElement.addAttribute("message", nextCause.getMessage());
			// some exceptions rudely return themselves for cause, so we have to guard
			// against that.
			nextCause = !nextCause.equals(nextCause.getCause()) ? nextCause.getCause() : null;
		}
	}
	
	protected Document executeRSSCall(RequestContext context) throws IOException
	{
		URL url = getRSSUrl(context);
		
		// security hook for filtering or modifying the URL
		url = filterRestUrl(url, context);

		// For now, we take the default content-type that the REST service offers,
		// rather than negotiate for XML.
		URLConnection connection = url.openConnection();
		try
		{
			// pre-configure the connection with request headers and (potentially)
			// security cookies.
			configureConnection(connection, context);

			// Establish the connection and ensure that its content is ready to read.
			// Replace the connection if security hoop-jumping is required.
			connection = establishConnection(connection, context);

			// establish the connection and retrieve the response
			return readContentToXml(connection, context);
		}
		finally
		{
			// close the connection
			if (connection instanceof HttpURLConnection)
			{
				((HttpURLConnection) connection).disconnect();
			}
		}
	}
	
	protected void configureConnection(URLConnection connection, RequestContext context)
	{
		// add a User-Agent header if needed
		String userAgent = context.getParameterString(PARAMETER_RSS_USER_AGENT, "");
		if (StringUtils.isNotEmpty(userAgent))
		{
			connection.setRequestProperty("User-Agent", userAgent);
		}
		// add security to the request metadata if needed
		if (SECURITY_MODE_BASIC.equals(context.getParameterString(PARAMETER_RSS_SECURITY_MODE, SECURITY_MODE_NONE)))
		{
			String username = context.getParameterString(PARAMETER_RSS_USERNAME, "");
			String password = context.getParameterString(PARAMETER_RSS_PASSWORD, "");
			String token = username + ":" + password;
			String encodedPasskey = Base64.encode(token.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + encodedPasskey);
		}
		// some REST APIs require a referrer, so we'll use our LiveSite page.
		connection.setRequestProperty("Referer", context.getRequest().getRequestURI());
	}

	protected URLConnection establishConnection(URLConnection connection, RequestContext context) throws IOException
	{
		if (connection instanceof HttpURLConnection)
		{
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED)
			{
				connection = handleHttpUnauthorized(httpConnection, context);
			}
		}
		return connection;
	}

	protected URL filterRestUrl(URL url, RequestContext context) throws MalformedURLException
	{
		return url;
	}

	protected URL getRSSUrl(RequestContext context) throws MalformedURLException
	{
		String urlString = context.getParameterString(PARAMETER_RSS_URL);
		URL url = new URL(urlString);
		return url;
	}

	protected URLConnection handleHttpUnauthorized(HttpURLConnection httpConnection, RequestContext context)
	{
		return httpConnection;
	}

	protected boolean isJsonContentType(String contentType)
	{
		return contentType != null && (contentType.indexOf("json") >= 0 || contentType.indexOf("javascript") >= 0);
	}

	protected boolean isXmlContentType(String contentType)
	{
		// known good values: "application/xml", "text/xml"
		return contentType != null && contentType.indexOf("/xml") >= 0;
	}

	/**
	 * Reads the content of the established connection and parses it as XML.
	 * 
	 * @param connection
	 *          the network connection.
	 * @param context
	 *          the current LiveSite request context.
	 * @return a DOM4j Document object representing the XML response. If the
	 *         response is plain text, it will be wrapped in a top-level
	 *         <code>Text</code> element. If the response is XML, it will be
	 *         wrapped in a top-level <code>Xml</code> element.
	 * @throws IOException
	 *           if something goes wrong reading the stream.
	 */
	protected Document readContentToXml(URLConnection connection, RequestContext context) throws IOException
	{
		InputStream is = new BufferedInputStream(connection.getInputStream());
		try
		{
			Document doc = Dom4jUtils.newDocument();
			// Since the REST service can return different representations, we'll
			// handle what gets returned as XML or plain text.
			String contentType = connection.getContentType();
			if (isXmlContentType(contentType))
			{
				// parse the XML stream into a DOM
				Document xml = Dom4jUtils.newDocument(is);
				// allow subclasses to modify or replace the xml
				xml = processXmlContent(xml);
				// wrap this XML tree in a top-level element called "Xml"
				doc.addElement("Xml").add(xml.getRootElement().detach());
			}
			else if (isJsonContentType(contentType))
			{
				String content = StreamUtil.read(is);
				try
				{
					JSONObject jsonContent = new JSONObject(content);
					Document xml = processJsonContent(jsonContent);
					doc.addElement("Xml").add(xml.getRootElement().detach());
				}
				catch (JSONException e)
				{
					throw new IOException(e.getMessage());
				}
			}
			else
			{
				// read the non-XML content in as plain text
				String content = StreamUtil.read(is);
				// return a single element called "Text"
				doc.addElement("Text").setText(content);
			}
			return doc;
		}
		finally
		{
			is.close();
		}
	}

	protected Document processXmlContent(Document xml)
	{
		return xml;
	}

	protected String processTextContent(String text)
	{
		return text;
	}
	
	 protected Document processJsonContent(JSONObject root) throws JSONException
	  {
	    String xmlString = XML.toString(root, "Json");
	    Document jsonAsXml = Dom4jUtils.newDocument(xmlString);
	    return processXmlContent(jsonAsXml);
	  }
}
