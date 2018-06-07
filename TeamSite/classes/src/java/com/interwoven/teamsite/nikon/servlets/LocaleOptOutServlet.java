package com.interwoven.teamsite.nikon.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.springx.NikonBusinessManager;

public class LocaleOptOutServlet extends HttpServlet {

	private Log log = LogFactory.getLog(LocaleOptOutServlet.class);
	NikonBusinessManager daoMan = null;

	/**
	 * Constructor of the object.
	 */
	public LocaleOptOutServlet() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		
		log.debug("Entering doGet(HttpServletRequest request, HttpServletResponse response)");

		String prodId      = request.getParameter(NikonHBN8ParamConstants.PRODUCT_ID);
		String countryCode = request.getParameter(NikonHBN8ParamConstants.COUNTRY_CODE);
		String addOptOut   = request.getParameter(NikonHBN8ParamConstants.ADD_PRD_OPT_OUT);

		log.debug(FormatUtils.mFormat("prodId     :{0}", prodId));
		log.debug(FormatUtils.mFormat("countryCode:{0}", countryCode));
		log.debug(FormatUtils.mFormat("addOptOut  :{0}", addOptOut));

		HBN8QueryParamDTO param = new HBN8QueryParamDTO();
		param.setProductId(prodId);
		param.setCountryCode(countryCode);
		param.setAddOptOut(FormatUtils.string2Boolean(addOptOut));
		ProductDTO prodDTO = daoMan.addProdLocaleOptOut(param);
		
		log.debug(prodDTO != null?prodDTO.getProdLocaleOptOut():FormatUtils.mFormat("Product with PROD_ID:{0} not found", prodId));

		log.debug("Exiting doGet(HttpServletRequest request, HttpServletResponse response)");
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
		log.debug("Entering init()");
		ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
		Object o = appCtx.getBean("nikon.hibernate.dao.manager");
		daoMan = (NikonBusinessManager)o;
		log.debug("Exiting init()");
	}
}
