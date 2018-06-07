package com.interwoven.teamsite.nikon.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.businessrules.CacheKeyManager;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.springx.NikonBusinessManager;

public class CacheInvalidationServlet extends HttpServlet {

	private Log log = LogFactory.getLog(CacheInvalidationServlet.class);
	NikonBusinessManager daoMan = null;

	/**
	 * Constructor of the object.
	 */
	public CacheInvalidationServlet() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		
		log.debug("Entering doGet(HttpServletRequest request, HttpServletResponse response)");

		String productId       = request.getParameter(NikonHBN8ParamConstants.PRODUCT_ID);
		String langCountryCode = request.getParameter(NikonHBN8ParamConstants.NKN_LOCALE);
		String methodName      = request.getParameter(NikonHBN8ParamConstants.MTHD_NAME);

		log.debug(FormatUtils.mFormat("nikonLocale:{0}", langCountryCode));
		log.debug(FormatUtils.mFormat("prodId     :{0}", productId));
		log.debug(FormatUtils.mFormat("methodName :{0}", methodName));
		
		String key = CacheKeyManager.productdtoKey(langCountryCode, productId, methodName);

		JCS cache = null;
		try 
		{
			cache = JCS.getInstance(NikonDomainConstants.JCS_REGION_PROD_DTO);
		} 
		catch (CacheException e) {
			log.warn(e);
		}

		//Implement code here to invalidate items
		if(cache != null)
		{
			try 
			{
				log.warn(FormatUtils.mFormat("Attempting to remove object with key {0} from cache", key));
				cache.remove(key);
			} 
			catch (CacheException e) {
				log.warn(FormatUtils.mFormat("Unable to remove {0} from cache", key));
			}

		}
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering doPost(HttpServletRequest request, HttpServletResponse response)");
		doGet(request, response);
		log.debug("Exiting doPost(HttpServletRequest request, HttpServletResponse response)");
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException {
	}
}
