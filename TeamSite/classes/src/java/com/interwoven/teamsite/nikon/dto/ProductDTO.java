package com.interwoven.teamsite.nikon.dto;

import static com.interwoven.teamsite.nikon.dto.CommonDTOFields.id;
import static com.interwoven.teamsite.nikon.dto.CommonDTOFields.nikonLocale;
import static com.interwoven.teamsite.nikon.dto.CommonDTOFields.path;
import static com.interwoven.teamsite.nikon.dto.CommonDTOFields.wwaDate;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.hibernate.beans.Product;

/**
 * @author nbamford
 * DTO version of the {@link Product} bean
 */
public class ProductDTO 
extends Product
implements CommonDTOFields, TeamsiteEnvironment
{

	//List of static bean field names for the HBN8 pk and more useful index index field
	//Kind of OK, but should inherit XMLEmitable and create the XML from there
	public static final String pk              = "id";
	public static final String fk              = "prodProdId";
	public static final String index           = "prodId";
	public static final String type            = "type";
	public static final String prodShortName   = "prodShortName";
	public static final String localShortName  = "localShortName";
	public static final String isNew           = "isNew";
	public static final String productCategory = "productCategory";
	public static final String navCat1         = "navCat1";
	public static final String navCat2         = "navCat2";
	public static final String navCat3         = "navCat3";
	public static final String isKit           = "isKit";
	public static final String upc             = "upc";
	public static final String productTitle    = "product_title";
	
	//Need putting in Product class via Hibernate Mapping file

	//List of compatible accessories
	private List<ProductDTO> accessories = new LinkedList<ProductDTO>();
	//Related products are actuall available versions/kits
	private List<ProductDTO> relatedProducts = new LinkedList<ProductDTO>();
	//Stuff that comes in the box
	private List<ProductDTO> billOfMaterials = new LinkedList<ProductDTO>();
	//Marketing related prducts, based on accessories functionality
	private List<ProductDTO> productMarketingRelated = new LinkedList<ProductDTO>();
	//Marketing related (reverse relationship, so which products is marketing related to this product)
	private List<ProductDTO> productMarketingRelatedReverse = new LinkedList<ProductDTO>();
	//If we're an accessory then a list of products which can be used with me
	private List<ProductDTO> accessoryOf = new LinkedList<ProductDTO>();

	public ProductDTO()
	{
	}
	
	public ProductDTO(Product product)
	{
		BeanUtils.copyProperties(product, this);
	}
	
	public ProductDTO(Product product, String env)
	{
		this(product);
		this.env = env;
	}

	public List<ProductDTO> getAccessories() {
		return accessories;
	}

	public void setAccessories(List<ProductDTO> accessories) {
		this.accessories = accessories;
	}

	public List<ProductDTO> getRelatedProducts() {
		return relatedProducts;
	}

	public void setRelatedProducts(List<ProductDTO> relatedProducts) {
		this.relatedProducts = relatedProducts;
	}

	public List<ProductDTO> getBillOfMaterials() {
		return billOfMaterials;
	}

	public void setBillOfMaterials(List<ProductDTO> billOfMaterials) {
		this.billOfMaterials = billOfMaterials;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(FormatUtils.mFormat("id:{0},", getId()));
		sb.append(FormatUtils.mFormat("prodId:{0},",getProdId()));
		sb.append(FormatUtils.mFormat("prodDevCode:{0},",getProdDevCode()));
		sb.append(FormatUtils.mFormat("upc:{0},",getUpc()));
		sb.append(FormatUtils.mFormat("description:{0},",getDescription()));
		sb.append(FormatUtils.mFormat("type:{0},",getType()));
		sb.append(FormatUtils.mFormat("wwaDate:{0},",FormatUtils.formatWWADate(getWwaDate())));
		sb.append(FormatUtils.mFormat("title:{0},",getTitle()));
		sb.append(FormatUtils.mFormat("discontinued:{0},",isDiscontinued()));
		sb.append(FormatUtils.mFormat("prodShortCode:{0},",getProdShortCode()));
		sb.append(FormatUtils.mFormat("prodShortName:{0},",getProdShortName()));
		sb.append(FormatUtils.mFormat("prodAccessoryOf:{0},",getAccessoryOf()));
		sb.append(FormatUtils.mFormat("nikonLocale:{0},",getNikonLocale()));
		sb.append(FormatUtils.mFormat("path:{0},",getPath()));
		sb.append(FormatUtils.mFormat("sortOrder:{0},",getSortOrder()));
		sb.append(FormatUtils.mFormat("stillNew:{0},",isStillNew()));
		sb.append(FormatUtils.mFormat("localShortName:{0},",getLocalShortName()));
		sb.append(FormatUtils.mFormat("accessory:{0},",isAccessory()));
		sb.append(FormatUtils.mFormat("navCat1:{0},",getNavCat1()));
		sb.append(FormatUtils.mFormat("navCat2:{0},",getNavCat2()));
		sb.append(FormatUtils.mFormat("navCat3:{0},",getNavCat3()));
		sb.append(FormatUtils.mFormat("kit:{0},",isKit()));
		sb.append(FormatUtils.mFormat("productCategory:{0},",getProductCategory()));
		sb.append(FormatUtils.mFormat("prodLocaleOptOut:{0},",getProdLocaleOptOut()));
		sb.append(FormatUtils.mFormat("migrated:{0},",isMigrated()));
		sb.append(FormatUtils.mFormat("localProduct:{0},",isLocalProduct()));
		
		return sb.toString();
	}

	public List<ProductDTO> getAccessoryOf() {
		return accessoryOf;
	}

	public void setAccessoryOf(List<ProductDTO> accessoryOf) {
		this.accessoryOf = accessoryOf;
	}
	
	public List<ProductDTO> getProductMarketingRelated() {
		return productMarketingRelated;
	}

	public void setProductMarketingRelated(List<ProductDTO> productMarketingRelated) {
		this.productMarketingRelated = productMarketingRelated;
	}
	

	// NB 20090820 Added Concept of an environment so that the component can render differently;
	private String env = TeamsiteEnvironment.PRODUCTION;
	
	public String getEnvironment() {
		return env;
	}
	
	public boolean isDevelopment() {
		return TeamsiteEnvironment.DEVELOPMENT.equals(env);
	}

	public boolean isProduction() {
		return TeamsiteEnvironment.PRODUCTION.equals(env);
	}

	public boolean isStaging() {
		return TeamsiteEnvironment.STAGING.equals(env);
	}

	public boolean isTesting() {
		return TeamsiteEnvironment.TESTING.equals(env);
	}

	public void setEnvironment(String environment) {
		this.env = environment;
	}

	public List<ProductDTO> getProductMarketingRelatedReverse() {
		return productMarketingRelatedReverse;
	}

	public void setProductMarketingRelatedReverse(
			List<ProductDTO> productMarketingRelatedReverse) {
		this.productMarketingRelatedReverse = productMarketingRelatedReverse;
	}

}
