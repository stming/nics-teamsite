package nhk.ls.runtime.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import nhk.ls.runtime.common.Logger;

import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * User: fish
 * Date: 22/06/2011
 * Time: 6:09 PM
 */
public class RemoveExternalFilter implements Filter {

    private Logger mLogger = new Logger(LogFactory.getLog("nhk.ls.runtime.filter.RemoveExternalFilter"));

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        mLogger.createLogInfo("INTO doFilter start");
        servletResponse.setContentType("text/html; charset=UTF-8");
        servletResponse.setCharacterEncoding("UTF-8");

        PrintWriter pwout = servletResponse.getWriter();

        //ServletOutputStream out = servletResponse.getOutputStream();
        ResponseWrapper wrapper = new ResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(servletRequest, wrapper);

        mLogger.createLogInfo("RETURN FROM filterChain.doFilter");

        boolean writeTranformedXML = false;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StreamResult result = new StreamResult(new StringWriter());
            DocumentBuilder builder = dbf.newDocumentBuilder();

            Document doc = builder.parse(new ByteArrayInputStream(wrapper.getStringData().getBytes("UTF-8")));
            NodeList list = doc.getElementsByTagName("External");
            for (int i = 0; i < list.getLength(); i++) {
                list.item(i).getParentNode().removeChild(list.item(i));
            }
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();

            writeTranformedXML = true;
            servletResponse.setContentLength(xmlString.getBytes("UTF-8").length);

            //out.println(xmlString);

            pwout.println(xmlString);
        } catch (Exception e) {
            mLogger.createLogDebug("Not a xml DOM. Hence, standard html output.");
        }

        if (!writeTranformedXML) {
            mLogger.createLogDebug("Setting default wrapper.getData()");
            servletResponse.setContentLength(wrapper.getContentLength());
            //out.write(wrapper.getData());
            pwout.write(new String(wrapper.getData(), "UTF-8"));
        }
        pwout.flush();
        //out.flush();
        mLogger.createLogInfo("Done");
    }

    public void destroy() {
    }
}
