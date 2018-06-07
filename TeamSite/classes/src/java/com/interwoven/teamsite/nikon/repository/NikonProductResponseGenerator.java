package com.interwoven.teamsite.nikon.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.livesite.spring.ApplicationContextUtils;
import com.interwoven.teamsite.ext.common.TeamsiteEnvironment;
import com.interwoven.teamsite.ext.util.FormatUtils;
import com.interwoven.teamsite.nikon.common.NikonHBN8ParamConstants;
import com.interwoven.teamsite.nikon.dto.HBN8QueryParamDTO;
import com.interwoven.teamsite.nikon.externals.NikonLiveSiteHBN8ExternalDelegate;
import com.interwoven.teamsite.nikon.springx.NikonBusinessManager;

public class NikonProductResponseGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 5){
			System.out.println("Usage: It should have 5 parameters:\n" +
					"- The path to the spring configuration file\n" +
					"- The input file, which contains a list of product ids\n" +
					"- The output file, the program will update with a list of files has been modified\n"+
					"- Locale it try to generate\n" +
					"- Source path, where the DCR located (WWA WORKAREA?)");
			System.exit(0);
		}
		String inputFile = args[1];
		String outputFile = args[2];
		String locale = args[3];
		String sourcePath = args[4];
		
		ApplicationContextUtils.createApplicationContext(args[0]);
		ApplicationContext context = ApplicationContextUtils.getApplicationContext();
		try {
			generateProductList(context, locale, sourcePath, inputFile, outputFile);
		}catch (Exception e){
			e.printStackTrace();
		}
		System.exit(0);

	}

	private static void generateProductList(ApplicationContext context,
			String locale, String sourcePath, String inputFilePath, String outputFilePath) throws Exception {
		System.out.println("Processing file ["+inputFilePath+"]");
		File inputFile = new File(inputFilePath);
		if (!inputFile.exists()){
			throw new Exception("Input file does not exist, please check the path and try again");
		}else{
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			List<String> prodIds = new ArrayList<String>();
			while ((line = reader.readLine()) != null){
				if (!"".equals(line)){
					prodIds.add(line);
				}
			}
			File outputFile = new File(outputFilePath);
			if (!outputFile.exists()){
				if (!outputFile.getParentFile().exists()){
					if (!outputFile.getParentFile().mkdirs()){
						throw new Exception("Cannot create outputFile directory");
					}
				}
				if (!outputFile.createNewFile()){
					throw new Exception("Cannot create outputFile ["+outputFilePath+"]");
				}
			}
			PrintWriter writer = new PrintWriter(outputFile);
			NikonBusinessManager businessManager = (NikonBusinessManager) context.getBean("nikon.hibernate.dao.manager");
			NikonRepository repo = (NikonRepository) context.getBean("nikon.Repository");
			TeamsiteEnvironment env = (TeamsiteEnvironment) context.getBean("nikon.teamsite.Environment");
			NikonLiveSiteHBN8ExternalDelegate delegate = new NikonLiveSiteHBN8ExternalDelegate(businessManager, env, repo);
			HBN8QueryParamDTO param = new HBN8QueryParamDTO(null, (TeamsiteEnvironment) context.getBean("nikon.teamsite.Environment")); // this should change in later stage
			param.setTeamsiteEnvironment(env);
			param.setMode(NikonHBN8ParamConstants.MODE_GENERATE);
			param.setRepo(repo);
			param.setEnableWWAFilter(false);
			param.setSourcePath(sourcePath);
			param.setNikonLocale(locale);
			param.setSiteCountryCode(FormatUtils.countryCode(param.getNikonLocale()));		
			param.setLanguageCode(FormatUtils.languageCode(param.getNikonLocale()));
			param.setCountryCode(FormatUtils.countryCode(param.getNikonLocale()));
			ArrayList<String> localeList = new ArrayList<String>(); 
			for (String prodId : prodIds){
				param.setProductId(prodId);
				try {
					Document document = delegate.listProductDetails(param, null);
					if (document != null){
						Node node = document.selectSingleNode("//updatedRelationshipFile");
						if (node != null){
							String idList = node.getText();
							if (idList != null && !"".equals(idList)){
								String[] ids = idList.split(",");
								for (String id : ids){
									writer.println(id);
								}
							}
						}
						node = document.selectSingleNode("//productDCRPath");
						if (node != null){
							String dcrPath = node.getText();
							writer.println(prodId + "_fragment.xml|"+ dcrPath);
							writer.println(prodId + ".xml|"+ dcrPath);
							writer.println(prodId + "_org.xml|"+ dcrPath);
							writer.println(prodId + "_relationship.properties|"+ dcrPath);
						}else{
							System.out.println("No node for productDCRPath ["+prodId+"], probably unable to find the product");
						}
					}else{
						System.out.println("No Document response for Product id: ["+prodId+"]");
					}
				}catch (Exception e){
					System.out.println("####ERROR: Exception while trying to generate repsonse for ["+prodId+"]");
					e.printStackTrace();
				}
			}
			
			writer.flush();
			writer.close();
			
			
			
		}
		
	}

}
