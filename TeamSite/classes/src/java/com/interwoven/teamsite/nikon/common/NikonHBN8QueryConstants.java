package com.interwoven.teamsite.nikon.common;

/**
 * @author nbamford
 * This class contains the names queries in the hibernate configuration file 
 * used in the custom classes
 *
 */
public class NikonHBN8QueryConstants 
{
	public static String ACC_PRD_ID_COUNTRY                  = "nikonAccessoryByProductAndCountryCode";
	public static String DCR_FOR_ACC_ID                      = "nikonDCRByAccessoryId";
	public static String DCR_FOR_PROD_ID                     = "nikonDCRByProductId";
	public static String NAV_NEWS_ARTICLE                    = "nikonNavNewsArticle";
	public static String PRD_COUNTRY_CATEGORY                = "nikonProductByCountryCodeCategories";
	public static String PRD_MT_DT_FRM_PTH                   = "nikonProdMetaDataFromPath";
	public static String PRD_ON_PRD_ID_IN_DEF                = "nikonProdOnProdIdInDefault";
	
	public static String PRC_MULTI_PRD_IN_LOCALE             = "nikonMultiProductOnProdDevCodeAndNikonLocale";
	
	public static String PRD_DETS                            = "nikonProductDetails";
	public static String PRD_NAV_L1                          = "nikonNavProductsLevel1";
	public static String PRD_NAV_L2                          = "nikonNavProductsLevel2";
	public static String PRD_NAV_L3                          = "nikonNavProductsLevel3";
	public static String PRD_NAV_L4                          = "nikonNavProductsLevel4";
	
	
	public static String SNG_PROD_IN_LOCALE                  = "nikonSingleProductOnProdIdAndNikonLocale";
	public static String PRD_2_ACC      	                 = "nikonProduct2Accessories";
	public static String PRD_2_PRD_MARKETING_RELATED         = "nikonProduct2ProductMarketingRelated";
	public static String PRD_MARKETING_RELATED_2_PRD         = "nikonProductMarketingRelated2Product";
    public static String ACC_2_PRD                           = "nikonAccessory2Product";
	public static String PRD_2_BOM			                 = "nikonProduct2Bom";
	public static String PRD_2_PRD                           = "nikonProduct2Products";
	public static String PRD_2_AWRD                          = "nikonProduct2Award";
	
	public static String MLT_PRD_PRD_ID_LOC                  = "nikonMultiProductOnProdIdAndNikonLocale";
	
	public static String PRD_CAT_1                           = "nikonProdCat1";
	public static String PRD_CAT_2                           = "nikonProdCat2";
	public static String PRD_CAT_3                           = "nikonProdCat3";
	public static String PRD_CAT_4                           = "nikonProdCat4";
	
	public final static String NKN_DISCONT_PRD_ID_LCL 		 	= "Product.nikonDiscontinuedProductIDsForLocale";
	public final static String NKN_ACTIVE_PRD_ID_LCL  		 	= "Product.nikonActiveProductIDsForLocale";
	public final static String NKN_REMOVALOVERRIDE_PRD_ID_LCL 	= "Product.nikonRemovalOverrideProductIDsForLocale";
	
	public static String ASST_CAT_1							 = "nikonPressLibraryCat1";
	public static String ASST_CAT_2							 = "nikonPressLibraryCat2";
	public static String ASST_CAT_3							 = "nikonPressLibraryCat3";
	public static String ASST_CAT_4							 = "nikonPressLibraryCat4";
	
	public static String AWARD_TEST_2_AWARD                  = "nikonAwardTest2Award";
	
	public static String MULTI_AWARD_TEST_IN_LOCALE          = "nikonMultipleAwardTestimoniesOnAwardTestIdsAndNikonLocale";
	public static String MULTI_AWARD_IN_LOCALE               = "nikonMultiAwardOnAwardTestIdAndNikonLocale";
	public static String MULTI_AWARD_TEST_IN_LOCALE_FOR_YEAR = "nikonMultipleAwardTestimoniesInYear";
}
