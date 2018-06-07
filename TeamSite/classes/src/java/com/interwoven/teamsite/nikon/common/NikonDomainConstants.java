package com.interwoven.teamsite.nikon.common;

import com.interwoven.teamsite.ext.util.FormatUtils;


/**
 * This class is the Domain constants class for the Nikon project
 * @author nbamford
 *
 */
public class NikonDomainConstants {

	//Please put in Alphabetical order
	//Session attribute values
	public final static String COUNTRY_CODE                   = "countryCode";
	public final static String LANGUAGE_CODE                  = "languageCode";
	
	public final static String LCLZBL_DCR                     = "localisableDCR";
	public final static String LD_DIC                         = "Load Dictionary";
	public final static String LD_MT_DT                       = "Load Meta Data";
	public final static String HDR_DCR                        = "header";
	public final static String FTR_DCR                        = "footer";
	public final static String DT_DCR                         = "data";
	
	public final static String MTDT_DCR_PTH                   = "metaDataDCRPath";
	
	//We use the below as the ultimate Fallback language_COUNTRY code i.e. en_EU. 
	//I would suggest however that this goes into the DCR pointed at by NikonDomainConstants.FALLBACK_RULES_DCR
	public final static String ENGLISH                        = "en";
	public final static String ASIA                         = "Asia";

	//Defaults
	public final static String DEFAULT_LANGUAGE               = ENGLISH;
	public final static String DEFAULT_CATEGORY               = "Digital Cameras";
	
	public final static String DEFAULT_COUNTRY                = ASIA;
	public final static String DEFAULT_LANGUAGE_AND_COUNTRY   = FormatUtils.languageCountryCode(DEFAULT_LANGUAGE, DEFAULT_COUNTRY);
	public final static String DICTIONARY_DCR_NAME            = "dictionary";
	public final static String GLANCE_SPECS_DCR_NAME          = "glance_specs";
	public final static String DEFAULT_DICTIONARY             = FormatUtils.mFormat("/templatedata/{0}/glossary/data/{1}", DEFAULT_LANGUAGE_AND_COUNTRY, DICTIONARY_DCR_NAME);
	public final static String DEFAULT_COMPARE_GLANCE_SPECS   = FormatUtils.mFormat("/templatedata/{0}/compare_glance_specs/data/{1}", DEFAULT_LANGUAGE_AND_COUNTRY, GLANCE_SPECS_DCR_NAME);
	public final static String FALLBACK_RULES_DCR             = "templatedata/Admin/fallback_rules/data/localisation_fallback_rules";
	
	//Livesite Component Param Names
	//Cookie Names
	public final static String CKIE_LANG_CODE                 = "langCookie";
	public final static String CKIE_LANG_PATCH_VAL            = "CKIE_LANG_PATCH_VAL";
	
	//Context Parameter Names
	public final static String CTXT_NAV_NEWS_ARCHIVE          = "Archive";
	public final static String CTXT_NAV_NEWS_NEW              = "New";
	public final static String CTXT_NAV_NEWS_RECENT           = "Recent";
	public final static String CTXT_NM_ART                    = "Number Articles";
	public final static String CTXT_PRD                       = "Period";
	public final static String CTXT_PRM_ID                    = "ID"; 
	public final static String CTXT_PRM_VL                    = "ParamValue";
	public final static String CTXT_QRT                       = "Quarter";
	public final static String CTXT_RN_QRY                    = "RunQuery";
	public final static String CTXT_SB_NAV_1                  = "Subnav1Param";
	public final static String CTXT_SB_NAV_2                  = "Subnav2Param";
	public final static String CTXT_SB_NAV_3                  = "Subnav3Param";
	public final static String CTXT_SB_NAV_4                  = "Subnav4Param";
	public final static String CTXT_SB_NAV_5                  = "Subnav5Param";

	public final static String CTXT_SB_CATEGORY_LBL           = "sParamValueLbl";
	public final static String CTXT_SB_NAV_1_LBL              = "sParam1ValueLbl";
	public final static String CTXT_SB_NAV_2_LBL              = "sSubnav2ParamLbl";
	public final static String CTXT_SB_NAV_3_LBL              = "sSubnav3ParamLbl";
	public final static String CTXT_SB_NAV_4_LBL              = "sSubnav4ParamLbl";
	public final static String CTXT_SB_NAV_5_LBL              = "sSubnav5ParamLbl";
	public final static String CTXT_SY                        = "SY";

	public final static String CTXT_TST_APPLY_WWA_DATE        = "testDisableWWADateFilter";
	public final static String CTXT_TST_CNT_CD                = "testLanguageCountry";
	public final static String CTXT_TST_PRD_ID                = "testProductId";
	public final static String CTXT_TST_OVERIDE_PARAMS        = "testOverideParams";
	public final static String CTXT_TST_CAT                   = "testCategoryParam";
	public final static String CTXT_TST_RN_QRY                = "testRunQuery";
	public final static String CTXT_TST_SB_NAV_1              = "testSubnav1Param";
	public final static String CTXT_TST_SB_NAV_2              = "testSubnav2Param";
	public final static String CTXT_TST_SB_NAV_3              = "testSubnav3Param";
	public final static String CTXT_TST_AWRD_TST_ID           = "testAwardTestId";
	public final static String CTXT_TST_AWRD_TST_YR           = "testAwardTestYear";

	public final static String CATEGORY_LIST                  = "Category List";
	public final static String APPLY_WWA_DATE                 = "Apply WWA Date";
	
	public final static String AWRD_TST_YEAR                  = "AwardTestimonialYear";
	
	//Default Metta Keywords
	public final static String META_DATA_DEFAULT              = "NIKON DEFAULT KEYWORDS";
	
	public final static String NT_APPLCBL                     = "NA";
	public final static String RN_QRY_VAL_LVL_0               = "0";
	public final static String RN_QRY_VAL_LVL_1               = "l1";
	public final static String RN_QRY_VAL_LVL_2               = "l2";
	public final static String RN_QRY_VAL_LVL_3               = "l3";
	public final static String RN_QRY_VAL_LVL_4               = "l4";
	
	public final static String VAL_FALSE                      = "false"; 
	public final static String VAL_TRUE                       = "true";
	

	public final static String getX(){return "en_Asia";}
	
	public final static long OneK                             = 1024;

	//-1 means a cookie becomes a session cookie.
	public final static int CKIE_LANG_CODE_EXPR_INT           = (60 * 60 * 24 * 60);
	public final static String CKIE_DEF_PATH                  = "/"; 
	public final static String EXT_ATT_NIKON_LOCALE           = "TeamSite/Metadata/NikonLocale";
	public final static String EXT_ATT_PROD_WWA_DATE          = "TeamSite/Metadata/prod_wwa_date";
	public final static String EXT_ATT_REALTES_TO_PRODUCT     = "TeamSite/Metadata/relates_to_product";
	public final static String EXT_ATT_PROD_RELATED		      = "TeamSite/Metadata/prod_related";
	public final static String EXT_ATT_PRODUCT_ID			  = "TeamSite/Metadata/prod_id";
	public final static String EXT_ATT_SHORT_NAME             = "TeamSite/Metadata/prod_short_name";
	public final static String EXT_ATT_LOCAL_NAME             = "TeamSite/Metadata/local_short_name";
	public final static String EXT_ATT_PROD_CATEGORY		  = "TeamSite/Metadata/ProductCategory";
	public final static String EXT_ATT_PROD_NAVCAT1		  	  = "TeamSite/Metadata/NavCat1";
	public final static String EXT_ATT_PROD_NAVCAT2		  	  = "TeamSite/Metadata/NavCat2";
	public final static String EXT_ATT_PROD_NAVCAT3		  	  = "TeamSite/Metadata/NavCat3";
	public final static String ATT_COMPONENT_LIST             = "COMPONENT_LIST";

	public final static String JCS_REGION_DCR_DOC             = "dcrdoc";
	public final static String JCS_REGION_PAGE                = "page";
	public final static String JCS_REGION_COMPONENT           = "component";
	public final static String JCS_REGION_PROD_DTO            = "productdto";
	public final static String JCS_REGION_PROD_DTO_NAV        = "productdtonav";
	public final static String JCS_REGION_PROD_DTO_CAT        = "productdtocat";
	public final static String JCS_REGION_AWARD_DTO           = "awardsdto";
	public final static String JCS_REGION_PRESS_LIBRARY_DTO   = "presslibrarydto";
	public final static String JCS_REGION_PROD_RESPONSE       = "productresponse";
	public final static String JCS_REGION_COMMERCE_MAP        = "commercemap";
	
	public final static boolean ENABLE_JCS_CACHING            = true;

	public final static String PAR_MANIFEST_FILE_NAME         = "PAR_MANIFEST_FILE_NAME"; 
	public final static String PAR_FLAT_FILE_NAME             = "PAR_FLAT_FILE_NAME"; 
	public final static String PAR_LIVESITE_RUNTIME_HOME      = "PAR_LIVESITE_RUNTIME_HOME"; 
	public final static String PAR_LIVESITE_RUNTIME_URL       = "PAR_LIVESITE_RUNTIME_URL";
	public final static String PAR_PAGE_LIST                  = "PAR_PAGE_LIST";
	public final static String PAR_DCR_LIST                   = "DCR_LIST";
	public final static String PAR_DCR_LIST_REGEX_INC         = "PAR_DCR_LIST_REGEX_INC";
	public final static String PAR_CLEAR_CUSTOM_CACHE_REGIONS = "PAR_CLEAR_CUSTOM_CACHE_REGIONS";
	public final static String PAR_URL_DELAY_MILLIS           = "PAR_URL_DELAY_MILLIS";
	
	public final static String XPTH_MTDT_TTL                  = "TeamSite/Metadata/title";
	public final static String XPTH_MTDT_DSC                  = "TeamSite/Metadata/description";
	public final static String XPTH_MTDT_KYWRDS               = "TeamSite/Metadata/Keywords";

	public final static String DEFAULT_ENCODING               = "UTF-8";
	
	public static final String PAR_PDCWWA_WA_REGEX_MATCH      = "PAR_PDCWWA_WA_REGEX_MATCH"; 
	public static final String PAR_EXATTI_WA_REGEX_MATCH      = "PAR_EXATTI_WA_REGEX_MATCH"; 
	
}
