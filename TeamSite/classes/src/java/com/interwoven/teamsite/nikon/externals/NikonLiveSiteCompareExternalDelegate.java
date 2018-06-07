package com.interwoven.teamsite.nikon.externals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.nikon.common.NikonDomainConstants;
import com.interwoven.teamsite.nikon.components.ComponentHelper;
import com.interwoven.teamsite.nikon.components.ComponentHelper.LocalisedDCRTO;
import com.interwoven.teamsite.nikon.hibernate.manager.NikonHBN8DAOManager;

@SuppressWarnings("deprecation")
public class NikonLiveSiteCompareExternalDelegate extends NikonLiveSiteBaseDelegate {

	public NikonLiveSiteCompareExternalDelegate(){}

	public NikonLiveSiteCompareExternalDelegate(NikonHBN8DAOManager dm, TeamsiteEnvironment env) {
		super();
		this.dm = dm;
		this.environment = env;
	}
	
	/**
	 * Method to return compare data on given products
	 * @param  requestContext
	 * @return XMLDocument
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public Document listCompareDetails(RequestContext requestContext) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		log.debug("Entering Document listCompareDetails(RequestContext requestContext)");
		
		Document doc = _listCompareDetails(requestContext);
		
		return doc;
		
	}
	
	/**
	 * Method to build the XML/JSON response for compare data on given products
	 * @param  param
	 * @param  requestContext
	 * @return XMLDocument
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Document _listCompareDetails(RequestContext requestContext) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		//-----------------------------------------------------------
		// Initialise Variables
		//-----------------------------------------------------------
		Document returnDocument = Dom4jUtils.newDocument();
        
		NikonLiveSiteHBN8ExternalDelegate HBN8ExternalDelegate = new NikonLiveSiteHBN8ExternalDelegate();
		
		List<Map<String,String>> mapProductDCRs = new ArrayList<Map<String,String>>();
		List<Map<String,String>> mapProductSpecs = new ArrayList<Map<String,String>>();
		List<ObjectNode> mapProductColors = new ArrayList<ObjectNode>();
		
		//-----------------------------------------------------------
		// Retrieve Parameters From Query String
		//-----------------------------------------------------------
        String nikonLocale = requestContext.getParameterString("locale");
        String[] compareIds = requestContext.getParameterString("prodNumbers").split(",");
        String jqueryCallback = requestContext.getParameterString("callback");
        
        if (((requestContext.getParameterString("prodNumbers") != null) && (nikonLocale != null)) && 
            ((!requestContext.getParameterString("prodNumbers").equals("")) && (!nikonLocale.equals("")))) {
        	
	        //-----------------------------------------------------------
			// Process Each DCR
	        //-----------------------------------------------------------
	        for (int i = 0; i < compareIds.length; i++) {
	        
	        	//-----------------------------------------------
				// DCR Variables
				//-----------------------------------------------
				Map<String, String> productInfo = new TreeMap<String, String>();
				LinkedHashMap<String,String> productSpecifications = new LinkedHashMap<String, String>();
				
	        	Document productDoc = HBN8ExternalDelegate.listCompareProductDetailsFromIdCode(compareIds[i], nikonLocale, requestContext);
	        	
	        	Element rootElement = productDoc.getRootElement();
	        	Node productNode = rootElement.selectSingleNode("Product");
	        	Node productDetailsNode = rootElement.selectSingleNode("product_details");
	        	
	        	// Product ID (Key - Product Id + _ + Locale)
	        	productInfo.put("Id", compareIds[i] + "_" + nikonLocale);
	        	
	        	// Product Number (Product Id)
	        	productInfo.put("Number", compareIds[i]);
	        	
	        	// Product Name (Meta / HBN8)
	        	if (productNode.selectSingleNode("@prodShortName") != null)
				{
	        		productInfo.put("Title",  productNode.selectSingleNode("@prodShortName").getText() + "");
				}
	        	
				// Product Type (Meta / HBN8)
	        	if (productNode.selectSingleNode("@type") != null)
				{
	        		productInfo.put("ProdType", productNode.selectSingleNode("@type").getText() + "");
				}
	        	
				// Product Category (Meta / HBN8)
	        	if (productNode.selectSingleNode("@productCategory") != null)
				{
	        		productInfo.put("ProdCategory", productNode.selectSingleNode("@productCategory").getText() + "");
				}
	        	
				// Product NavCat1 (Meta / HBN8)
				if ((productNode.selectSingleNode("@navCat1") != null) && (!productNode.selectSingleNode("@navCat1").getText().equals("")))
				{
					productInfo.put("ProdNavCat1", productNode.selectSingleNode("@navCat1").getText() + "");
				}
				
				// Product NavCat2 (Meta / HBN8)
				if ((productNode.selectSingleNode("@navCat2") != null) && (!productNode.selectSingleNode("@navCat2").getText().equals("")))
				{
					productInfo.put("ProdNavCat2", productNode.selectSingleNode("@navCat2").getText() + "");
				}
				
				// Product NavCat3 (Meta / HBN8)
				if ((productNode.selectSingleNode("@navCat3") != null) && (!productNode.selectSingleNode("@navCat3").getText().equals("")))
				{
					productInfo.put("ProdNavCat3", productNode.selectSingleNode("@navCat3").getText() + "");
				}
				
				// Product URL
				productInfo.put("URL",  "/" + nikonLocale + "/products/product_details.page?RunQuery=l3&ID=" + compareIds[i]);
	        	      
	        	// Product Overview Image (DCR)
			    if ((productDetailsNode.selectSingleNode("filter_image_source") != null) && (productDetailsNode.selectSingleNode("filter_image_source").getText().equals("category_image"))){
			    		productInfo.put("ViewImage",  "/" + productDetailsNode.selectSingleNode("category_image").getText());
				}
			    else{
			    	if (productDetailsNode.selectSingleNode("prod_overview_image") != null) {						
						productInfo.put("ViewImage",  "/" + productDetailsNode.selectSingleNode("prod_overview_image").getText());
					}
					else if (productDetailsNode.selectSingleNode("overview_image") != null) {
						productInfo.put("ViewImage",  "/" + productDetailsNode.selectSingleNode("overview_image").getText());
					}
			    }
			    
			    
			    // Product Specifications (DCR)
				if (productDetailsNode.selectSingleNode("specifications") != null) {
					   
					  List <Node> accessoriesNodes = productDetailsNode.selectNodes("specifications");
					  
					  for ( Node n : accessoriesNodes )
					  {
						 if ((n.selectSingleNode("name") != null) && (n.selectSingleNode("description") != null)) {
						
							 String fullSpec = n.selectSingleNode("name").getText().trim();
							        fullSpec = fullSpec.replaceAll("\\*\\d*$", "");
							        fullSpec = fullSpec.replaceAll("<sup>.*</sup>", "");
							        fullSpec = fullSpec.replaceAll("(.*) \\(.*\\)", "$1");
							        //fullSpec = lowercase(fullSpec);
							        //fullSpec = capitalise(fullSpec);
							        
							 productSpecifications.put(fullSpec, escapeText(n.selectSingleNode("description").getText()));
						 }
						              					  
					  }			   
				}
				
				// Product Colors (DCR)
				if (productDetailsNode.selectSingleNode("Colourways") != null) {
	  				   
       				List <Node> viewsNodes = productDetailsNode.selectNodes("Colourways");
   				  
       				ObjectMapper mapper = new ObjectMapper();
       				ObjectNode rootObj = mapper.createObjectNode();
       				
       				ArrayNode colorsArray = rootObj.putArray("ColorVariants");
       				
       				int count = 0;
   				  	
   				  	for ( Node n : viewsNodes )
   				  	{
   				  		if (((n.selectSingleNode("title") != null) && (!n.selectSingleNode("title").getText().equals(""))) && 
   				  			((n.selectSingleNode("swatch_image") != null) && (!n.selectSingleNode("swatch_image").getText().equals(""))) && 
   				  			((n.selectSingleNode("image") != null) && (!n.selectSingleNode("image").getText().equals("")))) {
   				  			
   				  			ObjectNode colorArrayList = colorsArray.addObject();
   				  			
   				  			if (count == 0) {
   				  				
   				  				colorArrayList.put("Active", true);
   				  			}
   				  			
   				  			ObjectNode colorList = colorArrayList.putObject("Color");
   				  		
   				  			colorList.put("Name", n.selectSingleNode("title").getText());
   				  			colorList.put("Swatch", n.selectSingleNode("swatch_image").getText());
   				  			
   				  			ObjectNode colorProductList = colorArrayList.putObject("Product");
   				  		
   				  			colorProductList.put("Id", compareIds[i] + "_" + nikonLocale + "_" + count);
   				  			colorProductList.put("Number", compareIds[i] + "_" + count);
   				  			colorProductList.put("Title",  productNode.selectSingleNode("@prodShortName").getText() + "");
   				  			colorProductList.put("URL",  "/" + nikonLocale + "/products/product_details.page?RunQuery=l3&ID=" + compareIds[i]);
   				  			colorProductList.put("ViewImage", n.selectSingleNode("image").getText());
   				  			colorProductList.put("AuthorizedDealersButton", true);
   				  			
   				  			count++;
   				  		}
   				  	}
   				  	
   					// Add Product Colors
   					mapProductColors.add(rootObj);
   			    
   			    } else {
   			    	
   			    	ObjectMapper mapper = new ObjectMapper();
       				ObjectNode rootObj = mapper.createObjectNode();
       				
       				rootObj.putArray("ColorVariants");
       				
       				// Add Product Colors
   					mapProductColors.add(rootObj);
   			    }
				
				// Add Product Info
				mapProductDCRs.add(productInfo);
				
				// Add Product Specifications
				mapProductSpecs.add(productSpecifications);

	        }
	        
	        //=================================================================
			// Create Set of Specification Keys Based On DCRs
			//=================================================================
			LinkedHashSet<String> sharedKeys = new LinkedHashSet<String>();
			
			for (Map<String, String> map : mapProductSpecs) {
	
				for (Map.Entry entry : map.entrySet()) {
						
					String fullSpec = entry.getKey().toString();
						   
					sharedKeys.add(fullSpec);
						
				}			
			}
			
			LinkedHashMap<String, String> allKeys = new LinkedHashMap<String, String>();
			Iterator<String> it = sharedKeys.iterator();
	
			MessageDigest digest = MessageDigest.getInstance("MD5");
			while(it.hasNext()) {
				String key = it.next().toString();
				StringBuffer sb = new StringBuffer();
				byte[] byteArr = digest.digest(key.getBytes("UTF-8"));
				for (byte b : byteArr) {
					sb.append(Integer.toHexString((int) (b & 0xff)));
				}				
				allKeys.put(key, sb.toString());
			}
			
			//=================================================================
			// Create JSON Response
			//=================================================================
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootObj = mapper.createObjectNode();
			
			// Product List
			ArrayNode productListObj = rootObj.putArray("ProductList");
			
			// Loop Product DCR Info
			int count = 0;
			
			for (Map<String, String> map : mapProductDCRs) {
	
				ObjectNode productList = productListObj.addObject();
				
				// Add DCR General Info
				for (Map.Entry entry : map.entrySet()) {
					
					if ((!entry.getKey().toString().equals("ProdCategory")) &&
						(!entry.getKey().toString().equals("ProdNavCat1")) &&
						(!entry.getKey().toString().equals("ProdNavCat2")) &&
						(!entry.getKey().toString().equals("ProdNavCat3"))) 
					{
						
						productList.put(entry.getKey().toString(), entry.getValue().toString());
					}
				}
				
				// Product Authorized Dealers Button
				productList.put("AuthorizedDealersButton", true);  	
				
				// Category Data
				ObjectNode categoryList = productList.putObject("ContentCategory");
				
				if (map.get("ProdCategory") != null) {
					
					categoryList.put("ProdCategory", map.get("ProdCategory"));
				}
				
				if (map.get("ProdNavCat1") != null) {
				
					categoryList.put("ProdNavCat1", map.get("ProdNavCat1"));
				}
				
				if (map.get("ProdNavCat2") != null) {
				
					categoryList.put("ProdNavCat2", map.get("ProdNavCat2"));
				}
				
				if (map.get("ProdNavCat3") != null) {
				
					categoryList.put("ProdNavCat3", map.get("ProdNavCat3"));
				}
				
				// Colors
				productList.putAll(mapProductColors.get(count));
				System.out.println("Product Count: " + count);
				System.out.println("Colors: " + mapProductColors.size());
				
				// Tech Specs
				ObjectNode techSpecsList = productList.putObject("TechSpecs");
				
				for (Map.Entry specEntry : mapProductSpecs.get(count).entrySet()) {
					
					ObjectNode id = techSpecsList.putObject(allKeys.get(specEntry.getKey().toString()));
					
					ArrayNode groupsArray = id.putArray("groups");
					
					ObjectNode groupArrayObject = groupsArray.addObject();
					
					groupArrayObject.put("prefix", "");
					groupArrayObject.put("suffix", "");
					
					ArrayNode valuesArray = groupArrayObject.putArray("values");
					valuesArray.add(escapeText(specEntry.getValue().toString()));
					
					groupArrayObject.put("notes", "");
				}
				
				count++;
			}
	
			// Full Specs
			ArrayNode fullSpecsArray = rootObj.putArray("FullSpecs");
			
			for (String key : allKeys.keySet()) {
				
				ObjectNode fullSpecsArrayObject = fullSpecsArray.addObject();
				
				fullSpecsArrayObject.put("Name", key);
				
				fullSpecsArrayObject.putArray("Ids").add(allKeys.get(key));
			}
			
			// Glance Specs
			ArrayList<String> glanceSpecValues = retrieveGlanceSpecKeys(requestContext, nikonLocale);
			
			ArrayNode glanceSpecsArray = rootObj.putArray("GlanceSpecs");
			
			for (int k = 0; k < glanceSpecValues.size(); k++) {
				
				String id = allKeys.get(glanceSpecValues.get(k));
			
				if (id != null) {	
				
					ObjectNode glanceSpecsArrayObject = glanceSpecsArray.addObject();
							   glanceSpecsArrayObject.put("Name", glanceSpecValues.get(k));
							   glanceSpecsArrayObject.putArray("Ids").add(id);
				}
			}
	
			// Reviews
			rootObj.putObject("Reviews");
			
			//=================================================================
			// JSON Output
			//=================================================================	
			if (jqueryCallback != null) {
				
				returnDocument.addElement("JSON").setText(jqueryCallback + "(" + rootObj.toString() + ")");
			
			} else {
				
				returnDocument.addElement("JSON").setText(rootObj.toString());
			}
		
        } else {
        	
        	if (jqueryCallback != null) {
				
				returnDocument.addElement("JSON").setText(jqueryCallback + "({ \"error\": {\"message\": \"An Error Occurred\", \"details\": \"Id or Locale Not Set\"}}" + ")");
			
			} else {
				
				returnDocument.addElement("JSON").setText("{ \"error\": {\"message\": \"An Error Occurred\", \"details\": \"Id or Locale Not Set\"}}");
			}
        }
	
		return returnDocument;
	}
	
	/**
	 * Method to retrieve glance specification keys from locale DCR.
	 * @param  requestContext
	 * @param  nikonLocale
	 * @return ArrayList
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> retrieveGlanceSpecKeys(RequestContext requestContext, String nikonLocale) {
		
		ArrayList<String> glanceSpecKeys = new ArrayList<String>();
		
		String defaultLangGlanceSpecs = NikonDomainConstants.DEFAULT_COMPARE_GLANCE_SPECS;
		
		ComponentHelper ch = new ComponentHelper();
		
		LocalisedDCRTO lDCRTo = ch.localisedPathToDCR(requestContext, defaultLangGlanceSpecs, nikonLocale);
		
		String fullyLocalisedDCRPath = lDCRTo.getFullyLocalisedDCRPath();
	
		log.debug("retrieveGlanceSpecs - localisedDCRPath" + fullyLocalisedDCRPath);
			
		File f = new File(fullyLocalisedDCRPath);
	
		if (f.exists() && f.isFile())
		{
			try {
				
				InputStream is = requestContext.getFileDal().getStream(fullyLocalisedDCRPath);
				
				Document localisedGlanceSpecsXML = Dom4jUtils.newDocument(is);
				
				Element rootElement = localisedGlanceSpecsXML.getRootElement();
	        	
				if (rootElement.selectNodes("glance_spec").size() > 0) {
	        	
					List <Node> glanceSpecNodes = rootElement.selectNodes("glance_spec");
				  
					for ( Node n : glanceSpecNodes )
					{
						if (n.selectSingleNode("string_key") != null) {
					
							String glanceSpec = n.selectSingleNode("string_key").getText();
								   glanceSpec = glanceSpec.replaceAll("\\*\\d*$", "");
							       glanceSpec = glanceSpec.replaceAll("<sup>.*</sup>", "");
							       glanceSpec = glanceSpec.replaceAll("(.*) \\(.*\\)", "$1");
							       glanceSpec = lowercase(glanceSpec);
							       glanceSpec = capitalise(glanceSpec);
							          
							glanceSpecKeys.add(glanceSpec);
						}           					  
					}
				}
				
				is.close();
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		return glanceSpecKeys;
		
	}
	
	//====================================================================
	// lowercase()
	//====================================================================
	public static String lowercase(final String string) 
	{
		if (string == null)
			return string;
		if (string.equals(""))
			return string;
		
		return string.toLowerCase();
	}
	
	//====================================================================
	// capitalise()
	//====================================================================
	public static String capitalise(final String string)
	{
		if (string == null)
			return string;
		if (string.equals(""))
			return string;
		
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}
	
	//====================================================================
	// escapeText()
	//====================================================================
	public String escapeText(String text) {
		
		String escapedText = StringEscapeUtils.unescapeHtml(text);
	           escapedText = StringEscapeUtils.unescapeHtml(escapedText);
	           escapedText = StringEscapeUtils.unescapeHtml(escapedText);
	       
	    return escapedText;
	}
}
