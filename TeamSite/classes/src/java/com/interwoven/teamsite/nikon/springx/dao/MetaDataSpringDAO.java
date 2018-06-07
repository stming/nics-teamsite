package com.interwoven.teamsite.nikon.springx.dao;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.ext.util.Utils;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.springx.rowmappers.ProductDTORowMapper;
import com.interwoven.teamsite.nikon.springx.rowmappers.ProductDTOWWABeforeRowMapper;
import com.interwoven.teamsite.nikon.util.NikonUtils;

/**
 * ProductDTO DAO which makes use of Core Spring JDBC classes
 * @author nbamford
 *
 */
public class MetaDataSpringDAO
{
	private Log log = LogFactory.getLog(MetaDataSpringDAO.class);
	/**
	 * Default constructor 
	 */
	public MetaDataSpringDAO(){};

	private NamedParameterJdbcTemplate simpleJdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public List<ProductDTO> listAllProductAccessoriesInViewMetadataBeforeWWA()
	{


		String sql = "SELECT DISTINCT PROD_DEV_CODE AS PROD_DEV_CODE" +
		",ISNULL(LOCAL_PRODUCT, 0) AS LOCAL_PROD" +
		",ISNULL(MIGRATED, 0)      AS MIGRATED " +
		"FROM V_METADATA_BEFORE_WWA " +
		"WHERE PRODUCT_TYPE = 'Accessory' OR PRODUCT_TYPE = 'Product' " +
		"ORDER BY PROD_DEV_CODE ASC";

		Map paramMap = null;
		List<ProductDTO> retList = simpleJdbcTemplate.query(sql, paramMap, new ProductDTOWWABeforeRowMapper());
		
		return retList;
	}
	/**
	 * Finder method to return a List<ProductDTO> given a CSV sarparated list of ProductDevCodes
	 * @param csvProdDevCodes comma separated list of product dev codes
	 * @return List<ProductDTO>
	 */
	public List<ProductDTO> findProductsByProdDevCode(String csvProdDevCodes) 
	{
		log.debug("Entering public List<ProductDTO> findProductsByProdDevCode(String csvProdDevCodes)");
		
		//Turn the CSV list of prodDevCodes into a List<String>
		csvProdDevCodes = NikonUtils.cleanCSVProdDevCode(csvProdDevCodes);
		log.debug(FormatUtils.mFormat("Setting prodDevCodes:{0}", csvProdDevCodes));
		
		//Query Param Map
		Map<String, Object> paramMap = new WeakHashMap<String, Object>();
		List<String> prodDevCodeList= Utils.stringArrayToList(csvProdDevCodes.split(","));
		paramMap.put("prodDevCodes", prodDevCodeList);
		
		String sql = "SELECT ID" +
		", PROD_ID" +
		", PROD_DEV_CODE" +
		", PATH" +
		", LOCAL_PROD" +
		", MIGRATED " +
		"FROM V_PRODUCT " +
		"WHERE ((NIKON_LOCALE = 'en_Asia') " +
		"OR ((LOCAL_PROD = 1) AND (LOCAL_PROD_LANG_COUNTRY = REVERSE(NIKON_LOCALE)))) " +
		"AND PROD_DEV_CODE IN (:prodDevCodes) " +
		"ORDER BY PROD_DEV_CODE ASC";

		List<ProductDTO> retList = simpleJdbcTemplate.query(sql, paramMap, new ProductDTORowMapper());
		log.debug(FormatUtils.mFormat("List<ProductDTO>.size():{0}", ((retList != null)&&(retList.size() > 0))?"" + retList.size():"Empty or Null"));
		
		log.debug("Entering public List<ProductDTO> findProductsByProdDevCode(String csvProdDevCodes)");
		return retList;
	}		
	
	/**
	 * Returns a Map<String, ProductDRO> where the key is the product dev code
	 * @param csvProdDevCodes
	 * @return
	 */
	public Map<String, ProductDTO> mapOfProductDevCodesAsProductDTO(String csvProdDevCodes)
	{
		Map<String, ProductDTO> retMap = new WeakHashMap<String, ProductDTO>();
		
		for(ProductDTO to: findProductsByProdDevCode(csvProdDevCodes))
		{
			retMap.put(to.getProdDevCode(), to);
		}
		
		return retMap;
	}
	
	/**
	 * Finder method to return the maximum (furthest away) WWA Date for a a CSV separated list of ProductDevCodes and a NikonLocale (for local products)
	 * @param csvProdDevCodes comma separated list of product dev codes
	 * @param nikonLocale - Locale we're finding from
	 * @return Date 
	 */
	public Date findMaxWWADateForCSVProdDevCodes(String csvProdDevCodes, String nikonLocale)
	{
		log.debug("Entering public Date findMaxWWADateForCSVProdDevCodes(String csvProdDevCodes, String nikonLocale)");
		log.debug(FormatUtils.mFormat("Setting csvProdDevCodes:{0}", csvProdDevCodes));
		log.debug(FormatUtils.mFormat("Setting nikonLocale:{0}", nikonLocale));
		Map<String, String> paramMap = new WeakHashMap<String, String>();
		paramMap.put("productDevCodesCSV", csvProdDevCodes);
		paramMap.put("nikonLocale", nikonLocale);
		
		String sql = "SELECT [dbo].WWA_DATE_CHECK(:productDevCodesCSV, :nikonLocale) AS PROD_WWA_DATE";
		Date retVal = (Date)simpleJdbcTemplate.queryForObject(sql, paramMap, Date.class); 
		log.debug(FormatUtils.mFormat("retVal:{0}", FormatUtils.formatWWADate(retVal)));	
		log.debug("Exiting public Date findMaxWWADateForCSVProdDevCodes(String csvProdDevCodes, String nikonLocale)");

		return retVal;
	}
}
