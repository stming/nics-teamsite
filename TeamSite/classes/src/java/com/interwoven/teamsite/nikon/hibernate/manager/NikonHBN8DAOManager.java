package com.interwoven.teamsite.nikon.hibernate.manager;


import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;

import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.hibernate.manager.AbstractTSEnvAwareHibernateDAOManager;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.businessrules.LocaleResolver;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.common.NikonHBN8FilterConstants;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.common.NikonHBN8QueryConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.dto.builders.ProductDTOAssembler;
import com.interwoven.teamsite.nikon.hibernate.beans.Product;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2Accessory;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2Bom;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2Product;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2ProductMarketingRelated;
import com.interwoven.teamsite.nikon.springx.NikonBusinessManager;

/**
 * @author nbamford
 *
 * Manager class offering services to a client. These services
 * are the business logic to retrieve data from the database
 * this is done via Hibernate. An instance of the class itself
 * is stored in the Spring container both development/runtime
 * though it can be instantiated in it's own right as long
 * as it is correctly configured. What's required is
 * a TeamsiteEnvironment call the method setTeamsiteEnvironment(environment);
 * a Hibernate session factory          setSessionFactory(sessionFactory);
 */
@SuppressWarnings("unchecked")
public class NikonHBN8DAOManager extends AbstractTSEnvAwareHibernateDAOManager implements NikonBusinessManager
{
	private Log log = LogFactory.getLog(NikonHBN8DAOManager.class);
	private static Log staticLog = LogFactory.getLog(NikonHBN8DAOManager.class);

	//Name of the instantiated class in Spring. This should match the id given in the Spring configuration file
	public static final String beanName = "nikon.hibernate.dao.manager";

	/**
	 * This method will return the instance stored in Spring via
	 * the Spring configuration file
	 *
	 * @return Instance configured in the Spring container with the name nikon.hibernate.dao.manager
	 *
	 * @throws BeanDefinitionStoreException
	 */
	public static final NikonHBN8DAOManager getSpringAppCtxInstance()
	throws BeanDefinitionStoreException
	{
		staticLog.debug("Entering public static final NikonHBN8DAOManager getSpringAppCtxInstance()");
		ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
		Object o = appCtx.getBean(beanName);

		if (o == null)
		{
			throw new BeanDefinitionStoreException("Bean of name" + beanName + " not found in ApplicationContext");
		}

		return (NikonHBN8DAOManager)o;
	}

	public NikonHBN8DAOManager()
	{
		log.debug("Creating instance of " + this.getClass().getName());
	}

	/**
	 * New Product Details. Once things are working, will refactor ProductDTO to proper place
	 * and update here
	 * @param param instance of HBN8QueryParamDTO
	 * @return Single ProductDTO
	 */
	public ProductDTO listProductDetails(HBN8QueryParamDTO param)
	{
		log.debug("Entering ProductDTO listProductDetails(HBN8QueryParamDTO param)");

		ProductDTO retVal = null;

		// Setup the session
		getSession().beginTransaction().begin();

		// Set the WWA Date Filter
		enableDisableFilter(NikonHBN8FilterConstants.WWA_DATE, param.isEnableWWAFilter());

		//Set up the query and parameters
		Query singleProductQry = getSession().getNamedQuery(NikonHBN8QueryConstants.SNG_PROD_IN_LOCALE);
		      singleProductQry.setParameter(NikonHBN8ParamConstants.PRODUCT_ID, param.getProductId());

		//Get the list of possible locale fallback country code for this country code
		Collection<String> possibleLocales = localeEscalationResolver(param);
		log.debug(FormatUtils.collection2String(FormatUtils.mFormat("Country Code {0}", param.getSiteCountryCode()), possibleLocales));

		singleProductQry.setParameter(NikonHBN8ParamConstants.NKN_CNTRY_CD, param.getSiteCountryCode());
		singleProductQry.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);


		//Run the query, we're expecting just the one here, anything else and we need to get the default
		List<ProductDTO> queryList = (List<ProductDTO>)filterUnwantedLocales(singleProductQry.list(), new LocaleComparator(possibleLocales), ProductDTO.class);
		
		// Remove Products that are marked as Locale Opt-Out
		removeLocaleOptOutProducts(queryList);

		ProductDTO prod = null;

		//if we found something and we
		boolean prodFound = true;
		
		if((queryList != null) && (queryList.size() > 0))
		{
			if(queryList.size() == 1)
			{
				prod = queryList.get(0);
			}
			//More than one results, need to move up rules list
			else
			{
				try
				{
					Collections.sort(queryList, new ProductLocaleComparator(possibleLocales));
					prod = (ProductDTO)resolveLocale(queryList.iterator(), "nikonLocale", param.getSiteCountryCode(), param.getNikonLocale());
				}
				catch (Exception e)
				{
					prodFound = false;
					log.error("Unable to resolve", e);
				}

				log.warn(FormatUtils.mFormat("More than one Product found with prodId:{0} in nikonLocal:{1}. Could be due to localisation resolving", param.getProductId(), param.getNikonLocale()));
			}
		}
		else
		{
			log.error(FormatUtils.mFormat("Unable to find Product with prodId:{0} in nikonLocal:{1}", param.getProductId(), param.getNikonLocale()));
			prodFound = false;
		}

		//Only do the rest of the
		if(prodFound)
		{
			//Accessories
			//List the accessory relations from the golden set
			Query prod2AccessoryQuery = getSession().getNamedQuery(NikonHBN8QueryConstants.PRD_2_ACC);
			      prod2AccessoryQuery.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, beanFieldToListOfStrings(prod, ProductDTO.index));
			      prod2AccessoryQuery.setParameter(NikonHBN8ParamConstants.NKN_CNTRY_CD, param.getSiteCountryCode());

			List<Product2Accessory> prod2AccList = prod2AccessoryQuery.list();

			log.info(FormatUtils.mFormat("prod2AccList.size({0})", prod2AccList.size()));

			Query multiProductQry;
			List<ProductDTO> accessories 				= new LinkedList<ProductDTO>();
			List<ProductDTO> relatedProducts 			= new LinkedList<ProductDTO>();
			List<ProductDTO> bomProducts 				= new LinkedList<ProductDTO>();
			List<ProductDTO> accessoryOf 				= new LinkedList<ProductDTO>();
			List<ProductDTO> marketRelatedProducts    	= new LinkedList<ProductDTO>();

			// Go get the values
			if(prod2AccList.size() > 0)
			{
				multiProductQry = getSession().getNamedQuery(NikonHBN8QueryConstants.MLT_PRD_PRD_ID_LOC);
				multiProductQry.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, listOfBeansFieldToListOfStrings(prod2AccList, ProductDTO.index));
				multiProductQry.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);

				accessories = (List<ProductDTO>)filterUnwantedLocales(multiProductQry.list(), new LocaleComparator(possibleLocales), ProductDTO.class);
				
				removeLocaleOptOutProducts(accessories);
			}

			// Marketing Related Products
			Query prod2MarketingRelatedProdQuery = getSession().getNamedQuery(NikonHBN8QueryConstants.PRD_2_PRD_MARKETING_RELATED);
			      prod2MarketingRelatedProdQuery.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, beanFieldToListOfStrings(prod, ProductDTO.index));
			      prod2MarketingRelatedProdQuery.setParameter(NikonHBN8ParamConstants.NKN_CNTRY_CD, param.getSiteCountryCode());

			List<Product2ProductMarketingRelated> prod2MarkPrdList = prod2MarketingRelatedProdQuery.list();

			log.info(FormatUtils.mFormat("prod2MarkPrdList.size({0})", prod2MarkPrdList.size()));

			// Go get the values
			if(prod2MarkPrdList.size() > 0)
			{
				multiProductQry = getSession().getNamedQuery(NikonHBN8QueryConstants.MLT_PRD_PRD_ID_LOC);
				multiProductQry.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, listOfBeansFieldToListOfStrings(prod2MarkPrdList, ProductDTO.index));
				multiProductQry.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);

				marketRelatedProducts = (List<ProductDTO>)filterUnwantedLocales(multiProductQry.list(), new LocaleComparator(possibleLocales), ProductDTO.class);
				
				removeLocaleOptOutProducts(marketRelatedProducts);
			}			

			//Related Products
			//List all of the related products from the golden set for this product

			Query prod2RelatedProductsQuery = getSession().getNamedQuery(NikonHBN8QueryConstants.PRD_2_PRD);
			      prod2RelatedProductsQuery.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, beanFieldToListOfStrings(prod, ProductDTO.index));
			      prod2RelatedProductsQuery.setParameter(NikonHBN8ParamConstants.NKN_CNTRY_CD, param.getSiteCountryCode());

			List<Product2Product> prod2RelatedProdList = prod2RelatedProductsQuery.list();

			log.info(FormatUtils.mFormat("prod2RelatedProdList.size({0})", prod2RelatedProdList.size()));

			Collection<String> colOfProdIds = null;

			// Go get the values
			if(prod2RelatedProdList.size() > 0)
			{
				multiProductQry = getSession().getNamedQuery(NikonHBN8QueryConstants.MLT_PRD_PRD_ID_LOC);
				colOfProdIds = listOfBeansFieldToListOfStrings(prod2RelatedProdList, ProductDTO.index);
				multiProductQry.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, colOfProdIds);
				multiProductQry.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
				relatedProducts = (List<ProductDTO>)filterUnwantedLocales(multiProductQry.list(), new LocaleComparator(possibleLocales), ProductDTO.class);

				removeLocaleOptOutProducts(relatedProducts);
			}

			//BOM Products Accessories
			//List all of the bom products from the golden set for this product
			Query prod2BomProductsQuery = getSession().getNamedQuery(NikonHBN8QueryConstants.PRD_2_BOM);
			      prod2BomProductsQuery.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, beanFieldToListOfStrings(prod, ProductDTO.index));
			      prod2BomProductsQuery.setParameter(NikonHBN8ParamConstants.NKN_CNTRY_CD, param.getSiteCountryCode());
			
			List<Product2Bom> prod2BomProdList = prod2BomProductsQuery.list();

			log.info(FormatUtils.mFormat("prod2BomProdList.size({0})", prod2BomProdList.size()));

			// Go get the values
			if (prod2BomProdList.size() > 0)
			{
				multiProductQry = getSession().getNamedQuery(NikonHBN8QueryConstants.MLT_PRD_PRD_ID_LOC);
				colOfProdIds = listOfBeansFieldToListOfStrings(prod2BomProdList, ProductDTO.index);
				multiProductQry.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, colOfProdIds);
				multiProductQry.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
				bomProducts = (List<ProductDTO>)filterUnwantedLocales(multiProductQry.list(), new LocaleComparator(possibleLocales), ProductDTO.class);
				
				removeLocaleOptOutProducts(bomProducts);
			}

			List<Product2Accessory> acc2ProdList = null;
			
			if (prod.isAccessory() || true)
			{
				Query accessory2ProductQuery = getSession().getNamedQuery(NikonHBN8QueryConstants.ACC_2_PRD);

				accessory2ProductQuery.setParameter(NikonHBN8ParamConstants.PRODUCT_ID, prod.getProdId());
				accessory2ProductQuery.setParameter(NikonHBN8ParamConstants.NKN_CNTRY_CD, param.getSiteCountryCode());

				acc2ProdList = accessory2ProductQuery.list();
				
				log.info(FormatUtils.mFormat("acc2ProdList.size({0})", acc2ProdList.size()));

				if(acc2ProdList.size() > 0)
				{
					multiProductQry = getSession().getNamedQuery(NikonHBN8QueryConstants.MLT_PRD_PRD_ID_LOC);
					multiProductQry.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, listOfBeansFieldToListOfStrings(acc2ProdList, ProductDTO.fk));
					multiProductQry.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
					accessoryOf = (List<ProductDTO>)filterUnwantedLocales(multiProductQry.list(), new LocaleComparator(possibleLocales), ProductDTO.class);

					removeLocaleOptOutProducts(accessoryOf);
				}

				log.info(FormatUtils.mFormat("accessoryOf.size({0})", accessoryOf.size()));

			}

			getSession().close();


			//Create an Assembler for ProductDTO
			//Assemble to return a single instance
			com.interwoven.teamsite.nikon.dto.builders.ProductDTOAssembler assembler = new com.interwoven.teamsite.nikon.dto.builders.ProductDTOAssembler();
			assembler.setTeamsiteEnvironment(param.getTeamsiteEnvironment());
			assembler.setProductDTO(prod);
			assembler.setProduct2accessories(prod2AccList);
			assembler.setAccessories(accessories);
			assembler.setProduct2product(prod2RelatedProdList);
			assembler.setRelatedProducts(relatedProducts);
			assembler.setProduct2bom(prod2BomProdList);
			assembler.setBillOfMaterials(bomProducts);
			assembler.setAccessory2Product(acc2ProdList); 
			assembler.setAccessoryOf(accessoryOf);
			assembler.setProduct2marketingRelatedProduct(prod2MarkPrdList);
			assembler.setMarketingRelatedProducts(marketRelatedProducts);

			retVal = assembler.assembleSingle();
		}

		log.debug("Exiting ProductDTO listProductDetails(HBN8QueryParamDTO param)");
		
		return retVal;
	}
	
	/**
	 * This method returns ProductDTO meta data for a product from its path value
	 * 
	 * @param param Param class with the following fields set
	 *                    path : path of the DCR
	 * 
	 * @return Instance of ProductDTO containing a copy of the Product data
	 */
	public ProductDTO listProductMetaDataFromDCRPath(HBN8QueryParamDTO param)
	{
		ProductDTO retDto = new ProductDTO();

		getSession().beginTransaction().begin();

		Query productMetaDataFromPath = getSession().getNamedQuery(NikonHBN8QueryConstants.PRD_MT_DT_FRM_PTH);
		productMetaDataFromPath.setParameter(NikonHBN8ParamConstants.NKN_PTH, param.getPath());
		productMetaDataFromPath.setParameter(NikonHBN8ParamConstants.NKN_PTH_ALT, param.getPath().replace("/", "\\"));
		log.debug(FormatUtils.mFormat("param.getPath()", param.getPath()));
		List<ProductDTO> list = productMetaDataFromPath.list();
		if(list.size() == 1)
		{
			retDto = list.iterator().next();
		}

		getSession().close();

		return retDto;
	}

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
	public ProductDTO addProdLocaleOptOut(HBN8QueryParamDTO param)
	{
		ProductDTO retDto = null;
		getSession().beginTransaction().begin();

		Query productByDevCodeInDefault = getSession().getNamedQuery(NikonHBN8QueryConstants.PRD_ON_PRD_ID_IN_DEF);
		productByDevCodeInDefault.setParameter(NikonHBN8ParamConstants.PRODUCT_ID, param.getProductId());
		productByDevCodeInDefault.setParameter(NikonHBN8ParamConstants.NKN_CNTRY_CD, NikonDomainConstants.DEFAULT_LANGUAGE_AND_COUNTRY);

		List<Product> pList = productByDevCodeInDefault.list();
		if(pList.size() == 1)
		{
			Product p = (Product)pList.iterator().next();


			String currVal = p.getProdLocaleOptOut();
			String newVal  = null;

			if(param.isAddOptOut())
			{
				/* NB20090507 
				 * Seems MessageFormat String will show null in the String output if
				 * the String is null, instead of "" so we will wrap with an nvl call
				 */        	
				newVal = FormatUtils.mFormat("{0},{1}", FormatUtils.nvl(currVal, ""), param.getCountryCode());
				newVal = newVal.replaceAll("^,", "");
			}
			else
			{
				//Do the logic to remove
				newVal = FormatUtils.mFormat("({0})", param.getCountryCode());
				newVal = currVal.replaceAll(FormatUtils.mFormat("({0})", param.getCountryCode()), "");
				newVal = newVal.replaceAll("^,", "");
				newVal = newVal.replaceAll(",$", "");
				newVal = newVal.replaceAll(",,", ",");
			}


			log.debug(FormatUtils.mFormat("currVal:{0}", currVal));
			log.debug(FormatUtils.mFormat("newVal :{0}", newVal));
			p.setProdLocaleOptOut(newVal);
			getSession().save(p);
			retDto = new ProductDTO(p);
			getSession().flush();
			getSession().getTransaction().commit();
		}
		else
		{
			getSession().close();
		}
		return retDto;
	}

	private Map<String, Map<String, ProductDTO>> putProdDTOInMapIndexByProdId(List<ProductDTO> listOfProdDTO, Comparator c)
	{
		Map<String, Map<String, ProductDTO>> retMap = new LinkedHashMap<String, Map<String, ProductDTO>>();

		for(ProductDTO p: listOfProdDTO)
		{
			String key = p.getProdId();
			if(!retMap.containsKey(key))
			{
				Map<String, ProductDTO> subMap = new  TreeMap<String, ProductDTO>(c);
				retMap.put(key, subMap);
			}

			Map<String, ProductDTO> subMap = retMap.get(key);
			String subKey = p.getNikonLocale();
			subMap.put(subKey, p);
		}

		return retMap;
	}

	/**
	 * Products based on product navigation categories.
	 * @param param instance of HBN8QueryParamDTO
	 * @return List of ProductDTO
	 */
	public List<ProductDTO> listNavCatProducts(HBN8QueryParamDTO param)
	{
		log.debug("Entering List<ProductDTO> listNavCatProducts(HBN8QueryParamDTO param)");

		//Setup the session
		log.debug("Begin HBN8 Transaction");
		getSession().beginTransaction().begin();

		//WWA Date Filter
		log.debug("Begin HBN8 Transaction");

		//Set the WWA Date Filter
		enableDisableFilter(NikonHBN8FilterConstants.WWA_DATE, param.isEnableWWAFilter());

		//Get the products
		Query navCatQuery = null;

		log.debug(FormatUtils.mFormat("Using query:{0}", param.getRunQuery()));
		Collection<String> possibleLocales = localeEscalationResolver(param);
		log.debug(FormatUtils.collection2String(FormatUtils.mFormat("Country Code {0}", param.getSiteCountryCode()), possibleLocales));

		String namedQueryToUse = null;
		if(NikonDomainConstants.RN_QRY_VAL_LVL_0.equals(param.getRunQuery()))
		{
			namedQueryToUse = NikonHBN8ParamConstants.NAV_CAT_1;
			navCatQuery = getSession().getNamedQuery(namedQueryToUse);
			navCatQuery.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
		}
		else if(NikonDomainConstants.RN_QRY_VAL_LVL_1.equals(param.getRunQuery()))
		{
			namedQueryToUse = NikonHBN8ParamConstants.NAV_CAT_1;
			navCatQuery = getSession().getNamedQuery(namedQueryToUse);
			navCatQuery.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
		}
		else if(NikonDomainConstants.RN_QRY_VAL_LVL_2.equals(param.getRunQuery()))
		{
			namedQueryToUse = NikonHBN8ParamConstants.NAV_CAT_2;
			navCatQuery = getSession().getNamedQuery(namedQueryToUse);
			navCatQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_1, param.getNavCat1());
			navCatQuery.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
		}
		else if(NikonDomainConstants.RN_QRY_VAL_LVL_3.equals(param.getRunQuery()))
		{
			namedQueryToUse = NikonHBN8ParamConstants.NAV_CAT_3;
			navCatQuery = getSession().getNamedQuery(namedQueryToUse);
			navCatQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_1, param.getNavCat1());
			navCatQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_2, param.getNavCat2());
			navCatQuery.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
		}
		else if(NikonDomainConstants.RN_QRY_VAL_LVL_4.equals(param.getRunQuery()))
		{
			namedQueryToUse = NikonHBN8ParamConstants.NAV_CAT_4;
			navCatQuery = getSession().getNamedQuery(namedQueryToUse);
			navCatQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_1, param.getNavCat1());
			navCatQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_2, param.getNavCat2());
			navCatQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_3, param.getNavCat3());
			navCatQuery.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);
		}

		List<ProductDTO> navProductDetails = null;

		// Catch all to make sure we chose a query coming in
		if(navCatQuery != null)
		{
			//Common parameters
			navCatQuery.setParameter(NikonHBN8ParamConstants.PRD_CAT, param.getCategory());

			log.debug(FormatUtils.mFormat("Calling HBN8 query {0}", namedQueryToUse));
			navProductDetails = (List<ProductDTO>)filterUnwantedLocales(navCatQuery.list(), new LocaleComparator(possibleLocales), ProductDTO.class);
			
			removeLocaleOverrideProducts(getSession(),navProductDetails, param.getLanguageCountryCode(), param.getCategory());
			removeLocaleOptOutProducts(navProductDetails);
			
			FormatUtils.pFormat("Named Query:{0}", namedQueryToUse);
			FormatUtils.pFormat("{0}:{1}", NikonHBN8ParamConstants.PRD_CAT, param.getCategory());
			FormatUtils.pFormat("{0}:{1}", NikonHBN8ParamConstants.NAV_CAT_1, param.getNavCat1());
			FormatUtils.pFormat("{0}:{1}", NikonHBN8ParamConstants.NAV_CAT_2, param.getNavCat2());
			FormatUtils.pFormat("{0}:{1}", NikonHBN8ParamConstants.NAV_CAT_3, param.getNavCat3());
			FormatUtils.pFormat("param:{0}", param.toString());
		}

		log.debug("Closing HBN8 session");
		getSession().close();

		log.debug("Assembling results");
		com.interwoven.teamsite.nikon.dto.builders.ProductDTOAssembler assembler = new com.interwoven.teamsite.nikon.dto.builders.ProductDTOAssembler();
		assembler.setTeamsiteEnvironment(param.getTeamsiteEnvironment());
		assembler.setProductDTO(navProductDetails);

		log.debug("Exiting List<ProductDTO> listNavCatProducts(HBN8QueryParamDTO param)");
		
		return assembler.assembleMulti();
	}

	/**
	 * Products for the Catalogue. works various levels
	 * of aggregation
	 * and update here
	 * @param param instance of HBN8QueryParamDTO
	 * @return List of ProductDTO
	 */
	public List<ProductDTO> listCatalogueProducts(HBN8QueryParamDTO param)
	{
		log.debug("Entering List<ProductDTO> listCatalogueProducts(HBN8QueryParamDTO param)");

		//Setup the session
		log.debug("Begin HBN8 Transaction");
		getSession().beginTransaction().begin();

		//Set the WWA Date Filter
		enableDisableFilter(NikonHBN8FilterConstants.WWA_DATE, param.isEnableWWAFilter());

		//Get the products
		Query catQuery = null;

		String namedQueryToUse = null;

		namedQueryToUse = NikonHBN8QueryConstants.PRD_CAT_1;

		if(!param.getNavCat1().equals("0")){
			namedQueryToUse = NikonHBN8QueryConstants.PRD_CAT_2;
		}

		if(!param.getNavCat2().equals("0")){
			namedQueryToUse = NikonHBN8QueryConstants.PRD_CAT_3;
		}

		if(!param.getNavCat3().equals("0")){
			namedQueryToUse = NikonHBN8QueryConstants.PRD_CAT_4;
		}

		log.info("Parameter Values");
		log.info("================");
		log.info(FormatUtils.mFormat("namedQueryToUse     :{0}", namedQueryToUse));
		catQuery = getSession().getNamedQuery(namedQueryToUse);

		if("nikonProdCat2".equals(namedQueryToUse))
		{
			catQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_1, param.getNavCat1());
		}
		else if("nikonProdCat3".equals(namedQueryToUse))
		{
			catQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_1, param.getNavCat1());
			catQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_2, param.getNavCat2());
		}
		else if("nikonProdCat4".equals(namedQueryToUse))
		{
			catQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_1, param.getNavCat1());
			catQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_2, param.getNavCat2());
			catQuery.setParameter(NikonHBN8ParamConstants.NAV_CAT_3, param.getNavCat3());
		}

		Collection<String> possibleLocales = localeEscalationResolver(param);
		log.debug(FormatUtils.collection2String(FormatUtils.mFormat("Country Code {0}", param.getSiteCountryCode()), possibleLocales));
		catQuery.setParameter("nikonProductCategory", param.getCategory());
		catQuery.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, possibleLocales);


		log.info(FormatUtils.mFormat("nikonProductCategory:{0}", param.getCategory()));
		log.info(FormatUtils.mFormat("nikonNavCat1        :{0}", param.getNavCat1()));
		log.info(FormatUtils.mFormat("nikonNavCat2        :{0}", param.getNavCat2()));
		for(String s : localeEscalationResolver(param))
		{
			log.info(FormatUtils.mFormat("nikonLocales        :{0}", s));
		}

		List<ProductDTO> catProductDetails = (List<ProductDTO>)filterUnwantedLocales(catQuery.list(), new LocaleComparator(possibleLocales), ProductDTO.class);
		
		removeLocaleOverrideProducts(getSession(), catProductDetails, param.getLanguageCountryCode(), param.getCategory());
		removeLocaleOptOutProducts(catProductDetails);
		
		log.debug("Closing HBN8 session");
		getSession().close();

		log.debug("Assembling results");
		ProductDTOAssembler assembler = new ProductDTOAssembler();
		assembler.setTeamsiteEnvironment(param.getTeamsiteEnvironment());
		assembler.setProductDTO(catProductDetails);
		log.debug("Exiting List<ProductDTO> listCatalogueProducts(HBN8QueryParamDTO param)");
		
		return assembler.assembleMulti();
	}

	/**
	 * Service method to resolve locales based on the locale escalation rules.
	 * @param param
	 * @return
	 */
	public Collection<String> localeEscalationResolver(HBN8QueryParamDTO param)
	{
		//Currently this is resolved through the Localeresolver class. Ultimatley
		//It should be coming from the DCR localisation_fallback_rules
		return LocaleResolver.resolvePossibleLocales(param);
	}

	//Test methods. Depreciated
	@Deprecated
	public List<ProductDTO> listTestFilter(HBN8QueryParamDTO param, boolean enableTestFilter)
	{
		log.debug("Entering List<ProductDTO> listTestFilter(HBN8QueryParamDTO param, boolean enableTestFilter)");


		//Setup the session
		getSession().beginTransaction().begin();

		//Set the WWA Date Filter
		enableDisableFilter("testFilter", enableTestFilter);

		Query singleProductQry = getSession().getNamedQuery(NikonHBN8QueryConstants.MLT_PRD_PRD_ID_LOC);
		singleProductQry.setParameterList(NikonHBN8ParamConstants.PRODUCT_IDS, param.getProductIds());
		singleProductQry.setParameterList(NikonHBN8ParamConstants.NKN_LOCALES, param.getNikonLocales());

		//Run the query, we're expecting just the one here, anything else and we need to get the default
		List<ProductDTO> queryList = singleProductQry.list();

		log.debug("Exiting List<ProductDTO> listTestFilter(HBN8QueryParamDTO param, boolean enableTestFilter)");
		return queryList;
	}


	/*
	 * Private helper methods
	 */

	//Helper method to fill a collection with a single value in a Collection<String> from a single bean given the field name
	private Collection<String> beanFieldToListOfStrings(Object object, String fieldName, boolean ignoreDuplicates)
	{
		List<Object> objects = new LinkedList<Object>();
		objects.add(object);
		return listOfBeansFieldToListOfStrings(objects, fieldName, ignoreDuplicates);
	}

	private Collection<String> beanFieldToListOfStrings(Object object, String fieldName)
	{
		return beanFieldToListOfStrings(object, fieldName, false);
	}

	//Helper method to fill a Collection<String> with values from a list of beans given the fieldName
	private Collection<String> listOfBeansFieldToListOfStrings(List<?> objects, String fieldName)
	{
		return listOfBeansFieldToListOfStrings(objects, fieldName, false);
	}
	private Collection<String> listOfBeansFieldToListOfStrings(List<?> objects, String fieldName, boolean ignoreDuplicates)
	{
		Collection<String> retList = new LinkedList<String>();
		for(Object o: objects)
		{
			try {

				//If we're ignoring and it's not already in then put it in
				if(ignoreDuplicates && !retList.contains(BeanUtils.getProperty(o, fieldName)))
				{
					retList.add(BeanUtils.getProperty(o, fieldName));
				}
				else
				{
					retList.add(BeanUtils.getProperty(o, fieldName));
				}

			} catch (IllegalAccessException e) {
				log.error(FormatUtils.mFormat("Unable to access field {0} in bean", fieldName), e);
			} catch (InvocationTargetException e) {
				log.error(FormatUtils.mFormat("Unable to access field {0} in bean", fieldName), e);
			} catch (NoSuchMethodException e) {
				log.error(FormatUtils.mFormat("Unable to access field {0} in bean", fieldName), e);
			}
		}
		//TODO this is a quick dirty fix
		if(retList.size() == 0)
		{
			retList.add("-1");
		}
		return retList;
	}

	//Method to resolve a locale given a starting locale
	private Object resolveLocale(Iterator<?> listIter, String fieldName, String countryCode, String langCountryCode)
	throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		Object prod = null;

		while(listIter.hasNext() && prod == null)
		{

			Object pd = listIter.next();
			for(String locale: LocaleResolver.resolvePossibleLocales(countryCode, langCountryCode))
			{
				if(locale.equals(BeanUtils.getProperty(pd, fieldName)))
				{
					prod = pd;
				}
			}
		}
		return prod;
	}
	/**
	 * @author nbamford
	 *
	 * Private Comparator class which sorts data based on
	 * the locale as defined in the the {@link
	 * LocaleResolver}
	 *
	 */
	private class ProductLocaleComparator
	implements Comparator
	{
		@SuppressWarnings("unused")
		private Collection<String> c;
		private Map<String, String> a;
		private boolean asc;

		public ProductLocaleComparator(Collection<String> c)
		{
			this(c, false);
		}

		public ProductLocaleComparator(Collection<String> c, boolean asc)
		{
			this.asc = asc;
			this.c = c;
			a = new LinkedHashMap<String, String>();
			int count = 0;
			for(String s: c)
			{
				a.put(s, "" + (count++));
			}
		}

		public int compare(Object arg0, Object arg1) {
			// TODO Auto-generated method stub
			ProductDTO p0 = (ProductDTO)arg0;
			ProductDTO p1 = (ProductDTO)arg1;
			int i0 = Integer.parseInt(a.get(p0.getNikonLocale()));
			int i1 = Integer.parseInt(a.get(p1.getNikonLocale()));
			return asc?i1 - i0:i0 - i1;
		}
	}

	
	//	private List<PressLibraryDTO> filterUnwantedLocales(List<PressLibraryDTO> prodList, Comparator comparator)
	//	{
	//		Map<String, Map<String, ProductDTO>> map = putProdDTOInMapIndexByProdId(prodList, comparator);
	//
	//		List<ProductDTO> retList = new LinkedList<ProductDTO>();
	//		for(String key: map.keySet())
	//		{
	//			ProductDTO pdto = map.get(key).entrySet().iterator().next().getValue();
	//			retList.add(pdto);
	//		}
	//
	//		return retList;
	//	}

	private List<?> filterUnwantedLocales(List<?> list, Comparator comparator, Class clazz)
	{
		List<?> retList = null;

		if(clazz == ProductDTO.class)
		{
			List<ProductDTO> prodList = (List<ProductDTO>)list;
			Map<String, Map<String, ProductDTO>> map = putProdDTOInMapIndexByProdId(prodList, comparator);
			List<ProductDTO> retListProduct = new LinkedList<ProductDTO>();
			for(String key: map.keySet())
			{
				ProductDTO pdto = map.get(key).entrySet().iterator().next().getValue();
				retListProduct.add(pdto);
			}
			retList = retListProduct;
		}
		
		return retList;
	}

	private class LocaleComparator implements Comparator
	{
		@SuppressWarnings("unused")
		private Collection<String> c;
		private Map<String, String> a;
		private boolean asc;

		public LocaleComparator(Collection<String> c)
		{
			this(c, false);
		}

		public LocaleComparator(Collection<String> c, boolean asc)
		{
			this.asc = asc;
			this.c = c;
			a = new LinkedHashMap<String, String>();
			int count = 0;
			for(String s: c)
			{
				a.put(s, "" + (count++));
			}
		}

		public int compare(Object arg0, Object arg1) {
			int i0 = Integer.parseInt(a.get(arg0));
			int i1 = Integer.parseInt(a.get(arg1));
			return asc?i1 - i0:i0 - i1;
		}
	}

	//Method to enable/disable a HBN8 filter
	private void enableDisableFilter(String filterName, boolean state)
	{
		if(state)
		{
			log.debug(FormatUtils.mFormat("Enabling filter {0}", filterName));
			getSession().enableFilter(filterName);
		}
		else
		{
			log.debug(FormatUtils.mFormat("Disabling filter {0}", filterName));

			getSession().disableFilter(filterName);
			log.debug(FormatUtils.mFormat("Disabled"));
		}
	}
	
	/**
	 * This will remove any products that should not be in the final list because the locale version should control the discontinued function.
	 * <p>
	 * It may be possible that a product is marked as <b>discontinued</b> in a fallback locale but <b>active</b> in the locale,
	 * this would result in the fallback version of a product being listed in the Discontinued section of the explorer block.
	 * </p>
	 * <p>
	 * Conversely it may be possible that a product is marked as <b>active</b> in a fallback locale but <b>discontinued</b> at the locale level,
	 * this would result in the fallback version of a product being listed in the Products section of the explorer block.
	 * </p>
	 * <p>
	 * This method will remove any products (prodIds) that should not be visible based on the state of the locale version.
	 * </p>
	 * @param session 
	 * 
	 * @param productsList The list of products (which may contain active products)
	 * @param languageCountryCode The xx_XX language country code used to find products for that locale
	 * @param category The category of product being searched for
	 * @param session The Hibernate Session instance to use to do the query
	 */
	private void removeLocaleOverrideProducts(Session session, List<ProductDTO> productsList, String languageCountryCode, String category) {
		
		log.debug(String.format("About to remove discontinued DCRs that conflict with locale: %s version" ,languageCountryCode));
		
		boolean discontinued = NikonHBN8ParamConstants.DISCONTINUED.equals(category);
		
		// Get the 'Product.nikonRemavalOverrideProductIDsForLocale' query which will retrieve a list of Prod IDs that should be removed
		Query query = session.getNamedQuery(NikonHBN8QueryConstants.NKN_REMOVALOVERRIDE_PRD_ID_LCL);
		
		// Set the :nikonLocale parameter to the passed in locale (should be xx_XX)
		query.setParameter(NikonHBN8ParamConstants.NKN_LOCALE, "en_Asia");

		// Set the :discontinued parameter to the opposite of is discontinued
		query.setParameter("discontinued", !discontinued);
		
		//log.debug("Discontinued Flag: " + !discontinued);
		
		// Get a list of prod ids from the locale that should not appear in the final list
		List<String> removalProdIDsInLocaleList = (List<String>)query.list();
		log.debug(String.format("There are %d Asia products", removalProdIDsInLocaleList.size()));
		
		/*for (String discontinuedProdID : removalProdIDsInLocaleList) {
			log.debug("ProdID: " + discontinuedProdID);
		}*/
		
		log.debug("Looping through the navProductDetails list checking to see if the prodID is active");
		for (Iterator<ProductDTO> iterator = productsList.iterator(); iterator.hasNext();) {
			ProductDTO productDTO = (ProductDTO) iterator.next();
			
			//log.debug("Fallback ProdID:" + productDTO.getProdId());
			
			// Remove any products from the list that are in the removal list
			if(removalProdIDsInLocaleList.contains(productDTO.getProdId())){
				iterator.remove();
				log.debug(String.format("Removed product id:%s", productDTO.getProdId()));
			}
		}
		
		log.debug("Removed any discontinued products that were not needed");
		
	}
	
	/**
	 * Removes any Products (ProductDTO) that are set to be locale opt-out as these products should not be displayed for the locale. 
	 * 
	 * 
	 * <p>This method should be called whenever there is a query to return ProductDTO objects so it will remove any products that 
	 * are marked as locale opt-out. This method should be called after the filterUnwantedLocales method call as that call
	 * will return a 'flattened' list of ProductDTOs including the most suitable fallback, 
	 * this then implies that the locale op-out option is inherited from a fallback so if a fallback is set to locale opt-out it will
	 * not be displayed on any locale that uses that locale in its fallback rules (ie: will not fallback to the next suitable non 
	 * locale opt-out fallback).</p>
	 * 
	 * @param productsList A list of ProductDTOs that should have all locale opt-out products removed.
	 */
	private void removeLocaleOptOutProducts(List<ProductDTO> productsList) {
		log.debug("Looping through the productsList list removing any products marked as locale opt-out...");
		for (Iterator<ProductDTO> iterator = productsList.iterator(); iterator.hasNext();) {
			ProductDTO productDTO = (ProductDTO) iterator.next();

			if(productDTO.getProdLocaleOptOut()!= null) {
				iterator.remove();
				log.debug(String.format("Removed Local Product id:%s", productDTO.getProdId()));
			}
		}
		log.debug("Removed any products that are marked as locale opt-out");
	}

	public String getProductDCRByIdAndLocale(HBN8QueryParamDTO param) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
