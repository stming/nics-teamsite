package com.interwoven.teamsite.nikon.util;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.interwoven.teamsite.nikon.data.DataAccessException;
import com.interwoven.teamsite.nikon.data.ProductManager;
import com.interwoven.teamsite.nikon.dto.ProductDTO;
import com.interwoven.teamsite.nikon.hibernate.beans.Product;

public class FriendlyURLGenerator {

	public static final String PREFIX = "product";
	
	public static final String DEFAULT_PRODUCT_PAGE = "products/product_details.page";

	public static final XPathFactory PATHFACTORY = XPathFactory.newInstance();

    public static String replaceSpecialCharacters(String input)
    {
       return input.replaceAll("&", "%26").trim();
    }
    
    public static String replaceTemporaryFix(String cat)
    {
        if("Fixed Focus".equals(cat))
            return "Single Focal Length";
        else
            return cat;
    }
    
	public static String getShortURL(String locale, Product product) {
		//codes for NICS only, so comment out here
		//String strDCRName = product.getPath();
		//strDCRName = strDCRName.replaceAll("^.*\\\\([^\\\\]+)$","$1");

		String url = "";
		url = "/" + locale + "/" + 
			normalizePath(replaceTemporaryFix(product.getProductCategory())) + "/" + 
			normalizePath(replaceTemporaryFix(product.getNavCat1())) + "/" + 
			normalizePath(replaceTemporaryFix(product.getNavCat2())) + "/" + 
			normalizePath(replaceTemporaryFix(product.getNavCat3())) + "/" + 
			//normalizePath(replaceTemporaryFix(strDCRName));
			normalizePath(replaceTemporaryFix(product.getProdShortName()));
		url = url.replaceAll("[\\/]{2,}", "/"); // Remove empty path name
		url = url.replaceAll("[\\-]{2,}", "/"); // Remove empty path name
		return url;
	}
	
	public static String getLongURL (String locale, Product product, Document productHierarchy) throws Exception {
		String url = "";
		XPath xpath = PATHFACTORY.newXPath();
		String category = product.getProductCategory();
		XPathExpression expression = xpath.compile("//catalogue/ParamValue[text()='"+category+"']/../CatalogueSection");
		Node node = (Node) expression.evaluate(productHierarchy, XPathConstants.NODE);
		url = "/" + locale + "/" + DEFAULT_PRODUCT_PAGE + "?sParamValueLbl=" + ((node == null) ? replaceSpecialCharacters(category) : replaceSpecialCharacters(node.getTextContent())) + "&ParamValue=" + replaceSpecialCharacters(category);
		String runQuery = "l1";
		if (product.getNavCat1() != null) {
			expression = xpath.compile("//catalogue/ParamValue[text()='"+category+"']/../Sub1/Subnav1Param[text()='"+product.getNavCat1()+"']/../Subnav1");
			node = (Node) expression.evaluate(productHierarchy, XPathConstants.NODE);
			url += "&sParam1ValueLbl=" + ((node == null) ? replaceSpecialCharacters(product.getNavCat1()) : replaceSpecialCharacters(node.getTextContent())) + "&Subnav1Param=" + replaceSpecialCharacters(product.getNavCat1());
			runQuery = "l2";
		}else{
			url += "&Subnav1Param=0";
		}
		if (product.getNavCat2() != null) {
			expression = xpath.compile("//catalogue/ParamValue[text()='"+category+"']/../Sub1/Subnav1Param[text()='"+product.getNavCat1()+"']/../Sub2/Subnav2Param[text()='"+product.getNavCat2()+"']/../Subnav2");
			node = (Node) expression.evaluate(productHierarchy, XPathConstants.NODE);
			url += "&sSubnav2ParamLbl=" + ((node == null) ? replaceSpecialCharacters(product.getNavCat2()) : replaceSpecialCharacters(node.getTextContent())) + "&Subnav2Param=" + replaceSpecialCharacters(product.getNavCat2());
			runQuery = "l3";
		}else{
			url += "&Subnav2Param=0";
		}
		if (product.getNavCat3() != null) {
			expression = xpath.compile("//catalogue/ParamValue[text()='"+category+"']/../Sub1/Subnav1Param[text()='"+product.getNavCat1()+"']/../Sub2/Subnav2Param[text()='"+product.getNavCat2()+"']/../Sub3/Subnav3Param[text()='"+product.getNavCat3()+"']/../Subnav3");
			node = (Node) expression.evaluate(productHierarchy, XPathConstants.NODE);
			url += "&sSubnav3ParamLbl=" + ((node == null) ? replaceSpecialCharacters(product.getNavCat3()) : replaceSpecialCharacters(node.getTextContent())) + "&Subnav3Param=" + replaceSpecialCharacters(product.getNavCat3());
			runQuery = "l4";
		}else{
			url += "&Subnav3Param=0";
		}
		url += "&RunQuery=" + runQuery + "&ID="+product.getProdId();
		url = url.replace(" " , "+");
		return url;
	}
	
	public static String normalizePath(String path) {
		if ("".equals(path) || path == null || "null".equals(path)){
			return "";
		}else{
			return replaceAccentLetters(path.toLowerCase().replaceAll("[ \\+\\#\\/\\(\\)\\,\\&\\:\\.]", "-").replaceAll("\\-$", ""));
		}
	}
	
	public static String replaceAccentLetters (String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
	
	public static void main(String[] args) {
		System.out.println("WARNING: This is just a wrapper to the friendly URL generator, " +
				"this should ideally run in the workflow or some other mechanism. But until we" +
				" integrate this process into the workflow, we will relay on the command line " +
				"to generate the rewrite map");
		
		if (args.length < 3){
			System.out.println("Usage: It should have 3 parameters:\n" +
					"- The path to the spring configuration file\n" +
					"- The directory where the locale product hierarchy DCR located\n" +
					"- The output directory");
			System.exit(0);
		}
		ApplicationContext context = new FileSystemXmlApplicationContext(args[0]);
		try {
			generateLocale(context, args[1], args[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void generateLocale(ApplicationContext context, String localeHierarchy, String outputDirectory) throws Exception {
		ProductManager pManager = (ProductManager) context.getBean("nikon.teamsite.data.ProductManager");
		List<String> allLocale = (List<String>) context.getBean("availableLocale");
		List<Product> productList = pManager.retrieveProductsByLocale(allLocale.get(0));
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		for (int i=0;i<allLocale.size();i++){
			Document localeDoc = builder.parse(localeHierarchy + "/" + allLocale.get(i));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDirectory + "/" +  allLocale.get(i) + "_rewritemap.txt"), "UTF-8"));
			System.out.println("Processing ["+allLocale.get(i)+"]\n");
			
			String previousProductCat = "";
			for (int j=0;j<productList.size();j++){
				if (productList.get(j).getProductCategory() != null){
					if (!"".equals(previousProductCat) && !previousProductCat.equalsIgnoreCase(productList.get(j).getProductCategory())) {
						writer.write("\n");
					}
					if (!productList.get(j).isKit()){
						String shortURL = (FriendlyURLGenerator.getShortURL(allLocale.get(i), productList.get(j)));
						String longURL = (FriendlyURLGenerator.getLongURL(allLocale.get(i), productList.get(j), localeDoc));
						writer.write(shortURL.replace("/" + allLocale.get(i) + "/", "") + "  " + longURL + "\n");
						writer.write(productList.get(j).getProdId() + "  " + shortURL.replace("/" + allLocale.get(i) + "/", "") + "\n");
					}
					previousProductCat = ((productList.get(j).getProductCategory() != null) ? productList.get(j).getProductCategory(): "");
				}else{
					System.out.println("Skipping product because product category is null:  " + productList.get(j).getId());
					
				}
				
			}
			writer.flush();
			writer.close();
		}
	}
	
}
