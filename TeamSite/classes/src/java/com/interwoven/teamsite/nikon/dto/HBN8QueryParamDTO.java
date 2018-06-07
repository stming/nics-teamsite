package com.interwoven.teamsite.nikon.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.repository.NikonRepository;

/**
 * This is a bean class to hold HBN8 parameterised data
 * 
 * @author nbamford
 * 
 */
public class HBN8QueryParamDTO implements CommonDTOFields {

	private Log log = LogFactory.getLog(HBN8QueryParamDTO.class);
	private TeamsiteEnvironment teamsiteEnvironment;
	private NikonRepository repo;
	private String sourcePath;
	
	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public NikonRepository getRepo() {
		return repo;
	}

	public void setRepo(NikonRepository repo) {
		this.repo = repo;
	}

	private String mode = NikonHBN8ParamConstants.MODE_READ_DB;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public final static String NIKON_LOCALE = "nikonLocale";

//	private MetricLog metricLog;

	public HBN8QueryParamDTO(HBN8QueryParamDTO hbn8QueryParamDTO) {
		org.springframework.beans.BeanUtils.copyProperties(hbn8QueryParamDTO,
				this);
	}

	// Always default to true so that it fails safe
	// Setting it externally will iradicate this
	private boolean production = true;

	public HBN8QueryParamDTO() {
	}

	public HBN8QueryParamDTO(String field, String value) {
		this(new String[][] { { field, value } });
	}

	public HBN8QueryParamDTO(String[][] fieldValueArray) {
		for (String[] nameValPair : fieldValueArray) {
			String name = nameValPair[0];
			String value = nameValPair[1];
			try {
				BeanUtils.setProperty(this, name, value);
				log.debug(FormatUtils.mFormat("Setting field {0}:{1}", name,
						value));
			} catch (IllegalAccessException e) {
				log.debug(FormatUtils.mFormat("Error setting field {0}:{1}",
						name, value), e);
			} catch (InvocationTargetException e) {
				log.debug(FormatUtils.mFormat("Error setting field {0}:{1}",
						name, value), e);
			}
		}
	}

	// Request Context Constructor
	// Put the parsing all in one place
	public HBN8QueryParamDTO(RequestContext context, boolean production) {
		this.requestContext = context;
		this.production = production;
		bindRequestContextParams();
	}

	public HBN8QueryParamDTO(RequestContext context, TeamsiteEnvironment environment)
	{
		this.requestContext = context;
		this.teamsiteEnvironment = environment;
		bindRequestContextParams();
	}
	
	HttpServletRequest request;
	
	public HBN8QueryParamDTO(HttpServletRequest request)
	{
		this.request = request;
	}

	public RequestContext getRequestContext() {
		return requestContext;
	}

	// local RequestContext
	private RequestContext requestContext;

	private void bindRequestContextParams() {
		log.debug("Entering private void bindRequestContextParams()");
		// Set the Language Country Code

		if (this.requestContext == null) return;
		// Look for it as a Cookie, if not there set to NA
		String lngCntCd = getCookie(NikonDomainConstants.CKIE_LANG_CODE, NikonDomainConstants.NT_APPLCBL);
		log.debug("lngCntCd:" + lngCntCd);
		log.debug("--------------1");
		if (NikonDomainConstants.NT_APPLCBL.equals(lngCntCd)) {
			log.debug("--------------1.1");
			// Check for an overriden one in DEV
			if (!isProduction()) {
				log.debug("--------------1.1.1");
				lngCntCd = getContextParameter(
						NikonDomainConstants.CTXT_TST_CNT_CD,
						NikonDomainConstants.NT_APPLCBL);
			}
		}

//		if (!production) {
//			String a = getContextParameter(
//					NikonDomainConstants.CTXT_TST_APPLY_WWA_DATE,
//					NikonDomainConstants.VAL_FALSE);
//
//			Boolean booleanVal = new Boolean(a);
//			enableWWAFilter = !booleanVal.booleanValue();
//			log.debug(FormatUtils.mFormat("a:{0}", a));
//			log.debug(FormatUtils.mFormat("enableWWAFilter:{0}",
//					enableWWAFilter));
//		} else {
//			enableWWAFilter = true;
//		}

		enableWWAFilter = isProduction();
		
		// If we're still not getting then set to default
		log.debug("--------------2");
		if (NikonDomainConstants.NT_APPLCBL.equals(lngCntCd)) {
			log.debug("--------------2.1");
			lngCntCd = NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY;
		}

		// Finally set on bean itself
		log.debug("--------------3");
		setLanguageCode(FormatUtils.languageCode(lngCntCd));
		log.debug("--------------4");
		setCountryCode(FormatUtils.countryCode(lngCntCd));

		setSiteCountryCode(LocaleResolver.getSiteCountryCode(requestContext));
		log.debug("--------------5");
		setNikonLocale(lngCntCd);

		log.debug("--------------6");
		String productId = getContextParameter(
				NikonDomainConstants.CTXT_PRM_ID,
				NikonDomainConstants.NT_APPLCBL);

		// If we don't have it then we either need to
		log.debug("--------------7");
		if (NikonDomainConstants.NT_APPLCBL.equals(productId)) {
			log.debug("--------------7.1");
			if (!isProduction()) {
				log.debug("--------------7.1.1");
				productId = getContextParameter(
						NikonDomainConstants.CTXT_TST_PRD_ID,
						NikonDomainConstants.NT_APPLCBL);

			}
		}
		log.debug("--------------8");
		setProductId(productId);

		// ProductCategory

		log.debug("--------------9");
		String category = getContextParameter(NikonDomainConstants.CTXT_PRM_VL,
				NikonDomainConstants.DEFAULT_CATEGORY);
		log.debug("--------------10");
		setCategory(category);

		// SubNav 1
		log.debug("--------------11");
		setNavCat1(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_1, "0"));

		// SubNav 2
		log.debug("--------------12");
		setNavCat2(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_2, "0"));

		// SubNav 3
		log.debug("--------------13");
		setNavCat3(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_3, "0"));
		setNavCat4(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_4, "0"));
		setNavCat5(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_5, "0"));

		setCategoryLbl(getContextParameter(NikonDomainConstants.CTXT_SB_CATEGORY_LBL, "0"));
		setNavCat1Lbl(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_1_LBL, "0"));
		setNavCat2Lbl(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_2_LBL, "0"));
		setNavCat3Lbl(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_3_LBL, "0"));
		setNavCat4Lbl(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_4_LBL, "0"));
		setNavCat5Lbl(getContextParameter(NikonDomainConstants.CTXT_SB_NAV_5_LBL, "0"));

		// RunQuery
		log.debug("--------------14");
		setRunQuery(getContextParameter(NikonDomainConstants.CTXT_RN_QRY,
				NikonDomainConstants.RN_QRY_VAL_LVL_0));

		// Period
		log.debug("--------------15");
		setPeriod(getContextParameter(NikonDomainConstants.CTXT_PRD,
				NikonDomainConstants.CTXT_NAV_NEWS_NEW));

		// Quarter
		log.debug("--------------16");
		setQuarter(getContextParameter(NikonDomainConstants.CTXT_QRT, ""));

		// SY
		log.debug("--------------17");
		setSy(getContextParameter(NikonDomainConstants.CTXT_SY, ""));

		// Number Articles
		log.debug("--------------18");
		setNumberOfArticles(getContextParameter(
				NikonDomainConstants.CTXT_NM_ART, "0"));

		// Set the Testimonial Year
		setAwardTestYear(getContextParameter(
				NikonDomainConstants.AWRD_TST_YEAR, ""));

		// Set the Award Testimonial Id
		setAwardTestId(getContextParameter(
				NikonHBN8ParamConstants.AWARD_TEST_ID, ""));
		
		setPath(getContextParameter(
				NikonDomainConstants.MTDT_DCR_PTH, ""));

		// Override with test

		try {
			// If we're overriding
			BeanUtils.setProperty(this, "testOverideParams",
					getContextParameter(
							NikonDomainConstants.CTXT_TST_OVERIDE_PARAMS,
							Boolean.toString(false)));
			if (testOverideParams) {
				BeanUtils.setProperty(this, "category", getContextParameter(
						NikonDomainConstants.CTXT_TST_CAT, category));
				BeanUtils.setProperty(this, "navCat1", getContextParameter(
						NikonDomainConstants.CTXT_TST_SB_NAV_1, navCat1));
				BeanUtils.setProperty(this, "navCat2", getContextParameter(
						NikonDomainConstants.CTXT_TST_SB_NAV_2, navCat2));
				BeanUtils.setProperty(this, "navCat3", getContextParameter(
						NikonDomainConstants.CTXT_TST_SB_NAV_3, navCat3));

				BeanUtils
						.setProperty(this, "awardTestId", getContextParameter(
								NikonDomainConstants.CTXT_TST_AWRD_TST_ID,
								awardTestId));
				BeanUtils.setProperty(this, "awardTestYear",
						getContextParameter(
								NikonDomainConstants.CTXT_TST_AWRD_TST_YR,
								awardTestYear));
			}
		} catch (IllegalAccessException e) {
			log.error("Unable to set property", e);
		} catch (InvocationTargetException e) {
			log.error("Unable to set property", e);
		}

		log.debug("Exiting private void bindRequestContextParams()");
	}

	private String period;
	private String quarter;
	private String sy;
	private String numberOfArticles;

	// Field variables
	boolean clientOverrideTxn;

	String productId;
	Collection<String> productIds;
	String languageCode;
	String countryCode;
	String siteCountryCode;
	String category;
	String[] categories;
	boolean enableWWAFilter;
	String runQuery;
	String navCat1;
	String navCat2;
	String navCat3;
	String navCat4;
	String navCat5;

	String categoryLbl;
	String navCat1Lbl;
	String navCat2Lbl;
	String navCat3Lbl;
	String navCat4Lbl;
	String navCat5Lbl;

	Collection<String> awardTestIds;
	String awardTestId;
	String awardTestYear;

	boolean testOverideParams;
	String testRunQuery;
	String testNavCat1;
	String testNavCat2;
	String testNavCat3;

	String nikonLocale;
	Collection<String> nikonLocales;
	String path;
	String prodDevCode;
	boolean addOptOut;
	
	public boolean isAddOptOut() {
		return addOptOut;
	}

	public void setAddOptOut(boolean addOptOut) {
		this.addOptOut = addOptOut;
	}

	public String getProdDevCode() {
		return prodDevCode;
	}

	public void setProdDevCode(String prodDevCode) {
		this.prodDevCode = prodDevCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String[] getCategories() {
		return categories;
	}

	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	public boolean isClientOverrideTxn() {
		return clientOverrideTxn;
	}

	public void setClientOverrideTxn(boolean clientOverrideTxn) {
		this.clientOverrideTxn = clientOverrideTxn;
	}

	public boolean isEnableWWAFilter() {
		return enableWWAFilter;
	}

	public void setEnableWWAFilter(boolean enableWWAFilter) {
		this.enableWWAFilter = enableWWAFilter;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getLanguageCountryCode() {
		return FormatUtils.languageCountryCode(this.languageCode,
				this.countryCode);
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	private String getCookie(String name, String nullValue) {
		String retVal = null;
		if ((requestContext != null) && (requestContext.getCookies() != null)) {
			Cookie cookie = requestContext.getCookies().getCookie(name);
			if (cookie == null) {
				retVal = nullValue;
			} else {
				retVal = cookie.getValue();
				log.debug(FormatUtils.mFormat(
						"- Cookie:{0} found, setting value to:{1}", name,
						retVal));
			}
		} else {
			retVal = nullValue;
		}

		return retVal;
	}

	private String getContextParameter(String name, String nullValue) {

		String retVal = nullValue;
		if (requestContext != null) {
			retVal = requestContext.getParameterString(name);
			retVal = Utils.URLDecode(retVal);
			if ((retVal == null) || ("".equals(retVal))) {
				retVal = nullValue;
			}
		} else {
			retVal = nullValue;
		}
		return retVal;
	}

	public String getRunQuery() {
		return runQuery;
	}

	public void setRunQuery(String runQuery) {
		this.runQuery = runQuery;
	}

	public String getNavCat1() {
		return navCat1;
	}

	public void setNavCat1(String navCat1) {
		this.navCat1 = navCat1;
	}

	public String getNavCat2() {
		return navCat2;
	}

	public void setNavCat2(String navCat2) {
		this.navCat2 = navCat2;
	}

	public String getNavCat3() {
		return navCat3;
	}

	public void setNavCat3(String navCat3) {
		this.navCat3 = navCat3;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(FormatUtils.mFormat("category:{0},", category));
		sb.append(FormatUtils.mFormat("navCat1 :{0},", navCat1));
		sb.append(FormatUtils.mFormat("navCat2 :{0},", navCat2));
		sb.append(FormatUtils.mFormat("navCat3 :{0}", navCat3));
		sb.append("\n");
		return sb.toString();
	}

	public RequestContext getContext() {
		return requestContext;
	}

	public void setContext(RequestContext context) {
		this.requestContext = context;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

	public String getSy() {
		return sy;
	}

	public void setSy(String sy) {
		this.sy = sy;
	}

	public String getNumberOfArticles() {
		return numberOfArticles;
	}

	public void setNumberOfArticles(String numberOfArticles) {
		this.numberOfArticles = numberOfArticles;
	}

	public String getNikonLocale() {
		return nikonLocale;
	}

	public void setNikonLocale(String nikonLocale) {
		this.nikonLocale = nikonLocale;
	}

	public boolean isProduction() {
		boolean retVal = this.teamsiteEnvironment == null?production:this.teamsiteEnvironment.isProduction();
		log.debug(FormatUtils.mFormat("| Production:{0}", retVal));
		return retVal;
	}

	public void setProduction(boolean production) {
		this.production = production;
	}

	public Collection getProductIds() {
		return productIds;
	}

	public void setProductIds(Collection productIds) {
		this.productIds = productIds;
	}

	public Collection<String> getNikonLocales() {
		return nikonLocales;
	}

	public void setNikonLocales(Collection<String> nikonLocales) {
		this.nikonLocales = nikonLocales;
	}

	public boolean isTestOverideParams() {
		return testOverideParams;
	}

	public void setTestOverideParams(boolean testOverideParams) {
		this.testOverideParams = testOverideParams;
	}

	public String getTestRunQuery() {
		return testRunQuery;
	}

	public void setTestRunQuery(String testRunQuery) {
		this.testRunQuery = testRunQuery;
	}

	public String getTestNavCat1() {
		return testNavCat1;
	}

	public void setTestNavCat1(String testNavCat1) {
		this.testNavCat1 = testNavCat1;
	}

	public String getTestNavCat2() {
		return testNavCat2;
	}

	public void setTestNavCat2(String testNavCat2) {
		this.testNavCat2 = testNavCat2;
	}

	public String getTestNavCat3() {
		return testNavCat3;
	}

	public void setTestNavCat3(String testNavCat3) {
		this.testNavCat3 = testNavCat3;
	}

//	public MetricLog getMetricLog() {
//		return metricLog;
//	}
//
//	public void setMetricLog(MetricLog metricLog) {
//		this.metricLog = metricLog;
//	}

	public Collection<String> getAwardTestIds() {
		return awardTestIds;
	}

	public void setAwardTestIds(Collection<String> awardTestIds) {
		this.awardTestIds = awardTestIds;
	}

	public String getSiteCountryCode() {
		return siteCountryCode;
	}

	public void setSiteCountryCode(String siteCountryCode) {
		this.siteCountryCode = siteCountryCode;
	}

	public String getAwardTestYear() {
		return awardTestYear;
	}

	public void setAwardTestYear(String awardTestYear) {
		this.awardTestYear = awardTestYear;
	}

	public String getAwardTestId() {
		return awardTestId;
	}

	public void setAwardTestId(String awardTestId) {
		this.awardTestId = awardTestId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getNavCat4() {
		return navCat4;
	}

	public void setNavCat4(String navCat4) {
		this.navCat4 = navCat4;
	}

	public String getNavCat5() {
		return navCat5;
	}

	public void setNavCat5(String navCat5) {
		this.navCat5 = navCat5;
	}

	public String getCategoryLbl() {
		return categoryLbl;
	}

	public void setCategoryLbl(String categoryLbl) {
		this.categoryLbl = categoryLbl;
	}
	
	public String getNavCat1Lbl() {
		return navCat1Lbl;
	}
	
	public void setNavCat1Lbl(String navCat1Lbl) {
		this.navCat1Lbl = navCat1Lbl;
	}

	public String getNavCat2Lbl() {
		return navCat2Lbl;
	}

	public void setNavCat2Lbl(String navCat2Lbl) {
		this.navCat2Lbl = navCat2Lbl;
	}

	public String getNavCat3Lbl() {
		return navCat3Lbl;
	}

	public void setNavCat3Lbl(String navCat3Lbl) {
		this.navCat3Lbl = navCat3Lbl;
	}

	public String getNavCat4Lbl() {
		return navCat4Lbl;
	}

	public void setNavCat4Lbl(String navCat4Lbl) {
		this.navCat4Lbl = navCat4Lbl;
	}

	public String getNavCat5Lbl() {
		return navCat5Lbl;
	}

	public void setNavCat5Lbl(String navCat5Lbl) {
		this.navCat5Lbl = navCat5Lbl;
	}

	public TeamsiteEnvironment getTeamsiteEnvironment() {
		return teamsiteEnvironment;
	}

	public void setTeamsiteEnvironment(TeamsiteEnvironment teamsiteEnvironment) {
		this.teamsiteEnvironment = teamsiteEnvironment;
	}

	//Methods to encapsulate logic to decide which Award Testimonial query to run
	public boolean isMultipleAwardsQry()
	{
		return ((getAwardTestYear() == null) || ("".equals(getAwardTestYear()))) && ((getAwardTestId() == null) || ("".equals(getAwardTestId())));
	}
	
	public boolean isSingleAwardQry()
	{
		return ((getAwardTestYear() == null) || ("".equals(getAwardTestYear()))) && ((getAwardTestId() != null) && (!"".equals(getAwardTestId())));
	}
	
	public boolean isAwardYearQry()
	{
		return (getAwardTestYear() != null) && (!"".equals(getAwardTestYear()));
	}
}
