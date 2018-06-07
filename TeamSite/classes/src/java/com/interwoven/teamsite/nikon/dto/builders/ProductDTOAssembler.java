package com.interwoven.teamsite.nikon.dto.builders;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2Accessory;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2Bom;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2Product;
import com.interwoven.teamsite.nikon.hibernate.beans.Product2ProductMarketingRelated;

/**
 * This class is responsible for the assembling of the ProductDTO given a list of 
 * @author nbamford
 *
 */
public class ProductDTOAssembler 
{
	private Log log = LogFactory.getLog(ProductDTOAssembler.class);
	private TeamsiteEnvironment teamsiteEnvironment;
	//Single
	private ProductDTO productDTO;
	//List
	private List<ProductDTO> products;
	
	//Accessories and linkage
	private List<ProductDTO> accessories = new LinkedList<ProductDTO>();
	private List<Product2Accessory> product2accessories;
	
	//Related and linkage
	private List<ProductDTO> relatedProducts = new LinkedList<ProductDTO>();
	private List<Product2Product> product2product;
	
	//BOM and linkage
	private List<ProductDTO> billOfMaterials = new LinkedList<ProductDTO>();
	private List<Product2Bom> product2bom;

	//AccessoryOf
	private List<ProductDTO> accessoryOf = new LinkedList<ProductDTO>();
	private List<Product2Accessory> accessory2Product;
	
	// Product Marketing Related
	private List<ProductDTO> marketingRelatedProducts = new LinkedList<ProductDTO>();
	private List<Product2ProductMarketingRelated> product2marketingRelatedProduct;

	// Product Marketing Related (Reverse)
	private List<ProductDTO> marketingRelatedProductsReverse = new LinkedList<ProductDTO>();
	private List<Product2ProductMarketingRelated> product2marketingRelatedProductReverse;
	
	//Assemblers
	
	//Single. If productDTO not set then return an empty one
	public ProductDTO assembleSingle()
	{
		ProductDTO retVal = null;
		
		if(productDTO != null)
		{
			retVal = singleBuild(productDTO);
		}
		else
		{
			
		}
		
		return retVal;
	}
	
	//Multi. If products not set then return empty LinkedList
	public List<ProductDTO> assembleMulti()
	{
		List<ProductDTO> retVal = new LinkedList<ProductDTO>();
		
		//Multiple
		if(products != null)
		{
			retVal = multiBuild(products);
		}
		
		return retVal;
	}
	
	//Builds a single by delegating to the multi build
	private ProductDTO singleBuild(ProductDTO product)
	{	
		List<ProductDTO> list = new ProductDTOAssembler().assembleMulti();
		list.add(product);
		list = multiBuild(list);
		return list.get(0); 
	}
	
	//Takes all the bits and puts them together
	private List<ProductDTO> multiBuild(List<ProductDTO> products)
	{
		List<ProductDTO> retList = new LinkedList<ProductDTO>();
		
		Map<String, ProductDTO> accessoriesMap = new LinkedHashMap<String, ProductDTO>();
		Map<String, List<ProductDTO>> accessoriesListMap = new LinkedHashMap<String, List<ProductDTO>>();
		
		Map<String, ProductDTO> relProdMap = new LinkedHashMap<String, ProductDTO>();
		Map<String, List<ProductDTO>> relProductsListMap = new LinkedHashMap<String, List<ProductDTO>>();
		
		Map<String, ProductDTO> bomProdMap = new LinkedHashMap<String, ProductDTO>();
		Map<String, List<ProductDTO>> bomProductsListMap = new LinkedHashMap<String, List<ProductDTO>>();
		
		Map<String, ProductDTO> accessoryOfMap = new LinkedHashMap<String, ProductDTO>();
		Map<String, List<ProductDTO>> accessoryOfListMap = new LinkedHashMap<String, List<ProductDTO>>();
		
		Map<String, ProductDTO> productMarketingRelatedMap = new LinkedHashMap<String, ProductDTO>();
		Map<String, List<ProductDTO>> productMarketingRelatedListMap = new LinkedHashMap<String, List<ProductDTO>>();
		
		Map<String, ProductDTO> productMarketingRelatedMapReverse = new LinkedHashMap<String, ProductDTO>();
		Map<String, List<ProductDTO>> productMarketingRelatedListMapReverse = new LinkedHashMap<String, List<ProductDTO>>();
		
		//Create a map of the relationships
		//Accessories and a Map of the accessories to build later
		if(product2accessories != null)
		{
			for(ProductDTO accessory: accessories)
			{
				if(accessory != null)accessoriesMap.put(accessory.getProdId(), accessory);
			}

			//Loop through product to accessory
			for(Product2Accessory p2a: product2accessories)
			{
				List<ProductDTO> mapList = null;
				
				//Look for the linked list of accessories 
				//if it doesn't exists create and use it
				//if it does then reference it
				if(accessoriesListMap.get(p2a.getProdProdId()) == null)
				{
					mapList = new LinkedList<ProductDTO>();
					accessoriesListMap.put(p2a.getProdProdId(), mapList);
				}
				else
				{
					mapList = accessoriesListMap.get(p2a.getProdProdId()); 
				}
				
				//Add the accessory ProductDTO to the linked list
				if(accessoriesMap.get(p2a.getProdId()) != null)mapList.add(accessoriesMap.get(p2a.getProdId()));
			}
			
		}
		
		//Related Products
		if(product2product != null)
		{
			for(ProductDTO relProd: relatedProducts)
			{
				if(relProd != null)relProdMap.put(relProd.getProdId(), relProd);
			}

			//Loop through product to product
			for(Product2Product p2p: product2product)
			{
				List<ProductDTO> mapList = null;
				
				//Look for the linked list of accessories 
				//if it doesn't exists create and use it
				//if it does then reference it
				if(relProductsListMap.get(p2p.getProdProdId()) == null)
				{
					mapList = new LinkedList<ProductDTO>();
					relProductsListMap.put(p2p.getProdProdId(), mapList);
				}
				else
				{
					mapList = relProductsListMap.get(p2p.getProdProdId()); 
				}
				
				//Add the accessory ProductDTO to the linked list
				if(relProdMap.get(p2p.getProdId()) != null)mapList.add(relProdMap.get(p2p.getProdId()));
			}
		}

		//Bill Of Materials Products
		if(product2bom != null)
		{
			for(ProductDTO bomProd: billOfMaterials)
			{
				if(bomProd != null)bomProdMap.put(bomProd.getProdId(), bomProd);
			}

			//Loop through product to bom product
			for(Product2Bom p2b: product2bom)
			{
				List<ProductDTO> mapList = null;
				
				//Look for the linked list of accessories 
				//if it doesn't exists create and use it
				//if it does then reference it
				if(bomProductsListMap.get(p2b.getProdProdId()) == null)
				{
					mapList = new LinkedList<ProductDTO>();
					bomProductsListMap.put(p2b.getProdProdId(), mapList);
				}
				else
				{
					mapList = bomProductsListMap.get(p2b.getProdProdId()); 
				}
				
				//Add the accessory ProductDTO to the linked list
				if(bomProdMap.get(p2b.getProdId()) != null)mapList.add(bomProdMap.get(p2b.getProdId()));
			}
		}

        if(accessory2Product != null)
        {
            for(ProductDTO accOfProd: accessoryOf)
            {
                if(accOfProd != null)accessoryOfMap.put(accOfProd.getProdId(), accOfProd);
            }

            //Loop through product to bom product
            for(Product2Accessory a2p: accessory2Product)
            {
                List<ProductDTO> mapList = null;
                
                //Look for the linked list of accessories 
                //if it doesn't exists create and use it
                //if it does then reference it
                if(accessoryOfListMap.get(a2p.getProdId()) == null)
                {
                    mapList = new LinkedList<ProductDTO>();
                    accessoryOfListMap.put(a2p.getProdId(), mapList);
                }
                else
                {
                    mapList = accessoryOfListMap.get(a2p.getProdId()); 
                }
                
                //Add the accessory ProductDTO to the linked list
                if(accessoryOfMap.get(a2p.getProdProdId()) != null)mapList.add(accessoryOfMap.get(a2p.getProdProdId()));
            }
        }
		
//        if(product2marketingRelatedProduct != null)
//        {
//            for(ProductDTO productMarketingRelatedOfProd: marketingRelatedProducts)
//            {
//                if(productMarketingRelatedOfProd != null)productMarketingRelatedMap.put(productMarketingRelatedOfProd.getProdId(), productMarketingRelatedOfProd);
//            }
//
//            //Loop through product to bom product
//            for(Product2ProductMarketingRelated p2pmr: product2marketingRelatedProduct)
//            {
//                List<ProductDTO> mapList = null;
//                
//                //Look for the linked list of marketing related product 
//                //if it doesn't exists create and use it
//                //if it does then reference it
//                if(productMarketingRelatedListMap.get(p2pmr.getProdId()) == null)
//                {
//                    mapList = new LinkedList<ProductDTO>();
//                    productMarketingRelatedListMap.put(p2pmr.getProdId(), mapList);
//                }
//                else
//                {
//                    mapList = productMarketingRelatedListMap.get(p2pmr.getProdId()); 
//                }
//                
//                //Add the accessory ProductDTO to the linked list
//                if(productMarketingRelatedMap.get(p2pmr.getProdProdId()) != null)mapList.add(productMarketingRelatedMap.get(p2pmr.getProdProdId()));
//            }
//        }
        
        if(product2marketingRelatedProduct != null)
        {
            for(ProductDTO marketingRelatedProduct: marketingRelatedProducts)
            {
                if(marketingRelatedProduct != null)
                {
                    productMarketingRelatedMap.put(marketingRelatedProduct.getProdId(), marketingRelatedProduct);
                }
            }

            //Loop through product to marketingRelatedProduct
            for(Product2ProductMarketingRelated p2mp: product2marketingRelatedProduct)
            {
                List<ProductDTO> mapList = null;
                
                //Look for the linked list of marketingRelatedProducts 
                //if it doesn't exists create and use it
                //if it does then reference it
                if(productMarketingRelatedListMap.get(p2mp.getProdProdId()) == null)
                {
                    mapList = new LinkedList<ProductDTO>();
                    productMarketingRelatedListMap.put(p2mp.getProdProdId(), mapList);
                }
                else
                {
                    mapList = productMarketingRelatedListMap.get(p2mp.getProdProdId()); 
                }
                
                //Add the marketingRelatedProduct ProductDTO to the linked list
                if(productMarketingRelatedMap.get(p2mp.getProdId()) != null)
                {
                    mapList.add(productMarketingRelatedMap.get(p2mp.getProdId()));
                }
            }
            
        }
        
        if(product2marketingRelatedProductReverse != null)
        {
            for(ProductDTO marketingRelatedProductReverse: marketingRelatedProductsReverse)
            {
                if(marketingRelatedProductReverse != null)
                {
                    productMarketingRelatedMapReverse.put(marketingRelatedProductReverse.getProdId(), marketingRelatedProductReverse);
                }
            }

            //Loop through product to marketingRelatedProduct
            for(Product2ProductMarketingRelated p2mp: product2marketingRelatedProductReverse)
            {
                List<ProductDTO> mapList = null;
                
                //Look for the linked list of marketingRelatedProducts 
                //if it doesn't exists create and use it
                //if it does then reference it
                if(productMarketingRelatedListMapReverse.get(p2mp.getProdId()) == null)
                {
                    mapList = new LinkedList<ProductDTO>();
                    productMarketingRelatedListMapReverse.put(p2mp.getProdId(), mapList);
                }
                else
                {
                    mapList = productMarketingRelatedListMapReverse.get(p2mp.getProdId()); 
                }
                
                //Add the marketingRelatedProduct ProductDTO to the linked list
                if(productMarketingRelatedMapReverse.get(p2mp.getProdProdId()) != null)
                {
                    mapList.add(productMarketingRelatedMapReverse.get(p2mp.getProdProdId()));
                }
            }
            
        }	
        addEnvironment(products);
		//Now loop through the products and add the accessories, related products, boms, accessoryOf and productMarketingRelated
		for(ProductDTO p: products)
		{
			List<ProductDTO> accessoryList = accessoriesListMap.get(p.getProdId()) != null?accessoriesListMap.get(p.getProdId()):new LinkedList<ProductDTO>();
			addEnvironment(accessoryList);
			p.setAccessories(accessoryList);
			
			List<ProductDTO> relProdList = relProductsListMap.get(p.getProdId()) != null?relProductsListMap.get(p.getProdId()):new LinkedList<ProductDTO>();
			addEnvironment(relProdList);
			p.setRelatedProducts(relProdList);
			
			List<ProductDTO> bomProdList = bomProductsListMap.get(p.getProdId()) != null?bomProductsListMap.get(p.getProdId()):new LinkedList<ProductDTO>();
			addEnvironment(bomProdList);
			p.setBillOfMaterials(bomProdList);

			List<ProductDTO> accessoryOfList = accessoryOfListMap.get(p.getProdId()) != null?accessoryOfListMap.get(p.getProdId()):new LinkedList<ProductDTO>();
			addEnvironment(accessoryOfList);
			p.setAccessoryOf(accessoryOfList);
			
			List<ProductDTO> productMarketingRelatedList = productMarketingRelatedListMap.get(p.getProdId()) != null?productMarketingRelatedListMap.get(p.getProdId()):new LinkedList<ProductDTO>();
			addEnvironment(productMarketingRelatedList);
			p.setProductMarketingRelated(productMarketingRelatedList);
			
			List<ProductDTO> productMarketingRelatedListReverse = productMarketingRelatedListMapReverse.get(p.getProdId()) != null?productMarketingRelatedListMapReverse.get(p.getProdId()):new LinkedList<ProductDTO>();
			addEnvironment(productMarketingRelatedListReverse);
			p.setProductMarketingRelatedReverse(productMarketingRelatedListReverse);
			
			retList.add(p);
		}
		
		return retList;
	}

	private void addEnvironment(List<ProductDTO> listOfProducts) {
		for(ProductDTO prod : listOfProducts)
		{
			prod.setEnvironment(teamsiteEnvironment.getEnvironment());
		}
	}

	//Getters Setters
	public void setProductDTO(ProductDTO productDTO) {
		this.productDTO = productDTO;
	}
	public void setProductDTO(List<ProductDTO> products) {
		this.products = products;
	}
	public void setAccessories(List<ProductDTO> accessories) {
		this.accessories = accessories;
	}
	public void setRelatedProducts(List<ProductDTO> relatedProducts) {
		this.relatedProducts = relatedProducts;
	}
	public void setBillOfMaterials(List<ProductDTO> billOfMaterials) {
		this.billOfMaterials = billOfMaterials;
	}

	public void setProduct2accessories(List<Product2Accessory> product2accessories) {
		this.product2accessories = product2accessories;
	}

	public void setProduct2product(List<Product2Product> product2product) {
		this.product2product = product2product;
	}

	public void setProduct2bom(List<Product2Bom> product2bom) {
		this.product2bom = product2bom;
	}

	public List<ProductDTO> getAccessoryOf() {
		return accessoryOf;
	}

	public void setAccessoryOf(List<ProductDTO> accessoryOf) {
		this.accessoryOf = accessoryOf;
	}

	public List<Product2Accessory> getAccessory2Product() {
		return accessory2Product;
	}

	public void setAccessory2Product(List<Product2Accessory> accessory2product) {
		this.accessory2Product = accessory2product;
	}

	public List<ProductDTO> getMarketingRelatedProducts() {
		return marketingRelatedProducts;
	}

	public void setMarketingRelatedProducts(
			List<ProductDTO> marketingRelatedProducts) {
		this.marketingRelatedProducts = marketingRelatedProducts;
	}

	public List<Product2ProductMarketingRelated> getProduct2marketingRelatedProduct() {
		return product2marketingRelatedProduct;
	}

	public void setProduct2marketingRelatedProduct(
			List<Product2ProductMarketingRelated> product2marketingRelatedProduct) {
		this.product2marketingRelatedProduct = product2marketingRelatedProduct;
	}

	public void setTeamsiteEnvironment(TeamsiteEnvironment teamsiteEnvironment) {
		this.teamsiteEnvironment = teamsiteEnvironment;
	}

	public List<ProductDTO> getMarketingRelatedProductsReverse() {
		return marketingRelatedProductsReverse;
	}

	public void setMarketingRelatedProductsReverse(
			List<ProductDTO> marketingRelatedProductsReverse) {
		this.marketingRelatedProductsReverse = marketingRelatedProductsReverse;
	}

	public List<Product2ProductMarketingRelated> getProduct2marketingRelatedProductReverse() {
		return product2marketingRelatedProductReverse;
	}

	public void setProduct2marketingRelatedProductReverse(
			List<Product2ProductMarketingRelated> product2marketingRelatedProductReverse) {
		this.product2marketingRelatedProductReverse = product2marketingRelatedProductReverse;
	}

}
