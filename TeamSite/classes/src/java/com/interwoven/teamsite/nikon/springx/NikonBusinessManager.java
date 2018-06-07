package com.interwoven.teamsite.nikon.springx;

import java.util.Collection;
import java.util.List;

import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.dto.ProductDTO;

public interface NikonBusinessManager {

	//These are for the Spring Advice Interceptors and should mirror the names of these
	//interface methods exactly
    public final static String addProdLocaleOptOut = "addProdLocaleOptOut";
    public final static String listCatalogueProducts = "listCatalogueProducts";
    public final static String listNavCatProducts = "listNavCatProducts";
    public final static String listPressLibrayByCategoryNav = "listPressLibrayByCategoryNav";
    public final static String listProductDetails = "listProductDetails";
    public final static String listProductMetaDataFromDCRPath = "listProductMetaDataFromDCRPath";
	
	
    /**
 	 * Service method to add or remove a two letter country code
 	 * into the column PROD_LOCALE_OPT_OUT for a given product with
 	 * a given prod_dev_code within the default language_COUNTRY code
 	 * 
 	 * @param param Param class with the following fields set
 	 *                    prodDevCode : Dev code of the product
 	 *                    addOptOut   : true == add, false == remove
 	 * The code will attempt to remove or add the country code regardless
 	 * of previous state. It may be worth checking the returned ProductDTO instance
 	 * to ensure this has happened.
 	 * @return An instance of ProductDTO containing a copy of the data.
 	 */
    public ProductDTO addProdLocaleOptOut(HBN8QueryParamDTO param);
	
   /**
	 * Products for the Catalogue. works various levels
	 * of aggregation
	 * and update here
	 * @param param instance of HBN8QueryParamDTO
	 * @return List of ProductDTO
	 */
	public List<ProductDTO> listCatalogueProducts(HBN8QueryParamDTO param);
	
	
	/**
	 * Products based on product navigation categories.
	 * @param param instance of HBN8QueryParamDTO
	 * @return List of ProductDTO
	 */
	public List<ProductDTO> listNavCatProducts(HBN8QueryParamDTO param);
	
	/**
	 * New Product Details. Once things are working, will refactor ProductDTO to proper place
	 * and update here
	 * @param param instance of HBN8QueryParamDTO
	 * @return Single ProductDTO
	 */
	public ProductDTO listProductDetails(HBN8QueryParamDTO param);
	
	/**
	 * New Product Details. Once things are working, will refactor ProductDTO to proper place
	 * and update here
	 * @param param instance of HBN8QueryParamDTO
	 * @return Single ProductDTO
	 */
	public String getProductDCRByIdAndLocale(HBN8QueryParamDTO param);
	
	/**
	 * This method returns ProductDTO meta data for a product from its path value
	 * 
	 * @param param Param class with the following fields set
	 *                    path : path of the DCR
	 * 
	 * @return Instance of ProductDTO containing a copy of the Product data
	 */
	public ProductDTO listProductMetaDataFromDCRPath(HBN8QueryParamDTO param);
	
	
	/**
	 * @Deprecated
	 * @return
	 */
	public List<ProductDTO> listTestFilter(HBN8QueryParamDTO param, boolean enableTestFilter);
	
	/**
	 * Service method to resolve locales based on the locale escalation rules.
	 * @param param
	 * @return
	 */
	public Collection<String> localeEscalationResolver(HBN8QueryParamDTO param);
}
